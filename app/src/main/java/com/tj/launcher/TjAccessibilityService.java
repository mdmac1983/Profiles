package com.tj.launcher;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
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

    private Rect cachedCard() {
        String v = Features.getString(this, CARD_KEY, "");
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
        Log.i(TAG, "connected; power mode=" + Features.powerMode(this) + " custom=" + Mode.isCustom(this));
    }

    /** Research only (debug builds declare the capability): logs which hardware keys reach an accessibility service. Never consumes a key. */
    @Override protected boolean onKeyEvent(android.view.KeyEvent event) {
        Log.i(TAG, "KEY code=" + event.getKeyCode() + " (" + android.view.KeyEvent.keyCodeToString(event.getKeyCode()) + ") action=" + event.getAction() + " t=" + event.getEventTime());
        return false;
    }

    @Override public void onInterrupt() { removeOverlay(); }

    @Override public boolean onUnbind(android.content.Intent i) { instance = null; removeOverlay(); return super.onUnbind(i); }

    @Override public void onDestroy() { instance = null; removeOverlay(); super.onDestroy(); }

    private void evaluate() {
        String mode = Features.powerMode(this);
        boolean custom = Mode.isCustom(this);
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        boolean locked = km != null && km.isKeyguardLocked();
        Rect shot = new Rect();
        boolean menuOpen = false, found = false;
        if (custom && !locked && !Features.POWER_OFF.equals(mode)) {
            List<AccessibilityWindowInfo> windows = getWindows();
            if (overlay == null && Features.POWER_HIDE_SCREENSHOT.equals(mode)) {
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
            Features.setString(this, CARD_KEY, shot.left + "," + shot.top + "," + shot.right + "," + shot.bottom);
            handler.removeCallbacks(recheck);
            handler.postDelayed(recheck, 1500);   // evaluate() again: removes the cover if the menu is gone   // if the menu closed without an event we still clean up
        } else {
            removeOverlay();
            if (action == PowerMenuRule.CLOSE_MENU) performGlobalAction(GLOBAL_ACTION_BACK);
        }
    }


    // ---------------------------------------------------------------- return reset (RESET2): press "Clear all" in the REAL notification shade and the REAL Recents screen
    public static final String RESET_REPORT_KEY = "reset_last";
    private final StringBuilder resetReport = new StringBuilder();

    /** Runs on the main thread. Calls {@code done} when finished (always, even if nothing could be pressed). Fail-open: a missing button just means that step is skipped. */
    public void runReset(final Runnable done) {
        resetReport.setLength(0);
        removeOverlay();
        Log.i(TAG, "RESET start");
        performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
        handler.postDelayed(new Runnable() { @Override public void run() {
            boolean cleared = pressInSystemUi("clear all", "com.android.systemui");
            resetReport.append("notifications: ").append(cleared ? "cleared" : "no Clear all button (nothing to clear?)").append("\n");
            Log.i(TAG, "RESET notifications cleared=" + cleared);
            performGlobalAction(GLOBAL_ACTION_BACK);
            handler.postDelayed(new Runnable() { @Override public void run() { resetRecents(done); } }, 350);
        } }, 600);
    }

    private void resetRecents(final Runnable done) {
        performGlobalAction(GLOBAL_ACTION_RECENTS);
        recentsAttempt(0, done);
    }

    private void recentsAttempt(final int attempt, final Runnable done) {
        handler.postDelayed(new Runnable() { @Override public void run() {
            boolean cleared = pressInSystemUi("clear all", "com.android.launcher3", "com.android.systemui");
            if (!cleared && attempt < 6) {
                // the button can sit at the far end of the task carousel: scroll it and look again
                scrollSomething("com.android.launcher3", "com.android.systemui");
                recentsAttempt(attempt + 1, done);
                return;
            }
            resetReport.append("recents: ").append(cleared ? "cleared (apps closed)" : "no Clear all button found").append("\n");
            Log.i(TAG, "RESET recents cleared=" + cleared + " attempts=" + (attempt + 1));
            performGlobalAction(GLOBAL_ACTION_HOME);
            handler.postDelayed(new Runnable() { @Override public void run() {
                Features.setString(TjAccessibilityService.this, RESET_REPORT_KEY, resetReport.toString());
                try { done.run(); } catch (Throwable t) { Log.e(TAG, "reset done failed", t); }
            } }, 450);
        } }, attempt == 0 ? 750 : 280);
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

    private void showOverlay(Rect r) {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                r.width(), r.height(), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.OPAQUE);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = r.left; lp.y = r.top;
        if (overlay == null) {
            overlay = buildCard();
            wm.addView(overlay, lp);
            shownAt.set(r);
        } else if (!shownAt.equals(r)) {
            wm.updateViewLayout(overlay, lp);
            shownAt.set(r);
        }
    }

    private void removeOverlay() {
        handler.removeCallbacks(recheck);
        if (overlay == null) return;
        try { ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(overlay); } catch (Throwable ignored) { }
        overlay = null;
        shownAt.setEmpty();
    }
}
