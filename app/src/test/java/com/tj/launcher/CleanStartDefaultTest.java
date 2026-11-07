package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** BE2 / BE3: the shipped defaults of the clean-start and sweep switches, and the key names the scenario and the report rely on. */
public class CleanStartDefaultTest {
    @Test public void theShippedAppStartsCleanAndTheDebugBuildKeepsTheOlderBehaviour() {
        assertTrue("release: clean start ON", ReturnReset.cleanStartDefault(false));
        assertFalse("debug (emulator): the Solitaire behaviour the older sections were written for", ReturnReset.cleanStartDefault(true));
    }

    @Test public void theSweepSwitchFollowsTheSameRule() {
        assertTrue(RecentsSweep.enabledDefault(false));
        assertFalse(RecentsSweep.enabledDefault(true));
    }

    @Test public void theKeysAreStable() {
        // the emulator section `cs` writes these names into tj_features.xml; a rename would silently turn the tests into no-ops
        assertEquals("clean_start", ReturnReset.CLEAN_START_KEY);
        assertEquals("clean_pending_boot", ReturnReset.BOOT_CLEAN_KEY);
        assertEquals("recents_sweep", RecentsSweep.KEY);
    }
}
