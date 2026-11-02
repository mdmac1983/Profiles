package com.tj.launcher;

import android.content.Context;
import android.content.SharedPreferences;

/** The launcher's own feature switches. Everything that is risky or needs a permission defaults to OFF. */
public final class Features {
    private static final String PREFS = "tj_features";

    /** Power menu: OFF | HIDE_SCREENSHOT | BLOCK (close the whole menu). Default OFF. */
    public static final String POWER_MODE = "power_mode";
    public static final String POWER_OFF = "off", POWER_HIDE_SCREENSHOT = "hide_screenshot", POWER_BLOCK = "block";

    private Features() {}

    private static SharedPreferences p(Context c) { return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    public static boolean get(Context c, String key, boolean def) { return p(c).getBoolean(key, def); }
    public static void set(Context c, String key, boolean on) { p(c).edit().putBoolean(key, on).commit(); }
    public static String getString(Context c, String key, String def) { return p(c).getString(key, def); }
    public static void setString(Context c, String key, String v) { p(c).edit().putString(key, v).commit(); }
    public static void allOff(Context c) { p(c).edit().clear().commit(); }

    public static String powerMode(Context c) {
        String m = getString(c, POWER_MODE, POWER_OFF);
        return POWER_HIDE_SCREENSHOT.equals(m) || POWER_BLOCK.equals(m) ? m : POWER_OFF;
    }
}
