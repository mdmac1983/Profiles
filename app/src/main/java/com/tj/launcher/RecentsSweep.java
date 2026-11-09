package com.tj.launcher;

import android.content.Context;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BE2 (Tj, 2026-11-07, twice: "No apps should appear at any time in the recents list in the custom launcher unless they are already unhidden in the custom launcher app drawer" / "At no time should there
 * ever be an app that is hidden from the app drawer for the custom launcher inside the recent apps for the custom launcher"; audit: research/RECENTS_AND_UNLOCK_AUDIT_2026-11-07.md).
 *
 * The old clean-up decided by ORIGIN (which launcher opened the app, from accessibility events and a task snapshot). Origin is a heuristic: an app whose window event was missed, or whose task already existed
 * when a Quickstep session began, was "the custom account's" for ever (BD1: Telegram and Claude). This class decides by MEMBERSHIP, the one fact that matters: in the custom launcher every Recents task whose
 * package is not in the custom drawer ({@link HiddenApps#isPackageShown}) goes, whatever its origin. With the shell helper the tasks are removed by id (the same call as swiping a card away: the app's
 * notification channel stays intact, nothing is force-stopped), invisibly, so there is no card to see, no animation and no flicker.
 *
 * Two policies: {@link #HIDDEN_ONLY} (the guarantee, any time in the custom launcher) and {@link #ALL} (a custom session STARTS clean: Tj, "completely clear the recent apps list every time the custom launcher
 * starts"). Never touched, in both: this app, the stock launcher, the keyboard, the system ({@link ResetPolicy#shouldKill}) and, when the caller names it, the app that is on screen right now.
 * Pure rules here (JVM-tested); the Android side is a bounded background job that never throws.
 */
public final class RecentsSweep {
    private static final String TAG = "TjSweep";
    /** Profiles settings switch. Shipped default ON; debug builds default OFF (the older emulator sections count tasks of apps they never put in the drawer and set what they test explicitly). */
    public static final String KEY = "recents_sweep";
    public static final int HIDDEN_ONLY = 0, ALL = 1;
    /** A sanity bound for one sweep (Android keeps far fewer tasks); the remainder is counted in {@link Plan#deferred} and the caller must NOT call the result complete. */
    public static final int MAX_TASKS = 300;

    /**
     * Pure: packages that are not "apps" in the sense of Tj's rule but transient system screens (an active call, an install or permission prompt, a file picker, Bluetooth / NFC / emergency screens). They are not in the
     * drawer, yet removing their task while they are on screen would break what the user is doing (code review of the first version, 2026-11-07). Never removed, by either policy.
     */
    public static boolean systemLike(String pkg) {
        if (pkg == null) return false;
        switch (pkg) {
            case "com.android.packageinstaller": case "com.google.android.packageinstaller":
            case "com.android.permissioncontroller": case "com.google.android.permissioncontroller":
            case "com.android.dialer": case "com.google.android.dialer": case "com.android.incallui":
            case "com.android.documentsui": case "com.google.android.documentsui":
            case "com.android.bluetooth": case "com.android.nfc": case "com.android.emergency": case "com.android.cellbroadcastreceiver":
            case "com.android.providers.downloads.ui": case "com.android.printspooler":
                return true;
            default:
                return false;
        }
    }

    private RecentsSweep() {}

    /** Pure: the default of the switch. */
    static boolean enabledDefault(boolean debuggable) { return !debuggable; }

    public static boolean enabled(Context c) {
        return Features.get(c, KEY, enabledDefault((c.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0));
    }

    /** Is this package an app of the custom drawer? */
    public interface Membership { boolean inDrawer(String pkg); }

    /** What a sweep decided. */
    public static final class Plan {
        /** Recents tasks to remove, by id (at most {@link #MAX_TASKS}; {@link ShellHelper#removeTasks} sends them in chunks the helper accepts). */
        public final List<Integer> removeIds = new ArrayList<>();
        /** The apps those tasks belong to (for the log). */
        public final Set<String> packages = new LinkedHashSet<>();
        public int tasks, protectedTasks, kept, deferred;
    }

    /**
     * Pure. {@code tasks} = id -> package of every Recents task ({@code dumpsys activity recents}, newest first). A task is removed when its app may be closed at all ({@code guard}), is not in {@code skip}
     * (the app on screen) and, for {@link #HIDDEN_ONLY}, is not in the drawer. {@code drawer} may be null for {@link #ALL}.
     */
    public static Plan plan(Map<Integer, String> tasks, int policy, Membership drawer, TaskOrigin.Guard guard, Set<String> skip) {
        Plan p = new Plan();
        if (tasks == null) return p;
        for (Map.Entry<Integer, String> e : tasks.entrySet()) {
            p.tasks++;
            String pkg = e.getValue();
            if (pkg == null || pkg.isEmpty() || e.getKey() == null || e.getKey() <= 0 || !guard.mayClose(pkg) || systemLike(pkg)) { p.protectedTasks++; continue; }
            if (skip != null && skip.contains(pkg)) { p.kept++; continue; }
            boolean remove = policy == ALL || drawer == null || !drawer.inDrawer(pkg);
            if (!remove) { p.kept++; continue; }
            if (p.removeIds.size() >= MAX_TASKS) { p.deferred++; continue; }
            p.removeIds.add(e.getKey());
            p.packages.add(pkg);
        }
        return p;
    }

    public static final int STEP_NOTHING = 0, STEP_REMEMBER = 1, STEP_SWEEP = 2;

    /**
     * Pure: the memory "an app of no drawer has been on screen". {@code atHome} = the window in front is the custom home or the Recents screen. At home with a hidden app remembered = sweep now (its card could be drawn
     * next); otherwise a hidden app (not in the drawer, may be closed at all, not system-like) in front is remembered; everything else changes nothing. A hidden app's own child windows (a share sheet, a picker, a
     * permission prompt) are "everything else": nothing is swept in the middle of its flow.
     */
    public static int hiddenSeenStep(boolean seen, boolean atHome, boolean pkgInDrawer, boolean pkgMayClose, boolean pkgSystemLike) {
        if (atHome) return seen ? STEP_SWEEP : STEP_NOTHING;
        if (!seen && !pkgInDrawer && pkgMayClose && !pkgSystemLike) return STEP_REMEMBER;
        return STEP_NOTHING;
    }

    /** Pure: the line for the lock log / the tablet report ("SWEEP lock: removed 2 task(s) ..."). Package names only. */
    static String describe(String why, int policy, Plan p, int removed) {
        StringBuilder b = new StringBuilder(why).append(policy == ALL ? " (complete)" : "").append(": ");
        if (removed < 0) return b.append("the helper stopped answering, ").append(p.removeIds.size()).append(" task(s) NOT removed").toString();
        b.append("removed ").append(removed).append(" of ").append(p.removeIds.size()).append(" task(s) ").append(policy == ALL ? "" : "of non-drawer apps ").append(p.packages);
        b.append("; ").append(p.kept).append(" kept, ").append(p.protectedTasks).append(" protected");
        if (p.deferred > 0) b.append(", ").append(p.deferred).append(" left for the next sweep");
        return b.toString();
    }

    // ------------------------------------------------------------------ Android side

    private static final AtomicBoolean BUSY = new AtomicBoolean(false);
    private static volatile long lastStartMs = 0;
    private static volatile String lastResult = "";
    private static final android.os.Handler MAIN = new android.os.Handler(android.os.Looper.getMainLooper());

    /** One request that could not run yet (a sweep was running, or the minimum gap was not over): the newest wins, it runs as soon as it can. Guarded by {@code PENDING_LOCK}. */
    private static final Object PENDING_LOCK = new Object();
    private static Request pending = null;
    private static final class Request {
        final Context app; final String why; final int policy; final Set<String> skip;
        Request(Context app, String why, int policy, Set<String> skip) { this.app = app; this.why = why; this.policy = policy; this.skip = skip; }
    }

    /** What the last sweep did (Profiles settings / the tablet report). */
    public static String lastResult() { return lastResult; }

    /**
     * Starts a sweep on its own thread (the helper answers in 100-400 ms; never wait for it on the main thread). A request that arrives while a sweep runs, or {@code minGapMs} after the last start, is NOT dropped (a
     * dropped request was a hole in the guarantee, code review 2026-11-07): the newest one waits and runs the moment it can. Needs the shell helper and the switch; otherwise it returns at once and changes nothing.
     */
    public static void requestAsync(final Context c, final String why, final int policy, final Set<String> skip, final long minGapMs) {
        try {
            final Context app = c.getApplicationContext();
            if (!enabled(app) || !ShellHelper.enabled(app) || CrashGuard.automationOff(app)) return;
            if (policy == HIDDEN_ONLY && !Mode.isCustom(app)) return;       // Quickstep may use every app
            long now = android.os.SystemClock.uptimeMillis();
            long wait = minGapMs - (now - lastStartMs);
            if (wait > 0 || !BUSY.compareAndSet(false, true)) {
                synchronized (PENDING_LOCK) { pending = new Request(app, why, policy, skip); }
                MAIN.postDelayed(new Runnable() { @Override public void run() { drain(); } }, Math.max(wait, 250));
                return;
            }
            start(app, why, policy, skip);
        } catch (Throwable t) { android.util.Log.e(TAG, "sweep request failed", t); }
    }

    /** BUSY is already ours: run the sweep on a thread, then take the waiting request (if any). */
    private static void start(final Context app, final String why, final int policy, final Set<String> skip) {
        lastStartMs = android.os.SystemClock.uptimeMillis();
        new Thread(new Runnable() { @Override public void run() {
            try { sweepNow(app, why, policy, skip); }
            catch (Throwable t) { android.util.Log.e(TAG, "sweep failed", t); }
            finally { BUSY.set(false); MAIN.post(new Runnable() { @Override public void run() { drain(); } }); }
        } }, "tj-sweep").start();
    }

    private static void drain() {
        Request r;
        synchronized (PENDING_LOCK) { r = pending; }
        if (r == null) return;
        if (android.os.SystemClock.uptimeMillis() - lastStartMs < 250) {   // right behind the last one: look again in a moment
            MAIN.postDelayed(new Runnable() { @Override public void run() { drain(); } }, 250);
            return;
        }
        if (!BUSY.compareAndSet(false, true)) return;                       // the running sweep drains when it ends
        synchronized (PENDING_LOCK) { if (pending == r) pending = null; else r = pending; }
        if (r == null) { BUSY.set(false); return; }
        start(r.app, r.why, r.policy, r.skip);
    }

    /** Blocking: call off the main thread. Returns what it did, or null when the helper is not there / nothing had to go. */
    static String sweepNow(final Context app, String why, int policy, Set<String> skip) {
        if (!ReturnReset.invisibleReady(app)) return null;                   // no helper: AP1 (RecentsFilter) is the fallback; cached 4 s, a refused connection fails at once
        if (policy == HIDDEN_ONLY && !Mode.isCustom(app)) return null;       // re-checked here: the mode may have changed while the job waited
        LinkedHashMap<Integer, String> tasks = ShellHelper.listTasks();
        if (tasks == null) return null;
        Membership drawer = new Membership() { @Override public boolean inDrawer(String pkg) { return HiddenApps.isPackageShown(app, pkg); } };
        Plan p = plan(tasks, policy, drawer, ReturnReset.guardFor(app), skip);
        if (p.removeIds.isEmpty()) return null;
        int n = ShellHelper.removeTasks(p.removeIds);
        String line = describe(why, policy, p, n);
        lastResult = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date()) + "  " + line;
        LockLog.add(app, "SWEEP", line);
        android.util.Log.i(TAG, "SWEEP " + line);
        return line;
    }
}
