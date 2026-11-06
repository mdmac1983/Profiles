package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** AC3: the battery-saver guard's rules. The guard itself needs Android; emulator section "ac" proves it on a real Android 10 system. */
public class BatterySaverPolicyTest {
    private static final long NOW = 1_000_000;
    private static final long[] NONE = {0, 0, 0, 0};

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
    // AG1: Quickstep turns the saver ON and keeps it on
    private static final long[] NO_ON = {0, 0, 0};
    @Test public void quickstepSwitchesItOn() { assertTrue(BatterySaverPolicy.shouldTurnOn(true, false, false, false, NOW, NO_ON)); }
    @Test public void neverInTheCustomLauncherOn() { assertFalse(BatterySaverPolicy.shouldTurnOn(true, true, false, false, NOW, NO_ON)); }
    @Test public void onlyWhenTheQuickstepSwitchIsOn() { assertFalse(BatterySaverPolicy.shouldTurnOn(false, false, false, false, NOW, NO_ON)); }
    @Test public void onNeverWhileAutomationIsPaused() { assertFalse(BatterySaverPolicy.shouldTurnOn(true, false, true, false, NOW, NO_ON)); }
    @Test public void nothingToDoWhenItIsAlreadyOn() { assertFalse(BatterySaverPolicy.shouldTurnOn(true, false, false, true, NOW, NO_ON)); }
    @Test public void androidTurningItOffByItselfIsNotFoughtInATightLoop() {
        long[] three = {NOW - 1000, NOW - 60_000, NOW - 300_000};
        assertFalse(BatterySaverPolicy.shouldTurnOn(true, false, false, false, NOW, three));
        long[] two = {NOW - 1000, NOW - 60_000, 0};
        assertTrue(BatterySaverPolicy.shouldTurnOn(true, false, false, false, NOW, two));
    }
    @Test public void oldSwitchOnsDoNotCount() {
        long[] old = {NOW - 11 * 60_000, NOW - 20 * 60_000, NOW - 30 * 60_000};
        assertTrue(BatterySaverPolicy.shouldTurnOn(true, false, false, false, NOW, old));
        assertTrue(BatterySaverPolicy.shouldTurnOn(true, false, false, false, NOW, null));
    }
    @Test public void theFollowRuleIsExclusive() {   // never both directions for the same state
        for (boolean custom : new boolean[] { true, false }) for (boolean on : new boolean[] { true, false }) {
            boolean off = BatterySaverPolicy.shouldTurnOff(true, custom, false, on, NOW, NONE);
            boolean up = BatterySaverPolicy.shouldTurnOn(true, custom, false, on, NOW, NO_ON);
            assertFalse("custom=" + custom + " saverOn=" + on, off && up);
        }
        assertEquals("profile_battery_saver_on_quickstep", BatterySaverPolicy.QS_KEY);
    }
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
