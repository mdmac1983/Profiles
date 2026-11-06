package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** AE3 / AD7b: the lock-screen patch rests visible over the Screenshot slot, but never over the PIN pad, and never at home. */
public class PatchRestTest {
    @Test public void theSwitchKeyIsStable() { assertEquals("lock_patch_always", PatchRest.KEY); }

    @Test public void switchOffKeepsTheOldBehaviourInvisibleUntilTheMenuOpens() {
        assertFalse(PatchRest.visible(false, false, true, false));
        assertTrue(PatchRest.visible(true, false, true, false));
    }
    @Test public void switchOnRestsVisibleOnTheLockScreen() { assertTrue(PatchRest.visible(false, true, true, false)); }
    @Test public void neverRestsOverThePinPad() {
        assertFalse(PatchRest.visible(false, true, true, true));
        assertTrue("an open power menu still gets its patch over the pad", PatchRest.visible(true, true, true, true));
    }
    @Test public void theHomeScreenPatchNeverRests() {
        assertFalse(PatchRest.visible(false, true, false, false));
        assertTrue(PatchRest.visible(true, true, false, false));
    }

    @Test public void theRestingPatchPaintsOnlyBelowTheCard() {
        assertEquals(150, PatchRest.restTop(100, 250, 400));
        assertEquals(0, PatchRest.restTop(100, 100, 400));
    }
    @Test public void theOpenMenuScrimIsTheStockValueUntilTjNudgesIt() {
        assertEquals(0.79f, PatchRest.openScrim(0.79f, 0), 0.0001f);
        assertEquals(0.79f * 0.9f, PatchRest.openScrim(0.79f, -10), 0.0001f);
        assertEquals(1f, PatchRest.openScrim(0.79f, 40), 0.0001f);     // never brighter than the screen itself
        assertEquals(0.3f, PatchRest.openScrim(0.79f, -90), 0.0001f);  // never black
    }
    @Test public void aCardOutsideTheWindowIsClamped() {
        assertEquals(0, PatchRest.restTop(100, 40, 400));        // card bottom above the window: paint it all
        assertEquals(300, PatchRest.restTop(100, 900, 400));     // card bottom below the window: paint nothing
        assertEquals(0, PatchRest.restTop(400, 500, 300));       // an empty / inverted window never goes negative
    }

    // ---- AH3 (v0.7.6): the slot-only patch with soft edges
    @Test public void theSlotWindowIsThePanelPlusTheMarginFromOneFeatherAboveTheCardBottom() {
        int[] w = PatchRest.slotWindow(480, 700, 585, 830, 2, 8, 600, 1024);   // learned panel x 480..585, card bottom 700, panel bottom 830
        assertEquals(470, w[0]); assertEquals(692, w[1]); assertEquals(595, w[2]); assertEquals(840, w[3]);
    }
    @Test public void theSlotWindowIsClampedToTheDisplay() {
        int[] w = PatchRest.slotWindow(495, 700, 600, 1020, 2, 8, 600, 1024);   // panel flush with the right edge, close to the bottom
        assertEquals(600, w[2]); assertEquals(1024, w[3]);
        int[] z = PatchRest.slotWindow(4, 3, 100, 200, 2, 8, 600, 1024);        // close to the top left corner: never negative
        assertEquals(0, z[0]); assertEquals(0, z[1]);
    }
    @Test public void insideTheCoreThePatchIsFullyOpaque() { assertEquals(255, PatchRest.alphaAt(50, 50, 10, 10, 90, 90, 8, 8, 8, 8)); assertEquals(255, PatchRest.alphaAt(10, 10, 10, 10, 90, 90, 8, 8, 8, 8)); assertEquals(255, PatchRest.alphaAt(89, 89, 10, 10, 90, 90, 8, 8, 8, 8)); }
    @Test public void aHardEdgeShowsNothingOutsideTheCore() {
        assertEquals(0, PatchRest.alphaAt(9, 50, 10, 10, 90, 90, 0, 0, 0, 0));
        assertEquals(0, PatchRest.alphaAt(90, 50, 10, 10, 90, 90, 0, 0, 0, 0));
        assertEquals(0, PatchRest.alphaAt(50, 9, 10, 10, 90, 90, 0, 0, 0, 0));
        assertEquals(0, PatchRest.alphaAt(50, 90, 10, 10, 90, 90, 0, 0, 0, 0));
    }
    @Test public void theSoftFringeFadesOutwardAndEndsAtZero() {
        int prev = 255;
        for (int d = 1; d <= 8; d++) { int a = PatchRest.alphaAt(10 - d, 50, 10, 10, 90, 90, 8, 8, 8, 8); assertTrue("fade must not grow outward at d=" + d, a <= prev); assertTrue(a > 0 && a < 255); prev = a; }
        assertEquals(0, PatchRest.alphaAt(10 - 9, 50, 10, 10, 90, 90, 8, 8, 8, 8));
        assertEquals(0, PatchRest.alphaAt(0, 50, 10, 10, 90, 90, 8, 8, 8, 8));
    }
    @Test public void aCornerFadesInBothDirections() {
        int edge = PatchRest.alphaAt(5, 50, 10, 10, 90, 90, 8, 8, 8, 8), corner = PatchRest.alphaAt(5, 5, 10, 10, 90, 90, 8, 8, 8, 8);
        assertTrue(corner < edge && corner > 0);
        assertEquals(PatchRest.alphaAt(5, 50, 10, 10, 90, 90, 8, 8, 8, 8), PatchRest.alphaAt(94, 50, 10, 10, 90, 90, 8, 8, 8, 8));   // symmetric left / right
    }
    @Test public void theOpenPatchHasAHardTopSoTheStockPanelNeverShowsThroughIt() {
        // the OPEN mask: top fringe 0 -> everything above the core is transparent, everything inside is fully opaque
        assertEquals(0, PatchRest.alphaAt(50, 9, 10, 10, 90, 90, 8, 0, 8, 8));
        assertEquals(255, PatchRest.alphaAt(50, 10, 10, 10, 90, 90, 8, 0, 8, 8));
    }
    @Test public void aWobbleOfAFewPixelsDoesNotRebuildThePatch() {
        int[] a = { 470, 692, 595, 840 };
        assertTrue(PatchRest.sameWindow(a, new int[] { 470, 693, 595, 840 }, 2));
        assertTrue(PatchRest.sameWindow(a, new int[] { 472, 690, 593, 842 }, 2));
        assertFalse(PatchRest.sameWindow(a, new int[] { 470, 695, 595, 840 }, 2));   // a real change (3 px) rebuilds it
        assertFalse(PatchRest.sameWindow(a, null, 2));
        assertFalse(PatchRest.sameWindow(a, new int[] { 1, 2, 3 }, 2));
    }
    @Test public void theMaskHasOneByteForEveryPixelAndMatchesAlphaAt() {
        byte[] m = PatchRest.mask(100, 100, 10, 10, 90, 90, 8, 8, 8, 8);
        assertEquals(100 * 100, m.length);
        assertEquals(PatchRest.alphaAt(7, 40, 10, 10, 90, 90, 8, 8, 8, 8), m[40 * 100 + 7] & 0xff);
        assertEquals(255, m[50 * 100 + 50] & 0xff);
    }
}
