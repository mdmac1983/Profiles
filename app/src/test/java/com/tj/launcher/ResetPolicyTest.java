package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import org.junit.Test;

public class ResetPolicyTest {
    @Test public void neverKillsUsTheStockLauncherOrTheKeyboard() {
        assertFalse(ResetPolicy.shouldKill("app.orionmd.profiles", "app.orionmd.profiles", "com.android.launcher3", "com.touchtype.swiftkey"));
        assertFalse(ResetPolicy.shouldKill("com.android.launcher3", "app.orionmd.profiles", "com.android.launcher3", null));
        assertFalse(ResetPolicy.shouldKill("com.touchtype.swiftkey", "app.orionmd.profiles", "com.android.launcher3", "com.touchtype.swiftkey"));
        assertFalse(ResetPolicy.shouldKill("com.android.systemui", "app.orionmd.profiles", "x", null));
    }
    @Test public void killsOrdinaryApps() { assertTrue(ResetPolicy.shouldKill("com.rovio.angrybirds", "app.orionmd.profiles", "com.android.launcher3", null)); }
    @Test public void picksSolitaireByLabelPreferringPrefix() {
        assertEquals(2, ResetPolicy.pickLabel(Arrays.asList("Calendar", "Spider Solitaire pack", "Solitaire"), "solitaire"));
        assertEquals(1, ResetPolicy.pickLabel(Arrays.asList("Calendar", "Klondike Solitaire"), "Solitaire"));
        assertEquals(-1, ResetPolicy.pickLabel(Arrays.asList("Calendar"), "solitaire"));
        assertEquals(-1, ResetPolicy.pickLabel(Arrays.asList("Calendar"), "  "));
    }
}
