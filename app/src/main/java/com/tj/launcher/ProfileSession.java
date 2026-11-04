package com.tj.launcher;

import android.content.Context;

/**
 * N3 (Tj, 2026-11-04): the custom launcher and Quickstep behave like TWO USER ACCOUNTS. The custom launcher is the default one; Quickstep exists only behind a secret handshake on the
 * lock screen ({@link UnlockMethods}). Locking the tablet ENDS a Quickstep session (like logging out): a plain PIN afterwards is the custom account again.
 *
 * What makes the custom account open "like a regular unlock, no delay": all of the switching is done WHILE the lock screen covers the screen.
 *  - wake on the lock screen: the custom stack (custom home + Solitaire) is started behind the keyguard (Mode is left alone until the unlock decides),
 *  - handshake accepted: Quickstep's home is started behind the keyguard instead (and Mode flips, so tiles / keyboard follow),
 *  - plain unlock after a Quickstep session: Mode flips to custom and the return reset closes what Quickstep left behind (hidden by the black cover that went up at the Enter tap).
 * Pure rules here (JVM-testable); {@link TjAccessibilityService} feeds the events and does the starting.
 */
public final class ProfileSession {
    public static final String END_ON_LOCK_KEY = "profile_end_on_lock";      // locking ends a Quickstep session, default ON
    public static final String PREARM_KEY = "profile_prearm";                // lock screen: put Solitaire (custom) / the stock home (handshake) in front behind the keyguard, default ON
    public static final String RADIOS_KEY = "profile_radios_off";            // custom account: Wi-Fi and Bluetooth are switched off again if something turns them on, default ON
    public static final String BUZZ_KEY = "unlock_buzz";                     // a short buzz when a handshake is accepted, default ON
    public static final String PREARM_REPORT_KEY = "profile_last";           // what the last lock-screen switch did (shown in Launcher control)

    private ProfileSession() {}

    public static boolean endOnLock(Context c) { return Features.get(c, END_ON_LOCK_KEY, true); }
    public static boolean prearm(Context c) { return Features.get(c, PREARM_KEY, true); }
    public static boolean radiosOff(Context c) { return Features.get(c, RADIOS_KEY, true); }
    public static boolean buzz(Context c) { return Features.get(c, BUZZ_KEY, true); }

    /** Pure: wake-up on the LOCK screen (keyguard locked, screen on). Put the custom stack in front behind the keyguard? Never while a handshake is armed (the target is Quickstep then). */
    public static boolean prearmCustomStackOnLockedWake(boolean prearmEnabled, boolean keyguardLocked, boolean handshakeArmed, boolean alreadyDoneThisLock, boolean crashTripped) {
        return prearmEnabled && keyguardLocked && !handshakeArmed && !alreadyDoneThisLock && !crashTripped;
    }

    /** Pure: a plain unlock (USER_PRESENT, no handshake): is the Quickstep session over, so the custom account takes the tablet? */
    public static boolean plainUnlockEndsQuickstep(boolean endOnLock, boolean customMode, boolean crashTripped) {
        return endOnLock && !customMode && !crashTripped;
    }

    /** Pure: the Enter tap of a plain PIN while a Quickstep session is still current: raise the black cover now, so the unlock animation never shows Quickstep's leftovers. */
    public static boolean coverAtEnter(boolean endOnLock, boolean customMode, boolean crashTripped, boolean handshakeGiven, boolean maybeWorkToDo, boolean coverEnabled) {
        return plainUnlockEndsQuickstep(endOnLock, customMode, crashTripped) && !handshakeGiven && maybeWorkToDo && coverEnabled;
    }

    /** Pure: is this the first Home of a NEW boot (the boot counter changed since we last looked) while a Quickstep session is still marked? A restart kills every app, so the session is over. */
    public static boolean bootEndsQuickstep(int seenBootCount, int nowBootCount, boolean endOnLock, boolean customMode, boolean crashTripped) {
        return nowBootCount >= 0 && seenBootCount != nowBootCount && endOnLock && !customMode && !crashTripped;
    }

    /** Pure: switch Wi-Fi / Bluetooth off again? Custom account only, at most 3 attempts per 60 s (a radio that refuses to go off is never fought in a loop). */
    public static boolean enforceRadios(boolean enabled, boolean customMode, long now, long[] recent) {
        if (!enabled || !customMode) return false;
        int n = 0;
        for (long t : recent) if (t > 0 && now - t <= 60_000) n++;
        return n < 3;
    }

    /**
     * Pure failsafe (L10): with the accessibility helper OFF no handshake can be seen, so the custom launcher would lock Tj in for good. Three Home presses inside 1.6 s then let him out
     * to Quickstep. With the helper ON this is ignored: the only way out is the handshake.
     */
    public static boolean homeFailsafe(boolean helperRunning, boolean customMode, long now, long p1, long p2) {
        if (helperRunning || !customMode) return false;
        return p1 > 0 && p2 > 0 && now - p2 <= 800 && p2 - p1 <= 800 && now >= p2 && p2 >= p1;
    }

    /** Called at every Home (the router): notices a new boot, remembers it, and ends a Quickstep session that outlived the restart. Cheap: one settings read. */
    public static void checkBoot(Context c) {
        try {
            int now = android.provider.Settings.Global.getInt(c.getContentResolver(), android.provider.Settings.Global.BOOT_COUNT, -1);
            int seen = Features.getInt(c, "boot_count_seen", -2);
            if (now == seen) return;
            Features.setInt(c, "boot_count_seen", now);
            if (seen == -2) return;   // first look after install
            if (bootEndsQuickstep(seen, now, endOnLock(c), Mode.isCustom(c), CrashGuard.wasTripped(c))) {
                Mode.set(c, Mode.CUSTOM);
                AppOrigin.clearQuickstep(c);
                Features.setString(c, PREARM_REPORT_KEY, "boot: a new boot ended the Quickstep session (custom launcher)");
            }
        } catch (Throwable t) { android.util.Log.e("TjBoot", "boot check failed", t); }
    }

    // ------------------------------------------------------------------ the two actions every method ends in
    /** Quickstep -> custom (any "kill switch" method): same as the double-Home return. Safe to call from any thread with a context. */
    public static void returnToCustom(Context c, String why) {
        Context app = c.getApplicationContext();
        if (Mode.isCustom(app)) return;
        CrashGuard.clearTripped(app);
        Mode.set(app, Mode.CUSTOM);
        Features.setString(app, FailsafeKeys.LAST_KEY, "back to custom via " + why + " at " + new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date()));
        app.startActivity(new android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME).setPackage(app.getPackageName())
                .putExtra(RouterActivity.EXTRA_PREARM, "custom").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION));
        ReturnReset.run(app);
    }

    /** Custom -> Quickstep (the master failsafe). Not a Home press: the router must not read it as a double-Home. */
    public static void goToQuickstep(Context c, String why) {
        Context app = c.getApplicationContext();
        Mode.set(app, Mode.QUICKSTEP);
        Features.setString(app, FailsafeKeys.LAST_KEY, "Quickstep via " + why + " at " + new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date()));
        app.startActivity(new android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME).setPackage(app.getPackageName())
                .putExtra(RouterActivity.EXTRA_PREARM, "stock").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
}
