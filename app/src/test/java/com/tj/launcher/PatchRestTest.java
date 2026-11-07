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
    // ---- BB1 (v0.8.3): the opaque core reaches further DOWN (the stock panel's shadow), within what the stored crop covers
    @Test public void theExtendedWindowIsTallerAtTheBottomOnly() {
        int[] old = PatchRest.slotWindow(480, 700, 585, 830, 2, 8, 600, 1024);
        int[] ext = PatchRest.slotWindow(480, 700, 585, 830, 2, 8, 20, 8, 600, 1024);
        assertEquals(old[0], ext[0]); assertEquals(old[1], ext[1]); assertEquals(old[2], ext[2]);   // left, top, right: untouched
        assertEquals(old[3] + 20, ext[3]);                                                                // 830 + 2 + 20 + 8
        assertEquals(860, ext[3]);
    }
    @Test public void theOldSignatureIsTheNoExtensionGeometry() {
        for (int[] a : new int[][] { { 480, 700, 585, 830 }, { 495, 700, 600, 1020 }, { 4, 3, 100, 200 } })
            assertArrayEquals(PatchRest.slotWindow(a[0], a[1], a[2], a[3], 2, 8, 600, 1024), PatchRest.slotWindow(a[0], a[1], a[2], a[3], 2, 8, 0, 8, 600, 1024));
    }
    @Test public void theExtendedWindowIsStillClampedToTheDisplay() {
        assertEquals(1024, PatchRest.slotWindow(495, 700, 600, 1015, 2, 8, 20, 8, 600, 1024)[3]);
    }
    @Test public void theExtensionNeverLeavesTheStoredCrop() {
        // the crop was cut for the OLD window + margin; whatever the wish (and the bottom fringe), the new window's bottom stays inside it with the slack to spare
        int margin = 24, e = 2, f = 9;
        for (int wish = -5; wish <= 60; wish++) for (int bf = 0; bf <= 24; bf++) {
            int x = PatchRest.clampExtra(wish, bf, f, margin);
            assertTrue("never negative", x >= 0);
            int oldBottom = 830 + e + f, crop = oldBottom + margin, newBottom = 830 + e + x + bf;
            if (x > 0) assertTrue("wish " + wish + " bf " + bf + " -> " + x, newBottom <= crop - PatchRest.CROP_SLACK_PX);
        }
    }
    @Test public void theDefaultExtensionFitsTheDefaultFringeAtTheTabletsDensity() {
        int px = Math.round(PatchRest.EXTRA_BOTTOM_DP * 1.0625f), f = Math.round(PatchRest.FEATHER_DP * 1.0625f);
        assertEquals(px, PatchRest.clampExtra(px, f, f, 24));   // 21 px: not clamped at 170 dpi
        assertEquals(0, PatchRest.clampExtra(px, f, f, 2));     // a tiny crop margin: no extension
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
    @Test public void theMaskOfTheRealTabletWindowIsAsymmetricAsDesigned() {
        // v0.7.6 (review): the slot window on the 600 x 1024 tablet is 126 x 115: opaque core x 9..123, hard top at row 6 (open menu), fringes left 9 / right 2 (the panel is 4 px from the screen edge) / bottom 9
        byte[] m = PatchRest.mask(126, 115, 9, 6, 124, 106, 9, 0, 2, 9);
        assertEquals(126 * 115, m.length);
        assertEquals(255, m[60 * 126 + 60] & 0xff);            // inside the core
        assertEquals(0, m[5 * 126 + 60] & 0xff);               // above the hard top: nothing (the stock panel must never show through a fade)
        assertEquals(189, m[60 * 126 + 124] & 0xff);           // first column of the short right fringe
        assertEquals(66, m[60 * 126 + 125] & 0xff);            // last column = the screen edge
        assertTrue((m[60 * 126 + 4] & 0xff) > 0 && (m[60 * 126 + 4] & 0xff) < 255);   // the long left fringe fades
        assertTrue((m[112 * 126 + 60] & 0xff) > 0 && (m[112 * 126 + 60] & 0xff) < 255); // and so does the bottom fringe
    }
    @Test public void theMaskHasOneByteForEveryPixelAndMatchesAlphaAt() {
        byte[] m = PatchRest.mask(100, 100, 10, 10, 90, 90, 8, 8, 8, 8);
        assertEquals(100 * 100, m.length);
        assertEquals(PatchRest.alphaAt(7, 40, 10, 10, 90, 90, 8, 8, 8, 8), m[40 * 100 + 7] & 0xff);
        assertEquals(255, m[50 * 100 + 50] & 0xff);
    }
}
