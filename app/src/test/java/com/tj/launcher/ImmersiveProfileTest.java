package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/** AA1: the pure rules of the status-bar switch. The real effect (bar hidden, shade still reachable by a second swipe, lock screen untouched) is measured by emulator sections rs and im. */
public class ImmersiveProfileTest {
    @Test public void ourEntryIsAddedToNothing() { assertEquals("immersive.status=*", ImmersiveProfile.merge(null)); assertEquals("immersive.status=*", ImmersiveProfile.merge("")); }
    @Test public void otherEntriesAreKeptExactly() {
        assertEquals("immersive.preconfirms=*:immersive.status=*", ImmersiveProfile.merge("immersive.preconfirms=*"));
        assertEquals("immersive.navigation=com.x.y:immersive.status=*", ImmersiveProfile.merge("immersive.navigation=com.x.y"));
    }
    @Test public void anExistingStatusOrFullEntryIsReplaced() {
        assertEquals("immersive.status=*", ImmersiveProfile.merge("immersive.status=com.a.b"));
        assertEquals("immersive.preconfirms=*:immersive.status=*", ImmersiveProfile.merge("immersive.full=*:immersive.preconfirms=*"));
    }
    @Test public void mergingTwiceChangesNothing() { String once = ImmersiveProfile.merge("immersive.preconfirms=*"); assertEquals(once, ImmersiveProfile.merge(once)); }
    @Test public void switchedOffMeansLeaveTheSystemAlone() {
        assertEquals(ImmersiveProfile.NOTHING, ImmersiveProfile.valueFor(false, Mode.CUSTOM, null));
        assertEquals(ImmersiveProfile.NOTHING, ImmersiveProfile.valueFor(false, Mode.QUICKSTEP, "x"));
    }
    @Test public void customHidesQuickstepGetsTheOriginalBack() {
        assertEquals("immersive.status=*", ImmersiveProfile.valueFor(true, Mode.CUSTOM, null));
        assertEquals("immersive.preconfirms=*:immersive.status=*", ImmersiveProfile.valueFor(true, Mode.CUSTOM, "immersive.preconfirms=*"));
        assertNull(ImmersiveProfile.valueFor(true, Mode.QUICKSTEP, null));                       // there was nothing before: the setting is removed again
        assertEquals("immersive.preconfirms=*", ImmersiveProfile.valueFor(true, Mode.QUICKSTEP, "immersive.preconfirms=*"));
    }
    @Test public void aSwitchedOffLauncherOnlyUndoesWhatItOwns() {
        assertEquals(true, ImmersiveProfile.mayUndoWhenOff(true, true));      // we took a snapshot and the bar is hidden: put the original back
        assertEquals(false, ImmersiveProfile.mayUndoWhenOff(false, true));    // the same value, but nobody ever snapshotted it: it is not ours, leave it
        assertEquals(false, ImmersiveProfile.mayUndoWhenOff(true, false));    // nothing hidden right now: nothing to undo
        assertEquals(false, ImmersiveProfile.mayUndoWhenOff(false, false));
    }
    @Test public void theSettingNameAndSwitchKeyAreStable() { assertEquals("policy_control", ImmersiveProfile.SETTING); assertEquals("profile_hide_status_bar", ImmersiveProfile.KEY); }
}
