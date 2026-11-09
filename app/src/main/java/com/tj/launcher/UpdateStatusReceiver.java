package com.tj.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;

/**
 * BS1a: the PackageInstaller session's answer for the in-app updater. NOT exported: only our own PendingIntent (see {@link Updater}) reaches it, so the confirmation Intent
 * it starts really comes from Android's installer. "Needs the user" opens Android's own confirmation screen; when Android 10 refuses that start from the background
 * (no visible window, no bound helper), the Intent waits in {@link Updater#pendingConfirm} and Profiles settings opens it the next time it is shown.
 */
public class UpdateStatusReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        final Context c = context.getApplicationContext();
        if (intent == null || !Updater.ACTION_INSTALL_STATUS.equals(intent.getAction())) return;
        int st = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
        if (st == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent confirm = intent.getParcelableExtra(Intent.EXTRA_INTENT);
            if (confirm == null) { Updater.status(c, "the installer gave no confirmation screen"); return; }
            confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Updater.pendingConfirm = confirm;
            try { c.startActivity(confirm); Updater.status(c, "confirm the install on screen"); }
            catch (Throwable t) { Updater.status(c, "open Profiles settings to confirm the install"); }
        } else if (st == PackageInstaller.STATUS_SUCCESS) {
            Updater.pendingConfirm = null;
            Updater.status(c, "installed " + Updater.installedVersion(c));
        } else {
            Updater.pendingConfirm = null;
            String m = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
            Updater.status(c, "install did not happen (" + (st == PackageInstaller.STATUS_FAILURE_ABORTED ? "cancelled" : "status " + st + (m == null ? "" : ": " + m)) + ")");
        }
    }
}
