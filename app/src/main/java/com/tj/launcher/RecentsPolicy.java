package com.tj.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Tj (2026-11-02): "permanently only show solitaire in the recent apps menu". Every app the custom launcher starts gets FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS (the task never enters Recents)
 * except the target app (Solitaire). No automation, no flicker. Only affects launches made from the custom launcher; Quickstep mode is stock.
 */
public final class RecentsPolicy {
    public static final String KEY = "recents_only_target";   // default ON
    private RecentsPolicy() {}

    /** Pure rule (JVM-testable). */
    public static boolean shouldExclude(boolean enabled, String pkg, String targetPkg, String selfPkg) {
        if (!enabled || pkg == null || pkg.isEmpty()) return false;
        return !pkg.equals(targetPkg) && !pkg.equals(selfPkg);
    }

    public static boolean enabled(Context c) { return Features.get(c, KEY, true); }

    /** Called by the patched Launcher3 right before it starts an app. Never throws; on any problem the launch stays exactly as stock. */
    public static void apply(Context c, Intent intent) {
        try {
            ComponentName cn = intent.getComponent();
            String pkg = cn != null ? cn.getPackageName() : intent.getPackage();
            if (shouldExclude(enabled(c), pkg, ReturnReset.targetPackage(c), c.getPackageName())) {
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            }
        } catch (Throwable t) {
            Log.e("TjRecents", "apply failed", t);
        }
    }
}
