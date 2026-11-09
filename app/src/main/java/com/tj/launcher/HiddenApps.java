package com.tj.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import java.util.HashSet;
import java.util.Set;

/** Storage + Android glue for {@link AppVisibility}. Plain app preferences (no secure settings). Every method is exception-safe: on any failure apps stay SHOWN (never hide by accident). */
public final class HiddenApps {
    private static final String PREFS = "tj_apps";
    private static final String ALLOWED = "allowed";
    private static final String BASELINE = "baseline_done";
    private static final Object LOCK = new Object();

    private HiddenApps() {}

    private static SharedPreferences p(Context c) { return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    public static Set<String> installedLaunchablePackages(Context c) {
        Set<String> s = new HashSet<>();
        for (ResolveInfo ri : c.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)) {
            s.add(ri.activityInfo.packageName);
        }
        return s;
    }

    /** BT1a: the allowed set of the ACTIVE custom profile (FBOP keeps the old key, Rental has its own and starts as a copy of FBOP's). */
    private static Set<String> allowed(Context c) { return allowed(c, Profiles.active(c)); }

    private static Set<String> allowed(Context c, ProfileRules.Profile profile) {
        SharedPreferences sp = p(c);
        if (!sp.getBoolean(BASELINE, false)) {
            Set<String> base = AppVisibility.baseline(installedLaunchablePackages(c), c.getPackageName());
            sp.edit().putStringSet(ALLOWED, base).putBoolean(BASELINE, true).commit();
        }
        String key = ProfileRules.scopedKey(ALLOWED, profile);
        if (!key.equals(ALLOWED) && !sp.contains(key)) return new HashSet<>(sp.getStringSet(ALLOWED, new HashSet<String>()));   // a new Rental = a copy of FBOP
        return new HashSet<>(sp.getStringSet(key, new HashSet<String>()));
    }

    /** BT1a: shown in THAT profile's drawer (the apps screen edits one profile at a time). */
    public static boolean isPackageShownIn(Context c, ProfileRules.Profile profile, String pkg) {
        try { synchronized (LOCK) { return AppVisibility.isShown(allowed(c, profile), pkg, null, c.getPackageName()); } } catch (Throwable t) { return true; }
    }

    public static void setShown(Context c, ProfileRules.Profile profile, String pkg, boolean shown) {
        synchronized (LOCK) {
            Set<String> a = AppVisibility.withShown(allowed(c, profile), pkg, shown);
            p(c).edit().putStringSet(ProfileRules.scopedKey(ALLOWED, profile), a).commit();
        }
    }

    public static boolean isShown(Context c, ComponentName app) {
        try {
            synchronized (LOCK) { return AppVisibility.isShown(allowed(c), app.getPackageName(), app.getClassName(), c.getPackageName()); }
        } catch (Throwable t) {
            android.util.Log.e("TjHidden", "isShown failed, showing the app", t);
            return true;
        }
    }

    public static boolean isPackageShown(Context c, String pkg) {
        try {
            synchronized (LOCK) { return AppVisibility.isShown(allowed(c), pkg, null, c.getPackageName()); }
        } catch (Throwable t) { return true; }
    }

    public static void setShown(Context c, String pkg, boolean shown) { setShown(c, Profiles.active(c), pkg, shown); }

    public static void onRemoved(Context c, String pkg) {
        try {
            synchronized (LOCK) {   // BT1a: an uninstalled app is forgotten by every profile (a reinstall is new = hidden everywhere until allowed)
                for (ProfileRules.Profile pr : new ProfileRules.Profile[] { ProfileRules.Profile.FBOP, ProfileRules.Profile.RENTAL }) {
                    String key = ProfileRules.scopedKey(ALLOWED, pr);
                    if (!key.equals(ALLOWED) && !p(c).contains(key)) continue;
                    p(c).edit().putStringSet(key, AppVisibility.withoutRemoved(allowed(c, pr), pkg)).commit();
                }
            }
        } catch (Throwable ignored) { }
    }

    /** Ask Launcher3 to rebuild its app list so a change shows up at once. */
    public static void reloadLauncher(Context c) {
        try { com.android.launcher3.LauncherAppState.getInstance(c).getModel().forceReload(); }
        catch (Throwable t) { android.util.Log.e("TjHidden", "reload failed", t); }
    }
}
