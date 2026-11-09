package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * BH1: the pure rules of {@link NotifSightings}: which notifications count, when a lock-log line is written, the ring buffer and the per-app summary the tablet report prints. Only package names are ever
 * stored (the test lines carry no title or text because the class has no way to take one).
 */
public class NotifSightingsTest {
    @Test public void theShadeStepFollowsTheIconsOrTheFlagButNeverWhenTheSwitchIsOff() {
        assertTrue("an icon", NotifSightings.shadeStepWanted(true, Boolean.TRUE, false));
        assertTrue("unknown (the bar is hidden): look", NotifSightings.shadeStepWanted(true, null, false));
        assertFalse("no icon, nothing posted: skip", NotifSightings.shadeStepWanted(true, Boolean.FALSE, false));
        assertTrue("BH1e: no icon but something was posted (a silent notification): open the shade", NotifSightings.shadeStepWanted(true, Boolean.FALSE, true));
        assertFalse("the Clear notifications switch is off", NotifSightings.shadeStepWanted(false, Boolean.TRUE, true));
        assertFalse(NotifSightings.shadeStepWanted(false, null, true));
    }

    @Test public void aNotificationOfAnOrdinaryAppCounts() {
        assertTrue(NotifSightings.counts("org.telegram.messenger", "app.orionmd.profiles"));
    }

    @Test public void ourOwnAndTheSystemsNotificationsDoNotCount() {
        assertFalse(NotifSightings.counts("app.orionmd.profiles", "app.orionmd.profiles"));
        assertFalse(NotifSightings.counts("android", "app.orionmd.profiles"));
        assertFalse(NotifSightings.counts("com.android.systemui", "app.orionmd.profiles"));
        assertFalse(NotifSightings.counts(null, "app.orionmd.profiles"));
        assertFalse(NotifSightings.counts("", "app.orionmd.profiles"));
    }

    @Test public void aLockLogLineIsWrittenForTheFirstOneAndThenOncePerMinute() {
        assertTrue(NotifSightings.shouldLog(0, 5_000));
        assertFalse(NotifSightings.shouldLog(10_000, 10_000 + NotifSightings.LOG_GAP_MS - 1));
        assertTrue(NotifSightings.shouldLog(10_000, 10_000 + NotifSightings.LOG_GAP_MS));
        assertTrue("a clock that went backwards must not silence the log for ever", NotifSightings.shouldLog(50_000, 40_000));
    }

    @Test public void theStateWordsSayWhichLauncherAndWhetherTheScreenWasDarkOrLocked() {
        assertEquals("custom", NotifSightings.stateWords(false, true, false));
        assertEquals("custom/lock", NotifSightings.stateWords(false, true, true));
        assertEquals("custom/dark", NotifSightings.stateWords(false, false, false));
        assertEquals("quickstep", NotifSightings.stateWords(true, true, false));
        assertEquals("quickstep/lock", NotifSightings.stateWords(true, true, true));
        assertEquals("a dark screen is dark whatever the keyguard says", "quickstep/dark", NotifSightings.stateWords(true, false, true));
    }

    @Test public void theRingBufferKeepsTheNewestLines() {
        String r = "";
        for (int i = 1; i <= 5; i++) r = NotifSightings.append(r, "line" + i, 3);
        assertEquals("line3\nline4\nline5\n", r);
    }

    @Test public void appendingToAnEmptyOrUnterminatedBufferWorks() {
        assertEquals("a\n", NotifSightings.append(null, "a", 5));
        assertEquals("a\n", NotifSightings.append("", "a", 5));
        assertEquals("x\na\n", NotifSightings.append("x", "a", 5));
    }

    @Test public void aBufferAtTheLimitDropsExactlyTheOldest() {
        String r = NotifSightings.append("one\ntwo\n", "three", 3);
        assertEquals("one\ntwo\nthree\n", r);
        assertEquals("two\nthree\nfour\n", NotifSightings.append(r, "four", 3));
    }

    @Test public void theSummaryGroupsByAppAndStateNewestFirst() {
        String r = ""
                + "11-08 02:00:01 org.a custom drawer\n"
                + "11-08 02:00:02 org.b quickstep/lock hidden\n"
                + "11-08 02:00:03 org.a custom drawer\n"
                + "11-08 02:00:04 org.b custom hidden\n";
        assertEquals("org.b x1 (custom hidden), org.a x2 (custom drawer), org.b x1 (quickstep/lock hidden)", NotifSightings.summarize(r, 8));
    }

    @Test public void theSummaryIsCappedAndSaysSo() {
        String r = "11-08 02:00:01 a custom hidden\n11-08 02:00:02 b custom hidden\n11-08 02:00:03 c custom hidden\n";
        assertEquals("c x1 (custom hidden), b x1 (custom hidden), ...", NotifSightings.summarize(r, 2));
    }

    @Test public void aLineThatDoesNotParseIsSkippedAndAnEmptyBufferIsSaid() {
        assertEquals("(none)", NotifSightings.summarize("", 8));
        assertEquals("(none)", NotifSightings.summarize(null, 8));
        assertEquals("(none)", NotifSightings.summarize("garbage\n", 8));
        assertEquals("a x1 (custom hidden)", NotifSightings.summarize("garbage\n11-08 02:00:01 a custom hidden\n", 8));
    }
}
