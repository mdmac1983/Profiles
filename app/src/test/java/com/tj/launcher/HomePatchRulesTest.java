package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * BI1: the rules of the real-pixel patch on the unlocked home screen ({@link HomePatchRules}). Each test pins one way a wrong answer would show Tj the stock Screenshot button on the home screen, a patch
 * that does not match what is under it, or a camera flash at a bad moment.
 */
public class HomePatchRulesTest {
    private static HomePatchRules.Facts good() {
        HomePatchRules.Facts f = new HomePatchRules.Facts();
        f.switchOn = true; f.coverMode = true; f.customMode = true; f.unlocked = true; f.screenOn = true; f.homeShowing = true; f.rectKnown = true; f.cropValid = true;
        return f;
    }

    private static HomePatchRules.CalState calGood() {
        HomePatchRules.CalState s = new HomePatchRules.CalState();
        s.switchOn = true; s.coverMode = true; s.customMode = true; s.unlocked = true; s.screenOn = true; s.homeAtRest = true; s.rectKnown = true; s.permission = true;
        return s;
    }

    // ---------------------------------------------------------------- the window is wanted

    @Test public void everythingInOrderWantsThePatch() {
        assertEquals("", HomePatchRules.whyNot(good()));
        assertTrue(HomePatchRules.wanted(good()));
    }

    @Test public void eachConditionAloneTakesThePatchAway() {
        HomePatchRules.Facts f;
        f = good(); f.switchOn = false; assertEquals("switch off", HomePatchRules.whyNot(f));
        f = good(); f.coverMode = false; assertEquals("power menu mode is not 'hide Screenshot'", HomePatchRules.whyNot(f));
        f = good(); f.automationOff = true; assertEquals("automation paused", HomePatchRules.whyNot(f));
        f = good(); f.customMode = false; assertEquals("Quickstep", HomePatchRules.whyNot(f));
        f = good(); f.unlocked = false; assertEquals("lock screen", HomePatchRules.whyNot(f));
        f = good(); f.screenOn = false; assertEquals("screen off", HomePatchRules.whyNot(f));
        f = good(); f.homeShowing = false; assertEquals("not on the plain home screen", HomePatchRules.whyNot(f));
        f = good(); f.floatingViewOpen = true; assertEquals("a folder or popup is open", HomePatchRules.whyNot(f));
        f = good(); f.busy = true; assertEquals("dragging or changing screen", HomePatchRules.whyNot(f));
        f = good(); f.calibrating = true; assertEquals("calibrating", HomePatchRules.whyNot(f));
        f = good(); f.rectKnown = false; assertTrue(HomePatchRules.whyNot(f).startsWith("open the power menu once"));
        f = good(); f.cropValid = false; assertEquals("not calibrated for this wallpaper and page yet", HomePatchRules.whyNot(f));
    }

    @Test public void theFirstFailingConditionIsTheOneReported() {
        HomePatchRules.Facts f = new HomePatchRules.Facts();   // everything false
        assertEquals("switch off", HomePatchRules.whyNot(f));
        f.switchOn = true;
        assertEquals("power menu mode is not 'hide Screenshot'", HomePatchRules.whyNot(f));
        f.coverMode = true; f.automationOff = true;
        assertEquals("automation paused", HomePatchRules.whyNot(f));
    }

    @Test public void theSwitchShipsOnInReleaseAndOffInDebug() {
        assertTrue(HomePatchRules.defaultOn(false));
        assertFalse(HomePatchRules.defaultOn(true));
    }

    // ---------------------------------------------------------------- fresh, due, visible

    @Test public void aPatchBuiltAfterTheLastDrawIsFreshAndOneBuiltBeforeItIsNot() {
        assertTrue(HomePatchRules.compositeFresh(1000, 900, true));
        assertTrue("built in the same millisecond as the last draw: the draw may have come first, but the request was made after the frame", HomePatchRules.compositeFresh(1000, 1000, true));
        assertFalse(HomePatchRules.compositeFresh(1000, 1001, true));
    }

    @Test public void aPatchBuiltWithAnotherWallpaperCropIsNeverFresh() {
        assertFalse(HomePatchRules.compositeFresh(1000, 0, false));
    }

    @Test public void noPatchBuiltYetIsNotFresh() {
        assertFalse(HomePatchRules.compositeFresh(0, 0, true));
    }

    @Test public void aBuildIsDueOnlyWhenNothingFreshIsThereNothingIsBuildingAndTheHomeIsQuiet() {
        assertTrue(HomePatchRules.buildDue(10_000, 9_000, false, false));
        assertFalse("a fresh one exists", HomePatchRules.buildDue(10_000, 9_000, true, false));
        assertFalse("one is being built", HomePatchRules.buildDue(10_000, 9_000, false, true));
        assertFalse("the home drew 100 ms ago", HomePatchRules.buildDue(10_000, 9_900, false, false));
        assertTrue("exactly the quiet time", HomePatchRules.buildDue(10_000, 10_000 - HomePatchRules.DRAW_QUIET_MS, false, false));
        assertTrue("never drew", HomePatchRules.buildDue(10_000, 0, false, false));
    }

    @Test public void thePatchIsVisibleOnlyWhenRestableAndBothTimersAreOver() {
        long now = 100_000;
        assertTrue(HomePatchRules.visible(true, now, now - HomePatchRules.TOUCH_AWAY_MS, now - HomePatchRules.DRAW_QUIET_MS));
        assertFalse("not restable", HomePatchRules.visible(false, now, 0, 0));
        assertFalse("a finger went down 300 ms ago", HomePatchRules.visible(true, now, now - 300, 0));
        assertFalse("the home drew 100 ms ago", HomePatchRules.visible(true, now, 0, now - 100));
        assertTrue("never touched, never drew", HomePatchRules.visible(true, now, 0, 0));
    }

    @Test public void waitMsIsTheLongerOfTheTwoRemainingTimers() {
        long now = 100_000;
        assertEquals(0, HomePatchRules.waitMs(now, 0, 0));
        assertEquals(HomePatchRules.TOUCH_AWAY_MS, HomePatchRules.waitMs(now, now, 0));
        assertEquals(HomePatchRules.DRAW_QUIET_MS - 50, HomePatchRules.waitMs(now, 0, now - 50));
        assertEquals(HomePatchRules.TOUCH_AWAY_MS - 100, HomePatchRules.waitMs(now, now - 100, now - 50));
        assertEquals(0, HomePatchRules.waitMs(now, now - 5000, now - 5000));
    }

    @Test public void aClockThatWentBackwardsNeverHidesThePatchForEver() {
        assertTrue(HomePatchRules.visible(true, 1000, 5000, 6000));
        assertEquals(0, HomePatchRules.waitMs(1000, 5000, 6000));
        assertTrue(HomePatchRules.buildDue(1000, 6000, false, false));
    }

    // ---------------------------------------------------------------- which wallpaper crop

    @Test public void theWallpaperOffsetIsAStepNumber() {
        assertEquals(0, HomePatchRules.step(0f));
        assertEquals(500, HomePatchRules.step(0.5f));
        assertEquals(333, HomePatchRules.step(1f / 3f));
        assertEquals(1000, HomePatchRules.step(1f));
        assertEquals("out of range values are clamped", 1000, HomePatchRules.step(1.7f));
        assertEquals(0, HomePatchRules.step(-0.2f));
        assertEquals(0, HomePatchRules.step(Float.NaN));
    }

    @Test public void offsetsThatDifferInTheLastDigitsAreTheSamePosition() {
        assertTrue(HomePatchRules.sameStep(333, 334));
        assertTrue(HomePatchRules.sameStep(333, 331));
        assertFalse(HomePatchRules.sameStep(333, 336));
        assertFalse(HomePatchRules.sameStep(0, 333));
    }

    @Test public void theSignatureHasTheSameShapeAsTheLockScreensAndNamesTheFilePerRotationAndStep() {
        assertEquals("42|r0|600x1024", HomePatchRules.sig(42, 0, 600, 1024));
        assertEquals("homeshot_r0_x0.png", HomePatchRules.fileName(0, 0));
        assertEquals("homeshot_r1_x333.png", HomePatchRules.fileName(1, 333));
        assertEquals("home_shot_r0_x500", HomePatchRules.metaKey(0, 500));
    }

    // ---------------------------------------------------------------- the calibration by itself

    @Test public void everythingQuietStartsTheCalibration() {
        assertEquals("", HomePatchRules.calDecision(calGood()));
    }

    @Test public void eachVetoStopsTheCalibrationAndTheFirstOneIsReported() {
        HomePatchRules.CalState s;
        s = calGood(); s.switchOn = false; assertEquals("switch off", HomePatchRules.calDecision(s));
        s = calGood(); s.coverMode = false; assertEquals("power menu mode is not 'hide Screenshot'", HomePatchRules.calDecision(s));
        s = calGood(); s.automationOff = true; assertEquals("automation paused", HomePatchRules.calDecision(s));
        s = calGood(); s.customMode = false; assertEquals("Quickstep", HomePatchRules.calDecision(s));
        s = calGood(); s.unlocked = false; assertEquals("lock screen", HomePatchRules.calDecision(s));
        s = calGood(); s.screenOn = false; assertEquals("screen off", HomePatchRules.calDecision(s));
        s = calGood(); s.permission = false; assertTrue(HomePatchRules.calDecision(s).startsWith("needs 'Allow wallpaper access'"));
        s = calGood(); s.running = true; assertEquals("a calibration is running", HomePatchRules.calDecision(s));
        s = calGood(); s.otherCalibration = true; assertEquals("the lock-screen calibration is running", HomePatchRules.calDecision(s));
        s = calGood(); s.hasCrop = true; assertEquals("already calibrated for this wallpaper and page", HomePatchRules.calDecision(s));
        s = calGood(); s.rectKnown = false; assertTrue(HomePatchRules.calDecision(s).startsWith("open the power menu once"));
        s = calGood(); s.homeAtRest = false; assertEquals("the home screen is not at rest", HomePatchRules.calDecision(s));
        s = calGood(); s.menuOpen = true; assertEquals("the power menu is open", HomePatchRules.calDecision(s));
    }

    @Test public void theCalibrationWaitsForTheFingerTheDrawsAndTheWake() {
        HomePatchRules.CalState s;
        s = calGood(); s.msSinceTouch = HomePatchRules.CAL_TOUCH_QUIET_MS - 1; assertEquals("touched a moment ago", HomePatchRules.calDecision(s));
        s = calGood(); s.msSinceTouch = HomePatchRules.CAL_TOUCH_QUIET_MS; assertEquals("", HomePatchRules.calDecision(s));
        s = calGood(); s.msSinceDraw = HomePatchRules.CAL_DRAW_QUIET_MS - 1; assertEquals("the home screen is still moving", HomePatchRules.calDecision(s));
        s = calGood(); s.msSinceScreenOn = HomePatchRules.CAL_SCREEN_ON_MS - 1; assertEquals("the screen came on a moment ago", HomePatchRules.calDecision(s));
    }

    @Test public void theCalibrationIsRateLimited() {
        HomePatchRules.CalState s = calGood();
        s.msSinceLastStart = HomePatchRules.CAL_MIN_GAP_MS - 1;
        assertEquals("tried a moment ago", HomePatchRules.calDecision(s));
        s = calGood(); s.startsToday = HomePatchRules.CAL_MAX_PER_DAY;
        assertEquals("tried " + HomePatchRules.CAL_MAX_PER_DAY + " times today", HomePatchRules.calDecision(s));
        s = calGood(); s.startsToday = HomePatchRules.CAL_MAX_PER_DAY - 1;
        assertEquals("", HomePatchRules.calDecision(s));
    }

    @Test public void aScreenshotIsOursWhenItIsNoOlderThanTheRequestMinusTheRoundingSlack() {
        long asked = 1_000_000_000_000L;
        assertTrue(HomePatchRules.shotIsOurs(asked / 1000, asked));
        assertTrue("a second early: the rounding", HomePatchRules.shotIsOurs(asked / 1000 - 1, asked));
        assertFalse("an old one", HomePatchRules.shotIsOurs(asked / 1000 - 10, asked));
        assertFalse("no request made", HomePatchRules.shotIsOurs(asked / 1000, 0));
    }
}
