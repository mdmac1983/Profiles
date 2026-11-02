package com.tj.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.List;

/** Finds the device's own launcher (Quickstep) at runtime. Nothing is hard-coded except a preference for com.android.launcher3. */
public final class StockLauncher {
    private StockLauncher() {}

    /** Pure choice rule, testable on the JVM: skip ourselves, prefer a system app, prefer com.android.launcher3, else first. */
    public static ActivityInfo choose(List<ActivityInfo> homes, String selfPackage) {
        ActivityInfo best = null;
        int bestScore = -1;
        for (ActivityInfo a : homes) {
            if (a == null || selfPackage.equals(a.packageName)) continue;
            int score = 1;
            if (a.applicationInfo != null && (a.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) score += 2;
            if ("com.android.launcher3".equals(a.packageName)) score += 4;
            if (score > bestScore) { bestScore = score; best = a; }
        }
        return best;
    }

    public static ComponentName find(Context c) {
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> ris = c.getPackageManager().queryIntentActivities(home, PackageManager.MATCH_DEFAULT_ONLY);
        java.util.ArrayList<ActivityInfo> infos = new java.util.ArrayList<>();
        for (ResolveInfo ri : ris) infos.add(ri.activityInfo);
        ActivityInfo a = choose(infos, c.getPackageName());
        return a == null ? null : new ComponentName(a.packageName, a.name);
    }
}
