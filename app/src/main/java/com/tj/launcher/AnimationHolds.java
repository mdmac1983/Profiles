package com.tj.launcher;

import java.util.HashMap;
import java.util.Map;

/**
 * BF1: the hold bookkeeping of {@link AnimationSpeedup} as a pure state machine (JVM-tested, no Android classes): which REASONS hold the three animation scales at zero, what to remember first and what to put back.
 * {@link AnimationSpeedup} adapts SharedPreferences and Settings.Global to the two small interfaces below.
 *
 * Rules (each one is a test): the FIRST reason saves the originals to disk BEFORE the first change, then writes zeros (a scale that already is zero is left alone); a reason that JOINS one already holding rewrites
 * nothing; the LAST one ending puts the originals back, scale by scale, only where the scale is still at OUR zero (a value changed by hand meanwhile is his) and the original was not zero itself; `pending` is cleared
 * only AFTER the writes (a death in between is repaired by the next start); a write that fails half way undoes what it did and leaves the stored mask as it was; without the permission an acquire does nothing and a
 * release keeps `pending` so that the next start with the permission finishes the job; originals are re-captured at every first hold (a leftover `pending` keeps the saved original only for a scale that is STILL
 * at our zero).
 */
public final class AnimationHolds {
    private AnimationHolds() {}

    /** What survives a process death (the preferences). */
    public interface State {
        int holds();
        boolean pending();
        boolean hasOriginal(String scale);
        /** The saved original, "1.0" when none. */
        String original(String scale);
        /** Stores everything in ONE commit; {@code originals == null} leaves the saved originals alone. Must throw when the write did not happen. */
        void save(int holds, boolean pending, Map<String, String> originals);
    }

    /** The scales (Settings.Global). */
    public interface Scales {
        String get(String scale);
        /** Must throw when the write did not happen. */
        void put(String scale, String value);
    }

    public interface Sink { void log(String message); }

    /**
     * Sets or clears one reason ({@code bit}) and does what follows from the new mask. Returns the mask afterwards. An exception from {@code Scales.put} / {@code State.save} while TAKING the animations away is
     * caught here (undone, mask unchanged); one while PUTTING THEM BACK propagates, with nothing cleared, so the next trigger tries again.
     */
    public static int set(State st, Scales sc, String[] names, boolean permitted, int bit, boolean on, Sink log) {
        final int holds = st.holds();
        final int next = AnimationPolicy.withHold(holds, bit, on);
        final boolean pending = st.pending();
        if (next != 0) {
            if (!permitted) return holds;
            final boolean first = holds == 0 || !pending;   // nobody held them before: this reason makes them zero
            boolean snapshotDone = false;
            try {
                if (first) {
                    Map<String, String> originals = new HashMap<>();
                    for (String n : names) {
                        String cur = sc.get(n);
                        // a scale still at OUR zero while an earlier restore never finished keeps the original saved then; any other value is the truth NOW (never reuse stale originals)
                        if (!(pending && AnimationPolicy.isOff(cur) && st.hasOriginal(n))) originals.put(n, AnimationPolicy.originalOf(cur));
                    }
                    st.save(next, true, originals);   // on disk BEFORE the first change
                    snapshotDone = true;
                    for (String n : names) if (AnimationPolicy.needsZero(sc.get(n))) sc.put(n, "0");
                } else if (next != holds) {
                    st.save(next, pending, null);   // joining: nothing is rewritten, a scale changed by hand since stays his
                }
                return next;
            } catch (RuntimeException e) {
                log.log("could not turn animations off: " + e);
                if (first && snapshotDone) {
                    try { for (String n : names) { String w = AnimationPolicy.restoreValue(sc.get(n), st.original(n)); if (w != null) sc.put(n, w); } } catch (RuntimeException ignored) { }
                }
                try { st.save(holds, pending, null); } catch (RuntimeException ignored) { }   // the plain undo: the mask and `pending` as they were
                return holds;
            }
        }
        if (holds == 0 && !pending) return 0;   // nothing was ever held
        if (pending) {
            if (!permitted) { st.save(0, true, null); return 0; }   // cannot put them back now: `pending` stays
            for (String n : names) {
                String w = AnimationPolicy.restoreValue(sc.get(n), st.original(n));
                if (w != null) sc.put(n, w);
            }
            log.log("animations restored");
            st.save(0, false, null);   // cleared only AFTER the writes
        } else {
            st.save(0, false, null);   // a mask without `pending`: nothing was changed, just clear it
        }
        return 0;
    }
}
