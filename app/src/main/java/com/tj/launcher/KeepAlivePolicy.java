package com.tj.launcher;

/**
 * AA2 (Tj, 2026-11-05: "Build number 3" = keep the launcher in RAM so it starts as fast as possible). The pure rules for {@link KeepAliveService}, kept free of Android classes so they are JVM-tested.
 *
 * KA1 (v0.7.2, from the first real tablet session, 2026-11-05): on the MediaTek tablet the keep-alive notification crash-looped the launcher on its very first open (three crashes in seven seconds: Android's
 * "Bad notification ... Couldn't create icon"), which tripped the crash breaker, wiped Tj's switches and left the accessibility helper unbound until a restart. So: (1) the small icon is now an ordinary PNG,
 * (2) the switch is OFF by default until the service has been proven on the tablet, (3) a FUSE: if Android ever crashes us again for a bad notification, only keep-alive is switched off (and stays off until Tj
 * turns it on himself) - that one crash is not counted toward the 3-crash breaker, so nothing else of Tj's setup is lost.
 */
public final class KeepAlivePolicy {
    public static final String KEY = "keep_alive";       // switch in Profiles settings, default OFF since v0.7.2 (KA1)
    /** KA1: the default of the switch. It stays false until keep-alive has run on the real tablet without a crash and Tj has decided. */
    public static final boolean DEFAULT_ON = false;
    /** KA1: set by the crash fuse (and by nothing else); cleared only when Tj switches keep-alive on again in Profiles settings. */
    public static final String BLOCKED_KEY = "keep_alive_blocked";
    /** KA1: when and why the fuse blew (shown in the report and in Profiles settings). */
    public static final String BLOCKED_WHY_KEY = "keep_alive_blocked_why";
    /** KA1, EMULATOR TEST ONLY (an int, default 0 = off, like patch_test_skew): 1 makes the service use an icon that does not exist, 2 no icon at all, so Android crashes the app the way it did on the tablet and the fuse can be proven end to end. */
    public static final String TEST_BAD_ICON_KEY = "keep_alive_test_bad_icon";

    private KeepAlivePolicy() {}

    /**
     * Should the keep-alive service be running right now?
     * Never while the crash-loop breaker is tripped: a launcher that keeps crashing must not be restarted by its own service (the breaker switched everything off on purpose).
     * Never while the KA1 fuse is blown: Android refused our notification once, so we do not post it again until Tj asks.
     */
    public static boolean shouldRun(boolean switchOn, boolean crashTripped, boolean fuseBlown) {
        return switchOn && !crashTripped && !fuseBlown;
    }

    /**
     * The quietest notification Android allows for a foreground service. We ask for IMPORTANCE_MIN (1) and the secret lock-screen visibility (-1); Android raises it to LOW (2) and ignores the
     * lock-screen request (apps cannot set it) - measured on the Android 10 emulator, run 79. The numbers are pinned here so a test can check them without Android.
     */
    public static final int IMPORTANCE_MIN = 1, VISIBILITY_SECRET = -1;

    /** The notification's title. The accessibility helper's status-bar look recognises our own icon by it (its content description is "<app> notification: <title>"). */
    public static final String TITLE = "Launcher is kept ready";

    /** Pure: is this status-bar icon our own keep-alive icon? (so it never counts as "a notification to clear") */
    public static boolean isOurIcon(CharSequence contentDescription) {
        return contentDescription != null && contentDescription.toString().contains(TITLE);
    }

    // ------------------------------------------------------------------ the KA1 fuse (pure part)
    /** The class Android uses to crash an app from outside ({@code ActivityThread.scheduleCrash}). */
    public static final String REMOTE_SERVICE_EXCEPTION = "android.app.RemoteServiceException";

    /**
     * Pure: is a crash with this exception class and message the system killing us over the keep-alive notification? Seen on the real tablet (Android 10, MediaTek SystemUI):
     * {@code RemoteServiceException: Bad notification(tag=null, id=7471) posted from package com.tj.launcher, crashing app(uid=10243, pid=9473): Couldn't create icon: StatusBarIcon(...)}.
     * The other Android wording for the same family is "Bad notification for startForeground: ..." (it also starts with "Bad notification") and, on older Android releases, the exact phrase
     * "did not then call Service.startForeground()". The keep-alive service is the ONLY thing in this app that posts a notification, so any of them is its; a bare "startForeground" in some other message is NOT matched.
     */
    public static boolean isBadNotificationCrash(String exceptionClass, String message) {
        if (!REMOTE_SERVICE_EXCEPTION.equals(exceptionClass) || message == null) return false;
        return message.contains("Bad notification") || message.contains("did not then call Service.startForeground()");
    }

    /** Pure: the same test over an exception and its causes (a few levels at most). */
    public static boolean isBadNotificationCrash(Throwable e) {
        int depth = 0;
        for (Throwable t = e; t != null && depth < 6; t = t.getCause(), depth++)
            if (isBadNotificationCrash(t.getClass().getName(), t.getMessage())) return true;
        return false;
    }

    /**
     * Pure: does the fuse take this crash (instead of the 3-crash breaker)? Only the FIRST such crash: if the fuse is already blown and the same crash comes again, something else is wrong and it must
     * count like any other crash, so a real loop still reaches the breaker.
     */
    public static boolean fuseHandles(Throwable e, boolean alreadyBlown) { return !alreadyBlown && isBadNotificationCrash(e); }
}
