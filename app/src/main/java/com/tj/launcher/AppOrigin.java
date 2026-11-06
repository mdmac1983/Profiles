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

    public static void clearQuickstep(Context c) { p(c).edit().putStringSet("quickstep", new HashSet<String>()).putLong("usageFrom", System.currentTimeMillis()).apply(); }   // AI1: usage events up to now are dealt with

    private static long lastMergeAt = 0;

    /**
     * AI1 (Tj, 2026-11-06: Portfolio, opened only in Quickstep, was still in Recents after "back to custom"): the accessibility tracking above can miss an app (an event never arrived, the marks were cleared early).
     * Android's own usage events say which apps were used during the Quickstep session ({@link QuickstepUsage}); every such app that is not ours, not the stock launcher / keyboard / target and not started from the
     * custom launcher is marked as opened in Quickstep too. Bounded to the session ({@link Mode#qsStartAt} .. {@link Mode#qsEndAt}, or now while it is still on) and to what has not been cleared yet. Cheap, never throws.
     */
    public static void mergeUsage(Context c) {
        try {
            long now = System.currentTimeMillis();
            long start = Mode.qsStartAt(c);
            if (start <= 0) return;
            long end = Mode.isCustom(c) ? Mode.qsEndAt(c) : now;
            if (end <= start) return;
            long from = Math.max(start, p(c).getLong("usageFrom", 0));
            if (from >= end) return;
            synchronized (AppOrigin.class) { if (now - lastMergeAt < 1000) return; lastMergeAt = now; }
            java.util.Set<String> used = QuickstepUsage.resumedDuring(c, from, end);
            if (used.isEmpty()) return;
            android.content.ComponentName stock = StockLauncher.find(c);
            String ime = null;
            String cur = android.provider.Settings.Secure.getString(c.getContentResolver(), android.provider.Settings.Secure.DEFAULT_INPUT_METHOD);
            if (cur != null && cur.contains("/")) ime = cur.substring(0, cur.indexOf('/'));
            String target = ReturnReset.targetPackage(c);
            java.util.Set<String> known = quickstep(c), custom = custom(c);
            StringBuilder added = new StringBuilder();
            for (String pkg : used) {
                if (known.contains(pkg) || custom.contains(pkg)) continue;
                if (!RecentsDirty.shouldMark(pkg, c.getPackageName(), stock == null ? null : stock.getPackageName(), ime, target, true)) continue;
                markQuickstep(c, pkg);
                if (added.length() > 0) added.append(", ");
                added.append(pkg);
            }
            if (added.length() > 0) {
                RecentsDirty.mark(c);
                LockLog.add(c, "USAGE", "the usage log shows apps used in Quickstep that the window tracker missed: " + added + " (now counted as opened in Quickstep)");
            }
        } catch (Throwable t) { android.util.Log.e("TjOrigin", "usage merge failed", t); }
    }
    /** False until the first successful Recents clear after install: older Quickstep apps are unknown, so the first reset clears everything. */
    public static boolean tracked(Context c) { return p(c).getBoolean("tracked", false); }
    public static void setTracked(Context c) { p(c).edit().putBoolean("tracked", true).apply(); }

    /** Pure rule (JVM-testable): the apps to close when returning to the custom launcher = Quickstep's, minus any the custom launcher also started. */
    public static Set<String> pending(Set<String> custom, Set<String> quickstep) {
        Set<String> r = new HashSet<>(quickstep);
        r.removeAll(custom);
        return r;
    }

    public static Set<String> pending(Context c) { mergeUsage(c); return pending(custom(c), quickstep(c)); }   // AI1: every clean-up asks here, so every clean-up also sees what Android's usage log says

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
