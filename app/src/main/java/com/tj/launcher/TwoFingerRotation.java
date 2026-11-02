package com.tj.launcher;

/**
 * Detects a two-finger rotation from successive (x0,y0,x1,y1) samples. Pure Java (JVM-testable).
 * Screen coordinates (y down): an INCREASING angle of atan2(dy, dx) is a clockwise turn on screen.
 * Fires once per gesture after >= 18 degrees accumulated, and only if the finger distance stays within +-50% of its
 * starting value (so a pinch/zoom is not a rotation). Call reset() when fewer than two fingers are down.
 */
public final class TwoFingerRotation {
    public static final int NONE = 0, CLOCKWISE = 1, COUNTER_CLOCKWISE = 2;
    private static final double THRESHOLD_RAD = Math.toRadians(18);
    private static final double MAX_DISTANCE_CHANGE = 0.5;
    /** Two fingers closer than this (px) are treated as one touching finger pair/noise, not a twist. */
    private static final double MIN_DISTANCE_PX = 80;

    private boolean active, fired;
    private double lastAngle, accumulated, startDistance;

    public void reset() { active = false; fired = false; accumulated = 0; }

    public int onTwoPointers(float x0, float y0, float x1, float y1) {
        double dx = x1 - x0, dy = y1 - y0;
        double angle = Math.atan2(dy, dx), dist = Math.hypot(dx, dy);
        if (dist < MIN_DISTANCE_PX) { reset(); return NONE; }
        if (!active) {
            active = true; fired = false; accumulated = 0; lastAngle = angle; startDistance = Math.max(dist, 1.0);
            return NONE;
        }
        double delta = angle - lastAngle;
        while (delta > Math.PI) delta -= 2 * Math.PI;
        while (delta <= -Math.PI) delta += 2 * Math.PI;
        lastAngle = angle;
        if (Math.abs(dist - startDistance) / startDistance > MAX_DISTANCE_CHANGE) { accumulated = 0; return NONE; }
        accumulated += delta;
        if (fired) return NONE;
        if (accumulated >= THRESHOLD_RAD) { fired = true; return CLOCKWISE; }
        if (accumulated <= -THRESHOLD_RAD) { fired = true; return COUNTER_CLOCKWISE; }
        return NONE;
    }
}
