package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PatchCalibratorTest {
    private static int[] wallpaper(int w, int h) {
        int[] p = new int[w * h];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int r = (int) (127 + 120 * Math.sin(x / 23.0) * Math.cos(y / 41.0));
            int g = (int) (127 + 120 * Math.sin((x + y) / 31.0));
            int b = (int) (127 + 120 * Math.cos(x / 17.0 - y / 29.0));
            p[y * w + x] = 0xFF000000 | (r << 16) | (g << 8) | b;
        }
        return p;
    }

    /** Draws the wallpaper the way the model says (scale m, shift dx/dy) onto a W x H "screen". */
    private static int[] render(int[] wall, int bw, int bh, int W, int H, float xOff, float m, int dx, int dy) {
        float s = Math.max((float) H / bh, (float) W / bw) * m, dw = bw * s, dh = bh * s;
        int[] out = new int[W * H];
        for (int y = 0; y < H; y++) for (int x = 0; x < W; x++) {
            int u = (int) ((x - dx + (dw - W) * xOff) / s), v = (int) ((y - dy + (dh - H) * 0.5f) / s);
            out[y * W + x] = (u < 0 || v < 0 || u >= bw || v >= bh) ? 0xFF000000 : wall[v * bw + u];
        }
        return out;
    }

    @Test public void findsTheRealScaleAndShift() {
        int bw = 800, bh = 1200, W = 300, H = 512;     // small "screen": the maths is resolution independent
        int[] wall = wallpaper(bw, bh);
        int[] shot = render(wall, bw, bh, W, H, 0.5f, 1.08f, -14, 9);
        PatchCalibrator.Result r = PatchCalibrator.solve(wall, bw, bh, shot, W, H, 180, 150, 290, 300, 0.5f);
        assertNotNull(r);
        assertEquals(1.08f, r.scale, 0.012f);
        assertTrue("dx " + r.dx, Math.abs(r.dx + 14) <= 3);
        assertTrue("dy " + r.dy, Math.abs(r.dy - 9) <= 3);
        assertTrue(r.error < r.baselineError);
    }

    @Test public void keepsTheDefaultWhenItAlreadyMatches() {
        int bw = 800, bh = 1200, W = 300, H = 512;
        int[] wall = wallpaper(bw, bh);
        int[] shot = render(wall, bw, bh, W, H, 0.5f, 1f, 0, 0);
        PatchCalibrator.Result r = PatchCalibrator.solve(wall, bw, bh, shot, W, H, 180, 150, 290, 300, 0.5f);
        assertNotNull(r);
        assertEquals(1f, r.scale, 0.0001f); assertEquals(0, r.dx); assertEquals(0, r.dy);
    }

    @Test public void aFlatRegionCannotBeCalibrated() {
        int bw = 400, bh = 600, W = 300, H = 512;
        int[] wall = new int[bw * bh]; java.util.Arrays.fill(wall, 0xFF336699);
        int[] shot = new int[W * H]; java.util.Arrays.fill(shot, 0xFF336699);
        assertNull(PatchCalibrator.solve(wall, bw, bh, shot, W, H, 180, 150, 290, 300, 0.5f));
    }

    @Test public void aScreenThatShowsAnotherPictureIsRejected() {
        int bw = 800, bh = 1200, W = 300, H = 512;
        int[] wall = wallpaper(bw, bh);
        int[] shot = new int[W * H];
        for (int i = 0; i < shot.length; i++) shot[i] = 0xFF000000 | (int) (((i * 2654435761L) >>> 8) & 0xFFFFFF);   // noise
        assertNull(PatchCalibrator.solve(wall, bw, bh, shot, W, H, 180, 150, 290, 300, 0.5f));
    }
}
