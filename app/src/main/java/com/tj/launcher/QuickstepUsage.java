package com.tj.launcher;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * AI1 (Tj, 2026-11-06: "I had an app called portfolio open. It is only in the quickstep launcher. I pressed the app icon to return to custom launcher, and portfolio was in the recent apps list with solitaire").
 * Which apps were opened in Quickstep is normally tracked by the accessibility helper (a window of the app appeared while Quickstep was on). If that tracking misses an app (an event never arrived, or the marks were
 * cleared early) the clean-up never closes it and nothing says why. Android keeps a second, independent record: the usage events (ACTIVITY_RESUMED), which the app can already read (the usage-access app-op the adb
 * setup grants; the unlock safety net {@link UsageGap} uses the same source). This class turns that record into "the apps used during THIS Quickstep session": pure and JVM-tested, plus the thin Android reader.
 *
 * Events that happen while the lock screen is up, or in the moments right after it goes away, are NOT counted: then Android reveals the app that was in front behind the keyguard (the custom account's last app,
 * AC2), which Tj did not open in Quickstep. The tracker has the same rule ({@link ProfileSession#windowIsLockLeftover}).
 */
public final class QuickstepUsage {
    /** UsageEvents.Event.ACTIVITY_RESUMED (= the old MOVE_TO_FOREGROUND). */
    public static final int RESUMED = 1;
    public static final int KEYGUARD_SHOWN = 17, KEYGUARD_HIDDEN = 18;

    private QuickstepUsage() {}

    /** One usage event, reduced to what the rule needs. */
    public static final class Ev {
        public final long t; public final int type; public final String pkg;
        public Ev(long t, int type, String pkg) { this.t = t; this.type = type; this.pkg = pkg; }
    }

    /**
     * Pure: the packages that were resumed during the session, in order of first use, without the ones that only appeared under the lock screen. {@code events} must be in time order and limited to the session.
     * If the first keyguard event of the session is "hidden" the lock screen was up when the session began (the gesture's handshake happens ON the lock screen), so everything before it is lock-screen time.
     */
    public static Set<String> packages(List<Ev> events) {
        Set<String> out = new LinkedHashSet<>();
        if (events == null) return out;
        boolean shown = false;
        for (Ev e : events) {
            if (e.type == KEYGUARD_SHOWN) { shown = false; break; }
            if (e.type == KEYGUARD_HIDDEN) { shown = true; break; }
        }
        long hiddenAt = 0;
        for (Ev e : events) {
            if (e.type == KEYGUARD_SHOWN) { shown = true; continue; }
            if (e.type == KEYGUARD_HIDDEN) { shown = false; hiddenAt = e.t; continue; }
            if (e.type != RESUMED || e.pkg == null || e.pkg.isEmpty()) continue;
            if (shown) continue;                                                               // under the lock screen
            if (hiddenAt > 0 && e.t - hiddenAt < ProfileSession.UNLOCK_QUIET_MS) continue;     // the moments right after the unlock
            out.add(e.pkg);
        }
        return out;
    }

    /** Android side: the packages resumed between {@code fromMs} and {@code toMs}. Never throws; empty when usage access is not granted. */
    public static Set<String> resumedDuring(Context c, long fromMs, long toMs) {
        try {
            if (toMs <= fromMs) return new LinkedHashSet<>();
            UsageStatsManager um = (UsageStatsManager) c.getSystemService(Context.USAGE_STATS_SERVICE);
            if (um == null) return new LinkedHashSet<>();
            UsageEvents ev = um.queryEvents(Math.max(fromMs, toMs - UsageGap.MAX_LOOKBACK_MS), toMs);
            if (ev == null) return new LinkedHashSet<>();
            List<Ev> list = new ArrayList<>();
            UsageEvents.Event e = new UsageEvents.Event();
            while (ev.hasNextEvent()) {
                ev.getNextEvent(e);
                int ty = e.getEventType();
                if (ty == RESUMED || ty == KEYGUARD_SHOWN || ty == KEYGUARD_HIDDEN) list.add(new Ev(e.getTimeStamp(), ty, e.getPackageName()));
            }
            return packages(list);
        } catch (Throwable t) { return new LinkedHashSet<>(); }
    }
}
