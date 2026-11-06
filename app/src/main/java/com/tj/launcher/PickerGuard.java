package com.tj.launcher;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Locale;

/**
 * AO1 (Tj, 2026-11-06: "When the wallpaper selector shows up, permanently remove 'my photos' as an option. It is at the very left of the wallpapers. This should never appear").
 * The wallpaper selector is the system's own app: its tiles cannot be edited from here. While it is in front in the custom launcher the helper finds the tile by its label ("My photos"), covers it with an opaque
 * window in the picker's own background colour (read from the picker's theme) that also swallows the touches, and follows the tile with a one-node refresh (no tree search while it stays on screen).
 * The first look at every picker visit is also written to the tablet report ({@link #DUMP_KEY}): the real labels, ids and sizes of THIS tablet's picker, so the cover can be made exact where it is not.
 * Best effort and fail-open: anything unexpected removes the cover; a failsafe removes it a few seconds after the picker was last seen.
 */
final class PickerGuard {
    private static final String TAG = "TjPickerGuard";
    static final String KEY = "hide_my_photos";            // switch, default ON
    static final String DUMP_KEY = "picker_dump";            // the picker's visible nodes, one line (tablet report)
    static final String STATUS_KEY = "picker_status";        // what the guard did last (Launcher control and report)
    private static final String LABEL = "my photos";
    private static final long MIN_GAP_MS = 120, TRAILING_MS = 160, FAILSAFE_MS = 4000, DUMP_GAP_MS = 30_000, PKG_TTL_MS = 10 * 60_000;

    private final AccessibilityService svc;
    private final android.os.Handler handler;
    private View cover;
    private final Rect coverAt = new Rect();
    private AccessibilityNodeInfo tile;          // the "My photos" tile, refreshed (not searched) while it stays on screen
    private long lastRunMs = 0, lastDumpMs = -DUMP_GAP_MS, lastSeenMs = 0, pkgAt = -PKG_TTL_MS;
    private boolean pending = false;
    private String pickerPkg = "";
    private ComponentName pickerComp;
    private String label = LABEL;
    private int color = 0xFFFFFFFF;

    PickerGuard(AccessibilityService svc, android.os.Handler handler) { this.svc = svc; this.handler = handler; }

    // ---------------------------------------------------------------- pure rules (JVM-tested)

    /** Pure: is this node's label the "My photos" tile? Exact (ignoring case and spaces), on the text or on the content description. */
    static boolean isMyPhotos(CharSequence text, CharSequence desc) { return matches(LABEL, text, desc); }

    /** Pure: does the node's text or content description equal {@code label} (lower case) exactly, ignoring case and surrounding spaces? */
    static boolean matches(String label, CharSequence text, CharSequence desc) { return same(label, text) || same(label, desc); }
    private static boolean same(String label, CharSequence s) { return s != null && label.equals(s.toString().trim().toLowerCase(Locale.ROOT)); }

    /** Pure: does the guard work now? Only in the custom launcher, with its switch on, the tablet unlocked and the automation not paused by the crash breaker. */
    static boolean wanted(boolean custom, boolean switchOn, boolean locked, boolean automationOff) { return custom && switchOn && !locked && !automationOff; }

    /** Pure: the colour to cover with when the picker's theme gives none: the day / night surface. */
    static int fallbackColor(boolean night) { return night ? 0xFF202124 : 0xFFFFFFFF; }

    /**
     * Pure: the rectangle to cover: the tile ({@code tileBounds}) when it is a sensible tile (not the whole screen), else the label's own bounds grown by {@code pad}; one pixel more on every side so no edge shows; clamped to the display.
     * {l,t,r,b}; null when nothing sensible.
     */
    static int[] coverRect(int[] label, int[] tileBounds, int pad, int dispW, int dispH) {
        int[] r = null;
        if (tileBounds != null && tileBounds[2] > tileBounds[0] && tileBounds[3] > tileBounds[1]
                && (long) (tileBounds[2] - tileBounds[0]) * (tileBounds[3] - tileBounds[1]) <= (long) dispW * dispH / 2) r = tileBounds;
        else if (label != null && label[2] > label[0] && label[3] > label[1]) r = new int[] { label[0] - pad, label[1] - pad, label[2] + pad, label[3] + pad };
        if (r == null) return null;
        return new int[] { Math.max(0, r[0] - 1), Math.max(0, r[1] - 1), Math.min(dispW, r[2] + 1), Math.min(dispH, r[3] + 1) };
    }

    // ---------------------------------------------------------------- Android side

    /** Called for EVERY accessibility event (cheap: one string compare for events of other apps). Never throws. */
    void onEvent(AccessibilityEvent e) {
        try {
            if (e == null) return;
            CharSequence pkg = e.getPackageName();
            if (pkg == null) return;
            long now = android.os.SystemClock.uptimeMillis();
            if (now - pkgAt > PKG_TTL_MS) refreshPackage(now);
            if (pickerPkg.isEmpty()) return;
            if (!pickerPkg.contentEquals(pkg)) {
                // another app's window came to the front: the cover goes with the picker
                if (cover != null && e.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                        && !"com.android.systemui".contentEquals(pkg) && !svc.getPackageName().contentEquals(pkg)) remove("another app in front");
                return;
            }
            if (!wanted(Mode.isCustom(svc), Features.get(svc, KEY, true), keyguardLocked(), CrashGuard.automationOff(svc))) { if (cover != null) remove("switched off or locked"); return; }
            lastSeenMs = now;
            if (now - lastRunMs >= MIN_GAP_MS) { lastRunMs = now; run(); }
            if (!pending) { pending = true; handler.postDelayed(trailing, TRAILING_MS); }
            handler.removeCallbacks(failsafe); handler.postDelayed(failsafe, FAILSAFE_MS);
        } catch (Throwable t) { Log.e(TAG, "picker guard event failed", t); remove("error"); }
    }

    private final Runnable trailing = new Runnable() { @Override public void run() { pending = false; lastRunMs = android.os.SystemClock.uptimeMillis(); PickerGuard.this.run(); } };
    private final Runnable failsafe = new Runnable() { @Override public void run() {
        // no picker event for a while: if the picker is not the active window any more the cover must go
        try {
            AccessibilityNodeInfo root = svc.getRootInActiveWindow();
            boolean still = root != null && root.getPackageName() != null && pickerPkg.contentEquals(root.getPackageName());
            if (root != null) root.recycle();
            if (!still) remove("picker left"); else handler.postDelayed(this, FAILSAFE_MS);
        } catch (Throwable t) { remove("failsafe error"); }
    } };

    private boolean keyguardLocked() {
        try { android.app.KeyguardManager km = (android.app.KeyguardManager) svc.getSystemService(Context.KEYGUARD_SERVICE); return km != null && km.isKeyguardLocked(); }
        catch (Throwable t) { return true; }
    }

    private void refreshPackage(long now) {
        pkgAt = now;
        WallpaperPickerChoice.Result r = WallpaperPickerChoice.resolve(svc);
        pickerComp = r.component;
        pickerPkg = r.component == null ? "" : r.component.getPackageName();
        label = LABEL;
        try {   // TEST KNOB (debuggable builds only): the emulator's picker has no "My photos" tile, so the scenario points the guard at a row of the Settings app and proves the find / cover / swallow / remove machinery on it
            if ((svc.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                ComponentName tc = ComponentName.unflattenFromString(Features.getString(svc, "picker_test_comp", ""));
                String tl = Features.getString(svc, "picker_test_label", "").trim().toLowerCase(Locale.ROOT);
                if (tc != null && !tl.isEmpty()) { pickerComp = tc; pickerPkg = tc.getPackageName(); label = tl; }
            }
        } catch (Throwable ignored) { }
        if (pickerComp != null) color = themeBackground(svc, pickerComp);
    }

    /** The picker's own background colour from its theme (what its window is painted with); the day / night surface when the theme gives none. Never throws. */
    static int themeBackground(Context c, ComponentName picker) {
        boolean night = (c.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        try {
            ActivityInfo ai = c.getPackageManager().getActivityInfo(picker, 0);
            int theme = ai.getThemeResource();
            if (theme == 0) theme = ai.applicationInfo.theme;
            if (theme == 0) return fallbackColor(night);
            Context pc = c.createPackageContext(picker.getPackageName(), Context.CONTEXT_IGNORE_SECURITY).createConfigurationContext(c.getResources().getConfiguration());
            android.content.res.Resources.Theme th = new ContextThemeWrapper(pc, theme).getTheme();
            TypedValue tv = new TypedValue();
            if (th.resolveAttribute(android.R.attr.windowBackground, tv, true)) {
                if (tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) return 0xFF000000 | tv.data;
                Drawable d = pc.getResources().getDrawable(tv.resourceId, null);
                if (d instanceof ColorDrawable) return 0xFF000000 | ((ColorDrawable) d).getColor();
            }
            if (th.resolveAttribute(android.R.attr.colorBackground, tv, true) && tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) return 0xFF000000 | tv.data;
        } catch (Throwable t) { Log.w(TAG, "picker theme colour unknown: " + t); }
        return fallbackColor(night);
    }

    private void run() {
        try {
            AccessibilityNodeInfo root = svc.getRootInActiveWindow();
            if (root == null) return;
            try {
                if (root.getPackageName() == null || !pickerPkg.contentEquals(root.getPackageName())) return;
                Rect b = followTile();
                if (b == null) b = searchTile(root);
                long now = android.os.SystemClock.uptimeMillis();
                if (now - lastDumpMs > DUMP_GAP_MS) { lastDumpMs = now; dump(root, b != null); }
                if (b != null) show(b); else remove("no 'My photos' tile on this screen");
            } finally { root.recycle(); }
        } catch (Throwable t) { Log.e(TAG, "picker guard run failed", t); remove("error"); }
    }

    private Rect followTile() {
        AccessibilityNodeInfo t = tile;
        if (t == null) return null;
        try {
            if (t.refresh() && t.isVisibleToUser()) { Rect b = new Rect(); t.getBoundsInScreen(b); if (b.width() > 0 && b.height() > 0) return grow(b); }
        } catch (Throwable ignored) { }
        dropTile();
        return null;
    }

    private Rect grow(Rect b) {
        android.util.DisplayMetrics dm = svc.getResources().getDisplayMetrics();
        int[] r = coverRect(null, new int[] { b.left, b.top, b.right, b.bottom }, 0, dm.widthPixels, dm.heightPixels);
        return r == null ? null : new Rect(r[0], r[1], r[2], r[3]);
    }

    private Rect searchTile(AccessibilityNodeInfo root) {
        List<AccessibilityNodeInfo> hits = root.findAccessibilityNodeInfosByText(label);
        if (hits == null) return null;
        for (AccessibilityNodeInfo n : hits) {
            if (!matches(label, n.getText(), n.getContentDescription())) continue;
            Rect label = new Rect(); n.getBoundsInScreen(label);
            AccessibilityNodeInfo t = clickableAncestor(n);
            Rect tb = new Rect(); (t == null ? n : t).getBoundsInScreen(tb);
            android.util.DisplayMetrics dm = svc.getResources().getDisplayMetrics();
            int pad = Math.round(8 * dm.density);
            int[] r = coverRect(new int[] { label.left, label.top, label.right, label.bottom }, new int[] { tb.left, tb.top, tb.right, tb.bottom }, pad, dm.widthPixels, dm.heightPixels);
            if (r == null) continue;
            dropTile();
            try { tile = AccessibilityNodeInfo.obtain(t == null ? n : t); } catch (Throwable ignored) { tile = null; }
            Features.setString(svc, STATUS_KEY, stamp() + "  found the 'My photos' tile at " + r[0] + "," + r[1] + "," + r[2] + "," + r[3] + " and covered it (picker " + pickerPkg + ")");
            return new Rect(r[0], r[1], r[2], r[3]);
        }
        return null;
    }

    private static AccessibilityNodeInfo clickableAncestor(AccessibilityNodeInfo n) {
        AccessibilityNodeInfo cur = n;
        for (int i = 0; i < 5 && cur != null; i++) {
            if (cur.isClickable()) return cur;
            cur = cur.getParent();
        }
        return null;
    }

    private void dropTile() {
        AccessibilityNodeInfo t = tile;
        tile = null;
        if (t != null) { try { t.recycle(); } catch (Throwable ignored) { } }
    }

    private WindowManager.LayoutParams params(Rect r) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(r.width(), r.height(), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.OPAQUE);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = r.left; lp.y = r.top;
        lp.setTitle("TjPhotosCover");
        return lp;
    }

    private void show(Rect r) {
        WindowManager wm = (WindowManager) svc.getSystemService(Context.WINDOW_SERVICE);
        if (cover == null) {
            View v = new View(svc);
            v.setBackgroundColor(color);
            v.setOnTouchListener(new View.OnTouchListener() { @Override public boolean onTouch(View view, MotionEvent ev) { return true; } });   // the tile can neither be seen nor tapped
            wm.addView(v, params(r));
            cover = v; coverAt.set(r);
            Log.i(TAG, "PHOTOS cover shown " + r.toShortString() + " colour #" + Integer.toHexString(color));
        } else if (!coverAt.equals(r)) {
            wm.updateViewLayout(cover, params(r));
            coverAt.set(r);
        }
    }

    /** Removes the cover (always safe to call). {@code why} goes to the log only. */
    void remove(String why) {
        handler.removeCallbacks(trailing); handler.removeCallbacks(failsafe); pending = false;
        dropTile();
        if (cover == null) return;
        try { ((WindowManager) svc.getSystemService(Context.WINDOW_SERVICE)).removeView(cover); } catch (Throwable ignored) { }
        cover = null; coverAt.setEmpty();
        Log.i(TAG, "PHOTOS cover removed (" + why + ")");
    }

    private static String stamp() { return new java.text.SimpleDateFormat("HH:mm:ss", Locale.US).format(new java.util.Date()); }

    /** One line: the picker's visible nodes (class, label, description, id, bounds, flags), at most 120 of them and 6000 characters, for the tablet report. */
    private void dump(AccessibilityNodeInfo root, boolean found) {
        try {
            StringBuilder b = new StringBuilder();
            ArrayDeque<AccessibilityNodeInfo> q = new ArrayDeque<>(); ArrayDeque<Integer> depth = new ArrayDeque<>();
            q.add(AccessibilityNodeInfo.obtain(root)); depth.add(0);
            int n = 0;
            while (!q.isEmpty() && n < 120 && b.length() < 6000) {
                AccessibilityNodeInfo x = q.poll(); int d = depth.poll();
                if (x.isVisibleToUser()) {
                    Rect r = new Rect(); x.getBoundsInScreen(r);
                    b.append(d).append(' ').append(shortClass(x.getClassName())).append(" '").append(clip(x.getText())).append("' '").append(clip(x.getContentDescription())).append("' ")
                            .append(x.getViewIdResourceName() == null ? "-" : x.getViewIdResourceName().replace(pickerPkg + ":id/", "")).append(" [").append(r.left).append(',').append(r.top).append(',').append(r.right).append(',').append(r.bottom).append(']')
                            .append(x.isClickable() ? " C" : "").append(x.isScrollable() ? " S" : "").append(" | ");
                    n++;
                    if (d < 10) for (int i = 0; i < x.getChildCount(); i++) { AccessibilityNodeInfo ch = x.getChild(i); if (ch != null) { q.add(ch); depth.add(d + 1); } }
                }
                x.recycle();
            }
            while (!q.isEmpty()) { try { q.poll().recycle(); } catch (Throwable ignored) { } }
            Features.setString(svc, DUMP_KEY, stamp() + (found ? " (tile found) " : " (NO 'My photos' tile found) ") + pickerPkg + ": " + b);
        } catch (Throwable t) { Log.e(TAG, "picker dump failed", t); }
    }

    private static String clip(CharSequence s) { if (s == null) return ""; String t = s.toString().replace('\n', ' ').replace('|', '/').replace('\'', '`'); return t.length() > 40 ? t.substring(0, 40) : t; }
    private static String shortClass(CharSequence c) { if (c == null) return "?"; String t = c.toString(); int i = t.lastIndexOf('.'); return i >= 0 ? t.substring(i + 1) : t; }
}
