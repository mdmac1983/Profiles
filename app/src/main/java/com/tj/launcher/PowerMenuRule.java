package com.tj.launcher;

/** Pure decision logic for the power-menu helper (JVM-testable). */
public final class PowerMenuRule {
    public static final int NOTHING = 0, COVER_SCREENSHOT = 1, CLOSE_MENU = 2;

    private PowerMenuRule() {}

    /**
     * @param customMode      our launcher is the active one (Quickstep mode never touches the power menu)
     * @param keyguardLocked  the device is on the lock screen: the replacement card works there too (Tj, 2026-11-03), blocking does not
     * @param menuOpen        the window shows both "Power off" and "Screenshot"
     * @param screenshotFound we located the Screenshot entry's bounds
     */
    public static int decide(String powerMode, boolean customMode, boolean keyguardLocked, boolean menuOpen, boolean screenshotFound) {
        if (!customMode || !menuOpen) return NOTHING;
        if (Features.POWER_BLOCK.equals(powerMode)) return keyguardLocked ? NOTHING : CLOSE_MENU;   // closing the menu stays off the lock screen
        if (Features.isCover(powerMode)) return screenshotFound ? COVER_SCREENSHOT : NOTHING;
        return NOTHING;
    }
}
