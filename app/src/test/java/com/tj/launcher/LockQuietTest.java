package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** AK1: the lock screen shows no Wi-Fi, no Bluetooth and no app notifications, from any launcher. The Android side is proven by emulator section lq; these are the pure rules. */
public class LockQuietTest {
    private static final long NOW = 1_000_000;
    private static final long[] NONE = {0, 0, 0};

    @Test public void aRadioThatComesOnWhileLockedIsSwitchedOff() { assertTrue(LockQuiet.enforceRadios(true, true, false, NOW, NONE)); }
    @Test public void neverWhileTheTabletIsUnlocked() { assertFalse(LockQuiet.enforceRadios(true, false, false, NOW, NONE)); }
    @Test public void neverWithTheSwitchOff() { assertFalse(LockQuiet.enforceRadios(false, true, false, NOW, NONE)); }
    @Test public void neverWhileAutomationIsPaused() { assertFalse(LockQuiet.enforceRadios(true, true, true, NOW, NONE)); }
    @Test public void aRadioThatRefusesToStayOffIsNotFoughtInALoop() {
        long[] three = {NOW - 1000, NOW - 20_000, NOW - 59_000};
        assertFalse(LockQuiet.enforceRadios(true, true, false, NOW, three));
        long[] two = {NOW - 1000, NOW - 20_000, 0};
        assertTrue(LockQuiet.enforceRadios(true, true, false, NOW, two));
    }
    @Test public void oldSwitchOffsDoNotCountAndNullIsEmpty() {
        long[] old = {NOW - 61_000, NOW - 120_000, NOW - 300_000};
        assertTrue(LockQuiet.enforceRadios(true, true, false, NOW, old));
        assertTrue(LockQuiet.enforceRadios(true, true, false, NOW, null));
    }

    @Test public void theMemoryOfWhatWasOnRoundTrips() {
        assertEquals("w1b0", LockQuiet.encodePrev(true, false));
        assertEquals("w0b1", LockQuiet.encodePrev(false, true));
        assertTrue(LockQuiet.prevWifi("w1b0")); assertFalse(LockQuiet.prevBluetooth("w1b0"));
        assertTrue(LockQuiet.prevBluetooth("w0b1")); assertFalse(LockQuiet.prevWifi("w0b1"));
        assertFalse(LockQuiet.prevWifi(null)); assertFalse(LockQuiet.prevBluetooth(""));
    }

    @Test public void aGestureUnlockIntoQuickstepPutsWhatWasOnBack() { assertTrue(LockQuiet.shouldRestore(true, true, true, false, "w1b0")); }
    @Test public void aPlainUnlockIntoCustomLeavesThemOff() { assertFalse(LockQuiet.shouldRestore(true, true, false, false, "w1b1")); }
    @Test public void nothingToRestoreWhenNothingWasOn() { assertFalse(LockQuiet.shouldRestore(true, true, true, false, "w0b0")); assertFalse(LockQuiet.shouldRestore(true, true, true, false, "")); }
    @Test public void restoreNeedsBothSwitchesAndNoPause() {
        assertFalse(LockQuiet.shouldRestore(false, true, true, false, "w1b0"));
        assertFalse(LockQuiet.shouldRestore(true, false, true, false, "w1b0"));
        assertFalse(LockQuiet.shouldRestore(true, true, true, true, "w1b0"));
    }

    @Test public void theFirstLockMomentRecordsAndARadioThatIsOnNowRefreshesIt() {
        assertTrue(LockQuiet.shouldRecordPrev("", false, false));
        assertTrue(LockQuiet.shouldRecordPrev(null, false, false));
        assertFalse("the second lock moment of the same lock keeps the first memory", LockQuiet.shouldRecordPrev("w1b0", false, false));
        assertTrue("a radio that is on right now is the freshest truth", LockQuiet.shouldRecordPrev("w0b0", true, false));
    }

    @Test public void theNamesAreAndroidsOwnAndTheSwitchKeysAreStable() {
        assertEquals("lock_screen_show_notifications", LockQuiet.SHOW);
        assertEquals("lock_screen_allow_private_notifications", LockQuiet.PRIVATE);
        assertEquals("lock_quiet", LockQuiet.KEY);
        assertEquals("lock_quiet_restore", LockQuiet.RESTORE_KEY);
    }
}
