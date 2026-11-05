package com.tj.launcher;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/**
 * REMOVED AS A FEATURE (AB1 safety audit, Tj 2026-11-05: "It is important that this project doesn't break anything or require me to factory reset my tablet").
 *
 * This used to switch {@code Settings.Global.device_demo_mode} on while the custom launcher was active, because Android's own quick-settings footer is drawn without the pencil and the gear in
 * demo mode (emulator run 55). But device_demo_mode is Android's RETAIL-DEMO flag: AOSP's retail-mode documentation says that once it is set, RetailDemoModeService "creates a demo user and
 * switches to it when user 0 is started" (that is, at boot), enables an overlay-specified launcher and disables setup. Nobody tested a reboot with the flag on, and on a tablet the way out of a
 * surprise demo user could be a factory reset. The pencil cover (accessibility) does the job without any such risk, so the switch, the Control entry and the guide card are gone.
 *
 * What stays: the one-time cleanup for anybody who had switched the old option on (it puts the flag back to what it was), and a read of the current value (the pencil cover steps aside
 * if some other tool already turned the flag on).
 */
public final class DemoModeProfile {
    private static final String TAG = "TjDemo";
    public static final String KEY = "profile_hide_pencil_native";     // the old switch (kept only so the cleanup can see it)
    public static final String SETTING = "device_demo_mode";
    private static final String DONE_KEY = "demo_flag_cleanup_done";

    private DemoModeProfile() {}

    public static boolean permitted(Context c) { return c.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == android.content.pm.PackageManager.PERMISSION_GRANTED; }

    /** True while the flag is currently on (somebody else turned it on: the pencil cover is then unnecessary). */
    public static boolean active(Context c) {
        try { return Settings.Global.getInt(c.getContentResolver(), SETTING, 0) == 1; } catch (Throwable t) { return false; }
    }

    /**
     * Pure rule (JVM-testable): what to write to undo the old option, or null = leave the system alone. Only when the flag is on NOW, it was turned on by us (evidence: the old switch was on or a
     * snapshot exists), and the value before our change was not 1. An unset original becomes "0".
     */
    public static String undoValue(String current, boolean ours, String original) {
        if (!ours || !"1".equals(current) || "1".equals(original)) return null;
        return original == null || original.isEmpty() || "null".equals(original) ? "0" : original;
    }

    /** Runs the cleanup once per install after an update (needs the adb grant to write; without it nothing could have been switched on, so it just marks itself done). Cheap. */
    public static void cleanupOnce(Context c) {
        try {
            if (Features.get(c, DONE_KEY, false)) return;
            boolean ours = Features.get(c, KEY, false) || RestoreStore.hasGlobalSnapshot(c, SETTING);
            if (ours && !permitted(c)) return;   // cannot write yet: try again after the grant
            if (ours) {
                String cur = Settings.Global.getString(c.getContentResolver(), SETTING);
                String undo = undoValue(cur, true, RestoreStore.globalSnapshot(c, SETTING, null));
                if (undo != null) { Settings.Global.putString(c.getContentResolver(), SETTING, undo); Log.i(TAG, "cleanup: the retail-demo flag was on from the old option; put back to " + undo); }
                Features.set(c, KEY, false);
            }
            Features.set(c, DONE_KEY, true);
        } catch (Throwable t) { Log.e(TAG, "cleanup failed", t); }
    }
}
