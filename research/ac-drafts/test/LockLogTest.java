package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** AC1/AC7: the persistent lock-screen decision log. The Android side only appends to a file; every rule worth testing is pure. */
public class LockLogTest {
    @Test public void aLineIsOneLineWithTimeEventAndDetail() {
        String l = LockLog.line(0, "UNLOCK", "plain, mode quickstep -> custom");
        assertTrue(l.endsWith("\n"));
        assertEquals(1, l.split("\n").length);
        assertTrue(l.contains("UNLOCK plain, mode quickstep -> custom"));
        assertTrue(l.matches("\\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d .*\n"));
    }
    @Test public void newlinesInDetailsCannotSplitALine() {
        String l = LockLog.line(0, "X", "a\nb\rc");
        assertEquals(1, l.split("\n").length);
    }
    @Test public void aLongDetailIsCut() { assertTrue(LockLog.line(0, "X", new String(new char[500]).replace('\0', 'a')).length() <= 201); }
    @Test public void aMissingEventAndDetailStillGiveALine() { assertTrue(LockLog.line(0, null, null).contains(" ?")); }

    @Test public void trimKeepsTheNewestWholeLines() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 100; i++) b.append("line").append(i).append("-xxxxxxxxxx\n");
        String t = LockLog.trim(b.toString(), 500, 300);
        assertTrue(t.length() <= 300);
        assertTrue(t.startsWith("line"));                 // starts at a line boundary
        assertTrue(t.endsWith("line99-xxxxxxxxxx\n"));    // the newest line survives
        assertFalse(t.contains("line0-"));                // the oldest is gone
    }
    @Test public void trimLeavesAShortLogAlone() { assertEquals("a\nb\n", LockLog.trim("a\nb\n", 500, 300)); assertEquals("", LockLog.trim(null, 500, 300)); }

    @Test public void tailGivesTheLastLinesInOrder() {
        String all = "1\n2\n3\n4\n5\n";
        assertEquals("4\n5\n", LockLog.tailLines(all, 2));
        assertEquals("1\n2\n3\n4\n5\n", LockLog.tailLines(all, 9));
        assertEquals("5\n", LockLog.tailLines(all, 1));
        assertEquals("", LockLog.tailLines("", 3));
        assertEquals("", LockLog.tailLines(all, 0));
        assertEquals("only\n", LockLog.tailLines("only", 3));
    }
    @Test public void theSizesAreSane() { assertTrue(LockLog.KEEP_BYTES < LockLog.MAX_BYTES); assertTrue(LockLog.MAX_BYTES <= 64 * 1024); }
}
