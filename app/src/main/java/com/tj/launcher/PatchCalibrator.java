package com.tj.launcher;

/**
 * V4 (Tj, 2026-11-04): "the wallpaper patch may need sizing because last time it was the wrong part of the wallpaper so it didn't match".
 * Instead of trusting a model of how Android places the wallpaper, compare the wallpaper picture with a REAL screenshot of the lock screen (taken by the shell helper
 * with the menu closed) and find the scale / shift at which the picture matches what is on screen. Pure Java so it is unit-tested without a device.
 *
 * Model (same as the patch view's onDraw): the bitmap is scaled by s = max(H/bh, W/bw) * m (cover), centred vertically, shifted sideways by xOff, then moved by (dx, dy):
 *   screen X = u*s - (dw - W) * xOff + dx        screen Y = v*s - (dh - H) * 0.5 + dy        (dw = bw*s, dh = bh*s)
 */
public final class PatchCalibrator {
    private PatchCalibrator() {}

    /** Why the last solve() answered as it did (spread of the photographed area, best / default error): written to the log so a "kept" answer on a device can be understood without the picture. */
    public static volatile String lastNote = "";

    public static final class Result {
        public final float scale; public final int dx, dy; public final double error, baselineError;
        Result(float scale, int dx, int dy, double error, double baselineError) { this.scale = scale; this.dx = dx; this.dy = dy; this.error = error; this.baselineError = baselineError; }
        @Override public String toString() { return "scale=" + scale + " dx=" + dx + " dy=" + dy + " error=" + String.format(java.util.Locale.US, "%.4f", error) + " (default model " + String.format(java.util.Locale.US, "%.4f", baselineError) + ")"; }
    }

    /** Mean absolute colour difference (0..1) over the sampled pixels of the region for one candidate; 1.0 for pixels that fall outside the bitmap. */
    static double error(int[] wall, int bw, int bh, int[] shot, int W, int H, int l, int t, int r, int b, float xOff, float m, int dx, int dy, int stride, float skew) {
        float s = Math.max((float) H / bh, (float) W / bw) * skew * m;
        float dw = bw * s, dh = bh * s;
        float offX = (dw - W) * xOff - dx, offY = (dh - H) * 0.5f - dy;
        double sum = 0; int n = 0;
        for (int y = t; y < b; y += stride) {
            int v = (int) ((y + offY) / s);
            for (int x = l; x < r; x += stride) {
                int u = (int) ((x + offX) / s);
                n++;
                if (u < 0 || v < 0 || u >= bw || v >= bh) { sum += 1.0; continue; }
                int p = wall[v * bw + u], q = shot[y * W + x];
                sum += (Math.abs(((p >> 16) & 255) - ((q >> 16) & 255)) + Math.abs(((p >> 8) & 255) - ((q >> 8) & 255)) + Math.abs((p & 255) - (q & 255))) / 765.0;
            }
        }
        return n == 0 ? 1.0 : sum / n;
    }

    /** Standard deviation (0..1) of the screenshot's brightness in the region: a flat patch cannot be aligned (any position matches). */
    static double spread(int[] shot, int W, int l, int t, int r, int b) {
        double s = 0, s2 = 0; int n = 0;
        for (int y = t; y < b; y += 3) for (int x = l; x < r; x += 3) {
            int q = shot[y * W + x]; double g = (((q >> 16) & 255) + ((q >> 8) & 255) + (q & 255)) / 765.0;
            s += g; s2 += g * g; n++;
        }
        if (n == 0) return 0;
        double mean = s / n; return Math.sqrt(Math.max(0, s2 / n - mean * mean));
    }

    /**
     * @param wall  the wallpaper picture, ARGB, bw x bh
     * @param shot  the real screen, ARGB, W x H
     * @param l,t,r,b the patch rectangle in screen pixels
     * @return the best fit, or null when the region is too flat / nothing fits (caller keeps the default model and the manual nudges)
     */
    public static Result solve(int[] wall, int bw, int bh, int[] shot, int W, int H, int l, int t, int r, int b, float xOff) { return solve(wall, bw, bh, shot, W, H, l, t, r, b, xOff, 1f); }

    /** @param skew multiplier on the model's base scale (1 normally; the emulator test sets a wrong one on purpose to prove the calibration corrects it) */
    public static Result solve(int[] wall, int bw, int bh, int[] shot, int W, int H, int l, int t, int r, int b, float xOff, float skew) {
        l = Math.max(0, l); t = Math.max(0, t); r = Math.min(W, r); b = Math.min(H, b);
        if (r - l < 12 || b - t < 12 || wall == null || shot == null) { lastNote = "region too small or no picture"; return null; }
        double sp = spread(shot, W, l, t, r, b);
        if (sp < 0.015) { lastNote = String.format(java.util.Locale.US, "spread %.4f < 0.015 (flat area)", sp); return null; }
        double base = error(wall, bw, bh, shot, W, H, l, t, r, b, xOff, 1f, 0, 0, 2, skew);
        double best = Double.MAX_VALUE; float bm = 1f; int bx = 0, by = 0;
        int coarse = 6;
        for (float m = 0.80f; m <= 1.30001f; m += 0.02f)
            for (int dx = -W / 4; dx <= W / 4; dx += 8)
                for (int dy = -H / 6; dy <= H / 6; dy += 8) {
                    double e = error(wall, bw, bh, shot, W, H, l, t, r, b, xOff, m, dx, dy, coarse, skew);
                    if (e < best) { best = e; bm = m; bx = dx; by = dy; }
                }
        for (int pass = 0; pass < 6; pass++) {            // scale and shift trade off against each other: walk downhill until nothing improves
            float cm = bm; int cx = bx, cy = by; double before = best;
            for (float m = cm - 0.02f; m <= cm + 0.02001f; m += 0.004f)
                for (int dx = cx - 8; dx <= cx + 8; dx += 1)
                    for (int dy = cy - 8; dy <= cy + 8; dy += 1) {
                        double e = error(wall, bw, bh, shot, W, H, l, t, r, b, xOff, m, dx, dy, 2, skew);
                        if (e < best) { best = e; bm = m; bx = dx; by = dy; }
                    }
            if (best >= before - 1e-9) break;
        }
        lastNote = String.format(java.util.Locale.US, "spread %.4f, best error %.4f at m=%.3f dx=%d dy=%d, default model %.4f, region %dx%d at %d,%d on %dx%d, picture %dx%d", sp, best, bm, bx, by, base, r - l, b - t, l, t, W, H, bw, bh);
        if (best > 0.08) return null;                      // nothing matches well (lock-screen text over the patch, a different picture): do not guess
        if (base <= best + 0.004) return new Result(1f, 0, 0, base, base);   // the default model is already as good as anything: keep it
        return new Result(bm, bx, by, best, base);
    }
}
