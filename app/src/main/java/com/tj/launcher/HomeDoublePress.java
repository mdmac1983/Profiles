package com.tj.launcher;

/** Pure rule (JVM-testable): two Home presses within WINDOW_MS (0.9 s) while in Quickstep mode mean "back to the custom launcher". */
public final class HomeDoublePress {
    public static final long WINDOW_MS = 900;
    private HomeDoublePress() {}

    public static boolean isDouble(long lastPressMs, long nowMs) {
        return lastPressMs > 0 && nowMs >= lastPressMs && nowMs - lastPressMs <= WINDOW_MS;
    }
}
