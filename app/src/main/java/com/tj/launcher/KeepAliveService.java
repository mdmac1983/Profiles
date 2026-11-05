package com.tj.launcher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.android.launcher3.R;

/**
 * AA2 (Tj, 2026-11-05: "Build number 3"; goal "as fast as possible launcher startup (if it can be kept in ram ...)"). A foreground service that does NOTHING except exist: it lifts this app's process
 * from "cached / background service" (the first thing Android's low-memory killer takes on a 1-2 GB Go tablet) to "foreground service", so the launcher process, the Launcher3 model and the
 * accessibility helper are still alive when the tablet wakes. A killed process is why an unlock can be slow: the process must start, the helper must reconnect, and the lock-screen pre-arm
 * (Solitaire + custom home put in front BEHIND the lock screen) has not happened yet.
 *
 * Cost, said honestly: the process stays in RAM (about what it uses now) and ONE silent notification ("Launcher is kept ready") sits in the shade. MEASURED (emulator run 79): Android raises a foreground-service
 * notification to at least LOW importance whatever the channel asks for, so it gets a status-bar slot; its icon is fully transparent (tj_ic_blank) and the accessibility helper's "are there notifications to clear"
 * look ignores it by its title, so the reset still skips the shade when nothing else is there. Effect on priority, MEASURED: with the accessibility helper bound the process already sat at oom_score_adj 100
 * (visible) and survived `am kill-all`; with the service it is 50. The gain on the real tablet (MediaTek's own memory cleaner) can only be seen there ("Last unlock" in Launcher control). Switch in Launcher control; "Restore everything" turns it off and stops it. It does no work, holds no wake lock and never touches the lock screen.
 * KA1 (v0.7.2): OFF by default; a PNG icon; a crash fuse (see {@link KeepAlivePolicy}) because on the real tablet the notification crash-looped the launcher.
 */
public final class KeepAliveService extends Service {
    private static final String TAG = "TjKeepAlive";
    private static final String CHANNEL = "tj_keep_alive";
    private static final int NOTIFICATION_ID = 7471;
    private static volatile boolean running = false;

    /** The switch. KA1 (v0.7.2): OFF unless Tj has switched it on. */
    public static boolean enabled(Context c) { return Features.get(c, KeepAlivePolicy.KEY, KeepAlivePolicy.DEFAULT_ON); }
    /** KA1: true after the crash fuse blew (Android refused our notification once): stays true until Tj switches keep-alive on again himself. */
    public static boolean blocked(Context c) { return Features.get(c, KeepAlivePolicy.BLOCKED_KEY, false); }
    /** KA1: why the fuse blew (time + Android's wording), or "". */
    public static String blockedWhy(Context c) { return Features.getString(c, KeepAlivePolicy.BLOCKED_WHY_KEY, ""); }
    /** The Launcher-control switch = Tj's own choice. Switching ON is his explicit retry, so it re-arms the fuse. */
    public static void setUserChoice(Context c, boolean on) {
        Features.set(c, KeepAlivePolicy.BLOCKED_KEY, false); Features.setString(c, KeepAlivePolicy.BLOCKED_WHY_KEY, "");   // on = his retry; off = nothing left to explain
        Features.set(c, KeepAlivePolicy.KEY, on);
    }
    /**
     * KA1, called from the crash handler ({@link CrashGuard}) on the main thread of the dying process: the system just crashed us because it could not draw the keep-alive notification. Switch ONLY keep-alive off
     * and remember why; synchronous (commit) because the process is about to die. Must never throw.
     */
    static void blowFuse(Context c, Throwable e) {
        try {
            Features.set(c, KeepAlivePolicy.KEY, false);
            Features.set(c, KeepAlivePolicy.BLOCKED_KEY, true);
            String m = e == null ? "" : String.valueOf(e.getMessage()).replaceAll("\\s+", " ");   // one flat line
            Features.setString(c, KeepAlivePolicy.BLOCKED_WHY_KEY, new java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.US).format(new java.util.Date()) + "  " + (m.length() > 120 ? m.substring(0, 120) : m));
        } catch (Throwable ignored) { }
    }
    private static boolean mayRun(Context c) { return KeepAlivePolicy.shouldRun(enabled(c), CrashGuard.wasTripped(c), blocked(c)); }
    /** True while this process runs the service (a new process starts as false). */
    public static boolean running() { return running; }

    /** Starts the service if the rules say so (idempotent, cheap, never throws); stops it when they say not. Safe from the main thread, the router and the accessibility helper. */
    public static void ensure(Context c) {
        try {
            Context app = c.getApplicationContext();
            if (!mayRun(app)) { if (running) stop(app); return; }
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
        // KA1: tj_ic_blank is now a fully transparent PNG (it was an EMPTY vector drawable, which the tablet's MediaTek SystemUI could not turn into an icon: "Couldn't create icon", then Android crashed us).
        // The test key makes the icon one that does not exist (emulator test of the fuse only; it is off unless a test writes it into our private preferences).
        boolean debuggable = (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;   // the knob exists only in the emulator's debug build: a release APK can never be told to crash itself
        int bad = debuggable ? Features.getInt(this, KeepAlivePolicy.TEST_BAD_ICON_KEY, 0) : 0;   // 0 = normal, 1 = an icon resource that does not exist (SystemUI cannot draw it: the tablet's crash), 2 = no icon at all (Android refuses it at once)
        int icon = bad == 1 ? 0x7f0e7777 : bad == 2 ? 0 : R.drawable.tj_ic_blank;
        Notification n = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(icon)     // invisible: Android floors this notification at LOW importance, which would otherwise show an icon in the status bar
                .setContentTitle(KeepAlivePolicy.TITLE)
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
        // KA1: Android re-creates a killed sticky service BEFORE onStartCommand runs, so the rule has to be checked here too: reposting the notification the system could not draw is what made the three crashes in seven
        // seconds on the real tablet. Not allowed now (switch off, fuse blown, breaker tripped) = end at once without posting anything.
        if (!mayRun(this)) { running = false; Log.i(TAG, "keep-alive not allowed now (switch off, fuse blown or breaker tripped): stopping without a notification"); stopSelf(); return; }
        try { goForeground(); running = true; Log.i(TAG, "keep-alive service running (the launcher process is now a foreground service)"); }
        catch (Throwable t) { Log.e(TAG, "keep-alive could not go foreground, stopping", t); running = false; stopSelf(); }
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        // START_STICKY: Android restarts us (and with us the process) if it is ever killed. A restart request after the rules said "off" ends at once.
        if (!mayRun(this)) { stopSelf(); return START_NOT_STICKY; }
        try { goForeground(); running = true; }
        catch (Throwable t) {
            // a start made with startForegroundService() that never reaches startForeground() crashes the whole app after ~5 s: if going foreground fails, end the service instead (review of 2026-11-05)
            Log.e(TAG, "keep-alive refresh failed, stopping", t); running = false; stopSelf(); return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @Override public void onDestroy() {
        running = false;
        try { stopForeground(true); } catch (Throwable ignored) { }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
