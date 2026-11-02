package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CrashGuardTest {
    @Test public void firstCrashCountsOne() { assertEquals(1, CrashGuard.countInWindow(new long[]{0, 0, 0}, 1_000_000)); }

    @Test public void threeInTwoMinutesTrips() {
        long now = 1_000_000;
        assertEquals(CrashGuard.LIMIT, CrashGuard.countInWindow(new long[]{now - 30_000, now - 60_000, 0}, now));
    }

    @Test public void oldCrashesDoNotCount() {
        long now = 1_000_000;
        assertEquals(1, CrashGuard.countInWindow(new long[]{now - 600_000, now - 500_000, now - 400_000}, now));
    }
}
