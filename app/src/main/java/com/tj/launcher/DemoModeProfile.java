package com.tj.launcher;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/**
 * Q3 / R3 (Tj, 2026-11-04: "remove the pencil icon from the notification panel without delay, or so fast it isn't noticeable"). MEASURED on the emulator (run 55): with the global setting
 * {@code device_demo_mode = 1} SystemUI's own quick-settings footer is drawn WITHOUT the pencil and WITHOUT the gear (QSFooterImpl hides both in demo mode), so there is nothing to cover and no delay.
 * The launcher already holds WRITE_SECURE_SETTINGS; this writes the flag only while the CUSTOM launcher is active and puts the original value back for Quickstep (snapshot first, like the tiles).
 * EXPERIMENTAL and default OFF: demo mode is a retail-demo flag; the emulator showed no other change (power menu, lock screen and shade are the same), but only the tablet can confirm.
 */
public final class DemoModeProfile {
    private static final String TAG = "TjDemo";
    public static final String KEY = "profile_hide_pencil_native";     // switch, default OFF
    public static final String SETTING = "device_demo_mode";

    private DemoModeProfile() {}

    public static boolean enabled(Context c) { return Features.get(c, KEY, false); }
    public static boolean permitted(Context c) { return c.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == android.content.pm.PackageManager.PERMISSION_GRANTED; }

    /** Pure rule (JVM-testable): the value to write for a mode, or null = leave the system alone. Custom -> "1" (hide pencil + gear); Quickstep -> the original value. */
    public static String valueFor(boolean enabled, String mode, String original) {
        if (!enabled) return null;
        return Mode.QUICKSTEP.equals(mode) ? (original == null || original.isEmpty() ? "0" : original) : "1";
    }

    /** True while the flag is currently on (the pencil cover is then unnecessary). */
    public static boolean active(Context c) {
        try { return Settings.Global.getInt(c.getContentResolver(), SETTING, 0) == 1; } catch (Throwable t) { return false; }
    }

    public static void apply(Context c, String mode) {
        try {
            if (!permitted(c)) return;
            String original = RestoreStore.globalSnapshot(c, SETTING, "0");
            if (enabled(c)) RestoreStore.snapshotGlobal(c, SETTING);   // the original is on disk BEFORE the first change
            original = RestoreStore.globalSnapshot(c, SETTING, "0");
            String want = valueFor(enabled(c), mode, original);
            if (want == null) {
                // switched off: if we had turned it on, put the original back
                if (active(c) && Mode.isCustom(c)) { Settings.Global.putString(c.getContentResolver(), SETTING, original); Log.i(TAG, "switched off: demo flag back to " + original); }
                return;
            }
            String cur = Settings.Global.getString(c.getContentResolver(), SETTING);
            if (want.equals(cur) || (cur == null && "0".equals(want))) return;
            Settings.Global.putString(c.getContentResolver(), SETTING, want);
            Log.i(TAG, "device_demo_mode -> " + want + " for " + mode);
        } catch (Throwable t) { Log.e(TAG, "could not set the demo flag", t); }
    }
}
