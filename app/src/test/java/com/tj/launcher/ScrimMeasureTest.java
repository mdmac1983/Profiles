package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** BA1: when the helper measures the stock menu's scrim, how often, and where it samples. The Android side is proved by an emulator section (pt phase C). */
public class ScrimMeasureTest {
    private static ScrimMeasure.State go() {
        ScrimMeasure.State s = new ScrimMeasure.State();
        s.switchOn = true; s.coverOn = true; s.lockPatchOn = true; s.permitted = true; s.geometryKnown = true; s.validCrop = true;
        s.interactive = true; s.keyguardLocked = true; s.now = 1_000_000; s.screenOnMs = 900_000; s.lastStartMs = 0;
        return s;
    }

    // ---- the decision
    @Test public void aPlainLockScreenWithACalibratedPatchStarts() { assertTrue(ScrimMeasure.shouldStart(go())); assertEquals("", ScrimMeasure.decision(go())); }
    @Test public void nothingStartsOnceThereIsAMeasurement() { ScrimMeasure.State s = go(); s.haveMeasurement = true; assertFalse(ScrimMeasure.shouldStart(s)); }
    @Test public void theSwitchAndThePauseWin() {
        ScrimMeasure.State s = go(); s.switchOn = false; assertFalse(ScrimMeasure.shouldStart(s)); assertTrue(ScrimMeasure.decision(s).contains("switched off"));
        s = go(); s.paused = true; assertFalse(ScrimMeasure.shouldStart(s));
        s = go(); s.coverOn = false; assertFalse(ScrimMeasure.shouldStart(s));
        s = go(); s.lockPatchOn = false; assertFalse(ScrimMeasure.shouldStart(s));
        s = go(); s.permitted = false; assertFalse(ScrimMeasure.shouldStart(s));
    }
    @Test public void itWaitsForTheCalibrationAndTheMenuGeometry() {
        ScrimMeasure.State s = go(); s.validCrop = false; assertTrue(ScrimMeasure.decision(s).contains("calibration"));
        s = go(); s.geometryKnown = false; assertTrue(ScrimMeasure.decision(s).contains("side key"));
    }
    @Test public void itNeverStartsOnAnythingButAPlainLockScreen() {
        ScrimMeasure.State s = go(); s.interactive = false; assertTrue(ScrimMeasure.decision(s).contains("screen is off"));
        s = go(); s.keyguardLocked = false; assertFalse(ScrimMeasure.shouldStart(s));
        s = go(); s.pinPadUp = true; assertTrue(ScrimMeasure.decision(s).contains("PIN pad"));
        s = go(); s.menuOpen = true; assertTrue(ScrimMeasure.decision(s).contains("menu is open"));
        s = go(); s.handshakeArmed = true; assertTrue(ScrimMeasure.decision(s).contains("gesture"));
        s = go(); s.calibrating = true; assertFalse(ScrimMeasure.shouldStart(s));
        s = go(); s.stageRunning = true; assertFalse(ScrimMeasure.shouldStart(s));
    }
    @Test public void theLockScreenSettlesFirst() {
        ScrimMeasure.State s = go(); s.screenOnMs = s.now - 1000; assertFalse(ScrimMeasure.shouldStart(s));
        s.screenOnMs = s.now - ScrimMeasure.SETTLE_AFTER_WAKE_MS; assertTrue(ScrimMeasure.shouldStart(s));
        s.screenOnMs = 0; assertTrue(ScrimMeasure.shouldStart(s));   // unknown: not a reason to wait
    }
    @Test public void triesAreLimited() {
        ScrimMeasure.State s = go(); s.lastStartMs = s.now - 10_000; assertTrue(ScrimMeasure.decision(s).contains("just now"));
        s.lastStartMs = s.now - ScrimMeasure.MIN_GAP_MS; assertTrue(ScrimMeasure.shouldStart(s));
        s = go(); s.startsThisLock = ScrimMeasure.MAX_PER_LOCK; assertTrue(ScrimMeasure.decision(s).contains("this lock"));
        s = go(); s.startsToday = ScrimMeasure.MAX_PER_DAY; assertTrue(ScrimMeasure.decision(s).contains("today"));
    }
    // ---- review of BA1: a lock screen that cannot be fitted is not retried every day
    @Test public void aLockScreenThatCannotBeFittedIsGivenUpAfterTwoTries() {
        ScrimMeasure.State s = go(); s.failures = 1; assertTrue(ScrimMeasure.shouldStart(s));
        s.failures = ScrimMeasure.MAX_FAILURES; assertFalse(ScrimMeasure.shouldStart(s)); assertTrue(ScrimMeasure.decision(s).contains("gave up for this lock screen"));
        s.haveMeasurement = true; assertEquals("", ScrimMeasure.decision(s));   // a measurement that exists always wins
    }
    @Test public void failuresAreCountedPerLockScreenAndTheme() {
        String sig = PatchShot.sig(7, 0, 600, 1024), other = PatchShot.sig(8, 0, 600, 1024);
        assertEquals(0, ScrimMeasure.failures("", sig, false)); assertEquals(0, ScrimMeasure.failures(null, sig, false));
        String one = ScrimMeasure.addFailure("", sig, false); assertEquals(1, ScrimMeasure.failures(one, sig, false));
        String two = ScrimMeasure.addFailure(one, sig, false); assertEquals(2, ScrimMeasure.failures(two, sig, false));
        assertEquals(0, ScrimMeasure.failures(two, other, false));       // another wallpaper starts afresh
        assertEquals(0, ScrimMeasure.failures(two, sig, true));          // the dark theme is another lock screen
        assertEquals(1, ScrimMeasure.failures(ScrimMeasure.addFailure(two, other, false), other, false));   // and a record of another lock screen is replaced, not added to
        assertEquals(0, ScrimMeasure.failures("garbage", sig, false)); assertEquals(0, ScrimMeasure.failures(sig + "#d#abc", sig, false));
    }
    @Test public void onlyFramesThatCouldNotBeFittedCountAsFailures() {
        assertTrue(ScrimMeasure.measurementFailure("not measured: the fit is not tight enough (rms 4.1 levels)"));
        assertTrue(ScrimMeasure.measurementFailure("stopped: too little plain wallpaper on this lock screen to measure (120 usable points)"));
        assertFalse(ScrimMeasure.measurementFailure("stopped: the screen went off"));
        assertFalse(ScrimMeasure.measurementFailure("stopped: the PIN pad came up"));
        assertFalse(ScrimMeasure.measurementFailure("MEASURED scale 0.800 / 0.800 / 0.800"));
        assertFalse(ScrimMeasure.measurementFailure(null));
        assertEquals("scrim_fail_r0", ScrimMeasure.failKey(0));
    }
    @Test public void theSecondFrameWaitsForTheWholeFadeAndMore() { assertTrue(ScrimMeasure.OPEN_SETTLE_MS >= PatchFade.OPEN_MS + 800); }
    @Test public void aDebugBuildDoesNotMeasureUnlessAskedTo() { assertFalse(ScrimMeasure.defaultOn(true)); assertTrue(ScrimMeasure.defaultOn(false)); }
    @Test public void theValueKeyIsPerRotation() { assertEquals("patch_scrim_r0", ScrimMeasure.valueKey(0)); assertEquals("patch_scrim_r3", ScrimMeasure.valueKey(3)); }

    // ---- the sampling grid
    @Test public void theGridSkipsTheExcludedRectangles() {
        int[][] ex = { { 0, 0, 600, 36 }, { 300, 300, 500, 500 } };
        int[] g = ScrimMeasure.grid(600, 1024, 4, ex);
        assertTrue(g.length > 10_000);
        for (int v : g) {
            int x = v & 0xFFFF, y = v >>> 16;
            assertTrue(y >= 36);
            assertFalse(x >= 300 && x < 500 && y >= 300 && y < 500);
        }
    }
    @Test public void theGridDecodesToValidPixels() {
        int[] g = ScrimMeasure.grid(600, 1024, 4, null);
        for (int v : g) { int x = v & 0xFFFF, y = v >>> 16; assertTrue(x >= 1 && x < 599 && y >= 1 && y < 1023); }
    }
    @Test public void theExclusionRectanglesCoverTheBarsTheNumberThePanelAndThePatch() {
        int[][] r = ScrimMeasure.excludeRects(600, 1024, new int[] { 300, 100, 560, 400 }, new int[] { 300, 330, 560, 420 }, 20);
        assertEquals(5, r.length);
        assertEquals(280, r[3][0]); assertEquals(80, r[3][1]); assertEquals(580, r[3][2]); assertEquals(420, r[3][3]);   // panel + 20 px shadow
        assertEquals(294, r[4][0]);                                                                                      // patch + 6 px
        assertEquals(3, ScrimMeasure.excludeRects(600, 1024, null, null, 20).length);
    }
    @Test public void theNumberRectScalesWithTheScreen() {
        int[] a = ScrimMeasure.numberRect(600, 1024), b = ScrimMeasure.numberRect(1200, 2048);
        assertEquals(a[0] * 2, b[0], 1); assertEquals(a[3] * 2, b[3], 1);
        assertTrue(a[0] < 205 && a[2] > 396 && a[1] < 907 && a[3] > 924);   // it contains the measured bbox
    }
    @Test public void theBareFramePicksFlatPixelsOutsideTheExclusions() {
        int w = 200, h = 300;
        int[] px = new int[w * h];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) px[y * w + x] = 0xFF000000 | ((x * 255 / w) << 16) | ((y * 255 / h) << 8) | 90;   // a smooth gradient: every pixel is flat
        int[][] ex = { { 0, 0, w, 10 }, { 100, 100, 150, 150 } };
        int[] pts = ScrimMeasure.pickPoints(px, w, h, ex);
        assertTrue(pts.length > 1000);
        for (int v : pts) { int x = v & 0xFFFF, y = v >>> 16; assertTrue(y >= 10); assertFalse(x >= 100 && x < 150 && y >= 100 && y < 150); }
        // a chessboard of 1-pixel squares has no flat pixel at all
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) px[y * w + x] = ((x + y) & 1) == 0 ? 0xFFFFFFFF : 0xFF000000;
        assertEquals(0, ScrimMeasure.pickPoints(px, w, h, null).length);
        assertEquals(0, ScrimMeasure.pickPoints(null, w, h, null).length);
    }
    @Test public void aChannelIsReadAtThePoints() {
        int w = 10;
        int[] px = new int[w * 10];
        px[3 * w + 4] = 0xFF112233;
        int[] pts = { (3 << 16) | 4 };
        assertEquals(0x11, ScrimMeasure.channel(px, w, pts, 16)[0]);
        assertEquals(0x22, ScrimMeasure.channel(px, w, pts, 8)[0]);
        assertEquals(0x33, ScrimMeasure.channel(px, w, pts, 0)[0]);
    }
    @Test public void aFlatPixelHasSmallNeighbourDifferences() {
        assertTrue(ScrimMeasure.flat(101, 100, 104, 98, 101));
        assertFalse(ScrimMeasure.flat(100, 20, 200, 100, 100));    // an edge through the pixel
        assertFalse(ScrimMeasure.flat(255, 0, 0, 0, 0));           // a bright dot / a one-pixel stroke: both sides equal, the pixel itself is not
        assertFalse(ScrimMeasure.flat(0, 255, 255, 255, 255));     // and a dark one
        assertEquals(255, ScrimMeasure.lum(0xFFFFFF)); assertEquals(0, ScrimMeasure.lum(0x000000));
        assertTrue(ScrimMeasure.lum(0x00FF00) > ScrimMeasure.lum(0xFF0000));   // green counts more than red
    }
}
