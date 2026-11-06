package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** AD5 (Tj, 2026-11-06): a gesture unlock comes back to the app that was open, not the stock home. */
public class ResumeTargetTest {
    private static final String OWN = "com.tj.launcher", STOCK = "com.android.launcher3";

    @Test public void anAppOpenedInQuickstepWhileUnlockedIsRemembered() { assertTrue(ResumeTarget.remember("com.android.chrome", OWN, STOCK, false, false)); }
    @Test public void nothingIsRememberedInTheCustomLauncher() { assertFalse(ResumeTarget.remember("com.android.chrome", OWN, STOCK, true, false)); }
    @Test public void nothingIsRememberedOnTheLockScreen() { assertFalse(ResumeTarget.remember("com.android.chrome", OWN, STOCK, false, true)); }
    @Test public void ourOwnWindowsTheStockHomeAndTheSystemAreNeverTheApp() {
        assertFalse(ResumeTarget.remember(OWN, OWN, STOCK, false, false));
        assertFalse(ResumeTarget.remember(STOCK, OWN, STOCK, false, false));
        assertFalse(ResumeTarget.remember("android", OWN, STOCK, false, false));
        assertFalse(ResumeTarget.remember("com.android.systemui", OWN, STOCK, false, false));
        assertFalse(ResumeTarget.remember(null, OWN, STOCK, false, false));
        assertFalse(ResumeTarget.remember("", OWN, STOCK, false, false));
    }
    @Test public void theStockHomeMeansNothingToComeBackTo() { assertTrue(ResumeTarget.isStockHome(STOCK, STOCK)); assertFalse(ResumeTarget.isStockHome("com.android.chrome", STOCK)); assertFalse(ResumeTarget.isStockHome(null, STOCK)); }

    @Test public void theGestureStartsTheRememberedApp() { assertEquals("com.android.chrome", ResumeTarget.pick(true, "com.android.chrome", 1_000, 5_000)); }
    @Test public void switchOffMeansTheStockHome() { assertNull(ResumeTarget.pick(false, "com.android.chrome", 1_000, 5_000)); }
    @Test public void nothingRememberedMeansTheStockHome() { assertNull(ResumeTarget.pick(true, null, 1_000, 5_000)); assertNull(ResumeTarget.pick(true, "", 1_000, 5_000)); }
    @Test public void anOldMemoryIsNotBroughtBack() {
        long t = 1_000_000;
        assertEquals("a.b", ResumeTarget.pick(true, "a.b", t, t + ResumeTarget.MAX_AGE_MS));
        assertNull(ResumeTarget.pick(true, "a.b", t, t + ResumeTarget.MAX_AGE_MS + 1));
    }
    @Test public void aClockThatWentBackwardsOrNoTimestampIsNotTrusted() { assertNull(ResumeTarget.pick(true, "a.b", 10_000, 5_000)); assertNull(ResumeTarget.pick(true, "a.b", 0, 5_000)); }
    @Test public void theSwitchKeyIsStable() { assertEquals("resume_qs_app", ResumeTarget.KEY); }
}
