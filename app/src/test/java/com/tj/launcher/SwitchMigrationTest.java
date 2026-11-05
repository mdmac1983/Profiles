package com.tj.launcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** v0.7.3: the one-time undo of the old breaker reset's "false" values. */
public class SwitchMigrationTest {
    @Test public void allThreeStoredAsOffIsTheOldBreakerReset() { assertTrue(SwitchMigration.looksLikeTheBreakerWipe(false, false, false)); }
    @Test public void oneSwitchTjSetHimselfNeverMatches() {
        assertFalse(SwitchMigration.looksLikeTheBreakerWipe(true, false, false));
        assertFalse(SwitchMigration.looksLikeTheBreakerWipe(false, true, false));
        assertFalse(SwitchMigration.looksLikeTheBreakerWipe(false, false, true));
    }
    @Test public void nothingStoredIsNotAWipe() { assertFalse(SwitchMigration.looksLikeTheBreakerWipe(null, null, null)); }
    @Test public void aPartialStoreIsNotAWipe() { assertFalse(SwitchMigration.looksLikeTheBreakerWipe(false, null, false)); }
}
