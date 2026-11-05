package com.tj.launcher;

/**
 * AA2 (Tj, 2026-11-05: "Build number 3" = keep the launcher in RAM so it starts as fast as possible). The pure rules for {@link KeepAliveService}, kept free of Android classes so they are JVM-tested.
 */
public final class KeepAlivePolicy {
    public static final String KEY = "keep_alive";       // switch in Launcher control, default ON

    private KeepAlivePolicy() {}

    /**
     * Should the keep-alive service be running right now?
     * Never while the crash-loop breaker is tripped: a launcher that keeps crashing must not be restarted by its own service (the breaker switched everything off on purpose).
     */
    public static boolean shouldRun(boolean switchOn, boolean crashTripped) {
        return switchOn && !crashTripped;
    }

    /**
     * The quietest notification Android allows for a foreground service. We ask for IMPORTANCE_MIN (1) and the secret lock-screen visibility (-1); Android raises it to LOW (2) and ignores the
     * lock-screen request (apps cannot set it) - measured on the Android 10 emulator, run 79. The numbers are pinned here so a test can check them without Android.
     */
    public static final int IMPORTANCE_MIN = 1, VISIBILITY_SECRET = -1;

    /** The notification's title. The accessibility helper's status-bar look recognises our own icon by it (its content description is "<app> notification: <title>"). */
    public static final String TITLE = "Launcher is kept ready";

    /** Pure: is this status-bar icon our own keep-alive icon? (so it never counts as "a notification to clear") */
    public static boolean isOurIcon(CharSequence contentDescription) {
        return contentDescription != null && contentDescription.toString().contains(TITLE);
    }

    /** Pure: a restart request for a service that was killed (START_STICKY) is honoured only while the rule above still says run. */
    public static boolean honourRestart(boolean switchOn, boolean crashTripped) { return shouldRun(switchOn, crashTripped); }
}
