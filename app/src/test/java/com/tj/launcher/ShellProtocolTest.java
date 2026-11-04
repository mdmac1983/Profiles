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
    @Test public void readsTheClientUidFromProcNetTcp() {
        String t = "  sl  local_address rem_address   st tx_queue rx_queue tr tm->when retrnsmt   uid  timeout inode\n"
                 + "   0: 0100007F:B84F 0100007F:2CCF 01 00000000:00000000 00:00000000 00000000 10123        0 4711 1 0000000000000000 20 4 30 10 -1\n"
                 + "   1: 0100007F:2CCF 0100007F:B84F 01 00000000:00000000 00:00000000 00000000  2000        0 4712 1 0000000000000000 20 4 30 10 -1\n";
        // client port 0xB84F = 47183, server port 0x2CCF = 11471
        assertEquals(10123, ShellProtocol.uidForPorts(t, 0xB84F, 0x2CCF));
        assertEquals(-1, ShellProtocol.uidForPorts(t, 1234, 0x2CCF));
        assertEquals(-1, ShellProtocol.uidForPorts(null, 1, 2));
        assertEquals(-1, ShellProtocol.uidForPorts("garbage", 1, 2));
    }
    @Test public void tcpPortRangeIsSmall() { assertTrue(ShellProtocol.TCP_PORT_LAST - ShellProtocol.TCP_PORT_FIRST <= 5); }
    private static final String DUMP =
            "ACTIVITY MANAGER RECENT TASKS (dumpsys activity recents)\n"
          + "  mRecentsUid=-1\n  Recent tasks:\n"
          + "  * Recent #0: TaskRecord{4e6fbae #17 A=com.android.contacts U=0 StackId=1 sz=1}\n"
          + "    userId=0 effectiveUid=u0a15 mCallingUid=u0a1\n"
          + "    affinity=com.android.contacts\n"
          + "    intent={act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10200000 cmp=com.android.contacts/.activities.PeopleActivity}\n"
          + "    realActivity=com.android.contacts/.activities.PeopleActivity\n"
          + "  * Recent #1: TaskRecord{9f1c2aa #18 A=com.android.calendar U=0 StackId=1 sz=1}\n"
          + "    realActivity=com.android.calendar/.AllInOneActivity\n"
          + "  * Recent #2: TaskRecord{77ab11c #19 A=com.android.email U=0 StackId=1 sz=1}\n"
          + "    realActivity=com.android.email/.activity.Welcome\n";
    @Test public void findsTheRecentTasksOfThePackages() {
        assertEquals(Arrays.asList(17, 19), ShellProtocol.recentTaskIds(DUMP, Arrays.asList("com.android.contacts", "com.android.email")));
        assertEquals(Arrays.asList(18), ShellProtocol.recentTaskIds(DUMP, Arrays.asList("com.android.calendar")));
        assertTrue(ShellProtocol.recentTaskIds(DUMP, Arrays.asList("com.nothing.here")).isEmpty());
        assertTrue(ShellProtocol.recentTaskIds(null, Arrays.asList("a.b")).isEmpty());
        assertTrue(ShellProtocol.recentTaskIds(DUMP, Arrays.<String>asList()).isEmpty());
    }

    // ---- S2: the allow-listed jobs
    @Test public void onlyListedJobsHaveALine() {
        assertEquals("pm grant com.tj.launcher android.permission.WRITE_SECURE_SETTINGS", ShellProtocol.runLine("GRANT_WSS"));
        assertNull(ShellProtocol.runLine("pm grant com.evil.app android.permission.WRITE_SECURE_SETTINGS"));
        assertNull(ShellProtocol.runLine("GRANT_WSS; reboot"));
        assertNull(ShellProtocol.runLine(null));
    }
    @Test public void everyJobLineOnlyTouchesThisAppOrTheBrowser() {
        for (String[] r : ShellProtocol.RUN) {
            assertTrue(r[0], r[1].contains("com.tj.launcher") || r[1].contains("com.android.browser") || r[1].startsWith("mkdir -p /sdcard/Download/launcher-probe"));
            assertFalse(r[0], r[1].contains(";") || r[1].contains("&") || r[1].contains("|") || r[1].contains("`") || r[1].contains("$"));
        }
    }
    @Test public void parsesRunAndProbeLines() {
        assertEquals("OP_BG", ShellProtocol.runId("RUN OP_BG"));
        assertNull(ShellProtocol.runId("PROBE"));
        assertTrue(ShellProtocol.isProbe("PROBE")); assertTrue(ShellProtocol.isA11yOn(" A11YON "));
    }
    @Test public void mergesTheAccessibilityHelperIntoTheList() {
        assertEquals(ShellProtocol.A11Y, ShellProtocol.mergeA11y(null));
        assertEquals(ShellProtocol.A11Y, ShellProtocol.mergeA11y("null"));
        assertEquals("a.b/c.D:" + ShellProtocol.A11Y, ShellProtocol.mergeA11y("a.b/c.D"));
        assertEquals("a.b/c.D:" + ShellProtocol.A11Y, ShellProtocol.mergeA11y("a.b/c.D:" + ShellProtocol.A11Y));
    }
    @Test public void parsesTheProbeFileAndSkipsItsNotes() {
        String f = "## Read-only probe commands. note\n## Used by tools\n## build and device\ngetprop ro.build.fingerprint\n\n## users\npm list users\n";
        java.util.List<ShellProtocol.ProbeLine> l = ShellProtocol.parseProbeList(f);
        assertEquals(4, l.size());
        assertTrue(l.get(0).title); assertEquals("build and device", l.get(0).text);
        assertFalse(l.get(1).title); assertEquals("getprop ro.build.fingerprint", l.get(1).text);
        assertEquals("pm list users", l.get(3).text);
    }

    @Test public void deviceShellLinesHaveNoAdbPrefixAndStartTheHelper() {
        for (String l : new String[] { ShellProtocol.START_DEVICE_LINE, ShellProtocol.START_DEVICE_SH_C, ShellProtocol.START_DEVICE_TWO_STEP }) {
            assertFalse(l.startsWith("adb"));
            assertTrue(l.contains("app_process") && l.contains("com.tj.launcher.ShellServer"));
        }
        assertTrue(ShellProtocol.START_DEVICE_SH_C.startsWith("sh -c '"));
        assertTrue(ShellProtocol.START_DEVICE_LINE.contains("$(pm path com.tj.launcher"));
    }
    @Test public void shotsAndApksAreFixedRoutines() {
        assertTrue(ShellProtocol.isShots(" SHOTS ")); assertTrue(ShellProtocol.isApks("APKS"));
        assertFalse(ShellProtocol.isShots("SHOTS; reboot")); assertFalse(ShellProtocol.isApks(null));
        for (String p : ShellProtocol.SHOT_PAGES) assertTrue(p, p.matches("[A-Z_]+"));
        for (String p : ShellProtocol.APK_PACKAGES) assertTrue(p, p.matches("[a-z.]+"));
        assertTrue(ShellProtocol.PUBLISH_DIR.startsWith("/sdcard/Download/"));
    }
}
