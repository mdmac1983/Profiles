package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MotionDetectorTest {
    /** Feeds 20 ms samples of gravity (0,0,9.81) with spikes of the given height (m/s2) lasting ~40 ms at the given times; returns the first thing recognised (NONE if nothing). */
    private static MotionDetector.Kind run(long endMs, float[] spikeAt, float height) {
        MotionDetector d = new MotionDetector();
        MotionDetector.Kind first = MotionDetector.Kind.NONE;
        for (long t = 0; t <= endMs; t += 20) {
            float z = 9.81f;
            for (float s : spikeAt) if (t >= s && t < s + 40) z += height;
            MotionDetector.Kind k = d.onSample(0, 0, z, t);
            if (k != MotionDetector.Kind.NONE && first == MotionDetector.Kind.NONE) first = k;
        }
        return first;
    }

    @Test public void restingTabletIsNothing() { assertEquals(MotionDetector.Kind.NONE, run(5000, new float[] {}, 0)); }
    @Test public void threeFirmJoltsAreAShake() { assertEquals(MotionDetector.Kind.SHAKE, run(3000, new float[] {1000, 1300, 1600}, 14)); }
    @Test public void twoFirmJoltsAreNotAShake() { assertEquals(MotionDetector.Kind.NONE, run(3000, new float[] {1000, 1300}, 14)); }
    @Test public void tooSlowJoltsAreNotAShake() { assertEquals(MotionDetector.Kind.NONE, run(6000, new float[] {1000, 2200, 3400}, 14)); }
    @Test public void twoSmallSharpJoltsAfterQuietAreAKnock() { assertEquals(MotionDetector.Kind.KNOCK, run(3000, new float[] {1000, 1500}, 5)); }
    @Test public void aSingleKnockIsNothing() { assertEquals(MotionDetector.Kind.NONE, run(3000, new float[] {1000}, 5)); }
    @Test public void knocksFarApartAreNothing() { assertEquals(MotionDetector.Kind.NONE, run(4000, new float[] {1000, 2500}, 5)); }
    @Test public void fingerTapsOnAHeldTabletAreIgnored() { assertEquals(MotionDetector.Kind.NONE, run(3000, new float[] {1000, 1500}, 1.5f)); }
    @Test public void walkingJoltsEveryHalfSecondNeverMakeAKnock() {
        float[] walk = new float[10];
        for (int i = 0; i < walk.length; i++) walk[i] = 1000 + i * 500;
        assertEquals(MotionDetector.Kind.NONE, run(7000, walk, 4.5f));
    }
    @Test public void aKnockAfterWalkingStopsStillWorks() {
        assertEquals(MotionDetector.Kind.KNOCK, run(9000, new float[] {1000, 1500, 2000, 6000, 6500}, 4.5f));
    }
}
