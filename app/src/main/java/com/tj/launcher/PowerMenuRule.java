package com.tj.launcher;

/** Pure decision logic for the power-menu helper (JVM-testable). */
public final class PowerMenuRule {
    public static final int NOTHING = 0, COVER_SCREENSHOT = 1, CLOSE_MENU = 2;

    private PowerMenuRule() {}

    /**
     * @param customMode      our launcher is the active one (Quickstep mode never touches the power menu)
     * @param keyguardLocked  the device is on the lock screen: never act there (lock-screen safety rule)
     * @param menuOpen        the window shows both "Power off" and "Screenshot"
     * @param screenshotFound we located the Screenshot entry's bounds
     */
    public static int decide(String powerMode, boolean customMode, boolean keyguardLocked, boolean menuOpen, boolean screenshotFound) {
        if (!customMode || keyguardLocked || !menuOpen) return NOTHING;
        if (Features.POWER_BLOCK.equals(powerMode)) return CLOSE_MENU;
        if (Features.POWER_HIDE_SCREENSHOT.equals(powerMode)) return screenshotFound ? COVER_SCREENSHOT : NOTHING;
        return NOTHING;
    }
}
