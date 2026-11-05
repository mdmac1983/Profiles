package com.tj.launcher;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;

/**
 * AC1/AC7 (found while reading the code for Tj's 2026-11-05 requests): "sometimes the plain PIN opens Quickstep", "a couple of times the gesture opened the custom launcher". The tablet's logcat only keeps about
 * three minutes, so the cause of a one-off could never be read afterwards. This is a small PERSISTENT log of what the lock screen logic saw and decided (screen on/off, PIN pad shown, the KIND of key tapped,
 * the first two key kinds at Enter, handshake accepted / withdrawn, the unlock decision with the mode before and after, how long Enter to unlock took). It goes into the tablet report.
 *
 * PRIVACY (hard rule, same as the unlock handshakes): never a digit, never how many digits, never the PIN. Callers pass key CLASSES only and at most two of them ({@link UnlockMethods#padLead}).
 * Size: a ring file of at most {@link #MAX_BYTES}; when it grows past that the oldest lines are dropped. Never throws, never blocks the lock screen logic for more than one small append.
 */
public final class LockLog {
    static final String FILE = "lock-events.log";
    public static final int MAX_BYTES = 48 * 1024;
    public static final int KEEP_BYTES = 32 * 1024;
    private static final Object LOCK = new Object();
    private static int sinceTrimCheck = 0;

    private LockLog() {}

    /** Pure: one log line ("HH:mm:ss.SSS EVENT detail"), flattened to a single line and cut to 200 characters. */
    public static String line(long nowMs, String event, String detail) {
        String d = detail == null ? "" : detail.replace('\r', ' ').replace('\n', ' ');
        String l = new java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.US).format(new java.util.Date(nowMs)) + " " + (event == null ? "?" : event) + (d.isEmpty() ? "" : " " + d);
        if (l.length() > 200) l = l.substring(0, 200);
        return l + "\n";
    }

    /** Pure: {@code all} if it fits in {@code max} characters, else its last {@code keep} characters starting at a line boundary. */
    public static String trim(String all, int max, int keep) {
        if (all == null || all.length() <= max) return all == null ? "" : all;
        String t = all.substring(all.length() - keep);
        int nl = t.indexOf('\n');
        return nl >= 0 && nl + 1 < t.length() ? t.substring(nl + 1) : t;
    }

    /** Pure: the last {@code n} lines of {@code all}. */
    public static String tailLines(String all, int n) {
        if (all == null || all.isEmpty() || n <= 0) return "";
        int end = all.length();
        if (all.charAt(end - 1) == '\n') end--;
        int pos = end, seen = 0;
        while (pos > 0 && seen < n) { pos = all.lastIndexOf('\n', pos - 1); seen++; if (pos < 0) break; }
        String t = all.substring(pos < 0 ? 0 : pos + 1);
        return t.endsWith("\n") ? t : t + "\n";
    }

    /** Appends one line. Safe from any thread. */
    public static void add(Context c, String event, String detail) {
        try {
            String l = line(System.currentTimeMillis(), event, detail);
            File f = c.getApplicationContext().getFileStreamPath(FILE);
            synchronized (LOCK) {
                FileOutputStream o = new FileOutputStream(f, true);
                try { o.write(l.getBytes("UTF-8")); } finally { o.close(); }
                if (++sinceTrimCheck >= 40 || f.length() > MAX_BYTES) { sinceTrimCheck = 0; if (f.length() > MAX_BYTES) rewriteTrimmed(f); }
            }
        } catch (Throwable ignored) { }
    }

    private static void rewriteTrimmed(File f) throws java.io.IOException {
        String all = read(f);
        String t = trim(all, MAX_BYTES, KEEP_BYTES);
        FileOutputStream o = new FileOutputStream(f, false);
        try { o.write(t.getBytes("UTF-8")); } finally { o.close(); }
    }

    private static String read(File f) {
        try {
            if (!f.exists()) return "";
            java.io.FileInputStream in = new java.io.FileInputStream(f);
            try {
                java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[4096]; int n;
                while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
                return bo.toString("UTF-8");
            } finally { in.close(); }
        } catch (Throwable t) { return ""; }
    }

    /** The last {@code n} lines (for the report and Launcher control); "" when there is no log. */
    public static String tail(Context c, int n) {
        synchronized (LOCK) { return tailLines(read(c.getApplicationContext().getFileStreamPath(FILE)), n); }
    }
}
