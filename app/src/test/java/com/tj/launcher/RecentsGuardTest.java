package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.view.accessibility.AccessibilityEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/** AP1: the pure rules of the Recents filter (only apps of the custom drawer may stay in the custom launcher's Recents). */
public class RecentsGuardTest {
    private static Set<String> set(String... s) { return new HashSet<>(Arrays.asList(s)); }

    @Test public void theGuardWorksOnlyInTheCustomLauncherUnlockedWithItsSwitchOn() {
        assertTrue(RecentsGuard.wanted(true, true, false, false, false));
        assertFalse(RecentsGuard.wanted(false, true, false, false, false));   // Quickstep keeps its own Recents
        assertFalse(RecentsGuard.wanted(true, false, false, false, false));   // switched off
        assertFalse(RecentsGuard.wanted(true, true, true, false, false));     // lock screen
        assertFalse(RecentsGuard.wanted(true, true, false, true, false));     // crash breaker tripped
        assertFalse(RecentsGuard.wanted(true, true, false, false, true));     // the return reset is driving Recents itself
    }
    @Test public void hiddenAndQuickstepOnlyAppsAreBlockedButNeverADrawerLabel() {
        Set<String> b = RecentsGuard.blockedLabels(set("Calendar", "Portfolio"), set("Chess", "Contacts"), set("Contacts", "Clock"));
        assertEquals(set("calendar", "portfolio", "chess"), b);   // Contacts was opened in Quickstep but is also a drawer app: it may stay
    }
    @Test public void aLabelThatAHiddenAndAShownAppShareIsNotBlocked() {
        assertTrue(RecentsGuard.blockedLabels(set("Gallery"), set(), set("gallery")).isEmpty());
    }
    @Test public void emptyAndNullInputsGiveNothingToClose() {
        assertTrue(RecentsGuard.blockedLabels(null, null, null).isEmpty());
        assertTrue(RecentsGuard.blockedLabels(set("", "  "), set((String) null), set()).isEmpty());
    }
    @Test public void aCardIsMatchedByTheLabelItStartsWith() {
        Set<String> b = set("calendar", "portfolio");
        assertEquals("calendar", RecentsGuard.matchingLabel("Calendar", b));
        assertEquals("calendar", RecentsGuard.matchingLabel("  CALENDAR  ", b));
        assertEquals("portfolio", RecentsGuard.matchingLabel("Portfolio, My holdings", b));
        assertEquals("portfolio", RecentsGuard.matchingLabel("Portfolio Tracker", b));
    }
    @Test public void aCardThatMatchesNothingKnownIsLeftAlone() {
        Set<String> b = set("calendar");
        assertNull(RecentsGuard.matchingLabel("Wikipedia", b));            // a browser card is titled with the page
        assertNull(RecentsGuard.matchingLabel("Calendars of the world", b)); // not the label followed by a separator
        assertNull(RecentsGuard.matchingLabel("", b));
        assertNull(RecentsGuard.matchingLabel(null, b));
        assertNull(RecentsGuard.matchingLabel("Calendar", null));
        assertNull(RecentsGuard.matchingLabel("Calendar", set()));
    }
    @Test public void theLongestMatchingLabelWins() {
        assertEquals("my files", RecentsGuard.matchingLabel("My Files, Downloads", set("my", "my files")));
    }
    @Test public void onlyWindowChangesOfTheStockLauncherOrSystemUiCanOpenRecents() {
        assertTrue(RecentsGuard.mayOpenRecents(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, "com.android.launcher3", "com.android.launcher3"));
        assertTrue(RecentsGuard.mayOpenRecents(AccessibilityEvent.TYPE_WINDOWS_CHANGED, "com.vendor.home", "com.vendor.home"));
        assertTrue(RecentsGuard.mayOpenRecents(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, "com.android.systemui", null));
        assertFalse(RecentsGuard.mayOpenRecents(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED, "com.android.launcher3", "com.android.launcher3"));   // a redraw is no new screen
        assertFalse(RecentsGuard.mayOpenRecents(AccessibilityEvent.TYPE_VIEW_CLICKED, "com.android.systemui", null));
        assertFalse(RecentsGuard.mayOpenRecents(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, "com.android.calendar", "com.android.launcher3"));    // an app's own window
        assertFalse(RecentsGuard.mayOpenRecents(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, null, "com.android.launcher3"));
    }
}
