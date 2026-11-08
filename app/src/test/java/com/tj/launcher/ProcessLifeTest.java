package com.tj.launcher;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** BG1 ("Can it be kept in ram?"): the record of how often Android restarts the launcher process and at what priority it sits while locked. */
public class ProcessLifeTest {
    private static final long MIN = 60_000L, H = 3600_000L;

    @Test public void theRingKeepsTheNewestEntriesOldestFirst() {
        String r = "";
        for (long i = 1; i <= 30; i++) r = ProcessLife.add(r, i, 24);
        long[] v = ProcessLife.parse(r);
        assertEquals(24, v.length);
        assertEquals(7, v[0]); assertEquals(30, v[23]);
    }

    @Test public void aFreshRingHoldsOneEntryAndGarbageIsSkipped() {
        assertArrayEquals(new long[] { 5 }, ProcessLife.parse(ProcessLife.add(null, 5, 24)));
        assertArrayEquals(new long[] { 1, 3 }, ProcessLife.parse("1,x,,3"));
        assertEquals(0, ProcessLife.parse("").length);
        assertEquals(0, ProcessLife.parse(null).length);
        assertArrayEquals(new long[] { 2, 9 }, ProcessLife.parse(ProcessLife.add("junk,2", 9, 24)));
    }

    @Test public void startsAreCountedInAWindowAndAFutureTimeNeverCounts() {
        long now = 100 * H;
        long[] s = { now - 30 * H, now - 5 * H, now - 50 * MIN, now - 2 * MIN, now + H };
        assertEquals(3, ProcessLife.countSince(s, now, 24 * H));
        assertEquals(2, ProcessLife.countSince(s, now, H));
    }

    @Test public void agesReadInPlainUnits() {
        assertEquals("12 s", ProcessLife.age(12_000));
        assertEquals("3 min", ProcessLife.age(3 * MIN + 5000));
        assertEquals("2 h 5 min", ProcessLife.age(2 * H + 5 * MIN));
        assertEquals("1 d 3 h", ProcessLife.age(27 * H));
        assertEquals("?", ProcessLife.age(-1));
    }

    @Test public void oomScoresAreExplainedForTheKillOrder() {
        assertTrue(ProcessLife.oomWords(-800).contains("never killed"));
        assertTrue(ProcessLife.oomWords(0).contains("killed last"));
        assertTrue(ProcessLife.oomWords(50).contains("killed last"));
        assertTrue(ProcessLife.oomWords(250).contains("late"));
        assertTrue(ProcessLife.oomWords(700).contains("memory is short"));
        assertTrue(ProcessLife.oomWords(900).contains("first to be killed"));
    }

    @Test public void theBlockSaysHowOftenTheProcessStartedAndWhatPriorityItHadAtTheLastLock() {
        long now = 1000 * H;
        long[] starts = { now - 20 * H, now - 3 * H, now - 10 * MIN };
        String[] locks = { (now - 3 * H) + ":700:400", (now - 30 * MIN) + ":50:125" };
        String d = ProcessLife.describe(starts, locks, now, now - 10 * MIN, true);
        assertTrue(d, d.contains("this process is 10 min old"));
        assertTrue(d, d.contains("in the last 24 h: 3"));
        assertTrue(d, d.contains("in the last hour: 1"));
        assertTrue(d, d.contains("keep-alive service switch ON"));
        assertTrue(d, d.contains("3 h 0 min ago 700 / 400"));
        assertTrue(d, d.contains("30 min ago 50 / 125"));
        assertTrue("the last sample decides the verdict", d.contains("-> last: foreground / visible: killed last"));
    }

    @Test public void withNothingRecordedYetTheBlockSaysSo() {
        String d = ProcessLife.describe(new long[0], new String[0], 5 * H, 4 * H, false);
        assertTrue(d, d.contains("process starts recorded: 0"));
        assertTrue(d, d.contains("no lock sampled yet"));
        assertTrue(d, d.contains("keep-alive service switch off"));
    }

    @Test public void aBrokenLockSampleIsSkippedNotFatal() {
        String d = ProcessLife.describe(new long[0], new String[] { "oops", "1:2" }, H, 0, false);
        assertTrue(d, d.contains("priority while locked"));
    }
}
