package com.tj.launcher;

import android.content.Context;
import java.util.Map;

/**
 * v0.7.3 (found while answering Tj's AC1/AC8, 2026-11-05): the crash-breaker reset of v0.7.1 (12:48 on the real tablet) did not only turn the launcher's Two-profiles switches off, it WROTE "false" for
 * "Prepare the custom launcher behind the lock screen", "No Wi-Fi or Bluetooth in the custom launcher" and "Close Quickstep's apps at lock" (code default: ON). Those explicit values survive an update, so the
 * custom launcher would stay without them until someone found the switches. If ALL THREE are explicitly false (the signature of that reset: nobody turns all three off on purpose) they are removed once, so the
 * defaults (ON) apply again, and Launcher control says so. A single switch Tj set himself never matches.
 */
public final class SwitchMigration {
    static final String DONE_KEY = "migrated_wipe_073";
    public static final String NOTE_KEY = "migrate_note";

    private SwitchMigration() {}

    /** Pure: do these three stored values (null = not stored) look like the old breaker reset? */
    public static boolean looksLikeTheBreakerWipe(Boolean prearm, Boolean radios, Boolean preclean) {
        return Boolean.FALSE.equals(prearm) && Boolean.FALSE.equals(radios) && Boolean.FALSE.equals(preclean);
    }

    private static Boolean bool(Object o) { return o instanceof Boolean ? (Boolean) o : null; }

    /** Runs once per install (the marker is a preference). Cheap, exception-safe. */
    public static void run(Context c) {
        try {
            if (Features.get(c, DONE_KEY, false)) return;
            Map<String, ?> all = Features.all(c);
            boolean wipe = looksLikeTheBreakerWipe(bool(all.get(ProfileSession.PREARM_KEY)), bool(all.get(ProfileSession.RADIOS_KEY)), bool(all.get(ProfileSession.PRECLEAN_KEY)));
            Features.set(c, DONE_KEY, true);
            if (!wipe) return;
            Features.remove(c, ProfileSession.PREARM_KEY); Features.remove(c, ProfileSession.RADIOS_KEY); Features.remove(c, ProfileSession.PRECLEAN_KEY);
            Features.setString(c, NOTE_KEY, "This update switched three switches back ON that the earlier crash reset had turned off: \"Prepare the custom launcher behind the lock screen\", \"No Wi-Fi or Bluetooth in the custom launcher\" and \"Close Quickstep's apps and notifications the moment the tablet locks\". Turn any of them off again below if you really want that.");
            LockLog.add(c, "MIGRATE", "pre-arm, radios and pre-clean were all stored as off (the old breaker reset): back to their defaults (on)");
        } catch (Throwable t) { android.util.Log.e("TjMigrate", "switch migration failed", t); }
    }
}
