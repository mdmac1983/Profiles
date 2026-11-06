package com.tj.launcher;

/**
 * AD4 (Tj, 2026-11-06: "the hidden solitaire box to get to quickstep doesn't work. Think of and build a different secret, clever way to get to quickstep from inside the custom launcher without any hardware button
 * presses or accelerometer use. Better yet, give me several options I can toggle on and off in the launcher settings ... as long as they are unlikely to be triggered by accident").
 * This supersedes N3a (2026-11-04: every way into Quickstep from the custom launcher was removed) for these HIDDEN, individually switchable entrances. The lock-screen handshakes ({@link UnlockMethods}) and the old
 * Solitaire hotspot stay as they are. Each entrance only works in the CUSTOM launcher with the tablet unlocked, and goes through {@link ProfileSession#goToQuickstep} (no shell helper needed).
 *
 * Pure rules (JVM-tested): the three detectors and the swipe classifier. {@link EntranceWatcher} feeds them from the real world.
 */
public final class SecretEntrances {
    private SecretEntrances() {}

    public enum Method {
        FLICK("flick", "Page flick", "On the home screen swipe left, right, left, right, left, right: six quick swipes within 8 seconds (each at least 40% of the screen wide).", true),
        ROTATE("rotate_tile", "Rotation tile", "Open the quick settings and tap the Auto-rotate tile five times, fast (within 10 seconds).", true),
        CHARGER("charger", "Charger flick", "With the tablet unlocked, plug the charger in, pull it out and plug it in again within 8 seconds.", false);

        public final String id, title, how;
        public final boolean defaultOn;
        Method(String id, String title, String how, boolean defaultOn) { this.id = id; this.title = title; this.how = how; this.defaultOn = defaultOn; }
        public String prefKey() { return "entrance_" + id; }
    }

    // ------------------------------------------------------------------ page flick
    /** Finger moved right = +1, left = -1, anything else (too short, too slow, too diagonal) = 0. */
    public static int swipeDirection(float dx, float dy, long durationMs, int screenWidthPx) {
        if (screenWidthPx <= 0 || durationMs <= 0 || durationMs > Flick.SWIPE_MAX_MS) return 0;
        float ax = Math.abs(dx), ay = Math.abs(dy);
        if (ax < Flick.MIN_FRACTION * screenWidthPx || ay > 0.5f * ax) return 0;
        return dx > 0 ? 1 : -1;
    }

    /** Six swipes that ALTERNATE in direction, each within 2.5 s of the one before, all inside 8 s. A swipe that repeats the direction (or comes too late) starts the count over from itself. */
    public static final class Flick {
        public static final int SWIPES = 6;
        public static final long WINDOW_MS = 8_000, GAP_MAX_MS = 2_500, SWIPE_MAX_MS = 800;
        public static final float MIN_FRACTION = 0.40f;

        private final long[] at = new long[SWIPES];
        private final int[] dir = new int[SWIPES];
        private int n = 0;

        /** @return true when this swipe completes the code (the detector then starts over) */
        public boolean onSwipe(int direction, long now) {
            if (direction == 0) return false;
            if (n > 0 && (now - at[n - 1] > GAP_MAX_MS || direction == dir[n - 1])) n = 0;
            if (n == SWIPES) { System.arraycopy(at, 1, at, 0, SWIPES - 1); System.arraycopy(dir, 1, dir, 0, SWIPES - 1); n = SWIPES - 1; }   // a longer back-and-forth keeps the LAST six
            at[n] = now; dir[n] = direction; n++;
            if (n == SWIPES && now - at[0] <= WINDOW_MS) { n = 0; return true; }
            return false;
        }

        public void reset() { n = 0; }
        public int count() { return n; }
    }

    // ------------------------------------------------------------------ the Auto-rotate tile (or any change of the rotation lock)
    /** Five changes of the rotation setting inside 10 s. Changes closer than 120 ms are one change (a single tap can notify twice). */
    public static final class Toggles {
        public static final int COUNT = 5;
        public static final long WINDOW_MS = 10_000, MIN_GAP_MS = 120;

        private final long[] at = new long[COUNT];
        private int n = 0;

        public boolean onChange(long now) {
            if (n > 0 && now - at[n - 1] < MIN_GAP_MS) return false;
            int keep = 0;                                           // drop everything older than the window
            for (int i = 0; i < n; i++) if (now - at[i] <= WINDOW_MS) at[keep++] = at[i];
            n = keep;
            if (n == COUNT) { System.arraycopy(at, 1, at, 0, COUNT - 1); n = COUNT - 1; }
            at[n++] = now;
            if (n == COUNT && now - at[0] <= WINDOW_MS) { n = 0; return true; }
            return false;
        }

        public void reset() { n = 0; }
        public int count() { return n; }
    }

    // ------------------------------------------------------------------ the charger
    /** Plugged in, pulled out, plugged in again: all three inside 8 s. Anything out of order starts over (a "plugged in" starts a new attempt). */
    public static final class Plug {
        public static final long WINDOW_MS = 8_000;

        private int step = 0;        // 0 = waiting for the first "in", 1 = waiting for "out", 2 = waiting for the last "in"
        private long first = 0;

        public boolean onPower(boolean connected, long now) {
            if (step > 0 && now - first > WINDOW_MS) step = 0;
            switch (step) {
                case 0: if (connected) { step = 1; first = now; } return false;
                case 1: if (!connected) step = 2; else first = now; return false;     // "in" again without an "out": that "in" is the new first one
                default:
                    if (connected) { step = 0; return true; }
                    return false;                                                       // a second "out" in a row changes nothing
            }
        }

        public void reset() { step = 0; first = 0; }
    }
}
