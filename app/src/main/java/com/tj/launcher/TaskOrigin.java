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
 * AB3 (Tj: "never show notifications from apps opened in the quickstep launcher"): an app that BOTH worlds use and that has a notification in the shade is not spared after all: leaving its notification
 * in the custom account's shade would show Quickstep's app there, so such an app is force-stopped (which also cancels its notifications). Apps without a notification are spared as before.
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

    /** May this app be closed at all? (the caller's rule: never this app, the stock launcher, the keyboard, the system; the helper checks its own deny-list again) */
    public interface Guard { boolean mayClose(String pkg); }

    /** What to do at the end of a session. */
    public static final class Plan {
        /** Apps that exist only in Quickstep: force-stop (process, notifications, cards). */
        public final Set<String> stopPackages = new LinkedHashSet<>();
        /** Apps that ALSO have a custom-account task: keep the app, remove just Quickstep's task(s) by id. */
        public final List<Integer> removeIds = new ArrayList<>();
        /** Apps the old package list wanted to force-stop but that have a custom-account task: spared. */
        public final Set<String> spared = new LinkedHashSet<>();
        /** AB3: shared apps that have a notification, closed after all (subset of stopPackages). */
        public final Set<String> notificationStops = new LinkedHashSet<>();
        public int newTasks = 0;
    }

    /**
     * @param base      the tasks listed when the Quickstep session began
     * @param now       the tasks listed now
     * @param pending   the packages the accessibility-based tracking says Quickstep opened (AppOrigin.pending)
     * @param custom    packages the custom launcher itself started (AppOrigin.custom)
     * @param target    the app that must always stay (Solitaire)
     * @param guard     false for packages that must never be touched (this app, the stock launcher, the keyboard, the system): decided by the caller with ResetPolicy
     */
    public static Plan plan(Map<Integer, String> base, Map<Integer, String> now, Collection<String> pending, Set<String> custom, String target, Guard guard) {
        return plan(base, now, pending, custom, target, guard, null);
    }

    /** @param notified packages that have a notification in the shade now (null = not known: shared apps are spared as before) */
    public static Plan plan(Map<Integer, String> base, Map<Integer, String> now, Collection<String> pending, Set<String> custom, String target, Guard guard, Set<String> notified) {
        Plan plan = new Plan();
        Set<String> basePkgs = new LinkedHashSet<>(base.values());
        for (Map.Entry<Integer, String> e : now.entrySet()) {
            if (base.containsKey(e.getKey())) continue;           // existed when the session began: the custom account's
            String pkg = e.getValue();
            if (pkg == null || pkg.equals(target) || !guard.mayClose(pkg)) continue;
            plan.newTasks++;
            if (basePkgs.contains(pkg) || custom.contains(pkg)) {
                if (notified != null && notified.contains(pkg)) { plan.stopPackages.add(pkg); plan.notificationStops.add(pkg); }   // AB3: its notification must not stay behind
                else plan.removeIds.add(e.getKey());
            }
            else plan.stopPackages.add(pkg);
        }
        if (pending != null) for (String pkg : pending) {
            if (pkg == null || pkg.equals(target) || !guard.mayClose(pkg)) continue;
            if (basePkgs.contains(pkg) || custom.contains(pkg)) {     // the same "shared" test as for new tasks above (AppOrigin.pending already leaves custom apps out; this keeps plan() consistent on its own)
                if (notified != null && notified.contains(pkg)) { plan.stopPackages.add(pkg); plan.notificationStops.add(pkg); }
                else plan.spared.add(pkg);                           // the old list would have closed an app the custom account is using
            }
            else plan.stopPackages.add(pkg);
        }
        // a task of an app that is being force-stopped goes with it: no separate removal
        if (!plan.notificationStops.isEmpty()) {
            List<Integer> keep = new ArrayList<>();
            for (Integer id : plan.removeIds) { String pk = now.get(id); if (pk == null || !plan.stopPackages.contains(pk)) keep.add(id); }
            plan.removeIds.clear(); plan.removeIds.addAll(keep);
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
        /** BE3: the helper's task list was read (so {@link #removeIds} is the complete answer, not a fallback). */
        public final boolean listed;
        Outcome(List<String> stop, List<Integer> removeIds, String note) { this(stop, removeIds, note, true); }
        Outcome(List<String> stop, List<Integer> removeIds, String note, boolean listed) { this.stop = stop; this.removeIds = removeIds; this.note = note; this.listed = listed; }
    }

    /**
     * The Quickstep session ends and the helper cleans up. Blocking (talks to the helper): call from the clean-up thread. Always returns a usable list: with no snapshot, or if the helper
     * cannot list the tasks, it is just {@code oldList} (the package-based clean-up) and the note says why.
     */
    public static Outcome finish(Context c, List<String> oldList, String target, Guard guard) { return finish(c, oldList, target, guard, false); }

    /**
     * @param complete BE3 (clean start): EVERY Recents task that may be closed at all is removed by id, whatever its origin ({@link RecentsSweep#ALL}); the app list to force-stop (processes, notifications) is still the
     *                 origin-based one, because force-stopping an app also puts it into Android's "stopped" state, which silences its pushes until it is opened again: only apps Quickstep opened get that.
     */
    public static Outcome finish(Context c, List<String> oldList, String target, Guard guard, boolean complete) {
        Context app = c.getApplicationContext();
        LinkedHashMap<Integer, String> base = base(app);
        clearBase(app);                                            // one-shot: a snapshot is only ever valid for the session it was taken for
        List<Integer> noIds = new ArrayList<>();
        if (complete) {
            LinkedHashMap<Integer, String> all = ShellHelper.listTasks();
            if (all == null) return new Outcome(oldList, noIds, "tasks: the helper could not list Recents: package-based clean-up only (Recents NOT verified empty)", false);
            RecentsSweep.Plan sp = RecentsSweep.plan(all, RecentsSweep.ALL, null, guard, null);
            List<String> stopAll = new ArrayList<>(oldList);
            if (base != null) for (String p : plan(base, all, oldList, AppOrigin.custom(app), target, guard).stopPackages) if (!stopAll.contains(p)) stopAll.add(p);
            android.util.Log.i("TjTasks", "complete clear: tasks now " + encode(all) + " | remove by id " + sp.removeIds + " | stop " + stopAll);
            return new Outcome(stopAll, sp.removeIds, "tasks: complete clear (clean start): " + sp.removeIds.size() + " of " + all.size() + " task(s) removed by id, " + sp.protectedTasks + " protected (us, the stock launcher, the keyboard, the system), " + stopAll.size() + " app(s) force-stopped", true);
        }
        if (base == null) return new Outcome(oldList, noIds, "tasks: no start-of-session snapshot (the helper was not running when Quickstep began): package-based clean-up only", false);
        LinkedHashMap<Integer, String> now = ShellHelper.listTasks();
        if (now == null) return new Outcome(oldList, noIds, "tasks: the helper could not list Recents: package-based clean-up only", false);
        Plan plan = plan(base, now, oldList, AppOrigin.custom(app), target, guard);
        if (!plan.spared.isEmpty() || !plan.removeIds.isEmpty()) {     // AB3: shared apps exist: do any of them have a notification? (one extra helper call, only then)
            java.util.Set<String> notified = ShellHelper.notifPackages();
            if (notified != null && !notified.isEmpty()) plan = plan(base, now, oldList, AppOrigin.custom(app), target, guard, notified);
        }
        List<String> stop = new ArrayList<>(plan.stopPackages);
        // the evidence a failed emulator check needs (2026-11-05, run 99: the custom account's Contacts task vanished although it was "spared"): package names and task ids only, no personal data
        android.util.Log.i("TjTasks", "session end: base " + encode(base) + " | now " + encode(now) + " | stop " + plan.stopPackages + " | remove by id " + plan.removeIds + " | spared " + plan.spared);
        return new Outcome(stop, plan.removeIds, "tasks: " + plan.newTasks + " new since the session began, " + plan.stopPackages.size() + " app(s) closed, " + plan.removeIds.size()
                + " task(s) removed by id, " + plan.spared.size() + " app(s) spared because the custom account has a task of its own" + (plan.spared.isEmpty() ? "" : " " + plan.spared)
                + (plan.notificationStops.isEmpty() ? "" : ", " + plan.notificationStops.size() + " shared app(s) closed because they have a notification " + plan.notificationStops));
    }
}
