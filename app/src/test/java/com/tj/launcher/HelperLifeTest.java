package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/** HLP1: the helper's flight recorder format and the summary the tablet report prints (how long it lived, how it ended, what the system log said just before). */
public class HelperLifeTest {
    private static final long T0 = 1_794_000_000_000L;

    private static List<String> run(int beats, String ending) {
        List<String> l = new ArrayList<>();
        l.add(HelperLife.line("START", T0, null, "pid", "4321", "uid", "2000", "file", "/sdcard/x/helper-life.txt"));
        for (int i = 1; i <= beats; i++) l.add(HelperLife.line("BEAT", T0 + i * HelperLife.BEAT_MS, null, "up", String.valueOf(i * 30), "rss", "41234", "oom", "0", "usb", "mtp,adb", "adbd", "running"));
        if (ending != null) l.add(ending);
        return l;
    }

    @Test public void aLineRoundTripsItsTimeAndValues() {
        String l = HelperLife.line("BEAT", T0, null, "up", "30", "usb", "mtp adb");
        assertEquals(T0, HelperLife.timeOf(l));
        assertEquals("30", HelperLife.value(l, "up"));
        assertEquals("mtp_adb", HelperLife.value(l, "usb"));   // a space would split the value
        assertEquals(null, HelperLife.value(l, "rss"));
        assertEquals(-1, HelperLife.timeOf("garbage"));
        assertEquals(-1, HelperLife.timeOf(null));
    }

    @Test public void anOverlongLineIsCutAndTheTextOfAnExceptionStaysOnOneLine() {
        String l = HelperLife.line("EXCEPTION", T0, "tj-shell-conn: java.lang.Boom: a\nmultiline\r\nmessage " + new String(new char[400]).replace('\0', 'x'));
        assertTrue(l.length() <= 260);
        assertFalse(l.contains("\n")); assertFalse(l.contains("\r"));
    }

    @Test public void aHelperThatKeepsBeatingIsStillAlive() {
        String s = HelperLife.summarize(run(4, null), T0 + 4 * HelperLife.BEAT_MS + 5000);
        assertTrue(s, s.contains("heartbeats: 4"));
        assertTrue(s, s.contains("still alive when the report was written"));
        assertTrue(s, s.contains("oom_score_adj 0"));
        assertTrue(s, s.contains("usb mtp,adb"));
    }

    @Test public void aHelperThatWentSilentWithoutAnEndMarkerWasKilledByASignalThatCannotBeCaught() {
        // the case of Tj's tablet (HLP1): alive for a few minutes, then nothing
        String s = HelperLife.summarize(run(10, null), T0 + 30 * 60_000L);
        assertTrue(s, s.contains("STOPPED without a trace after 5 min"));
        assertTrue(s, s.contains("no shutdown hook, no exception"));
    }

    @Test public void aShutdownHookOrAnExceptionIsNamedAsTheEnd() {
        String hook = HelperLife.line("SHUTDOWN", T0 + 100_000L, "the VM was asked to exit (SIGTERM, SIGHUP or SIGINT)", "up", "100");
        String s = HelperLife.summarize(run(3, hook), T0 + 10 * 60_000L);
        assertTrue(s, s.contains("ENDED: SHUTDOWN"));
        assertFalse(s, s.contains("STOPPED without a trace"));
        String boom = HelperLife.line("EXCEPTION", T0 + 100_000L, "tj-shell-unix: java.lang.OutOfMemoryError");
        String e = HelperLife.summarize(run(3, boom), T0 + 10 * 60_000L);
        assertTrue(e, e.contains("ENDED: EXCEPTION"));
        assertTrue(e, e.contains("OutOfMemoryError"));
    }

    @Test public void onlyTheNewestRunIsSummarisedAndTheRunsAreCounted() {
        List<String> two = run(2, null);
        two.addAll(Arrays.asList(HelperLife.line("START", T0 + 3_600_000L, null, "pid", "999"), HelperLife.line("BEAT", T0 + 3_630_000L, null, "up", "30", "rss", "1", "oom", "0", "usb", "x", "adbd", "y")));
        String s = HelperLife.summarize(two, T0 + 3_640_000L);
        assertTrue(s, s.contains("runs recorded: 2"));
        assertTrue(s, s.contains("heartbeats: 1"));
    }

    @Test public void theSystemLogLinesBeforeTheEndAreQuoted() {
        List<String> l = run(2, null);
        l.add(HelperLife.line("LOGCAT", T0 + 70_000L, "11-08 01:23:45.000   507   507 I lmkd    : Kill 'app_process' (4321), uid 2000, oom_adj 0"));
        String s = HelperLife.summarize(l, T0 + 20 * 60_000L);
        assertTrue(s, s.contains("lmkd"));
        assertTrue(s, s.contains("Kill 'app_process'"));
    }

    @Test public void anEmptyOrStartlessFileSaysSo() {
        assertTrue(HelperLife.summarize(null, T0).contains("no flight recorder file"));
        assertTrue(HelperLife.summarize(new ArrayList<String>(), T0).contains("no flight recorder file"));
        assertTrue(HelperLife.summarize(Arrays.asList("BEAT t=5 up=1"), T0).contains("no START line"));
    }

    @Test public void theFileIsTrimmedAtALineBoundaryOncePastItsLimit() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 3000; i++) b.append("BEAT t=").append(i).append(" up=").append(i).append('\n');
        String all = b.toString();
        String t = HelperLife.trim(all, 10_000, 4_000);
        assertTrue(t.length() <= 4_000);
        assertTrue("starts on a whole line", t.startsWith("BEAT t="));
        assertTrue(t.endsWith("up=2999\n"));
        assertEquals("under the limit nothing changes", "abc", HelperLife.trim("abc", 10, 5));
        assertEquals("", HelperLife.trim(null, 10, 5));
    }
}
