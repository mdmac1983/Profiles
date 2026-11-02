package com.tj.launcher;

import android.content.Context;
import android.content.SharedPreferences;

/** The launcher's own feature switches. Everything that is risky or needs a permission defaults to OFF. */
public final class Features {
    private static final String PREFS = "tj_features";
    private Features() {}

    private static SharedPreferences p(Context c) { return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    public static boolean get(Context c, String key, boolean def) { return p(c).getBoolean(key, def); }
    public static void set(Context c, String key, boolean on) { p(c).edit().putBoolean(key, on).commit(); }
    public static void allOff(Context c) { p(c).edit().clear().commit(); }
}
