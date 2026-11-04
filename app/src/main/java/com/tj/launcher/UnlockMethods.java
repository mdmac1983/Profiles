package com.tj.launcher;

import java.util.ArrayDeque;

/**
 * N2 (Tj, 2026-11-04): "multiple creative options on how to get to quickstep from the lock screen ... not too hard or long to execute, but also unlikely to happen by accident".
 * The tablet has ONE lock credential, so the PIN cannot choose the launcher. What the accessibility helper CAN do is notice a SECRET HANDSHAKE done on the lock screen.
 * No handshake = the plain PIN opens the custom launcher (the default). Quickstep exists only behind a handshake.
 *
 * Six independent methods, each with its own switch (Launcher control > Lock screen). Every one is anchored to a quiet lock screen and needs a shape nobody produces by accident:
 *   ERASE2     on the EMPTY PIN pad tap Delete twice, then type the PIN                       (the original; a typo correction never counts: a digit came first)
 *   TAP_ERASE  on the empty pad tap any digit and erase it, three times, then type the PIN      (digit, Delete, digit, Delete, digit, Delete)
 *   VOLUME     press Volume Up, Up, Down, Down within 6 seconds on the lock screen, then the PIN (nets out to the same volume)
 *   SIDEKEY    press the side key four times quickly (off, on, off, on = 4 changes in 4 s), then the PIN
 *   PEEK       pull the PIN pad up and push it away three times in 15 s, leave it up the third time, then the PIN
 *   VOLUP_ERASE two steps: tap Volume Up once, then within 10 s tap Delete twice on the empty pad, then the PIN
 * Privacy: keys are only ever classified as DIGIT / DELETE / ENTER by their view id; WHICH digit is never read, kept or logged. Pure rules (JVM-testable); the service feeds the events.
 */
public final class UnlockMethods {
    public enum Method {
        ERASE2("erase2", "Double erase", "On the empty PIN pad tap Delete twice, then type your PIN.", true),
        TAP_ERASE("taperase", "Tap and erase", "On the empty pad tap any digit then Delete, three times in a row, then type your PIN.", false),
        VOLUME("volume", "Volume code", "On the lock screen press Volume Up, Up, Down, Down within 6 seconds, then the PIN.", false),
        SIDEKEY("sidekey", "Side-key flurry", "Press the side key 4 times quickly (screen off, on, off, on), then the PIN.", false),
        PEEK("peek", "Peek three times", "Pull the PIN pad up and push it away, three times within 15 seconds (leave it up the third time), then the PIN.", false),
        VOLUP_ERASE("volerase", "Volume Up + double erase", "Tap Volume Up once, then within 10 seconds tap Delete twice on the empty pad, then the PIN.", false);

        public final String id, title, how;
        public final boolean defaultOn;
        Method(String id, String title, String how, boolean defaultOn) { this.id = id; this.title = title; this.how = how; this.defaultOn = defaultOn; }
        public String prefKey() { return "unlock_m_" + id; }
    }

    public static final long VALID_PAD_MS = 8_000;          // a pad-method latch made at Enter counts only this long (a wrong PIN never unlocks)
    public static final long VALID_FREE_MS = 90_000;        // a latch made away from the pad (volume / side key / peek) lasts this long
    public static final long VOLUME_WINDOW_MS = 6_000, SIDEKEY_WINDOW_MS = 4_000, PEEK_WINDOW_MS = 15_000, VOLUP_ERASE_WINDOW_MS = 10_000;

    /** Which methods are switched on; the service refreshes this from the stored switches. */
    public interface Enabled { boolean on(Method m); }

    public static final Enabled ALL = new Enabled() { @Override public boolean on(Method m) { return true; } };

    private final Enabled enabled;
    public UnlockMethods(Enabled enabled) { this.enabled = enabled; }

    // ---- pad session: what was tapped since this PIN pad appeared. 'N' = digit, 'D' = delete.
    private final StringBuilder pad = new StringBuilder();
    // ---- free detectors
    private final long[] vol = new long[4];                 // last 4 volume events: +time for Up, -time for Down
    private int volN = 0;
    private final ArrayDeque<Long> screenChanges = new ArrayDeque<>();
    private final ArrayDeque<Long> peeks = new ArrayDeque<>();
    private long lastVolUpAt = 0;
    // ---- the latch
    private Method armed = null;
    private long armedAt = 0;
    private boolean armedAtPad = false;                     // pad methods latch at Enter, free methods at once
    private Method padMatch = null;                         // the pad method the CURRENT pad entry matches so far (decided at Enter)
    private boolean screenOn = true;

    /** Listener for "the secret was just accepted / withdrawn". Called from the thread that feeds the events. */
    public interface Listener { void onArmed(Method m, boolean padMethodPendingEnter); void onDisarmed(); }
    private Listener listener;
    public void setListener(Listener l) { listener = l; }

    public Method armedMethod() { return armed; }

    /** Forget everything of the previous lock session (screen off, or the unlock was handled). */
    public void reset() {
        pad.setLength(0); volN = 0; peeks.clear(); lastVolUpAt = 0;
        disarm();
    }

    private void disarm() {
        boolean was = armed != null;
        armed = null; armedAt = 0; armedAtPad = false; padMatch = null;
        if (was && listener != null) listener.onDisarmed();
    }

    // ------------------------------------------------------------------ PIN pad
    /** The PIN pad (bouncer) appeared or went away. Appearing starts a fresh pad session and counts as one "peek". */
    public void onPad(boolean shown, long now) {
        pad.setLength(0);
        padMatch = null;
        if (shown) {
            peeks.addLast(now);
            while (!peeks.isEmpty() && now - peeks.peekFirst() > PEEK_WINDOW_MS) peeks.pollFirst();
            if (peeks.size() >= 3 && enabled.on(Method.PEEK)) arm(Method.PEEK, now, false);
        }
    }

    public void onKey(UnlockPick.Key k, long now) {
        if (k == UnlockPick.Key.DIGIT) pad.append('N');
        else if (k == UnlockPick.Key.DELETE) pad.append('D');
        if (pad.length() > 12) pad.setLength(12);
    }

    /** Enter was tapped: decide which pad method (if any) the entry just submitted matches, latch it, and start a fresh attempt (a wrong PIN leaves the pad empty again). */
    public Method onEnter(long now) {
        String p = pad.toString();
        Method m = null;
        if (enabled.on(Method.TAP_ERASE) && p.startsWith("NDNDND")) m = Method.TAP_ERASE;
        else if (enabled.on(Method.VOLUP_ERASE) && p.startsWith("DD") && lastVolUpAt > 0 && now - lastVolUpAt <= VOLUP_ERASE_WINDOW_MS + 30_000) m = Method.VOLUP_ERASE;   // Enter comes after typing the PIN: allow a slow typist
        else if (enabled.on(Method.ERASE2) && p.startsWith("DD")) m = Method.ERASE2;
        pad.setLength(0);
        if (m != null) arm(m, now, true);
        else if (armedAtPad) disarm();     // a later plain attempt withdraws the earlier pad latch; free latches (volume, side key, peek) stay
        return m;
    }

    // ------------------------------------------------------------------ volume keys (lock screen only: the service checks)
    public void onVolume(boolean up, long now) {
        if (up) lastVolUpAt = now;
        // shift in
        if (volN == vol.length) { System.arraycopy(vol, 1, vol, 0, vol.length - 1); volN--; }
        vol[volN++] = up ? now : -now;
        if (volN == 4 && enabled.on(Method.VOLUME)) {
            boolean shape = vol[0] > 0 && vol[1] > 0 && vol[2] < 0 && vol[3] < 0;
            long first = Math.abs(vol[0]), last = Math.abs(vol[3]);
            if (shape && last - first <= VOLUME_WINDOW_MS) { volN = 0; arm(Method.VOLUME, now, false); }
        }
    }

    // ------------------------------------------------------------------ screen on / off (side key)
    public void onScreen(boolean on, long now) {
        screenOn = on;
        screenChanges.addLast(now);
        while (!screenChanges.isEmpty() && now - screenChanges.peekFirst() > SIDEKEY_WINDOW_MS) screenChanges.pollFirst();
        if (!on) {                     // the screen going dark ends the lock session: pad, volume code, peeks and any free latch are gone
            pad.setLength(0); volN = 0; peeks.clear(); lastVolUpAt = 0;
            disarm();
        } else if (screenChanges.size() >= 4 && enabled.on(Method.SIDEKEY)) {
            screenChanges.clear();
            arm(Method.SIDEKEY, now, false);
        }
    }

    // ------------------------------------------------------------------ latch
    private void arm(Method m, long now, boolean padLatch) {
        armed = m; armedAt = now; armedAtPad = padLatch;
        if (listener != null) listener.onArmed(m, padLatch);
    }

    /** The unlock succeeded: which method was just used (null = plain unlock)? Consumes the latch. */
    public Method consume(long now) {
        Method m = armed;
        if (m == null) return null;
        long valid = armedAtPad ? VALID_PAD_MS : VALID_FREE_MS;
        boolean fresh = now - armedAt <= valid;
        armed = null; armedAt = 0; armedAtPad = false;
        return fresh ? m : null;
    }

    public boolean screenIsOn() { return screenOn; }
}
