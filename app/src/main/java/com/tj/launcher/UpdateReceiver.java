package com.tj.launcher;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.util.Log;

/**
 * BS1a: results for the in-app updater ({@link Updater}). (1) DownloadManager says a download finished: only OUR two download ids are acted on, everything else is ignored.
 * (2) The PackageInstaller session reports back: "needs the user" opens Android's own confirmation screen; a failure is written into the status line.
 */
public class UpdateReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        final Context c = context.getApplicationContext();
        String a = intent == null ? null : intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(a)) {
            final long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            final PendingResult pr = goAsync();
            new Thread(new Runnable() { @Override public void run() {
                try { Updater.onDownloadComplete(c, id); } catch (Throwable t) { Log.e("TjUpdate", "download result failed", t); } finally { pr.finish(); }
            } }, "tj-update").start();
        } else if (Updater.ACTION_INSTALL_STATUS.equals(a)) {
            int st = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
            if (st == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                Intent confirm = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                if (confirm != null) {
                    try { c.startActivity(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); Updater.status(c, "confirm the install on screen"); }
                    catch (Throwable t) { Updater.status(c, "could not show the install screen: " + t.getClass().getSimpleName()); }
                }
            } else if (st == PackageInstaller.STATUS_SUCCESS) {
                Updater.status(c, "installed " + Updater.installedVersion(c));
            } else {
                String m = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
                Updater.status(c, "install did not happen (" + (st == PackageInstaller.STATUS_FAILURE_ABORTED ? "cancelled" : "status " + st + (m == null ? "" : ": " + m)) + ")");
            }
        }
    }
}
