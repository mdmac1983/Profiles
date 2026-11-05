package com.tj.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AA3 (Tj, 2026-11-05: "Build number 4"): the Recents clean-up at the end of a Quickstep session works on TASKS, not on guesses about packages.
 *
 * Until now the clean-up closed "the packages whose window appeared while Quickstep was on" ({@link AppOrigin}). That list comes from accessibility events, so it can miss an app (helper restarted
 * mid-session) and it cannot tell two worlds apart when the SAME app lives in both: Contacts open in the custom account and opened again in Quickstep was force-stopped, taking the custom
 * account's Contacts with it.
 *
 * With the shell helper there is ground truth: the list of Recents tasks (id + package) is read when the Quickstep session BEGINS (that is the custom account's world) and again when it ENDS.
 *   - a task that is new since the beginning is Quickstep's;
 *   - an app that already had a task at the beginning (or that the custom launcher started) is NOT force-stopped, only its new task is removed, by id, so the custom account keeps its own;
 *   - an app that appears only in Quickstep is force-stopped as before (its notifications and process go too).
 * Without the helper nothing here runs and the old package-based clean-up does everything.
 */
public final class TaskOrigin {
    private static final String PREFS = "tj_task_origin";
    private static final String KEY_BASE = "base_tasks";
    private TaskOrigin() {}

    private static SharedPreferences p(Context c) { return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    // ------------------------------------------------------------------ pure rules (JVM-tested)

    /** "12:com.a,14:com.b" */
    public static String encode(Map<Integer, String> tasks) {
        StringBuilder sb = new StringBuilder();
        if (tasks != null) for (Map.Entry<Integer, String> e : tasks.entrySet()) {
            if (e.getValue() == null || e.getValue().indexOf(',') >= 0 || e.getValue().indexOf(':') >= 0) continue;
            if (sb.length() > 0) sb.append(',');
            sb.append(e.getKey()).append(':').append(e.getValue());
        }
        return sb.toString();
    }

    /** Inverse of {@link #encode}; bad pieces are skipped. */
    public static LinkedHashMap<Integer, String> decode(String s) {
        LinkedHashMap<Integer, String> out = new LinkedHashMap<>();
        if (s == null || s.isEmpty()) return out;
        for (String part : s.split(",")) {
            int i = part.indexOf(':');
            if (i <= 0 || i == part.length() - 1) continue;
            try { out.put(Integer.parseInt(part.substring(0, i)), part.substring(i + 1)); } catch (NumberFormatException ignored) { }
        }
        return out;
    }

    /** The tasks to remember at the beginning of a Quickstep session: everything Android lists, except tasks of apps that are still marked as Quickstep leftovers (they must be cleaned next time, not protected). */
    public static LinkedHashMap<Integer, String> baseFor(Map<Integer, String> listed, Set<String> quickstepLeftovers) {
        LinkedHashMap<Integer, String> out = new LinkedHashMap<>();
        if (listed == null) return out;
        for (Map.Entry<Integer, String> e : listed.entrySet()) if (quickstepLeftovers == null || !quickstepLeftovers.contains(e.getValue())) out.put(e.getKey(), e.getValue());
        return out;
    }

    /** What to do at the end of a session. */
    public static final class Plan {
        /** Apps that exist only in Quickstep: force-stop (process, notifications, cards). */
        public final Set<String> stopPackages = new LinkedHashSet<>();
        /** Apps that ALSO have a custom-account task: keep the app, remove just Quickstep's task(s) by id. */
        public final List<Integer> removeIds = new ArrayList<>();
        /** Apps the old package list wanted to force-stop but that have a custom-account task: spared. */
        public final Set<String> spared = new LinkedHashSet<>();
        public int newTasks = 0;
    }

    /**
     * @param base      the tasks listed when the Quickstep session began
     * @param now       the tasks listed now
     * @param pending   the packages the accessibility-based tracking says Quickstep opened (AppOrigin.pending)
     * @param custom    packages the custom launcher itself started (AppOrigin.custom)
     * @param target    the app that must always stay (Solitaire)
     * @param protectedPkgs packages that must never be touched (this app, the stock launcher, the keyboard): decided by the caller with ResetPolicy
     */
    public static Plan plan(Map<Integer, String> base, Map<Integer, String> now, Collection<String> pending, Set<String> custom, String target, Set<String> protectedPkgs) {
        Plan plan = new Plan();
        Set<String> basePkgs = new LinkedHashSet<>(base.values());
        for (Map.Entry<Integer, String> e : now.entrySet()) {
            if (base.containsKey(e.getKey())) continue;           // existed when the session began: the custom account's
            String pkg = e.getValue();
            if (pkg == null || pkg.equals(target) || protectedPkgs.contains(pkg)) continue;
            plan.newTasks++;
            if (basePkgs.contains(pkg) || custom.contains(pkg)) plan.removeIds.add(e.getKey());
            else plan.stopPackages.add(pkg);
        }
        if (pending != null) for (String pkg : pending) {
            if (pkg == null || pkg.equals(target) || protectedPkgs.contains(pkg)) continue;
            if (basePkgs.contains(pkg)) plan.spared.add(pkg);       // the old list would have closed an app the custom account is using
            else plan.stopPackages.add(pkg);
        }
        return plan;
    }

    // ------------------------------------------------------------------ the session hooks

    public static LinkedHashMap<Integer, String> base(Context c) {
        String s = p(c).getString(KEY_BASE, null);
        return s == null ? null : decode(s);
    }

    public static void clearBase(Context c) { p(c).edit().remove(KEY_BASE).commit(); }

    /**
     * A Quickstep session begins (custom -> Quickstep): forget the old snapshot at once (a stale one would make the custom account's own tasks look new), then, off the main thread and only if the
     * shell helper answers, remember the tasks that exist right now. No helper = no snapshot = the package-based clean-up, exactly as before.
     */
    public static void sessionStart(final Context c) {
        final Context app = c.getApplicationContext();
        clearBase(app);
        new Thread(new Runnable() { @Override public void run() {
            try {
                if (!ShellHelper.enabled(app) || !ShellHelper.reachable()) return;
                LinkedHashMap<Integer, String> listed = ShellHelper.listTasks();
                if (listed == null) return;
                LinkedHashMap<Integer, String> base = baseFor(listed, AppOrigin.pending(app));
                p(app).edit().putString(KEY_BASE, encode(base)).commit();
                android.util.Log.i("TjTasks", "session start: " + base.size() + " task(s) belong to the custom account " + encode(base));
            } catch (Throwable t) { android.util.Log.e("TjTasks", "snapshot failed", t); }
        } }, "tj-task-base").start();
    }

    /** What {@link #finish} decided. */
    public static final class Outcome {
        public final List<String> stop; public final List<Integer> removeIds; public final String note;
        Outcome(List<String> stop, List<Integer> removeIds, String note) { this.stop = stop; this.removeIds = removeIds; this.note = note; }
    }

    /**
     * The Quickstep session ends and the helper cleans up. Blocking (talks to the helper): call from the clean-up thread. Always returns a usable list: with no snapshot, or if the helper
     * cannot list the tasks, it is just {@code oldList} (the package-based clean-up) and the note says why.
     */
    public static Outcome finish(Context c, List<String> oldList, String target, Set<String> protectedPkgs) {
        Context app = c.getApplicationContext();
        LinkedHashMap<Integer, String> base = base(app);
        clearBase(app);                                            // one-shot: a snapshot is only ever valid for the session it was taken for
        List<String> none = new ArrayList<>();
        if (base == null) return new Outcome(oldList, none, "tasks: no start-of-session snapshot (the helper was not running when Quickstep began): package-based clean-up only");
        LinkedHashMap<Integer, String> now = ShellHelper.listTasks();
        if (now == null) return new Outcome(oldList, new ArrayList<Integer>() == null ? null : none, "tasks: the helper could not list Recents: package-based clean-up only").withIds(new ArrayList<Integer>());
        Plan plan = plan(base, now, oldList, AppOrigin.custom(app), target, protectedPkgs);
        List<String> stop = new ArrayList<>(plan.stopPackages);
        return new Outcome(stop, plan.removeIds, "tasks: " + plan.newTasks + " new since the session began, " + plan.stopPackages.size() + " app(s) closed, " + plan.removeIds.size()
                + " task(s) removed by id, " + plan.spared.size() + " app(s) spared because the custom account has a task of its own" + (plan.spared.isEmpty() ? "" : " " + plan.spared));
    }
}
