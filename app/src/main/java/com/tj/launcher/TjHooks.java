package com.tj.launcher;

import android.content.Context;
import android.view.MotionEvent;

/** The few entry points the patched Launcher3 calls. Everything here must be exception-safe: a bug here must never crash the Home screen. */
public final class TjHooks {
    public static final String EXTRA_HOME_WHILE_ON_HOME = "tj_home_while_on_home";
    private static volatile long lastLauncherPauseMs = 0;
    private static volatile boolean launcherResumed = false;

    private TjHooks() {}

    private static boolean startedOnce = false;
    public static final String STARTUP_KEY = "startup_open";   // default ON

    /** Called by Launcher.onResume. The FIRST resume of each launcher process (boot, or after Android restarted it) puts the target app (Solitaire) in front. */
    public static void onLauncherResumed(final Context c) {
        launcherResumed = true;
        try { if (c instanceof com.android.launcher3.Launcher) launcherNormal = ((com.android.launcher3.Launcher) c).isInState(com.android.launcher3.LauncherState.NORMAL); } catch (Throwable ignored) { }
        try { TjAccessibilityService s = TjAccessibilityService.get(); if (s != null) s.refreshLayersSoon(); } catch (Throwable ignored) { }
        RecentsGuard.excludeOurTasks(c);                       // W2: also fixes a Launcher task created before the exclude flags existed
        if (c instanceof android.app.Activity) RecentsGuard.describeAsTarget((android.app.Activity) c);
        try {
            if (!startedOnce) {
                startedOnce = true;
                if (Mode.isCustom(c) && ReturnReset.autoOpenWanted(c)) {   // BE3: a clean start opens no stand-in app
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override public void run() { try { ReturnReset.openStartupApp(c); } catch (Throwable t) { android.util.Log.e("TjHooks", "startup open failed", t); } }
                    }, 350);
                }
            }
        } catch (Throwable t) { android.util.Log.e("TjHooks", "startup hook failed", t); }
    }

    /** Current wallpaper x offset (0..1) as the launcher last set it; 0.5 until the launcher says otherwise. */
    public static volatile float wallpaperX = 0.5f;

    public static void onWallpaperOffset(float x) {
        wallpaperX = x;
        try { TjAccessibilityService s = TjAccessibilityService.get(); if (s != null) s.onWallpaperOffset(); } catch (Throwable ignored) { }
    }

    // ---------------------------------------------------------------- AD4: the page-flick entrance watches every single-finger swipe that STARTS on the settled home screen (a passive observer: it never consumes an event)
    private static float downX, downY;
    private static long downAt;
    private static boolean downHome, multiTouch;

    /** Called by Launcher.dispatchTouchEvent before the event is handled as usual. */
    public static void onTouch(android.app.Activity a, MotionEvent ev) {
        try {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: downHome = homeScreenShowing() && EntranceWatcher.on(a.getApplicationContext(), SecretEntrances.Method.FLICK); multiTouch = false;   // nothing else is done for a touch while the entrance is off or the home screen is not showing
                    if (downHome) { downX = ev.getRawX(); downY = ev.getRawY(); downAt = ev.getEventTime(); } break;
                case MotionEvent.ACTION_POINTER_DOWN: multiTouch = true; break;
                case MotionEvent.ACTION_UP:
                    if (downHome && !multiTouch) {
                        int dir = SecretEntrances.swipeDirection(ev.getRawX() - downX, ev.getRawY() - downY, ev.getEventTime() - downAt, a.getResources().getDisplayMetrics().widthPixels);
                        if (dir != 0) EntranceWatcher.onSwipe(a.getApplicationContext(), dir);
                    }
                    downHome = false;
                    break;
                case MotionEvent.ACTION_CANCEL: downHome = false; break;
                default: break;
            }
        } catch (Throwable t) { android.util.Log.e("TjHooks", "touch hook failed", t); }
    }

    /** True while the plain home screen (workspace, NORMAL state) is the visible, settled screen. */
    public static volatile boolean launcherNormal = false;

    public static boolean homeScreenShowing() { return launcherResumed && launcherNormal; }

    public static void watchState(com.android.launcher3.LauncherStateManager sm) {
        try {
            sm.addStateListener(new com.android.launcher3.LauncherStateManager.StateListener() {
                @Override public void onStateTransitionStart(com.android.launcher3.LauncherState toState) { setNormal(false); }
                @Override public void onStateTransitionComplete(com.android.launcher3.LauncherState finalState) { setNormal(finalState == com.android.launcher3.LauncherState.NORMAL); }
            });
        } catch (Throwable t) { android.util.Log.e("TjHooks", "state watch failed", t); }
    }

    private static void setNormal(boolean v) {
        launcherNormal = v;
        try { TjAccessibilityService s = TjAccessibilityService.get(); if (s != null) s.refreshLayersSoon(); } catch (Throwable ignored) { }
    }

    public static void onLauncherPaused() {
        launcherResumed = false;
        try { TjAccessibilityService s = TjAccessibilityService.get(); if (s != null) s.refreshLayersSoon(); } catch (Throwable ignored) { }
        lastLauncherPauseMs = System.currentTimeMillis();
    }

    /** True when Home was pressed while our launcher was the visible screen (it paused a moment ago because the router appeared). */
    public static boolean launcherWasForeground() {
        return launcherResumed || System.currentTimeMillis() - lastLauncherPauseMs < 600;
    }
}
