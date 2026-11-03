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
        // 6.png (tablet, v0.3.2, em 0.04045 x 600 px): bbox 180 px wide; 5.png scaled: 190.6 px => em must grow by 1.059..1.07
        assertTrue(LockNumber.EM_PER_WIDTH > 0.04045f * 1.05f && LockNumber.EM_PER_WIDTH < 0.04045f * 1.08f);
        assertTrue(LockNumber.BASELINE_PER_HEIGHT > 0.85 && LockNumber.BASELINE_PER_HEIGHT < 0.95);
        assertTrue(LockNumber.WEIGHT == 300);   // Light
        assertTrue(LockNumber.ALPHA > 170 && LockNumber.ALPHA < 190);   // ~71 % white (7.png)
        assertTrue(Math.abs(LockNumber.BASELINE_PER_HEIGHT * 1024 - 925.0) < 1.5);   // 7.png: digits end at y=925
        assertTrue(LockNumber.DEFAULT_TEXT.equals("71125011907547"));
    }
}
