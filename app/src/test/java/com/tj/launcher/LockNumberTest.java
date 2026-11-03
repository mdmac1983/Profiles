package com.tj.launcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class LockNumberTest {
    @Test public void shownOnTheLockScreen() { assertTrue(LockNumber.shouldShow(true, true, true, false)); }
    @Test public void hiddenWhenThePinPadIsUp() { assertFalse(LockNumber.shouldShow(true, true, true, true)); }
    @Test public void hiddenWhenUnlocked() { assertFalse(LockNumber.shouldShow(true, true, false, false)); }
    @Test public void hiddenWhenTheScreenIsOff() { assertFalse(LockNumber.shouldShow(true, false, true, false)); }
    @Test public void hiddenWhenSwitchedOff() { assertFalse(LockNumber.shouldShow(false, true, true, false)); }
    @Test public void measuredLookIsSane() {
        // 14 Roboto digits (0.5615 em each) must be 304/957 of the screen width
        double w = 14 * 0.5615 * LockNumber.EM_PER_WIDTH;
        assertTrue(Math.abs(w - 304.0 / 957.0) < 0.003);
        assertTrue(LockNumber.BASELINE_PER_HEIGHT > 0.85 && LockNumber.BASELINE_PER_HEIGHT < 0.95);
        assertTrue(LockNumber.DEFAULT_TEXT.equals("71125011907547"));
    }
}
