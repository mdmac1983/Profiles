package com.tj.launcher;

import java.util.ArrayList;
import java.util.List;

/**
 * HLP1 (open since 2026-11-05): on Tj's tablet the adb-started shell helper (ShellServer) is gone a few minutes after it was started, and nobody knows why: the start line sends its output to /dev/null, so a
 * death left no trace. Every invisible feature (clean-up at the lock, sweeping hidden apps out of Recents, a fresh-pixel power-menu patch) waits for the answer. This is the FLIGHT RECORDER's file format and
 * the reader of it (pure, JVM-tested): the helper writes one line per fact to a file the app can read (its own external files directory), and the tablet report prints the summary.
 *
 * Line format: {@code TYPE t=<epoch ms> key=value ...}. Types: START (pid, uid, where the file lives), BEAT (every 30 s: seconds alive, VmRSS, oom_score_adj, usb state), SHUTDOWN (the VM was asked to exit:
 * SIGTERM / SIGHUP / SIGINT ran the shutdown hook), EXCEPTION (an uncaught exception, with its text), LOGCAT (a kill-related line of the system log, copied the moment it appeared).
 * A helper that dies of SIGKILL (the low-memory killer, a vendor cleaner, a process-group kill) writes NOTHING at the end: its last BEAT, and the LOGCAT lines just before it, are the evidence.
 */
public final class HelperLife {
    private HelperLife() {}

    public static final long BEAT_MS = 30_000L;
    public static final int MAX_FILE_BYTES = 48 * 1024, KEEP_FILE_BYTES = 24 * 1024;

    /** Pure: one line of the file. {@code kv} = alternating key, value (values must not contain spaces; the text of an exception goes last as {@code rest}). */
    public static String line(String type, long tMs, String rest, String... kv) {
        StringBuilder b = new StringBuilder(type).append(" t=").append(tMs);
        for (int i = 0; i + 1 < kv.length; i += 2) b.append(' ').append(kv[i]).append('=').append(kv[i + 1] == null ? "" : kv[i + 1].replace(' ', '_'));
        if (rest != null && !rest.isEmpty()) b.append(' ').append(rest.replace('\n', ' ').replace('\r', ' '));
        String s = b.toString();
        return s.length() > 260 ? s.substring(0, 260) : s;
    }

    /** Pure: the {@code t=} value of a line, or -1. */
    public static long timeOf(String line) {
        if (line == null) return -1;
        int i = line.indexOf(" t=");
        if (i < 0) return -1;
        int j = line.indexOf(' ', i + 3);
        try { return Long.parseLong(line.substring(i + 3, j < 0 ? line.length() : j)); } catch (NumberFormatException e) { return -1; }
    }

    /** Pure: the value of {@code key=} in a line, or null. */
    public static String value(String line, String key) {
        if (line == null) return null;
        int i = line.indexOf(" " + key + "=");
        if (i < 0) return null;
        int s = i + key.length() + 2, j = line.indexOf(' ', s);
        return line.substring(s, j < 0 ? line.length() : j);
    }

    private static String type(String line) { int i = line.indexOf(' '); return i < 0 ? line : line.substring(0, i); }

    private static String clock(long ms) { return new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date(ms)); }

    /**
     * Pure: what the recorder says about the NEWEST run of the helper (everything after the last START line), in plain words, plus the last kill-related system log lines. {@code nowMs} = when the report is
     * written. An empty / missing file says so (a helper started before this version has no recorder).
     */
    public static String summarize(List<String> lines, long nowMs) {
        if (lines == null || lines.isEmpty()) return "(no flight recorder file: the helper has not been started since v0.8.7, or it could not write where the app can read)";
        int start = -1;
        for (int i = 0; i < lines.size(); i++) if (type(lines.get(i)).equals("START")) start = i;
        if (start < 0) return "(the recorder file has no START line)";
        int runs = 0;
        for (String l : lines) if (type(l).equals("START")) runs++;
        long t0 = timeOf(lines.get(start));
        String lastBeat = null, ended = null;
        int beats = 0;
        List<String> logcat = new ArrayList<>();
        for (int i = start + 1; i < lines.size(); i++) {
            String l = lines.get(i), ty = type(l);
            if (ty.equals("BEAT")) { beats++; lastBeat = l; }
            else if (ty.equals("SHUTDOWN") || ty.equals("EXCEPTION")) ended = l;
            else if (ty.equals("LOGCAT")) logcat.add(l);
        }
        long lastT = lastBeat != null ? timeOf(lastBeat) : t0;
        StringBuilder b = new StringBuilder();
        b.append("runs recorded: ").append(runs).append("; the newest started ").append(clock(t0)).append(" (").append(ProcessLife.age(nowMs - t0)).append(" before this report)");
        b.append("; heartbeats: ").append(beats);
        if (lastBeat != null) {
            b.append("; last heartbeat ").append(clock(lastT)).append(" (").append(ProcessLife.age(nowMs - lastT)).append(" ago, alive ").append(value(lastBeat, "up")).append(" s, VmRSS ").append(value(lastBeat, "rss"))
                    .append(" kB, oom_score_adj ").append(value(lastBeat, "oom")).append(", usb ").append(value(lastBeat, "usb")).append(")");
        }
        boolean silent = nowMs - lastT > 3 * BEAT_MS;
        if (ended != null) b.append("; ENDED: ").append(type(ended)).append(" at ").append(clock(timeOf(ended))).append(" - ").append(ended.length() > 120 ? ended.substring(0, 120) : ended);
        else if (silent) b.append("; STOPPED without a trace after ").append(ProcessLife.age(lastT - t0)).append(" (no shutdown hook, no exception: killed by a signal that cannot be caught, or the VM crashed)");
        else b.append("; still alive when the report was written");
        int from = Math.max(0, logcat.size() - 6);
        for (int i = from; i < logcat.size(); i++) b.append("\n    ").append(logcat.get(i));
        return b.toString();
    }

    /** Pure: the file text cut to its newest {@code keepBytes} at a line boundary once it passes {@code maxBytes} (the recorder must never fill the storage). */
    public static String trim(String all, int maxBytes, int keepBytes) {
        if (all == null || all.length() <= maxBytes) return all == null ? "" : all;
        String t = all.substring(all.length() - keepBytes);
        int nl = t.indexOf('\n');
        return nl >= 0 && nl + 1 < t.length() ? t.substring(nl + 1) : t;
    }
}
