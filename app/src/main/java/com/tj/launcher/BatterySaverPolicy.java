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

    /**
     * AG1 (Tj, 2026-11-06: "Now battery saver doesn't work in quickstep. I want it to turn on and stay on, just turn off in custom launcher"): the battery saver FOLLOWS the launcher. Quickstep turns it ON and keeps it on;
     * the custom launcher turns it off (AC3 above). Needed because since v0.7.3 every plain-PIN unlock ends a Quickstep session (the custom launcher then switched the saver off) and nothing switched it back on
     * when Tj returned to Quickstep with the gesture. Switch in Launcher control, default ON.
     */
    public static final String QS_KEY = "profile_battery_saver_on_quickstep";
    /** At most this many switch-ONs per window: Android turns the saver off by itself in some situations (charging, battery recovered); that is never fought in a tight loop. */
    public static final int MAX_ON_WRITES = 3;
    public static final long ON_WINDOW_MS = 10 * 60_000;

    /**
     * AN1 (Tj, 2026-11-06: "the battery saver tile sometimes works. But it is mostly unresponsive. When it does work it turns on very slow ... It wasn't happening before"). Since v0.7.3 / v0.7.5 the guard did not only
     * switch the saver at a launcher CHANGE, it also undid every change made behind its back (the observer) and re-applied the rule at EVERY process start: a tap on the tile in Quickstep (off) was switched back on
     * within milliseconds, a tap in the custom launcher (on) was switched off again, and a Go-device process restart minutes later undid a tap too. That is the tile "not answering". Now the saver FOLLOWS the launcher
     * only at a real change (Quickstep begins / ends, the first start after a boot, a Control switch turned on); between those the TILE is the boss. The old behaviour is the opt-in "strict" switch.
     */
    public static final String STRICT_KEY = "profile_battery_saver_strict";   // switch in Launcher control, default OFF
    /** Only the first minutes after a boot re-apply the rule at process start (the setting survives a reboot); a later restart of our process must never undo a tap on the tile. */
    public static final long BOOT_GRACE_MS = 3 * 60_000;

    private BatterySaverPolicy() {}

    /** Pure (AN1): may a check act now? A DELIBERATE check (a real launcher change, the first start after a boot, a Control switch) always may; a REACTIVE one (the setting changed behind our back) only in strict mode. */
    public static boolean mayAct(boolean reactive, boolean strict) { return !reactive || strict; }

    /** Pure (AN1): does the process-start check run? Always in strict mode; otherwise only within {@link #BOOT_GRACE_MS} of the boot ({@code sinceBootMs} = SystemClock.elapsedRealtime()). */
    public static boolean startCheckWanted(boolean strict, long sinceBootMs) { return strict || (sinceBootMs >= 0 && sinceBootMs < BOOT_GRACE_MS); }

    /** Pure (AN1): does a mode "set" call run the check? A real change always; a repeated set of the same mode (an unlock that re-asserts it) only in strict mode. */
    public static boolean modeSetChecks(boolean modeChanged, boolean strict) { return modeChanged || strict; }

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

    /**
     * Pure (AG1): switch battery saver ON now? Only in Quickstep (never in the custom launcher), only while the Quickstep switch is on, never while automation is paused (breaker tripped / "Restore everything"),
     * only when it really is off, and not more than {@link #MAX_ON_WRITES} times per {@link #ON_WINDOW_MS}.
     */
    public static boolean shouldTurnOn(boolean switchOn, boolean customMode, boolean automationOff, boolean saverOn, long now, long[] recentOnWrites) {
        if (!deliberateOn(switchOn, customMode, automationOff, saverOn)) return false;
        int n = 0;
        if (recentOnWrites != null) for (long t : recentOnWrites) if (t > 0 && now - t <= ON_WINDOW_MS) n++;
        return n < MAX_ON_WRITES;
    }

    /**
     * v0.7.6 (review of v0.7.5): the switch-ON that follows a DELIBERATE entry into Quickstep (a mode change, the process start) is never capped: the cap exists only so that a ROM that turns the saver off
     * again behind our back (charging, battery recovered) is not fought in a tight loop, and that loop is made of REACTIVE switch-ons (the setting changed to 0), which still use {@link #shouldTurnOn}.
     * Counting deliberate entries as well left the saver off for good after the 4th Quickstep entry within 10 minutes.
     */
    public static boolean deliberateOn(boolean switchOn, boolean customMode, boolean automationOff, boolean saverOn) {
        return switchOn && !customMode && !automationOff && !saverOn;
    }
}
