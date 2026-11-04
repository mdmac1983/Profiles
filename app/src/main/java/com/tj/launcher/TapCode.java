package com.tj.launcher;

/**
 * N5: the hidden Solitaire hotspot. FIVE quick taps (each shorter than 350 ms, each within 900 ms of the one before) and then a sixth touch HELD for 800 ms = the master failsafe into
 * Quickstep. Pure (JVM-testable): the overlay view feeds touch-down / touch-up times; the service starts the 800 ms timer when {@link #onDown} says the hold is armed.
 */
public final class TapCode {
    public static final int TAPS = 5;
    public static final long TAP_MAX_MS = 350, GAP_MAX_MS = 900, HOLD_MS = 800;

    private int taps = 0;
    private long lastTapUp = 0, downAt = 0;

    /** Touch down. @return true if this touch must now be HELD for HOLD_MS to complete the code (the caller starts its timer). */
    public boolean onDown(long now) {
        downAt = now;
        if (taps > 0 && now - lastTapUp > GAP_MAX_MS) taps = 0;      // too slow: start over
        return taps >= TAPS;
    }

    /** Touch up (or cancel). A short press counts as a tap; a long one that was not the armed hold starts over. */
    public void onUp(long now) {
        long held = now - downAt;
        if (held <= TAP_MAX_MS) { taps++; lastTapUp = now; if (taps > TAPS) taps = TAPS; }
        else taps = 0;                                                // released the hold early or a long press at the wrong moment
    }

    public int taps() { return taps; }
    public void reset() { taps = 0; lastTapUp = 0; downAt = 0; }
}
