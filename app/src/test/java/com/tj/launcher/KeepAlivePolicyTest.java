package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** AA2: the keep-alive rules. The service itself needs a device; the emulator section "ka" proves it is a foreground service and what it does to the process. */
public class KeepAlivePolicyTest {
    @Test public void runsWhenTheSwitchIsOnAndNothingCrashed() { assertTrue(KeepAlivePolicy.shouldRun(true, false)); }
    @Test public void neverWhenTheSwitchIsOff() { assertFalse(KeepAlivePolicy.shouldRun(false, false)); }
    @Test public void neverWhileTheCrashBreakerIsTripped() { assertFalse(KeepAlivePolicy.shouldRun(true, true)); assertFalse(KeepAlivePolicy.shouldRun(false, true)); }
    @Test public void aStickyRestartFollowsTheSameRule() {
        assertTrue(KeepAlivePolicy.honourRestart(true, false));
        assertFalse(KeepAlivePolicy.honourRestart(false, false));
        assertFalse(KeepAlivePolicy.honourRestart(true, true));
    }
    @Test public void theNotificationIsAsQuietAsAndroidAllows() {
        // android.app.NotificationManager.IMPORTANCE_MIN == 1 (no sound, no status-bar icon), android.app.Notification.VISIBILITY_SECRET == -1 (hidden on the lock screen)
        assertEquals(1, KeepAlivePolicy.IMPORTANCE_MIN);
        assertEquals(-1, KeepAlivePolicy.VISIBILITY_SECRET);
    }
    @Test public void ourOwnStatusBarIconIsRecognisedByItsTitleAndNothingElseIs() {
        assertTrue(KeepAlivePolicy.isOurIcon("Launcher notification: Launcher is kept ready"));
        assertTrue(KeepAlivePolicy.isOurIcon("Launcher is kept ready"));
        assertFalse(KeepAlivePolicy.isOurIcon("Gmail notification: 3 new messages"));
        assertFalse(KeepAlivePolicy.isOurIcon("Launcher notification: something else"));
        assertFalse(KeepAlivePolicy.isOurIcon(null)); assertFalse(KeepAlivePolicy.isOurIcon(""));
    }
    @Test public void theSwitchKeyIsStable() { assertEquals("keep_alive", KeepAlivePolicy.KEY); }

    // ---- KA1 (v0.7.2): the first real tablet session found that this service crash-looped the launcher on its first open
    @Test public void keepAliveIsOffByDefaultUntilItIsProvenOnTheRealTablet() { assertFalse(KeepAlivePolicy.DEFAULT_ON); }
    @Test public void aBlownFuseStopsItWhateverTheSwitchSays() {
        assertFalse(KeepAlivePolicy.shouldRun(true, false, true));
        assertFalse(KeepAlivePolicy.honourRestart(true, false, true));
        assertTrue(KeepAlivePolicy.shouldRun(true, false, false));
    }
    @Test public void theFuseKeysAreStable() {
        assertEquals("keep_alive_blocked", KeepAlivePolicy.BLOCKED_KEY);
        assertEquals("keep_alive_blocked_why", KeepAlivePolicy.BLOCKED_WHY_KEY);
    }
    @Test public void theRealTabletsCrashIsRecognised() {
        // verbatim from the tablet's crash buffer (logcat -b crash, 2026-11-05 12:48:47)
        String m = "Bad notification(tag=null, id=7471) posted from package com.tj.launcher, crashing app(uid=10243, pid=9473): Couldn't create icon: StatusBarIcon(icon=Icon(typ=RESOURCE pkg=com.tj.launcher id=0x7f07009a) visible user=0 )";
        assertTrue(KeepAlivePolicy.isBadNotificationCrash("android.app.RemoteServiceException", m));
    }
    @Test public void theOtherAndroidWordingsForTheSameFamilyAreRecognisedToo() {
        assertTrue(KeepAlivePolicy.isBadNotificationCrash("android.app.RemoteServiceException", "Bad notification for startForeground: java.lang.RuntimeException: invalid channel for service notification"));
        assertTrue(KeepAlivePolicy.isBadNotificationCrash("android.app.RemoteServiceException", "Context.startForegroundService() did not then call Service.startForeground(): ServiceRecord{1 u0 com.tj.launcher/.KeepAliveService}"));
    }
    @Test public void otherCrashesAreNeverTheFuses() {
        assertFalse(KeepAlivePolicy.isBadNotificationCrash("java.lang.NullPointerException", "Bad notification in a made-up NPE"));   // not the system's crash class
        assertFalse(KeepAlivePolicy.isBadNotificationCrash("android.app.RemoteServiceException", "Some other remote crash"));
        assertFalse(KeepAlivePolicy.isBadNotificationCrash("android.app.RemoteServiceException", null));
        assertFalse(KeepAlivePolicy.isBadNotificationCrash((Throwable) null));
        assertFalse(KeepAlivePolicy.isBadNotificationCrash(new RuntimeException("Bad notification(tag=null, id=7471)")));   // right words, wrong class
        assertFalse(KeepAlivePolicy.isBadNotificationCrash(new IllegalStateException("x", new RuntimeException("y"))));
    }
    @Test public void theFuseTakesOnlyTheFirstCrashSoARealLoopStillReachesTheBreaker() {
        Throwable other = new RuntimeException("boom");
        assertFalse(KeepAlivePolicy.fuseHandles(other, false));
        assertFalse(KeepAlivePolicy.fuseHandles(other, true));
        // the same crash again while the fuse is already blown is NOT the fuse's any more (that test needs the system's class, which cannot be built here: pinned through the string form above)
        assertFalse(KeepAlivePolicy.fuseHandles(null, false));
    }
}
