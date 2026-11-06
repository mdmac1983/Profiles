package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tj.launcher.QuickstepUsage.Ev;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/** AI1: which apps were used during a Quickstep session (Android's usage events), without what only appeared under the lock screen. */
public class QuickstepUsageTest {
    private static final int R = QuickstepUsage.RESUMED, S = QuickstepUsage.KEYGUARD_SHOWN, H = QuickstepUsage.KEYGUARD_HIDDEN;
    private static List<Ev> evs(Ev... e) { return new ArrayList<>(Arrays.asList(e)); }

    @Test public void anAppResumedWhileUnlockedIsQuickstepsApp() {
        assertEquals(Arrays.asList("com.portfolio"), new ArrayList<>(QuickstepUsage.packages(evs(new Ev(1000, R, "com.portfolio")))));
    }
    @Test public void firstUseOrderIsKeptAndDuplicatesAreDropped() {
        List<String> got = new ArrayList<>(QuickstepUsage.packages(evs(new Ev(1000, R, "a"), new Ev(2000, R, "b"), new Ev(3000, R, "a"))));
        assertEquals(Arrays.asList("a", "b"), got);
    }
    @Test public void nothingUsedMeansNothing() {
        assertTrue(QuickstepUsage.packages(evs()).isEmpty());
        assertTrue(QuickstepUsage.packages(null).isEmpty());
    }
    @Test public void whatAppearsWhileTheLockScreenIsUpIsNotCounted() {
        // the session began unlocked, the tablet locked, Android revealed the custom account's last app behind the keyguard
        List<Ev> e = evs(new Ev(1000, R, "a"), new Ev(2000, S, null), new Ev(2100, R, "custom.last"), new Ev(5000, H, null), new Ev(9000, R, "b"));
        assertEquals(Arrays.asList("a", "b"), new ArrayList<>(QuickstepUsage.packages(e)));
    }
    @Test public void aSessionThatBeganOnTheLockScreenIgnoresEverythingBeforeTheUnlock() {
        // the handshake happens ON the lock screen: the first keyguard event is HIDDEN, so the early resume is lock-screen time
        List<Ev> e = evs(new Ev(1000, R, "custom.last"), new Ev(1500, H, null), new Ev(9000, R, "portfolio"));
        assertEquals(Arrays.asList("portfolio"), new ArrayList<>(QuickstepUsage.packages(e)));
    }
    @Test public void theMomentsRightAfterTheUnlockAreNotCountedEither() {
        List<Ev> e = evs(new Ev(1000, S, null), new Ev(2000, H, null), new Ev(2000 + ProfileSession.UNLOCK_QUIET_MS - 1, R, "leftover"), new Ev(2000 + ProfileSession.UNLOCK_QUIET_MS, R, "real"));
        assertEquals(Arrays.asList("real"), new ArrayList<>(QuickstepUsage.packages(e)));
    }
    @Test public void emptyOrMissingPackageNamesAreIgnored() {
        List<Ev> e = evs(new Ev(1000, R, null), new Ev(1100, R, ""), new Ev(1200, R, "ok"));
        assertEquals(Collections.singletonList("ok"), new ArrayList<>(QuickstepUsage.packages(e)));
    }
    @Test public void otherEventTypesDoNotCount() {
        assertFalse(QuickstepUsage.packages(evs(new Ev(1000, 2, "paused"), new Ev(1100, 23, "stopped"))).contains("paused"));
    }
}
