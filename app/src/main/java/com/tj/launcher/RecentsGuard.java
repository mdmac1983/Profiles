package com.tj.launcher;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import java.util.List;

/**
 * W2 (Tj, 2026-11-03): "Launcher still shows in the recent apps menu. I want it to only show solitaire."
 * Manifest and launch flags only apply when a task is CREATED, so a task that already existed (made by an earlier version, or by Android itself) kept showing.
 * This sets the flag on our tasks at runtime (AppTask.setExcludeFromRecents works on an existing task), every time one of our screens is resumed.
 * Hedge (Tj's own fallback): should a card ever still appear, it carries the target app's name and icon instead of "Launcher".
 */
public final class RecentsGuard {
    private static final String TAG = "TjRecentsGuard";
    private static volatile boolean describedOnce = false;

    private RecentsGuard() {}

    /** Marks every task of this app as excluded from Recents. Cheap, never throws. */
    public static void excludeOurTasks(Context c) {
        try {
            ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> tasks = am == null ? null : am.getAppTasks();
            if (tasks == null) return;
            for (ActivityManager.AppTask t : tasks) t.setExcludeFromRecents(true);
        } catch (Throwable t) { Log.e(TAG, "exclude failed", t); }
    }

    /** Makes a leftover Recents card look like the target app (name + icon). Done once per process, off the critical path. */
    public static void describeAsTarget(final Activity a) {
        if (describedOnce) return;
        describedOnce = true;
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() { @Override public void run() {
            try {
                String pkg = ReturnReset.targetPackage(a);
                if (pkg == null) { describedOnce = false; return; }
                PackageManager pm = a.getPackageManager();
                CharSequence label = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0));
                Drawable d = pm.getApplicationIcon(pkg);
                int px = (int) (48 * a.getResources().getDisplayMetrics().density);
                Bitmap bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
                Canvas cv = new Canvas(bmp);
                d.setBounds(0, 0, px, px);
                d.draw(cv);
                a.setTaskDescription(new ActivityManager.TaskDescription(String.valueOf(label), bmp));
            } catch (Throwable t) { describedOnce = false; Log.e(TAG, "describe failed", t); }
        } }, 1500);
    }
}
