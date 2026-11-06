package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** AD4 (Tj, 2026-11-06): hidden, switchable entrances to Quickstep from the custom launcher; "unlikely to be triggered by accident". */
public class SecretEntrancesTest {
    private static final int W = 600;
    private static final int L = -1, R = 1;

    // ---- the swipe classifier
    @Test public void aLongQuickHorizontalSwipeCounts() { assertEquals(R, SecretEntrances.swipeDirection(300, 20, 200, W)); assertEquals(L, SecretEntrances.swipeDirection(-300, -40, 300, W)); }
    @Test public void aShortSwipeIsNothing() { assertEquals(0, SecretEntrances.swipeDirection(150, 0, 200, W)); }
    @Test public void aSlowDragIsNothing() { assertEquals(0, SecretEntrances.swipeDirection(400, 0, 1200, W)); }
    @Test public void aDiagonalSwipeIsNothing() { assertEquals(0, SecretEntrances.swipeDirection(300, 200, 200, W)); }
    @Test public void aVerticalSwipeIsNothing() { assertEquals(0, SecretEntrances.swipeDirection(10, 500, 200, W)); }
    @Test public void nonsenseInputIsNothing() { assertEquals(0, SecretEntrances.swipeDirection(300, 0, 0, W)); assertEquals(0, SecretEntrances.swipeDirection(300, 0, 100, 0)); }

    // ---- page flick
    private static boolean flick(SecretEntrances.Flick f, long t0, long gap, int... dirs) {
        boolean done = false; long t = t0;
        for (int d : dirs) { done = f.onSwipe(d, t); t += gap; }
        return done;
    }
    @Test public void sixAlternatingSwipesInTimeComplete() { assertTrue(flick(new SecretEntrances.Flick(), 1000, 800, L, R, L, R, L, R)); assertTrue(flick(new SecretEntrances.Flick(), 1000, 800, R, L, R, L, R, L)); }
    @Test public void fiveAreNotEnough() { assertFalse(flick(new SecretEntrances.Flick(), 1000, 800, L, R, L, R, L)); }
    @Test public void aRepeatedDirectionStartsOver() { assertFalse(flick(new SecretEntrances.Flick(), 1000, 800, L, R, L, L, R, L)); }
    @Test public void tooSlowIsNothing() { assertFalse(flick(new SecretEntrances.Flick(), 1000, 1700, L, R, L, R, L, R)); }   // 5 x 1.7 s = 8.5 s > 8 s window
    @Test public void aLongPauseBetweenSwipesStartsOver() { assertFalse(flick(new SecretEntrances.Flick(), 1000, 2600, L, R, L, R, L, R)); }
    @Test public void aLongerBackAndForthKeepsTheLastSix() {
        SecretEntrances.Flick f = new SecretEntrances.Flick();
        assertFalse(flick(f, 1000, 1500, L, R, L));                       // slow start (4.5 s so far) ...
        assertTrue(flick(f, 1000 + 3 * 1500, 400, R, L, R));              // ... fast finish: the LAST six alternate and sit inside the window
    }
    @Test public void afterSuccessItStartsOver() { SecretEntrances.Flick f = new SecretEntrances.Flick(); assertTrue(flick(f, 1000, 500, L, R, L, R, L, R)); assertEquals(0, f.count()); assertFalse(f.onSwipe(L, 9000)); }
    @Test public void aNonSwipeDirectionIsIgnored() { SecretEntrances.Flick f = new SecretEntrances.Flick(); assertFalse(f.onSwipe(0, 1000)); assertEquals(0, f.count()); }

    // ---- rotation tile
    @Test public void fiveFastChangesComplete() {
        SecretEntrances.Toggles t = new SecretEntrances.Toggles();
        boolean done = false; for (int i = 0; i < 5; i++) done = t.onChange(1000 + i * 1500L);
        assertTrue(done);
    }
    @Test public void fourAreNotEnough() { SecretEntrances.Toggles t = new SecretEntrances.Toggles(); boolean done = false; for (int i = 0; i < 4; i++) done = t.onChange(1000 + i * 1500L); assertFalse(done); }
    @Test public void tooSlowIsNotACode() { SecretEntrances.Toggles t = new SecretEntrances.Toggles(); boolean done = false; for (int i = 0; i < 5; i++) done = t.onChange(1000 + i * 3000L); assertFalse(done); }   // 12 s span
    @Test public void aDoubleNotificationOfOneTapCountsOnce() {
        SecretEntrances.Toggles t = new SecretEntrances.Toggles();
        t.onChange(1000); t.onChange(1050); t.onChange(1090);
        assertEquals(1, t.count());
    }
    @Test public void afterSuccessTheTogglesStartOver() { SecretEntrances.Toggles t = new SecretEntrances.Toggles(); for (int i = 0; i < 5; i++) t.onChange(1000 + i * 1000L); assertEquals(0, t.count()); }

    // ---- charger
    @Test public void inOutInInsideTheWindowCompletes() {
        SecretEntrances.Plug p = new SecretEntrances.Plug();
        assertFalse(p.onPower(true, 1000)); assertFalse(p.onPower(false, 3000)); assertTrue(p.onPower(true, 6000));
    }
    @Test public void tooSlowIsNotACodeForTheCharger() {
        SecretEntrances.Plug p = new SecretEntrances.Plug();
        p.onPower(true, 1000); p.onPower(false, 5000); assertFalse(p.onPower(true, 9500));   // 8.5 s after the first plug-in
    }
    @Test public void outInOutIsNothing() { SecretEntrances.Plug p = new SecretEntrances.Plug(); assertFalse(p.onPower(false, 1000)); assertFalse(p.onPower(true, 2000)); assertFalse(p.onPower(false, 3000)); }
    @Test public void aSecondPlugInRestartsTheAttempt() {
        SecretEntrances.Plug p = new SecretEntrances.Plug();
        p.onPower(true, 1000); p.onPower(true, 5000);                               // the attempt restarts at 5000
        assertFalse(p.onPower(false, 6000)); assertTrue(p.onPower(true, 12000));    // inside 8 s of the restart, outside 8 s of the first
    }

    // ---- the list
    @Test public void defaultsKeepTwoEntrancesAlwaysOnAndTheChargerOff() {
        assertTrue(SecretEntrances.Method.FLICK.defaultOn); assertTrue(SecretEntrances.Method.ROTATE.defaultOn); assertFalse(SecretEntrances.Method.CHARGER.defaultOn);
    }
    @Test public void switchKeysAreStable() {
        assertEquals("entrance_flick", SecretEntrances.Method.FLICK.prefKey());
        assertEquals("entrance_rotate_tile", SecretEntrances.Method.ROTATE.prefKey());
        assertEquals("entrance_charger", SecretEntrances.Method.CHARGER.prefKey());
    }
}
