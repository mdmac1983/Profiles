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
}
