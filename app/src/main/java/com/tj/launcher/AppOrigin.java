package com.tj.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

/**
 * D5 (Tj, 2026-11-04): "keep the two launchers' recents lists functional but separate". Remembers which launcher an app was opened from:
 * custom = started from the custom launcher (Launcher3 hook), quickstep = its window appeared while Quickstep mode was on and the custom launcher did not start it.
 * When Tj returns to the custom launcher only the QUICKSTEP-opened apps are closed (killed + their Recents card dismissed); apps opened from the custom launcher stay in Recents
 * normally until he closes them. Android has ONE Recents list; this is how it is kept apart: by removing the other launcher's entries at the switch.
 */
public final class AppOrigin {
    private static final String PREFS = "tj_origin";
    private AppOrigin() {}

    private static SharedPreferences p(Context c) { return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE); }
    private static Set<String> get(Context c, String k) { return new HashSet<>(p(c).getStringSet(k, new HashSet<String>())); }

    public static Set<String> custom(Context c) { return get(c, "custom"); }
    public static Set<String> quickstep(Context c) { return get(c, "quickstep"); }

    /** An app was started from the custom launcher: it is ours to keep (and no longer counts as Quickstep's). */
    public static void markCustom(Context c, String pkg) {
        if (pkg == null || pkg.isEmpty()) return;
        Set<String> cu = get(c, "custom"), qs = get(c, "quickstep");
        boolean changed = cu.add(pkg) | qs.remove(pkg);
        if (changed) p(c).edit().putStringSet("custom", cu).putStringSet("quickstep", qs).apply();
    }

    /** A window of this app appeared while Quickstep was on: it belongs to Quickstep unless the custom launcher started it. */
    public static void markQuickstep(Context c, String pkg) {
        if (pkg == null || pkg.isEmpty()) return;
        if (get(c, "custom").contains(pkg)) return;
        Set<String> qs = get(c, "quickstep");
        if (qs.add(pkg)) p(c).edit().putStringSet("quickstep", qs).apply();
    }

    public static void clearQuickstep(Context c) { p(c).edit().putStringSet("quickstep", new HashSet<String>()).apply(); }
    /** False until the first successful Recents clear after install: older Quickstep apps are unknown, so the first reset clears everything. */
    public static boolean tracked(Context c) { return p(c).getBoolean("tracked", false); }
    public static void setTracked(Context c) { p(c).edit().putBoolean("tracked", true).apply(); }

    /** Pure rule (JVM-testable): the apps to close when returning to the custom launcher = Quickstep's, minus any the custom launcher also started. */
    public static Set<String> pending(Set<String> custom, Set<String> quickstep) {
        Set<String> r = new HashSet<>(quickstep);
        r.removeAll(custom);
        return r;
    }

    public static Set<String> pending(Context c) { return pending(custom(c), quickstep(c)); }

    /** Pure rule: does a Recents card description (the app label, maybe followed by extra text) belong to one of these labels? */
    public static boolean cardMatches(String description, Set<String> labels) {
        if (description == null) return false;
        String d = description.trim().toLowerCase(java.util.Locale.ROOT);
        if (d.isEmpty()) return false;
        for (String l : labels) {
            if (l == null) continue;
            String x = l.trim().toLowerCase(java.util.Locale.ROOT);
            if (!x.isEmpty() && (d.equals(x) || d.startsWith(x + ",") || d.startsWith(x + " "))) return true;
        }
        return false;
    }
}
