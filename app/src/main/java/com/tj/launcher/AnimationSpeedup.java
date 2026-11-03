package com.tj.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

/**
 * Y2b (Tj, 2026-11-03: "as fast as possible, instant if possible"): most of the return reset is animation time (shade opens, Recents opens, cards fly off).
 * While the reset runs, the three global animation scales are set to 0, then put back EXACTLY as they were. Needs WRITE_SECURE_SETTINGS (the same one-time adb grant as the
 * keyboard switch); without it nothing changes.
 * Safety (L10): the original values are written to our prefs BEFORE anything is changed; they are restored (1) when the reset is done, (2) by a 10 s failsafe,
 * (3) the next time any process of this app starts (TjApplication), and (4) by "Restore everything". A value that was not touched is never written.
 */
public final class AnimationSpeedup {
    private static final String TAG = "TjAnim";
    public static final String KEY = "reset_animations_off";   // switch, default ON (only does anything when the adb grant exists)
    private static final String PREFS = "tj_anim";
    private static final String[] NAMES = { "window_animation_scale", "transition_animation_scale", "animator_duration_scale" };
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private AnimationSpeedup() {}

    public static boolean permitted(Context c) { return c.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED; }
    public static boolean enabled(Context c) { return Features.get(c, KEY, true); }
    private static SharedPreferences p(Context c) { return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    /** Turns the animations off for the reset. Returns true if they are now off (so the caller can shorten its waits). */
    public static synchronized boolean begin(final Context c) {
        try {
            if (!enabled(c) || !permitted(c)) return false;
            restoreIfPending(c);                      // never stack on an earlier, interrupted run
            SharedPreferences sp = p(c);
            SharedPreferences.Editor e = sp.edit();
            for (String n : NAMES) {
                String cur = Settings.Global.getString(c.getContentResolver(), n);
                e.putString(n, cur == null ? "1.0" : cur);
            }
            e.putBoolean("pending", true).commit();   // the originals are on disk BEFORE the first change
            for (String n : NAMES) Settings.Global.putString(c.getContentResolver(), n, "0");
            MAIN.postDelayed(new Runnable() { @Override public void run() { restoreIfPending(c); } }, 10_000);   // failsafe
            Log.i(TAG, "animations off for the reset");
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "could not turn animations off", t);
            try { restoreIfPending(c); } catch (Throwable ignored) { }
            return false;
        }
    }

    public static void restoreLater(final Context c, long delayMs) {
        MAIN.postDelayed(new Runnable() { @Override public void run() { restoreIfPending(c); } }, delayMs);
    }

    /** Puts the saved values back (only if a run left them changed). Safe to call any time, from any process of ours. */
    public static synchronized void restoreIfPending(Context c) {
        try {
            SharedPreferences sp = p(c);
            if (!sp.getBoolean("pending", false)) return;
            if (permitted(c)) {
                for (String n : NAMES) {
                    String v = sp.getString(n, "1.0");
                    Settings.Global.putString(c.getContentResolver(), n, v);
                }
                Log.i(TAG, "animations restored");
            }
            sp.edit().putBoolean("pending", false).commit();
        } catch (Throwable t) { Log.e(TAG, "restore failed", t); }
    }
}
