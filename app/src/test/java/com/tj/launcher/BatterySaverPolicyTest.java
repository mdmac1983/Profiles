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
}
