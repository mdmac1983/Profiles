package com.tj.launcher;

/**
 * AC3 (Tj, 2026-11-05 ~22:17): "make it so custom launcher automatically disables battery saver mode. I turned on battery saver from quickstep then went to the custom launcher and it was still on.
 * I want the custom launcher to turn it off immediately, if possible without any delay, maybe this can be prepped ahead of time".
 *
 * The pure rules for {@link BatterySaverGuard} (JVM-tested). Android 10's BatterySaverStateMachine watches {@code Settings.Global low_power}: writing 0 (with the WRITE_SECURE_SETTINGS grant the
 * launcher already has) switches battery saver off exactly as the Settings screen does. "Prepared ahead of time" = a ContentObserver on that setting registered for the life of the process, so the
 * write happens the moment something turns it on; nothing polls.
 */
public final class BatterySaverPolicy {
    public static final String KEY = "profile_battery_saver_off";   // switch in Launcher control, default ON (Tj asked for it)
    public static final String SETTING = "low_power";               // Settings.Global: 1 = battery saver on
    public static final String STICKY_SETTING = "low_power_sticky"; // Settings.Global: Android keeps it on until the battery recovers; cleared together with the main flag
    public static final String LAST_KEY = "battery_saver_last";     // what the guard did last (shown in Launcher control and the report)
    /** At most this many switch-offs per window: a ROM that turns it on again by itself is never fought in a loop. */
    public static final int MAX_WRITES = 4;
    public static final long WINDOW_MS = 60_000;

    private BatterySaverPolicy() {}

    /** Pure: is this stored setting value "battery saver on"? */
    public static boolean isOn(String value) { return value != null && "1".equals(value.trim()); }

    /**
     * Pure: switch battery saver off now? Only in the custom launcher, only while the switch is on, never while automation is paused (breaker tripped / "Restore everything"),
     * only when it really is on, and not more than {@link #MAX_WRITES} times per {@link #WINDOW_MS}.
     */
    public static boolean shouldTurnOff(boolean switchOn, boolean customMode, boolean automationOff, boolean saverOn, long now, long[] recentWrites) {
        if (!switchOn || !customMode || automationOff || !saverOn) return false;
        int n = 0;
        if (recentWrites != null) for (long t : recentWrites) if (t > 0 && now - t <= WINDOW_MS) n++;
        return n < MAX_WRITES;
    }
}
