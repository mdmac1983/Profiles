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

    /**
     * Review RV2 (2026-11-06): the remembered time is stamped only when the foreground package CHANGES, so with one app in front all day the 6 h limit would run from when it first came forward and the resume
     * would silently stop. At the moment the screen goes off the app that is still in front is, by definition, the one Tj had open: refresh its time. Anything else keeps the old time.
     */
    public static long refreshedAt(String remembered, String foreground, long rememberedAtMs, long nowMs) {
        if (remembered == null || remembered.isEmpty() || !remembered.equals(foreground)) return rememberedAtMs;
        return nowMs;
    }

    /** Plain-words reason why {@link #pick} answered null (for the lock log and the tablet report: "why did the gesture open the stock home?"). Mirrors pick()'s conditions in the same order. */
    public static String whyNot(boolean enabled, String remembered, long rememberedAtMs, long nowMs) {
        if (!enabled) return "the switch is off";
        if (remembered == null || remembered.isEmpty()) return "no Quickstep app is remembered";
        if (rememberedAtMs <= 0 || nowMs < rememberedAtMs) return "the remembered time is not valid";
        if (nowMs - rememberedAtMs > MAX_AGE_MS) return "the remembered app is older than 6 hours";
        return "an app is remembered";
    }

    /** What the gesture starts: the remembered app (switch on, remembered recently), or null = the stock home. */
    public static String pick(boolean enabled, String remembered, long rememberedAtMs, long nowMs) {
        if (!enabled || remembered == null || remembered.isEmpty()) return null;
        if (rememberedAtMs <= 0 || nowMs < rememberedAtMs || nowMs - rememberedAtMs > MAX_AGE_MS) return null;
        return remembered;
    }
}
