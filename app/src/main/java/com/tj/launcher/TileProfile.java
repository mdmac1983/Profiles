package com.tj.launcher;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/**
 * D6 (Tj, 2026-11-04): ONE quick-settings tile in the custom launcher, THREE in Quickstep. The tile list is the secure setting {@code sysui_qs_tiles} (SystemUI reloads it live).
 * Android's "minimum number of tiles" rule exists only in SystemUI's tile EDITOR (that is why Tj needed adb to get down to one); writing the setting directly bypasses the editor.
 * Needs the WRITE_SECURE_SETTINGS adb grant (without it nothing happens). The value that was there first is snapshotted (RestoreStore) so "Restore everything" puts it back.
 */
public final class TileProfile {
    private static final String TAG = "TjTiles";
    public static final String KEY = "qs_tiles_per_mode";                // switch, default ON (does nothing without the adb grant)
    public static final String CUSTOM_KEY = "qs_tiles_custom";           // default "rotation"
    public static final String QUICKSTEP_KEY = "qs_tiles_quickstep";     // default "wifi,bt,rotation"
    public static final String SETTING = "sysui_qs_tiles";
    public static final String DEFAULT_CUSTOM = "rotation", DEFAULT_QUICKSTEP = "wifi,bt,rotation";
    /** The built-in tile names Tj can pick from in Launcher control. */
    public static final String[] CHOICES = { "wifi", "bt", "rotation", "flashlight", "airplane", "dnd", "location", "battery", "cast", "hotspot", "cell", "inversion", "night" };

    private TileProfile() {}

    public static boolean enabled(Context c) { return Features.get(c, KEY, true); }
    public static boolean permitted(Context c) { return c.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == android.content.pm.PackageManager.PERMISSION_GRANTED; }

    /** Pure rule (JVM-testable): a comma list of tile specs; keeps letters, digits and _ ( ) . / : only; never empty (falls back to {@code fallback}). */
    public static String sanitize(String in, String fallback) {
        if (in == null) return fallback;
        StringBuilder b = new StringBuilder();
        for (String part : in.split(",")) {
            String t = part.trim();
            StringBuilder ok = new StringBuilder();
            for (int i = 0; i < t.length(); i++) {
                char ch = t.charAt(i);
                if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '(' || ch == ')' || ch == '.' || ch == '/' || ch == ':') ok.append(ch);
            }
            if (ok.length() == 0) continue;
            if (b.length() > 0) b.append(',');
            b.append(ok);
        }
        return b.length() == 0 ? fallback : b.toString();
    }

    public static String tilesFor(Context c, String mode) {
        return Mode.QUICKSTEP.equals(mode) ? sanitize(Features.getString(c, QUICKSTEP_KEY, DEFAULT_QUICKSTEP), DEFAULT_QUICKSTEP)
                                           : sanitize(Features.getString(c, CUSTOM_KEY, DEFAULT_CUSTOM), DEFAULT_CUSTOM);
    }

    /** Writes the tile list for this mode. Cheap and exception-safe; does nothing when switched off or the adb grant is missing. */
    public static void apply(Context c, String mode) {
        try {
            if (!enabled(c) || !permitted(c)) return;
            RestoreStore.snapshotSecure(c, SETTING);   // the original list is on disk BEFORE the first change
            String want = tilesFor(c, mode);
            String cur = Settings.Secure.getString(c.getContentResolver(), SETTING);
            if (want.equals(cur)) return;
            Settings.Secure.putString(c.getContentResolver(), SETTING, want);
            Log.i(TAG, "tiles for " + mode + ": " + want);
        } catch (Throwable t) { Log.e(TAG, "could not set the tiles", t); }
    }
}
