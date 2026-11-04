package com.tj.launcher;

import android.content.Context;
import java.util.ArrayDeque;

/**
 * N4 (Tj, 2026-11-04): several ways from Quickstep BACK to the custom launcher (the "kill switch"), besides double Home. Each has its own switch in Launcher control (Quickstep mode).
 * They only act while Quickstep is the active launcher, so none of them can be done "by accident" from the custom launcher.
 *   HOME2    press Home twice quickly                              (the original; handled by the router)
 *   ICON     tap the "Back to custom" icon in Quickstep's app drawer / home   (an app icon that acts as the kill switch)
 *   TILE     tap the "Back to custom" quick-settings tile          (add it in the tile editor; Quickstep has the pencil)
 *   VOLCODE  Volume Down, Down, Up, Up within 6 s
 *   BACKS    Back, Back, Back, then Volume Down within 6 s
 *   VOLBOTH  press both volume keys at the same moment (within 250 ms)
 *   FLURRY   press the side key 4 times quickly
 * Pure rules (JVM-testable); the helper feeds the key / screen events.
 */
public final class ReturnMethods {
    public enum Method {
        HOME2("home2", "Press Home twice", "Two quick presses of the Home button.", true),
        ICON("icon", "\"Back to custom\" icon", "An app icon in Quickstep's drawer: tap it and you are back in the custom launcher.", true),
        TILE("tile", "Quick-settings tile", "Pull down the panel and tap the \"Back to custom\" tile (add it with the pencil in Quickstep's tile editor).", true),
        VOLCODE("volcode", "Volume code", "Volume Down, Down, Up, Up within 6 seconds.", true),
        BACKS("backs", "Three Backs + Volume Down", "Press Back three times, then Volume Down, within 6 seconds.", false),
        VOLBOTH("volboth", "Both volume keys together", "Press Volume Up and Volume Down at the same moment.", false),
        FLURRY("flurry", "Side-key flurry", "Press the side key 4 times quickly (screen off, on, off, on).", false);

        public final String id, title, how;
        public final boolean defaultOn;
        Method(String id, String title, String how, boolean defaultOn) { this.id = id; this.title = title; this.how = how; this.defaultOn = defaultOn; }
        public String prefKey() { return "return_m_" + id; }
    }

    public static final long SEQ_WINDOW_MS = 6_000, BOTH_MS = 250, FLURRY_MS = 4_000;
    public static final int[] VOLCODE = { KeySeq.VOL_DOWN, KeySeq.VOL_DOWN, KeySeq.VOL_UP, KeySeq.VOL_UP };
    public static final int[] BACKS = { KeySeq.BACK, KeySeq.BACK, KeySeq.BACK, KeySeq.VOL_DOWN };

    private ReturnMethods() {}

    public static boolean on(Context c, Method m) { return Features.get(c, m.prefKey(), m.defaultOn); }

    /** True if any method that needs the key-event access is switched on. */
    public static boolean needsKeys(Context c) { return on(c, Method.VOLCODE) || on(c, Method.BACKS) || on(c, Method.VOLBOTH); }

    /** Pure: both volume keys pressed within BOTH_MS of each other (one press each, any order). */
    public static boolean bothVolume(long lastUpAt, long lastDownAt, long now) {
        return lastUpAt > 0 && lastDownAt > 0 && Math.abs(lastUpAt - lastDownAt) <= BOTH_MS && now - Math.max(lastUpAt, lastDownAt) <= BOTH_MS;
    }

    /** Four screen changes inside FLURRY_MS, the last one ON. */
    public static final class Flurry {
        private final ArrayDeque<Long> t = new ArrayDeque<>();
        public boolean onChange(boolean on, long now) {
            t.addLast(now);
            while (!t.isEmpty() && now - t.peekFirst() > FLURRY_MS) t.pollFirst();
            if (on && t.size() >= 4) { t.clear(); return true; }
            return false;
        }
        public void reset() { t.clear(); }
    }
}
