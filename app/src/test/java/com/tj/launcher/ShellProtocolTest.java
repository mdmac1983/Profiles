package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import org.junit.Test;

public class ShellProtocolTest {
    @Test public void ordinaryAppsAreAllowed() {
        assertTrue(ShellProtocol.allowed("com.android.contacts"));
        assertTrue(ShellProtocol.allowed("com.google.android.apps.chrome"));
        assertTrue(ShellProtocol.allowed("org.mozilla.firefox"));
    }
    @Test public void systemAndOurOwnPackagesAreNeverStopped() {
        for (String p : new String[] { "android", "com.android.systemui", "com.android.phone", "com.tj.launcher", "com.android.launcher3", "com.android.providers.contacts",
                "com.google.android.gms", "com.android.inputmethod.latin", "com.mediatek.ims", "com.android.shell", "com.android.bluetooth", "com.android.server.telecom" })
            assertFalse(p, ShellProtocol.allowed(p));
    }
    @Test public void oddNamesAreRejected() {
        for (String p : new String[] { null, "", "a", "com.x; reboot", "com.x && id", "../x", "com..x", "1com.x", "com.x\nreboot", "$(id).a" }) assertFalse(String.valueOf(p), ShellProtocol.allowed(p));
    }
    @Test public void forceStopLineIsFilteredAndDeduplicated() {
        assertEquals(Arrays.asList("com.android.contacts", "com.android.email"),
                ShellProtocol.forceStopTargets("FORCESTOP com.android.contacts android com.android.email com.android.contacts com.x;reboot"));
    }
    @Test public void otherLinesAreNotForceStop() {
        assertNull(ShellProtocol.forceStopTargets("PING"));
        assertNull(ShellProtocol.forceStopTargets("FORCESTOP"));
        assertNull(ShellProtocol.forceStopTargets("force-stop com.a.b"));
        assertNull(ShellProtocol.forceStopTargets(null));
        assertTrue(ShellProtocol.isPing(" PING ")); assertFalse(ShellProtocol.isPing("PINGX")); assertTrue(ShellProtocol.isQuit("QUIT"));
    }
    @Test public void atMostFortyPackages() {
        StringBuilder b = new StringBuilder("FORCESTOP");
        for (int i = 0; i < 80; i++) b.append(" com.app").append(i).append(".x");
        assertEquals(ShellProtocol.MAX_PACKAGES, ShellProtocol.forceStopTargets(b.toString()).size());
    }
    @Test public void parsesTheUidFromPmOutput() {
        assertEquals(10123, ShellProtocol.parseUid("package:com.tj.launcher uid:10123\n"));
        assertEquals(10123, ShellProtocol.parseUid("package:com.tj.launcherX uid:1\npackage:com.tj.launcher uid:10123"));
        assertEquals(-1, ShellProtocol.parseUid("Error: nothing"));
        assertEquals(-1, ShellProtocol.parseUid(null));
    }
    @Test public void theAdbLineExpandsOnTheDeviceNotTheComputer() {
        assertTrue(ShellProtocol.START_ONE_LINER.contains("adb shell '"));
        assertTrue(ShellProtocol.START_ONE_LINER.contains("com.tj.launcher.ShellServer"));
    }
}
