package com.tj.launcher;

/**
 * N4/N5: matches a secret SEQUENCE of key presses (key codes) inside a time window. Any other key press in between breaks it. Pure (JVM-testable); the helper feeds it key-down events
 * (volume keys, Back) and never consumes them.
 */
public final class KeySeq {
    public static final int BACK = 4, VOL_UP = 24, VOL_DOWN = 25;   // android.view.KeyEvent values, copied so this class stays pure

    private final int[] seq;
    private final long windowMs;
    private final int[] codes;
    private final long[] times;
    private int n = 0;

    public KeySeq(long windowMs, int... seq) {
        this.seq = seq; this.windowMs = windowMs;
        this.codes = new int[seq.length]; this.times = new long[seq.length];
    }

    /** @return true when this press completed the sequence (the buffer is then cleared). */
    public boolean onKey(int code, long now) {
        if (n == seq.length) {                         // slide the window left
            System.arraycopy(codes, 1, codes, 0, n - 1);
            System.arraycopy(times, 1, times, 0, n - 1);
            n--;
        }
        codes[n] = code; times[n] = now; n++;
        if (n < seq.length) return false;
        for (int i = 0; i < seq.length; i++) if (codes[i] != seq[i]) return false;
        if (times[n - 1] - times[0] > windowMs) return false;
        n = 0;
        return true;
    }

    public void reset() { n = 0; }
}
