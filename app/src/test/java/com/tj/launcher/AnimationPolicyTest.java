package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * BF1 (Tj, 2026-11-07 23:36 UTC): "turn off all animations ... turned back on automatically and immediately on both the lock screen and in the custom launcher. Animations should only ever be turned off inside
 * quickstep launcher". The rule that decides WHEN, and the bookkeeping of the two reasons that may hold the scales at zero.
 */
public class AnimationPolicyTest {
    private static AnimationPolicy.Facts session() { return AnimationPolicy.Facts.session(); }

    @Test public void anOrdinaryUnlockedQuickstepSessionWantsTheAnimationsOff() {
        assertTrue(AnimationPolicy.wanted(session()));
        assertEquals("", AnimationPolicy.why(session()));
    }

    @Test public void theLockScreenIsNeverAnimationFree() {
        AnimationPolicy.Facts f = session(); f.lockUp = true;
        assertFalse("the lock screen's patch follows the stock 300 ms fade: the animations must be real there", AnimationPolicy.wanted(f));
        assertEquals("lock screen", AnimationPolicy.why(f));
    }

    @Test public void aDarkScreenIsTheLockScreenAboutToShow() {
        AnimationPolicy.Facts f = session(); f.screenOn = false;
        assertFalse("screen off: the animations are back BEFORE the lock screen can show", AnimationPolicy.wanted(f));
        assertEquals("screen off", AnimationPolicy.why(f));
    }

    @Test public void theCustomLauncherIsNeverAnimationFree() {
        AnimationPolicy.Facts f = session(); f.quickstepMode = false;
        assertFalse(AnimationPolicy.wanted(f));
        assertEquals("custom launcher", AnimationPolicy.why(f));
    }

    @Test public void aPlainPinRightAfterAQuickstepSessionDoesNotBlinkTheAnimationsOff() {
        // the unlock handler flips the stored mode to custom a few ms AFTER the keyguard is gone: until it has answered, "Quickstep + unlocked" is the OLD session
        AnimationPolicy.Facts f = session(); f.unlockUndecided = true;
        assertFalse(AnimationPolicy.wanted(f));
        assertEquals("unlock not decided yet", AnimationPolicy.why(f));
    }

    @Test public void withoutTheHelperNothingCouldEndTheHold() {
        AnimationPolicy.Facts f = session(); f.helperUp = false;
        assertFalse("the accessibility helper is what hears the screen go off", AnimationPolicy.wanted(f));
        assertEquals("accessibility helper not running", AnimationPolicy.why(f));
    }

    @Test public void theSwitchAndTheAdbGrantAreNeeded() {
        AnimationPolicy.Facts f = session(); f.switchOn = false;
        assertEquals("switch off", AnimationPolicy.why(f));
        f = session(); f.grant = false;
        assertEquals("no adb permission", AnimationPolicy.why(f));
    }

    @Test public void restoreEverythingAndTheCrashBreakerPauseIt() {
        AnimationPolicy.Facts f = session(); f.automationOff = true;
        assertFalse(AnimationPolicy.wanted(f));
        assertEquals("automation paused", AnimationPolicy.why(f));
    }

    @Test public void theFirstFailingConditionIsTheOneNamed() {
        AnimationPolicy.Facts f = session(); f.switchOn = false; f.quickstepMode = false; f.lockUp = true;
        assertEquals("switch off", AnimationPolicy.why(f));
        f.switchOn = true;
        assertEquals("custom launcher", AnimationPolicy.why(f));
        f.quickstepMode = true; f.screenOn = false;
        assertEquals("screen off", AnimationPolicy.why(f));
    }

    @Test public void exactlyOneCombinationOfTheEightFactsWantsThemOff() {
        int wanted = 0;
        for (int m = 0; m < 256; m++) {
            AnimationPolicy.Facts f = new AnimationPolicy.Facts();
            f.switchOn = (m & 1) != 0; f.grant = (m & 2) != 0; f.helperUp = (m & 4) != 0; f.quickstepMode = (m & 8) != 0;
            f.automationOff = (m & 16) != 0; f.screenOn = (m & 32) != 0; f.lockUp = (m & 64) != 0; f.unlockUndecided = (m & 128) != 0;
            boolean expect = f.switchOn && f.grant && f.helperUp && f.quickstepMode && !f.automationOff && f.screenOn && !f.lockUp && !f.unlockUndecided;
            assertEquals("mask " + m, expect, AnimationPolicy.wanted(f));
            assertEquals("mask " + m + ": the words and the decision never disagree", expect, AnimationPolicy.why(f).isEmpty());
            if (expect) wanted++;
        }
        assertEquals("only the ordinary unlocked Quickstep session", 1, wanted);
    }

    @Test public void theShippedAppAnimatesLessAndTheDebugBuildKeepsTheOlderBehaviour() {
        assertTrue("release: ON", AnimationPolicy.defaultOn(false));
        assertFalse("debug (emulator): OFF, so the older sections keep their premises", AnimationPolicy.defaultOn(true));
    }

    @Test public void theKeyIsStable() {
        // the emulator section `an` writes this name into tj_features.xml; a rename would silently turn it into a no-op
        assertEquals("quickstep_animations_off", AnimationPolicy.KEY);
    }

    @Test public void theUndecidedValveIsShort() {
        assertTrue(AnimationPolicy.UNDECIDED_MAX_MS >= 2000 && AnimationPolicy.UNDECIDED_MAX_MS <= 10_000);
    }

    // ---------------------------------------------------------------- the two reasons

    @Test public void theTwoReasonsAreIndependent() {
        int h = 0;
        h = AnimationPolicy.withHold(h, AnimationPolicy.HOLD_QUICKSTEP, true);
        h = AnimationPolicy.withHold(h, AnimationPolicy.HOLD_RESET, true);
        assertTrue(AnimationPolicy.has(h, AnimationPolicy.HOLD_QUICKSTEP));
        assertTrue(AnimationPolicy.has(h, AnimationPolicy.HOLD_RESET));
        h = AnimationPolicy.withHold(h, AnimationPolicy.HOLD_RESET, false);
        assertTrue("the reset ending must not give the animations back inside the Quickstep session", AnimationPolicy.has(h, AnimationPolicy.HOLD_QUICKSTEP));
        assertFalse(AnimationPolicy.has(h, AnimationPolicy.HOLD_RESET));
        h = AnimationPolicy.withHold(h, AnimationPolicy.HOLD_QUICKSTEP, false);
        assertEquals("the last reason ending leaves nothing held", 0, h);
    }

    @Test public void settingAReasonTwiceOrClearingAnUnsetOneChangesNothing() {
        assertEquals(AnimationPolicy.HOLD_RESET, AnimationPolicy.withHold(AnimationPolicy.HOLD_RESET, AnimationPolicy.HOLD_RESET, true));
        assertEquals(0, AnimationPolicy.withHold(0, AnimationPolicy.HOLD_QUICKSTEP, false));
        assertEquals(AnimationPolicy.HOLD_RESET, AnimationPolicy.withHold(AnimationPolicy.HOLD_RESET, AnimationPolicy.HOLD_QUICKSTEP, false));
    }

    // ---------------------------------------------------------------- one scale

    @Test public void zeroIsRecognisedInAnySpelling() {
        assertTrue(AnimationPolicy.isOff("0")); assertTrue(AnimationPolicy.isOff("0.0")); assertTrue(AnimationPolicy.isOff("0.00")); assertTrue(AnimationPolicy.isOff(" 0 "));
        assertFalse(AnimationPolicy.isOff("1.0")); assertFalse(AnimationPolicy.isOff("0.5")); assertFalse(AnimationPolicy.isOff("10")); assertFalse(AnimationPolicy.isOff(""));
        assertFalse(AnimationPolicy.isOff(null)); assertFalse(AnimationPolicy.isOff("abc")); assertFalse(AnimationPolicy.isOff("NaN"));
    }

    @Test public void anUnsetScaleIsRememberedAsAndroidsDefault() {
        assertEquals("1.0", AnimationPolicy.originalOf(null));
        assertEquals("1.0", AnimationPolicy.originalOf("  "));
        assertEquals("0.5", AnimationPolicy.originalOf("0.5"));
        assertEquals("0", AnimationPolicy.originalOf("0"));
    }

    @Test public void aScaleThatIsAlreadyOffIsNotWritten() {
        assertFalse("Tj's own permanent setting stays his", AnimationPolicy.needsZero("0"));
        assertFalse(AnimationPolicy.needsZero("0.0"));
        assertTrue(AnimationPolicy.needsZero("1.0")); assertTrue(AnimationPolicy.needsZero("0.5")); assertTrue(AnimationPolicy.needsZero(null));
    }

    @Test public void onlyAScaleThatIsStillAtOurZeroIsPutBack() {
        assertEquals("1.0", AnimationPolicy.restoreValue("0", "1.0"));
        assertEquals("0.5", AnimationPolicy.restoreValue("0", "0.5"));
        assertEquals("1.0", AnimationPolicy.restoreValue("0.0", null));
        assertNull("Tj set it to 1.5 in Developer options during the session: his value stays", AnimationPolicy.restoreValue("1.5", "1.0"));
        assertNull("... even when it equals the original: nothing to write", AnimationPolicy.restoreValue("1.0", "1.0"));
        assertNull("it was zero before we came: it stays zero", AnimationPolicy.restoreValue("0", "0"));
        assertNull(AnimationPolicy.restoreValue("0", "0.0"));
        assertNull("unreadable current value: write nothing", AnimationPolicy.restoreValue(null, "1.0"));
    }
}
