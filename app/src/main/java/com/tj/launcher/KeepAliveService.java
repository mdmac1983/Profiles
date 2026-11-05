package com.tj.launcher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * AA2 (Tj, 2026-11-05: "Build number 3"; goal "as fast as possible launcher startup (if it can be kept in ram ...)"). A foreground service that does NOTHING except exist: it lifts this app's process
 * from "cached / background service" (the first thing Android's low-memory killer takes on a 1-2 GB Go tablet) to "foreground service", so the launcher process, the Launcher3 model and the
 * accessibility helper are still alive when the tablet wakes. A killed process is why an unlock can be slow: the process must start, the helper must reconnect, and the lock-screen pre-arm
 * (Solitaire + custom home put in front BEHIND the lock screen) has not happened yet.
 *
 * Cost, said honestly: the process stays in RAM (about what it uses now) and ONE silent notification ("Launcher is kept ready") sits in the shade. It uses the lowest importance, so it has no status-bar icon and
 * is secret on the lock screen. Switch in Launcher control; "Restore everything" turns it off and stops it. It does no work, holds no wake lock and never touches the lock screen.
 */
public final class KeepAliveService extends Service {
    private static final String TAG = "TjKeepAlive";
    private static final String CHANNEL = "tj_keep_alive";
    private static final int NOTIFICATION_ID = 7471;
    private static volatile boolean running = false;

    public static boolean enabled(Context c) { return Features.get(c, KeepAlivePolicy.KEY, true); }
    /** True while this process runs the service (a new process starts as false). */
    public static boolean running() { return running; }

    /** Starts the service if the rules say so (idempotent, cheap, never throws); stops it when they say not. Safe from the main thread, the router and the accessibility helper. */
    public static void ensure(Context c) {
        try {
            Context app = c.getApplicationContext();
            if (!KeepAlivePolicy.shouldRun(enabled(app), CrashGuard.wasTripped(app))) { if (running) stop(app); return; }
            if (running) return;
            app.startForegroundService(new Intent(app, KeepAliveService.class));
        } catch (Throwable t) { Log.e(TAG, "could not start the keep-alive service", t); }
    }

    public static void stop(Context c) {
        try {
            Context app = c.getApplicationContext();
            app.stopService(new Intent(app, KeepAliveService.class));
        } catch (Throwable t) { Log.e(TAG, "could not stop the keep-alive service", t); }
    }

    private void goForeground() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Launcher kept ready", KeepAlivePolicy.IMPORTANCE_MIN);
            ch.setDescription("Keeps the launcher in memory so it opens faster after an unlock. Switch it off in Launcher control.");
            ch.setShowBadge(false);
            ch.setLockscreenVisibility(KeepAlivePolicy.VISIBILITY_SECRET);
            nm.createNotificationChannel(ch);
        }
        Notification n = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(getApplicationInfo().icon)
                .setContentTitle("Launcher is kept ready")
                .setContentText("Faster unlock. Turn off in Launcher control.")
                .setOngoing(true)
                .setShowWhen(false)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setVisibility(Notification.VISIBILITY_SECRET)
                .build();
        startForeground(NOTIFICATION_ID, n);
    }

    @Override public void onCreate() {
        super.onCreate();
        try { goForeground(); running = true; Log.i(TAG, "keep-alive service running (the launcher process is now a foreground service)"); }
        catch (Throwable t) { Log.e(TAG, "keep-alive could not go foreground, stopping", t); running = false; stopSelf(); }
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        // START_STICKY: Android restarts us (and with us the process) if it is ever killed. A restart request after the rules said "off" ends at once.
        if (!KeepAlivePolicy.honourRestart(enabled(this), CrashGuard.wasTripped(this))) { stopSelf(); return START_NOT_STICKY; }
        try { goForeground(); running = true; } catch (Throwable t) { Log.e(TAG, "keep-alive refresh failed", t); }
        return START_STICKY;
    }

    @Override public void onDestroy() {
        running = false;
        try { stopForeground(true); } catch (Throwable ignored) { }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
