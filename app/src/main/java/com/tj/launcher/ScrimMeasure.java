package com.tj.launcher;

/**
 * BA1 (Tj, 2026-11-07: "make the brightness of the square patch match the darkened lock screen after the side key press 100% identical"): WHEN and HOW the helper measures the stock menu's real scrim on the tablet
 * (the maths is {@link ScrimFit}). The sequence, once per lock screen (wallpaper / rotation / size / theme): a bare frame of the plain lock screen, the stock power menu opened by the helper itself
 * ({@code GLOBAL_ACTION_POWER_DIALOG}) until its fade is complete, a second frame, the menu closed again ({@code GLOBAL_ACTION_BACK}, retried until it is gone). It only ever starts on a PLAIN lock screen (screen on,
 * no PIN pad, no menu, no gesture in progress) and gives up at the first sign of anything else: the lock screen is never blocked. This class is the pure part (JVM-tested): the decision, the limits and the sampling grid.
 * {@link TjAccessibilityService} does the Android side.
 */
public final class ScrimMeasure {
    /** Launcher control switch. Default ON in the app, OFF in a debuggable build (the emulator sections that do not test this must not see a power menu open by itself). */
    public static final String KEY = "scrim_measure";
    /** The epoch ms of the recent starts, comma separated (the cap per day; {@link AutoCalibration#addTry} keeps the list). */
    public static final String TRIES_KEY = "scrim_tries";
    /** What happened last, in plain words (Launcher control, the tablet report). */
    public static final String NOTE_KEY = "scrim_note";
    /** The stored measurement of one rotation: {@code VALUE_PREFIX + rotation} ({@link ScrimFit#encode}). */
    public static final String VALUE_PREFIX = "patch_scrim_r";

    public static final long MIN_GAP_MS = 45_000;
    public static final int MAX_PER_LOCK = 2, MAX_PER_DAY = 4;
    /** The screen has to have been on this long: the lock screen settles (clock, wallpaper fade-in) before the first frame. */
    public static final long SETTLE_AFTER_WAKE_MS = 3_500;
    /** How long to wait for a screenshot to appear in the media store, for the menu to open, and the steps of closing it again. */
    public static final long SHOT_WAIT_MS = 8_000, MENU_WAIT_MS = 4_000, CLOSE_STEP_MS = 900;
    public static final int CLOSE_TRIES = 5;
    /** Between the bare frame and the menu: the screenshot's own animation (flash, shrink, thumbnail in the corner) must be over before the next frame. */
    public static final long SHOT_GAP_MS = 2_400;
    /** After the menu opened: the stock fade (300 ms) is complete, plus a margin for a slow tablet. */
    public static final long OPEN_SETTLE_MS = PatchFade.SETTLE_MS + 270;
    /** The whole sequence gives up after this. */
    public static final long STAGE_LIMIT_MS = 45_000;

    private ScrimMeasure() {}

    /** The default of the switch: ON in the app, OFF in a debuggable (emulator test) build. */
    public static boolean defaultOn(boolean debuggable) { return !debuggable; }

    /** Everything the decision depends on; the service fills it in. */
    public static final class State {
        public boolean switchOn, coverOn, lockPatchOn, paused, permitted, geometryKnown, validCrop, haveMeasurement, calibrating, stageRunning;
        public boolean interactive, keyguardLocked, pinPadUp, menuOpen, handshakeArmed;
        public long now, screenOnMs, lastStartMs;
        public int startsThisLock, startsToday;
    }

    /** Pure: why NOT to start now ("" = go). The reasons are shown in Launcher control, so they are plain words. */
    public static String decision(State s) {
        if (!s.switchOn) return "measuring the menu dimming is switched off";
        if (s.paused) return "paused (Restore everything / the crash breaker)";
        if (!s.coverOn || !s.lockPatchOn) return "the lock-screen patch is switched off";
        if (!s.permitted) return "needs permission: Launcher control > Allow wallpaper access";
        if (s.haveMeasurement) return "";   // nothing to do; callers treat haveMeasurement separately
        if (s.calibrating || s.stageRunning) return "a calibration or a measurement is already running";
        if (!s.validCrop) return "waiting: the patch has no real-pixel picture yet (the automatic calibration comes first)";
        if (!s.geometryKnown) return "waiting: hold the side key once on the lock screen so the menu's place is known";
        if (!s.interactive) return "waiting: the screen is off";
        if (!s.keyguardLocked) return "waiting for the lock screen";
        if (s.pinPadUp) return "waiting: the PIN pad is up";
        if (s.menuOpen) return "waiting: the power menu is open";
        if (s.handshakeArmed) return "waiting: an unlock gesture is in progress";
        if (s.screenOnMs > 0 && s.now >= s.screenOnMs && s.now - s.screenOnMs < SETTLE_AFTER_WAKE_MS) return "waiting: the screen has only just come on";
        if (s.lastStartMs > 0 && s.now >= s.lastStartMs && s.now - s.lastStartMs < MIN_GAP_MS) return "waiting: the last measurement was just now";
        if (s.startsThisLock >= MAX_PER_LOCK) return "gave up for this lock after " + MAX_PER_LOCK + " tries";
        if (s.startsToday >= MAX_PER_DAY) return "gave up for today after " + MAX_PER_DAY + " tries";
        return "";
    }

    /** Pure: start now? Only when there is no measurement yet for this lock screen AND {@link #decision} says go. */
    public static boolean shouldStart(State s) { return !s.haveMeasurement && decision(s).isEmpty(); }

    /** Pure: the preference key of the measurement of one rotation. */
    public static String valueKey(int rotation) { return VALUE_PREFIX + rotation; }

    // ---------------------------------------------------------------- the sampling grid

    /**
     * The rectangles (left, top, right, bottom) that must NOT be sampled: the status bar and navigation bar bands (icons, clock), the stock panel with room for its shadow, the patch window, and our own lock number.
     * {@code panel} / {@code patch} may be null (unknown). Everything else on the lock screen is either plain wallpaper (the scrim applied to it) or small clutter that the robust fit throws out.
     */
    public static int[][] excludeRects(int w, int h, int[] panel, int[] patch, int shadowPx) {
        int top = Math.round(h * 0.035f), bottomBand = Math.round(h * 0.065f);
        int[] num = numberRect(w, h);
        int n = 3 + (panel != null ? 1 : 0) + (patch != null ? 1 : 0), i = 0;
        int[][] r = new int[n][];
        r[i++] = new int[] { 0, 0, w, top };
        r[i++] = new int[] { 0, h - bottomBand, w, h };
        r[i++] = num;
        if (panel != null) r[i++] = new int[] { panel[0] - shadowPx, panel[1] - shadowPx, panel[2] + shadowPx, panel[3] + shadowPx };
        if (patch != null) r[i++] = new int[] { patch[0] - 6, patch[1] - 6, patch[2] + 6, patch[3] + 6 };
        return r;
    }

    /** Our own lock number (v0.3.3: bbox x 205-396, y 907-924 on a 600 x 1024 screen) scaled to this screen, with room around it. */
    public static int[] numberRect(int w, int h) {
        float sx = w / 600f, sy = h / 1024f;
        return new int[] { Math.round(190 * sx), Math.round(895 * sy), Math.round(410 * sx), Math.round(938 * sy) };
    }

    private static boolean inside(int x, int y, int[] r) { return x >= r[0] && x < r[2] && y >= r[1] && y < r[3]; }

    /**
     * Pure: the sample points, every {@code step} pixels, as {@code (y << 16) | x}, skipping the excluded rectangles and a margin of 1 pixel at the screen edge (the flatness test looks at the neighbours).
     */
    public static int[] grid(int w, int h, int step, int[][] exclude) {
        int cap = ((w / step) + 1) * ((h / step) + 1);
        int[] out = new int[cap];
        int n = 0;
        for (int y = 1; y < h - 1; y += step) {
            for (int x = 1; x < w - 1; x += step) {
                boolean skip = false;
                if (exclude != null) for (int[] r : exclude) if (r != null && inside(x, y, r)) { skip = true; break; }
                if (!skip) out[n++] = (y << 16) | x;
            }
        }
        return java.util.Arrays.copyOf(out, n);
    }

    /**
     * Pure: is a pixel flat enough to sample? Neither an edge through it (the opposite neighbours differ) nor a one-pixel line or dot on it (it differs from the average of its four neighbours: a central
     * difference alone is blind to a thin stroke, whose two sides are equal), so a text stroke or a one-pixel shift cannot spoil the pair.
     */
    public static boolean flat(int lumCenter, int lumLeft, int lumRight, int lumUp, int lumDown) {
        return Math.abs(lumRight - lumLeft) + Math.abs(lumDown - lumUp) <= FLAT_MAX
                && Math.abs(4 * lumCenter - (lumLeft + lumRight + lumUp + lumDown)) <= FLAT_MAX;
    }
    public static final int FLAT_MAX = 60;

    /** Pure: the luminance (0..255) of a packed RGB value. */
    public static int lum(int rgb) { return (((rgb >> 16) & 255) * 77 + ((rgb >> 8) & 255) * 150 + (rgb & 255) * 29) >> 8; }

    /**
     * Pure: the sample points of the BARE frame: the grid (every 4th pixel, every 2nd when that leaves too few), minus the excluded rectangles, keeping only flat pixels. {@code px} = the frame's pixels
     * ({@code Bitmap.getPixels}, row-major, w * h). The same points are then read from the menu frame.
     */
    public static int[] pickPoints(int[] px, int w, int h, int[][] exclude) {
        if (px == null || px.length < w * h || w < 4 || h < 4) return new int[0];
        int[] kept = new int[0];
        for (int step : new int[] { 4, 2 }) {
            int[] g = grid(w, h, step, exclude);
            kept = new int[g.length];
            int n = 0;
            for (int v : g) {
                int x = v & 0xFFFF, y = v >>> 16, i = y * w + x;
                if (flat(lum(px[i]), lum(px[i - 1]), lum(px[i + 1]), lum(px[i - w]), lum(px[i + w]))) kept[n++] = v;
            }
            kept = java.util.Arrays.copyOf(kept, n);
            if (n >= 2 * ScrimFit.MIN_SAMPLES) break;
        }
        return kept;
    }

    /** Pure: one colour channel (0..255) of the frame at the points; {@code shift} 16 = red, 8 = green, 0 = blue. */
    public static int[] channel(int[] px, int w, int[] points, int shift) {
        int[] out = new int[points.length];
        for (int k = 0; k < points.length; k++) out[k] = (px[(points[k] >>> 16) * w + (points[k] & 0xFFFF)] >> shift) & 255;
        return out;
    }
}
