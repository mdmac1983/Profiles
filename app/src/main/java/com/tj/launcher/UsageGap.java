package com.tj.launcher;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;

/**
 * AC1 (Tj, 2026-11-05: "type the regular code with no gesture ... it should ALWAYS open to the custom launcher, without exception"). Everything the lock-screen rules do happens in the accessibility helper's
 * process. If that process is not running at the moment of the unlock (the system killed it, or it is re-binding after a crash) nothing sees the plain PIN, and Android just shows whatever was in front before the
 * lock: Quickstep. When the helper comes back (or the next Home press arrives) it can still find out that the keyguard WAS shown after the Quickstep session began, and no handshake was confirmed for that
 * lock: the session is over, so the custom launcher takes the tablet (the same ending a plain unlock gives). The facts come from Android's own usage events (KEYGUARD_SHOWN), which any app with the
 * "usage access" app-op (GET_USAGE_STATS, already granted by the adb setup) can read after the fact.
 */
public final class UsageGap {
    /** android.app.usage.UsageEvents.Event.KEYGUARD_SHOWN (public since API 28). */
    public static final int KEYGUARD_SHOWN = 17;
    /** Never look further back than this (a query over days of events is heavy and no session lives that long). */
    public static final long MAX_LOOKBACK_MS = 12L * 3600_000L;

    private UsageGap() {}

    /** Pure: was the keyguard shown AFTER the Quickstep session began (the later of the mode change and the last confirmed handshake unlock)? A reference of 0 = unknown = never. */
    public static boolean lockedSince(long lastKeyguardShownMs, long sessionRefMs) {
        return sessionRefMs > 0 && lastKeyguardShownMs > 0 && lastKeyguardShownMs > sessionRefMs;
    }

    /**
     * Pure: end the Quickstep session now? Quickstep mode, automation not paused, the tablet is unlocked and awake right now (so the user is already looking at the wrong launcher),
     * and the keyguard was shown after the session began.
     */
    public static boolean shouldEndSession(boolean customMode, boolean automationOff, boolean keyguardLockedNow, boolean interactive, long lastKeyguardShownMs, long sessionRefMs) {
        return !customMode && !automationOff && !keyguardLockedNow && interactive && lockedSince(lastKeyguardShownMs, sessionRefMs);
    }

    /** Pure: the start of the window to ask Android about: the session reference, but never more than {@link #MAX_LOOKBACK_MS} ago. */
    public static long queryFrom(long sessionRefMs, long nowMs) { return Math.max(sessionRefMs, nowMs - MAX_LOOKBACK_MS); }

    /** The time of the newest KEYGUARD_SHOWN event since {@code sinceMs}, or 0 when there is none or usage access is not granted. Never throws. */
    public static long lastKeyguardShown(Context c, long sinceMs) {
        try {
            UsageStatsManager um = (UsageStatsManager) c.getSystemService(Context.USAGE_STATS_SERVICE);
            if (um == null) return 0;
            UsageEvents ev = um.queryEvents(sinceMs, System.currentTimeMillis());
            if (ev == null) return 0;
            UsageEvents.Event e = new UsageEvents.Event();
            long last = 0;
            while (ev.hasNextEvent()) {
                ev.getNextEvent(e);
                if (e.getEventType() == KEYGUARD_SHOWN && e.getTimeStamp() > last) last = e.getTimeStamp();
            }
            return last;
        } catch (Throwable t) { return 0; }
    }

    /**
     * Android side of the safety net: true when the Quickstep session must end because the keyguard was shown since it began. Does the cheap tests first; the usage query only runs in Quickstep mode with
     * automation not paused and a known session start.
     */
    public static boolean sessionEndedWhileAway(Context c) {
        try {
            if (Mode.isCustom(c) || CrashGuard.automationOff(c)) return false;
            long ref = Mode.sessionRefMs(c);
            if (ref <= 0) return false;
            android.app.KeyguardManager km = (android.app.KeyguardManager) c.getSystemService(Context.KEYGUARD_SERVICE);
            android.os.PowerManager pm = (android.os.PowerManager) c.getSystemService(Context.POWER_SERVICE);
            boolean locked = km != null && km.isKeyguardLocked(), interactive = pm != null && pm.isInteractive();
            if (locked || !interactive) return false;
            long shown = lastKeyguardShown(c, queryFrom(ref, System.currentTimeMillis()));
            return shouldEndSession(false, false, locked, interactive, shown, ref);
        } catch (Throwable t) { return false; }
    }
}
