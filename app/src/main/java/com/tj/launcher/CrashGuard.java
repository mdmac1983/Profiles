package com.tj.launcher;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Crash-loop breaker: if the launcher process dies of an uncaught exception 3 times within 2 minutes, switch to QUICKSTEP and
 * restore every saved modification, so a bug can never trap Tj in a crash loop. Always hands the exception on to the previous handler.
 *
 * KA1 (v0.7.2): one kind of crash is taken by a FUSE instead: Android crashing us over the keep-alive notification ("Bad notification ... Couldn't create icon"; it crash-looped the real tablet on 2026-11-05 and the
 * breaker then wiped Tj's switches). That first crash only switches keep-alive off ({@link KeepAliveService#blowFuse}) and is NOT counted here; a repeat while the fuse is already blown counts like any crash.
 * CBR1 (v0.7.2): a tripped breaker is cleared by the first start of a NEWER version (a fair try; three new crashes trip it again). The mode is left as it is: the way back to custom stays Tj's choice.
 */
public final class CrashGuard {
    private static final String PREFS = "tj_crash";
    static final int LIMIT = 3;
    static final long WINDOW_MS = 120_000;
    /** CBR1: the versionCode that tripped the breaker (absent = tripped by a version older than this logic). */
    static final String TRIP_VERSION_KEY = "trip_version";

    private CrashGuard() {}

    /** Pure decision (JVM-testable): given previous crash times (ms) and now, how many are inside the window, including this one. */
    public static int countInWindow(long[] previous, long now) {
        int n = 1;
        for (long t : previous) if (t > 0 && now - t <= WINDOW_MS) n++;
        return n;
    }

    /** Pure (CBR1): clear a tripped breaker at start-up? Only when it is tripped and the installed versionCode is newer than the one that tripped it (0 = tripped before this logic existed). */
    public static boolean shouldAutoClear(boolean tripped, int tripVersion, int installedVersion) {
        return tripped && installedVersion > tripVersion;
    }

    public static void install(final Context app) {
        final Thread.UncaughtExceptionHandler prev = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override public void uncaughtException(Thread t, Throwable e) {
                try { record(app, e); } catch (Throwable ignored) { }
                if (prev != null) prev.uncaughtException(t, e);
            }
        });
    }

    static void record(Context app, Throwable e) {
        // KA1: the keep-alive notification crash is the fuse's, not the breaker's (only the first one: see KeepAlivePolicy.fuseHandles).
        if (KeepAlivePolicy.fuseHandles(e, KeepAliveService.blocked(app))) { KeepAliveService.blowFuse(app, e); return; }
        SharedPreferences p = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        long[] prev = { p.getLong("t1", 0), p.getLong("t2", 0), p.getLong("t3", 0) };
        int n = countInWindow(prev, now);
        p.edit().putLong("t3", prev[1]).putLong("t2", prev[0]).putLong("t1", now).commit();
        if (n >= LIMIT) {
            Mode.set(app, Mode.QUICKSTEP);
            RestoreStore.restoreAll(app);
            p.edit().putBoolean("tripped", true).putInt(TRIP_VERSION_KEY, installedVersion(app)).commit();
        }
    }

    public static boolean wasTripped(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("tripped", false);
    }

    public static void clearTripped(Context c) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("tripped", false)
                .putLong("t1", 0).putLong("t2", 0).putLong("t3", 0).putInt(TRIP_VERSION_KEY, 0).commit();
    }

    /** CBR1: called once per process start (TjApplication). Cheap: one preference read unless the breaker is tripped. Never throws. */
    public static void clearIfNewer(Context app) {
        try {
            SharedPreferences p = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            if (!p.getBoolean("tripped", false)) return;
            int installed = installedVersion(app);
            if (shouldAutoClear(true, p.getInt(TRIP_VERSION_KEY, 0), installed)) {
                clearTripped(app);
                android.util.Log.i("TjCrash", "crash breaker cleared at start-up: version " + installed + " is newer than the one that tripped it");
            }
        } catch (Throwable ignored) { }
    }

    /** The installed versionCode, or 0 if it cannot be read (then nothing is ever auto-cleared). */
    static int installedVersion(Context c) {
        try { return c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionCode; }
        catch (Throwable t) { return 0; }
    }
}
