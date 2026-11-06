package com.tj.launcher;

/**
 * AD5 (Tj, 2026-11-06: "when I unlock to quickstep, it minimizes my open apps and goes to the home screen. Is this necessary?"). No. A handshake used to start the stock HOME behind the lock screen, which covered the app
 * Tj was in. Now the gesture brings back the Quickstep app that was in front when the tablet locked (started like a tap on its icon: its task comes forward exactly as it was); with nothing to come back to it is the
 * stock home as before. Pure rules, JVM-tested; {@link TjAccessibilityService} remembers the app and starts it.
 */
public final class ResumeTarget {
    public static final String KEY = "resume_qs_app";   // switch in Launcher control, default ON
    /** An app remembered longer ago than this is not brought back (its task is probably gone; the stock home is the honest answer). */
    public static final long MAX_AGE_MS = 6L * 60 * 60 * 1000;

    private ResumeTarget() {}

    /** The stock launcher's package (home AND its Recents screen): in front = there is no app to come back to. */
    public static boolean isStockHome(String pkg, String stockHomePkg) { return pkg != null && pkg.equals(stockHomePkg); }

    /**
     * Should this window be remembered as "the Quickstep app Tj is in"? Only while Quickstep is active and the tablet is unlocked; never our own launcher / Solitaire stand-in windows (those belong to the custom
     * account), the stock launcher, the system UI, "android" (system dialogs) or an empty name. (Keyboards are filtered by the caller.)
     */
    public static boolean remember(String pkg, String ownPkg, String stockHomePkg, boolean customMode, boolean keyguardLocked) {
        if (pkg == null || pkg.isEmpty() || customMode || keyguardLocked) return false;
        if (pkg.equals(ownPkg) || pkg.equals(stockHomePkg)) return false;
        return !"android".equals(pkg) && !"com.android.systemui".equals(pkg);
    }

    /** What the gesture starts: the remembered app (switch on, remembered recently), or null = the stock home. */
    public static String pick(boolean enabled, String remembered, long rememberedAtMs, long nowMs) {
        if (!enabled || remembered == null || remembered.isEmpty()) return null;
        if (rememberedAtMs <= 0 || nowMs < rememberedAtMs || nowMs - rememberedAtMs > MAX_AGE_MS) return null;
        return remembered;
    }
}
