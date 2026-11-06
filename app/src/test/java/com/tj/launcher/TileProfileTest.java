package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TileProfileTest {
    @Test public void keepsAValidList() { assertEquals("wifi,bt,rotation", TileProfile.sanitize("wifi,bt,rotation", "x")); }
    @Test public void trimsAndDropsEmptyEntries() { assertEquals("wifi,rotation", TileProfile.sanitize(" wifi , ,rotation,", "x")); }
    @Test public void dropsOddCharacters() { assertEquals("wifi,bt", TileProfile.sanitize("wi;fi,b'''t", "x").replace("wifi", "wifi")); }
    @Test public void emptyFallsBack() { assertEquals("rotation", TileProfile.sanitize("", "rotation")); assertEquals("rotation", TileProfile.sanitize(null, "rotation")); assertEquals("rotation", TileProfile.sanitize(",,;;", "rotation")); }
    @Test public void customTileSpecsSurvive() { assertEquals("custom(com.a.b/.C)", TileProfile.sanitize("custom(com.a.b/.C)", "x")); }
    @Test public void leavingQuickstepRemembersWhatWasArranged() {
        assertEquals(true, TileProfile.shouldRememberQuickstepList("quickstep", "custom", "wifi,bt,rotation,flashlight", "rotation"));
    }
    @Test public void nothingIsRememberedWhenNotLeavingQuickstep() {
        assertEquals(false, TileProfile.shouldRememberQuickstepList("custom", "quickstep", "wifi,bt,rotation", "rotation"));
        assertEquals(false, TileProfile.shouldRememberQuickstepList("quickstep", "quickstep", "wifi,bt,rotation", "rotation"));
        assertEquals(false, TileProfile.shouldRememberQuickstepList("", "custom", "wifi,bt,rotation", "rotation"));
    }
    @Test public void anEmptyOrCustomLookingListIsNotRemembered() {
        assertEquals(false, TileProfile.shouldRememberQuickstepList("quickstep", "custom", "", "rotation"));
        assertEquals(false, TileProfile.shouldRememberQuickstepList("quickstep", "custom", null, "rotation"));
        assertEquals(false, TileProfile.shouldRememberQuickstepList("quickstep", "custom", "rotation", "rotation"));
    }

    // ---- AW2: the lock screen always shows the custom (rotation-only) list, from any launcher
    @Test public void theKeyguardForcesTheCustomListWhateverTheModeSays() {
        assertEquals("custom", TileProfile.effectiveMode("quickstep", true));
        assertEquals("custom", TileProfile.effectiveMode("custom", true));
        assertEquals("custom", TileProfile.effectiveMode(null, true));
    }
    @Test public void withoutAKeyguardTheModesOwnListComesBack() {
        assertEquals("quickstep", TileProfile.effectiveMode("quickstep", false));
        assertEquals("custom", TileProfile.effectiveMode("custom", false));
        assertEquals("custom", TileProfile.effectiveMode("nonsense", false));
    }
    @Test public void locking_FromQuickstepRemembersTjsArrangementAndTheGestureUnlockDoesNotOverwriteIt() {
        // lock from Quickstep: applied mode quickstep -> effective custom: what Tj arranged with the pencil is remembered as his Quickstep list
        assertEquals(true, TileProfile.shouldRememberQuickstepList("quickstep", TileProfile.effectiveMode("quickstep", true), "wifi,bt,rotation,battery", "rotation"));
        // the gesture unlock: applied mode is now custom (the lock list), the new effective mode is quickstep: nothing is remembered, the stored list is written back
        assertEquals(false, TileProfile.shouldRememberQuickstepList("custom", TileProfile.effectiveMode("quickstep", false), "rotation", "rotation"));
    }
    @Test public void aSecondLockMomentDoesNotRememberTheLockListAsTjsList() {
        // screen off + screen on while locked: applied mode is already custom, so the lock list ("rotation") is never mistaken for Quickstep's
        assertEquals(false, TileProfile.shouldRememberQuickstepList("custom", TileProfile.effectiveMode("quickstep", true), "rotation", "rotation"));
    }
}
