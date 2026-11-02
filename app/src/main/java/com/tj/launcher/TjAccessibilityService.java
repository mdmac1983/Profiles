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
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.List;

/**
 * Power-menu helper (L4 / G3b). Only active in custom-launcher mode, only when Tj chose a power mode, never on the lock screen.
 * HIDE_SCREENSHOT: covers the "Screenshot" entry with a panel-coloured overlay (TYPE_ACCESSIBILITY_OVERLAY, needs no overlay permission).
 * BLOCK: closes the whole power menu (Back). Fails open: any problem -> the menu is simply left alone and the overlay removed.
 */
public class TjAccessibilityService extends AccessibilityService {
    private static final String TAG = "TjA11y";
    private static final int PANEL_COLOR = Color.parseColor("#F4F6F7");

    private final Handler handler = new Handler(Looper.getMainLooper());
    private View overlay;
    private final Runnable recheck = new Runnable() { @Override public void run() { evaluate(); } };

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        try { evaluate(); } catch (Throwable t) { Log.e(TAG, "evaluate failed", t); removeOverlay(); }
    }

    @Override public void onInterrupt() { removeOverlay(); }

    @Override public boolean onUnbind(android.content.Intent i) { removeOverlay(); return super.onUnbind(i); }

    @Override public void onDestroy() { removeOverlay(); super.onDestroy(); }

    private void evaluate() {
        String mode = Features.powerMode(this);
        boolean custom = Mode.isCustom(this);
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        boolean locked = km != null && km.isKeyguardLocked();
        Rect shot = new Rect();
        boolean menuOpen = false, found = false;
        if (custom && !locked && !Features.POWER_OFF.equals(mode)) {
            List<AccessibilityWindowInfo> windows = getWindows();
            if (windows != null) {
                for (AccessibilityWindowInfo w : windows) {
                    AccessibilityNodeInfo root = w.getRoot();
                    if (root == null) continue;
                    try {
                        List<AccessibilityNodeInfo> off = root.findAccessibilityNodeInfosByText("Power off");
                        List<AccessibilityNodeInfo> sc = root.findAccessibilityNodeInfosByText("Screenshot");
                        if (off != null && !off.isEmpty() && sc != null && !sc.isEmpty()) {
                            menuOpen = true;
                            AccessibilityNodeInfo n = sc.get(0);
                            // the tappable entry is usually a parent of the text node: grow to the first clickable ancestor
                            AccessibilityNodeInfo t = n;
                            for (int i = 0; i < 4 && t != null && !t.isClickable(); i++) t = t.getParent();
                            (t != null && t.isClickable() ? t : n).getBoundsInScreen(shot);
                            found = !shot.isEmpty();
                            break;
                        }
                    } finally { root.recycle(); }
                }
            }
        }
        int action = PowerMenuRule.decide(mode, custom, locked, menuOpen, found);
        if (action == PowerMenuRule.COVER_SCREENSHOT) {
            showOverlay(shot);
            handler.removeCallbacks(recheck);
            handler.postDelayed(recheck, 1500);   // if the menu closed without an event we still clean up
        } else {
            removeOverlay();
            if (action == PowerMenuRule.CLOSE_MENU) performGlobalAction(GLOBAL_ACTION_BACK);
        }
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
            overlay = new View(this);
            overlay.setBackgroundColor(PANEL_COLOR);
            wm.addView(overlay, lp);
        } else {
            wm.updateViewLayout(overlay, lp);
        }
    }

    private void removeOverlay() {
        handler.removeCallbacks(recheck);
        if (overlay == null) return;
        try { ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(overlay); } catch (Throwable ignored) { }
        overlay = null;
    }
}
