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

    @Test public void aLongListIsNotCappedAtWhatOneHelperLineTakes() {
        // code review 2026-11-07: the complete clear has no "next sweep", so one plan holds every task (the helper client sends them in chunks)
        LinkedHashMap<Integer, String> t = new LinkedHashMap<>();
        for (int i = 1; i <= 120; i++) t.put(i, "org.hidden.app" + i);
        RecentsSweep.Plan p = RecentsSweep.plan(t, RecentsSweep.ALL, null, GUARD, null);
        assertEquals(120, p.removeIds.size());
        assertEquals(0, p.deferred);
        LinkedHashMap<Integer, String> big = new LinkedHashMap<>();
        for (int i = 1; i <= RecentsSweep.MAX_TASKS + 5; i++) big.put(i, "org.hidden.app" + i);
        RecentsSweep.Plan q = RecentsSweep.plan(big, RecentsSweep.ALL, null, GUARD, null);
        assertEquals(RecentsSweep.MAX_TASKS, q.removeIds.size());
        assertEquals(5, q.deferred);      // only the absurd case: the caller must not call that complete
    }

    @Test public void transientSystemScreensAreNeverRemoved() {
        // an active call, an install / permission prompt, a file picker: not in the drawer, but not "apps" either (code review 2026-11-07)
        LinkedHashMap<Integer, String> t = tasks(1, "com.google.android.dialer", 2, "com.android.incallui", 3, "com.android.packageinstaller", 4, "com.google.android.permissioncontroller", 5, "com.android.documentsui", 6, "org.telegram.messenger");
        for (int policy : new int[] { RecentsSweep.HIDDEN_ONLY, RecentsSweep.ALL }) {
            RecentsSweep.Plan p = RecentsSweep.plan(t, policy, drawer(), GUARD, null);
            assertEquals("policy " + policy, Collections.singletonList(6), p.removeIds);
            assertEquals(5, p.protectedTasks);
        }
        assertTrue(RecentsSweep.systemLike("com.android.packageinstaller"));
        assertFalse(RecentsSweep.systemLike("org.telegram.messenger"));
        assertFalse(RecentsSweep.systemLike(null));
    }

    @Test public void nullTaskListIsNothing() {
        RecentsSweep.Plan p = RecentsSweep.plan(null, RecentsSweep.ALL, null, GUARD, null);
        assertTrue(p.removeIds.isEmpty());
        assertEquals(0, p.tasks);
    }

    @Test public void aHiddenAppOnScreenIsRememberedAndSweptWhenTheHomeComesBack() {
        // (seen, atHome, inDrawer, mayClose, systemLike)
        assertEquals(RecentsSweep.STEP_REMEMBER, RecentsSweep.hiddenSeenStep(false, false, false, true, false));
        assertEquals("home after a hidden app: sweep", RecentsSweep.STEP_SWEEP, RecentsSweep.hiddenSeenStep(true, true, true, false, false));
        assertEquals("home with nothing remembered: nothing", RecentsSweep.STEP_NOTHING, RecentsSweep.hiddenSeenStep(false, true, true, false, false));
    }

    @Test public void aHiddenAppsOwnChildWindowSweepsNothing() {
        // the hidden app is remembered; a share sheet / picker / prompt comes to the front (not home, not a new hidden app): nothing happens until the HOME is back
        assertEquals(RecentsSweep.STEP_NOTHING, RecentsSweep.hiddenSeenStep(true, false, false, false, false));   // the chooser is package 'android': mayClose is false
        assertEquals(RecentsSweep.STEP_NOTHING, RecentsSweep.hiddenSeenStep(true, false, false, true, true));    // a permission prompt: system-like
        assertEquals(RecentsSweep.STEP_NOTHING, RecentsSweep.hiddenSeenStep(true, false, true, false, false));   // a drawer app opened from it
    }

    @Test public void drawerAppsSystemAppsAndLikeAreNeverRemembered() {
        assertEquals(RecentsSweep.STEP_NOTHING, RecentsSweep.hiddenSeenStep(false, false, true, true, false));    // in the drawer
        assertEquals(RecentsSweep.STEP_NOTHING, RecentsSweep.hiddenSeenStep(false, false, false, false, false));  // may not be closed at all (system, keyboard, us)
        assertEquals(RecentsSweep.STEP_NOTHING, RecentsSweep.hiddenSeenStep(false, false, false, true, true));    // system-like (call, installer, picker)
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
