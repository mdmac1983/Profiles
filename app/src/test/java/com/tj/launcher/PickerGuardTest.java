package com.tj.launcher;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** AO1: the pure rules of the helper that hides "My photos" in the system wallpaper picker. */
public class PickerGuardTest {
    @Test public void onlyTheMyPhotosLabelMatches() {
        assertTrue(PickerGuard.isMyPhotos("My photos", null));
        assertTrue(PickerGuard.isMyPhotos("  my PHOTOS ", null));      // case and spaces do not matter
        assertTrue(PickerGuard.isMyPhotos(null, "My photos"));         // the content description counts too
        assertFalse(PickerGuard.isMyPhotos("Photos", null));
        assertFalse(PickerGuard.isMyPhotos("My photos and videos", null));   // a substring is not the tile
        assertFalse(PickerGuard.isMyPhotos("Live wallpapers", "Wallpapers"));
        assertFalse(PickerGuard.isMyPhotos(null, null));
        assertFalse(PickerGuard.isMyPhotos("", ""));
    }
    @Test public void theGuardWorksOnlyInTheCustomLauncherUnlockedWithItsSwitchOn() {
        assertTrue(PickerGuard.wanted(true, true, false, false));
        assertFalse(PickerGuard.wanted(false, true, false, false));   // Quickstep: the stock picker stays stock
        assertFalse(PickerGuard.wanted(true, false, false, false));   // switched off
        assertFalse(PickerGuard.wanted(true, true, true, false));     // lock screen
        assertFalse(PickerGuard.wanted(true, true, false, true));     // crash breaker tripped: automation paused
    }
    @Test public void theTileIsCoveredWhenItIsASensibleSize() {
        // the tile (150 x 150 px) inside a 600 x 1024 screen, label inside it: cover the tile plus one pixel
        assertArrayEquals(new int[] { 9, 799, 161, 951 }, PickerGuard.coverRect(new int[] { 40, 900, 120, 930 }, new int[] { 10, 800, 160, 950 }, 14, 600, 1024));
    }
    @Test public void aWholeScreenOrstripIsNeverCoveredInsteadTheLabelGrownByAPad() {
        // the "clickable ancestor" turned out to be the whole screen: cover the label with a pad instead
        assertArrayEquals(new int[] { 25, 885, 135, 945 }, PickerGuard.coverRect(new int[] { 40, 900, 120, 930 }, new int[] { 0, 0, 600, 1024 }, 14, 600, 1024));
        assertArrayEquals(new int[] { 25, 885, 135, 945 }, PickerGuard.coverRect(new int[] { 40, 900, 120, 930 }, null, 14, 600, 1024));
    }
    @Test public void theCoverStaysInsideTheDisplayAndNothingIsCoveredWithoutABox() {
        assertArrayEquals(new int[] { 0, 0, 90, 90 }, PickerGuard.coverRect(null, new int[] { 0, 0, 89, 89 }, 0, 600, 1024));
        assertArrayEquals(new int[] { 510, 934, 600, 1024 }, PickerGuard.coverRect(null, new int[] { 511, 935, 600, 1024 }, 0, 600, 1024));
        assertNull(PickerGuard.coverRect(null, null, 8, 600, 1024));
        assertNull(PickerGuard.coverRect(new int[] { 5, 5, 5, 5 }, new int[] { 7, 7, 7, 7 }, 8, 600, 1024));   // empty boxes
    }
    @Test public void theFallbackColourIsTheDayOrNightSurface() {
        assertEquals(0xFFFFFFFF, PickerGuard.fallbackColor(false));
        assertEquals(0xFF202124, PickerGuard.fallbackColor(true));
    }
}
