package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CrashGuardTest {
    @Test public void firstCrashCountsOne() { assertEquals(1, CrashGuard.countInWindow(new long[]{0, 0, 0}, 1_000_000)); }

    @Test public void threeInTwoMinutesTrips() {
        long now = 1_000_000;
        assertEquals(CrashGuard.LIMIT, CrashGuard.countInWindow(new long[]{now - 30_000, now - 60_000, 0}, now));
    }

    // ---- CBR1 (v0.7.2): a tripped breaker is cleared by the first start of a NEWER version (a fair try); three new crashes trip it again
    @Test public void aNewerVersionClearsATrippedBreaker() { assertTrue(CrashGuard.shouldAutoClear(true, 18, 19)); }
    @Test public void aBreakerTrippedBeforeThisLogicExistedIsClearedByTheFirstNewVersion() { assertTrue(CrashGuard.shouldAutoClear(true, 0, 19)); }
    @Test public void theSameOrAnOlderVersionNeverClearsIt() { assertFalse(CrashGuard.shouldAutoClear(true, 19, 19)); assertFalse(CrashGuard.shouldAutoClear(true, 20, 19)); }
    @Test public void anUnreadableVersionNeverClearsIt() { assertFalse(CrashGuard.shouldAutoClear(true, 0, 0)); }
    @Test public void aBreakerThatIsNotTrippedIsNothingToClear() { assertFalse(CrashGuard.shouldAutoClear(false, 0, 19)); }
    @Test public void theTrippingVersionIsRememberedAndAnUnreadableOneNeverClearsAutomatically() {
        assertEquals(19, CrashGuard.tripVersionToStore(19));
        assertEquals(Integer.MAX_VALUE, CrashGuard.tripVersionToStore(0));      // 0 means "tripped before this logic existed", so an unreadable version must not use it
        assertEquals(Integer.MAX_VALUE, CrashGuard.tripVersionToStore(-1));
        assertFalse(CrashGuard.shouldAutoClear(true, CrashGuard.tripVersionToStore(0), 19));
    }

    // ---- AC1 (v0.7.3): the launcher's own lock-screen automation is paused by the breaker OR by "Restore everything" / an update that cleared the breaker
    @Test public void automationIsOffWhenTrippedOrPaused() {
        assertTrue(CrashGuard.automationOff(true, false));
        assertTrue(CrashGuard.automationOff(false, true));
        assertTrue(CrashGuard.automationOff(true, true));
        assertFalse(CrashGuard.automationOff(false, false));
    }

    @Test public void oldCrashesDoNotCount() {
        long now = 1_000_000;
        assertEquals(1, CrashGuard.countInWindow(new long[]{now - 600_000, now - 500_000, now - 400_000}, now));
    }
}
