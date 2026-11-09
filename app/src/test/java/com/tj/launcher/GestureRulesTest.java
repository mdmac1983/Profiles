package com.tj.launcher;

import static org.junit.Assert.*;

import com.tj.launcher.GestureRules.Action;
import com.tj.launcher.GestureRules.Gesture;

import org.junit.Test;

public class GestureRulesTest {
    private static final int W = 600;

    @Test public void swipeDownAndTwoFingers() {
        assertEquals(Gesture.SWIPE_DOWN, GestureRules.classify(10, 200, 250, 1, W, 1000, 300, 600, 0, 0, 0));
        assertNull("a sideways page swipe is not a swipe down", GestureRules.classify(250, 40, 250, 1, W, 1000, 300, 600, 0, 0, 0));
        assertNull("a swipe UP stays Launcher3's drawer gesture", GestureRules.classify(0, -300, 250, 1, W, 1000, 300, 600, 0, 0, 0));
        assertNull("too slow = a drag, not a swipe", GestureRules.classify(0, 300, 1500, 1, W, 1000, 300, 600, 0, 0, 0));
        assertEquals(Gesture.TWO_FINGER_DOWN, GestureRules.classify(10, 200, 300, 2, W, 1000, 300, 600, 0, 0, 0));
    }

    @Test public void doubleTapNeedsTwoQuickTapsInThePlace() {
        assertNull("one tap", GestureRules.classify(2, 2, 100, 1, W, 1000, 300, 600, 0, 0, 0));
        assertEquals(Gesture.DOUBLE_TAP, GestureRules.classify(2, 2, 100, 1, W, 1300, 302, 601, 1000, 300, 600));
        assertNull("too late", GestureRules.classify(2, 2, 100, 1, W, 1500, 300, 600, 1000, 300, 600));
        assertNull("somewhere else", GestureRules.classify(2, 2, 100, 1, W, 1300, 100, 900, 1000, 300, 600));
        assertNull("a long press is not a tap", GestureRules.classify(2, 2, 600, 1, W, 1300, 300, 600, 1000, 300, 600));
    }

    @Test public void defaultsAsSuggestedInReleaseAndNothingInDebug() {
        assertEquals(Action.NOTIFICATIONS, GestureRules.defaultFor(Gesture.SWIPE_DOWN, false));
        assertEquals(Action.LOCK, GestureRules.defaultFor(Gesture.DOUBLE_TAP, false));
        assertEquals(Action.NOTHING, GestureRules.defaultFor(Gesture.TWO_FINGER_DOWN, false));
        for (Gesture g : Gesture.values()) assertEquals(Action.NOTHING, GestureRules.defaultFor(g, true));
    }

    @Test public void noActionOpensOwner() {
        for (Action a : Action.values()) assertFalse(a.id.contains("owner") || a.id.contains("quickstep"));
        assertEquals(Action.NOTIFICATIONS, GestureRules.next(Action.NOTHING));
        assertEquals(Action.NOTHING, GestureRules.next(Action.DRAWER));
        assertEquals(Action.LOCK, GestureRules.parse("lock", Action.NOTHING));
        assertEquals(Action.NOTHING, GestureRules.parse("x", Action.NOTHING));
    }
}
