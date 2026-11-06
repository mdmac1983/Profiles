package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/** PG3: the short summary promises to stay under 12 KB; the log at its end gets what is left of the budget, newest lines first. */
public class TabletReportTest {
    @Test public void everythingThatFitsIsKept() {
        List<String> lines = Arrays.asList("aaa", "bbb", "ccc");          // 4 characters each with its newline
        assertEquals(lines, TabletReport.newestWithin(lines, 12));
        assertEquals(lines, TabletReport.newestWithin(lines, 1000));
    }

    @Test public void theOldestLinesGoFirst() {
        List<String> lines = Arrays.asList("aaa", "bbb", "ccc");
        assertEquals(Arrays.asList("bbb", "ccc"), TabletReport.newestWithin(lines, 11));
        assertEquals(Arrays.asList("bbb", "ccc"), TabletReport.newestWithin(lines, 8));
        assertEquals(Arrays.asList("ccc"), TabletReport.newestWithin(lines, 7));
        assertEquals(Arrays.asList("ccc"), TabletReport.newestWithin(lines, 4));
    }

    @Test public void nothingFitsMeansNoLines() {
        List<String> lines = Arrays.asList("aaa", "bbb");
        assertTrue(TabletReport.newestWithin(lines, 3).isEmpty());
        assertTrue(TabletReport.newestWithin(lines, 0).isEmpty());
        assertTrue(TabletReport.newestWithin(lines, -50).isEmpty());   // the budget can already be spent by the other parts
        assertTrue(TabletReport.newestWithin(new ArrayList<String>(), 100).isEmpty());
    }

    @Test public void aLongLineAtTheEndIsNotSkippedForOlderOnes() {
        // newest first: the last line does not fit, so NOTHING older may be returned in its place (the log must stay contiguous)
        List<String> lines = Arrays.asList("a", "b", "0123456789");
        assertTrue(TabletReport.newestWithin(lines, 5).isEmpty());
    }

    @Test public void theBudgetIsBelowTheGateLimit() {
        assertTrue("the gate checks the summary is under 12000 bytes", TabletReport.SUMMARY_MAX < 12_000);
    }
}
