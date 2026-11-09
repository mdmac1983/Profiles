package com.tj.launcher;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * BT1d (v1.3, user 2026-11-09: per-profile home layout; "4. copy of fbop"): each custom profile has its OWN home-screen layout. Launcher3 keeps the layout in one SQLite
 * database (launcher.db, FBOP's, unchanged); Rental's lives in launcher_rental.db, which starts as a copy of FBOP's the first time Rental is used. The provider asks
 * {@link #dbName} when it opens its database (LauncherProvider.DatabaseHelper), and {@link Profiles#applyActive} asks it to close the old one ({@link #METHOD_SWITCH}) before
 * the model is reloaded, so the next read opens the active profile's file.
 */
public final class ProfileLayout {
    private static final String TAG = "TjLayout";
    public static final String FBOP_DB = com.android.launcher3.LauncherFiles.LAUNCHER_DB, RENTAL_DB = "launcher_rental.db";
    /** LauncherProvider.call: close the open database if it is not the active profile's (the next query opens the right one). */
    public static final String METHOD_SWITCH = "tj_profile_db";
    private static final String[] SUFFIXES = { "", "-wal", "-shm", "-journal" };

    private ProfileLayout() {}

    public static String dbNameFor(ProfileRules.Profile p) { return ProfileRules.customOrDefault(p) == ProfileRules.Profile.RENTAL ? RENTAL_DB : FBOP_DB; }

    /** The database the provider opens now. A Rental database that does not exist yet is made as a copy of FBOP's first. Called with no database open. */
    public static String dbName(Context c) {
        String name = dbNameFor(Profiles.active(c));
        if (RENTAL_DB.equals(name)) ensureCopy(c);
        return name;
    }

    /** Copies FBOP's database files to Rental's names when Rental has none. Only while no helper has the files open (provider start, or after the switch closed it). */
    public static void ensureCopy(Context c) {
        try {
            File rental = c.getDatabasePath(RENTAL_DB), fbop = c.getDatabasePath(FBOP_DB);
            if (rental.exists() || !fbop.exists()) return;
            for (String s : SUFFIXES) {
                File from = new File(fbop.getPath() + s);
                if (from.exists()) copy(from, new File(rental.getPath() + s));
            }
            Log.i(TAG, "Rental's home layout starts as a copy of FBOP's (" + fbop.length() + " bytes)");
        } catch (Throwable t) { Log.e(TAG, "copying FBOP's layout failed (Rental starts with the default layout)", t); }
    }

    private static void copy(File from, File to) throws java.io.IOException {
        File tmp = new File(to.getPath() + ".tmp");
        try (InputStream in = new FileInputStream(from); OutputStream out = new FileOutputStream(tmp)) {
            byte[] b = new byte[65536]; int n;
            while ((n = in.read(b)) > 0) out.write(b, 0, n);
        }
        if (!tmp.renameTo(to)) { tmp.delete(); throw new java.io.IOException("rename failed: " + to); }
    }

    /** Asks the provider (in this process) to let go of a database that is not the active profile's. Then the caller reloads the model. */
    public static void switchDb(Context c) {
        try { com.android.launcher3.LauncherSettings.Settings.call(c.getContentResolver(), METHOD_SWITCH); }
        catch (Throwable t) { Log.e(TAG, "layout switch failed", t); }
    }
}
