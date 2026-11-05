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
        VOLUP_ERASE("volerase", "Volume Up + double erase", "Tap Volume Up once, then within 10 seconds tap Delete twice on the empty pad, then the PIN.", false),
        // ---- O1 (Tj, 2026-11-04: "even more options ... clever ways. Gestures, volume buttons, key combinations")
        VOLBOTH("volboth", "Both volume keys together", "Press Volume Up and Volume Down at the same moment on the lock screen, then the PIN.", false),
        PINGPONG("pingpong", "Volume ping-pong", "Volume Up, Down, Up, Down, Up, Down within 6 seconds on the lock screen, then the PIN.", false),
        VOLHOLD("volhold", "Hold, then tap", "Hold Volume Down for about 3 seconds, let go, then tap Volume Up within 4 seconds, then the PIN.", false),
        BACKS_UP("backsup", "Three Backs + Volume Up", "Press Back three times, then Volume Up, within 6 seconds (Back closes the PIN pad: pull it up again afterwards), then the PIN.", false),
        LADDER("ladder", "Erase ladder", "On the empty pad: Delete, digit, Delete, digit, Delete, digit (any digits), then type your PIN.", false),
        PEEK5("peek5", "Peek five times", "Pull the PIN pad up and push it away five times within 25 seconds (leave it up the fifth time), then the PIN.", false),
        CHARGER("charger", "Charger flick", "Plug the charger in, pull it out, plug it in again within 8 seconds on the lock screen, then the PIN.", false),
        ROTATE("rotate", "Tablet twirl", "Turn the tablet to landscape, back to portrait, landscape, portrait within 8 seconds (auto-rotate on), then the PIN.", false),
        SHAKE("shake", "Triple shake (experimental)", "Shake the tablet firmly three times on the lock screen, then the PIN. Thresholds are a first guess: tell me if it is too touchy or too stiff.", false),
        KNOCK("knock", "Double knock (experimental)", "Knock twice on the back of the tablet with your knuckle, about half a second apart, on the lock screen, then the PIN. Thresholds are a first guess.", false);

        public final String id, title, how;
        public final boolean defaultOn;
        Method(String id, String title, String how, boolean defaultOn) { this.id = id; this.title = title; this.how = how; this.defaultOn = defaultOn; }
        public String prefKey() { return "unlock_m_" + id; }
    }

    public static final long VALID_PAD_MS = 8_000;          // a pad-method latch made at Enter counts only this long (a wrong PIN never unlocks)
    /**
     * AC7 (Tj, 2026-11-05: "a couple times I did the lock screen gesture to unlock to quickstep but it opened the custom launcher"): a typo in the PIN after the gesture must not lose the gesture.
     * A PLAIN Enter this soon after a pad latch (the first attempt, which did not unlock = a wrong PIN) keeps the latch and refreshes it; later than this it withdraws it as before.
     */
    public static final long RETRY_GRACE_MS = 20_000;
    public static final long VALID_FREE_MS = 90_000;        // a latch made away from the pad (volume / side key / peek) lasts this long
    public static final long VOLUME_WINDOW_MS = 6_000, SIDEKEY_WINDOW_MS = 4_000, PEEK_WINDOW_MS = 15_000, VOLUP_ERASE_WINDOW_MS = 10_000;
    public static final long PEEK5_WINDOW_MS = 25_000, CHARGER_WINDOW_MS = 8_000, ROTATE_WINDOW_MS = 8_000, HOLD_TAP_MS = 4_000;
    public static final int HOLD_REPEATS = 40;               // ~3 s of Volume Down auto-repeat (first repeat after ~0.5 s, then every ~50 ms)

    /** The methods that are effectively ON: the stored switches, but if every one is off the original (double erase) stays on, so Quickstep can never become unreachable. */
    public static java.util.List<Method> selected(Enabled stored) {
        java.util.List<Method> out = new java.util.ArrayList<>();
        for (Method m : Method.values()) if (stored.on(m)) out.add(m);
        if (out.isEmpty()) out.add(Method.ERASE2);
        return out;
    }

    /** True if any method that needs the hardware-key access is selected. */
    public static boolean needsKeys(Enabled stored) {
        for (Method m : selected(stored))
            if (m == Method.VOLUME || m == Method.VOLUP_ERASE || m == Method.VOLBOTH || m == Method.PINGPONG || m == Method.VOLHOLD || m == Method.BACKS_UP) return true;
        return false;
    }

    public static boolean needsMotion(Enabled stored) {
        for (Method m : selected(stored)) if (m == Method.SHAKE || m == Method.KNOCK) return true;
        return false;
    }

    /** Which methods are switched on; the service refreshes this from the stored switches. */
    public interface Enabled { boolean on(Method m); }

    public static final Enabled ALL = new Enabled() { @Override public boolean on(Method m) { return true; } };

    private final Enabled enabled;
    public UnlockMethods(Enabled enabled) { this.enabled = enabled; }

    // ---- pad session: what was tapped since this PIN pad appeared. 'N' = digit, 'D' = delete.
    private final StringBuilder pad = new StringBuilder();
    // ---- free detectors
    private final long[] vol = new long[4];                 // last 4 volume events: time
    private final boolean[] volUp = new boolean[4];         // ... and whether each was Up
    private int volN = 0;
    private final ArrayDeque<Long> screenChanges = new ArrayDeque<>();
    private final ArrayDeque<Long> peeks = new ArrayDeque<>();
    private long lastVolUpAt = 0, firstDeleteAt = 0, lastVolDownAt = 0, heldAt = 0;
    private boolean volDownHeld = false;
    private final KeySeq pingPong = new KeySeq(VOLUME_WINDOW_MS, KeySeq.VOL_UP, KeySeq.VOL_DOWN, KeySeq.VOL_UP, KeySeq.VOL_DOWN, KeySeq.VOL_UP, KeySeq.VOL_DOWN);
    private final KeySeq backsUp = new KeySeq(VOLUME_WINDOW_MS, KeySeq.BACK, KeySeq.BACK, KeySeq.BACK, KeySeq.VOL_UP);
    private final ArrayDeque<Long> power = new ArrayDeque<>();
    private final ArrayDeque<Long> rotations = new ArrayDeque<>();
    private boolean volUpBeforeErase = false;               // Volume Up came within 10 s BEFORE the first Delete of this pad entry
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
        pad.setLength(0); dismissedPad = null; volN = 0; peeks.clear(); lastVolUpAt = 0; volUpBeforeErase = false;
        lastVolDownAt = 0; volDownHeld = false; heldAt = 0; pingPong.reset(); backsUp.reset(); power.clear(); rotations.clear();
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
        // The pad going away right after typing = the Enter that unlocked: the keyguard tears its keys down before the Enter click can be recognised, so remember what was typed.
        if (!shown && pad.length() > 0) { dismissedPad = pad.toString(); dismissedAt = now; }
        pad.setLength(0);
        padMatch = null; volUpBeforeErase = false;
        if (shown) {
            peeks.addLast(now);
            while (!peeks.isEmpty() && now - peeks.peekFirst() > PEEK5_WINDOW_MS) peeks.pollFirst();
            int recent = 0;
            for (long t : peeks) if (now - t <= PEEK_WINDOW_MS) recent++;
            if (peeks.size() >= 5 && enabled.on(Method.PEEK5)) { peeks.clear(); arm(Method.PEEK5, now, false); }
            else if (recent >= 3 && enabled.on(Method.PEEK)) arm(Method.PEEK, now, false);
        }
    }

    /**
     * AC1/AC7: the first (at most two) key KINDS of the pad entry so far, e.g. "DD" (two Deletes) or "NN" (digits), for the persistent lock log. Never a digit, never more than two kinds: the length of the PIN is not revealed.
     */
    public String padLead() {
        String p = pad.toString();
        return p.length() > 2 ? p.substring(0, 2) : p;
    }

    /** True while pad taps were fed but no Enter was processed (the Enter click arrives after the keyguard tore its key down and cannot be recognised): the unlock itself then stands in for Enter. */
    public boolean hasPendingPad() { return pad.length() > 0 || dismissedPad != null; }

    /** The tablet just unlocked with pad taps never submitted by a recognised Enter: treat them as the entry that unlocked it (dismissed within 3 s). Returns the matched method or null. */
    public Method finishPendingPad(long now) {
        if (pad.length() == 0 && dismissedPad != null && now - dismissedAt <= 3000) pad.append(dismissedPad);
        dismissedPad = null;
        return pad.length() > 0 ? onEnter(now) : null;
    }
    private String dismissedPad; private long dismissedAt;

    public void onKey(UnlockPick.Key k, long now) {
        if (k == UnlockPick.Key.DIGIT) pad.append('N');
        else if (k == UnlockPick.Key.DELETE) {
            pad.append('D');
            if (pad.length() == 1) {
                firstDeleteAt = now;
                volUpBeforeErase = lastVolUpAt > 0 && lastVolUpAt <= now && now - lastVolUpAt <= VOLUP_ERASE_WINDOW_MS;
            }
        }
        if (pad.length() > 12) pad.setLength(12);
    }

    /** Enter was tapped: decide which pad method (if any) the entry just submitted matches, latch it, and start a fresh attempt (a wrong PIN leaves the pad empty again). */
    public Method onEnter(long now) {
        String p = pad.toString();
        Method m = null;
        if (enabled.on(Method.TAP_ERASE) && p.startsWith("NDNDND")) m = Method.TAP_ERASE;
        else if (enabled.on(Method.VOLUP_ERASE) && p.startsWith("DD") && volUpBeforeErase) m = Method.VOLUP_ERASE;
        else if (enabled.on(Method.LADDER) && p.startsWith("DNDNDN")) m = Method.LADDER;
        else if (enabled.on(Method.ERASE2) && p.startsWith("DD")) m = Method.ERASE2;
        pad.setLength(0); volUpBeforeErase = false;
        if (m != null) arm(m, now, true);
        else if (armedAtPad) {
            if (now - armedAt <= RETRY_GRACE_MS) armedAt = now;   // AC7: the retry after a wrong PIN inherits the gesture (a correct PIN would have unlocked within a second or two)
            else disarm();                                        // a plain attempt long after the gesture withdraws the pad latch; free latches (volume, side key, peek) stay
        }
        return m;
    }

    // ------------------------------------------------------------------ volume keys (lock screen only: the service checks)
    public void onVolume(boolean up, long now) {
        if (up) lastVolUpAt = now;
        if (volN == vol.length) {
            System.arraycopy(vol, 1, vol, 0, vol.length - 1);
            System.arraycopy(volUp, 1, volUp, 0, volUp.length - 1);
            volN--;
        }
        vol[volN] = now; volUp[volN] = up; volN++;
        if (volN == 4 && enabled.on(Method.VOLUME)) {
            boolean shape = volUp[0] && volUp[1] && !volUp[2] && !volUp[3];
            if (shape && vol[3] - vol[0] <= VOLUME_WINDOW_MS) { volN = 0; arm(Method.VOLUME, now, false); }
        }
    }

    /**
     * Hardware key on the lock screen (key-DOWN, repeats included for Volume Down). Feeds: both volume keys together, ping-pong, hold-then-tap, three Backs + Volume Up.
     * (The plain volume-code and Volume Up + erase detectors keep their own {@link #onVolume}.)
     */
    public void onHwKey(int code, int repeatCount, long now) {
        if (code == KeySeq.VOL_DOWN && repeatCount >= HOLD_REPEATS) { volDownHeld = true; heldAt = now; return; }
        if (repeatCount != 0) return;
        if (code == KeySeq.VOL_UP) {
            if (volDownHeld && now - heldAt <= HOLD_TAP_MS && enabled.on(Method.VOLHOLD)) { volDownHeld = false; arm(Method.VOLHOLD, now, false); }
            lastVolUpAt = now;
        } else if (code == KeySeq.VOL_DOWN) {
            lastVolDownAt = now;
        }
        if ((code == KeySeq.VOL_UP || code == KeySeq.VOL_DOWN) && enabled.on(Method.VOLBOTH) && ReturnMethods.bothVolume(lastVolUpAt, lastVolDownAt, now)) {
            lastVolUpAt = lastVolDownAt = 0; arm(Method.VOLBOTH, now, false);
        }
        if (enabled.on(Method.PINGPONG) && pingPong.onKey(code, now)) arm(Method.PINGPONG, now, false);
        if (enabled.on(Method.BACKS_UP) && backsUp.onKey(code, now)) arm(Method.BACKS_UP, now, false);
    }

    /** The charger was plugged in / pulled out while the lock screen is up: connected, disconnected, connected within 8 s. */
    public void onPower(boolean connected, long now) {
        power.addLast(connected ? now : -now);
        while (power.size() > 3) power.pollFirst();
        if (power.size() == 3 && enabled.on(Method.CHARGER)) {
            Long[] a = power.toArray(new Long[0]);
            boolean shape = a[0] > 0 && a[1] < 0 && a[2] > 0;
            if (shape && Math.abs(a[2]) - Math.abs(a[0]) <= CHARGER_WINDOW_MS) { power.clear(); arm(Method.CHARGER, now, false); }
        }
    }

    /** The screen orientation changed while the lock screen is up: four changes within 8 s. */
    public void onRotation(long now) {
        rotations.addLast(now);
        while (!rotations.isEmpty() && now - rotations.peekFirst() > ROTATE_WINDOW_MS) rotations.pollFirst();
        if (rotations.size() >= 4 && enabled.on(Method.ROTATE)) { rotations.clear(); arm(Method.ROTATE, now, false); }
    }

    /** A motion pattern the sensor detector recognised ({@link MotionDetector}). */
    public void onMotion(Method m, long now) {
        if ((m == Method.SHAKE || m == Method.KNOCK) && enabled.on(m)) arm(m, now, false);
    }

    // ------------------------------------------------------------------ screen on / off (side key)
    public void onScreen(boolean on, long now) {
        screenOn = on;
        screenChanges.addLast(now);
        while (!screenChanges.isEmpty() && now - screenChanges.peekFirst() > SIDEKEY_WINDOW_MS) screenChanges.pollFirst();
        if (!on) {                     // the screen going dark ends the lock session: pad, volume code, peeks and any free latch are gone
            pad.setLength(0); volN = 0; peeks.clear(); lastVolUpAt = 0; volUpBeforeErase = false;
            lastVolDownAt = 0; volDownHeld = false; heldAt = 0; pingPong.reset(); backsUp.reset(); power.clear(); rotations.clear();
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
