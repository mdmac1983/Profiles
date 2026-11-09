package com.tj.launcher;

/**
 * AH3 (v0.7.6): per-device corrections for the LOCK-screen wallpaper patch. The generic wallpaper model (cover-scaled, centred, dimmed like the AOSP keyguard) is exact on the Android 10 emulator, but on Tj's
 * MediaTek tablet ({@code Build.DEVICE} "tb8168p1_bsp") the patch showed the wrong piece of the wallpaper and too green a tone (Tj, 2026-11-06: "I think it is simply the wrong piece of wallpaper copied for the patch").
 * Side-by-side photos of the stock tablet and ours (same camera, same light) were compared: our patch content came from about 90 px (range 60-130, three independent true-wallpaper sources, one sharp minimum)
 * FURTHER LEFT than the truth on the 600 px wide screen, and after that shift it is still a few percent too dark (red most). The emulator and every other device keep the generic model (both values 0).
 * Pure, JVM-tested. The values are only DEFAULTS: Tj's own nudges in Profiles settings (patch left / right, patch brightness) add on top.
 */
public final class PatchProfile {
    private PatchProfile() {}

    /** The MediaTek board of Tj's tablet. The emulator reports something else, so the emulator tests keep exercising the generic model. */
    static final String TJ_TABLET_DEVICE_PREFIX = "tb8168";

    private static boolean tjTablet(String device) { return device != null && device.toLowerCase(java.util.Locale.ROOT).startsWith(TJ_TABLET_DEVICE_PREFIX); }

    /** The horizontal correction of the model's wallpaper as a fraction of the display width (negative = the picture moves LEFT, i.e. content from further right shows). -0.15 = -90 px at 600 px. */
    public static float lockShiftPerWidth(String device) { return tjTablet(device) ? -0.15f : 0f; }

    /** Extra brightness of the model patch in percent points on top of Tj's own nudge: after the shift the patch measured 3-25 % too dark (red most); only a mild step because the estimate is uncertain. */
    public static int lockDimPct(String device) { return tjTablet(device) ? 8 : 0; }

    /** The shift in pixels for a display {@code widthPx} wide. */
    public static int lockShiftPx(String device, int widthPx) { return Math.round(lockShiftPerWidth(device) * widthPx); }
}
