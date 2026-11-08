package com.tj.launcher;

/**
 * BH1: the hold bookkeeping of {@link NotifQuiet} as a pure state machine (JVM-tested, no Android classes): what to remember FIRST, when to write the zero and when to put the original back, so that no
 * trigger, crash or half-failed write can leave Tj's pop-up banners off in Quickstep or on in the custom launcher. {@link NotifQuiet} adapts SharedPreferences and Settings.Global to the two small interfaces.
 *
 * Rules (each one is a test): the original goes to disk BEFORE the first write; a setting that already reads "off" (his own permanent choice, or our zero still there) is left alone and nothing is held;
 * a hold that is already on and still at our zero rewrites nothing; ending the hold puts the original back only while the setting is STILL our zero (a value changed by hand meanwhile is his), and the
 * stored hold is cleared only AFTER that write; without the permission a hold is neither taken nor ended (the saved original waits for the next start with it); a write that fails while TAKING the hold is
 * undone, one that fails while ENDING it leaves the hold so the next trigger tries again.
 */
public final class NotifQuietHold {
    private NotifQuietHold() {}

    /** What survives a process death (the preferences). */
    public interface State {
        boolean held();
        /** The value to put back; only meaningful while {@link #held}. */
        String original();
        /** Stores both in ONE commit. Must throw when the write did not happen. */
        void save(boolean held, String original);
    }

    /** The one Settings.Global value. */
    public interface Setting {
        String get();
        /** Must throw when the write did not happen. */
        void put(String value);
    }

    public interface Sink { void log(String message); }

    /** Brings the setting in line with {@code want} and returns whether the hold is on afterwards. Never throws for an ordinary failed write (it is logged and the stored state stays consistent). */
    public static boolean set(State st, Setting s, boolean permitted, boolean want, Sink log) {
        final boolean held = st.held();
        if (want) {
            if (!permitted) return held;
            final String cur = s.get();
            if (!NotifQuietPolicy.needsZero(cur)) return held;   // already off: nothing to write, nothing to remember
            try {
                st.save(true, cur);   // the original (the newest "on" value) on disk BEFORE the change
                s.put("0");
                return true;
            } catch (RuntimeException e) {
                log.log("could not turn the pop-up banners off: " + e);
                if (!held) { try { st.save(false, null); } catch (RuntimeException ignored) { } }
                return held;
            }
        }
        if (!held) return false;
        if (!permitted) return true;   // cannot put it back now: the hold and the saved original stay
        try {
            final String w = NotifQuietPolicy.restoreValue(s.get(), st.original());
            if (w != null) { s.put(w); log.log("pop-up banners put back to " + w); }
            st.save(false, null);   // cleared only AFTER the write
            return false;
        } catch (RuntimeException e) {
            log.log("could not put the pop-up banners back: " + e);
            return true;
        }
    }
}
