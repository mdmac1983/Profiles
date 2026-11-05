package com.tj.launcher;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import com.android.launcher3.notification.NotificationListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Tj (2026-11-02): double Home back to the custom launcher "automatically and immediately closes all apps and clears all notifications and turns off all
 * connections ... and automatically and immediately opens the solitaire app". Each step is isolated (a failure in one never blocks the next), uses only normal
 * permissions (no secure settings, no lock-screen code), and the whole thing has a master switch in Launcher control.
 */
public final class ReturnReset {
    private static final String TAG = "TjReset";
    public static final String KEY = "return_reset";            // master switch, default ON
    public static final String APP_KEY = "return_app_label";    // label substring of the app to open, default "solitaire"
    public static final String DEFAULT_APP = "solitaire";
    public static final String CLEAR_NOTIF_KEY = "reset_clear_notifications";   // default ON
    public static final String CLEAR_RECENTS_KEY = "reset_clear_recents";       // default ON
    public static final String COVER_KEY = "reset_black_cover";                  // default ON: black cover while the reset drives the shade / Recents

    public static boolean clearNotifications(Context c) { return Features.get(c, CLEAR_NOTIF_KEY, true); }
    public static boolean clearRecents(Context c) { return Features.get(c, CLEAR_RECENTS_KEY, true); }

    private ReturnReset() {}

    public static boolean enabled(Context c) { return Features.get(c, KEY, true); }
    public static String appLabel(Context c) { return Features.getString(c, APP_KEY, DEFAULT_APP); }

    /**
     * N6: is the INVISIBLE path possible right now? Needs the shell helper (adb, once per boot) and the switch. Checked on the main thread: a refused local connection fails at once.
     * Cached for 4 s so the Enter-tap check and the unlock itself do not both pay for it.
     */
    private static volatile long invisAt = 0;
    private static volatile boolean invisVal = false;
    public static boolean invisibleReady(Context c) {
        if (!ShellHelper.enabled(c)) return false;
        long now = android.os.SystemClock.uptimeMillis();
        if (now - invisAt < 4000) return invisVal;
        boolean v = ShellHelper.reachable();
        invisVal = v; invisAt = now;
        return v;
    }

    /** The packages to force-stop on return: what Quickstep opened (or, before anything is tracked, every launchable app except the custom ones). Never us, the stock launcher, the keyboard, the target app. */
    private static java.util.List<String> invisibleTargets(Context c) {
        ComponentName stock = StockLauncher.find(c);
        String ime = null;
        String cur = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        if (cur != null && cur.contains("/")) ime = cur.substring(0, cur.indexOf('/'));
        String target = targetPackage(c);
        java.util.Set<String> pkgs = new java.util.LinkedHashSet<>();
        if (AppOrigin.tracked(c)) pkgs.addAll(AppOrigin.pending(c));
        else {
            java.util.Set<String> keep = AppOrigin.custom(c);
            for (ResolveInfo ri : c.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0))
                if (!keep.contains(ri.activityInfo.packageName)) pkgs.add(ri.activityInfo.packageName);
        }
        java.util.List<String> out = new ArrayList<>();
        for (String p : pkgs) {
            if (p.equals(target)) continue;
            if (ResetPolicy.shouldKill(p, c.getPackageName(), stock == null ? null : stock.getPackageName(), ime)) out.add(p);
        }
        return out;
    }

    /** AA3: what may be closed at all (never this app, the stock launcher, the keyboard, the system). */
    private static TaskOrigin.Guard guard(final Context c) {
        ComponentName stock = StockLauncher.find(c);
        final String stockPkg = stock == null ? null : stock.getPackageName();
        String cur = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        final String ime = (cur != null && cur.contains("/")) ? cur.substring(0, cur.indexOf('/')) : null;
        final String self = c.getPackageName();
        return new TaskOrigin.Guard() { @Override public boolean mayClose(String pkg) { return ResetPolicy.shouldKill(pkg, self, stockPkg, ime); } };
    }

    /** Solitaire (or the target app) at once; the Quickstep apps are force-stopped BEHIND it: Recents cards, notifications and processes vanish with no shade, no Recents screen, no cover. */
    private static void runInvisible(final Context app) {
        final long t0 = android.os.SystemClock.uptimeMillis();
        final java.util.List<String> pkgs = invisibleTargets(app);
        final boolean first = !AppOrigin.tracked(app);
        final String target = targetPackage(app);
        final TaskOrigin.Guard guard = guard(app);
        new Thread(new Runnable() { @Override public void run() {
            try { radiosOff(app); } catch (Throwable t) { Log.e(TAG, "radios failed", t); }
            TaskOrigin.Outcome oc = TaskOrigin.finish(app, pkgs, target, guard);   // AA3: ground truth from the helper's task list when a start-of-session snapshot exists
            int n = ShellHelper.forceStop(oc.stop);
            int m = (n >= 0 && !oc.removeIds.isEmpty()) ? ShellHelper.removeTasks(oc.removeIds) : 0;
            if (n >= 0) { AppOrigin.clearQuickstep(app); AppOrigin.setTracked(app); }
            invisAt = 0;
            String line = "INVISIBLE: " + (n < 0 ? "the shell helper stopped answering, " + oc.stop.size() + " apps NOT stopped (next return uses the cover method)" : "force-stopped " + n + " of " + oc.stop.size() + " apps"
                    + (first ? " (first run: every non-custom app)" : "") + " in " + (android.os.SystemClock.uptimeMillis() - t0) + " ms; no cover, no shade, no Recents screen; " + oc.note + (m != 0 ? " [task removal answered " + m + "]" : ""));
            report(app, line);
        } }, "tj-reset-invisible").start();
        try { openApp(app); } catch (Throwable t) { Log.e(TAG, "open app failed", t); }
    }

    /**
     * O4: the clean-up WITHOUT opening anything: force-stop Quickstep's apps (their notifications go with them), remove their Recents cards, radios off. Used at LOCK, so the unlock has nothing left to do.
     * Only with the shell helper (nothing here touches the screen). Returns false (and does nothing) when the helper is not reachable.
     */
    public static boolean cleanAtLock(final Context c) {
        final Context app = c.getApplicationContext();
        if (!enabled(app) || !invisibleReady(app)) return false;
        final long t0 = android.os.SystemClock.uptimeMillis();
        final java.util.List<String> pkgs = invisibleTargets(app);
        final boolean first = !AppOrigin.tracked(app);
        final String target = targetPackage(app);
        final TaskOrigin.Guard guard = guard(app);
        new Thread(new Runnable() { @Override public void run() {
            try { radiosOff(app); } catch (Throwable t) { Log.e(TAG, "radios failed", t); }
            TaskOrigin.Outcome oc = TaskOrigin.finish(app, pkgs, target, guard);   // AA3
            int n = ShellHelper.forceStop(oc.stop);
            int m = (n >= 0 && !oc.removeIds.isEmpty()) ? ShellHelper.removeTasks(oc.removeIds) : 0;
            if (n >= 0) { AppOrigin.clearQuickstep(app); AppOrigin.setTracked(app); }
            invisAt = 0;
            report(app, "AT LOCK: " + (n < 0 ? "the shell helper stopped answering, " + oc.stop.size() + " apps NOT stopped" : "force-stopped " + n + " of " + oc.stop.size() + " apps" + (first ? " (first run: every non-custom app)" : "") + " in " + (android.os.SystemClock.uptimeMillis() - t0) + " ms; " + oc.note + (m != 0 ? " [task removal answered " + m + "]" : "") + " ")
                    + "; the unlock has nothing left to close");
        } }, "tj-lock-clean").start();
        return true;
    }

    public static void run(final Context c) {
        if (!enabled(c)) return;
        final Context app = c.getApplicationContext();
        if (invisibleReady(app)) { runInvisible(app); return; }
        // W1: the slow, blocking parts (a binder call per app, Wi-Fi/Bluetooth) run on their own thread, so the main thread starts pressing "Clear all" at once.
        new Thread(new Runnable() { @Override public void run() {
            try { killApps(app); } catch (Throwable t) { Log.e(TAG, "kill failed", t); }
            try { radiosOff(app); } catch (Throwable t) { Log.e(TAG, "radios failed", t); }
        } }, "tj-reset-bg").start();
        final boolean notif = clearNotifications(app), recents = clearRecents(app);
        TjAccessibilityService svc = TjAccessibilityService.get();
        if (svc != null && (notif || recents)) {
            // B4: look first. Nothing in the status bar = no shade step; Recents untouched since the last clear = no Recents step. Both skipped = Solitaire at once, nothing else on screen.
            Boolean icons = notif ? svc.hasNotificationIcons() : Boolean.FALSE;
            final boolean doShade = notif && (icons == null || icons.booleanValue());
            // D5: only apps opened from QUICKSTEP are closed; apps opened from the custom launcher stay. First run after install (untracked) clears everything once.
            final boolean tracked = AppOrigin.tracked(app);
            final java.util.Set<String> pendPkgs = AppOrigin.pending(app);
            final boolean doRecents = recents && (!tracked || !pendPkgs.isEmpty());
            final java.util.Set<String> labels = (tracked && !AppOrigin.custom(app).isEmpty()) ? labelsOf(app, pendPkgs) : null;   // selective dismissal only when something must be preserved
            if (!doShade && !doRecents) {
                fastActive = false;
                svc.appendReport("nothing to clear (status bar shows no notifications" + (recents ? ", no app was opened from Quickstep since the last clear" : "") + "): Solitaire at once");
                Log.i(TAG, "RESET nothing to clear: Solitaire at once");
                try { openApp(app); } catch (Throwable t) { Log.e(TAG, "open app failed", t); }
                return;
            }
            final boolean fast = AnimationSpeedup.begin(app);   // Y2b: no animations while the reset runs (restored below, and by three failsafes)
            fastActive = fast;
            if (Features.get(app, COVER_KEY, true)) svc.showResetCover();   // the shade / Recents are driven behind a black cover: you never see them flicker
            // Android Go has no Notification access, and killBackgroundProcesses is not enough: press "Clear all" in the real shade / Recents, THEN open the app.
            try { svc.runReset(doShade, doRecents, labels, new Runnable() { @Override public void run() { try { openApp(app); } catch (Throwable t) { Log.e(TAG, "open app failed", t); } AnimationSpeedup.restoreLater(app, fast ? 700 : 0); } }); return; }
            catch (Throwable t) { Log.e(TAG, "accessibility reset failed, using the simple reset", t); svc.hideResetCover(); }
        } else if (svc == null && (notif || recents)) {
            Features.setString(app, TjAccessibilityService.RESET_REPORT_KEY, "Accessibility helper is OFF: notifications and Recents were NOT cleared (turn it on in Accessibility settings).");
        } else {
            Features.setString(app, TjAccessibilityService.RESET_REPORT_KEY, "Both Clear all switches are OFF.");
        }
        if (notif) { try { clearNotifications(); } catch (Throwable t) { Log.e(TAG, "notifications failed", t); } }
        try { openApp(app); } catch (Throwable t) { Log.e(TAG, "open app failed", t); }
        AnimationSpeedup.restoreLater(app, 0);
    }

    /** True while this reset runs with the animations off: the waits can be shorter. */
    public static volatile boolean fastActive = false;

    /** The app labels of these packages (what a Recents card says). */
    private static java.util.Set<String> labelsOf(Context c, java.util.Set<String> pkgs) {
        java.util.Set<String> out = new java.util.HashSet<>();
        PackageManager pm = c.getPackageManager();
        for (String p : pkgs) {
            try { out.add(String.valueOf(pm.getApplicationLabel(pm.getApplicationInfo(p, 0)))); } catch (Throwable ignored) { }
        }
        return out;
    }

    private static void killApps(Context c) {
        PackageManager pm = c.getPackageManager();
        ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName stock = StockLauncher.find(c);
        String ime = null;
        String cur = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        if (cur != null && cur.contains("/")) ime = cur.substring(0, cur.indexOf('/'));
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        java.util.Set<String> keep = AppOrigin.custom(c);
        int n = 0;
        for (ResolveInfo ri : pm.queryIntentActivities(launcher, 0)) {
            String pkg = ri.activityInfo.packageName;
            if (keep.contains(pkg)) continue;   // D5: apps opened from the custom launcher stay open
            if (ResetPolicy.shouldKill(pkg, c.getPackageName(), stock == null ? null : stock.getPackageName(), ime)) {
                am.killBackgroundProcesses(pkg);
                n++;
            }
        }
        Log.i(TAG, "asked Android to close " + n + " apps");
    }

    private static void clearNotifications() {
        NotificationListener l = NotificationListener.getInstanceIfConnected();
        if (l == null) { Log.i(TAG, "notifications NOT cleared: notification access is not enabled for the launcher"); return; }
        l.cancelAllNotifications();
        Log.i(TAG, "cleared notifications");
    }

    static void radiosOff(Context c) {
        WifiManager wm = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wm != null && wm.isWifiEnabled()) { boolean ok = wm.setWifiEnabled(false); Log.i(TAG, "wifi off -> " + ok); }
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if (bt != null && bt.isEnabled()) { boolean ok = bt.disable(); Log.i(TAG, "bluetooth off -> " + ok); }
    }

    private static volatile String cachedLabel, cachedPkg;
    private static volatile long cachedAt = 0;

    /** Package of the app we open / keep in Recents (e.g. Solitaire), or null. Cached for 30 s: it is consulted on every app launch. */
    public static String targetPackage(Context c) {
        long now = System.currentTimeMillis();
        String label = appLabel(c);
        if (cachedLabel != null && cachedLabel.equals(label) && now - cachedAt < 30_000) return cachedPkg;
        List<String> labels = new ArrayList<>();
        List<ResolveInfo> ris = launchables(c, labels);
        int i = ResetPolicy.pickLabel(labels, label);
        cachedPkg = i < 0 ? null : ris.get(i).activityInfo.packageName;
        cachedLabel = label; cachedAt = now;
        return cachedPkg;
    }

    /** Label of the app we would open, or null if none is installed (used by Launcher control's status line). */
    public static String findAppLabel(Context c) {
        List<String> labels = new ArrayList<>();
        List<ResolveInfo> ris = launchables(c, labels);
        int i = ResetPolicy.pickLabel(labels, appLabel(c));
        return i < 0 ? null : labels.get(i);
    }

    private static List<ResolveInfo> launchables(Context c, List<String> labelsOut) {
        PackageManager pm = c.getPackageManager();
        List<ResolveInfo> ris = pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0);
        for (ResolveInfo ri : ris) labelsOut.add(String.valueOf(ri.loadLabel(pm)));
        return ris;
    }

    /** N3: start the target app once, with no verification loop and no Home press: used while the lock screen still covers everything (the accessibility check cannot see behind it). */
    public static void startTargetBehindLock(Context c) {
        List<String> labels = new ArrayList<>();
        List<ResolveInfo> ris = launchables(c.getApplicationContext(), labels);
        int i = ResetPolicy.pickLabel(labels, appLabel(c));
        if (i >= 0) launchOnce(c.getApplicationContext(), ris.get(i), 0);
    }

    /** N3: might the return reset have anything to do (used to decide whether the black cover goes up at the Enter tap)? */
    public static boolean mayHaveWork(Context c) {
        if (!enabled(c) || invisibleReady(c)) return false;   // the invisible path needs no cover
        return clearNotifications(c) || (clearRecents(c) && (!AppOrigin.tracked(c) || !AppOrigin.pending(c).isEmpty()));
    }

    /** Puts the target app in front because the custom launcher just STARTED (boot / restart). Same safe, verified launch as the return reset; nothing else is touched. */
    public static void openStartupApp(Context c) { openApp(c.getApplicationContext()); }

    private static final android.os.Handler MAIN = new android.os.Handler(android.os.Looper.getMainLooper());

    /** Starts the target app and makes sure it ends up in FRONT: re-starts it (harmless if already on top) up to 3 more times, checking with the accessibility helper when it is on. */
    private static void openApp(final Context c) {
        final List<String> labels = new ArrayList<>();
        List<ResolveInfo> ris = launchables(c, labels);
        int i = ResetPolicy.pickLabel(labels, appLabel(c));
        if (i < 0) {
            Log.i(TAG, "no app matching '" + appLabel(c) + "' found"); report(c, "app: no app matching '" + appLabel(c) + "' is installed");
            // Nothing to put in front, so leave Recents / the shade ourselves (found by the E1 emulator test: Recents stayed open). No Solitaire start can race with this Home press.
            TjAccessibilityService svc = TjAccessibilityService.get();
            if (svc != null) { svc.hideResetCover(); svc.goHome(); }
            return;
        }
        final ResolveInfo ri = ris.get(i);
        final String label = labels.get(i);
        launchOnce(c, ri, 0);
        scheduleVerify(c, ri, label, 1);
    }

    private static void launchOnce(Context c, ResolveInfo ri, int attempt) {
        Intent it = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | (attempt > 0 ? Intent.FLAG_ACTIVITY_REORDER_TO_FRONT : 0));
        c.startActivity(it);
        Log.i(TAG, "start app attempt " + (attempt + 1));
    }

    private static void scheduleVerify(final Context c, final ResolveInfo ri, final String label, final int attempt) {
        MAIN.postDelayed(new Runnable() { @Override public void run() {
            try {
                TjAccessibilityService svc = TjAccessibilityService.get();
                boolean front = svc != null && svc.isForeground(ri.activityInfo.packageName);
                if (front) { report(c, "app: " + label + " is on screen" + (attempt > 1 ? " (needed " + attempt + " starts)" : "")); if (svc != null) svc.hideResetCover(); return; }
                if (attempt >= 5) { if (svc != null) svc.hideResetCover(); report(c, "app: " + label + " was started " + attempt + " times but could not be confirmed on screen" + (svc == null ? " (accessibility helper off, cannot check)" : "")); return; }
                launchOnce(c, ri, attempt);                 // not in front yet (e.g. the home screen came up late): bring it forward again
                scheduleVerify(c, ri, label, attempt + 1);
            } catch (Throwable t) { Log.e(TAG, "verify failed", t); }
        } }, attempt == 1 ? (fastActive ? 300 : 450) : 500);
    }

    private static void report(Context c, String line) {
        TjAccessibilityService svc = TjAccessibilityService.get();
        if (svc != null) svc.appendReport(line);
        else Features.setString(c, TjAccessibilityService.RESET_REPORT_KEY, line);
        Log.i(TAG, line);
    }
}
