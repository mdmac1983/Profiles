package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * AC3 / AN1 / AZ2: the battery-saver guard's rules. The guard itself needs Android; emulator sections "ac" (the switch-off) and "bs" (the real Settings screen against Android 10's state machine)
 * prove it on a real Android 10 system.
 */
public class BatterySaverPolicyTest {
    private static final long NOW = 1_000_000;
    private static final long[] NONE = {0, 0, 0, 0};

    // ---- AC3: the custom launcher switches the saver off
    @Test public void switchesItOffInTheCustomLauncher() { assertTrue(BatterySaverPolicy.shouldTurnOff(true, true, false, true, NOW, NONE)); }
    @Test public void neverInQuickstep() { assertFalse(BatterySaverPolicy.shouldTurnOff(true, false, false, true, NOW, NONE)); }
    @Test public void neverWhenTheSwitchIsOff() { assertFalse(BatterySaverPolicy.shouldTurnOff(false, true, false, true, NOW, NONE)); }
    @Test public void neverWhileAutomationIsPaused() { assertFalse(BatterySaverPolicy.shouldTurnOff(true, true, true, true, NOW, NONE)); }
    @Test public void nothingToDoWhenItIsAlreadyOff() { assertFalse(BatterySaverPolicy.shouldTurnOff(true, true, false, false, NOW, NONE)); }
    @Test public void aRomThatInsistsIsNotFoughtInALoop() {
        long[] four = {NOW - 1000, NOW - 2000, NOW - 3000, NOW - 4000};
        assertFalse(BatterySaverPolicy.shouldTurnOff(true, true, false, true, NOW, four));
        long[] three = {NOW - 1000, NOW - 2000, NOW - 3000, 0};
        assertTrue(BatterySaverPolicy.shouldTurnOff(true, true, false, true, NOW, three));
    }
    @Test public void oldWritesDoNotCount() {
        long[] old = {NOW - 61_000, NOW - 70_000, NOW - 80_000, NOW - 90_000};
        assertTrue(BatterySaverPolicy.shouldTurnOff(true, true, false, true, NOW, old));
    }
    @Test public void aMissingHistoryIsTreatedAsEmpty() { assertTrue(BatterySaverPolicy.shouldTurnOff(true, true, false, true, NOW, null)); }

    // ---- AN1: the tile in the shade is the boss between launcher changes
    @Test public void aChangeBehindOurBackIsLeftAloneUnlessStrict() {
        assertFalse(BatterySaverPolicy.mayAct(true, false));   // the tile / Android changed it: hands off
        assertTrue(BatterySaverPolicy.mayAct(true, true));     // strict = the old behaviour
        assertTrue(BatterySaverPolicy.mayAct(false, false));   // a real launcher change always applies the rule
        assertTrue(BatterySaverPolicy.mayAct(false, true));
    }
    @Test public void aProcessRestartLaterNeverUndoesATap() {
        assertTrue(BatterySaverPolicy.startCheckWanted(false, 30_000));                                   // just booted: apply the rule once
        assertTrue(BatterySaverPolicy.startCheckWanted(false, BatterySaverPolicy.BOOT_GRACE_MS - 1));
        assertFalse(BatterySaverPolicy.startCheckWanted(false, BatterySaverPolicy.BOOT_GRACE_MS));        // minutes later: a Go device killed and restarted us, not a boot
        assertFalse(BatterySaverPolicy.startCheckWanted(false, 6 * 3_600_000L));
        assertTrue(BatterySaverPolicy.startCheckWanted(true, 6 * 3_600_000L));                            // strict: every start, as before
        assertFalse(BatterySaverPolicy.startCheckWanted(false, -1));                                      // a clock that makes no sense never applies it
    }

    // ---- AZ2: Quickstep is stock; only the arrival of the custom launcher runs the guard
    @Test public void onlyTheCustomLauncherRunsTheGuard() {
        assertTrue(BatterySaverPolicy.modeSetChecks(true, true, false));    // Quickstep -> custom: the saver is switched off
        assertFalse(BatterySaverPolicy.modeSetChecks(false, true, false));  // custom -> Quickstep: NOTHING is written (the old switch-ON left Android's state machine out of step)
        assertFalse(BatterySaverPolicy.modeSetChecks(false, true, true));   // not even in strict mode
        assertFalse(BatterySaverPolicy.modeSetChecks(false, false, true));
    }
    @Test public void repeatingCustomIsNotAChangeUnlessStrict() {
        assertFalse(BatterySaverPolicy.modeSetChecks(true, false, false));  // an unlock that sets custom again while custom is already active
        assertTrue(BatterySaverPolicy.modeSetChecks(true, false, true));
    }
    @Test public void theRetiredKeysStayNamedSoOldPreferenceFilesAreUnderstood() {
        assertEquals("profile_battery_saver_on_quickstep", BatterySaverPolicy.QS_KEY);
        assertEquals("profile_battery_saver_strict", BatterySaverPolicy.STRICT_KEY);
        assertTrue(BatterySaverPolicy.BOOT_GRACE_MS >= 60_000 && BatterySaverPolicy.BOOT_GRACE_MS <= 10 * 60_000);
    }

    // ---- AZ2: the self-heal of a swallowed "turn ON"
    @Test public void aGrowingCounterIsARequest() {
        assertTrue(BatterySaverPolicy.activationRequested(3, 4));
        assertFalse(BatterySaverPolicy.activationRequested(4, 4));          // same value: a different change of that setting
        assertFalse(BatterySaverPolicy.activationRequested(5, 2));          // a reset of the setting is not a request
        assertFalse(BatterySaverPolicy.activationRequested(-1, 7));         // first read of this process: never a request
        assertTrue(BatterySaverPolicy.activationRequested(0, 1));
    }
    @Test public void aSwallowedRequestIsCarriedOut() {
        assertTrue(BatterySaverPolicy.healWanted(false, false, NOW, null));                         // asked to turn ON, still OFF, on battery
        assertTrue(BatterySaverPolicy.healWanted(false, false, NOW, new long[] {0, 0, 0, 0, 0, 0}));
    }
    @Test public void anAcceptedRequestNeedsNoHelp() { assertFalse(BatterySaverPolicy.healWanted(true, false, NOW, null)); }
    @Test public void neverWhileCharging() {
        assertFalse(BatterySaverPolicy.healWanted(false, true, NOW, null));   // Android 10 will not run the saver on charge (and does not count the request)
        assertTrue(BatterySaverPolicy.charging("ac")); assertTrue(BatterySaverPolicy.charging("usb")); assertTrue(BatterySaverPolicy.charging("wireless")); assertTrue(BatterySaverPolicy.charging("other"));
        assertFalse(BatterySaverPolicy.charging("no")); assertFalse(BatterySaverPolicy.charging("?")); assertFalse(BatterySaverPolicy.charging(null));
    }
    @Test public void aPersonTappingIsNeverALoop() {
        long[] six = {NOW - 1000, NOW - 2000, NOW - 3000, NOW - 4000, NOW - 5000, NOW - 6000};
        assertFalse(BatterySaverPolicy.healWanted(false, false, NOW, six));
        long[] five = {NOW - 1000, NOW - 2000, NOW - 3000, NOW - 4000, NOW - 5000, 0};
        assertTrue(BatterySaverPolicy.healWanted(false, false, NOW, five));
        long[] old = {NOW - 61_000, NOW - 70_000, NOW - 80_000, NOW - 90_000, NOW - 100_000, NOW - 110_000};
        assertTrue(BatterySaverPolicy.healWanted(false, false, NOW, old));
    }
    @Test public void theCounterIsAndroidsOwn() { assertEquals("low_power_manual_activation_count", BatterySaverPolicy.COUNT_SETTING); }

    // ---- AZ2: the one-time clean-up of a saver the old versions forced ON
    @Test public void aSaverTheOldGuardForcedOnIsClearedOnce() {
        String note = "20:11:05  switched battery saver ON in Quickstep (Quickstep became active)";
        assertTrue(BatterySaverPolicy.unstickWanted(false, true, note));
        assertFalse(BatterySaverPolicy.unstickWanted(true, true, note));                 // already done
        assertFalse(BatterySaverPolicy.unstickWanted(false, false, note));               // not on: nothing stuck
        assertFalse(BatterySaverPolicy.unstickWanted(false, true, "20:11:05  switched battery saver off in the custom launcher (x)"));   // somebody turned it on by hand later: leave it
        assertFalse(BatterySaverPolicy.unstickWanted(false, true, ""));
        assertFalse(BatterySaverPolicy.unstickWanted(false, true, null));
    }
    @Test public void theMarkMatchesWhatTheOldGuardWrote() {
        // v0.7.5 - v0.8.0 stored: stamp + "  switched battery saver ON in Quickstep (" + why + ")"
        assertTrue(("12:00:00  switched battery saver ON in Quickstep (the process start)").contains(BatterySaverPolicy.FORCED_ON_MARK));
    }

    // ---- the setting value is parsed strictly
    @Test public void theSettingValueIsParsedStrictly() {
        assertTrue(BatterySaverPolicy.isOn("1")); assertTrue(BatterySaverPolicy.isOn(" 1 "));
        assertFalse(BatterySaverPolicy.isOn("0")); assertFalse(BatterySaverPolicy.isOn(null)); assertFalse(BatterySaverPolicy.isOn("")); assertFalse(BatterySaverPolicy.isOn("true"));
    }
    @Test public void theSettingNamesAreAndroidsOwn() {
        assertEquals("low_power", BatterySaverPolicy.SETTING);
        assertEquals("low_power_sticky", BatterySaverPolicy.STICKY_SETTING);
        assertEquals("profile_battery_saver_off", BatterySaverPolicy.KEY);
    }

    // ================================================================ BA2 (Tj, 2026-11-07): the lock screen keeps the saver off, Quickstep's state comes back after a gesture unlock

    // ---- off at the lock moment, kept off while locked
    @Test public void theLockMomentSwitchesItOffFromAnyLauncher() {
        assertTrue(BatterySaverPolicy.lockOffWanted(true, true, false, true));
        assertFalse(BatterySaverPolicy.lockOffWanted(false, true, false, true));   // switch off
        assertFalse(BatterySaverPolicy.lockOffWanted(true, false, false, true));   // not locked: a plain wake must keep Quickstep's saver
        assertFalse(BatterySaverPolicy.lockOffWanted(true, true, true, true));     // automation paused (breaker / Restore everything)
        assertFalse(BatterySaverPolicy.lockOffWanted(true, true, false, false));   // nothing to switch off
    }
    @Test public void aSaverTurnedOnWhileLockedIsSwitchedOffAgain() {
        assertTrue(BatterySaverPolicy.keepOffWanted(true, true, false, true, NOW, 0, null));
        assertTrue(BatterySaverPolicy.keepOffWanted(true, true, false, true, NOW, 0, new long[] {0, 0, 0, 0, 0, 0}));
    }
    @Test public void keepOffNeverActsOutsideTheLockScreenOrWhenSwitchedOff() {
        assertFalse(BatterySaverPolicy.keepOffWanted(true, false, false, true, NOW, 0, null));
        assertFalse(BatterySaverPolicy.keepOffWanted(false, true, false, true, NOW, 0, null));
        assertFalse(BatterySaverPolicy.keepOffWanted(true, true, true, true, NOW, 0, null));
        assertFalse(BatterySaverPolicy.keepOffWanted(true, true, false, false, NOW, 0, null));
    }
    @Test public void keepOffIsASafetyNetNotALoop() {
        long[] six = {NOW - 1000, NOW - 2000, NOW - 3000, NOW - 4000, NOW - 5000, NOW - 6000};
        assertFalse(BatterySaverPolicy.keepOffWanted(true, true, false, true, NOW, 0, six));
        long[] five = {NOW - 1000, NOW - 2000, NOW - 3000, NOW - 4000, NOW - 5000, 0};
        assertTrue(BatterySaverPolicy.keepOffWanted(true, true, false, true, NOW, 0, five));
        long[] old = {NOW - 61_000, NOW - 70_000, NOW - 80_000, NOW - 90_000, NOW - 100_000, NOW - 110_000};
        assertTrue(BatterySaverPolicy.keepOffWanted(true, true, false, true, NOW, 0, old));
    }
    @Test public void aRestoreIsNeverUndoneByTheObserverWhileTheKeyguardCatchesUp() {
        assertFalse(BatterySaverPolicy.keepOffWanted(true, true, false, true, NOW, NOW - 1000, null));                                          // 1 s after our own restore: the keyguard may still say 'locked'
        assertFalse(BatterySaverPolicy.keepOffWanted(true, true, false, true, NOW, NOW - (BatterySaverPolicy.RESTORE_QUIET_MS - 1), null));
        assertTrue(BatterySaverPolicy.keepOffWanted(true, true, false, true, NOW, NOW - BatterySaverPolicy.RESTORE_QUIET_MS, null));            // a real new lock a few seconds later is looked after again
    }

    // ---- what is remembered as Quickstep's state
    @Test public void onlyQuickstepsOwnStateIsRemembered() {
        assertEquals("1", BatterySaverPolicy.qsStateAtHold(true, false, true, ""));      // locked from Quickstep with the saver on
        assertEquals("0", BatterySaverPolicy.qsStateAtHold(true, false, false, "1"));    // locked from Quickstep with it off: a stale "1" is cleared
        assertEquals("1", BatterySaverPolicy.qsStateAtHold(false, false, false, "1"));   // locked from the custom launcher: Quickstep's memory is not touched
        assertEquals("0", BatterySaverPolicy.qsStateAtHold(false, false, true, "0"));    // a saver somebody turned on in custom is not Quickstep's
    }
    @Test public void onceHeldTheGuardsOwnDoingIsNotRemembered() {
        assertEquals("1", BatterySaverPolicy.qsStateAtHold(true, true, false, "1"));     // second lock of the same hold: the saver is off because WE switched it off
        assertEquals("", BatterySaverPolicy.qsStateAtHold(true, true, true, null));
    }
    @Test public void whileQuickstepHasTheTabletTheLiveStateIsRemembered() {
        assertEquals("1", BatterySaverPolicy.qsStateLive(true, false, false, true, "0"));
        assertEquals("0", BatterySaverPolicy.qsStateLive(true, false, false, false, "1"));
        assertEquals("1", BatterySaverPolicy.qsStateLive(false, false, false, false, "1"));   // custom launcher: not Quickstep's
        assertEquals("1", BatterySaverPolicy.qsStateLive(true, true, false, false, "1"));     // on the lock screen: not Quickstep's
        assertEquals("1", BatterySaverPolicy.qsStateLive(true, false, true, false, "1"));     // our own write is not a choice of the person
    }

    // ---- the evidence that Android's state machine was not reset
    @Test public void aRiseOfTheBatteryLevelMeansItWasCharged() {
        assertTrue(BatterySaverPolicy.levelRose(40, 43));
        assertTrue(BatterySaverPolicy.levelRose(40, 90));
        assertFalse(BatterySaverPolicy.levelRose(40, 42));    // measurement jitter
        assertFalse(BatterySaverPolicy.levelRose(40, 35));    // discharging
        assertFalse(BatterySaverPolicy.levelRose(-1, 90));    // unknown
        assertFalse(BatterySaverPolicy.levelRose(40, -1));
    }
    @Test public void aRestartIsRecognisedByTheBootEpoch() {
        long boot = 1_700_000_000_000L;
        assertTrue(BatterySaverPolicy.sameBoot(boot, boot + 1500));                                     // clock rounding / a small NTP step
        assertTrue(BatterySaverPolicy.sameBoot(boot, boot - BatterySaverPolicy.BOOT_TOLERANCE_MS));
        assertFalse(BatterySaverPolicy.sameBoot(boot, boot + 3_600_000L));                              // a restart an hour later
        assertFalse(BatterySaverPolicy.sameBoot(0, boot));                                              // nothing recorded: not the same
        assertFalse(BatterySaverPolicy.sameBoot(boot, 0));
    }

    // ---- the restore decision: every doubt is a refusal
    private static BatterySaverPolicy.Restore go() {
        BatterySaverPolicy.Restore s = new BatterySaverPolicy.Restore();
        s.switchOn = true; s.restoreOn = true; s.automationOff = false; s.permitted = true; s.quickstepGesture = true;
        s.held = true; s.qsOn = "1"; s.saverOnNow = false; s.keyguardLocked = false; s.chargingOrUnknown = false; s.plugSeen = false;
        s.heldBootEpoch = 1_700_000_000_000L; s.nowBootEpoch = 1_700_000_000_500L; s.now = NOW; s.recentRestores = null;
        return s;
    }
    @Test public void aGestureUnlockIntoQuicksteptBringsItsSaverBack() { assertEquals("", BatterySaverPolicy.restoreRefusal(go())); }
    @Test public void aPlainUnlockNeverRestores() { BatterySaverPolicy.Restore s = go(); s.quickstepGesture = false; assertFalse(BatterySaverPolicy.restoreRefusal(s).isEmpty()); }
    @Test public void bothSwitchesMustBeOn() {
        BatterySaverPolicy.Restore s = go(); s.switchOn = false; assertFalse(BatterySaverPolicy.restoreRefusal(s).isEmpty());
        s = go(); s.restoreOn = false; assertFalse(BatterySaverPolicy.restoreRefusal(s).isEmpty());
    }
    @Test public void pausedOrWithoutPermissionNothingHappens() {
        BatterySaverPolicy.Restore s = go(); s.automationOff = true; assertFalse(BatterySaverPolicy.restoreRefusal(s).isEmpty());
        s = go(); s.permitted = false; assertFalse(BatterySaverPolicy.restoreRefusal(s).isEmpty());
    }
    @Test public void nothingIsRestoredThatQuickstepDidNotHave() {
        BatterySaverPolicy.Restore s = go(); s.qsOn = "0"; assertFalse(BatterySaverPolicy.restoreRefusal(s).isEmpty());
        s = go(); s.qsOn = ""; assertFalse(BatterySaverPolicy.restoreRefusal(s).isEmpty());
        s = go(); s.qsOn = null; assertFalse(BatterySaverPolicy.restoreRefusal(s).isEmpty());
        s = go(); s.held = false; assertFalse(BatterySaverPolicy.restoreRefusal(s).isEmpty());          // the launcher switched nothing off: nothing to bring back
        s = go(); s.saverOnNow = true; assertFalse(BatterySaverPolicy.restoreRefusal(s).isEmpty());     // somebody already turned it on
    }
    @Test public void nothingIsWrittenWhileTheLockScreenIsStillUp() { BatterySaverPolicy.Restore s = go(); s.keyguardLocked = true; assertFalse(BatterySaverPolicy.restoreRefusal(s).isEmpty()); }
    @Test public void chargingOrAnUnknownPowerStateIsARefusal() { BatterySaverPolicy.Restore s = go(); s.chargingOrUnknown = true; assertFalse(BatterySaverPolicy.restoreRefusal(s).isEmpty()); }
    @Test public void aPlugInSinceTheSwitchOffIsARefusalBecauseAndroidsStateWasReset() {
        BatterySaverPolicy.Restore s = go(); s.plugSeen = true;
        String why = BatterySaverPolicy.restoreRefusal(s);
        assertFalse(why.isEmpty()); assertTrue(why.contains("plugged in"));
    }
    @Test public void aRestartSinceTheSwitchOffIsARefusal() {
        BatterySaverPolicy.Restore s = go(); s.nowBootEpoch = s.heldBootEpoch + 7_200_000L;
        String why = BatterySaverPolicy.restoreRefusal(s);
        assertFalse(why.isEmpty()); assertTrue(why.contains("restarted"));
        s = go(); s.heldBootEpoch = 0; assertFalse(BatterySaverPolicy.restoreRefusal(s).isEmpty());     // no boot recorded: unknown = no
    }
    @Test public void restoresAreRateLimited() {
        BatterySaverPolicy.Restore s = go(); s.recentRestores = new long[] {NOW - 1000, NOW - 2000, NOW - 3000};
        assertFalse(BatterySaverPolicy.restoreRefusal(s).isEmpty());
        s.recentRestores = new long[] {NOW - 1000, NOW - 2000, 0};
        assertEquals("", BatterySaverPolicy.restoreRefusal(s));
        s.recentRestores = new long[] {NOW - 400_000, NOW - 500_000, NOW - 600_000};
        assertEquals("", BatterySaverPolicy.restoreRefusal(s));
    }
    @Test public void theFirstDoubtDecidesAndTheSwitchWinsOverEverything() {
        BatterySaverPolicy.Restore s = go(); s.switchOn = false; s.plugSeen = true; s.chargingOrUnknown = true;
        assertTrue(BatterySaverPolicy.restoreRefusal(s).contains("switched off"));
    }
    @Test public void theMemoryKeysAreStable() {
        assertEquals("profile_battery_saver_lock_off", BatterySaverPolicy.LOCK_OFF_KEY);
        assertEquals("profile_battery_saver_lock_restore", BatterySaverPolicy.LOCK_RESTORE_KEY);
        assertEquals("bs_qs_on", BatterySaverPolicy.QS_ON_KEY);
        assertEquals("bs_held", BatterySaverPolicy.HELD_KEY);
    }
    @Test public void theDescriptionSaysWhatIsRemembered() {
        assertTrue(BatterySaverPolicy.describe("1", true, false).contains("ON"));
        assertTrue(BatterySaverPolicy.describe("0", false, false).contains("off"));
        assertTrue(BatterySaverPolicy.describe("", false, false).contains("unknown"));
        assertTrue(BatterySaverPolicy.describe("1", true, true).contains("plugged in"));
    }
}
