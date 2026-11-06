package com.tj.launcher;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

/**
 * AC3: turns battery saver off the moment it is on while the custom launcher is active. See {@link BatterySaverPolicy} for the why; this class is the Android side (a ContentObserver on
 * {@code Settings.Global low_power} registered once per process from {@link TjApplication}, plus a check whenever the mode becomes custom). Cheap, exception-safe, never throws.
 * Quickstep is never touched. Needs the WRITE_SECURE_SETTINGS grant (already part of the adb setup): without it the guard does nothing and says so in its status line.
 */
public final class BatterySaverGuard {
    private static final String TAG = "TjBatSaver";
    private static final long[] writes = new long[BatterySaverPolicy.MAX_WRITES];
    private static int writeIdx = 0;
    private static ContentObserver observer;   // held strongly: the content resolver only keeps a weak reference

    private BatterySaverGuard() {}

    private static final long[] onWrites = new long[BatterySaverPolicy.MAX_ON_WRITES];   // AG1: when the guard last switched the saver ON
    private static int onIdx = 0;

    public static boolean enabled(Context c) { return Features.get(c, BatterySaverPolicy.KEY, true); }
    public static boolean qsEnabled(Context c) { return Features.get(c, BatterySaverPolicy.QS_KEY, true); }
    public static boolean permitted(Context c) { return c.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == android.content.pm.PackageManager.PERMISSION_GRANTED; }

    /** Registers the observer once per process (idempotent) and checks once. Called from {@link TjApplication#onCreate}. */
    public static synchronized void start(Context c) {
        try {
            if (observer != null) return;
            final Context app = c.getApplicationContext();
            observer = new ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override public void onChange(boolean selfChange) {
                    // AG2 (Tj, 2026-11-06: the battery saver TILE in Quickstep does not work): every change of low_power is written to the lock log with the mode and the guard's switches, so a report says WHY
                    try { LockLog.add(app, "BATSAVER", "low_power is now " + Settings.Global.getString(app.getContentResolver(), BatterySaverPolicy.SETTING) + ", mode=" + Mode.get(app) + ", off-in-custom=" + enabled(app) + ", on-in-Quickstep=" + qsEnabled(app) + ", power=" + powerSource(app) + (CrashGuard.automationOff(app) ? " PAUSED" : "")); } catch (Throwable ignored) { }
                    check(app, "setting changed", true);
                }
            };
            app.getContentResolver().registerContentObserver(Settings.Global.getUriFor(BatterySaverPolicy.SETTING), false, observer);
            check(app, "process start");
        } catch (Throwable t) { Log.e(TAG, "could not watch battery saver", t); }
    }

    /** Switches battery saver off now if the rules say so. {@code why} is for the log / status line only. A DELIBERATE check (a mode change, the process start): the switch-on that follows is never capped. */
    public static void check(Context c, String why) { check(c, why, false); }

    /** {@code reactive}: called because the setting CHANGED (something else switched the saver off): only these switch-ons count toward the cap that protects against a tight loop. */
    static void check(Context c, String why, boolean reactive) {
        try {
            ContentResolver cr = c.getContentResolver();
            boolean on = BatterySaverPolicy.isOn(Settings.Global.getString(cr, BatterySaverPolicy.SETTING));
            if (!on) { keepOnInQuickstep(c, cr, why, reactive); return; }   // custom: nothing to do (one settings read + one preference read); Quickstep: AG1 keeps it on
            long now = System.currentTimeMillis();
            synchronized (BatterySaverGuard.class) {
                if (!BatterySaverPolicy.shouldTurnOff(enabled(c), Mode.isCustom(c), CrashGuard.automationOff(c), true, now, writes)) return;
                if (!permitted(c)) { Features.setString(c, BatterySaverPolicy.LAST_KEY, stamp(now) + "  battery saver is on but the adb permission (WRITE_SECURE_SETTINGS) is missing"); return; }
                writes[writeIdx++ % writes.length] = now;
            }
            Settings.Global.putInt(cr, BatterySaverPolicy.SETTING, 0);
            if (BatterySaverPolicy.isOn(Settings.Global.getString(cr, BatterySaverPolicy.STICKY_SETTING))) Settings.Global.putInt(cr, BatterySaverPolicy.STICKY_SETTING, 0);
            Features.setString(c, BatterySaverPolicy.LAST_KEY, stamp(now) + "  switched battery saver off in the custom launcher (" + why + ")");
            Log.i(TAG, "BATSAVER off (" + why + ")");
            LockLog.add(c, "BATSAVER", "guard switched it OFF (custom launcher active; " + why + ")");   // AG2
        } catch (Throwable t) { Log.e(TAG, "battery saver check failed", t); }
    }

    /** AG1 (Tj, 2026-11-06): the saver FOLLOWS the launcher. In Quickstep a saver that is off is switched back ON (switch in Launcher control, default ON); at most {@link BatterySaverPolicy#MAX_ON_WRITES} times per 10 minutes. */
    private static void keepOnInQuickstep(Context c, ContentResolver cr, String why) {
        long now = System.currentTimeMillis();
        if (Mode.isCustom(c)) return;                                  // the custom launcher wants it off: nothing to do
        synchronized (BatterySaverGuard.class) {
            if (!BatterySaverPolicy.shouldTurnOn(qsEnabled(c), false, CrashGuard.automationOff(c), false, now, onWrites)) return;
            if (!permitted(c)) { Features.setString(c, BatterySaverPolicy.LAST_KEY, stamp(now) + "  battery saver is off in Quickstep but the adb permission (WRITE_SECURE_SETTINGS) is missing"); return; }
            onWrites[onIdx++ % onWrites.length] = now;
        }
        Settings.Global.putInt(cr, BatterySaverPolicy.SETTING, 1);
        Features.setString(c, BatterySaverPolicy.LAST_KEY, stamp(now) + "  switched battery saver ON in Quickstep (" + why + ")");
        Log.i(TAG, "BATSAVER on (" + why + ")");
        LockLog.add(c, "BATSAVER", "guard switched it ON (Quickstep active; " + why + ")");   // AG2
    }

    private static String stamp(long ms) { return new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date(ms)); }
}
