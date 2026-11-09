package com.tj.launcher;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/**
 * AA1 (Tj, 2026-11-05: "Build number 2"). Android 10's {@code Settings.Global.policy_control = immersive.status=*} hides the STATUS BAR for every app while it is set. The launcher writes it only while the
 * custom launcher is active and puts the original value back for Quickstep (snapshot first, like the tiles and the demo flag).
 *
 * WHAT IT IS, MEASURED on the Android 10 emulator (run 78, 2026-11-05) - and what I got WRONG when I proposed it ("seals the shade"): the bar disappears and nothing else on screen moves
 * (content MAE 0.0006), the LOCK SCREEN is untouched (MAE 0.002), but it is NOT a lock: ONE swipe down from the top edge only shows the bar again, a SECOND swipe opens the notification panel (its window grows
 * from 26 to 976 px), and {@code cmd statusbar expand-settings} still works. That is exactly what Tj needs for the rotation tile, so this is a QUIETER LOOK for the custom account (no clock, no
 * battery, no notification icons from Quickstep's apps until you reach for the bar), not a way to take the panel away. Default OFF. Needs WRITE_SECURE_SETTINGS (the adb grant), like the tiles.
 */
public final class ImmersiveProfile {
    private static final String TAG = "TjImmersive";
    public static final String KEY = "profile_hide_status_bar";      // switch, default OFF
    public static final String SETTING = "policy_control";
    public static final String OURS = "immersive.status=*";

    private ImmersiveProfile() {}

    public static boolean enabled(Context c) { return Profiles.getBool(c, Profiles.active(c), KEY, false); }   // BT1a: per custom profile
    public static boolean permitted(Context c) { return c.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == android.content.pm.PackageManager.PERMISSION_GRANTED; }

    /**
     * Pure (JVM-testable): {@code original} with our entry in it. policy_control is a ':'-separated list of "immersive.<kind>=<filter>" entries; an existing "immersive.status" entry is replaced
     * (ours wins while the custom launcher is active), every other entry (navigation bar, confirmations) is kept exactly.
     */
    public static String merge(String original) {
        StringBuilder sb = new StringBuilder();
        if (original != null) {
            for (String part : original.split(":")) {
                String t = part.trim();
                if (t.isEmpty() || t.startsWith("immersive.status=") || t.startsWith("immersive.full=")) continue;   // "full" would hide the status bar too: ours replaces both
                if (sb.length() > 0) sb.append(':');
                sb.append(t);
            }
        }
        if (sb.length() > 0) sb.append(':');
        return sb.append(OURS).toString();
    }

    /** Pure rule: what to write for this mode, or {@code NOTHING} = leave the system alone. Custom -> original + ours; Quickstep -> the original exactly (null = remove the setting). */
    public static final String NOTHING = "\u0000nothing";
    public static String valueFor(boolean enabled, String mode, String original) {
        if (!enabled) return NOTHING;
        return Mode.QUICKSTEP.equals(mode) ? original : merge(original);
    }

    /** True while the status bar is currently hidden by us. */
    public static boolean active(Context c) {
        try { String v = Settings.Global.getString(c.getContentResolver(), SETTING); return v != null && v.contains(OURS); } catch (Throwable t) { return false; }
    }

    /**
     * Pure rule (JVM-tested): may the switched-OFF launcher put the saved original back? Only when it has a snapshot (it took ownership of the setting at some point) AND the bar is hidden right now.
     * Without a snapshot the value is somebody else's (an adb setup, another tool), and deleting it would be a change the user never asked for (review of 2026-11-05).
     */
    public static boolean mayUndoWhenOff(boolean hasSnapshot, boolean hiddenNow) { return hasSnapshot && hiddenNow; }

    /** Writes the value for this mode. Cheap and exception-safe; does nothing when switched off or without the adb grant. */
    public static void apply(Context c, String mode) {
        try {
            if (!permitted(c)) return;
            boolean on = enabled(c), hiddenNow = active(c), have = RestoreStore.hasGlobalSnapshot(c, SETTING);
            // the original is on disk BEFORE the first change. While we are not the one holding the changed value, whatever is there IS the original (also when another tool changed it since the last time);
            // the very first snapshot is taken whatever the value looks like (it may already contain our entry: then that IS the user's original).
            if (on && (!hiddenNow || !have)) { RestoreStore.resnapshotGlobal(c, SETTING); have = true; }
            String original = RestoreStore.globalSnapshot(c, SETTING, null);
            String want = valueFor(on, mode, original);
            if (NOTHING.equals(want)) {
                // switched off: if WE had hidden the bar, put the original back
                if (mayUndoWhenOff(have, hiddenNow)) { Settings.Global.putString(c.getContentResolver(), SETTING, original); Log.i(TAG, "switched off: policy_control back to " + original); }
                return;
            }
            String cur = Settings.Global.getString(c.getContentResolver(), SETTING);
            if (want == null ? cur == null : want.equals(cur)) return;
            Settings.Global.putString(c.getContentResolver(), SETTING, want);
            Log.i(TAG, "policy_control -> " + want + " for " + mode);
        } catch (Throwable t) { Log.e(TAG, "could not set policy_control", t); }
    }
}
