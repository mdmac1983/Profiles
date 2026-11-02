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

    public static boolean clearNotifications(Context c) { return Features.get(c, CLEAR_NOTIF_KEY, true); }
    public static boolean clearRecents(Context c) { return Features.get(c, CLEAR_RECENTS_KEY, true); }

    private ReturnReset() {}

    public static boolean enabled(Context c) { return Features.get(c, KEY, true); }
    public static String appLabel(Context c) { return Features.getString(c, APP_KEY, DEFAULT_APP); }

    public static void run(final Context c) {
        if (!enabled(c)) return;
        final Context app = c.getApplicationContext();
        try { killApps(app); } catch (Throwable t) { Log.e(TAG, "kill failed", t); }
        try { radiosOff(app); } catch (Throwable t) { Log.e(TAG, "radios failed", t); }
        final boolean notif = clearNotifications(app), recents = clearRecents(app);
        TjAccessibilityService svc = TjAccessibilityService.get();
        if (svc != null && (notif || recents)) {
            // Android Go has no Notification access, and killBackgroundProcesses is not enough: press "Clear all" in the real shade / Recents (each switchable), THEN open the app.
            try { svc.runReset(notif, recents, new Runnable() { @Override public void run() { try { openApp(app); } catch (Throwable t) { Log.e(TAG, "open app failed", t); } } }); return; }
            catch (Throwable t) { Log.e(TAG, "accessibility reset failed, using the simple reset", t); }
        } else if (svc == null && (notif || recents)) {
            Features.setString(app, TjAccessibilityService.RESET_REPORT_KEY, "Accessibility helper is OFF: notifications and Recents were NOT cleared (turn it on in Accessibility settings).");
        } else {
            Features.setString(app, TjAccessibilityService.RESET_REPORT_KEY, "Both Clear all switches are OFF.");
        }
        if (notif) { try { clearNotifications(); } catch (Throwable t) { Log.e(TAG, "notifications failed", t); } }
        try { openApp(app); } catch (Throwable t) { Log.e(TAG, "open app failed", t); }
    }

    private static void killApps(Context c) {
        PackageManager pm = c.getPackageManager();
        ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName stock = StockLauncher.find(c);
        String ime = null;
        String cur = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        if (cur != null && cur.contains("/")) ime = cur.substring(0, cur.indexOf('/'));
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        int n = 0;
        for (ResolveInfo ri : pm.queryIntentActivities(launcher, 0)) {
            String pkg = ri.activityInfo.packageName;
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

    private static void radiosOff(Context c) {
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

    private static void openApp(Context c) {
        List<String> labels = new ArrayList<>();
        List<ResolveInfo> ris = launchables(c, labels);
        int i = ResetPolicy.pickLabel(labels, appLabel(c));
        if (i < 0) { Log.i(TAG, "no app matching '" + appLabel(c) + "' found"); return; }
        ResolveInfo ri = ris.get(i);
        c.startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
        Log.i(TAG, "opened " + labels.get(i));
    }
}
