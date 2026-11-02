package com.tj.launcher;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Crash-loop breaker: if the launcher process dies of an uncaught exception 3 times within 2 minutes, switch to QUICKSTEP and
 * restore every saved modification, so a bug can never trap Tj in a crash loop. Always hands the exception on to the previous handler.
 */
public final class CrashGuard {
    private static final String PREFS = "tj_crash";
    static final int LIMIT = 3;
    static final long WINDOW_MS = 120_000;

    private CrashGuard() {}

    /** Pure decision (JVM-testable): given previous crash times (ms) and now, how many are inside the window, including this one. */
    public static int countInWindow(long[] previous, long now) {
        int n = 1;
        for (long t : previous) if (t > 0 && now - t <= WINDOW_MS) n++;
        return n;
    }

    public static void install(final Context app) {
        final Thread.UncaughtExceptionHandler prev = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override public void uncaughtException(Thread t, Throwable e) {
                try { record(app); } catch (Throwable ignored) { }
                if (prev != null) prev.uncaughtException(t, e);
            }
        });
    }

    static void record(Context app) {
        SharedPreferences p = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        long[] prev = { p.getLong("t1", 0), p.getLong("t2", 0), p.getLong("t3", 0) };
        int n = countInWindow(prev, now);
        p.edit().putLong("t3", prev[1]).putLong("t2", prev[0]).putLong("t1", now).commit();
        if (n >= LIMIT) {
            Mode.set(app, Mode.QUICKSTEP);
            RestoreStore.restoreAll(app);
            p.edit().putBoolean("tripped", true).commit();
        }
    }

    public static boolean wasTripped(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("tripped", false);
    }

    public static void clearTripped(Context c) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("tripped", false)
                .putLong("t1", 0).putLong("t2", 0).putLong("t3", 0).commit();
    }
}
