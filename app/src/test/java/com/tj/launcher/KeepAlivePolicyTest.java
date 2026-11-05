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
    @Test public void theSwitchKeyIsStable() { assertEquals("keep_alive", KeepAlivePolicy.KEY); }
}
