package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import org.junit.Test;

public class ResetPolicyTest {
    @Test public void neverKillsUsTheStockLauncherOrTheKeyboard() {
        assertFalse(ResetPolicy.shouldKill("com.tj.launcher", "com.tj.launcher", "com.android.launcher3", "com.touchtype.swiftkey"));
        assertFalse(ResetPolicy.shouldKill("com.android.launcher3", "com.tj.launcher", "com.android.launcher3", null));
        assertFalse(ResetPolicy.shouldKill("com.touchtype.swiftkey", "com.tj.launcher", "com.android.launcher3", "com.touchtype.swiftkey"));
        assertFalse(ResetPolicy.shouldKill("com.android.systemui", "com.tj.launcher", "x", null));
    }
    @Test public void killsOrdinaryApps() { assertTrue(ResetPolicy.shouldKill("com.rovio.angrybirds", "com.tj.launcher", "com.android.launcher3", null)); }
    @Test public void picksSolitaireByLabelPreferringPrefix() {
        assertEquals(2, ResetPolicy.pickLabel(Arrays.asList("Calendar", "Spider Solitaire pack", "Solitaire"), "solitaire"));
        assertEquals(1, ResetPolicy.pickLabel(Arrays.asList("Calendar", "Klondike Solitaire"), "Solitaire"));
        assertEquals(-1, ResetPolicy.pickLabel(Arrays.asList("Calendar"), "solitaire"));
        assertEquals(-1, ResetPolicy.pickLabel(Arrays.asList("Calendar"), "  "));
    }
}
