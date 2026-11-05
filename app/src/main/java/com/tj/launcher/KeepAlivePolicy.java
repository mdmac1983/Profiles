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
     * A foreground service whose notification Android never shows in the status bar or on the lock screen: the lowest importance and a secret lock-screen visibility. Pure so a test can pin the
     * numbers (IMPORTANCE_MIN = 1, VISIBILITY_SECRET = -1) without Android.
     */
    public static final int IMPORTANCE_MIN = 1, VISIBILITY_SECRET = -1;

    /** Pure: a restart request for a service that was killed (START_STICKY) is honoured only while the rule above still says run. */
    public static boolean honourRestart(boolean switchOn, boolean crashTripped) { return shouldRun(switchOn, crashTripped); }
}
