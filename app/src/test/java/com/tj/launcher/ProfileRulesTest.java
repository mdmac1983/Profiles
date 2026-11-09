package com.tj.launcher;

import static org.junit.Assert.*;

import com.tj.launcher.ProfileRules.Profile;

import java.util.Arrays;

import org.junit.Test;

public class ProfileRulesTest {
    @Test public void aPlainPinAlwaysOpensFbop() {
        assertEquals(Profile.FBOP, ProfileRules.unlockTarget(false, null));
        assertEquals("a plain PIN ignores any stale target", Profile.FBOP, ProfileRules.unlockTarget(false, Profile.RENTAL));
        assertEquals(Profile.RENTAL, ProfileRules.unlockTarget(true, Profile.RENTAL));
        assertEquals(Profile.OWNER, ProfileRules.unlockTarget(true, Profile.OWNER));
    }

    @Test public void ownerStaysReachable() {
        assertTrue(ProfileRules.ownerReachable(Arrays.asList(true, false), Arrays.asList(Profile.OWNER, Profile.RENTAL)));
        assertFalse("only Rental is selected", ProfileRules.ownerReachable(Arrays.asList(false, true), Arrays.asList(Profile.OWNER, Profile.RENTAL)));
        assertFalse("nothing selected", ProfileRules.ownerReachable(Arrays.asList(false, false), Arrays.asList(Profile.OWNER, Profile.OWNER)));
        assertEquals("the safety method opens Owner when nothing else does", Profile.OWNER, ProfileRules.effectiveTarget("erase2", Profile.RENTAL, false));
        assertEquals("... and keeps its own choice when Owner is reachable another way", Profile.RENTAL, ProfileRules.effectiveTarget("erase2", Profile.RENTAL, true));
        assertEquals("other methods keep their choice", Profile.RENTAL, ProfileRules.effectiveTarget("volume", Profile.RENTAL, false));
        assertEquals("no choice stored = Owner (the old behaviour)", Profile.OWNER, ProfileRules.effectiveTarget("volume", null, true));
        assertTrue(ProfileRules.forcedOn("erase2", false));
        assertFalse(ProfileRules.forcedOn("erase2", true));
        assertFalse(ProfileRules.forcedOn("volume", false));
    }

    @Test public void rentalHasItsOwnKeysFbopKeepsTheOldOnes() {
        assertEquals("quiet_banners", ProfileRules.scopedKey("quiet_banners", Profile.FBOP));
        assertEquals("quiet_banners@rental", ProfileRules.scopedKey("quiet_banners", Profile.RENTAL));
        assertEquals("Owner has no custom-launcher settings: FBOP's are meant", "quiet_banners", ProfileRules.scopedKey("quiet_banners", Profile.OWNER));
    }

    @Test public void switchingCustomProfilesStartsClean() {
        assertTrue(ProfileRules.switchNeedsReset(Profile.FBOP, Profile.RENTAL));
        assertTrue(ProfileRules.switchNeedsReset(Profile.RENTAL, Profile.FBOP));
        assertFalse(ProfileRules.switchNeedsReset(Profile.FBOP, Profile.FBOP));
        assertFalse("unknown before = FBOP", ProfileRules.switchNeedsReset(null, Profile.FBOP));
    }

    @Test public void parsingAndCycling() {
        assertEquals(Profile.RENTAL, ProfileRules.parse("rental", Profile.OWNER));
        assertEquals(Profile.OWNER, ProfileRules.parse("junk", Profile.OWNER));
        assertEquals(Profile.OWNER, ProfileRules.parse(null, Profile.OWNER));
        assertEquals(Profile.FBOP, ProfileRules.next(Profile.OWNER));
        assertEquals(Profile.RENTAL, ProfileRules.next(Profile.FBOP));
        assertEquals(Profile.OWNER, ProfileRules.next(Profile.RENTAL));
        assertEquals(Profile.FBOP, ProfileRules.customOrDefault(Profile.OWNER));
    }
}
