package com.tj.launcher;

/**
 * AC3 (Tj, 2026-11-05 ~22:17): "make it so custom launcher automatically disables battery saver mode. I turned on battery saver from quickstep then went to the custom launcher and it was still on.
 * I want the custom launcher to turn it off immediately, if possible without any delay, maybe this can be prepped ahead of time".
 *
 * The pure rules for {@link BatterySaverGuard} (JVM-tested). Writing {@code Settings.Global low_power} = 0 (with the WRITE_SECURE_SETTINGS grant the launcher already has) switches battery saver off at once.
 *
 * AZ2 (Tj, 2026-11-06 20:40: "If I open the full settings and go to battery saver, then try to turn it off there, it says it turned off but it never does"): ROOT CAUSE of every battery-saver complaint since AC3.
 * Android 10's BatterySaverStateMachine switches the saver when the SETTING changes but never updates its own state ({@code mState}). Afterwards the tile and the Settings button
 * (PowerManager.setPowerSaveModeEnabled, which still returns true) are IGNORED by the state machine ("Tried to disable BS when it's already OFF" / "Tried to enable BS when it's already MANUAL_ON"), until the tablet is
 * plugged in or restarted. Evidence: research/AOSP10_BATTERYSAVER_AND_POWERMENU_2026-11-06.md (the AOSP source) and emulator section "bs" (the same sequence on a real Android 10, with the real Settings screen).
 * Consequences built into this class:
 *  - the launcher NEVER switches the saver ON any more (AG1's "on in Quickstep" is retired: Quickstep is 100 % stock);
 *  - a switch-OFF in the custom launcher (the only write left) can leave the state machine in MANUAL_ON / AUTOMATIC_ON, so the next "turn ON" is swallowed: {@link #healWanted} notices that request
 *    (Settings.Secure low_power_manual_activation_count grows for every ACCEPTED request, even a swallowed one) and carries it out itself (one write of low_power = 1, which also puts the state machine back in line);
 *  - a saver that v0.7.5 - v0.8.0 forced ON (state machine OFF, saver ON: it cannot be turned off by hand) is cleared once ({@link #unstickWanted}).
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
     * RETIRED (AZ2, v0.8.1). AG1 (v0.7.5, Tj: "I want it to turn on and stay on, just turn off in custom launcher") switched the saver ON when Quickstep began. That write is exactly what left Android's state machine
     * out of step (the tile and Settings could no longer turn it OFF). Nothing reads this key any more; it stays only so old preference files are understood.
     */
    public static final String QS_KEY = "profile_battery_saver_on_quickstep";

    /**
     * AN1 (Tj, 2026-11-06: "the battery saver tile sometimes works. But it is mostly unresponsive"). Between launcher changes the tile and Settings are the boss: the guard acts only at a REAL change
     * (Quickstep ends = the custom launcher begins, the first start after a boot, a Control switch turned on). The opt-in "strict" switch also undoes a saver that is turned ON while the custom launcher is active.
     */
    public static final String STRICT_KEY = "profile_battery_saver_strict";   // switch in Launcher control, default OFF
    /** Only the first minutes after a boot re-apply the rule at process start (the setting survives a reboot); a later restart of our process must never undo a tap on the tile. */
    public static final long BOOT_GRACE_MS = 3 * 60_000;

    // ---- AZ2: the self-heal and the one-time unstick
    /** Settings.Secure (hidden constant LOW_POWER_MANUAL_ACTIVATION_COUNT): +1 for every ACCEPTED request to turn the saver ON from the tile or Settings (BatterySaverUtils.setPowerSaveMode). */
    public static final String COUNT_SETTING = "low_power_manual_activation_count";
    /** The state machine has processed the request (and written low_power) before the counter grows; this short wait only covers the settings provider's own latency. */
    public static final long HEAL_DELAY_MS = 300;
    /** At most this many heals per window: a person tapping the tile, never a loop. */
    public static final int MAX_HEALS = 6;
    public static final long HEAL_WINDOW_MS = 60_000;
    public static final String HEALS_KEY = "bs_heals";                // how many times the heal acted (the tablet report)
    public static final String UNSTICK_DONE_KEY = "bs_unstick_done";  // the one-time clean-up of v0.7.5 - v0.8.0's forced ON has been looked at
    /** What the v0.7.5 - v0.8.0 guard wrote into {@link #LAST_KEY} when it forced the saver ON. */
    public static final String FORCED_ON_MARK = "switched battery saver ON in Quickstep";

    private BatterySaverPolicy() {}

    /** Pure (AN1): may a check act now? A DELIBERATE check (a real launcher change, the first start after a boot, a Control switch) always may; a REACTIVE one (the setting changed behind our back) only in strict mode. */
    public static boolean mayAct(boolean reactive, boolean strict) { return !reactive || strict; }

    /** Pure (AN1): does the process-start check run? Always in strict mode; otherwise only within {@link #BOOT_GRACE_MS} of the boot ({@code sinceBootMs} = SystemClock.elapsedRealtime()). */
    public static boolean startCheckWanted(boolean strict, long sinceBootMs) { return strict || (sinceBootMs >= 0 && sinceBootMs < BOOT_GRACE_MS); }

    /**
     * Pure (AZ2): does a "mode set" run the check? Only when the CUSTOM launcher is being set (Quickstep is never touched), and then only at a real change (an unlock that re-asserts custom while custom is already
     * active no longer undoes a tap) - or always in strict mode.
     */
    public static boolean modeSetChecks(boolean customRequested, boolean modeChanged, boolean strict) { return customRequested && (modeChanged || strict); }

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

    /** Pure (AZ2): did a request to turn the saver ON arrive? The counter only grows; {@code before} &lt; 0 = not read yet (never counts). */
    public static boolean activationRequested(int before, int now) { return before >= 0 && now > before; }

    /**
     * Pure (AZ2): Android's state machine swallowed a request to turn the saver ON (the counter grew but {@code low_power} is still off): carry the request out. Never while charging (Android refuses
     * to run the saver on charge, and then it does not even count the request), never more than {@link #MAX_HEALS} times per {@link #HEAL_WINDOW_MS}.
     */
    public static boolean healWanted(boolean saverOnNow, boolean charging, long now, long[] recentHeals) {
        if (saverOnNow || charging) return false;
        int n = 0;
        if (recentHeals != null) for (long t : recentHeals) if (t > 0 && now - t <= HEAL_WINDOW_MS) n++;
        return n < MAX_HEALS;
    }

    /** Pure (AZ2): is the tablet charging? "no" = on battery; "?" = could not tell (then the heal may run: it only carries out a request the person made). */
    public static boolean charging(String powerSource) { return powerSource != null && !"no".equals(powerSource) && !"?".equals(powerSource); }

    /**
     * Pure (AZ2): clear, ONCE, a saver that an older version of this launcher forced ON. Such a saver has Android's state machine in OFF, so neither the tile nor Settings can turn it off.
     * Evidence that it is ours: the guard's own last note says it forced the saver ON. {@code done}: this check has run before.
     */
    public static boolean unstickWanted(boolean done, boolean saverOn, String lastGuardNote) {
        return !done && saverOn && lastGuardNote != null && lastGuardNote.contains(FORCED_ON_MARK);
    }
}
