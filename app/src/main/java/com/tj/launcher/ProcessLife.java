package com.tj.launcher;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * BG1 (Tj, 2026-11-08: "Can it be kept in ram?"): the answer depends on how often Android really kills and restarts this launcher's process on THIS tablet (2 GB, Android 10 Go) and at what priority the process
 * sits while the tablet is locked. Nothing about that was ever measured, so the unlock-speed ideas (keep-alive service, warm home) were guesses. This records it, cheaply, and the tablet report prints it:
 * <ul>
 * <li>every start of the MAIN process (wall-clock ms, the last {@link #KEEP}), so "started 3 times in the last 24 h" is a fact;</li>
 * <li>at every lock (screen off + 0.7 s, when the lock preparation runs) the process's {@code oom_score_adj} and its importance as the activity manager reports it, the last {@link #KEEP_LOCKS} samples: a
 *     process at 700+ is a candidate for the low-memory killer, one at 0-250 is not.</li>
 * </ul>
 * Pure helpers (ring format, counting, wording) are JVM-tested; the Android calls are two preference writes with {@code apply()} (never an fsync on the main thread) and two cheap reads.
 */
public final class ProcessLife {
    private ProcessLife() {}

    static final int KEEP = 24, KEEP_LOCKS = 8;
    private static final String PREFS = "tj_life", STARTS = "starts", LOCKS = "locks";

    // ---------------------------------------------------------------- pure

    /** Pure: {@code ring} (comma-separated numbers, oldest first) with {@code value} added and cut to the newest {@code keep}. Unreadable entries are dropped. */
    public static String add(String ring, long value, int keep) {
        long[] old = parse(ring);
        int from = Math.max(0, old.length + 1 - keep);
        StringBuilder b = new StringBuilder();
        for (int i = from; i < old.length; i++) b.append(old[i]).append(',');
        b.append(value);
        return b.toString();
    }

    /** Pure: the numbers in {@code ring}; empty / null / garbage entries are skipped. */
    public static long[] parse(String ring) {
        if (ring == null || ring.trim().isEmpty()) return new long[0];
        String[] parts = ring.split(",");
        long[] out = new long[parts.length];
        int n = 0;
        for (String p : parts) { try { out[n] = Long.parseLong(p.trim()); n++; } catch (NumberFormatException ignored) { } }
        return n == out.length ? out : java.util.Arrays.copyOf(out, n);
    }

    /** Pure: how many of {@code times} lie in the {@code windowMs} before {@code nowMs} (a time in the future does not count). */
    public static int countSince(long[] times, long nowMs, long windowMs) {
        int n = 0;
        for (long t : times) if (t <= nowMs && nowMs - t <= windowMs) n++;
        return n;
    }

    /** Pure: "3 min", "2 h 5 min", "1 d 3 h" for a duration in milliseconds. */
    public static String age(long ms) {
        if (ms < 0) return "?";
        long min = ms / 60_000L;
        if (min < 1) return (ms / 1000L) + " s";
        if (min < 60) return min + " min";
        long h = min / 60;
        if (h < 24) return h + " h " + (min % 60) + " min";
        return (h / 24) + " d " + (h % 24) + " h";
    }

    /** Pure: what an {@code oom_score_adj} means for survival, in words (Android's own scale: 0 foreground, 100 visible, 200 perceptible, 250 / 300 service, 700 previous app, 900+ cached). */
    public static String oomWords(int adj) {
        if (adj < 0) return "system-pinned (never killed)";
        if (adj <= 100) return "foreground / visible: killed last";
        if (adj <= 300) return "perceptible or service: killed late";
        if (adj < 700) return "background service range";
        if (adj < 900) return "previous / home app range: killed when memory is short";
        return "CACHED: the first to be killed";
    }

    /**
     * Pure: the report block. {@code starts} = wall-clock ms of every recorded start (oldest first), {@code locks} = "wallMs:oomAdj:importance" samples (oldest first).
     * Says how often the process started in the last 24 h, how old this process is, and the priority it had at the last locks.
     */
    public static String describe(long[] starts, String[] locks, long nowMs, long thisProcessStartMs, boolean keepAliveOn) {
        StringBuilder b = new StringBuilder();
        b.append("this process is ").append(age(nowMs - thisProcessStartMs)).append(" old; process starts recorded: ").append(starts.length)
                .append(", in the last 24 h: ").append(countSince(starts, nowMs, 24L * 3600_000L)).append(", in the last hour: ").append(countSince(starts, nowMs, 3600_000L));
        if (starts.length > 0) {
            b.append(" (ages: ");
            int from = Math.max(0, starts.length - 6);
            for (int i = starts.length - 1; i >= from; i--) b.append(age(nowMs - starts[i])).append(i > from ? ", " : "");
            b.append(")");
        }
        b.append("; keep-alive service switch ").append(keepAliveOn ? "ON" : "off");
        if (locks.length == 0) b.append("; priority while locked: (no lock sampled yet)");
        else {
            b.append("; priority while locked (oom_score_adj / importance, newest last): ");
            for (int i = 0; i < locks.length; i++) {
                String[] p = locks[i].split(":");
                if (p.length < 3) continue;
                long at; int adj, imp;
                try { at = Long.parseLong(p[0]); adj = Integer.parseInt(p[1]); imp = Integer.parseInt(p[2]); } catch (NumberFormatException e) { continue; }
                b.append(i == 0 ? "" : " | ").append(age(nowMs - at)).append(" ago ").append(adj).append(" / ").append(imp);
            }
            String[] last = locks[locks.length - 1].split(":");
            if (last.length >= 3) { try { b.append(" -> last: ").append(oomWords(Integer.parseInt(last[1]))); } catch (NumberFormatException ignored) { } }
        }
        return b.toString();
    }

    // ---------------------------------------------------------------- Android

    private static SharedPreferences p(Context c) { return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    /** Called by the MAIN process at start (Application.onCreate): one preference write with apply(). Never throws. */
    public static void noteStart(Context c) {
        try {
            SharedPreferences sp = p(c);
            sp.edit().putString(STARTS, add(sp.getString(STARTS, ""), System.currentTimeMillis(), KEEP)).apply();
        } catch (Throwable ignored) { }
    }

    /** Called when the lock preparation runs (the screen has just gone off): samples this process's oom_score_adj and importance. Never throws. */
    public static void noteLock(Context c) {
        try {
            int adj = readOomAdj();
            int imp = -1;
            android.app.ActivityManager am = (android.app.ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                int me = android.os.Process.myPid();
                java.util.List<android.app.ActivityManager.RunningAppProcessInfo> l = am.getRunningAppProcesses();
                if (l != null) for (android.app.ActivityManager.RunningAppProcessInfo i : l) if (i.pid == me) { imp = i.importance; break; }
            }
            SharedPreferences sp = p(c);
            String old = sp.getString(LOCKS, "");
            String[] parts = old.isEmpty() ? new String[0] : old.split(";");
            int from = Math.max(0, parts.length + 1 - KEEP_LOCKS);
            StringBuilder b = new StringBuilder();
            for (int i = from; i < parts.length; i++) b.append(parts[i]).append(';');
            b.append(System.currentTimeMillis()).append(':').append(adj).append(':').append(imp);
            sp.edit().putString(LOCKS, b.toString()).apply();
        } catch (Throwable ignored) { }
    }

    /** This process's {@code oom_score_adj} ({@code -1000} when unreadable). */
    static int readOomAdj() {
        try {
            java.io.BufferedReader r = new java.io.BufferedReader(new java.io.FileReader("/proc/self/oom_score_adj"));
            try { return Integer.parseInt(r.readLine().trim()); } finally { r.close(); }
        } catch (Throwable t) { return -1000; }
    }

    /** The report block for this process; never throws. */
    public static String report(Context c) {
        try {
            SharedPreferences sp = p(c);
            String lk = sp.getString(LOCKS, "");
            long started = System.currentTimeMillis() - (android.os.SystemClock.elapsedRealtime() - android.os.Process.getStartElapsedRealtime());
            return describe(parse(sp.getString(STARTS, "")), lk.isEmpty() ? new String[0] : lk.split(";"), System.currentTimeMillis(), started, KeepAliveService.enabled(c));
        } catch (Throwable t) { return "(unreadable)"; }
    }
}
