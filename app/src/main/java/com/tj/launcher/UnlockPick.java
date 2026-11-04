package com.tj.launcher;

/**
 * E1 (Tj, 2026-11-04): choose the launcher when unlocking. Android has ONE lock credential and compares the whole entry, so a second PIN (or "10001" for a PIN of 1000) is simply a wrong
 * attempt. What an accessibility service CAN do is notice HOW the PIN was entered. Token: tap the Delete key twice on the EMPTY PIN pad before typing the normal PIN = "unlock into
 * Quickstep"; a plain PIN (or fingerprint) = the custom launcher. Keys are recognised by their view id (key0-9, delete_button, key_enter): no digit is ever read, kept or logged.
 * Opt-in (default OFF). Pure rules here (JVM-testable); the service does the watching and the switching.
 */
public final class UnlockPick {
    public static final String KEY = "unlock_pick";            // switch, default OFF
    public static final String LAST_KEY = "unlock_pick_last";   // what the last unlock decided
    public static final int DELETES_REQUIRED = 2;
    public static final long VALID_MS = 8_000;                  // a token latched at Enter counts only for this long (a wrong PIN never unlocks)

    public enum Key { DIGIT, DELETE, ENTER }

    private UnlockPick() {}

    /** The kind of PIN-pad key a view id belongs to, or null if it is not one. */
    public static Key classify(String viewId) {
        if (viewId == null) return null;
        if (viewId.endsWith(":id/delete_button")) return Key.DELETE;
        if (viewId.endsWith(":id/key_enter")) return Key.ENTER;
        int i = viewId.lastIndexOf(":id/key");
        if (i >= 0 && viewId.length() == i + 8 && Character.isDigit(viewId.charAt(i + 7))) return Key.DIGIT;
        return null;
    }

    /** Tracks one PIN-pad session. Deletes only count while NO digit has been tapped since the pad appeared (so correcting a typo never triggers it). */
    public static final class Token {
        private int deletes = 0;
        private boolean sawDigit = false;
        private boolean requested = false;
        private long requestedAt = 0;

        public void reset() { deletes = 0; sawDigit = false; requested = false; requestedAt = 0; }

        public void onKey(Key k) {
            if (k == Key.DIGIT) sawDigit = true;
            else if (k == Key.DELETE && !sawDigit) deletes++;
        }

        /** Enter tapped: latch whether the token was given, then start a fresh attempt (a wrong PIN leaves the pad empty again). */
        public void onEnter(long now) {
            requested = deletes >= DELETES_REQUIRED;
            requestedAt = now;
            deletes = 0; sawDigit = false;
        }

        /** The unlock succeeded: was the token given just before, and is it still fresh? Consumes it. */
        public boolean consume(long now) {
            boolean r = requested && now - requestedAt <= VALID_MS;
            requested = false;
            return r;
        }
    }
}
