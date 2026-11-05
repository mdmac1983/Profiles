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
