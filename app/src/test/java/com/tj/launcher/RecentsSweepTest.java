package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import org.junit.Test;

/** BE2: the drawer-membership rule for Recents tasks (pure parts of RecentsSweep). */
public class RecentsSweepTest {
    private static final TaskOrigin.Guard GUARD = new TaskOrigin.Guard() {
        @Override public boolean mayClose(String pkg) { return ResetPolicy.shouldKill(pkg, "com.tj.launcher", "com.android.launcher3", "com.android.inputmethod.latin"); }
    };

    private static RecentsSweep.Membership drawer(final String... shown) {
        final Set<String> s = new HashSet<>(Arrays.asList(shown));
        return new RecentsSweep.Membership() { @Override public boolean inDrawer(String pkg) { return s.contains(pkg); } };
    }

    private static LinkedHashMap<Integer, String> tasks(Object... idPkg) {
        LinkedHashMap<Integer, String> m = new LinkedHashMap<>();
        for (int i = 0; i < idPkg.length; i += 2) m.put((Integer) idPkg[i], (String) idPkg[i + 1]);
        return m;
    }

    @Test public void hiddenOnlyRemovesEveryTaskOfAnAppThatIsNotInTheDrawer() {
        // BD1 (Tj, 2026-11-07): "telegram and Claude were in the recent apps list ... they are hidden from the custom launcher" - whatever their origin
        LinkedHashMap<Integer, String> t = tasks(21, "org.telegram.messenger", 20, "com.anthropic.claude", 19, "com.android.contacts", 18, "com.tj.launcher", 17, "com.android.launcher3");
        RecentsSweep.Plan p = RecentsSweep.plan(t, RecentsSweep.HIDDEN_ONLY, drawer("com.android.contacts"), GUARD, null);
        assertEquals(Arrays.asList(21, 20), p.removeIds);
        assertEquals(new HashSet<>(Arrays.asList("org.telegram.messenger", "com.anthropic.claude")), p.packages);
        assertEquals(1, p.kept);              // Contacts is in the drawer: stays
        assertEquals(2, p.protectedTasks);    // us + the stock launcher: never touched
        assertEquals(5, p.tasks);
    }

    @Test public void aDrawerAppIsNeverRemovedByTheGuarantee() {
        LinkedHashMap<Integer, String> t = tasks(5, "com.android.chrome", 6, "com.android.calendar");
        RecentsSweep.Plan p = RecentsSweep.plan(t, RecentsSweep.HIDDEN_ONLY, drawer("com.android.chrome", "com.android.calendar"), GUARD, null);
        assertTrue(p.removeIds.isEmpty());
        assertEquals(2, p.kept);
    }

    @Test public void completePolicyRemovesDrawerAppsToo() {
        LinkedHashMap<Integer, String> t = tasks(5, "com.android.chrome", 6, "org.telegram.messenger", 7, "com.android.launcher3");
        RecentsSweep.Plan p = RecentsSweep.plan(t, RecentsSweep.ALL, null, GUARD, null);
        assertEquals(Arrays.asList(5, 6), p.removeIds);
        assertEquals(1, p.protectedTasks);
    }

    @Test public void theProtectedPackagesSurviveBothPolicies() {
        LinkedHashMap<Integer, String> t = tasks(1, "com.tj.launcher", 2, "com.android.launcher3", 3, "com.android.systemui", 4, "android", 5, "com.android.phone", 6, "com.android.inputmethod.latin", 7, "com.mediatek.ims");
        for (int policy : new int[] { RecentsSweep.HIDDEN_ONLY, RecentsSweep.ALL }) {
            RecentsSweep.Plan p = RecentsSweep.plan(t, policy, drawer(), GUARD, null);
            assertTrue("policy " + policy, p.removeIds.isEmpty());
            assertEquals(7, p.protectedTasks);
        }
    }

    @Test public void theAppOnScreenIsSkipped() {
        LinkedHashMap<Integer, String> t = tasks(8, "org.telegram.messenger", 9, "com.anthropic.claude");
        RecentsSweep.Plan p = RecentsSweep.plan(t, RecentsSweep.HIDDEN_ONLY, drawer(), GUARD, Collections.singleton("org.telegram.messenger"));
        assertEquals(Collections.singletonList(9), p.removeIds);
        assertEquals(1, p.kept);
    }

    @Test public void badRowsAreProtectedNotRemoved() {
        LinkedHashMap<Integer, String> t = new LinkedHashMap<>();
        t.put(0, "org.x.y");      // id 0 / negative: never a task id the helper may be given
        t.put(-3, "org.x.z");
        t.put(11, null);
        t.put(12, "");
        RecentsSweep.Plan p = RecentsSweep.plan(t, RecentsSweep.ALL, null, GUARD, null);
        assertTrue(p.removeIds.isEmpty());
        assertEquals(4, p.protectedTasks);
    }

    @Test public void oneSweepRemovesAtMostWhatTheHelperTakes() {
        LinkedHashMap<Integer, String> t = new LinkedHashMap<>();
        for (int i = 1; i <= 50; i++) t.put(i, "org.hidden.app" + i);
        RecentsSweep.Plan p = RecentsSweep.plan(t, RecentsSweep.HIDDEN_ONLY, drawer(), GUARD, null);
        assertEquals(ShellProtocol.MAX_PACKAGES, p.removeIds.size());
        assertEquals(50 - ShellProtocol.MAX_PACKAGES, p.deferred);
    }

    @Test public void nullTaskListIsNothing() {
        RecentsSweep.Plan p = RecentsSweep.plan(null, RecentsSweep.ALL, null, GUARD, null);
        assertTrue(p.removeIds.isEmpty());
        assertEquals(0, p.tasks);
    }

    @Test public void aHiddenAppThatLeftTheScreenInTheCustomLauncherIsSwept() {
        assertTrue(RecentsSweep.hiddenAppLeft(true, true, "org.telegram.messenger", "com.tj.launcher", true, false));
        assertFalse("Quickstep may use any app", RecentsSweep.hiddenAppLeft(false, true, "org.telegram.messenger", "com.tj.launcher", true, false));
        assertFalse("locked: the lock path handles it", RecentsSweep.hiddenAppLeft(true, false, "org.telegram.messenger", "com.tj.launcher", true, false));
        assertFalse("a drawer app stays", RecentsSweep.hiddenAppLeft(true, true, "com.android.chrome", "com.tj.launcher", true, true));
        assertFalse("system / protected packages are never closed", RecentsSweep.hiddenAppLeft(true, true, "com.android.systemui", "com.tj.launcher", false, false));
        assertFalse("still the same app", RecentsSweep.hiddenAppLeft(true, true, "org.telegram.messenger", "org.telegram.messenger", true, false));
        assertFalse("nothing before", RecentsSweep.hiddenAppLeft(true, true, null, "com.tj.launcher", true, false));
        assertFalse("nothing before (empty)", RecentsSweep.hiddenAppLeft(true, true, "", "com.tj.launcher", true, false));
    }

    @Test public void theSwitchShipsOnAndDebugBuildsKeepTheOlderBehaviour() {
        assertTrue(RecentsSweep.enabledDefault(false));
        assertFalse(RecentsSweep.enabledDefault(true));
    }

    @Test public void theLogLineNamesPackagesAndCountsOnly() {
        LinkedHashMap<Integer, String> t = tasks(21, "org.telegram.messenger", 19, "com.android.contacts", 17, "com.android.launcher3");
        RecentsSweep.Plan p = RecentsSweep.plan(t, RecentsSweep.HIDDEN_ONLY, drawer("com.android.contacts"), GUARD, null);
        String s = RecentsSweep.describe("lock", RecentsSweep.HIDDEN_ONLY, p, 1);
        assertTrue(s, s.startsWith("lock: removed 1 of 1 task(s) of non-drawer apps [org.telegram.messenger]"));
        assertTrue(s, s.contains("1 kept, 1 protected"));
        assertTrue(RecentsSweep.describe("lock", RecentsSweep.ALL, p, 1).contains("(complete)"));
        assertTrue(RecentsSweep.describe("lock", RecentsSweep.HIDDEN_ONLY, p, -1).contains("NOT removed"));
    }
}
