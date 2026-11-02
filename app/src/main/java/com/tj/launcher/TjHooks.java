package com.tj.launcher;

import android.content.Context;
import android.view.MotionEvent;

/** The few entry points the patched Launcher3 calls. Everything here must be exception-safe: a bug here must never crash the Home screen. */
public final class TjHooks {
    public static final String EXTRA_HOME_WHILE_ON_HOME = "tj_home_while_on_home";
    private static volatile long lastLauncherPauseMs = 0;
    private static volatile boolean launcherResumed = false;
    private static final TwoFingerRotation rotation = new TwoFingerRotation();

    private TjHooks() {}

    public static void onLauncherResumed() { launcherResumed = true; }

    public static void onLauncherPaused() {
        launcherResumed = false;
        lastLauncherPauseMs = System.currentTimeMillis();
    }

    /** True when Home was pressed while our launcher was the visible screen (it paused a moment ago because the router appeared). */
    public static boolean launcherWasForeground() {
        return launcherResumed || System.currentTimeMillis() - lastLauncherPauseMs < 600;
    }

    /** Observation only: never consumes an event. */
    public static void onTouch(Context c, MotionEvent ev) {
        try {
            if (ev.getPointerCount() == 2) {
                int dir = rotation.onTwoPointers(ev.getX(0), ev.getY(0), ev.getX(1), ev.getY(1));
                if (dir == TwoFingerRotation.CLOCKWISE) goToQuickstep(c);
                // counter-clockwise (kill switch) arrives with the kill-switch feature
            } else if (ev.getActionMasked() == MotionEvent.ACTION_UP || ev.getActionMasked() == MotionEvent.ACTION_CANCEL
                    || ev.getPointerCount() < 2) {
                rotation.reset();
            }
        } catch (Throwable t) {
            android.util.Log.e("TjHooks", "touch hook failed", t);
        }
    }

    public static void goToQuickstep(Context c) {
        Mode.set(c, Mode.QUICKSTEP);
        // Same HOME intent the Home button sends: the router sees QUICKSTEP and forwards to the stock launcher.
        c.startActivity(new android.content.Intent(android.content.Intent.ACTION_MAIN)
                .addCategory(android.content.Intent.CATEGORY_HOME)
                .setPackage(c.getPackageName())
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
