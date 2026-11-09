package com.tj.launcher;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * BS1a: the Android half of the in-app updater (rules: {@link UpdateRules}). Started only from Profiles settings (Owner = Quickstep): nothing runs at boot or in the background.
 *
 * Every download goes through the system's DownloadManager, never a socket of our own: on this tablet the launcher's UID is refused TCP sockets (TASKS HLP2, EPERM), while the
 * download service runs in its own process. Flow: (1) download the "latest release" JSON, (2) newer version -> the user taps Install, (3) download the APK, (4) check that it is
 * signed with OUR key, (5) install it with a PackageInstaller session (Android shows its own confirmation). {@link UpdateReceiver} receives the download and install results.
 */
public final class Updater {
    private static final String TAG = "TjUpdate";
    static final String KEY_STATUS = "update_status", KEY_TAG = "update_tag", KEY_URL = "update_url", KEY_CHECKED = "update_checked_ms",
            KEY_JSON_ID = "update_json_id", KEY_APK_ID = "update_apk_id";
    static final String ACTION_INSTALL_STATUS = "com.tj.launcher.UPDATE_INSTALL_STATUS";
    private static final String JSON_FILE = "latest.json", APK_FILE = "update.apk";

    private Updater() {}

    public static String installedVersion(Context c) {
        try { return c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName; } catch (Throwable t) { return "?"; }
    }

    static void status(Context c, String s) {
        Log.i(TAG, s);
        Features.setString(c, KEY_STATUS, new java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).format(new java.util.Date()) + "  " + s);
    }

    public static String statusLine(Context c) { return Features.getString(c, KEY_STATUS, "not checked yet"); }

    /** The newer version that can be installed now, or null. */
    public static String available(Context c) {
        String tag = Features.getString(c, KEY_TAG, "");
        return UpdateRules.newer(tag, installedVersion(c)) && !Features.getString(c, KEY_URL, "").isEmpty() ? tag : null;
    }

    private static File dir(Context c) {
        File d = c.getExternalFilesDir("update");
        if (d != null && !d.isDirectory()) d.mkdirs();
        return d;
    }

    /** Opening the settings screen: check by itself if the last check is old enough. */
    public static void autoCheck(Context c) {
        long last = 0;
        try { last = Long.parseLong(Features.getString(c, KEY_CHECKED, "0")); } catch (NumberFormatException ignored) {}
        if (UpdateRules.autoCheckDue(last, System.currentTimeMillis())) check(c);
    }

    /** Step 1: download the newest release's description. */
    public static void check(Context c) {
        try {
            File d = dir(c);
            if (d == null) { status(c, "cannot check: no storage for the download"); return; }
            new File(d, JSON_FILE).delete();
            DownloadManager dm = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(UpdateRules.LATEST_API))
                    .addRequestHeader("Accept", "application/vnd.github+json")
                    .addRequestHeader("User-Agent", "Profiles-updater")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                    .setDestinationInExternalFilesDir(c, "update", JSON_FILE);
            long id = dm.enqueue(r);
            Features.setString(c, KEY_JSON_ID, String.valueOf(id));
            Features.setString(c, KEY_CHECKED, String.valueOf(System.currentTimeMillis()));
            status(c, "checking for updates ...");
        } catch (Throwable t) { status(c, "check failed: " + t.getClass().getSimpleName()); Log.e(TAG, "check failed", t); }
    }

    /** Step 3: download the APK of the newer release. */
    public static void download(Context c) {
        try {
            String url = Features.getString(c, KEY_URL, "");
            if (available(c) == null || url.isEmpty()) { status(c, "no update to install"); return; }
            if (!c.getPackageManager().canRequestPackageInstalls()) {
                status(c, "allow \"Install unknown apps\" for Profiles, then tap Install again");
                c.startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + c.getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return;
            }
            File d = dir(c);
            if (d == null) { status(c, "cannot download: no storage"); return; }
            new File(d, APK_FILE).delete();
            DownloadManager dm = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url))
                    .addRequestHeader("User-Agent", "Profiles-updater")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                    .setDestinationInExternalFilesDir(c, "update", APK_FILE);
            Features.setString(c, KEY_APK_ID, String.valueOf(dm.enqueue(r)));
            status(c, "downloading " + Features.getString(c, KEY_TAG, "") + " ...");
        } catch (Throwable t) { status(c, "download failed: " + t.getClass().getSimpleName()); Log.e(TAG, "download failed", t); }
    }

    /** A download finished (DownloadManager broadcast). Worker thread. */
    static void onDownloadComplete(Context c, long id) {
        boolean json = String.valueOf(id).equals(Features.getString(c, KEY_JSON_ID, "-"));
        boolean apk = String.valueOf(id).equals(Features.getString(c, KEY_APK_ID, "-"));
        if (!json && !apk) return;
        DownloadManager dm = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
        int st = -1, reason = 0;
        try (Cursor q = dm.query(new DownloadManager.Query().setFilterById(id))) {
            if (q != null && q.moveToFirst()) {
                st = q.getInt(q.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                reason = q.getInt(q.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON));
            }
        } catch (Throwable t) { Log.e(TAG, "query failed", t); }
        if (st != DownloadManager.STATUS_SUCCESSFUL) {
            status(c, (json ? "check" : "download") + " failed (" + (reason == 404 ? "no release published yet" : "reason " + reason) + ")");
            return;
        }
        File d = dir(c);
        if (json) {
            String text = read(new File(d, JSON_FILE));
            UpdateRules.Release r = UpdateRules.parse(text);
            if (r == null) { status(c, "no release with a Profiles APK found"); return; }
            Features.setString(c, KEY_TAG, r.tag);
            Features.setString(c, KEY_URL, r.apkUrl);
            status(c, UpdateRules.newer(r.tag, installedVersion(c)) ? r.tag + " is available: tap Install" : "up to date (" + installedVersion(c) + ", newest release " + r.tag + ")");
        } else {
            File f = new File(d, APK_FILE);
            String why = whyNotOurs(c, f);
            if (!why.isEmpty()) { status(c, "not installed: " + why); f.delete(); return; }
            install(c, f);
        }
    }

    /** "" when the file is a newer Profiles signed with the same key as the installed app, else why not. */
    static String whyNotOurs(Context c, File f) {
        try {
            PackageManager pm = c.getPackageManager();
            @SuppressWarnings("deprecation") PackageInfo in = pm.getPackageArchiveInfo(f.getPath(), PackageManager.GET_SIGNATURES);
            if (in == null) return "the download is not an app";
            if (!c.getPackageName().equals(in.packageName)) return "it is another app (" + in.packageName + ")";
            @SuppressWarnings("deprecation") PackageInfo me = pm.getPackageInfo(c.getPackageName(), PackageManager.GET_SIGNATURES);
            if (!sameSigners(me.signatures, in.signatures)) return "it is signed with another key";
            @SuppressWarnings("deprecation") long newCode = in.versionCode, oldCode = me.versionCode;
            if (newCode <= oldCode) return "it is not newer (code " + newCode + ")";
            return "";
        } catch (Throwable t) { return "it could not be read (" + t.getClass().getSimpleName() + ")"; }
    }

    private static boolean sameSigners(Signature[] a, Signature[] b) {
        if (a == null || b == null || a.length == 0 || a.length != b.length) return false;
        String[] x = new String[a.length], y = new String[b.length];
        for (int i = 0; i < a.length; i++) { x[i] = a[i].toCharsString(); y[i] = b[i].toCharsString(); }
        Arrays.sort(x); Arrays.sort(y);
        return Arrays.equals(x, y);
    }

    /** Step 5: hand the APK to Android's installer. Android asks the user to confirm (see {@link UpdateReceiver}). */
    private static void install(Context c, File f) {
        PackageInstaller.Session s = null;
        try {
            PackageInstaller pi = c.getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams p = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            p.setAppPackageName(c.getPackageName());
            int sid = pi.createSession(p);
            s = pi.openSession(sid);
            try (OutputStream out = s.openWrite("profiles.apk", 0, f.length()); InputStream in = new FileInputStream(f)) {
                byte[] b = new byte[65536]; int n;
                while ((n = in.read(b)) > 0) out.write(b, 0, n);
                s.fsync(out);
            }
            Intent i = new Intent(c, UpdateReceiver.class).setAction(ACTION_INSTALL_STATUS);
            PendingIntent done = PendingIntent.getBroadcast(c, sid, i, PendingIntent.FLAG_UPDATE_CURRENT);
            status(c, "installing " + Features.getString(c, KEY_TAG, "") + " ...");
            s.commit(done.getIntentSender());
        } catch (Throwable t) {
            status(c, "install failed: " + t.getClass().getSimpleName());
            Log.e(TAG, "install failed", t);
            if (s != null) try { s.abandon(); } catch (Throwable ignored) {}
        } finally {
            if (s != null) try { s.close(); } catch (Throwable ignored) {}
        }
    }

    private static String read(File f) {
        if (f == null || !f.isFile() || f.length() > 512 * 1024) return null;
        try (InputStream in = new FileInputStream(f)) {
            byte[] b = new byte[(int) f.length()];
            int off = 0, n;
            while (off < b.length && (n = in.read(b, off, b.length - off)) > 0) off += n;
            return new String(b, 0, off, "UTF-8");
        } catch (Throwable t) { return null; }
    }
}
