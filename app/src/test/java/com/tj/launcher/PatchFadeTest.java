package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** AZ1: the curve the lock-screen patch follows while the stock power menu fades in (see PatchFade). */
public class PatchFadeTest {
    @Test public void theCurveIsAndroidsFastOutSlowIn() {
        // PathInterpolator(0.4, 0, 0.2, 1): fixed points and two well-known samples
        assertEquals(0f, PatchFade.fastOutSlowIn(0f), 1e-6f);
        assertEquals(1f, PatchFade.fastOutSlowIn(1f), 1e-6f);
        assertEquals(0.5f, PatchFade.fastOutSlowIn(0.35f), 0.005f);    // half way after 35 % of the time (control point s = 0.5)
        assertEquals(0.778f, PatchFade.fastOutSlowIn(0.5f), 0.005f);
        assertEquals(0.216f, PatchFade.fastOutSlowIn(0.2412f), 0.005f);   // s = 0.3
    }
    @Test public void theCurveOnlyEverGrows() {
        float last = -1f;
        for (int i = 0; i <= 300; i++) {
            float y = PatchFade.fastOutSlowIn(i / 300f);
            assertTrue("x=" + i + "/300", y >= last);
            assertTrue(y >= 0f && y <= 1f);
            last = y;
        }
    }
    @Test public void outOfRangeIsClamped() {
        assertEquals(0f, PatchFade.fastOutSlowIn(-3f), 0f);
        assertEquals(1f, PatchFade.fastOutSlowIn(7f), 0f);
        assertEquals(0f, PatchFade.openProgress(-50), 0f);
        assertEquals(0f, PatchFade.openProgress(0), 0f);
        assertEquals(1f, PatchFade.openProgress(PatchFade.OPEN_MS), 0f);
        assertEquals(1f, PatchFade.openProgress(5000), 0f);
    }
    @Test public void theStockScrimIsHalfThereAfterAbout105Ms() {
        assertEquals(0.5f, PatchFade.openProgress(105), 0.01f);
        assertEquals(0.78f, PatchFade.openProgress(150), 0.01f);
        assertEquals(0.97f, PatchFade.openProgress(236), 0.01f);
    }
    @Test public void aStaleEventMeansNoFade() {
        assertEquals(120, PatchFade.lagMs(1_120, 1_000));                       // fresh: the lag is the age of the event
        assertEquals(PatchFade.OPEN_MS, PatchFade.lagMs(1_000 + PatchFade.FRESH_MS + 1, 1_000));   // too old: this is not the event that opened the menu
        assertEquals(PatchFade.OPEN_MS, PatchFade.lagMs(1_000, 0));            // no event seen at all
        assertEquals(PatchFade.OPEN_MS, PatchFade.lagMs(900, 1_000));          // an event from the future (clock quirk): never trusted
        assertEquals(PatchFade.FRESH_MS, PatchFade.lagMs(1_000 + PatchFade.FRESH_MS, 1_000));
    }
    @Test public void theFirstFrameHasTheBrightnessOfTheSurround() {
        // The point of the whole class: whatever the lag, the first frame already has the brightness the stock scrim has when that frame is SEEN, so there is no step.
        float keep = 0.80f;   // the stock scrim leaves 80 % at the end
        for (long lag : new long[] { 0, 30, 60, 100, 150, 200, 280 }) {
            long start = 10_000 - lag, now = 10_000;
            float patch = PatchFade.mix(1f, keep, PatchFade.progressAt(now, start));
            float surroundWhenSeen = PatchFade.mix(1f, keep, PatchFade.openProgress(now + PatchFade.LEAD_MS - start));
            assertEquals("lag " + lag, surroundWhenSeen, patch, 1e-6f);
        }
    }
    /** v0.8.0 switched straight to the final brightness when it noticed the menu, while the surround was only part way there: the size of that step, in brightness (1.0 = full). */
    private static float oldStep(float keep, long lagMs) { return Math.abs(keep - PatchFade.mix(1f, keep, PatchFade.openProgress(lagMs + PatchFade.LEAD_MS))); }
    @Test public void theOldJumpWasAVisibleStep() {
        assertTrue(oldStep(0.80f, 40) > 0.15f);    // noticed 40 ms late: the patch was ~15 % darker than its surround at that moment
        assertTrue(oldStep(0.80f, 80) > 0.08f);
        assertTrue(oldStep(0.80f, 120) < 0.07f);   // only a late reaction (the scrim nearly there) hides the step (about 0.065 with LEAD_MS 10, 0.044 with 25)
    }
    @Test public void theFadeEndsWithTheFinalBrightness() {
        assertTrue(PatchFade.fading(10_000, 10_000));
        assertTrue(PatchFade.fading(10_000, 10_000 - 200));
        assertFalse(PatchFade.fading(10_000, 10_000 - 300));
        assertFalse(PatchFade.fading(10_000, 10_000 - PatchFade.OPEN_MS + PatchFade.LEAD_MS));
        assertEquals(0.8f, PatchFade.mix(1f, 0.8f, 1f), 1e-6f);
        assertEquals(1f, PatchFade.mix(1f, 0.8f, 0f), 1e-6f);
        assertEquals(0.9f, PatchFade.mix(1f, 0.8f, 0.5f), 1e-6f);
        assertEquals(PatchFade.OPEN_MS + 30, PatchFade.SETTLE_MS);
    }
    // ---- BC2: the fade-OUT of the stock dialog (LogAccelerateInterpolator(100, 0), 300 ms; the scrim alpha is (1 - interpolated) * 0.2)
    @Test public void theLogAccelerateCurveHasItsKnownShape() {
        assertEquals(0f, PatchFade.logAccelerate(0f), 1e-6f);
        assertEquals(1f, PatchFade.logAccelerate(1f), 1e-6f);
        assertEquals(0.0909f, PatchFade.logAccelerate(0.5f), 0.001f);   // slow start: 9 % at half time
        assertEquals(0.392f, PatchFade.logAccelerate(0.8f), 0.002f);
        assertEquals(0.627f, PatchFade.logAccelerate(0.9f), 0.002f);
    }
    @Test public void theCloseProgressRunsFromOneToZeroAndOnlyEverFalls() {
        assertEquals(1f, PatchFade.closeProgress(-20), 1e-6f);   // the dismissal has not begun
        assertEquals(1f, PatchFade.closeProgress(0), 1e-6f);
        assertEquals(0f, PatchFade.closeProgress(PatchFade.CLOSE_MS), 1e-6f);
        assertEquals(0f, PatchFade.closeProgress(900), 1e-6f);
        float prev = 1f;
        for (long t = 0; t <= PatchFade.CLOSE_MS; t += 5) { float p = PatchFade.closeProgress(t); assertTrue("t=" + t, p <= prev + 1e-6f); prev = p; }
    }
    @Test public void theStockScrimIsStillNinetyPercentThereAtHalfTimeAndGoneInTheLastHundredMilliseconds() {
        // the numbers behind the film of run #291 (section pt): the surround stayed at 0.80 until the last quarter, then brightened within ~60 ms
        assertEquals(0.909f, PatchFade.closeProgress(150), 0.002f);
        assertEquals(0.79f, PatchFade.closeProgress(200), 0.005f);
        assertEquals(0.608f, PatchFade.closeProgress(240), 0.005f);
        assertEquals(0.373f, PatchFade.closeProgress(270), 0.005f);
        assertTrue(PatchFade.closeProgress(285) < 0.25f);
    }
    @Test public void theLeadMakesTheFrameRightWhenItIsSeen() {
        assertEquals(PatchFade.closeProgress(210), PatchFade.closeProgressAt(1200, 1000, 10), 1e-6f);   // 200 ms after the start + a 10 ms lead
        assertEquals(1f, PatchFade.closeProgressAt(1000, 1015, 10), 1e-6f);                              // a start that lies a few ms ahead (our own BACK): nothing changes yet
        assertTrue(PatchFade.closing(1000 + 289, 1000, 10));
        assertFalse(PatchFade.closing(1000 + 290, 1000, 10));
    }
    @Test public void theOpenLookDimsTheSurroundAsTheStockScrimDoesWhileItFadesOut() {
        // out = (1 - 0.2 * p) * L: at p = closeProgress the patch mixes identity -> the measured look with the SAME progress, so patch and surround agree at every instant (here the generic 0.80 look)
        for (long t = 0; t <= 300; t += 20) {
            float p = PatchFade.closeProgress(t);
            float surround = 1f - 0.2f * p, patch = PatchFade.mix(1f, 0.8f, p);
            assertEquals("t=" + t, surround, patch, 1e-5f);
        }
    }
    @Test public void aLateAnimatorNeverLeavesThePatchBrighterThanItsScrim() {
        // the model ends at 300 ms after its start; the stock window may be removed 100 ms later (run #299): until then the patch keeps the floor, the removal / the timer ends it
        assertEquals(PatchFade.CLOSE_FLOOR, PatchFade.closeLook(1400, 1000, 0), 1e-6f);
        assertEquals(PatchFade.CLOSE_FLOOR, PatchFade.closeLook(5000, 1000, 10), 1e-6f);
        assertEquals(1f, PatchFade.closeLook(1000, 1040, 10), 1e-6f);   // before the (delayed) start: the open look
        assertEquals(PatchFade.closeProgress(150), PatchFade.closeLook(1150, 1000, 0), 1e-6f);   // in the middle the floor does not matter
        assertTrue(PatchFade.CLOSE_START_DELAY_MS >= 20 && PatchFade.CLOSE_START_DELAY_MS <= 100);
    }
    @Test public void theMixWorksForTheWallpaperModelToo() {
        // the MODEL patch rests at 0.77 (the keyguard's own dim) and ends at 0.77 * 0.79: the fade runs between those two
        float rest = 0.77f, open = 0.77f * 0.79f;
        assertEquals(rest, PatchFade.mix(rest, open, 0f), 1e-6f);
        assertEquals(open, PatchFade.mix(rest, open, 1f), 1e-6f);
        assertTrue(PatchFade.mix(rest, open, 0.5f) < rest && PatchFade.mix(rest, open, 0.5f) > open);
    }
}
