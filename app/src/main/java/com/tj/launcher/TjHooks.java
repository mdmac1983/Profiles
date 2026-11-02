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

    private static boolean startedOnce = false;
    public static final String STARTUP_KEY = "startup_open";   // default ON

    /** Called by Launcher.onResume. The FIRST resume of each launcher process (boot, or after Android restarted it) puts the target app (Solitaire) in front. */
    public static void onLauncherResumed(final Context c) {
        launcherResumed = true;
        try {
            if (!startedOnce) {
                startedOnce = true;
                if (Mode.isCustom(c) && Features.get(c, STARTUP_KEY, true)) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override public void run() { try { ReturnReset.openStartupApp(c); } catch (Throwable t) { android.util.Log.e("TjHooks", "startup open failed", t); } }
                    }, 350);
                }
            }
        } catch (Throwable t) { android.util.Log.e("TjHooks", "startup hook failed", t); }
    }

    public static void onLauncherPaused() {
        launcherResumed = false;
        lastLauncherPauseMs = System.currentTimeMillis();
    }

    /** True when Home was pressed while our launcher was the visible screen (it paused a moment ago because the router appeared). */
    public static boolean launcherWasForeground() {
        return launcherResumed || System.currentTimeMillis() - lastLauncherPauseMs < 600;
    }

    public static final int PASS = 0, CANCEL_THEN_SWALLOW = 1, SWALLOW = 2;
    private static boolean swallowing = false;

    /**
     * Called for every touch event the Launcher receives. Returns what Launcher.dispatchTouchEvent should do:
     * PASS (normal), CANCEL_THEN_SWALLOW (a second finger just came down: cancel the page scroll/drag the first finger started, then eat events),
     * SWALLOW (keep eating events until every finger is up, so the twist can never drag the home screen left/right).
     * Never throws: any failure returns PASS.
     */
    public static int onTouch(Context c, MotionEvent ev) {
        try {
            int action = ev.getActionMasked();
            if (ev.getPointerCount() >= 2 && !Features.get(c, "twist_off", false)) {
                int ret = swallowing ? SWALLOW : CANCEL_THEN_SWALLOW;
                swallowing = true;
                if (ev.getPointerCount() == 2) {
                    int dir = rotation.onTwoPointers(ev.getX(0), ev.getY(0), ev.getX(1), ev.getY(1));
                    if (dir == TwoFingerRotation.CLOCKWISE) goToQuickstep(c);
                }
                return ret;
            }
            if (swallowing) {
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) { swallowing = false; rotation.reset(); }
                return SWALLOW;   // a lone remaining finger after a two-finger gesture must not scroll the page
            }
            rotation.reset();
        } catch (Throwable t) {
            android.util.Log.e("TjHooks", "touch hook failed", t);
            swallowing = false;
        }
        return PASS;
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
