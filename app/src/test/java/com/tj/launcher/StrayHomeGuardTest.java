package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class StrayHomeGuardTest {
    private static final long[] NONE = new long[StrayHomeGuard.MAX];

    @Test public void stockHomeMatchesOnlyTheHomeActivity() {
        assertTrue(StrayHomeGuard.isStockHome("com.android.launcher3", "com.android.launcher3.Launcher", "com.android.launcher3", "com.android.launcher3.Launcher"));
        assertFalse(StrayHomeGuard.isStockHome("com.android.launcher3", "com.android.quickstep.RecentsActivity", "com.android.launcher3", "com.android.launcher3.Launcher"));
        assertFalse(StrayHomeGuard.isStockHome("com.tj.launcher", "com.android.launcher3.Launcher", "com.android.launcher3", "com.android.launcher3.Launcher"));
        assertFalse(StrayHomeGuard.isStockHome(null, null, "a", "b"));
    }
    @Test public void redirectsInCustomModeWhenStockHomeAppears() {
        assertTrue(StrayHomeGuard.shouldRedirect(true, true, false, true, 10_000, 0, 0, NONE));
    }
    @Test public void neverInQuickstepMode() { assertFalse(StrayHomeGuard.shouldRedirect(true, false, false, true, 10_000, 0, 0, NONE)); }
    @Test public void neverOnTheLockScreen() { assertFalse(StrayHomeGuard.shouldRedirect(true, true, true, true, 10_000, 0, 0, NONE)); }
    @Test public void switchedOffMeansStock() { assertFalse(StrayHomeGuard.shouldRedirect(false, true, false, true, 10_000, 0, 0, NONE)); }
    @Test public void nothingToDoWhenStockHomeIsNotShown() { assertFalse(StrayHomeGuard.shouldRedirect(true, true, false, false, 10_000, 0, 0, NONE)); }
    @Test public void failOpenWindowStandsDown() { assertFalse(StrayHomeGuard.shouldRedirect(true, true, false, true, 10_000, 20_000, 0, NONE)); }
    @Test public void standDownWindowStandsDown() { assertFalse(StrayHomeGuard.shouldRedirect(true, true, false, true, 10_000, 0, 40_000, NONE)); }
    @Test public void rateLimitStopsALoop() {
        long[] four = {9_000, 9_100, 9_200, 9_300};
        assertEquals(4, StrayHomeGuard.countInWindow(four, 10_000));
        assertFalse(StrayHomeGuard.shouldRedirect(true, true, false, true, 10_000, 0, 0, four));
        assertTrue(StrayHomeGuard.shouldRedirect(true, true, false, true, 20_000, 0, 0, four));   // window slid past
    }
}
