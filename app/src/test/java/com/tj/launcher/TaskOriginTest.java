package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

/** AA3: the task-precise clean-up rules (pure parts) and the helper's two new protocol lines. */
public class TaskOriginTest {
    private static final TaskOrigin.Guard GUARD = new TaskOrigin.Guard() {
        @Override public boolean mayClose(String pkg) { return ResetPolicy.shouldKill(pkg, "com.tj.launcher", "com.android.launcher3", "com.android.inputmethod.latin"); }
    };

    private static LinkedHashMap<Integer, String> tasks(Object... idPkg) {
        LinkedHashMap<Integer, String> m = new LinkedHashMap<>();
        for (int i = 0; i < idPkg.length; i += 2) m.put((Integer) idPkg[i], (String) idPkg[i + 1]);
        return m;
    }

    // what `dumpsys activity recents` really prints on Android 10 (the first lines of each block; note the affinities that are NOT package names)
    private static final String DUMP = ""
            + "ACTIVITY MANAGER RECENT TASKS (dumpsys activity recents)\n"
            + "mRecentsComponent=ComponentInfo{com.android.launcher3/com.android.quickstep.RecentsActivity}\n"
            + "Recent tasks:\n"
            + "* Recent #0: TaskRecord{5d4ea80 #8 A=android.task.calendar U=0 StackId=1 sz=1}\n"
            + "    userId=0 effectiveUid=u0a79 mCallingUid=u0a79 mUserSetupComplete=true mCallingPackage=null\n"
            + "    affinity=android.task.calendar\n"
            + "    intent={act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10200000 cmp=com.android.calendar/.AllInOneActivity}\n"
            + "    realActivity=com.android.calendar/.AllInOneActivity\n"
            + "    autoRemoveRecents=false isPersistable=true numFullscreen=1 activityType=1\n"
            + "* Recent #1: TaskRecord{24144fe #10 A=android.task.contacts U=0 StackId=2 sz=1}\n"
            + "    userId=0 effectiveUid=u0a80 mCallingUid=u0a80 mUserSetupComplete=true mCallingPackage=null\n"
            + "    affinity=android.task.contacts\n"
            + "    intent={act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10200000 cmp=com.android.contacts/.activities.PeopleActivity}\n"
            + "    realActivity=com.android.contacts/.activities.PeopleActivity\n"
            + "* Recent #2: TaskRecord{8de2eac #7 I=com.tj.launcher/com.android.launcher3.Launcher U=0 StackId=0 sz=1}\n"
            + "    userId=0 effectiveUid=u0a95 mCallingUid=u0a95 mUserSetupComplete=true mCallingPackage=null\n"
            + "    intent={act=android.intent.action.MAIN cat=[android.intent.category.SECONDARY_HOME] flg=0x10200000 pkg=com.tj.launcher cmp=com.tj.launcher/com.android.launcher3.Launcher}\n"
            + "    realActivity=com.tj.launcher/com.android.launcher3.Launcher\n"
            + "* Recent #3: TaskRecord{dda7775 #5 I=com.android.launcher3/.Launcher U=0 StackId=0 sz=1}\n"
            + "    userId=0 effectiveUid=u0a60 mCallingUid=u0a60 mUserSetupComplete=true mCallingPackage=null\n"
            + "* Recent #4: TaskRecord{5d777b #2 A=com.android.settings.FallbackHome U=0 StackId=-1 sz=0}\n";

    @Test public void theParserReadsTheRootActivityNotTheAffinity() {
        Map<Integer, String> t = ShellProtocol.parseTasks(DUMP);
        assertEquals("com.android.calendar", t.get(8));
        assertEquals("com.android.contacts", t.get(10));        // affinity android.task.contacts would have been wrong
        assertEquals("com.tj.launcher", t.get(7));
        assertEquals("com.android.launcher3", t.get(5));        // only the header says it (no realActivity line)
        assertEquals(Arrays.asList(8, 10, 7, 5, 2), Arrays.asList(t.keySet().toArray(new Integer[0])));   // order = Recents order
    }
    @Test public void theParserSurvivesGarbage() {
        assertTrue(ShellProtocol.parseTasks(null).isEmpty());
        assertTrue(ShellProtocol.parseTasks("").isEmpty());
        assertTrue(ShellProtocol.parseTasks("nothing useful\nrealActivity=com.x.y/.Z\n").isEmpty());   // a realActivity line outside any task block is ignored
    }
    @Test public void theHelpersTwoNewLines() {
        assertTrue(ShellProtocol.isListTasks(" LISTTASKS ")); assertFalse(ShellProtocol.isListTasks("LISTTASKS 3")); assertFalse(ShellProtocol.isListTasks(null));
        assertEquals(Arrays.asList(12, 14), ShellProtocol.removeTaskIds("RMTASKS 12 14 x 15;reboot -3 12 0 99999999999"));   // numbers only, positive, no duplicates, no huge values
        assertNull(ShellProtocol.removeTaskIds("FORCESTOP com.a.b")); assertNull(ShellProtocol.removeTaskIds(null)); assertNull(ShellProtocol.removeTaskIds("rmtasks 5"));
        assertTrue(ShellProtocol.removeTaskIds("RMTASKS").isEmpty());
    }
    @Test public void atMostFortyIds() {
        StringBuilder b = new StringBuilder("RMTASKS");
        for (int i = 1; i <= 90; i++) b.append(' ').append(i);
        assertEquals(ShellProtocol.MAX_PACKAGES, ShellProtocol.removeTaskIds(b.toString()).size());
    }

    @Test public void encodeAndDecodeRoundTrip() {
        LinkedHashMap<Integer, String> m = tasks(8, "com.android.calendar", 10, "com.android.contacts");
        assertEquals("8:com.android.calendar,10:com.android.contacts", TaskOrigin.encode(m));
        assertEquals(m, TaskOrigin.decode(TaskOrigin.encode(m)));
        assertTrue(TaskOrigin.decode(null).isEmpty()); assertTrue(TaskOrigin.decode("").isEmpty());
        assertEquals(tasks(3, "a.b"), TaskOrigin.decode("x:y,3:a.b,:q,4:,5"));   // bad pieces are skipped
    }
    @Test public void aStaleQuickstepLeftoverIsNotProtectedByTheSnapshot() {
        LinkedHashMap<Integer, String> listed = tasks(8, "com.android.calendar", 10, "com.android.contacts", 11, "org.leftover.app");
        Set<String> leftovers = new HashSet<>(Arrays.asList("org.leftover.app"));
        assertEquals(tasks(8, "com.android.calendar", 10, "com.android.contacts"), TaskOrigin.baseFor(listed, leftovers));
        assertEquals(listed, TaskOrigin.baseFor(listed, Collections.<String>emptySet()));
    }

    @Test public void theMainCase_newQuickstepAppsAreClosedAndASharedAppKeepsItsCustomTask() {
        // custom account at the start: home tasks, Solitaire (the target, here Calendar) and Contacts (opened earlier in the custom account)
        LinkedHashMap<Integer, String> base = tasks(5, "com.android.launcher3", 7, "com.tj.launcher", 8, "com.android.calendar", 10, "com.android.contacts");
        // during Quickstep: Gallery and Email were opened, Contacts was opened a SECOND time in its own new task, and Solitaire was pre-armed again
        LinkedHashMap<Integer, String> now = tasks(5, "com.android.launcher3", 7, "com.tj.launcher", 8, "com.android.calendar", 10, "com.android.contacts",
                11, "com.android.gallery3d", 12, "com.android.email", 13, "com.android.contacts", 14, "com.android.calendar");
        TaskOrigin.Plan p = TaskOrigin.plan(base, now, Arrays.asList("com.android.gallery3d", "com.android.contacts", "com.android.email"), new HashSet<String>(), "com.android.calendar", GUARD);
        assertEquals(new LinkedHashSet<>(Arrays.asList("com.android.gallery3d", "com.android.email")), p.stopPackages);   // closed completely
        assertEquals(Arrays.asList(13), p.removeIds);                                                                    // only Quickstep's own Contacts task goes
        assertEquals(new LinkedHashSet<>(Arrays.asList("com.android.contacts")), p.spared);                              // the old list would have killed the custom account's Contacts
        assertEquals(3, p.newTasks);                                                                                     // the target's new task is not counted, never touched
        assertFalse(p.stopPackages.contains("com.android.calendar"));
    }
    @Test public void anAppTheAccessibilityTrackingMissedIsStillFoundByTheTaskList() {
        TaskOrigin.Plan p = TaskOrigin.plan(tasks(8, "com.android.calendar"), tasks(8, "com.android.calendar", 20, "org.missed.app"),
                Collections.<String>emptyList(), new HashSet<String>(), "com.android.calendar", GUARD);
        assertEquals(new LinkedHashSet<>(Arrays.asList("org.missed.app")), p.stopPackages);
        assertTrue(p.removeIds.isEmpty());
    }
    @Test public void anAppTheCustomLauncherStartedOnlyLosesItsNewTask() {
        TaskOrigin.Plan p = TaskOrigin.plan(tasks(8, "com.android.calendar"), tasks(8, "com.android.calendar", 21, "org.shared.app"),
                Collections.<String>emptyList(), new HashSet<>(Arrays.asList("org.shared.app")), "com.android.calendar", GUARD);
        assertTrue(p.stopPackages.isEmpty());
        assertEquals(Arrays.asList(21), p.removeIds);
    }
    @Test public void nothingProtectedIsEverPlanned() {
        TaskOrigin.Plan p = TaskOrigin.plan(tasks(8, "com.android.calendar"),
                tasks(8, "com.android.calendar", 30, "com.tj.launcher", 31, "com.android.launcher3", 32, "com.android.inputmethod.latin", 33, "com.android.systemui", 34, "android"),
                Arrays.asList("com.tj.launcher", "com.android.launcher3", "com.android.systemui"), new HashSet<String>(), "com.android.calendar", GUARD);
        assertTrue(p.stopPackages.isEmpty()); assertTrue(p.removeIds.isEmpty()); assertEquals(0, p.newTasks);
    }
    @Test public void anOldTaskNeverLooksNew() {
        LinkedHashMap<Integer, String> same = tasks(5, "com.android.launcher3", 10, "com.android.contacts", 11, "com.android.gallery3d");
        TaskOrigin.Plan p = TaskOrigin.plan(same, same, Collections.<String>emptyList(), new HashSet<String>(), "com.android.calendar", GUARD);
        assertTrue(p.stopPackages.isEmpty()); assertTrue(p.removeIds.isEmpty()); assertEquals(0, p.newTasks);
    }
    @Test public void aPendingAppWithoutAnyTaskIsStillClosedAsBefore() {
        // e.g. an app whose window appeared but whose task is already gone: the old rule (force-stop) still applies when the custom account has nothing of it
        TaskOrigin.Plan p = TaskOrigin.plan(tasks(8, "com.android.calendar"), tasks(8, "com.android.calendar"), Arrays.asList("org.window.only"), new HashSet<String>(), "com.android.calendar", GUARD);
        assertEquals(new LinkedHashSet<>(Arrays.asList("org.window.only")), p.stopPackages);
    }

    // ---------------------------------------------------------------- AB3: a shared app with a notification is closed after all
    private static final String NOTIF_DUMP = ""
            + "Current Notification Manager state:\n"
            + "  Notification List:\n"
            + "    NotificationRecord(0x0aa30a1f: pkg=com.android.contacts user=UserHandle{0} id=1 tag=null importance=3 key=0|com.android.contacts|1|null|10080: Notification(channel=missed pri=0 contentView=null vibrate=null sound=null defaults=0x0 flags=0x10 color=0x00000000 vis=PRIVATE))\n"
            + "      uid=10080 userId=0\n"
            + "    NotificationRecord(0x0b7733c2: pkg=com.example.work user=UserHandle{10} id=4 tag=x importance=2 key=10|com.example.work|4|x|1010080: Notification(channel=a pri=0))\n"
            + "    NotificationRecord(0x0c1d44a0: pkg=com.tj.launcher user=UserHandle{0} id=7471 tag=null importance=2 key=0|com.tj.launcher|7471|null|10095: Notification(channel=tj_keep_alive pri=0))\n"
            + "  mArchive (last 5 removed notifications):\n"
            + "    StatusBarNotification(pkg=com.android.email user=UserHandle{0} id=2 tag=null key=0|com.android.email|2|null|10071: Notification(channel=mail pri=0))\n";

    @Test public void theNotificationParserReadsOnlyActiveRecordsOfUserZero() {
        Set<String> got = ShellProtocol.parseNotifPackages(NOTIF_DUMP);
        assertEquals(new LinkedHashSet<>(Arrays.asList("com.android.contacts", "com.tj.launcher")), got);   // not the work profile, not the archive
        assertTrue(ShellProtocol.parseNotifPackages(null).isEmpty());
        assertTrue(ShellProtocol.parseNotifPackages("nothing here").isEmpty());
        assertTrue(ShellProtocol.isNotifPkgs("NOTIFPKGS")); assertTrue(ShellProtocol.isNotifPkgs(" NOTIFPKGS \n")); assertFalse(ShellProtocol.isNotifPkgs("NOTIFPKGS x")); assertFalse(ShellProtocol.isNotifPkgs(null));
    }

    @Test public void aSharedAppWithANotificationIsClosedNotSpared() {
        // Contacts is in the custom account AND was opened again in Quickstep (new task 12) and has a notification
        LinkedHashMap<Integer, String> base = tasks(8, "com.android.calendar", 10, "com.android.contacts");
        LinkedHashMap<Integer, String> now = tasks(12, "com.android.contacts", 8, "com.android.calendar", 10, "com.android.contacts");
        Set<String> notified = new HashSet<>(Arrays.asList("com.android.contacts"));
        TaskOrigin.Plan p = TaskOrigin.plan(base, now, Arrays.asList("com.android.contacts"), new HashSet<String>(), "com.android.calendar", GUARD, notified);
        assertEquals(new LinkedHashSet<>(Arrays.asList("com.android.contacts")), p.stopPackages);
        assertEquals(new LinkedHashSet<>(Arrays.asList("com.android.contacts")), p.notificationStops);
        assertTrue("its task goes with the force-stop, no separate removal", p.removeIds.isEmpty());
        assertTrue(p.spared.isEmpty());
    }

    @Test public void aSharedAppWithoutANotificationIsStillSpared() {
        LinkedHashMap<Integer, String> base = tasks(8, "com.android.calendar", 10, "com.android.contacts");
        LinkedHashMap<Integer, String> now = tasks(12, "com.android.contacts", 8, "com.android.calendar", 10, "com.android.contacts");
        for (Set<String> notified : Arrays.<Set<String>>asList(null, new HashSet<String>(), new HashSet<>(Arrays.asList("com.example.other")))) {
            TaskOrigin.Plan p = TaskOrigin.plan(base, now, Arrays.asList("com.android.contacts"), new HashSet<String>(), "com.android.calendar", GUARD, notified);
            assertTrue(p.stopPackages.isEmpty());
            assertEquals(Arrays.asList(12), p.removeIds);
            assertEquals(new LinkedHashSet<>(Arrays.asList("com.android.contacts")), p.spared);
            assertTrue(p.notificationStops.isEmpty());
        }
    }

    @Test public void theTargetAndTheProtectedAppsKeepTheirNotifications() {
        // Solitaire (the target), the keyboard, this app and the stock launcher are never closed, whatever the notification list says
        LinkedHashMap<Integer, String> base = tasks(8, "com.android.calendar", 6, "com.tj.launcher", 5, "com.android.launcher3");
        LinkedHashMap<Integer, String> now = tasks(14, "com.android.calendar", 15, "com.tj.launcher", 16, "com.android.launcher3", 8, "com.android.calendar", 6, "com.tj.launcher", 5, "com.android.launcher3");
        Set<String> notified = new HashSet<>(Arrays.asList("com.android.calendar", "com.tj.launcher", "com.android.launcher3", "com.android.inputmethod.latin"));
        TaskOrigin.Plan p = TaskOrigin.plan(base, now, Arrays.asList("com.android.calendar", "com.tj.launcher", "com.android.inputmethod.latin"), new HashSet<String>(), "com.android.calendar", GUARD, notified);
        assertTrue(p.stopPackages.isEmpty()); assertTrue(p.removeIds.isEmpty()); assertTrue(p.notificationStops.isEmpty());
    }

    @Test public void anAppOnlyQuickstepOpenedIsClosedWhetherOrNotItHasANotification() {
        TaskOrigin.Plan p = TaskOrigin.plan(tasks(8, "com.android.calendar"), tasks(20, "com.android.email", 8, "com.android.calendar"), Arrays.asList("com.android.email"), new HashSet<String>(), "com.android.calendar", GUARD,
                new HashSet<>(Arrays.asList("com.android.email")));
        assertEquals(new LinkedHashSet<>(Arrays.asList("com.android.email")), p.stopPackages);
        assertTrue("it was never shared, so it is not counted as a notification stop", p.notificationStops.isEmpty());
    }

    @Test public void anAppOnlyTheCustomLauncherStartedIsSparedInBothLoops() {
        // review of 2026-11-05: the custom launcher started Contacts, its task is not in the start snapshot, and (however it got there) the package is also on the old Quickstep list: it must be spared like a new task of it would be
        TaskOrigin.Plan p = TaskOrigin.plan(tasks(8, "com.android.calendar"), tasks(8, "com.android.calendar"), Arrays.asList("com.android.contacts"),
                new HashSet<>(Arrays.asList("com.android.contacts")), "com.android.calendar", GUARD);
        assertTrue(p.stopPackages.isEmpty());
        assertEquals(new LinkedHashSet<>(Arrays.asList("com.android.contacts")), p.spared);
    }

    @Test public void aCustomLauncherStartedAppOpenedAgainInQuickstepWithANotificationIsClosed() {
        // the custom launcher started Contacts itself (AppOrigin.custom), the session began without a Contacts task, Quickstep opened a task and it posted a notification
        TaskOrigin.Plan p = TaskOrigin.plan(tasks(8, "com.android.calendar"), tasks(12, "com.android.contacts", 8, "com.android.calendar"), Collections.<String>emptyList(),
                new HashSet<>(Arrays.asList("com.android.contacts")), "com.android.calendar", GUARD, new HashSet<>(Arrays.asList("com.android.contacts")));
        assertEquals(new LinkedHashSet<>(Arrays.asList("com.android.contacts")), p.stopPackages);
        assertTrue(p.removeIds.isEmpty());
    }
}
