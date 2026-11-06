package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** ACC1: "enabled but not connected" is its own state. */
public class HelperStatusTest {
    private static final String PKG = "com.tj.launcher", CLS = "com.tj.launcher.TjAccessibilityService";

    @Test public void connectedIsConnectedWhateverElse() {
        assertEquals(HelperStatus.CONNECTED, HelperStatus.of(true, true, true));
        assertEquals(HelperStatus.CONNECTED, HelperStatus.of(false, false, true));
    }
    @Test public void listedAndSwitchedOnButNotConnectedIsTheStuckState() {
        assertEquals(HelperStatus.ENABLED_NOT_CONNECTED, HelperStatus.of(true, true, false));
    }
    @Test public void offWhenNotListedOrTheMasterSwitchIsOff() {
        assertEquals(HelperStatus.OFF, HelperStatus.of(false, true, false));
        assertEquals(HelperStatus.OFF, HelperStatus.of(true, false, false));
        assertEquals(HelperStatus.OFF, HelperStatus.of(false, false, false));
    }
    @Test public void theListIsReadInBothSpellings() {
        assertTrue(HelperStatus.listed(PKG + "/" + CLS, PKG, CLS));
        assertTrue(HelperStatus.listed("com.other/com.other.S:" + PKG + "/." + "TjAccessibilityService", PKG, CLS));
        assertTrue(HelperStatus.listed("a/b.C:" + PKG + "/" + CLS + ":x/y.Z", PKG, CLS));
    }
    @Test public void otherServicesDoNotCount() {
        assertFalse(HelperStatus.listed("com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService", PKG, CLS));
        assertFalse(HelperStatus.listed("", PKG, CLS));
        assertFalse(HelperStatus.listed(null, PKG, CLS));
        assertFalse(HelperStatus.listed("com.tj.launcherx/com.tj.launcherx.TjAccessibilityService", PKG, CLS));
    }
    @Test public void theWordsSayWhatToDo() {
        assertTrue(HelperStatus.describe(HelperStatus.ENABLED_NOT_CONNECTED).contains("restart the tablet"));
        assertTrue(HelperStatus.describe(HelperStatus.OFF).startsWith("OFF"));
        assertEquals("ON", HelperStatus.describe(HelperStatus.CONNECTED));
    }
}
