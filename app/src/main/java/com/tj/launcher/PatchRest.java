package com.tj.launcher;

/**
 * AE3 / AD7b (Tj, 2026-11-06: "any time I'm on the lock screen, there should be an accessibility overlay of the wallpaper patch exactly where the screenshot button shows up, so that when I hold the side key
 * to open the power menu, the screenshot button is hidden without delay"). Until now the lock-screen patch window stayed invisible and was shown only when the power menu had been DETECTED (a short delay in which
 * the stock Screenshot entry could be seen). With the switch on, the patch rests visible over the Screenshot slot the whole time the lock screen is up, except while the PIN pad is showing. Pure rules, JVM-tested;
 * {@link TjAccessibilityService} draws it.
 */
public final class PatchRest {
    /** Launcher control switch, default ON (since v0.8.0; OFF = the patch is only drawn while the menu is open). */
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

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    // AH3 (Tj, 2026-11-06, with side-by-side photos: our card is the right size and sits flush, but a flat bluish block beside / below it shows where the wallpaper patch does not match the live lock screen).
    // The old patch covered the WHOLE menu rectangle plus a 12 px ring on every side with hard edges. The new one covers ONLY the Screenshot slot (below our card), reaches just past the stock panel's edge and
    // fades out softly beyond it, so any residual mismatch is a gentle gradient instead of a visible frame. Pure geometry + mask, JVM-tested; {@link TjAccessibilityService} draws it.
    // ------------------------------------------------------------------------------------------------------------------------------------------------

    /** dp: how far the opaque core reaches beyond the stock panel's edge (covers its antialiased edge), the soft fringe outside that, and how far the OPEN patch reaches up under our card (hidden by it). */
    public static final float EDGE_DP = 2f, FEATHER_DP = 8f, OVERLAP_DP = 3f;

    /**
     * The slot window {left, top, right, bottom} in display px: the learned panel's left / right / bottom plus the margin (edge + feather), starting one feather width ABOVE the card's bottom edge (room for the soft
     * top edge of the RESTING patch; while the menu is open that strip is transparent and the card covers the rest). Clamped to the display.
     */
    public static int[] slotWindow(int panelLeft, int cardBottom, int panelRight, int panelBottom, int edge, int feather, int dispW, int dispH) {
        int m = edge + feather;
        return new int[] { Math.max(0, panelLeft - m), Math.max(0, cardBottom - feather), Math.min(dispW, panelRight + m), Math.min(dispH, panelBottom + m) };
    }

    /**
     * 0..255: how much of the patch shows at (x, y) of its window (window coordinates). The core [cl, cr) x [ct, cb) is fully opaque; fl / ft / fr / fb are the widths of the soft fringe OUTSIDE it on each side
     * (0 = a hard edge: nothing shows beyond it). The two directions multiply, so a corner fades in both; the ramp is a smoothstep so there is no visible slope break.
     */
    public static int alphaAt(int x, int y, int cl, int ct, int cr, int cb, int fl, int ft, int fr, int fb) {
        float ax = 1f, ay = 1f;
        if (x < cl) ax = ramp(cl - x, fl); else if (x >= cr) ax = ramp(x - cr + 1, fr);
        if (y < ct) ay = ramp(ct - y, ft); else if (y >= cb) ay = ramp(y - cb + 1, fb);
        return Math.round(255f * ax * ay);
    }

    private static float ramp(int dist, int width) {
        if (width <= 0) return 0f;
        float t = 1f - dist / (float) (width + 1);
        if (t <= 0f) return 0f;
        return t * t * (3f - 2f * t);
    }

    /**
     * Are two patch windows the same for all practical purposes? The slot window follows the card's bottom edge, which is measured on every menu scan and can wobble by a pixel; rebuilding the patch for that
     * would itself flicker. Each side may differ by up to {@code tol} px.
     */
    public static boolean sameWindow(int[] a, int[] b, int tol) {
        if (a == null || b == null || a.length != 4 || b.length != 4) return false;
        for (int i = 0; i < 4; i++) if (Math.abs(a[i] - b[i]) > tol) return false;
        return true;
    }

    /** The mask (w * h bytes, row-major, 0..255 as unsigned) for {@link #alphaAt}. */
    public static byte[] mask(int w, int h, int cl, int ct, int cr, int cb, int fl, int ft, int fr, int fb) {
        byte[] m = new byte[Math.max(0, w) * Math.max(0, h)];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) m[y * w + x] = (byte) alphaAt(x, y, cl, ct, cr, cb, fl, ft, fr, fb);
        return m;
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
