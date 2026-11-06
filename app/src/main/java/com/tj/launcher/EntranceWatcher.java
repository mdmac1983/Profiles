package com.tj.launcher;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

/**
 * AD4: the Android side of {@link SecretEntrances}. Started once per MAIN process from {@link TjApplication}; the page flick is fed by {@link TjHooks#onTouch}. Everything here runs on the main thread, is cheap
 * (a few comparisons per event, nothing while an entrance is switched off) and never throws. An entrance only counts while it is switched on, the custom launcher is active, the tablet is unlocked and
 * the automation is not paused by the crash breaker; then it takes Tj to Quickstep through {@link ProfileSession#goToQuickstep}, which needs no shell helper.
 */
public final class EntranceWatcher {
    private static final String TAG = "TjEntrance";
    private static final SecretEntrances.Flick flick = new SecretEntrances.Flick();
    private static final SecretEntrances.Toggles toggles = new SecretEntrances.Toggles();
    private static final SecretEntrances.Plug plug = new SecretEntrances.Plug();
    private static ContentObserver rotationObserver;     // held strongly: the content resolver only keeps a weak reference
    private static BroadcastReceiver powerReceiver;
    private static int lastRotation = -1;

    private EntranceWatcher() {}

    public static boolean on(Context c, SecretEntrances.Method m) { return Features.get(c, m.prefKey(), m.defaultOn); }

    /** True once the rotation observer and the charger receiver are registered in this process (shown in the tablet report). */
    public static boolean watching() { return rotationObserver != null && powerReceiver != null; }

    /** May this entrance count right now? (switch, custom launcher, unlocked, not paused) */
    private static boolean armed(Context c, SecretEntrances.Method m) {
        try {
            if (!on(c, m) || !Mode.isCustom(c) || CrashGuard.automationOff(c)) return false;
            KeyguardManager km = (KeyguardManager) c.getSystemService(Context.KEYGUARD_SERVICE);
            return km == null || !km.isKeyguardLocked();
        } catch (Throwable t) { return false; }
    }

    /** Registers the rotation observer and the charger receiver once per process (idempotent). */
    public static synchronized void start(Context c) {
        try {
            if (rotationObserver != null) return;
            final Context app = c.getApplicationContext();
            lastRotation = readRotation(app);
            rotationObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override public void onChange(boolean selfChange) { onRotation(app); }
            };
            app.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), false, rotationObserver);
            powerReceiver = new BroadcastReceiver() {
                @Override public void onReceive(Context ctx, Intent i) { if (i != null) onPower(app, Intent.ACTION_POWER_CONNECTED.equals(i.getAction())); }
            };
            IntentFilter f = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
            f.addAction(Intent.ACTION_POWER_DISCONNECTED);
            app.registerReceiver(powerReceiver, f);
        } catch (Throwable t) { Log.e(TAG, "could not watch the entrances", t); }
    }

    private static int readRotation(Context c) { return Settings.System.getInt(c.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, -1); }

    private static void onRotation(Context c) {
        try {
            int v = readRotation(c);
            if (v == lastRotation) return;            // not a change of the lock itself
            lastRotation = v;
            if (!armed(c, SecretEntrances.Method.ROTATE)) { toggles.reset(); return; }
            if (toggles.onChange(System.currentTimeMillis())) { restoreRotation(c, v); fire(c, SecretEntrances.Method.ROTATE); }
        } catch (Throwable t) { Log.e(TAG, "rotation entrance failed", t); }
    }

    /** Five changes leave the rotation lock the other way round from where it began (an odd count): flip it once more so using the entrance never changes Tj's rotation setting. Needs the WRITE_SETTINGS grant; without it nothing happens. */
    private static void restoreRotation(Context c, int current) {
        try {
            if (current != 0 && current != 1) return;
            Settings.System.putInt(c.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1 - current);
            lastRotation = 1 - current;      // our own write must not count as a change (the observer runs after this method, on the same thread)
        } catch (Throwable t) { Log.w(TAG, "could not put the rotation lock back: " + t); }
    }

    private static void onPower(Context c, boolean connected) {
        try {
            if (!armed(c, SecretEntrances.Method.CHARGER)) { plug.reset(); return; }
            if (plug.onPower(connected, System.currentTimeMillis())) fire(c, SecretEntrances.Method.CHARGER);
        } catch (Throwable t) { Log.e(TAG, "charger entrance failed", t); }
    }

    /** Called by {@link TjHooks#onTouch} with a finished single-finger swipe that began on the settled home screen. */
    static void onSwipe(Context c, int direction) {
        try {
            if (!armed(c, SecretEntrances.Method.FLICK)) { flick.reset(); return; }
            if (flick.onSwipe(direction, System.currentTimeMillis())) fire(c, SecretEntrances.Method.FLICK);
        } catch (Throwable t) { Log.e(TAG, "flick entrance failed", t); }
    }

    private static void fire(Context c, SecretEntrances.Method m) {
        flick.reset(); toggles.reset(); plug.reset();
        LockLog.add(c, "ENTRANCE", m.id + " -> Quickstep");
        Log.i(TAG, "ENTRANCE " + m.id + " -> Quickstep");
        try {
            if (ProfileSession.buzz(c)) {
                android.os.Vibrator v = (android.os.Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) v.vibrate(android.os.VibrationEffect.createOneShot(40, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            }
        } catch (Throwable ignored) { }
        ProfileSession.goToQuickstep(c, "secret entrance: " + m.title);
    }
}
