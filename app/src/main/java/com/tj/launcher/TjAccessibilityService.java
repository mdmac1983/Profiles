package com.tj.launcher;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.List;
import com.android.launcher3.R;

/**
 * Power-menu helper (L4 / G3b). Only active in custom-launcher mode, only when Tj chose a power mode, never on the lock screen.
 * HIDE_SCREENSHOT: covers the "Screenshot" entry with a panel-coloured overlay (TYPE_ACCESSIBILITY_OVERLAY, needs no overlay permission).
 * BLOCK: closes the whole power menu (Back). Fails open: any problem -> the menu is simply left alone and the overlay removed.
 */
public class TjAccessibilityService extends AccessibilityService {
    private static final String TAG = "TjA11y";
    private static final int PANEL_COLOR = Color.parseColor("#EFF1F3");   // measured on the AOSP Android 10 power menu; Tj's panel may differ slightly

    private static volatile TjAccessibilityService instance;
    /** The running helper, or null when Tj has not turned it on in Accessibility settings. */
    public static TjAccessibilityService get() { return instance; }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private View overlay;
    private final Runnable recheck = new Runnable() { @Override public void run() { evaluate(); } };

    private boolean pending = false;
    private final Rect shownAt = new Rect();
    private final Runnable debounced = new Runnable() {
        @Override public void run() {
            pending = false;
            try { evaluate(); } catch (Throwable t) { Log.e(TAG, "evaluate failed", t); removeOverlay(); }
        }
    };

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        // Never react to our own overlay (adding it fires events: that would be a feedback loop), and coalesce bursts of events into one check.
        if (event != null && getPackageName().contentEquals(event.getPackageName() == null ? "" : event.getPackageName())) return;
        scheduleLockUpdate();
        scheduleLayerRefresh();
        if (event != null) { try { guardStrayHome(event); } catch (Throwable t) { Log.e(TAG, "guard failed", t); } }
        if (Features.POWER_OFF.equals(Features.powerMode(this)) || !Mode.isCustom(this)) {
            if (overlay != null) removeOverlay();
            return;   // nothing to do and nothing scanned: zero cost when the feature is off
        }
        // Leading edge: evaluate on the FIRST event at once (the cover must appear as fast as possible), then once more shortly after to catch late content.
        long now = android.os.SystemClock.uptimeMillis();
        if (now - lastEvalMs >= 40) {
            lastEvalMs = now;
            try { evaluate(); } catch (Throwable t) { Log.e(TAG, "evaluate failed", t); removeOverlay(); }
        }
        if (!pending) { pending = true; handler.postDelayed(debounced, 80); }
    }

    private long lastEvalMs = 0;

    private static final String CARD_KEY = "power_card_rect";

    private int rotation() { return ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRotation(); }
    private String cardKey() { return CARD_KEY + "_r" + rotation(); }   // the menu sits somewhere else in landscape

    private Rect cachedCard() {
        String v = Features.getString(this, cardKey(), "");
        String[] p = v.split(",");
        if (p.length != 4) return null;
        try { return new Rect(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3])); }
        catch (NumberFormatException e) { return null; }
    }

    private boolean dialogWindowPresent(List<AccessibilityWindowInfo> windows) {
        if (windows == null) return false;
        for (AccessibilityWindowInfo w : windows) {
            CharSequence t = w.getTitle();
            if (t != null && t.toString().contains("ActionsDialog")) return true;
        }
        return false;
    }

    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        try { warmUp(); } catch (Throwable t) { Log.e(TAG, "warm failed", t); }
        try { startLockNumber(); } catch (Throwable t) { Log.e(TAG, "lock number failed", t); }
        try { startPatchWatch(); } catch (Throwable t) { Log.e(TAG, "patch watch failed", t); }
        Log.i(TAG, "connected; power mode=" + Features.powerMode(this) + " custom=" + Mode.isCustom(this));
    }

    // ---------------------------------------------------------------- W3b: wallpaper patch under the replacement card (Tj: permanent, custom launcher only)
    public static final String PATCH_STATUS_KEY = "wallpaper_patch_status";
    private View patchView;
    private android.graphics.Bitmap wallpaperBmp;
    private final Rect patchAt = new Rect();
    private int patchRot = -1;
    private boolean patchWatching = false;
    private final Runnable layerRefresh = new Runnable() { @Override public void run() { layerRefreshPending = false; refreshLayers(); } };
    private boolean layerRefreshPending = false;
    private long lastLayerMs = 0;
    private final Runnable patchRedraw = new Runnable() { @Override public void run() { if (patchView != null) patchView.invalidate(); } };
    private final android.content.SharedPreferences.OnSharedPreferenceChangeListener prefWatch = new android.content.SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override public void onSharedPreferenceChanged(android.content.SharedPreferences sp, String key) {
            if ("mode".equals(key) || Features.POWER_MODE.equals(key)) { handler.post(layerRefresh); handler.post(lockEval); }
        }
    };
    private final android.content.BroadcastReceiver wallpaperReceiver = new android.content.BroadcastReceiver() {
        @Override public void onReceive(android.content.Context c, android.content.Intent i) { wallpaperBmp = null; handler.post(new Runnable() { @Override public void run() { removeOverlay(); removePatch(); refreshLayers(); } }); }
    };

    private void startPatchWatch() {
        if (patchWatching) return;
        getSharedPreferences("tj_mode", MODE_PRIVATE).registerOnSharedPreferenceChangeListener(prefWatch);
        getSharedPreferences("tj_features", MODE_PRIVATE).registerOnSharedPreferenceChangeListener(prefWatch);
        registerReceiver(wallpaperReceiver, new android.content.IntentFilter(android.content.Intent.ACTION_WALLPAPER_CHANGED));
        patchWatching = true;
        refreshLayers();
    }

    private void stopPatchWatch() {
        if (!patchWatching) return;
        try { getSharedPreferences("tj_mode", MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(prefWatch); } catch (Throwable ignored) { }
        try { getSharedPreferences("tj_features", MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(prefWatch); } catch (Throwable ignored) { }
        try { unregisterReceiver(wallpaperReceiver); } catch (Throwable ignored) { }
        patchWatching = false;
    }

    /** Called by the launcher (same process) whenever it moves the wallpaper: redraw the patch with the new slice (debounced). */
    public void onWallpaperOffset() { handler.removeCallbacks(patchRedraw); handler.postDelayed(patchRedraw, 40); }

    private void scheduleLayerRefresh() {
        if (!Features.POWER_WALLPAPER.equals(Features.powerMode(this)) && patchView == null) return;   // not in this mode and nothing to clean up: free
        long now = android.os.SystemClock.uptimeMillis();
        if (now - lastLayerMs >= 150) { lastLayerMs = now; refreshLayers(); }
        if (!layerRefreshPending) { layerRefreshPending = true; handler.postDelayed(layerRefresh, 250); }
    }

    private void patchStatus(String s) {
        if (!s.equals(Features.getString(this, PATCH_STATUS_KEY, ""))) Features.setString(this, PATCH_STATUS_KEY, s);
    }

    /** All the reasons NOT to cover the system menu with a wallpaper patch; null = go. Each reason is shown in Launcher control. */
    private Rect patchTarget() {
        if (!Features.POWER_WALLPAPER.equals(Features.powerMode(this))) return null;
        if (!Mode.isCustom(this)) { patchStatus("off: Quickstep mode"); return null; }
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (km != null && km.isKeyguardLocked()) { patchStatus("off: lock screen"); return null; }
        Rect r = cachedCard();
        if (r == null) { patchStatus("waiting: open the power menu once so the helper learns where it sits (this screen orientation)"); return null; }
        if (loadWallpaper() == null) return null;
        return r;
    }

    private android.graphics.Bitmap loadWallpaper() {
        if (wallpaperBmp != null) return wallpaperBmp;
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            patchStatus("needs permission: Launcher control > Allow wallpaper access"); return null;
        }
        try {
            android.graphics.drawable.Drawable d = android.app.WallpaperManager.getInstance(this).getDrawable();
            if (d instanceof android.graphics.drawable.BitmapDrawable) wallpaperBmp = ((android.graphics.drawable.BitmapDrawable) d).getBitmap();
            else if (d != null && d.getIntrinsicWidth() > 0) {
                android.graphics.Bitmap b = android.graphics.Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), android.graphics.Bitmap.Config.ARGB_8888);
                d.setBounds(0, 0, b.getWidth(), b.getHeight()); d.draw(new android.graphics.Canvas(b)); wallpaperBmp = b;
            }
        } catch (Throwable t) { Log.e(TAG, "wallpaper read failed", t); }
        if (wallpaperBmp == null) patchStatus("off: the wallpaper could not be read");
        return wallpaperBmp;
    }

    /** Brings the patch (and, above it, the card) in line with the current mode / lock state / rotation. Cheap when nothing changed. */
    private void refreshLayers() {
        try {
            Rect want = patchTarget();
            if (want == null) {
                if (patchView != null) { removeOverlay(); removePatch(); }
                if (!Features.POWER_WALLPAPER.equals(Features.powerMode(this))) wallpaperBmp = null;
                if (Features.isCover(Features.powerMode(this)) && Mode.isCustom(this)) warmUp();   // plain cover mode keeps its pre-built card
                return;
            }
            if (patchView != null && (!patchAt.equals(want) || patchRot != rotation())) { removeOverlay(); removePatch(); }
            if (patchView == null) { removeOverlay(); addPatch(want); warmUp(); }
        } catch (Throwable t) { Log.e(TAG, "layer refresh failed", t); removeOverlay(); removePatch(); }
    }

    /** Used by the card creation paths: guarantees the patch exists BEFORE the card is added (later windows sit on top). */
    private void ensurePatch() {
        if (patchView != null) return;
        Rect want = patchTarget();
        if (want != null) addPatch(want);
    }

    private void addPatch(final Rect r) {
        final android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
        final Rect rect = new Rect(r);
        View v = new View(this) {
            private final android.graphics.Paint p = new android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG);
            private final android.graphics.Matrix m = new android.graphics.Matrix();
            @Override protected void onDraw(android.graphics.Canvas c) {
                android.graphics.Bitmap b = wallpaperBmp;
                if (b == null) return;
                // How Android 10's wallpaper engine places the picture: scaled to COVER the display, centred vertically, shifted sideways by the launcher's offset.
                float s = Math.max((float) dm.heightPixels / b.getHeight(), (float) dm.widthPixels / b.getWidth());
                float dw = b.getWidth() * s, dh = b.getHeight() * s;
                m.setScale(s, s);
                m.postTranslate(-(dw - dm.widthPixels) * TjHooks.wallpaperX - rect.left, -(dh - dm.heightPixels) * 0.5f - rect.top);
                c.drawBitmap(b, m, p);
            }
        };
        WindowManager.LayoutParams lp = overlayParams(r, false);
        lp.format = PixelFormat.OPAQUE;
        lp.setTitle("TjWallpaperPatch");
        ((WindowManager) getSystemService(WINDOW_SERVICE)).addView(v, lp);
        patchView = v; patchAt.set(r); patchRot = rotation();
        patchStatus("active over " + r.toShortString());
        Log.i(TAG, "PATCH shown " + r.toShortString());
    }

    private void removePatch() {
        if (patchView == null) return;
        try { ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(patchView); } catch (Throwable ignored) { }
        patchView = null; patchAt.setEmpty(); patchRot = -1;
        Log.i(TAG, "PATCH removed");
    }

    // ---------------------------------------------------------------- X1: the number on the lock screen (Tj relaxed the lock-screen rule for this)
    private View lockView;
    private boolean lockReceiverOn = false;
    private long lastLockEvalMs = 0;
    private boolean lockEvalPending = false;
    private final Runnable lockEval = new Runnable() { @Override public void run() { lockEvalPending = false; updateLockNumber(); } };
    private final android.content.BroadcastReceiver lockReceiver = new android.content.BroadcastReceiver() {
        @Override public void onReceive(android.content.Context c, android.content.Intent i) {
            handler.post(lockEval); handler.postDelayed(lockEval, 400);
            handler.post(layerRefresh);
        }
    };

    private void startLockNumber() {
        if (!lockReceiverOn) {
            android.content.IntentFilter f = new android.content.IntentFilter();
            f.addAction(android.content.Intent.ACTION_SCREEN_ON);
            f.addAction(android.content.Intent.ACTION_SCREEN_OFF);
            f.addAction(android.content.Intent.ACTION_USER_PRESENT);
            registerReceiver(lockReceiver, f);
            lockReceiverOn = true;
        }
        updateLockNumber();
    }

    private void stopLockNumber() {
        if (lockReceiverOn) { try { unregisterReceiver(lockReceiver); } catch (Throwable ignored) { } lockReceiverOn = false; }
        removeLockNumber();
    }

    /** Called on every accessibility event: leading edge at once (the number must go the moment the PIN pad appears), plus one trailing check. */
    private void scheduleLockUpdate() {
        if (!LockNumber.enabled(this) && lockView == null) return;   // feature off and nothing on screen: zero cost
        long now = android.os.SystemClock.uptimeMillis();
        if (now - lastLockEvalMs >= 60) { lastLockEvalMs = now; updateLockNumber(); }
        if (!lockEvalPending) { lockEvalPending = true; handler.postDelayed(lockEval, 120); }
    }

    private void updateLockNumber() {
        try {
            boolean enabled = LockNumber.enabled(this);
            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(POWER_SERVICE);
            KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            boolean on = pm != null && pm.isInteractive();
            boolean locked = km != null && km.isKeyguardLocked();
            boolean bouncer = enabled && on && locked && bouncerShown();
            if (LockNumber.shouldShow(enabled, on, locked, bouncer)) showLockNumber(); else removeLockNumber();
        } catch (Throwable t) { Log.e(TAG, "lock number update failed", t); removeLockNumber(); }
    }

    private static final String[] BOUNCER_IDS = {"keyguard_pin_view", "keyguard_password_view", "keyguard_pattern_view", "pinEntry", "passwordEntry", "lockPatternView"};
    private static final String[] BOUNCER_TEXTS = {"enter your pin", "enter your password", "draw your pattern", "enter pin", "enter password"};

    /** True when the PIN / password / pattern pad of the lock screen is visible (read-only look at SystemUI's windows). */
    private boolean bouncerShown() {
        List<AccessibilityWindowInfo> windows = getWindows();
        if (windows == null) return false;
        for (AccessibilityWindowInfo w : windows) {
            AccessibilityNodeInfo root = w.getRoot();
            if (root == null) continue;
            try {
                CharSequence pkg = root.getPackageName();
                if (pkg == null || !"com.android.systemui".contentEquals(pkg)) continue;
                for (String id : BOUNCER_IDS) {
                    List<AccessibilityNodeInfo> hit = root.findAccessibilityNodeInfosByViewId("com.android.systemui:id/" + id);
                    if (hit != null) for (AccessibilityNodeInfo n : hit) if (n.isVisibleToUser()) return true;
                }
                for (String t : BOUNCER_TEXTS) {
                    List<AccessibilityNodeInfo> hit = root.findAccessibilityNodeInfosByText(t);
                    if (hit != null) for (AccessibilityNodeInfo n : hit) if (n.isVisibleToUser()) return true;
                }
            } finally { root.recycle(); }
        }
        return false;
    }

    private View buildLockView(final String text, final float em, final float baselineInView) {
        return new View(this) {
            private final android.graphics.Paint p = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG | android.graphics.Paint.SUBPIXEL_TEXT_FLAG);
            { p.setColor(Color.WHITE); p.setTextSize(em); p.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, LockNumber.WEIGHT, false)); p.setTextAlign(android.graphics.Paint.Align.CENTER); }
            @Override protected void onDraw(android.graphics.Canvas c) { c.drawText(text, getWidth() / 2f + getWidth() * LockNumber.X_NUDGE_PER_WIDTH, baselineInView, p); }
        };
    }

    private void showLockNumber() {
        if (lockView != null) return;
        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
        float em = dm.widthPixels * LockNumber.EM_PER_WIDTH;
        float baselineY = dm.heightPixels * LockNumber.BASELINE_PER_HEIGHT;
        int h = (int) Math.ceil(em * 2f);
        float baselineInView = em * 1.3f;
        lockView = buildLockView(LockNumber.text(this), em, baselineInView);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                dm.widthPixels, h, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = 0; lp.y = Math.round(baselineY - baselineInView);
        lp.setTitle("TjLockNumber");
        ((WindowManager) getSystemService(WINDOW_SERVICE)).addView(lockView, lp);
        Log.i(TAG, "LOCKNUM shown");
    }

    private void removeLockNumber() {
        if (lockView == null) return;
        try { ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(lockView); } catch (Throwable ignored) { }
        lockView = null;
        Log.i(TAG, "LOCKNUM removed");
    }

    // ---------------------------------------------------------------- W5: keep the stock launcher's home screen away unless Tj did the gesture
    private ComponentName stockCache;
    private long stockCacheAt = 0;
    private final long[] guardHist = new long[StrayHomeGuard.MAX];
    private int guardIdx = 0;
    private long standDownUntil = 0, lastGuardMs = 0, guardPausedUntil = 0;

    private void guardStrayHome(AccessibilityEvent e) {
        if (e.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence pk = e.getPackageName(), cl = e.getClassName();
        if (pk == null || cl == null) return;
        if (!Mode.isCustom(this) || !Features.get(this, StrayHomeGuard.KEY, true)) return;
        long now = System.currentTimeMillis();
        if (now < guardPausedUntil || now - Mode.changedAt(this) < 2500) return;   // a deliberate mode change or the return reset is running: never fight it
        if (stockCache == null || now - stockCacheAt > 60_000) { stockCache = StockLauncher.find(this); stockCacheAt = now; }
        if (stockCache == null) return;
        if (!StrayHomeGuard.isStockHome(pk.toString(), cl.toString(), stockCache.getPackageName(), stockCache.getClassName())) return;
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        boolean locked = km != null && km.isKeyguardLocked();
        if (!StrayHomeGuard.shouldRedirect(true, true, locked, true, now, Mode.failOpenUntil(this), standDownUntil, guardHist)) {
            if (StrayHomeGuard.countInWindow(guardHist, now) >= StrayHomeGuard.MAX) standDownUntil = now + StrayHomeGuard.COOLDOWN_MS;
            return;
        }
        if (now - lastGuardMs < 700) return;   // one stock-home appearance can raise two events
        lastGuardMs = now;
        guardHist[guardIdx++ % guardHist.length] = now;
        int n = bump(StrayHomeGuard.COUNT_KEY);
        Features.setString(this, StrayHomeGuard.LAST_KEY, new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date(now)));
        Log.i(TAG, "STRAY stock home shown in custom mode -> pressing Home (#" + n + ")");
        performGlobalAction(GLOBAL_ACTION_HOME);   // the router answers with our Launcher
    }

    private int bump(String key) {
        int n = 1;
        try { n = Integer.parseInt(Features.getString(this, key, "0")) + 1; } catch (NumberFormatException ignored) { }
        Features.setString(this, key, String.valueOf(n));
        return n;
    }

    // ---------------------------------------------------------------- W4: the side key reaches the helper before the long-press power menu exists
    public static final String PWR_COUNT_KEY = "power_key_count", PWR_LAST_KEY = "power_key_last";

    /** Never consumes a key (always returns false). Only the POWER key is looked at; nothing else is logged or stored. */
    @Override protected boolean onKeyEvent(android.view.KeyEvent event) {
        try {
            if (event.getKeyCode() != android.view.KeyEvent.KEYCODE_POWER) return false;
            if (event.getAction() == android.view.KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                int n = bump(PWR_COUNT_KEY);
                Features.setString(this, PWR_LAST_KEY, new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date()));
                Log.i(TAG, "POWER key down seen by the helper (#" + n + ")");
                earlyCover();
            } else if (event.getAction() == android.view.KeyEvent.ACTION_UP) {
                handler.removeCallbacks(recheck);
                handler.postDelayed(recheck, 150);   // evaluate(): keeps the cover if the power menu opened, drops it after a short press
            }
        } catch (Throwable t) { Log.e(TAG, "key hook failed", t); }
        return false;
    }

    /** Side key just went down: put the replacement card in place now, before the (long-press) system power menu exists. */
    private void earlyCover() {
        if (!Mode.isCustom(this) || !Features.isCover(Features.powerMode(this))) return;
        Rect cached = cachedCard();
        if (cached == null) return;   // never learned where the menu sits yet: the first real menu teaches it
        showOverlay(cached);
        handler.removeCallbacks(recheck);
        handler.postDelayed(recheck, 900);   // no menu by then (it was a short press) -> evaluate() takes the card away
    }

    @Override public void onInterrupt() { removeOverlay(); removePatch(); removeLockNumber(); }

    @Override public boolean onUnbind(android.content.Intent i) { instance = null; removeOverlay(); removePatch(); stopPatchWatch(); stopLockNumber(); return super.onUnbind(i); }

    @Override public void onDestroy() { instance = null; removeOverlay(); removePatch(); stopPatchWatch(); stopLockNumber(); super.onDestroy(); }

    private void evaluate() {
        String mode = Features.powerMode(this);
        boolean custom = Mode.isCustom(this);
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        boolean locked = km != null && km.isKeyguardLocked();
        Rect shot = new Rect();
        boolean menuOpen = false, found = false;
        if (custom && !Features.POWER_OFF.equals(mode) && !(locked && Features.POWER_BLOCK.equals(mode))) {   // Y1: the cover also works on the lock screen; BLOCK still stays off it
            List<AccessibilityWindowInfo> windows = getWindows();
            if (!overlayShown && Features.isCover(mode)) {
                Rect cached = cachedCard();
                if (cached != null && dialogWindowPresent(windows)) showOverlay(cached);   // instant: no node scan yet
            }
            if (windows != null) {
                for (AccessibilityWindowInfo w : windows) {
                    AccessibilityNodeInfo root = w.getRoot();
                    if (root == null) continue;
                    if (getPackageName().contentEquals(root.getPackageName() == null ? "" : root.getPackageName())) { root.recycle(); continue; }
                    try {
                        List<AccessibilityNodeInfo> off = root.findAccessibilityNodeInfosByText("Power off");
                        List<AccessibilityNodeInfo> sc = root.findAccessibilityNodeInfosByText("Screenshot");
                        if (off != null && !off.isEmpty() && sc != null && !sc.isEmpty()) {
                            menuOpen = true;
                            // the card = the smallest ancestor of "Power off" that also contains the Screenshot entry
                            AccessibilityNodeInfo offItem = clickableAncestor(off.get(0));
                            Rect scr = new Rect(), c = new Rect();
                            clickableAncestor(sc.get(0)).getBoundsInScreen(scr);
                            AccessibilityNodeInfo t = offItem;
                            for (int i = 0; i < 6 && t != null; i++) {
                                t.getBoundsInScreen(c);
                                if (c.contains(scr.centerX(), scr.centerY()) && c.height() > scr.height()) { shot.set(c); break; }
                                t = t.getParent();
                            }
                            found = !shot.isEmpty();
                            break;
                        }
                    } finally { root.recycle(); }
                }
            }
        }
        int action = PowerMenuRule.decide(mode, custom, locked, menuOpen, found);
        if (menuOpen || action != PowerMenuRule.NOTHING) Log.i(TAG, "menuOpen=" + menuOpen + " found=" + found + " bounds=" + shot + " action=" + action);
        if (action == PowerMenuRule.COVER_SCREENSHOT) {
            showOverlay(shot);
            Features.setString(this, cardKey(), shot.left + "," + shot.top + "," + shot.right + "," + shot.bottom);
            handler.removeCallbacks(recheck);
            if (Features.POWER_WALLPAPER.equals(mode) && patchView == null) refreshLayers();
            handler.postDelayed(recheck, 1500);   // evaluate() again: removes the cover if the menu is gone   // if the menu closed without an event we still clean up
        } else {
            // Menu not (or no longer) open: keep the invisible, pre-built card ready (W3/W4) when the cover mode is on, else drop it.
            if (custom && Features.isCover(mode)) hideOverlay(); else removeOverlay();
            if (action == PowerMenuRule.CLOSE_MENU) performGlobalAction(GLOBAL_ACTION_BACK);
        }
    }


    // ---------------------------------------------------------------- return reset (RESET2): press "Clear all" in the REAL notification shade and the REAL Recents screen
    public static final String RESET_REPORT_KEY = "reset_last";
    private final StringBuilder resetReport = new StringBuilder();

    /**
     * Runs on the main thread. Calls {@code done} when finished (always, even if nothing could be pressed). Fail-open: a missing button just means that step is skipped.
     * SPEED (V3): no fixed sleeps. After opening the shade / Recents it polls every 40 ms and presses "Clear all" the moment it appears; an EMPTY shade / Recents
     * ("No notifications" / "No recent items") ends that step at once.
     */
    public void runReset(final boolean clearNotifications, final boolean clearRecents, final Runnable done) {
        resetReport.setLength(0);
        guardPausedUntil = System.currentTimeMillis() + 10_000;   // Recents / the shade are opened on purpose during the reset
        removeOverlay();
        final long t0 = android.os.SystemClock.uptimeMillis();
        Log.i(TAG, "RESET start notifications=" + clearNotifications + " recents=" + clearRecents);
        final Runnable afterShade = new Runnable() { @Override public void run() {
            if (clearRecents) resetRecents(t0, done); else { resetReport.append("recents: switch is OFF\n"); finishReset(t0, done); }
        } };
        if (!clearNotifications) {
            resetReport.append("notifications: switch is OFF\n");
            afterShade.run();
            return;
        }
        performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
        pollPress("clear all", new String[] {"no notifications"}, new String[] {"com.android.systemui"}, 1500, false, new PollResult() {
            @Override public void done(int result) {
                resetReport.append("notifications: ").append(result == PRESSED ? "cleared" : result == EMPTY ? "none to clear" : "no Clear all button found")
                        .append(" (+").append(android.os.SystemClock.uptimeMillis() - t0).append(" ms)\n");
                Log.i(TAG, "RESET notifications result=" + result + " +" + (android.os.SystemClock.uptimeMillis() - t0) + "ms");
                performGlobalAction(GLOBAL_ACTION_BACK);
                handler.postDelayed(afterShade, 90);   // 30 ms was too short: Recents opened while the shade was still collapsing timed out on the emulator
            }
        });
    }

    private static final int PRESSED = 1, EMPTY = 2, TIMEOUT = 3;
    private interface PollResult { void done(int result); }

    /** Polls every 20 ms (first look immediately): press {@code label} if it is there; end with EMPTY if one of {@code emptyTexts} is visible instead; scroll now and then if asked; TIMEOUT after maxMs. */
    private void pollPress(final String label, final String[] emptyTexts, final String[] packages, final long maxMs, final boolean scroll, final PollResult cb) {
        final long deadline = android.os.SystemClock.uptimeMillis() + maxMs;
        final int[] tick = {0};
        handler.post(new Runnable() { @Override public void run() {
            try {
                if (pressInSystemUi(label, packages)) { cb.done(PRESSED); return; }
                if (anyTextPresent(emptyTexts, packages)) { cb.done(EMPTY); return; }
                tick[0]++;
                if (scroll && tick[0] % 5 == 0) scrollSomething(packages);
            } catch (Throwable t) { Log.e(TAG, "poll failed", t); }
            if (android.os.SystemClock.uptimeMillis() >= deadline) { cb.done(TIMEOUT); return; }
            handler.postDelayed(this, 20);
        } });
    }

    private boolean anyTextPresent(String[] texts, String[] packages) {
        if (texts == null || texts.length == 0) return false;
        try {
            List<AccessibilityWindowInfo> windows = getWindows();
            if (windows == null) return false;
            for (AccessibilityWindowInfo w : windows) {
                AccessibilityNodeInfo root = w.getRoot();
                if (root == null) continue;
                try {
                    String pkg = root.getPackageName() == null ? "" : root.getPackageName().toString();
                    boolean ok = false;
                    for (String p : packages) if (p.equals(pkg)) ok = true;
                    if (!ok) continue;
                    for (String t : texts) {
                        List<AccessibilityNodeInfo> hit = root.findAccessibilityNodeInfosByText(t);
                        if (hit != null && !hit.isEmpty()) return true;
                    }
                } finally { root.recycle(); }
            }
        } catch (Throwable t) { Log.e(TAG, "anyTextPresent failed", t); }
        return false;
    }

    private void resetRecents(final long t0, final Runnable done) {
        performGlobalAction(GLOBAL_ACTION_RECENTS);
        pollPress("clear all", new String[] {"no recent items"}, new String[] {"com.android.launcher3", "com.android.systemui"}, 1800, true, new PollResult() {
            @Override public void done(int result) {
                resetReport.append("recents: ").append(result == PRESSED ? "cleared (apps closed)" : result == EMPTY ? "already empty" : "no Clear all button found")
                        .append(" (+").append(android.os.SystemClock.uptimeMillis() - t0).append(" ms)\n");
                Log.i(TAG, "RESET recents result=" + result + " +" + (android.os.SystemClock.uptimeMillis() - t0) + "ms");
                finishReset(t0, done);
            }
        });
    }

    private void finishReset(final long t0, final Runnable done) {
        // NO Home press here: on the slow tablet it landed AFTER the app start and covered it.
        handler.postDelayed(new Runnable() { @Override public void run() {
            resetReport.append("took ").append(android.os.SystemClock.uptimeMillis() - t0).append(" ms\n");
            Features.setString(TjAccessibilityService.this, RESET_REPORT_KEY, resetReport.toString());
            try { done.run(); } catch (Throwable t) { Log.e(TAG, "reset done failed", t); }
        } }, 40);
    }

    /** True if a window of this package is the active (front) one. */
    public boolean isForeground(String pkg) {
        try {
            AccessibilityNodeInfo r = getRootInActiveWindow();
            if (r == null) return false;
            try { return pkg.contentEquals(r.getPackageName() == null ? "" : r.getPackageName()); } finally { r.recycle(); }
        } catch (Throwable t) { return false; }
    }

    public void appendReport(String line) {
        resetReport.append(line).append("\n");
        Features.setString(this, RESET_REPORT_KEY, resetReport.toString());
    }

    /** Presses the first clickable node whose text starts with {@code label}, but ONLY inside windows of the given system packages (never in another app). */
    private boolean pressInSystemUi(String label, String... packages) {
        try {
            List<AccessibilityWindowInfo> windows = getWindows();
            if (windows == null) return false;
            for (AccessibilityWindowInfo w : windows) {
                AccessibilityNodeInfo root = w.getRoot();
                if (root == null) continue;
                try {
                    String pkg = root.getPackageName() == null ? "" : root.getPackageName().toString();
                    boolean ok = false;
                    for (String p : packages) if (p.equals(pkg)) ok = true;
                    if (!ok) continue;
                    List<AccessibilityNodeInfo> hit = root.findAccessibilityNodeInfosByText(label);
                    if (hit == null) continue;
                    for (AccessibilityNodeInfo n : hit) {
                        CharSequence t = n.getText() != null ? n.getText() : n.getContentDescription();
                        if (t == null || !t.toString().trim().toLowerCase(java.util.Locale.ROOT).startsWith(label)) continue;
                        if (clickableAncestor(n).performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true;
                    }
                } finally { root.recycle(); }
            }
        } catch (Throwable t) { Log.e(TAG, "pressInSystemUi failed", t); }
        return false;
    }

    private void scrollSomething(String... packages) {
        try {
            List<AccessibilityWindowInfo> windows = getWindows();
            if (windows == null) return;
            for (AccessibilityWindowInfo w : windows) {
                AccessibilityNodeInfo root = w.getRoot();
                if (root == null) continue;
                try {
                    String pkg = root.getPackageName() == null ? "" : root.getPackageName().toString();
                    boolean ok = false;
                    for (String p : packages) if (p.equals(pkg)) ok = true;
                    if (!ok) continue;
                    AccessibilityNodeInfo sc = findScrollable(root, 0);
                    if (sc != null && sc.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) return;
                } finally { root.recycle(); }
            }
        } catch (Throwable t) { Log.e(TAG, "scroll failed", t); }
    }

    private AccessibilityNodeInfo findScrollable(AccessibilityNodeInfo n, int depth) {
        if (n == null || depth > 8) return null;
        if (n.isScrollable()) return n;
        for (int i = 0; i < n.getChildCount(); i++) {
            AccessibilityNodeInfo r = findScrollable(n.getChild(i), depth + 1);
            if (r != null) return r;
        }
        return null;
    }

    private AccessibilityNodeInfo clickableAncestor(AccessibilityNodeInfo n) {
        AccessibilityNodeInfo t = n;
        for (int i = 0; i < 4 && t != null && !t.isClickable(); i++) t = t.getParent();
        return t != null && t.isClickable() ? t : n;
    }

    /** Presses the real system button with this label (found fresh, never a stale node). Returns false if it could not. */
    private boolean pressReal(String label) {
        try {
            List<AccessibilityWindowInfo> windows = getWindows();
            if (windows == null) return false;
            for (AccessibilityWindowInfo w : windows) {
                AccessibilityNodeInfo root = w.getRoot();
                if (root == null) continue;
                // never "press" our own card: it is the topmost window and carries the same labels (that was an endless self-click)
                if (getPackageName().contentEquals(root.getPackageName() == null ? "" : root.getPackageName())) { root.recycle(); continue; }
                try {
                    List<AccessibilityNodeInfo> hit = root.findAccessibilityNodeInfosByText(label);
                    if (hit != null && !hit.isEmpty()) {
                        boolean ok = clickableAncestor(hit.get(0)).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.i(TAG, "press '" + label + "' -> " + ok);
                        return ok;
                    }
                } finally { root.recycle(); }
            }
        } catch (Throwable t) { Log.e(TAG, "press failed", t); }
        return false;
    }

    private View cardButton(String label, int iconRes, final String realLabel) {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setGravity(Gravity.CENTER);
        b.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        android.widget.ImageView icon = new android.widget.ImageView(this);
        icon.setImageDrawable(getDrawable(iconRes));
        b.addView(icon, new LinearLayout.LayoutParams((int) (24 * getResources().getDisplayMetrics().density), (int) (24 * getResources().getDisplayMetrics().density)));
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(12);
        t.setTextColor(Color.parseColor("#FF5F6368"));
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, (int) (8 * getResources().getDisplayMetrics().density), 0, 0);
        b.addView(t);
        b.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, android.view.MotionEvent e) {
                Log.i(TAG, "card touch " + realLabel + " action=" + e.getActionMasked());
                return false;
            }
        });
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Log.i(TAG, "card click " + realLabel);
                // fail open: if the real button cannot be pressed, take our card away so the original menu is usable
                if (!pressReal(realLabel)) { Log.i(TAG, "press '" + realLabel + "' -> false (revealing original menu)"); removeOverlay(); }
            }
        });
        return b;
    }

    private View buildCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(PANEL_COLOR);
        card.addView(cardButton("Power off", R.drawable.tj_ic_power, "Power off"));
        card.addView(cardButton("Restart", R.drawable.tj_ic_restart, "Restart"));
        return card;
    }

    private boolean overlayShown = false, overlayTouchable = false;

    private WindowManager.LayoutParams overlayParams(Rect r, boolean touchable) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                r.width(), r.height(), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | (touchable ? 0 : WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE),
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = r.left; lp.y = r.top;
        return lp;
    }

    /** W3/W4: the card window can stay attached but invisible and untouchable (alpha 0 + NOT_TOUCHABLE) so showing it costs one redraw, not a window creation. */
    private void warmUp() {
        if (overlay != null || !Mode.isCustom(this) || !Features.isCover(Features.powerMode(this))) return;
        Rect cached = cachedCard();
        if (cached == null) return;
        ensurePatch();   // the wallpaper patch must be added BEFORE the card so the card is on top
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlay = buildCard();
        overlay.setAlpha(0f);
        wm.addView(overlay, overlayParams(cached, false));
        shownAt.set(cached);
        overlayShown = false; overlayTouchable = false;
    }

    private void showOverlay(Rect r) {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (overlay == null) {
            ensurePatch();
            overlay = buildCard();
            wm.addView(overlay, overlayParams(r, true));
            shownAt.set(r);
            overlayShown = true; overlayTouchable = true;
            return;
        }
        if (!overlayShown) { overlay.setAlpha(1f); overlayShown = true; }          // visible first (one redraw) ...
        if (!overlayTouchable || !shownAt.equals(r)) {                              // ... then touchable / moved
            wm.updateViewLayout(overlay, overlayParams(r, true));
            shownAt.set(r); overlayTouchable = true;
        }
    }

    /** Back to the warm state: still attached, invisible, untouchable. */
    private void hideOverlay() {
        handler.removeCallbacks(recheck);
        if (overlay == null) { warmUp(); return; }
        if (!overlayShown) return;
        overlay.setAlpha(0f);
        overlayShown = false;
        try {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).updateViewLayout(overlay, overlayParams(shownAt, false));
            overlayTouchable = false;
        } catch (Throwable t) { removeOverlay(); }
    }

    private void removeOverlay() {
        handler.removeCallbacks(recheck);
        if (overlay == null) return;
        try { ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(overlay); } catch (Throwable ignored) { }
        overlay = null;
        overlayShown = false; overlayTouchable = false;
        shownAt.setEmpty();
    }
}
