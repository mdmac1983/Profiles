package com.tj.launcher;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** AM1: the pure rules of the calibration from a real screenshot. */
public class PatchShotTest {
    private static final long START = 1_000_000_000_000L;   // window start, ms

    @Test public void theNewestScreenshotInsideTheWindowWins() {
        String[] n = { "Screenshot_a.png", "Screenshot_b.png", "IMG_0001.jpg" };
        long[] t = { START / 1000 + 10, START / 1000 + 30, START / 1000 + 50 };
        assertEquals(1, PatchShot.pickNewest(n, t, START));   // the photo is not a screenshot, the later screenshot wins
    }
    @Test public void aScreenshotFromBeforeTheWindowIsIgnored() {
        assertEquals(-1, PatchShot.pickNewest(new String[] { "Screenshot_old.png" }, new long[] { START / 1000 - 60 }, START));
        assertEquals(0, PatchShot.pickNewest(new String[] { "Screenshot_edge.png" }, new long[] { START / 1000 - 1 }, START));   // a second of clock slack
    }
    @Test public void theNameMatchIsCaseInsensitiveAndNullSafe() {
        assertEquals(1, PatchShot.pickNewest(new String[] { null, "SCREENSHOT_20261106-023015.PNG" }, new long[] { START / 1000 + 5, START / 1000 + 6 }, START));
        assertEquals(-1, PatchShot.pickNewest(null, null, START));
        assertEquals(-1, PatchShot.pickNewest(new String[0], new long[0], START));
    }
    @Test public void theCropIsThePatchWindowPlusTheMarginClampedToTheDisplay() {
        assertArrayEquals(new int[] { 450, 514, 600, 677 }, PatchShot.cropRect(474, 538, 600, 653, 24, 600, 1024));
        assertArrayEquals(new int[] { 0, 0, 84, 74 }, PatchShot.cropRect(10, 5, 60, 50, 24, 600, 1024));        // near the top-left corner: never negative
        assertArrayEquals(new int[] { 550, 976, 600, 1024 }, PatchShot.cropRect(574, 1000, 590, 1020, 24, 600, 1024)); // near the bottom-right corner
    }
    @Test public void theStockMenuPanelIsRecognisedAndRefused() {
        assertTrue(PatchShot.looksLikeMenu(239, 241, 243));   // the panel colour #EFF1F3
        assertTrue(PatchShot.looksLikeMenu(232, 240, 241));   // the colour of v0.7.6's card
    }
    @Test public void aWallpaperIsNeverMistakenForTheMenu() {
        assertFalse(PatchShot.looksLikeMenu(194, 148, 66));   // the emulator's bright orange test wallpaper: bright but colourful
        assertFalse(PatchShot.looksLikeMenu(30, 70, 140));    // Tj's dark blue
        assertFalse(PatchShot.looksLikeMenu(120, 120, 120));  // a mid grey is no panel
        assertFalse(PatchShot.looksLikeMenu(0, 0, 0));
    }
    @Test public void theSignatureNamesWallpaperRotationAndDisplay() {
        assertEquals("17|r0|600x1024", PatchShot.sig(17, 0, 600, 1024));
        assertFalse(PatchShot.sig(17, 0, 600, 1024).equals(PatchShot.sig(18, 0, 600, 1024)));   // another wallpaper
        assertFalse(PatchShot.sig(17, 0, 600, 1024).equals(PatchShot.sig(17, 1, 1024, 600)));   // another rotation
    }
    @Test public void theCalibrationWindowEndsAtItsDeadline() {
        assertTrue(PatchShot.calibrating("1000", 999));
        assertFalse(PatchShot.calibrating("1000", 1000));
        assertFalse(PatchShot.calibrating("0", 5));
        assertFalse(PatchShot.calibrating("", 5));
        assertFalse(PatchShot.calibrating(null, 5));
        assertFalse(PatchShot.calibrating("garbage", 5));
    }
}
