package com.tj.launcher;

/**
 * W5 (Tj, 2026-11-03): "the ONLY way that I should see the quickstep launcher and its home screen is by using the gesture."
 * Why it happens: our Launcher is started with an explicit intent, so Android does not treat it as a HOME-type task. Once the stock launcher's home
 * task exists (any earlier visit to Quickstep), closing the last app makes Android resume THAT task instead of asking the router.
 * The accessibility helper sees the stock home window appear while we are in custom mode and presses Home (which the router sends to our Launcher).
 * Pure rules here (JVM-testable); the service does the pressing.
 */
public final class StrayHomeGuard {
    public static final String KEY = "guard_stock_home";      // switch, default ON
    public static final String COUNT_KEY = "guard_count";
    public static final String LAST_KEY = "guard_last";
    /** At most MAX redirects inside WINDOW_MS, then stand down for COOLDOWN_MS: a launcher that cannot start can never be fought in a loop. */
    public static final int MAX = 4;
    public static final long WINDOW_MS = 8_000, COOLDOWN_MS = 30_000;

    private StrayHomeGuard() {}

    /** The window that just appeared is the stock launcher's HOME activity (not its Recents screen, not a dialog). */
    public static boolean isStockHome(String pkg, String cls, String stockPkg, String stockCls) {
        return pkg != null && cls != null && stockPkg != null && stockCls != null && pkg.equals(stockPkg) && cls.equals(stockCls);
    }

    /** @param recent times (ms) of earlier redirects, 0 = unused slot */
    public static boolean shouldRedirect(boolean enabled, boolean customMode, boolean keyguardLocked, boolean stockHomeShown,
                                         long now, long failOpenUntil, long standDownUntil, long[] recent) {
        if (!enabled || !customMode || keyguardLocked || !stockHomeShown) return false;
        if (now < failOpenUntil || now < standDownUntil) return false;
        return countInWindow(recent, now) < MAX;
    }

    public static int countInWindow(long[] recent, long now) {
        int n = 0;
        for (long t : recent) if (t > 0 && now - t <= WINDOW_MS) n++;
        return n;
    }
}
