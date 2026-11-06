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
    /** AN1: strict = also undo changes made behind our back (the old v0.7.3 - v0.7.6 behaviour); default OFF so the tile in the shade always answers. */
    public static boolean strict(Context c) { return Features.get(c, BatterySaverPolicy.STRICT_KEY, false); }
    public static boolean permitted(Context c) { return c.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == android.content.pm.PackageManager.PERMISSION_GRANTED; }

    /** Registers the observer once per process (idempotent) and checks once. Called from {@link TjApplication#onCreate}. */
    public static synchronized void start(Context c) {
        try {
            if (observer != null) return;
            final Context app = c.getApplicationContext();
            observer = new ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override public void onChange(boolean selfChange) {
                    // AG2 (Tj, 2026-11-06: the battery saver TILE in Quickstep does not work): every change of low_power is written to the lock log with the mode and the guard's switches, so a report says WHY
                    try { LockLog.add(app, "BATSAVER", "low_power is now " + Settings.Global.getString(app.getContentResolver(), BatterySaverPolicy.SETTING) + ", mode=" + Mode.get(app) + ", off-in-custom=" + enabled(app) + ", on-in-Quickstep=" + qsEnabled(app) + ", strict=" + strict(app) + ", power=" + powerSource(app) + (CrashGuard.automationOff(app) ? " PAUSED" : "")); } catch (Throwable ignored) { }
                    check(app, "setting changed", true);   // AN1: does nothing unless "strict" is on: a change made on the tile is left alone
                }
            };
            app.getContentResolver().registerContentObserver(Settings.Global.getUriFor(BatterySaverPolicy.SETTING), false, observer);
            // AN1: the rule is re-applied at a process start only in the first minutes after a boot; a restart of our process later (a Go device kills background processes often) must never undo a tap on the tile
            if (BatterySaverPolicy.startCheckWanted(strict(app), android.os.SystemClock.elapsedRealtime())) check(app, "process start");
        } catch (Throwable t) { Log.e(TAG, "could not watch battery saver", t); }
    }

    /** Switches battery saver off now if the rules say so. {@code why} is for the log / status line only. A DELIBERATE check (a mode change, the process start): the switch-on that follows is never capped. */
    public static void check(Context c, String why) { check(c, why, false); }

    /** {@code reactive}: called because the setting CHANGED (something else switched the saver off): only these switch-ons count toward the cap that protects against a tight loop. */
    static void check(Context c, String why, boolean reactive) {
        try {
            if (!BatterySaverPolicy.mayAct(reactive, strict(c))) return;   // AN1: a change behind our back (the tile, Android) is left alone unless "strict" is on
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
    private static void keepOnInQuickstep(Context c, ContentResolver cr, String why, boolean reactive) {
        long now = System.currentTimeMillis();
        if (Mode.isCustom(c)) return;                                  // the custom launcher wants it off: nothing to do
        boolean limited = false;
        synchronized (BatterySaverGuard.class) {
            // v0.7.6 (review of v0.7.5): "wanted" = the Quickstep switch is on, automation is not paused, the saver is off. Only a REACTIVE switch-on (the setting changed to 0 behind our back) uses and counts toward
            // the cap that protects against a tight loop; a deliberate entry into Quickstep (a mode change, the process start) always switches it on.
            if (!BatterySaverPolicy.deliberateOn(qsEnabled(c), false, CrashGuard.automationOff(c), false)) return;
            if (reactive && !BatterySaverPolicy.shouldTurnOn(qsEnabled(c), false, CrashGuard.automationOff(c), false, now, onWrites)) limited = true;   // only the cap holds it back
            else if (!permitted(c)) { Features.setString(c, BatterySaverPolicy.LAST_KEY, stamp(now) + "  battery saver is off in Quickstep but the adb permission (WRITE_SECURE_SETTINGS) is missing"); return; }
            else if (reactive) onWrites[onIdx++ % onWrites.length] = now;
        }
        if (limited) {   // say WHY the saver stayed off, so a tablet report shows it (Android turns it off by itself while charging)
            Features.setString(c, BatterySaverPolicy.LAST_KEY, stamp(now) + "  not switched on again in Quickstep: it was turned off " + BatterySaverPolicy.MAX_ON_WRITES + " times within 10 minutes (power: " + powerSource(c) + ")");
            LockLog.add(c, "BATSAVER", "cap reached: not switching it on again (" + why + ", power=" + powerSource(c) + ")");
            return;
        }
        Settings.Global.putInt(cr, BatterySaverPolicy.SETTING, 1);
        Features.setString(c, BatterySaverPolicy.LAST_KEY, stamp(now) + "  switched battery saver ON in Quickstep (" + why + ")");
        Log.i(TAG, "BATSAVER on (" + why + ")");
        LockLog.add(c, "BATSAVER", "guard switched it ON (Quickstep active; " + why + ")");   // AG2
    }

    /** "ac" / "usb" / "wireless" / "no" (not plugged in) / "?": the plugged-in state for the log and the report, because Android switches the saver off by itself while charging. Never throws. */
    static String powerSource(Context c) {
        try {
            android.content.Intent i = c.registerReceiver(null, new android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED));
            if (i == null) return "?";
            int p = i.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, 0);
            if (p == 0) return "no";
            if ((p & android.os.BatteryManager.BATTERY_PLUGGED_AC) != 0) return "ac";
            if ((p & android.os.BatteryManager.BATTERY_PLUGGED_USB) != 0) return "usb";
            if ((p & android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS) != 0) return "wireless";
            return "other";
        } catch (Throwable t) { return "?"; }
    }

    private static String stamp(long ms) { return new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date(ms)); }
}
