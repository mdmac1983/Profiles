package com.tj.launcher;

import android.content.Context;

/**
 * N5 (Tj, 2026-11-04): the MASTER FAILSAFE into Quickstep that does not depend on the lock-screen handshake. Ladder, easiest first (full text: research/PROFILES_2026-11-04.md):
 *  1. the lock-screen handshakes (UnlockMethods),
 *  2. the KONAMI key code, anywhere in the custom launcher, with the screen unlocked: Volume Up, Up, Down, Down, Back, Back, Up, Down within 20 s (helper key access),
 *  3. the hidden Solitaire hotspot: five quick taps then a hold on an invisible square in a corner of Solitaire ({@link TapCode}),
 *  4. helper OFF: three quick Home presses ({@link ProfileSession#homeFailsafe}),
 *  5. Android's own: Settings > Apps > Default apps > Home app; Safe mode (stock launcher only); adb `cmd package set-home-activity`.
 * Switches live in Launcher control (Quickstep mode).
 */
public final class FailsafeKeys {
    public static final String KONAMI_KEY = "failsafe_konami";        // default ON
    public static final String HOTSPOT_KEY = "failsafe_hotspot";      // default OFF: the square covers a bit of the game
    public static final String CORNER_KEY = "failsafe_corner";        // 0 top-left, 1 top-right, 2 bottom-left, 3 bottom-right (default 2)
    public static final String LAST_KEY = "failsafe_last";
    public static final String PROBE_UNTIL_KEY = "probe_until", PROBE_REPORT_KEY = "probe_report";
    public static final int[] KONAMI = { KeySeq.VOL_UP, KeySeq.VOL_UP, KeySeq.VOL_DOWN, KeySeq.VOL_DOWN, KeySeq.BACK, KeySeq.BACK, KeySeq.VOL_UP, KeySeq.VOL_DOWN };
    public static final long WINDOW_MS = 20_000;
    public static final String[] CORNERS = { "top left", "top right", "bottom left", "bottom right" };

    private FailsafeKeys() {}

    public static boolean konami(Context c) { return Features.get(c, KONAMI_KEY, true); }
    public static boolean hotspot(Context c) { return Features.get(c, HOTSPOT_KEY, false); }
    public static int corner(Context c) { int v = Features.getInt(c, CORNER_KEY, 2); return v < 0 || v > 3 ? 2 : v; }
}
