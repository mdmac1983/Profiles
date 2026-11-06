package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class LockNumberTest {
    @Test public void shownOnTheLockScreen() { assertTrue(LockNumber.shouldShow(true, true, true, false)); }
    @Test public void hiddenWhenThePinPadIsUp() { assertFalse(LockNumber.shouldShow(true, true, true, true)); }
    @Test public void hiddenWhenUnlocked() { assertFalse(LockNumber.shouldShow(true, true, false, false)); }
    @Test public void hiddenWhenTheScreenIsOff() { assertFalse(LockNumber.shouldShow(true, false, true, false)); }
    @Test public void hiddenWhenSwitchedOff() { assertFalse(LockNumber.shouldShow(false, true, true, false)); }
    // v0.7.6: Tj's nudges on top of the measured look
    @Test public void noNudgeMeansTheMeasuredLook() {
        assertEquals(600f * LockNumber.EM_PER_WIDTH, LockNumber.emFor(600f, 0), 0.001f);
        assertEquals(LockNumber.WEIGHT, LockNumber.weightFor(0));
        assertEquals(LockNumber.ALPHA, LockNumber.alphaFor(0));
    }
    @Test public void theNudgesAreClampedAndMonotonic() {
        assertEquals(LockNumber.emFor(600f, 30), LockNumber.emFor(600f, 99), 0.001f);
        assertEquals(LockNumber.emFor(600f, -30), LockNumber.emFor(600f, -99), 0.001f);
        assertTrue(LockNumber.emFor(600f, 10) > LockNumber.emFor(600f, 0) && LockNumber.emFor(600f, 0) > LockNumber.emFor(600f, -10));
        assertEquals(LockNumber.weightFor(4), LockNumber.weightFor(99));
        assertEquals(LockNumber.weightFor(-2), LockNumber.weightFor(-99));
        assertTrue(LockNumber.weightFor(1) == LockNumber.weightFor(0) + 100 && LockNumber.weightFor(-2) >= 100 && LockNumber.weightFor(4) <= 900);
        assertTrue(LockNumber.alphaFor(10) > LockNumber.alphaFor(0) || LockNumber.alphaFor(0) == 255);
        assertTrue(LockNumber.alphaFor(-99) >= 60 && LockNumber.alphaFor(99) <= 255);
    }
    @Test public void measuredLookIsSane() {
        // v0.7.6, side-by-side photos (reference and ours in one image): the stock number is 223.3 px wide on a 600 px screen, digit pitch 16.0, Medium-ish strokes, baseline 0.916 of the height, centre 299.6
        assertTrue(LockNumber.EM_PER_WIDTH > 0.0460f && LockNumber.EM_PER_WIDTH < 0.0480f);                 // em 27.6 .. 28.8 px at 600 px
        assertTrue(LockNumber.EM_PER_WIDTH * 600 > 0.0429f * 600 * 1.05f);                                  // clearly bigger than the old (too small) 0.0429
        assertTrue(LockNumber.BASELINE_PER_HEIGHT > 0.912 && LockNumber.BASELINE_PER_HEIGHT < 0.920);       // 934 .. 942 px of 1024
        assertTrue(LockNumber.WEIGHT == 500);                                                               // Medium (Regular 10 %, Medium 55-60 %, Bold 30 % from the stem fit); Tj can nudge it
        assertTrue(LockNumber.ALPHA >= 180 && LockNumber.ALPHA <= 215);
        assertTrue(Math.abs(LockNumber.X_NUDGE_PER_WIDTH) < 0.002);                                         // centred
        assertTrue(LockNumber.DEFAULT_TEXT.equals("71125011907547"));
    }
}
