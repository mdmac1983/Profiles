package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class AppOriginTest {
    private static Set<String> s(String... a) { return new HashSet<>(Arrays.asList(a)); }

    @Test public void onlyQuickstepOpenedAppsAreClosed() { assertEquals(s("brave"), AppOrigin.pending(s("clock"), s("brave"))); }

    // v0.7.6 (review of v0.7.5): the span of Android usage events that counts as "used in THIS Quickstep session" (AI1)
    @Test public void aSessionWithoutAKnownStartReadsNothing() { assertEquals(null, AppOrigin.usageWindow(0, false, 0, 1000, 0)); }
    @Test public void whileQuickstepIsStillOnTheWindowEndsNow() {
        long[] w = AppOrigin.usageWindow(100, false, 0, 1000, 0);
        assertEquals(100, w[0]); assertEquals(1000, w[1]);
    }
    @Test public void afterReturningToCustomTheWindowEndsWhenTheSessionEnded() {
        long[] w = AppOrigin.usageWindow(100, true, 600, 9999, 0);
        assertEquals(100, w[0]); assertEquals(600, w[1]);
    }
    @Test public void aNeverEndedOrReversedSessionReadsNothing() {
        assertEquals(null, AppOrigin.usageWindow(100, true, 0, 9999, 0));     // custom, but no end was ever stored
        assertEquals(null, AppOrigin.usageWindow(100, true, 100, 9999, 0));   // equal timestamps: empty
        assertEquals(null, AppOrigin.usageWindow(500, true, 400, 9999, 0));   // end before start
    }
    @Test public void whatAnEarlierCleanUpDealtWithIsNotReadAgain() {
        long[] w = AppOrigin.usageWindow(100, false, 0, 1000, 700);
        assertEquals(700, w[0]); assertEquals(1000, w[1]);
        assertEquals(null, AppOrigin.usageWindow(100, false, 0, 1000, 1000));   // dealt with up to now: nothing new
        assertEquals(null, AppOrigin.usageWindow(100, true, 600, 9999, 650));  // dealt with past the session end
        long[] older = AppOrigin.usageWindow(100, false, 0, 1000, 50);          // an older mark never widens the window before the session start
        assertEquals(100, older[0]);
    }
    @Test public void anAppTheCustomLauncherAlsoStartedIsKept() { assertEquals(s(), AppOrigin.pending(s("brave"), s("brave"))); }
    @Test public void nothingPendingWhenQuickstepOpenedNothing() { assertTrue(AppOrigin.pending(s("clock"), s()).isEmpty()); }
    @Test public void cardMatchesTheAppLabel() { assertTrue(AppOrigin.cardMatches("Brave", s("Brave"))); assertTrue(AppOrigin.cardMatches("brave, 5 minutes left today", s("Brave"))); }
    @Test public void cardDoesNotMatchOtherApps() { assertFalse(AppOrigin.cardMatches("Braver Rabbit", s("Brave"))); assertFalse(AppOrigin.cardMatches("Solitaire", s("Brave"))); assertFalse(AppOrigin.cardMatches(null, s("Brave"))); assertFalse(AppOrigin.cardMatches("", s("Brave"))); }
}
