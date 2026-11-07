package com.tj.launcher;

/**
 * BF1 (Tj, 2026-11-07 23:36 UTC: "... is it possible for the Quickstep launcher to automatically turn off all animations (this is an accessibility setting). When I turn off animations it makes the tablet
 * seem faster. But the animations must be turned back on automatically and immediately on both the lock screen and in the custom launcher. Animations should only ever be turned off inside quickstep launcher").
 *
 * The pure rules (JVM-tested, no Android classes). The accessibility setting "Remove animations" is nothing but the three {@code Settings.Global} scales set to 0; {@link AnimationSpeedup} writes them
 * (adb grant WRITE_SECURE_SETTINGS) and puts them back; THIS class decides WHEN they must be off.
 *
 * "Inside Quickstep" = the whole Quickstep session (the stock home and the apps opened from it: opening and closing an app is where the animation time is). The lock screen counts as the custom world
 * (like {@code TileProfile.effectiveMode}); there the animations are REAL on purpose: the lock-screen patch follows the stock power menu's 300 ms fade ({@link PatchFade}).
 */
public final class AnimationPolicy {
    private AnimationPolicy() {}

    /** The switch in Launcher control (shipped default ON, debug builds OFF so the older emulator sections keep their premises, like clean_start). */
    public static final String KEY = "quickstep_animations_off";

    /** The two reasons the scales may be held at 0. Stored as a bit mask, so each one can end without touching the other. */
    public static final int HOLD_RESET = 1, HOLD_QUICKSTEP = 2;

    /** The default of {@link #KEY}. */
    public static boolean defaultOn(boolean debuggable) { return !debuggable; }

    /** Everything the decision reads, by name (eight booleans in a row are an invitation to swap two). All false = nothing is wanted. */
    public static final class Facts {
        /** the switch in Launcher control */
        public boolean switchOn;
        /** WRITE_SECURE_SETTINGS (the one-time adb grant) */
        public boolean grant;
        /** the accessibility helper is connected: it is what hears the screen go off and the unlock; without it nothing could end the hold in time */
        public boolean helperUp;
        /** the stored mode says Quickstep */
        public boolean quickstepMode;
        /** crash breaker tripped, or "Restore everything" paused the launcher while in Quickstep ({@link CrashGuard#automationOff(boolean, boolean, boolean)}) */
        public boolean automationOff;
        /** the display is on (interactive); a dark screen is about to show the lock screen */
        public boolean screenOn;
        /** the lock screen is up (keyguard locked) */
        public boolean lockUp;
        /** the lock screen was up and the unlock choice (plain PIN = custom, gesture = Quickstep) has not been applied yet: the stored mode can still be the OLD session's */
        public boolean unlockUndecided;

        /** The facts of an ordinary unlocked Quickstep session with everything granted: the baseline the tests change one field of. */
        public static Facts session() {
            Facts f = new Facts();
            f.switchOn = true; f.grant = true; f.helperUp = true; f.quickstepMode = true; f.screenOn = true;
            return f;
        }
    }

    /**
     * Why the Quickstep hold is NOT wanted right now: the FIRST failing condition in plain words, or "" when it is wanted. One function for the decision and for the words (log, Launcher control), so they
     * can never disagree. Order = most informative first.
     */
    public static String why(Facts f) {
        if (!f.switchOn) return "switch off";
        if (!f.grant) return "no adb permission";
        if (!f.helperUp) return "accessibility helper not running";
        if (f.automationOff) return "automation paused";
        if (!f.quickstepMode) return "custom launcher";
        if (!f.screenOn) return "screen off";
        if (f.lockUp) return "lock screen";
        if (f.unlockUndecided) return "unlock not decided yet";
        return "";
    }

    public static boolean wanted(Facts f) { return why(f).isEmpty(); }

    /** How long a lock cycle may stay "undecided" after the lock screen is gone before the heartbeat stops waiting for the unlock handler (it normally answers in well under a second). */
    public static final long UNDECIDED_MAX_MS = 5000;

    // ---------------------------------------------------------------- hold bookkeeping (pure)

    /** The mask after {@code bit} was set or cleared. */
    public static int withHold(int holds, int bit, boolean on) { return on ? (holds | bit) : (holds & ~bit); }

    public static boolean has(int holds, int bit) { return (holds & bit) != 0; }

    // ---------------------------------------------------------------- one animation scale (a Settings.Global string)

    /** True when the stored scale is exactly zero ("0", "0.0", "0.00"); null, garbage and NaN are not. */
    public static boolean isOff(String v) {
        if (v == null) return false;
        try { return Float.parseFloat(v.trim()) == 0f; } catch (NumberFormatException e) { return false; }
    }

    /** What to remember as the original of a scale: its current text, or "1.0" (Android's default) when it is not set at all. */
    public static String originalOf(String current) { return current == null || current.trim().isEmpty() ? "1.0" : current; }

    /** Does this scale need to be written to switch the animation off? Not when it already is (Tj's own permanent setting stays his). */
    public static boolean needsZero(String current) { return !isOff(current); }

    /**
     * What to write back for one scale when the last hold ends, or null for "write nothing": only a scale that is STILL at our zero is put back, and only when the original was not zero itself. A value
     * Tj changed by hand while the hold was on (Developer options) is his and stays.
     */
    public static String restoreValue(String current, String original) {
        if (!isOff(current)) return null;
        String o = originalOf(original);
        return isOff(o) ? null : o;
    }
}
