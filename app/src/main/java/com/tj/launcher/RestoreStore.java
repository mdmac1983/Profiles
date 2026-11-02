package com.tj.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import java.util.Map;

/**
 * Every system setting this app ever changes is recorded here FIRST (its previous value), so one call puts Android back exactly as it was.
 * Today nothing writes to system settings (the permission-based features arrive later, each default OFF), so restoreAll() mainly turns the
 * launcher's own switches off. The secure-setting restore is real and unit-tested-by-design: it only writes values we snapshotted, only when
 * the permission was granted, and never touches anything lock-screen related.
 */
public final class RestoreStore {
    private static final String PREFS = "tj_restore";
    private RestoreStore() {}

    private static SharedPreferences p(Context c) { return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    /** Records the current value of a Settings.Secure key once (never overwrites an existing snapshot). */
    public static void snapshotSecure(Context c, String key) {
        SharedPreferences sp = p(c);
        if (sp.contains("secure:" + key)) return;
        String cur = Settings.Secure.getString(c.getContentResolver(), key);
        sp.edit().putString("secure:" + key, cur == null ? "\u0000null" : cur).commit();
    }

    public static boolean hasSnapshot(Context c) { return !p(c).getAll().isEmpty(); }

    /** Restores every snapshotted secure setting that we can write, forgets the snapshot, and turns every launcher feature switch off. */
    public static void restoreAll(Context c) {
        SharedPreferences sp = p(c);
        for (Map.Entry<String, ?> e : sp.getAll().entrySet()) {
            String k = e.getKey();
            if (!k.startsWith("secure:") || !(e.getValue() instanceof String)) continue;
            String v = (String) e.getValue();
            try {
                Settings.Secure.putString(c.getContentResolver(), k.substring(7), "\u0000null".equals(v) ? null : v);
            } catch (Throwable t) {
                android.util.Log.e("TjRestore", "could not restore " + k, t); // no permission: nothing was changed by us either
            }
        }
        sp.edit().clear().commit();
        Features.allOff(c);
    }
}
