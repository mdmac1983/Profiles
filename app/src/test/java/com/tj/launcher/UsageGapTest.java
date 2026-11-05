package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** AC1: the "the helper was away at the unlock" safety net. The usage query needs Android; the decision is pure. */
public class UsageGapTest {
    private static final long REF = 1_000_000, AFTER = 1_500_000, BEFORE = 900_000, NOW = 2_000_000;

    @Test public void aLockAfterTheSessionBeganEndsIt() { assertTrue(UsageGap.shouldEndSession(false, false, false, true, AFTER, REF)); }
    @Test public void aLockBeforeTheSessionBeganDoesNot() { assertFalse(UsageGap.shouldEndSession(false, false, false, true, BEFORE, REF)); }
    @Test public void noLockEventAtAllDoesNot() { assertFalse(UsageGap.shouldEndSession(false, false, false, true, 0, REF)); }
    @Test public void anUnknownSessionStartNeverEndsAnything() { assertFalse(UsageGap.lockedSince(AFTER, 0)); assertFalse(UsageGap.shouldEndSession(false, false, false, true, AFTER, 0)); }
    @Test public void neverInTheCustomLauncher() { assertFalse(UsageGap.shouldEndSession(true, false, false, true, AFTER, REF)); }
    @Test public void neverWhileAutomationIsPaused() { assertFalse(UsageGap.shouldEndSession(false, true, false, true, AFTER, REF)); }
    @Test public void neverOnTheLockScreenOrWithTheScreenOff() {
        assertFalse(UsageGap.shouldEndSession(false, false, true, true, AFTER, REF));    // the keyguard is up right now: the lock rules handle the unlock themselves
        assertFalse(UsageGap.shouldEndSession(false, false, false, false, AFTER, REF));  // screen off: nobody is looking at it
    }
    @Test public void theSameInstantIsNotAfter() { assertFalse(UsageGap.lockedSince(REF, REF)); }
    @Test public void theQueryNeverLooksBackMoreThanTwelveHours() {
        assertEquals(REF, UsageGap.queryFrom(REF, NOW));
        assertEquals(NOW - UsageGap.MAX_LOOKBACK_MS, UsageGap.queryFrom(1, NOW));
    }
    @Test public void theEventTypeIsAndroidsKeyguardShown() { assertEquals(17, UsageGap.KEYGUARD_SHOWN); }
}
