package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TwoFingerRotationTest {
    /** Rotates a finger pair (radius 100 around 300,300) by the given degrees in steps; positive = clockwise ON SCREEN (y down). */
    private int rotate(TwoFingerRotation r, double fromDeg, double toDeg, double radius, double radiusEnd) {
        int result = TwoFingerRotation.NONE;
        int steps = 20;
        for (int i = 0; i <= steps; i++) {
            double deg = fromDeg + (toDeg - fromDeg) * i / steps;
            double rad = Math.toRadians(deg);
            double rr = radius + (radiusEnd - radius) * i / steps;
            float x0 = (float) (300 - rr * Math.cos(rad)), y0 = (float) (300 - rr * Math.sin(rad));
            float x1 = (float) (300 + rr * Math.cos(rad)), y1 = (float) (300 + rr * Math.sin(rad));
            int d = r.onTwoPointers(x0, y0, x1, y1);
            if (d != TwoFingerRotation.NONE) result = d;
        }
        return result;
    }

    @Test public void clockwiseOnScreenIsDetected() {
        assertEquals(TwoFingerRotation.CLOCKWISE, rotate(new TwoFingerRotation(), 0, 70, 100, 100));
    }

    @Test public void counterClockwiseIsDetected() {
        assertEquals(TwoFingerRotation.COUNTER_CLOCKWISE, rotate(new TwoFingerRotation(), 0, -70, 100, 100));
    }

    @Test public void smallTwistIsIgnored() {
        assertEquals(TwoFingerRotation.NONE, rotate(new TwoFingerRotation(), 0, 12, 100, 100));
    }

    @Test public void pinchIsNotARotation() {
        assertEquals(TwoFingerRotation.NONE, rotate(new TwoFingerRotation(), 0, 60, 100, 260));
    }

    @Test public void firesOnlyOncePerGesture() {
        TwoFingerRotation r = new TwoFingerRotation();
        assertEquals(TwoFingerRotation.CLOCKWISE, rotate(r, 0, 70, 100, 100));
        assertEquals(TwoFingerRotation.NONE, rotate(r, 70, 140, 100, 100));
        r.reset();
        assertEquals(TwoFingerRotation.CLOCKWISE, rotate(r, 0, 70, 100, 100));
    }

    @Test public void wrapAroundAtPiDoesNotBreakAccumulation() {
        // crossing the +-180 degree boundary must not look like a huge opposite rotation
        assertEquals(TwoFingerRotation.CLOCKWISE, rotate(new TwoFingerRotation(), 150, 230, 100, 100));
    }

    @Test public void twentyDegreesNowFires() {
        assertEquals(TwoFingerRotation.CLOCKWISE, rotate(new TwoFingerRotation(), 0, 22, 100, 100));
    }

    @Test public void fingersTooCloseTogetherAreIgnored() {
        assertEquals(TwoFingerRotation.NONE, rotate(new TwoFingerRotation(), 0, 90, 40, 40));
    }
}
