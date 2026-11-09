package com.tj.launcher;

import static org.junit.Assert.*;

import com.tj.launcher.ProfileRules.Profile;

import org.junit.Test;

public class RentalRulesTest {
    @Test public void onlyRentalLosesSettingsAndTheStore() {
        assertTrue(RentalRules.hiddenInDrawer(Profile.RENTAL, "com.android.settings", true, true));
        assertTrue(RentalRules.hiddenInDrawer(Profile.RENTAL, "com.android.vending", true, true));
        assertFalse("FBOP keeps its own list", RentalRules.hiddenInDrawer(Profile.FBOP, "com.android.settings", true, true));
        assertFalse("switched off", RentalRules.hiddenInDrawer(Profile.RENTAL, "com.android.settings", false, true));
        assertFalse("other apps", RentalRules.hiddenInDrawer(Profile.RENTAL, "com.android.chrome", true, true));
    }

    @Test public void screensThatOpenAnywayAreSentAway() {
        assertEquals(RentalRules.GO_HOME, RentalRules.windowAction(true, false, Profile.RENTAL, "com.android.settings", "com.android.settings.Settings$WifiSettingsActivity", true, true, true));
        assertEquals(RentalRules.GO_HOME, RentalRules.windowAction(true, false, Profile.RENTAL, "com.android.vending", "com.google.android.finsky.activities.MainActivity", true, true, true));
        assertEquals(RentalRules.GO_BACK, RentalRules.windowAction(true, false, Profile.RENTAL, "com.android.packageinstaller", "com.android.packageinstaller.UninstallerActivity", true, true, true));
        assertEquals(RentalRules.GO_BACK, RentalRules.windowAction(true, false, Profile.RENTAL, "com.google.android.packageinstaller", "com.android.packageinstaller.UninstallerActivity", false, false, true));
        assertEquals("an INSTALL screen is not an uninstall", RentalRules.NOTHING, RentalRules.windowAction(true, false, Profile.RENTAL, "com.android.packageinstaller", "com.android.packageinstaller.PackageInstallerActivity", true, true, true));
    }

    @Test public void neverOutsideRentalNorOnTheLockScreen() {
        assertEquals(RentalRules.NOTHING, RentalRules.windowAction(true, false, Profile.FBOP, "com.android.settings", "x", true, true, true));
        assertEquals("Owner (Quickstep mode) keeps Settings", RentalRules.NOTHING, RentalRules.windowAction(false, false, Profile.RENTAL, "com.android.settings", "x", true, true, true));
        assertEquals(RentalRules.NOTHING, RentalRules.windowAction(true, true, Profile.RENTAL, "com.android.settings", "x", true, true, true));
        assertEquals("each switch on its own", RentalRules.NOTHING, RentalRules.windowAction(true, false, Profile.RENTAL, "com.android.settings", "x", false, true, true));
    }

    @Test public void releaseOnDebugOff() {
        assertTrue(RentalRules.defaultOn(false));
        assertFalse(RentalRules.defaultOn(true));
    }
}
