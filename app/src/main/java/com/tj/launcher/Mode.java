package com.tj.launcher;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Which launcher answers the Home button: our ported Launcher3 (CUSTOM) or the stock Quickstep (QUICKSTEP).
 * Stored in plain app preferences (no secure settings, no lock-screen state). CUSTOM is the default.
 */
public final class Mode {
    public static final String CUSTOM = "custom";
    public static final String QUICKSTEP = "quickstep";
    private static final String PREFS = "tj_mode";
    private static final String KEY = "mode";

    private Mode() {}

    public static long lastHomePressMs(Context c) { return prefs(c).getLong("lastHome", 0); }

    /** apply(): called on every Home press, must stay cheap. */
    public static void recordHomePress(Context c, long now) { prefs(c).edit().putLong("lastHome", now).apply(); }

    /** When the mode last changed (ms): the stray-Quickstep guard waits a moment after a deliberate change (late window events from the screen we just left). */
    public static long changedAt(Context c) { return prefs(c).getLong("changedAt", 0); }

    /** W5: while a fail-open window is active (the router had to fall back to the stock launcher because ours would not start) the stray-Quickstep guard stands down. */
    public static long failOpenUntil(Context c) { return prefs(c).getLong("failOpenUntil", 0); }
    public static void markFailOpen(Context c, long untilMs) { prefs(c).edit().putLong("failOpenUntil", untilMs).commit(); }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String get(Context c) {
        String m = prefs(c).getString(KEY, CUSTOM);
        return QUICKSTEP.equals(m) ? QUICKSTEP : CUSTOM;
    }

    public static boolean isCustom(Context c) { return CUSTOM.equals(get(c)); }

    /** commit() (not apply): the router reads this a moment later, possibly after our process is killed. */
    public static void set(Context c, String mode) {
        String m = QUICKSTEP.equals(mode) ? QUICKSTEP : CUSTOM;
        prefs(c).edit().putString(KEY, m).putLong("changedAt", System.currentTimeMillis()).commit();
        KeyboardSwitch.apply(c, m);   // V2: stock keyboard in custom mode, SwiftKey in Quickstep (only if switched on and adb-granted)
    }
}
