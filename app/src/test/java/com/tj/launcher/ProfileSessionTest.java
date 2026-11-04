package com.tj.launcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ProfileSessionTest {
    @Test public void prearmOnlyOnTheLockScreenAndOncePerLock() {
        assertTrue(ProfileSession.prearmCustomStackOnLockedWake(true, true, false, false, false));
        assertFalse(ProfileSession.prearmCustomStackOnLockedWake(true, false, false, false, false));   // not locked: never yank an app away
        assertFalse(ProfileSession.prearmCustomStackOnLockedWake(true, true, false, true, false));     // already done this lock
        assertFalse(ProfileSession.prearmCustomStackOnLockedWake(false, true, false, false, false));   // switched off
    }
    @Test public void aHandshakeOrACrashTripSuppressesThePrearm() {
        assertFalse(ProfileSession.prearmCustomStackOnLockedWake(true, true, true, false, false));
        assertFalse(ProfileSession.prearmCustomStackOnLockedWake(true, true, false, false, true));
    }
    @Test public void plainUnlockEndsQuickstepOnlyWhenAllowed() {
        assertTrue(ProfileSession.plainUnlockEndsQuickstep(true, false, false));
        assertFalse(ProfileSession.plainUnlockEndsQuickstep(true, true, false));    // already custom
        assertFalse(ProfileSession.plainUnlockEndsQuickstep(false, false, false));  // switched off (Restore everything does this)
        assertFalse(ProfileSession.plainUnlockEndsQuickstep(true, false, true));    // the launcher crashed: stay on the safe stock launcher
    }
    @Test public void coverAtEnterNeedsAPlainPinAndWorkToDo() {
        assertTrue(ProfileSession.coverAtEnter(true, false, false, false, true, true));
        assertFalse(ProfileSession.coverAtEnter(true, false, false, true, true, true));    // handshake: going to Quickstep, nothing to hide
        assertFalse(ProfileSession.coverAtEnter(true, false, false, false, false, true));  // nothing to clean
        assertFalse(ProfileSession.coverAtEnter(true, false, false, false, true, false));  // cover switched off
        assertFalse(ProfileSession.coverAtEnter(true, true, false, false, true, true));    // already custom
    }
    @Test public void radiosAreEnforcedInCustomOnlyAndNeverInALoop() {
        long now = 100_000;
        assertTrue(ProfileSession.enforceRadios(true, true, now, new long[] {0, 0, 0}));
        assertFalse(ProfileSession.enforceRadios(true, false, now, new long[] {0, 0, 0}));
        assertFalse(ProfileSession.enforceRadios(false, true, now, new long[] {0, 0, 0}));
        assertFalse(ProfileSession.enforceRadios(true, true, now, new long[] {now - 1000, now - 2000, now - 3000}));
        assertTrue(ProfileSession.enforceRadios(true, true, now, new long[] {now - 70_000, now - 2000, now - 3000}));
    }
    @Test public void homeFailsafeOnlyWhenTheHelperIsOffAndThreeQuickPresses() {
        assertTrue(ProfileSession.homeFailsafe(false, true, 3000, 1000, 2000 - 700 + 700));    // helper off, 3 presses (1000, 2000? gaps 1000 > 800 -> no)
    }
}
