package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import org.junit.Test;

/**
 * BA1 (Tj, 2026-11-07: "make the brightness of the square patch match the darkened lock screen after the side key press 100% identical"): the robust fit that measures the real scrim on the tablet. Synthetic scrims
 * stand in for the screenshots: the AOSP emulator's black one, a TINTED one (what the tablet seems to have), and heavy contamination (the menu panel, clock, icons).
 */
public class ScrimFitTest {
    private static final int N = 24_000;

    /** A wallpaper-like brightness distribution: a mix of dark, mid and bright areas (a leaf: lots of mid-dark green, some bright veins). */
    private static int[] wallpaper(Random r, int n) {
        int[] x = new int[n];
        for (int i = 0; i < n; i++) {
            double u = r.nextDouble();
            int v = u < 0.45 ? 15 + r.nextInt(70) : u < 0.85 ? 60 + r.nextInt(110) : 150 + r.nextInt(100);
            x[i] = Math.max(0, Math.min(255, v));
        }
        return x;
    }

    private static int[] scrim(int[] x, double s, double o, double noise, Random r) {
        int[] y = new int[x.length];
        for (int i = 0; i < x.length; i++) y[i] = Math.max(0, Math.min(255, (int) Math.round(s * x[i] + o + (noise == 0 ? 0 : r.nextGaussian() * noise))));
        return y;
    }

    // ---- the fit itself
    @Test public void aBlackScrimIsMeasuredExactly() {
        Random r = new Random(1);
        int[] x = wallpaper(r, N), y = scrim(x, 0.80, 0, 0.5, r);
        ScrimFit.Result f = ScrimFit.fitChannel(x, y, N);
        assertTrue(f.why, f.ok);
        assertEquals(0.80, f.s, 0.004); assertEquals(0.0, f.o, 0.8);
    }
    @Test public void aTintedScrimIsMeasuredPerChannel() {
        Random r = new Random(2);
        int[] x = wallpaper(r, N);
        double[] off = { 6, 12, 3 };
        ScrimFit.Result[] f = new ScrimFit.Result[3];
        for (int c = 0; c < 3; c++) {
            int[] y = scrim(x, 0.80, off[c], 0.6, r);
            f[c] = ScrimFit.fitChannel(x, y, N);
            assertTrue(f[c].why, f[c].ok);
            assertEquals(0.80, f[c].s, 0.005);
            assertEquals(off[c], f[c].o, 1.0);
        }
        String[] why = new String[1];
        ScrimFit.Transform t = ScrimFit.combine(f[0], f[1], f[2], why);
        assertNotNull(why[0], t);
        // the old scalar model (0.80, offset 0) would be 12 levels too dark in green: that is the square Tj sees
        assertTrue(Math.abs(t.o[1] - 0) > 10);
    }
    @Test public void aBigBlockOfWrongSamplesDoesNotMislead() {
        Random r = new Random(3);
        int[] x = wallpaper(r, N), y = scrim(x, 0.80, 4, 0.5, r);
        int panel = (int) (0.18 * N), junk = (int) (0.04 * N);
        for (int i = 0; i < panel; i++) y[i] = 239;                        // the stock panel, a flat light block, was not excluded
        for (int i = panel; i < panel + junk; i++) y[i] = r.nextInt(256);   // clock digits, icons, our own number
        ScrimFit.Result f = ScrimFit.fitChannel(x, y, N);
        assertTrue(f.why, f.ok);
        assertEquals(0.80, f.s, 0.006); assertEquals(4.0, f.o, 1.2);
    }
    @Test public void tooFewSamplesDoNotDecide() {
        Random r = new Random(4);
        int[] x = wallpaper(r, 800), y = scrim(x, 0.8, 0, 0.5, r);
        assertFalse(ScrimFit.fitChannel(x, y, 800).ok);
        assertFalse(ScrimFit.fitChannel(null, null, 0).ok);
    }
    @Test public void aFlatWallpaperCannotTellScaleFromOffset() {
        Random r = new Random(5);
        int[] x = new int[N];
        for (int i = 0; i < N; i++) x[i] = 100 + r.nextInt(4);
        int[] y = scrim(x, 0.8, 0, 0.3, r);
        ScrimFit.Result f = ScrimFit.fitChannel(x, y, N);
        assertFalse(f.ok);
    }
    @Test public void aNoisyMeasurementIsRefused() {
        Random r = new Random(6);
        int[] x = wallpaper(r, N), y = scrim(x, 0.8, 0, 6.0, r);
        assertFalse(ScrimFit.fitChannel(x, y, N).ok);
    }
    @Test public void samplesThatAreNotOneLineAreRefused() {
        Random r = new Random(7);
        int[] x = wallpaper(r, N), y = new int[N];
        for (int i = 0; i < N; i++) y[i] = r.nextInt(256);                  // two unrelated pictures
        assertFalse(ScrimFit.fitChannel(x, y, N).ok);
    }

    // ---- the three channels together
    private static ScrimFit.Result fitOf(double s, double o, long seed) {
        Random r = new Random(seed);
        int[] x = wallpaper(r, N), y = scrim(x, s, o, 0.5, r);
        return ScrimFit.fitChannel(x, y, N);
    }
    @Test public void threeAgreeingChannelsGiveATransform() {
        String[] why = new String[1];
        ScrimFit.Transform t = ScrimFit.combine(fitOf(0.80, 5, 10), fitOf(0.81, 11, 11), fitOf(0.80, 2, 12), why);
        assertNotNull(why[0], t);
        assertEquals(0.80, t.s[0], 0.01); assertEquals(11.0, t.o[1], 1.5);
    }
    @Test public void channelsThatDisagreeAboutTheScaleAreNotAScrim() {
        String[] why = new String[1];
        assertNull(ScrimFit.combine(fitOf(0.70, 0, 20), fitOf(0.80, 0, 21), fitOf(0.90, 0, 22), why));
        assertTrue(why[0].contains("disagree"));
    }
    @Test public void aBrighteningIsNotAScrim() {
        String[] why = new String[1];
        assertNull(ScrimFit.combine(fitOf(1.00, 0, 30), fitOf(1.00, 0, 31), fitOf(1.00, 0, 32), why));   // s = 1: nothing was darkened (the menu was not open)
        assertTrue(why[0].contains("not a darkening"));
    }
    @Test public void aHugeOffsetIsNotAScrim() {
        String[] why = new String[1];
        assertNull(ScrimFit.combine(fitOf(0.80, 50, 40), fitOf(0.80, 50, 41), fitOf(0.80, 50, 42), why));
        assertTrue(why[0].contains("offset"));
    }
    @Test public void aFailedChannelNamesItself() {
        String[] why = new String[1];
        ScrimFit.Result bad = ScrimFit.fitChannel(null, null, 0);
        assertNull(ScrimFit.combine(fitOf(0.8, 0, 50), bad, fitOf(0.8, 0, 51), why));
        assertTrue(why[0].startsWith("green"));
        assertNull(ScrimFit.combine(null, null, null, null));
    }

    // ---- following the stock fade
    @Test public void theStockFadeIsAnInterpolationFromIdentity() {
        ScrimFit.Transform t = new ScrimFit.Transform(new float[] { 0.8f, 0.8f, 0.8f }, new float[] { 5f, 10f, 2f }, 1000, 0.5);
        assertEquals(1f, t.sAt(1, 0f), 1e-6); assertEquals(0f, t.oAt(1, 0f), 1e-6);          // before the fade: the screen as it was
        assertEquals(0.8f, t.sAt(1, 1f), 1e-6); assertEquals(10f, t.oAt(1, 1f), 1e-6);        // complete: the measured scrim
        assertEquals(0.9f, t.sAt(0, 0.5f), 1e-6); assertEquals(2.5f, t.oAt(0, 0.5f), 1e-6);   // half way: half the alpha = half of both
    }
    @Test public void aHalfFadedScrimIsExactlyWhatTheStockMenuDraws() {
        // the stock scrim at alpha a over a pixel L: (1 - a) * L + a * C; the interpolation must give the same number for every a
        double C = 40, aEnd = 0.2, L = 150;
        ScrimFit.Transform t = new ScrimFit.Transform(new float[] { (float) (1 - aEnd), (float) (1 - aEnd), (float) (1 - aEnd) }, new float[] { (float) (aEnd * C), (float) (aEnd * C), (float) (aEnd * C) }, 1000, 0);
        for (double p = 0; p <= 1.0001; p += 0.125) {
            double a = aEnd * p;
            double stock = (1 - a) * L + a * C;
            double ours = t.sAt(0, (float) p) * L + t.oAt(0, (float) p);
            assertEquals(stock, ours, 1e-3);
        }
    }

    // ---- storage
    @Test public void aStoredMeasurementComesBackForTheSameLockScreenOnly() {
        ScrimFit.Transform t = new ScrimFit.Transform(new float[] { 0.8012f, 0.7990f, 0.8031f }, new float[] { 5.25f, 11.5f, 2.0f }, 22000, 0.62);
        String sig = PatchShot.sig(7, 0, 600, 1024);
        String stored = ScrimFit.encode(t, sig, false, 1_700_000_000_000L);
        ScrimFit.Transform back = ScrimFit.decode(stored, sig, false);
        assertNotNull(back);
        for (int c = 0; c < 3; c++) { assertEquals(t.s[c], back.s[c], 1e-4); assertEquals(t.o[c], back.o[c], 1e-4); }
        assertEquals(22000, back.n);
        assertNull(ScrimFit.decode(stored, PatchShot.sig(8, 0, 600, 1024), false));   // another wallpaper
        assertNull(ScrimFit.decode(stored, PatchShot.sig(7, 1, 1024, 600), false));   // another rotation / size
        assertNull(ScrimFit.decode(stored, sig, true));                               // the dark theme: another scrim
    }
    @Test public void garbageNeverDecodes() {
        String sig = PatchShot.sig(7, 0, 600, 1024);
        assertNull(ScrimFit.decode(null, sig, false)); assertNull(ScrimFit.decode("", sig, false)); assertNull(ScrimFit.decode("x#y", sig, false));
        assertNull(ScrimFit.decode(sig + "#d#0.8,0.8#1,2,3#10#0.5#1", sig, false));                // two scales
        assertNull(ScrimFit.decode(sig + "#d#0.8,0.8,abc#1,2,3#10#0.5#1", sig, false));            // not a number
        assertNull(ScrimFit.decode(sig + "#d#1.2,1.2,1.2#1,2,3#10#0.5#1", sig, false));            // a brightening
        assertNull(ScrimFit.decode(sig + "#d#0.8,0.8,0.8#90,2,3#10#0.5#1", sig, false));           // an offset no scrim has
    }
    @Test public void theDescriptionSaysWhatWasMeasured() {
        assertTrue(ScrimFit.describe(null).contains("not measured"));
        ScrimFit.Transform t = new ScrimFit.Transform(new float[] { 0.8f, 0.8f, 0.8f }, new float[] { 5f, 11f, 2f }, 22000, 0.6);
        String d = ScrimFit.describe(t);
        assertTrue(d.contains("0.800") && d.contains("11.0") && d.contains("22000"));
    }
}
