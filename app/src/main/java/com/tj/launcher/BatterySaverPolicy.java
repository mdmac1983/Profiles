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

    // ================================================================================================================================================================================
    // BA2 (Tj, 2026-11-07 00:07: "make the lock screen always have battery saver turned off permanently when the lock screen is up, no matter which launcher was on before the screen lock. If I go to the custom
    // launcher, have the battery saver remain turned off, but if I unlock to quickstep, keep the same battery saver state as the last time I was in quickstep. Only implement these battery saver features if you are
    // confident they can work and not break anything").
    //
    // WHY THIS CAN BE DONE WITHOUT BRINGING BACK THE AZ2 BUG (Android 10 source: research/AOSP10_BATTERYSAVER_AND_POWERMENU_2026-11-06.md):
    //  * Writing low_power = 0 is always consistent with Android's state machine (it is what the custom launcher has done since v0.7.3; the swallowed tap that can follow is completed by the AZ2b self-heal).
    //    It leaves the machine in MANUAL_ON / AUTOMATIC_ON ("stale", the saver itself is off).
    //  * Writing low_power = 1 is consistent ONLY while the machine is still in exactly that stale MANUAL_ON / AUTOMATIC_ON state (the setting then agrees with the state again and "Turn off now" works).
    //    The machine leaves that state ONLY by being plugged in (-> OFF, or PENDING_STICKY_ON) or by a reboot (a boot always starts with the saver off). After either, a direct write of 1 is the AZ2 bug again
    //    (the saver is on, the machine thinks OFF, "Turn off" does nothing). So the restore is done only when none of those can have happened, and in ANY doubt it is NOT done: the person taps the tile once
    //    (that tap is safe: it is a proper request, or a swallowed one that the self-heal completes).
    // The decisions are pure and JVM-tested here; {@link BatterySaverGuard} does the Android side.
    // ================================================================================================================================================================================

    /** Launcher control switches, both default ON: the lock screen keeps the saver off (any launcher); a gesture unlock into Quickstep brings Quickstep's own saver state back. */
    public static final String LOCK_OFF_KEY = "profile_battery_saver_lock_off";
    public static final String LOCK_RESTORE_KEY = "profile_battery_saver_lock_restore";
    /** Quickstep's own saver state the last time it had the tablet: "1" on, "0" off, "" unknown. */
    public static final String QS_ON_KEY = "bs_qs_on";
    /** "1" while the saver is off BECAUSE of the guard (lock screen or custom launcher) and Quickstep's state is waiting to come back; the facts below describe that moment. */
    public static final String HELD_KEY = "bs_held";
    public static final String HELD_AT_KEY = "bs_held_at";       // wall-clock ms when the hold began
    public static final String HELD_BOOT_KEY = "bs_held_boot";   // boot epoch (ms) when the guard switched it off
    public static final String LEVEL_KEY = "bs_last_level";      // the battery level (percent) at the last lock / unlock event
    public static final String PLUG_SEEN_KEY = "bs_plug_seen";   // "1": a plug-in (or a rise of the battery level) was seen while held
    public static final String RESTORES_KEY = "bs_restores";     // how many times the restore acted (the tablet report)
    public static final String NOTE_KEY = "bs_lock_note";        // what the lock rules did last, in plain words (Launcher control, report)
    /** The keep-off while locked is a safety net, not a loop: at most this many writes per window (it is the observer's reaction to something turning the saver on again). */
    public static final int MAX_LOCK_WRITES = 6;
    public static final long LOCK_WINDOW_MS = 60_000;
    /** After a restore the observer does not undo it for this long (the keyguard can report "locked" for a moment after the unlock broadcast). */
    public static final long RESTORE_QUIET_MS = 6_000;
    /** A boot epoch (now - elapsedRealtime) that differs by more than this = the tablet restarted (or the clock was set): the restore is skipped. */
    public static final long BOOT_TOLERANCE_MS = 120_000;
    /** A rise of the battery level by this many percent between two events = it was charged meanwhile. */
    public static final int LEVEL_RISE_PCT = 3;
    public static final int MAX_RESTORES = 3;
    public static final long RESTORE_WINDOW_MS = 5 * 60_000;

    /** Pure: switch the saver off at the lock moment (a DELIBERATE check, never capped)? Only while the lock screen is up, with the switch on, automation not paused and the saver really on. */
    public static boolean lockOffWanted(boolean switchOn, boolean keyguardLocked, boolean automationOff, boolean saverOn) {
        return switchOn && keyguardLocked && !automationOff && saverOn;
    }

    /**
     * Pure: the saver was turned ON while the lock screen is up (Android's own low-battery trigger, a notification button): switch it off again? Same conditions, plus: not within
     * {@link #RESTORE_QUIET_MS} of a restore (the unlock that is just finishing), and at most {@link #MAX_LOCK_WRITES} writes per {@link #LOCK_WINDOW_MS}. {@code lastRestoreMs} 0 = none.
     */
    public static boolean keepOffWanted(boolean switchOn, boolean keyguardLocked, boolean automationOff, boolean saverOn, long now, long lastRestoreMs, long[] recentWrites) {
        if (!lockOffWanted(switchOn, keyguardLocked, automationOff, saverOn)) return false;
        if (lastRestoreMs > 0 && now >= lastRestoreMs && now - lastRestoreMs < RESTORE_QUIET_MS) return false;
        int n = 0;
        if (recentWrites != null) for (long t : recentWrites) if (t > 0 && now >= t && now - t <= LOCK_WINDOW_MS) n++;
        return n < MAX_LOCK_WRITES;
    }

    /**
     * Pure: what to remember as Quickstep's saver state when the guard is about to switch the saver off (or finds it off) at a lock or at the end of a Quickstep session. Only the state of QUICKSTEP counts
     * ({@code fromQuickstep}), and only the first time of a hold: once the guard holds the saver off, what is on or off meanwhile is the guard's doing, not Quickstep's.
     */
    public static String qsStateAtHold(boolean fromQuickstep, boolean alreadyHeld, boolean saverOn, String stored) {
        if (!fromQuickstep || alreadyHeld) return stored == null ? "" : stored;
        return saverOn ? "1" : "0";
    }

    /**
     * Pure: is a hold that began at {@code heldAtMs} still the one Quickstep's memory belongs to? NOT when a Quickstep session began AFTER the hold ({@code qsStartMs}, {@link Mode#qsStartAt}) and Quickstep
     * has the tablet now: the tablet came back to Quickstep by another way than a gesture unlock (a secret entrance, a return method), and what is on or off now is that session's own state.
     */
    public static boolean holdStillValid(boolean held, long heldAtMs, long qsStartMs, boolean quickstepNow) {
        if (!held) return false;
        return !(quickstepNow && qsStartMs > heldAtMs);
    }

    /** Pure: the saver changed while Quickstep has the tablet (unlocked) and the change was not ours: that is Quickstep's state now. */
    public static String qsStateLive(boolean quickstepActive, boolean keyguardLocked, boolean ourOwnWrite, boolean saverOn, String stored) {
        if (!quickstepActive || keyguardLocked || ourOwnWrite) return stored == null ? "" : stored;
        return saverOn ? "1" : "0";
    }

    /** Pure: did the battery level rise between two readings (it was charged)? -1 = unknown. */
    public static boolean levelRose(int lastLevel, int nowLevel) { return lastLevel >= 0 && nowLevel >= 0 && nowLevel >= lastLevel + LEVEL_RISE_PCT; }

    /** Pure: is it the same boot? Both are {@code System.currentTimeMillis() - SystemClock.elapsedRealtime()}; 0 = unknown (never the same). */
    public static boolean sameBoot(long thenEpochMs, long nowEpochMs) { return thenEpochMs > 0 && nowEpochMs > 0 && Math.abs(nowEpochMs - thenEpochMs) <= BOOT_TOLERANCE_MS; }

    /** Everything the restore decision depends on; the guard fills it in. */
    public static final class Restore {
        public boolean switchOn, restoreOn, automationOff, permitted, quickstepGesture, held, saverOnNow, keyguardLocked, chargingOrUnknown, plugSeen;
        public String qsOn = "";
        public long heldBootEpoch, nowBootEpoch, now;
        public long[] recentRestores;
    }

    /** Pure: why NOT to bring Quickstep's saver back now, in plain words ("" = restore it). Every doubt is a refusal. */
    public static String restoreRefusal(Restore s) {
        if (!s.switchOn) return "the lock-screen battery saver rule is switched off";
        if (!s.restoreOn) return "bringing Quickstep's battery saver back is switched off";
        if (s.automationOff) return "automation is paused";
        if (!s.permitted) return "the adb permission (WRITE_SECURE_SETTINGS) is missing";
        if (!s.quickstepGesture) return "this was not a gesture unlock into Quickstep";
        if (!s.held) return "the launcher had not switched the saver off";
        if (!"1".equals(s.qsOn)) return "Quickstep did not have it on last time";
        if (s.saverOnNow) return "it is already on";
        if (s.keyguardLocked) return "the lock screen is still up";
        if (s.chargingOrUnknown) return "the tablet is charging (or its power state is unknown): Android switches the saver off on charge";
        if (s.plugSeen) return "the tablet was plugged in or charged meanwhile: that resets Android's own saver state, so switching it on from outside would leave 'Turn off now' dead";
        if (!sameBoot(s.heldBootEpoch, s.nowBootEpoch)) return "the tablet restarted meanwhile (a restart always starts with the saver off and Android's state reset)";
        int n = 0;
        if (s.recentRestores != null) for (long t : s.recentRestores) if (t > 0 && s.now >= t && s.now - t <= RESTORE_WINDOW_MS) n++;
        if (n >= MAX_RESTORES) return "restored a moment ago";
        return "";
    }

    /** Pure: one line for Launcher control and the tablet report. */
    public static String describe(String qsOn, boolean held, boolean plugSeen) {
        String qs = "1".equals(qsOn) ? "ON" : "0".equals(qsOn) ? "off" : "unknown";
        return "Quickstep's saver the last time: " + qs + "; the launcher is holding it off: " + (held ? "yes" : "no") + (held && plugSeen ? " (plugged in meanwhile: it will not be switched on again)" : "");
    }
}
