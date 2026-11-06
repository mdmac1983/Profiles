package com.tj.launcher;

/**
 * AE3 / AD7b (Tj, 2026-11-06: "any time I'm on the lock screen, there should be an accessibility overlay of the wallpaper patch exactly where the screenshot button shows up, so that when I hold the side key
 * to open the power menu, the screenshot button is hidden without delay"). Until now the lock-screen patch window stayed invisible and was shown only when the power menu had been DETECTED (a short delay in which
 * the stock Screenshot entry could be seen). With the switch on, the patch rests visible over the Screenshot slot the whole time the lock screen is up, except while the PIN pad is showing. Pure rules, JVM-tested;
 * {@link TjAccessibilityService} draws it.
 */
public final class PatchRest {
    /** Launcher control switch, default OFF. */
    public static final String KEY = "lock_patch_always";

    private PatchRest() {}

    /**
     * Is the patch window visible?
     * @param menuOpen   our cover is up for an open power menu (the full, dimmed patch)
     * @param always     the Control switch (and the lock-screen patch itself) is on
     * @param lockMode   this is the lock-screen patch (the home-screen patch never rests)
     * @param pinPadUp   the PIN / password / pattern pad is showing: the resting patch must never sit over it
     */
    public static boolean visible(boolean menuOpen, boolean always, boolean lockMode, boolean pinPadUp) {
        if (!lockMode) return menuOpen;      // home-screen patch: only while the menu is open, as before
        if (menuOpen) return true;           // an open menu always gets its patch (a power menu can also open over the PIN pad)
        return always && !pinPadUp;
    }

    /**
     * Where the RESTING patch starts to paint, in the patch window's own coordinates: the top of the Screenshot slot = the bottom of our two-button card. The part above it (where the card will appear) is left
     * alone so the resting patch covers as little of the lock screen (clock, notifications) as possible. A card bottom above the window paints everything; below the window paints nothing.
     */
    public static int restTop(int windowTop, int cardBottom, int windowBottom) {
        int h = Math.max(0, windowBottom - windowTop);
        int t = cardBottom - windowTop;
        return Math.max(0, Math.min(t, h));
    }

    /**
     * AH1: brightness the patch keeps while the stock power dialog is open. The dialog dims everything behind it ({@code stock} = the share of brightness that is kept, 0.79 measured on the emulator); the patch is drawn
     * ABOVE that scrim, so it must be dimmed the same or it shows as a lighter block (Tj: the patch "obviously doesn't blend in on the home screen"). {@code nudgePct} is Tj's own correction for his tablet.
     */
    public static float openScrim(float stock, int nudgePct) {
        float k = stock * (1f + nudgePct / 100f);
        return Math.max(0.3f, Math.min(1f, k));
    }
}
