package com.tj.launcher;

/** Pure decision logic for the power-menu helper (JVM-testable). */
public final class PowerMenuRule {
    public static final int NOTHING = 0, COVER_SCREENSHOT = 1, CLOSE_MENU = 2;

    private PowerMenuRule() {}

    /**
     * AD7a (Tj, 2026-11-06: "when I lock the screen from quickstep, the power menu overlay doesn't work. I need it to always work any time I'm on the lock screen"): the world in which the power menu is covered.
     * The custom launcher, AND every lock screen: a plain PIN always ends a Quickstep session in the custom launcher (AC1), so a lock screen is custom territory even while the mode flag still says Quickstep (it flips
     * only at the unlock, or at the lock after a successful shell-helper clean-up, which this tablet never gets). Unlocked Quickstep stays untouched.
     */
    public static boolean coverWorld(boolean customMode, boolean keyguardLocked) { return customMode || keyguardLocked; }

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
