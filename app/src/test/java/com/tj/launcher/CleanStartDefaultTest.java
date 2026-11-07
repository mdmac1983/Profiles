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

    @Test public void theBootFlagExpiresAfterADay() {
        int now = 2_000_000_000;
        assertTrue(ReturnReset.bootCleanValid(true, now - 60, now));
        assertTrue(ReturnReset.bootCleanValid(true, now - ReturnReset.BOOT_CLEAN_MAX_AGE_S, now));
        assertFalse("older than a day: a flag that outlived its boot must not clear a later session", ReturnReset.bootCleanValid(true, now - ReturnReset.BOOT_CLEAN_MAX_AGE_S - 1, now));
        assertFalse("not set", ReturnReset.bootCleanValid(false, now - 60, now));
        assertFalse("no time recorded", ReturnReset.bootCleanValid(true, 0, now));
        assertFalse("clock went backwards", ReturnReset.bootCleanValid(true, now + 100, now));
    }

    @Test public void theKeysAreStable() {
        // the emulator section `cs` writes these names into tj_features.xml; a rename would silently turn the tests into no-ops
        assertEquals("clean_start", ReturnReset.CLEAN_START_KEY);
        assertEquals("clean_pending_boot", ReturnReset.BOOT_CLEAN_KEY);
        assertEquals("recents_sweep", RecentsSweep.KEY);
    }
}
