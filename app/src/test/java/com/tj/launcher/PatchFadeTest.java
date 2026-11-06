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
        // The point of the whole class: whatever the lag, the patch starts where the stock scrim already is, so there is no step. Compare with the v0.8.0 behaviour (a jump to the final dim).
        float keep = 0.80f;   // the stock scrim leaves 80 % at the end
        for (long lag : new long[] { 0, 30, 60, 100, 150, 200 }) {
            long start = 10_000 - lag, now = 10_000;
            float patch = PatchFade.mix(1f, keep, PatchFade.progressAt(now, start));
            float surround = PatchFade.mix(1f, keep, PatchFade.openProgress(now + PatchFade.LEAD_MS - start));   // what the screen shows when this frame is seen
            assertEquals("lag " + lag, surround, patch, 1e-6f);
            float oldJump = keep;   // v0.8.0: straight to the final brightness
            float oldStep = Math.abs(oldJump - mixAt(keep, lag + PatchFade.LEAD_MS));
            assertTrue("the old jump was a visible step for lag " + lag + " (" + oldStep + ")", lag < 60 || oldStep > 0.03f || lag > 200);
        }
    }
    private static float mixAt(float keep, long elapsed) { return PatchFade.mix(1f, keep, PatchFade.openProgress(elapsed)); }
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
    @Test public void theMixWorksForTheWallpaperModelToo() {
        // the MODEL patch rests at 0.77 (the keyguard's own dim) and ends at 0.77 * 0.79: the fade runs between those two
        float rest = 0.77f, open = 0.77f * 0.79f;
        assertEquals(rest, PatchFade.mix(rest, open, 0f), 1e-6f);
        assertEquals(open, PatchFade.mix(rest, open, 1f), 1e-6f);
        assertTrue(PatchFade.mix(rest, open, 0.5f) < rest && PatchFade.mix(rest, open, 0.5f) > open);
    }
}
