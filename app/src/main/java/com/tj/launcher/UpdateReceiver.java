package com.tj.launcher;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * BS1a: DownloadManager's "download finished" for the in-app updater ({@link Updater}): only OUR two download ids are acted on, everything else is ignored.
 * Exported (the download service is another app). The installer's answer goes to the NON-exported {@link UpdateStatusReceiver}: an exported receiver that starts an
 * Intent it was handed would let any app start our private screens (code review 2026-11-09).
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
        }
    }
}
