package com.tj.launcher;

import android.content.Context;

/**
 * X1 (Tj, 2026-11-03, L10 lock-screen rule relaxed by Tj for THIS feature only): a number at the bottom of the lock screen that disappears when the PIN pad is pulled up.
 * The number is drawn by the accessibility helper as a non-touchable, non-focusable overlay (it can never take a touch or a key, so it cannot block the PIN pad).
 * Look: white Roboto digits, 14 characters, centred. v0.3.3 measured it from an old overlap photo; v0.7.6 re-measured it from side-by-side photos of the stock lock screen and ours (see the constants).
 */
public final class LockNumber {
    public static final String KEY = "lock_number";             // switch, default ON ("Restore everything" turns it OFF)
    public static final String TEXT_KEY = "lock_number_text";
    public static final String DEFAULT_TEXT = "71125011907547";
    /**
     * Text size (em) as a fraction of the screen width. v0.7.6 (Tj, 2026-11-06: "slightly smaller than the correct size and font"): MEASURED on Tj's side-by-side photos (reference tablet and ours in the SAME
     * photo, each rectified to 600 x 1024): the stock number is 223.3 px wide (ours 195.5 at 0.0429 Light), digit pitch 16.0 px, digit height 20.5 px; Roboto Medium at em 28.2 px reproduces that: 0.0470 x 600.
     * The v0.3.3 value (0.0429) was fitted to an overlap photo (7.png, two numbers on top of each other), which can only show the LARGER of the two, so it came out too small.
     */
    public static final float EM_PER_WIDTH = 0.0470f;
    /** Baseline as a fraction of the real display height: the stock number's baseline measures 0.9164 / 0.9167 (938 px) in two photos; ours was 0.9033 (925 px), i.e. 13 px too high. */
    public static final float BASELINE_PER_HEIGHT = 0.916f;
    /**
     * Font weight. v0.7.6: the stock strokes are heavier than Regular: after removing the camera blur (calibrated on our own Light number) the vertical stem of a '1' is about 3.5-4.1 px at em 28, against Roboto
     * Regular 2.6, Medium 3.3, Bold 3.9 px: Medium about 55-60 %, Bold 30 %, Regular 10 %. (The v0.3.3 "Light" came from an alpha-mass comparison with the old overlap photo.) Tj can nudge it in Launcher control.
     */
    public static final int WEIGHT = 500;
    /** Brightness. The stock number peaks at about the same value as ours (camera saturated: not resolved better than +/- 25 %); heavier strokes already look brighter, so only a small step up from 186. */
    public static final int ALPHA = 190;
    /** Horizontal nudge as a fraction of the screen width: the stock number's centre measures 299.6 +/- 0.7 px of 600 (the text is centred at 300). */
    public static final float X_NUDGE_PER_WIDTH = -0.0007f;

    /**
     * v0.7.6 (Tj, 2026-11-06: "the accessibility one you built is slightly smaller than the correct size and font"): Tj's own nudges (Launcher control) on top of the measured look, so a photo-based calibration
     * can be corrected on the tablet without a new release. All are plain integers, default 0 = the measured look above.
     */
    public static final String SIZE_KEY = "lock_number_size_pct";            // percent of the text size
    public static final String WEIGHT_KEY = "lock_number_weight_nudge";      // steps of 100 on the font weight (Light 300 -> Regular 400 -> Medium 500)
    public static final String ALPHA_KEY = "lock_number_alpha_pct";         // percent of the brightness (alpha)

    private LockNumber() {}

    /** Pure: the text size (em, px) for a screen {@code widthPx} wide, with Tj's size nudge (clamped to -30..+30 percent). */
    public static float emFor(float widthPx, int sizePct) { return widthPx * EM_PER_WIDTH * (1f + Math.max(-30, Math.min(30, sizePct)) / 100f); }

    /** Pure: the Android font weight (100..900 in steps of 100) for Tj's weight nudge. */
    public static int weightFor(int nudgeSteps) { return Math.max(100, Math.min(900, WEIGHT + 100 * Math.max(-2, Math.min(4, nudgeSteps)))); }

    /** Pure: the paint alpha (0..255) for Tj's brightness nudge (clamped to -40..+40 percent, never below 60 so the number cannot vanish). */
    public static int alphaFor(int pct) { return Math.max(60, Math.min(255, Math.round(ALPHA * (1f + Math.max(-40, Math.min(40, pct)) / 100f)))); }

    /** What Tj may type as the lock-screen text: letters, digits, space - : . / _ ; at most 24 characters; empty = the default number. */
    public static String sanitize(String in) {
        if (in == null) return DEFAULT_TEXT;
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < in.length() && b.length() < 24; i++) {
            char ch = in.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == ' ' || ch == '-' || ch == ':' || ch == '.' || ch == '/' || ch == '_') b.append(ch);
        }
        String t = b.toString().trim();
        return t.isEmpty() ? DEFAULT_TEXT : t;
    }

    public static boolean enabled(Context c) { return Features.get(c, KEY, true); }
    public static String text(Context c) {
        String t = Features.getString(c, TEXT_KEY, DEFAULT_TEXT);
        return t == null || t.trim().isEmpty() ? DEFAULT_TEXT : t.trim();
    }

    /** Pure rule (JVM-testable): shown only on the lock screen itself, screen on, switch on, and never while the PIN/pattern/password pad is up. */
    public static boolean shouldShow(boolean enabled, boolean screenOn, boolean keyguardLocked, boolean bouncerShown) {
        return enabled && screenOn && keyguardLocked && !bouncerShown;
    }
}
