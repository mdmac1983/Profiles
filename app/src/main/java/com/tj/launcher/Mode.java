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
        prefs(c).edit().putString(KEY, QUICKSTEP.equals(mode) ? QUICKSTEP : CUSTOM).commit();
    }
}
