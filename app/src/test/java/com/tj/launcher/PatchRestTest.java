package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** AE3 / AD7b: the lock-screen patch rests visible over the Screenshot slot, but never over the PIN pad, and never at home. */
public class PatchRestTest {
    @Test public void theSwitchKeyIsStable() { assertEquals("lock_patch_always", PatchRest.KEY); }

    @Test public void switchOffKeepsTheOldBehaviourInvisibleUntilTheMenuOpens() {
        assertFalse(PatchRest.visible(false, false, true, false));
        assertTrue(PatchRest.visible(true, false, true, false));
    }
    @Test public void switchOnRestsVisibleOnTheLockScreen() { assertTrue(PatchRest.visible(false, true, true, false)); }
    @Test public void neverRestsOverThePinPad() {
        assertFalse(PatchRest.visible(false, true, true, true));
        assertTrue("an open power menu still gets its patch over the pad", PatchRest.visible(true, true, true, true));
    }
    @Test public void theHomeScreenPatchNeverRests() {
        assertFalse(PatchRest.visible(false, true, false, false));
        assertTrue(PatchRest.visible(true, true, false, false));
    }

    @Test public void theRestingPatchPaintsOnlyBelowTheCard() {
        assertEquals(150, PatchRest.restTop(100, 250, 400));
        assertEquals(0, PatchRest.restTop(100, 100, 400));
    }
    @Test public void theOpenMenuScrimIsTheStockValueUntilTjNudgesIt() {
        assertEquals(0.79f, PatchRest.openScrim(0.79f, 0), 0.0001f);
        assertEquals(0.79f * 0.9f, PatchRest.openScrim(0.79f, -10), 0.0001f);
        assertEquals(1f, PatchRest.openScrim(0.79f, 40), 0.0001f);     // never brighter than the screen itself
        assertEquals(0.3f, PatchRest.openScrim(0.79f, -90), 0.0001f);  // never black
    }
    @Test public void aCardOutsideTheWindowIsClamped() {
        assertEquals(0, PatchRest.restTop(100, 40, 400));        // card bottom above the window: paint it all
        assertEquals(300, PatchRest.restTop(100, 900, 400));     // card bottom below the window: paint nothing
        assertEquals(0, PatchRest.restTop(400, 500, 300));       // an empty / inverted window never goes negative
    }
}
