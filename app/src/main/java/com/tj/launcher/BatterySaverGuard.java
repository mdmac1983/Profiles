package com.tj.launcher;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

/**
 * AC3: turns battery saver off the moment the custom launcher becomes active. See {@link BatterySaverPolicy} for the why and for AZ2 (the reason this class no longer switches the saver ON and heals instead).
 * This class is the Android side: a ContentObserver on {@code Settings.Global low_power} (log + the opt-in strict mode), a ContentObserver on the "manual activation" counter (the self-heal), the one-time
 * unstick, and the check whenever the custom launcher becomes active. All registered once per process from {@link TjApplication} / the accessibility service. Cheap, exception-safe, never throws.
 * Quickstep is never touched. Needs the WRITE_SECURE_SETTINGS grant (already part of the adb setup): without it the guard does nothing and says so in its status line.
 */
public final class BatterySaverGuard {
    private static final String TAG = "TjBatSaver";
    private static final long[] writes = new long[BatterySaverPolicy.MAX_WRITES];
    private static int writeIdx = 0;
    private static final long[] heals = new long[BatterySaverPolicy.MAX_HEALS];
    private static int healIdx = 0;
    private static int lastCount = -1;          // the last value of the "manual activation" counter we saw (-1 = not read yet)
    private static ContentObserver observer;    // held strongly: the content resolver only keeps a weak reference
    private static ContentObserver countObserver;
    private static Handler main;
    /** Review of v0.8.1: when low_power last CHANGED, and when the last accepted turn-ON request was counted. A request that Android really carried out changes low_power (even if the person turns it off again 100 ms later); a SWALLOWED one changes nothing, so only that one is healed. */
    private static volatile long lowPowerChangedAt = 0, bumpAt = 0;

    private BatterySaverGuard() {}

    public static boolean enabled(Context c) { return Features.get(c, BatterySaverPolicy.KEY, true); }
    /** AN1: strict = also undo a saver that is turned ON while the custom launcher is active (the old v0.7.3 - v0.7.6 behaviour); default OFF so the tile in the shade always answers. */
    public static boolean strict(Context c) { return Features.get(c, BatterySaverPolicy.STRICT_KEY, false); }
    public static boolean permitted(Context c) { return c.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == android.content.pm.PackageManager.PERMISSION_GRANTED; }

    /** Registers the observers once per process (idempotent), clears a saver an older version left stuck, and checks once. Called from {@link TjApplication#onCreate} and the accessibility service. */
    public static synchronized void start(Context c) {
        try {
            if (observer != null) return;
            final Context app = c.getApplicationContext();
            main = new Handler(Looper.getMainLooper());
            observer = new ContentObserver(main) {
                @Override public void onChange(boolean selfChange) {
                    lowPowerChangedAt = System.currentTimeMillis();
                    // AG2 (Tj, 2026-11-06: the battery saver TILE in Quickstep does not work): every change of low_power is written to the lock log with the mode and the guard's switches, so a report says WHY
                    try { LockLog.add(app, "BATSAVER", "low_power is now " + Settings.Global.getString(app.getContentResolver(), BatterySaverPolicy.SETTING) + ", mode=" + Mode.get(app) + ", off-in-custom=" + enabled(app) + ", strict=" + strict(app) + ", power=" + powerSource(app) + (CrashGuard.automationOff(app) ? " PAUSED" : "")); } catch (Throwable ignored) { }
                    check(app, "setting changed", true);   // AN1: does nothing unless "strict" is on: a change made on the tile is left alone
                }
            };
            app.getContentResolver().registerContentObserver(Settings.Global.getUriFor(BatterySaverPolicy.SETTING), false, observer);
            // AZ2: the self-heal. Android raises this counter for every request to turn the saver ON that it ACCEPTED (even one its state machine then ignores because a direct write put it out of step).
            lastCount = readCount(app);
            countObserver = new ContentObserver(main) { @Override public void onChange(boolean selfChange) { onActivationCount(app); } };
            app.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(BatterySaverPolicy.COUNT_SETTING), false, countObserver);
            unstickOnce(app);
            // AN1: the rule is re-applied at a process start only in the first minutes after a boot; a restart of our process later (a Go device kills background processes often) must never undo a tap on the tile
            if (BatterySaverPolicy.startCheckWanted(strict(app), android.os.SystemClock.elapsedRealtime())) check(app, "process start");
        } catch (Throwable t) { Log.e(TAG, "could not watch battery saver", t); }
    }

    /** Switches battery saver off now if the rules say so. {@code why} is for the log / status line only. A DELIBERATE check (a mode change, the process start): the switch-off that follows is never capped. */
    public static void check(Context c, String why) { check(c, why, false); }

    /** {@code reactive}: called because the setting CHANGED (something turned the saver on): only these switch-offs count toward the cap that protects against a tight loop. */
    static void check(Context c, String why, boolean reactive) {
        try {
            if (!BatterySaverPolicy.mayAct(reactive, strict(c))) return;   // AN1: a change behind our back (the tile, Android) is left alone unless "strict" is on
            ContentResolver cr = c.getContentResolver();
            boolean on = BatterySaverPolicy.isOn(Settings.Global.getString(cr, BatterySaverPolicy.SETTING));
            if (!on) return;   // nothing to do (one settings read); AZ2: the saver is never switched ON from here (Quickstep is stock)
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

    // ---------------------------------------------------------------- AZ2: the self-heal

    private static int readCount(Context c) {
        try { return Settings.Secure.getInt(c.getContentResolver(), BatterySaverPolicy.COUNT_SETTING, 0); } catch (Throwable t) { return -1; }
    }

    /** The "manual activation" counter changed: somebody asked (tile, Settings) to turn the saver ON and Android accepted the request. A moment later see whether it really is on. */
    private static void onActivationCount(final Context app) {
        try {
            int now = readCount(app), before = lastCount;
            lastCount = now;
            if (!BatterySaverPolicy.activationRequested(before, now) || main == null) return;
            bumpAt = System.currentTimeMillis();
            main.postDelayed(new Runnable() { @Override public void run() { healIfSwallowed(app); } }, BatterySaverPolicy.HEAL_DELAY_MS);
        } catch (Throwable t) { Log.e(TAG, "activation count failed", t); }
    }

    /**
     * Android accepted a request to turn the saver ON but {@code low_power} is still 0: its state machine is out of step (our own switch-off in the custom launcher left it in MANUAL_ON / AUTOMATIC_ON) and
     * ignored the request. Carry it out ourselves: one write of low_power = 1 (Android switches the saver on and the state machine agrees again, so the next "turn off" works). Deliberately NOT tied to the
     * guard's switches or to "automation paused": this only completes what the person asked for, and "Restore everything" must leave the tile working.
     */
    static void healIfSwallowed(Context c) {
        try {
            if (lowPowerChangedAt >= bumpAt - 20) { Log.i(TAG, "BATSAVER no heal: low_power changed after the request (it was carried out, then possibly undone)"); return; }
            ContentResolver cr = c.getContentResolver();
            boolean on = BatterySaverPolicy.isOn(Settings.Global.getString(cr, BatterySaverPolicy.SETTING));
            String src = powerSource(c);
            long now = System.currentTimeMillis();
            synchronized (BatterySaverGuard.class) {
                if (!BatterySaverPolicy.healWanted(on, BatterySaverPolicy.charging(src), now, heals)) return;
                if (!permitted(c)) { Features.setString(c, BatterySaverPolicy.LAST_KEY, stamp(now) + "  your tap to turn battery saver ON was ignored by Android but the adb permission (WRITE_SECURE_SETTINGS) is missing, so it cannot be completed"); return; }
                heals[healIdx++ % heals.length] = now;
            }
            Settings.Global.putInt(cr, BatterySaverPolicy.SETTING, 1);
            Features.setInt(c, BatterySaverPolicy.HEALS_KEY, Features.getInt(c, BatterySaverPolicy.HEALS_KEY, 0) + 1);
            Features.setString(c, BatterySaverPolicy.LAST_KEY, stamp(now) + "  your tap to turn battery saver ON was ignored by Android (its internal state was out of step after the launcher switched it off); switched it on for you");
            Log.i(TAG, "BATSAVER healed: a swallowed turn-ON was carried out");
            LockLog.add(c, "BATSAVER", "healed: the turn-ON request was swallowed by Android's state machine; low_power set to 1 (power=" + src + ")");
        } catch (Throwable t) { Log.e(TAG, "battery saver heal failed", t); }
    }

    /**
     * AZ2: v0.7.5 - v0.8.0 forced the saver ON when Quickstep began (a direct write). Android's state machine then thinks it is OFF, so the tile and Settings cannot turn it off ("it says it turned off but it
     * never does"). Once, after the update: if the guard's own last note says it forced the saver ON and it is still on, switch it off (OFF/OFF is a consistent pair again; turning it on by hand then works).
     */
    private static void unstickOnce(Context c) {
        try {
            if (Features.get(c, BatterySaverPolicy.UNSTICK_DONE_KEY, false)) return;
            ContentResolver cr = c.getContentResolver();
            boolean on = BatterySaverPolicy.isOn(Settings.Global.getString(cr, BatterySaverPolicy.SETTING));
            String last = Features.getString(c, BatterySaverPolicy.LAST_KEY, "");
            if (BatterySaverPolicy.unstickWanted(false, on, last)) {
                if (!permitted(c)) return;   // try again when the permission is there
                Settings.Global.putInt(cr, BatterySaverPolicy.SETTING, 0);
                if (BatterySaverPolicy.isOn(Settings.Global.getString(cr, BatterySaverPolicy.STICKY_SETTING))) Settings.Global.putInt(cr, BatterySaverPolicy.STICKY_SETTING, 0);
                long now = System.currentTimeMillis();
                Features.setString(c, BatterySaverPolicy.LAST_KEY, stamp(now) + "  switched off a battery saver that an older version had forced on (Android could no longer turn it off by hand)");
                Log.i(TAG, "BATSAVER unstuck: cleared a saver forced on by an older version");
                LockLog.add(c, "BATSAVER", "unstuck: cleared a saver forced on by an older version");
            }
            Features.set(c, BatterySaverPolicy.UNSTICK_DONE_KEY, true);
        } catch (Throwable t) { Log.e(TAG, "battery saver unstick failed", t); }
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
