package com.tj.launcher;

import java.util.Arrays;
import java.util.Locale;

/**
 * BA1 (Tj, 2026-11-07 00:07: "the final wallpaper patch is very slightly too dark and I can see the square patch, but barely. Try to make the brightness of the square patch match the darkened lock screen after the side
 * key press 100% identical").
 *
 * What the stock menu does to the screen (Android 10 source, research/AOSP10_BATTERYSAVER_AND_POWERMENU_2026-11-06.md): its background is a flat scrim, {@code out = (1 - a) * L + a * C} with a = 0.2 at the end and C the
 * colour extractor's neutral colour. Per colour channel that is an AFFINE map of what the lock screen showed before: {@code out = s * L + o} with {@code s = 1 - a} and {@code o = a * C}. The AOSP emulator has a black C
 * (o = 0, s = 0.80), which is why a plain "brightness 0.80" was exact there. On Tj's MediaTek tablet it is not exact (v0.8.0 used 0.79, v0.8.1 0.80: each step made the square fainter, never gone; earlier photos showed
 * this tablet's lock screen darkening with a colour offset the emulator does not have, red most), so nothing but a MEASUREMENT on the tablet can be 100 % identical.
 *
 * The measurement: one frame of the bare lock screen (S0) and one with the stock menu fully faded in (S1), both full-screen screenshots of the same lock screen. Outside the menu panel, the patch window and the
 * clutter (clock, icons, our own number) S1 is the scrim applied to S0, so a ROBUST per-channel line fit {@code S1 = s * S0 + o} over a grid of samples gives the exact transform. Because both {@code s} and {@code o}
 * are linear in the scrim's alpha, the patch can follow the stock fade by interpolating identity -> transform with the same {@link PatchFade} progress ({@link Transform#sAt}, {@link Transform#oAt}).
 *
 * Pure (no Android): JVM-tested with synthetic scrims, including a non-black colour and heavy contamination. {@link TjAccessibilityService} takes the screenshots and draws.
 */
public final class ScrimFit {
    private ScrimFit() {}

    /** The fitted line of one channel, with the numbers the gates look at. */
    public static final class Result {
        public final boolean ok;
        public final String why;            // "" when ok
        public final double s, o;           // y = s * x + o
        public final double rms;            // root-mean-square residual of the inliers, in levels (0..255)
        public final double spread;         // standard deviation of x over the inliers, in levels
        public final int n, inliers;
        Result(boolean ok, String why, double s, double o, double rms, double spread, int n, int inliers) {
            this.ok = ok; this.why = why; this.s = s; this.o = o; this.rms = rms; this.spread = spread; this.n = n; this.inliers = inliers;
        }
        static Result no(String why, int n) { return new Result(false, why, 1, 0, 99, 0, n, 0); }
    }

    /** Fewest samples that may decide, fewest per brightness bin, and the width of the first-guess bins (levels). */
    public static final int MIN_SAMPLES = 3000, MIN_BIN = 40, BIN_SHIFT = 4;
    /** Gates on one channel: the line must fit the inliers closely and the wallpaper must cover enough brightness levels to tell scale from offset. */
    public static final double MAX_RMS = 3.0, MIN_SPREAD = 12.0, MIN_INLIER_FRACTION = 0.55;
    /** Gates on the transform: a scrim darkens (s below 1) but not to black; an offset beyond +-45 levels is not a scrim. */
    public static final double MIN_S = 0.55, MAX_S = 0.95, MAX_ABS_O = 45.0;   // review of BA1: 0.98 let a frame from the middle of the fade (s 0.90) through; the stock scrim is 0.80, anything above 0.95 is "nothing was darkened" or "the fade had barely begun"
    /** The three channels of one scrim share the same s (1 - alpha): they may differ only by measurement noise. */
    public static final double MAX_S_DISAGREE = 0.05;

    /**
     * Pure: robust fit of {@code y = s * x + o} over the first {@code n} samples (values 0..255). First guess = a weighted line through the MEDIAN of y in 16 brightness bins (robust to a large block of wrong
     * samples such as a menu panel); then trimmed least squares on the samples within 3 robust sigmas of the line, repeated until the set is stable.
     */
    public static Result fitChannel(int[] x, int[] y, int n) {
        if (x == null || y == null) return Result.no("no samples", 0);
        n = Math.min(n, Math.min(x.length, y.length));
        if (n < MIN_SAMPLES) return Result.no("too few samples (" + n + ")", n);
        // ---- first guess: the median of y per brightness bin, a weighted line through those medians
        int bins = 256 >> BIN_SHIFT;
        int[][] hist = new int[bins][256];
        int[] cnt = new int[bins];
        double[] sumX = new double[bins];
        for (int i = 0; i < n; i++) {
            int xv = clamp255(x[i]), yv = clamp255(y[i]), b = xv >> BIN_SHIFT;
            hist[b][yv]++; cnt[b]++; sumX[b] += xv;
        }
        double sw = 0, swx = 0, swy = 0, swxx = 0, swxy = 0;
        int used = 0;
        for (int b = 0; b < bins; b++) {
            if (cnt[b] < MIN_BIN) continue;
            int half = (cnt[b] + 1) / 2, acc = 0, med = 0;
            for (int v = 0; v < 256; v++) { acc += hist[b][v]; if (acc >= half) { med = v; break; } }
            double bx = sumX[b] / cnt[b], w = cnt[b];
            sw += w; swx += w * bx; swy += w * med; swxx += w * bx * bx; swxy += w * bx * med; used++;
        }
        if (used < 2) return Result.no("the wallpaper covers too few brightness levels", n);
        double den0 = sw * swxx - swx * swx;
        if (den0 < 1e-9) return Result.no("the wallpaper covers too few brightness levels", n);
        double s = (sw * swxy - swx * swy) / den0, o = (swy - s * swx) / sw;
        // ---- trimmed least squares, repeated
        boolean[] in = new boolean[n];
        int inliers = n, last = -1;
        double[] res = new double[n];
        for (int iter = 0; iter < 8; iter++) {
            for (int i = 0; i < n; i++) res[i] = y[i] - (s * x[i] + o);
            double[] abs = new double[n];
            for (int i = 0; i < n; i++) abs[i] = Math.abs(res[i]);
            Arrays.sort(abs);
            double sigma = Math.max(1.0, 1.4826 * abs[n / 2]);
            double limit = Math.min(Math.max(2.5, 3.0 * sigma), 12.0);
            double a = 0, bx = 0, by = 0, bxx = 0, bxy = 0;
            inliers = 0;
            for (int i = 0; i < n; i++) {
                in[i] = Math.abs(res[i]) <= limit;
                if (!in[i]) continue;
                inliers++; a += 1; bx += x[i]; by += y[i]; bxx += (double) x[i] * x[i]; bxy += (double) x[i] * y[i];
            }
            if (inliers < MIN_SAMPLES / 2) return Result.no("most samples disagree with each other (" + inliers + " of " + n + " fit a line)", n);
            double den = a * bxx - bx * bx;
            if (den < 1e-9) return Result.no("the inliers cover too few brightness levels", n);
            s = (a * bxy - bx * by) / den; o = (by - s * bx) / a;
            if (inliers == last) break;
            last = inliers;
        }
        // ---- the numbers of the final fit
        double sr = 0, mx = 0, vx = 0; int m = 0;
        for (int i = 0; i < n; i++) {
            double r = y[i] - (s * x[i] + o);
            if (Math.abs(r) > 12.0) continue;
            sr += r * r; mx += x[i]; m++;
        }
        if (m < MIN_SAMPLES / 2) return Result.no("most samples disagree with each other", n);
        mx /= m;
        for (int i = 0; i < n; i++) { double r = y[i] - (s * x[i] + o); if (Math.abs(r) > 12.0) continue; vx += (x[i] - mx) * (x[i] - mx); }
        double rms = Math.sqrt(sr / m), spread = Math.sqrt(vx / m);
        if (inliers < MIN_INLIER_FRACTION * n) return new Result(false, "only " + Math.round(100.0 * inliers / n) + " % of the samples fit one line", s, o, rms, spread, n, inliers);
        if (spread < MIN_SPREAD) return new Result(false, "the wallpaper is too flat to tell the scale from the offset (spread " + Math.round(spread) + ")", s, o, rms, spread, n, inliers);
        if (rms > MAX_RMS) return new Result(false, "the fit is not tight enough (rms " + String.format(Locale.US, "%.1f", rms) + " levels)", s, o, rms, spread, n, inliers);
        return new Result(true, "", s, o, rms, spread, n, inliers);
    }

    private static int clamp255(int v) { return v < 0 ? 0 : Math.min(v, 255); }

    /** The transform of the three channels (R, G, B): {@code out = s[c] * in + o[c]}, o in levels (0..255 scale). */
    public static final class Transform {
        public final float[] s, o;
        public final int n; public final double rms;
        public Transform(float[] s, float[] o, int n, double rms) { this.s = s; this.o = o; this.n = n; this.rms = rms; }

        /** The scale of channel {@code c} at fade progress {@code p} (0 = none yet = 1, 1 = complete = the measured s): the stock scrim's s is {@code 1 - a} and a rises with the fade. */
        public float sAt(int c, float p) { return 1f + (s[c] - 1f) * p; }
        /** The offset of channel {@code c} at fade progress {@code p}: {@code a * C} rises with the fade too. */
        public float oAt(int c, float p) { return o[c] * p; }
    }

    /**
     * Pure: the transform from the three channel fits, or null with the reason in {@code why[0]}. All three must be ok, the scale in range and (nearly) the same for the three channels, the offsets small.
     */
    public static Transform combine(Result r, Result g, Result b, String[] why) {
        Result[] all = { r, g, b };
        String[] names = { "red", "green", "blue" };
        for (int i = 0; i < 3; i++) if (all[i] == null || !all[i].ok) { if (why != null && why.length > 0) why[0] = names[i] + ": " + (all[i] == null ? "no fit" : all[i].why); return null; }
        double smin = Math.min(r.s, Math.min(g.s, b.s)), smax = Math.max(r.s, Math.max(g.s, b.s));
        if (smax - smin > MAX_S_DISAGREE) { if (why != null && why.length > 0) why[0] = "the three colours disagree about the scale (" + f3(r.s) + " / " + f3(g.s) + " / " + f3(b.s) + "): that is not a flat scrim"; return null; }
        float[] s = new float[3], o = new float[3];
        for (int i = 0; i < 3; i++) {
            if (all[i].s < MIN_S || all[i].s > MAX_S) { if (why != null && why.length > 0) why[0] = names[i] + ": scale " + f3(all[i].s) + " is not a darkening scrim"; return null; }
            if (Math.abs(all[i].o) > MAX_ABS_O) { if (why != null && why.length > 0) why[0] = names[i] + ": offset " + Math.round(all[i].o) + " is too large for a scrim"; return null; }
            s[i] = (float) all[i].s; o[i] = (float) all[i].o;
        }
        return new Transform(s, o, Math.min(r.n, Math.min(g.n, b.n)), Math.max(r.rms, Math.max(g.rms, b.rms)));
    }

    private static String f3(double v) { return String.format(Locale.US, "%.3f", v); }

    // ---------------------------------------------------------------- storage (one preference string per rotation)

    /** What a stored measurement is valid for: the lock screen's signature ({@link PatchShot#sig}) and whether the system was in the dark theme. */
    public static String encode(Transform t, String sig, boolean night, long when) {
        return sig + "#" + (night ? "n" : "d") + "#" + f4(t.s[0]) + "," + f4(t.s[1]) + "," + f4(t.s[2]) + "#" + f4(t.o[0]) + "," + f4(t.o[1]) + "," + f4(t.o[2]) + "#" + t.n + "#" + f4(t.rms) + "#" + when;
    }

    /** The stored transform if it was measured for this signature and theme and passes the same gates again; else null. Never throws. */
    public static Transform decode(String stored, String sig, boolean night) {
        try {
            if (stored == null || stored.isEmpty()) return null;
            String[] p = stored.split("#");
            if (p.length != 7 || !p[0].equals(sig) || !p[1].equals(night ? "n" : "d")) return null;
            String[] ss = p[2].split(","), oo = p[3].split(",");
            if (ss.length != 3 || oo.length != 3) return null;
            float[] s = new float[3], o = new float[3];
            for (int i = 0; i < 3; i++) {
                s[i] = Float.parseFloat(ss[i]); o[i] = Float.parseFloat(oo[i]);
                if (!(s[i] >= MIN_S && s[i] <= MAX_S) || !(Math.abs(o[i]) <= MAX_ABS_O)) return null;
            }
            return new Transform(s, o, Integer.parseInt(p[4]), Double.parseDouble(p[5]));
        } catch (Throwable t) { return null; }
    }

    /** A line for Launcher control / the tablet report. */
    public static String describe(Transform t) {
        if (t == null) return "not measured for this lock screen (the patch uses the generic 0.80)";
        return "scale " + f3(t.s[0]) + " / " + f3(t.s[1]) + " / " + f3(t.s[2]) + ", offset " + String.format(Locale.US, "%.1f / %.1f / %.1f", t.o[0], t.o[1], t.o[2]) + " (R/G/B; " + t.n + " samples, rms " + String.format(Locale.US, "%.1f", t.rms) + ")";
    }

    /**
     * A line for the report / Launcher control from whatever is stored, WITHOUT checking that it is valid for the current lock screen (that is what {@link #decode} is for): what was measured, for which lock screen
     * and when. "none" for an empty / unreadable record. Never throws.
     */
    public static String describeStored(String stored) {
        try {
            if (stored == null || stored.isEmpty()) return "none";
            String[] p = stored.split("#");
            if (p.length != 7) return "unreadable";
            String[] ss = p[2].split(","), oo = p[3].split(",");
            String when = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new java.util.Date(Long.parseLong(p[6])));
            return "scale " + ss[0] + " / " + ss[1] + " / " + ss[2] + ", offset " + oo[0] + " / " + oo[1] + " / " + oo[2] + " (R/G/B) for " + p[0] + (p[1].equals("n") ? " dark theme" : "") + ", " + p[4] + " samples, rms " + p[5] + ", measured " + when;
        } catch (Throwable t) { return "unreadable"; }
    }

    private static String f4(double v) { return String.format(Locale.US, "%.4f", v); }
}
