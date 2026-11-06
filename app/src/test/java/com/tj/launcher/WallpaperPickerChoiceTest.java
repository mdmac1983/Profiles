package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** AO1: the wallpaper picker is ONE explicit system component; a gallery app, a user app or a guess is never "offered". */
public class WallpaperPickerChoiceTest {
    private static final String OWN = "com.tj.launcher";

    private static int pick(String[] pk, String[] cl, boolean[] sys, int[] pr, String hint) { return WallpaperPickerChoice.choose(pk, cl, sys, pr, hint, OWN); }

    @Test public void theStockLaunchersOwnSettingWins() {
        String[] pk = { "com.android.gallery3d", "com.android.wallpaper", "com.vendor.picker" };
        String[] cl = { "G", "W", "P" }; boolean[] sys = { true, true, true }; int[] pr = { 0, 0, 5 };
        assertEquals(2, pick(pk, cl, sys, pr, "com.vendor.picker"));   // whatever it is called: that is what the stock Wallpapers entry opens
    }
    @Test public void aHintForAnAppThatIsNotInstalledFallsBackToTheRules() {
        String[] pk = { "com.android.gallery3d", "com.android.wallpaperpicker" };
        String[] cl = { "G", "W" }; boolean[] sys = { true, true }; int[] pr = { 0, 0 };
        assertEquals(1, pick(pk, cl, sys, pr, "com.not.installed"));
        assertEquals(1, pick(pk, cl, sys, pr, ""));
        assertEquals(1, pick(pk, cl, sys, pr, null));
    }
    @Test public void aGalleryIsAnotherAppNotThePicker() {
        String[] pk = { "com.android.gallery3d", "com.google.android.apps.photos", "com.mediatek.camera" };
        String[] cl = { "A", "B", "C" }; boolean[] sys = { true, true, true }; int[] pr = { 9, 9, 9 };
        assertEquals(-1, pick(pk, cl, sys, pr, ""));   // nothing but media apps answers: do nothing rather than open a gallery
    }
    @Test public void aUserInstalledAppIsNeverUsed() {
        String[] pk = { "org.cool.wallpaper.free" }; String[] cl = { "Main" }; boolean[] sys = { false }; int[] pr = { 100 };
        assertEquals(-1, pick(pk, cl, sys, pr, ""));
    }
    @Test public void aWallpaperNamedSystemAppBeatsOtherSystemApps() {
        String[] pk = { "com.vendor.tool", "com.android.wallpaper", "com.android.gallery3d" };
        String[] cl = { "T", "W", "G" }; boolean[] sys = { true, true, true }; int[] pr = { 50, 0, 50 };
        assertEquals(1, pick(pk, cl, sys, pr, ""));
    }
    @Test public void twoWallpaperAppsAreDecidedByPriorityThenByNameNotByListOrder() {
        String[] pkA = { "com.android.wallpaper", "com.android.wallpaperpicker" };
        String[] clA = { "X", "Y" }; boolean[] sys = { true, true };
        assertEquals(1, pick(pkA, clA, sys, new int[] { 0, 3 }, ""));      // higher priority
        assertEquals(0, pick(pkA, clA, sys, new int[] { 2, 2 }, ""));      // same priority: the alphabetically first
        String[] pkB = { "com.android.wallpaperpicker", "com.android.wallpaper" }; String[] clB = { "Y", "X" };
        assertEquals(1, pick(pkB, clB, sys, new int[] { 2, 2 }, ""));      // listed the other way round: the same component wins
    }
    @Test public void aSingleUnknownSystemAppIsUsedButSeveralAreNotGuessed() {
        assertEquals(0, pick(new String[] { "com.vendor.styles" }, new String[] { "S" }, new boolean[] { true }, new int[] { 0 }, ""));
        assertEquals(-1, pick(new String[] { "com.vendor.a", "com.vendor.b" }, new String[] { "A", "B" }, new boolean[] { true, true }, new int[] { 0, 0 }, ""));
    }
    @Test public void ourOwnPackageIsNeverTheAnswer() {
        assertEquals(-1, pick(new String[] { OWN }, new String[] { "W" }, new boolean[] { true }, new int[] { 9 }, OWN));
        assertEquals(-1, pick(new String[] { OWN }, new String[] { "W" }, new boolean[] { true }, new int[] { 9 }, ""));
    }
    @Test public void badInputIsNoAnswer() {
        assertEquals(-1, WallpaperPickerChoice.choose(null, null, null, null, "", OWN));
        assertEquals(-1, pick(new String[0], new String[0], new boolean[0], new int[0], "x"));
        assertEquals(-1, pick(new String[] { null }, new String[] { "A" }, new boolean[] { true }, new int[] { 0 }, ""));
    }
    @Test public void mediaAppsAreRecognisedByName() {
        assertTrue(WallpaperPickerChoice.isMediaApp("com.android.gallery3d"));
        assertTrue(WallpaperPickerChoice.isMediaApp("com.google.android.apps.photos"));
        assertTrue(WallpaperPickerChoice.isMediaApp("com.android.documentsui"));
        assertTrue(WallpaperPickerChoice.isMediaApp(null));
        assertFalse(WallpaperPickerChoice.isMediaApp("com.android.wallpaper"));
        assertFalse(WallpaperPickerChoice.isMediaApp("com.android.wallpaperpicker"));
    }

    // ---- AO1b: where the stock picker's wallpapers are looked for
    @Test public void theStockWallpapersAreLookedForInTheRightOrder() {
        String[] pk = { "com.android.gallery3d", "com.vendor.tool", "com.mediatek.wallpaperpicker", OWN };
        boolean[] sys = { true, true, true, true };
        java.util.List<String> o = WallpaperPickerChoice.wallpaperPackages(pk, sys, "com.vendor.hint", "com.android.launcher3", OWN);
        assertEquals(java.util.Arrays.asList("com.android.wallpaperpicker", "com.vendor.hint", "com.mediatek.wallpaperpicker", "com.vendor.tool", "com.android.launcher3"), o);   // no gallery, never ourselves
    }
    @Test public void aUserAppIsNeverALookupPlaceAndMissingInputIsFine() {
        java.util.List<String> o = WallpaperPickerChoice.wallpaperPackages(new String[] { "org.free.wallpaper" }, new boolean[] { false }, "", null, OWN);
        assertEquals(java.util.Arrays.asList("com.android.wallpaperpicker"), o);
        assertEquals(java.util.Arrays.asList("com.android.wallpaperpicker"), WallpaperPickerChoice.wallpaperPackages(null, null, null, null, OWN));
    }
}
