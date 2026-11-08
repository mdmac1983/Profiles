package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * BH1: when the pop-up banners must be off ({@link NotifQuietPolicy}). The rule is the complement of the animation rule: quiet everywhere EXCEPT an unlocked, decided Quickstep session. Each test pins one
 * way a wrong answer would show Tj a banner in the custom launcher or on the lock screen, or take banners away in Quickstep.
 */
public class NotifQuietPolicyTest {
    private static NotifQuietPolicy.Facts custom() {
        NotifQuietPolicy.Facts f = NotifQuietPolicy.Facts.quickstepSession();
        f.quickstepMode = false;
        return f;
    }

    @Test public void anUnlockedQuickstepSessionIsTheOnlyThingThatIsNotQuiet() {
        NotifQuietPolicy.Facts f = NotifQuietPolicy.Facts.quickstepSession();
        assertEquals("Quickstep session", NotifQuietPolicy.why(f));
        assertFalse(NotifQuietPolicy.wanted(f));
        assertEquals("", NotifQuietPolicy.quietFor(f));
    }

    @Test public void theCustomLauncherIsQuietWhateverTheScreenSays() {
        NotifQuietPolicy.Facts f = custom();
        assertTrue(NotifQuietPolicy.wanted(f));
        assertEquals("custom launcher", NotifQuietPolicy.quietFor(f));
        f.screenOn = false; assertTrue(NotifQuietPolicy.wanted(f));
        f.screenOn = true; f.lockUp = true; assertTrue(NotifQuietPolicy.wanted(f));
        f.lockUp = false; f.unlockUndecided = true; assertTrue(NotifQuietPolicy.wanted(f));
    }

    @Test public void aDarkScreenInQuickstepIsQuietBecauseTheLockScreenIsNext() {
        NotifQuietPolicy.Facts f = NotifQuietPolicy.Facts.quickstepSession();
        f.screenOn = false;
        assertTrue(NotifQuietPolicy.wanted(f));
        assertEquals("screen off", NotifQuietPolicy.quietFor(f));
    }

    @Test public void theLockScreenIsQuietNoMatterWhichLauncherItWasOn() {
        NotifQuietPolicy.Facts f = NotifQuietPolicy.Facts.quickstepSession();
        f.lockUp = true;
        assertTrue(NotifQuietPolicy.wanted(f));
        assertEquals("lock screen", NotifQuietPolicy.quietFor(f));
    }

    @Test public void aPlainPinStillBeingDecidedStaysQuietBecauseTheStoredModeCanBeTheOldSessions() {
        NotifQuietPolicy.Facts f = NotifQuietPolicy.Facts.quickstepSession();
        f.unlockUndecided = true;
        assertTrue(NotifQuietPolicy.wanted(f));
        assertEquals("unlock not decided yet", NotifQuietPolicy.quietFor(f));
    }

    @Test public void aSwitchThatIsOffNeverHolds() {
        NotifQuietPolicy.Facts f = custom();
        f.switchOn = false;
        assertEquals("switch off", NotifQuietPolicy.why(f));
        assertFalse(NotifQuietPolicy.wanted(f));
    }

    @Test public void withoutTheAdbPermissionNothingIsHeld() {
        NotifQuietPolicy.Facts f = custom();
        f.grant = false;
        assertEquals("no adb permission", NotifQuietPolicy.why(f));
    }

    @Test public void whileAutomationIsPausedNothingIsHeld() {
        NotifQuietPolicy.Facts f = custom();
        f.automationOff = true;
        assertEquals("automation paused", NotifQuietPolicy.why(f));
    }

    @Test public void theFirstFailingConditionIsTheOneReported() {
        NotifQuietPolicy.Facts f = new NotifQuietPolicy.Facts();   // everything false
        assertEquals("switch off", NotifQuietPolicy.why(f));
        f.switchOn = true;
        assertEquals("no adb permission", NotifQuietPolicy.why(f));
        f.grant = true; f.automationOff = true;
        assertEquals("automation paused", NotifQuietPolicy.why(f));
    }

    @Test public void theCustomLauncherNeedsNoAccessibilityHelperForTheHold() {
        // a process restart must not flip the banners on while the helper binds (it takes a minute or two after a boot), and without the helper nothing can change the mode anyway
        NotifQuietPolicy.Facts f = custom();
        f.helperUp = false;
        assertTrue(NotifQuietPolicy.wanted(f));
        f.screenOn = false;
        assertTrue(NotifQuietPolicy.wanted(f));
    }

    @Test public void aQuickstepSessionWithoutTheHelperNeverHoldsBecauseNothingCouldEndTheHold() {
        // code review 2026-11-08: a hold taken at the lock with the helper gone would stay for the whole next Quickstep session (nothing hears the unlock)
        NotifQuietPolicy.Facts f = NotifQuietPolicy.Facts.quickstepSession();
        f.helperUp = false;
        assertEquals("accessibility helper not running", NotifQuietPolicy.why(f));
        f.screenOn = false; assertEquals("accessibility helper not running", NotifQuietPolicy.why(f));
        f.screenOn = true; f.lockUp = true; assertEquals("accessibility helper not running", NotifQuietPolicy.why(f));
        f.lockUp = false; f.unlockUndecided = true; assertEquals("accessibility helper not running", NotifQuietPolicy.why(f));
    }

    @Test public void theHelperFactComesAfterTheSwitchTheGrantAndThePause() {
        NotifQuietPolicy.Facts f = NotifQuietPolicy.Facts.quickstepSession();
        f.helperUp = false;
        f.automationOff = true;
        assertEquals("automation paused", NotifQuietPolicy.why(f));
        f.automationOff = false; f.grant = false;
        assertEquals("no adb permission", NotifQuietPolicy.why(f));
        f.grant = true; f.switchOn = false;
        assertEquals("switch off", NotifQuietPolicy.why(f));
    }

    @Test public void theSwitchShipsOnInReleaseAndOffInDebug() {
        assertTrue(NotifQuietPolicy.defaultOn(false));
        assertFalse(NotifQuietPolicy.defaultOn(true));
    }

    // ---------------------------------------------------------------- the setting

    @Test public void zeroAMissingValueAndGarbageAllMeanNoBanners() {
        // exactly what SystemUI reads: Settings.Global.getInt(..., HEADS_UP_OFF) != HEADS_UP_OFF
        assertTrue(NotifQuietPolicy.isOff("0"));
        assertTrue(NotifQuietPolicy.isOff(" 0 "));
        assertTrue(NotifQuietPolicy.isOff(null));
        assertTrue(NotifQuietPolicy.isOff(""));
        assertTrue(NotifQuietPolicy.isOff("abc"));
        assertFalse(NotifQuietPolicy.isOff("1"));
        assertFalse(NotifQuietPolicy.isOff("2"));
    }

    @Test public void onlyAnOnValueNeedsAWrite() {
        assertTrue(NotifQuietPolicy.needsZero("1"));
        assertFalse(NotifQuietPolicy.needsZero("0"));
        assertFalse(NotifQuietPolicy.needsZero(null));
    }

    @Test public void theOriginalIsPutBackOnlyWhileTheSettingIsStillOurZero() {
        assertEquals("1", NotifQuietPolicy.restoreValue("0", "1"));
        assertEquals("1", NotifQuietPolicy.restoreValue(" 0", "1"));
        assertEquals("2", NotifQuietPolicy.restoreValue("0", "2"));
        assertNull("he turned the banners on again by hand: leave his value", NotifQuietPolicy.restoreValue("1", "1"));
        assertNull("somebody deleted the setting: not ours any more", NotifQuietPolicy.restoreValue(null, "1"));
        assertNull("the original was off itself: there is nothing to put back", NotifQuietPolicy.restoreValue("0", "0"));
        assertNull(NotifQuietPolicy.restoreValue("0", null));
    }
}
