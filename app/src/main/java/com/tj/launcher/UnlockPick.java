package com.tj.launcher;

/**
 * E1 (Tj, 2026-11-04): choose the launcher when unlocking. (N2, same day: the handshakes themselves now live in {@link UnlockMethods}; this class keeps the master switch and the key classifier.) Android has ONE lock credential and compares the whole entry, so a second PIN (or "10001" for a PIN of 1000) is simply a wrong
 * attempt. What an accessibility service CAN do is notice HOW the PIN was entered. Token: tap the Delete key twice on the EMPTY PIN pad before typing the normal PIN = "unlock into
 * Quickstep"; a plain PIN (or fingerprint) = the custom launcher. Keys are recognised by their view id (key0-9, delete_button, key_enter): no digit is ever read, kept or logged.
 * Opt-in (default OFF). Pure rules here (JVM-testable); the service does the watching and the switching.
 */
public final class UnlockPick {
    public static final String KEY = "unlock_pick";            // switch, default OFF
    public static final String LAST_KEY = "unlock_pick_last";   // what the last unlock decided

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
}
