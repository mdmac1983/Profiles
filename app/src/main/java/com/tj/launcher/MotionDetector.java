package com.tj.launcher;

import java.util.ArrayList;
import java.util.List;

/**
 * O1: two motion handshakes on the lock screen, from the accelerometer (no permission needed). Pure (JVM-testable): the service feeds samples while the lock screen is up and one of them is selected.
 *  SHAKE  three firm jolts (peak change in the acceleration magnitude of at least 11 m/s2) within 2 s, each at least 120 ms after the last
 *  KNOCK  two sharp but small jolts (3.5 to 10.5 m/s2 above the running baseline) 150-700 ms apart, with NOTHING similar in the 500 ms before the first or after the second
 *         (walking or a bumpy ride makes jolts every ~0.5 s, so it never has that quiet lead-in; a finger tap on a held tablet is under 2 m/s2)
 * The numbers are a first guess from general knowledge of phone sensors; they cannot be tuned without the tablet, so both are labelled experimental and default OFF.
 */
public final class MotionDetector {
    public enum Kind { NONE, SHAKE, KNOCK }

    public static final float SHAKE_MIN = 11f, KNOCK_MIN = 3.5f, KNOCK_MAX = 10.5f;
    public static final long PEAK_MS = 80, SHAKE_WINDOW_MS = 2_000, SHAKE_GAP_MS = 120, KNOCK_GAP_MIN_MS = 150, KNOCK_GAP_MAX_MS = 700, KNOCK_QUIET_MS = 500;

    private static final class Peak { long t; float max; boolean done; }

    private float base = 9.81f;
    private boolean baseInit = false;
    private final List<Peak> peaks = new ArrayList<>();

    public void reset() { peaks.clear(); baseInit = false; }

    /** Feed one accelerometer sample (m/s2) with a millisecond timestamp; returns what was recognised by this sample (usually NONE). */
    public Kind onSample(float x, float y, float z, long tMs) {
        float mag = (float) Math.sqrt(x * x + y * y + z * z);
        if (!baseInit) { base = mag; baseInit = true; }
        float delta = Math.abs(mag - base);
        base += 0.04f * (mag - base);
        if (delta >= KNOCK_MIN) {
            Peak last = peaks.isEmpty() ? null : peaks.get(peaks.size() - 1);
            if (last != null && !last.done && tMs - last.t <= PEAK_MS) { if (delta > last.max) last.max = delta; }
            else { Peak p = new Peak(); p.t = tMs; p.max = delta; peaks.add(p); }
        }
        for (Peak p : peaks) if (!p.done && tMs - p.t > PEAK_MS) p.done = true;
        while (!peaks.isEmpty() && tMs - peaks.get(0).t > 6_000) peaks.remove(0);
        return evaluate(tMs);
    }

    private Kind evaluate(long now) {
        List<Peak> done = new ArrayList<>();
        for (Peak p : peaks) if (p.done) done.add(p);
        int n = done.size();
        // SHAKE: three firm, separated peaks inside 2 s
        List<Peak> firm = new ArrayList<>();
        for (Peak p : done) if (p.max >= SHAKE_MIN && now - p.t <= SHAKE_WINDOW_MS) firm.add(p);
        if (firm.size() >= 3) {
            boolean spaced = true;
            for (int i = 1; i < firm.size(); i++) if (firm.get(i).t - firm.get(i - 1).t < SHAKE_GAP_MS) spaced = false;
            if (spaced) { peaks.clear(); return Kind.SHAKE; }
        }
        // KNOCK: the last two finished peaks are small, sharp, 150-700 ms apart, quiet BEFORE the first and quiet for 500 ms AFTER the second (so the verdict comes 0.5 s after the knock)
        if (n >= 2) {
            Peak b = done.get(n - 1), a = done.get(n - 2);
            boolean small = a.max >= KNOCK_MIN && a.max <= KNOCK_MAX && b.max >= KNOCK_MIN && b.max <= KNOCK_MAX;
            long gap = b.t - a.t;
            long since = now - b.t;
            if (small && gap >= KNOCK_GAP_MIN_MS && gap <= KNOCK_GAP_MAX_MS && since >= KNOCK_QUIET_MS && since <= KNOCK_QUIET_MS + 300) {
                boolean quiet = true;
                for (int i = 0; i < n - 2; i++) if (a.t - done.get(i).t <= KNOCK_QUIET_MS) quiet = false;
                for (Peak p : peaks) if (p.t > b.t && p.t - b.t <= KNOCK_QUIET_MS) quiet = false;
                if (quiet) { peaks.clear(); return Kind.KNOCK; }
            }
        }
        return Kind.NONE;
    }
}
