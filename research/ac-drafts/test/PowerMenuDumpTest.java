package com.tj.launcher;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** AC6: the stock-menu geometry helpers. The tree walk needs Android; the rules are pure. */
public class PowerMenuDumpTest {
    @Test public void aNodeLineHasClassIdLabelBoundsAndClickable() {
        String l = PowerMenuDump.nodeLine(2, "android.widget.TextView", "com.android.systemui:id/message", "Power off", null, 485, 357, 596, 434, true);
        assertEquals("    TextView #message 'Power off' [485,357][596,434] click\n", l);
    }
    @Test public void theDescriptionIsUsedWhenThereIsNoText() {
        assertTrue(PowerMenuDump.nodeLine(0, "a.B", null, null, "Restart", 0, 0, 1, 1, false).contains("'Restart'"));
    }
    @Test public void longLabelsAreCutAndNewlinesFlattened() {
        String l = PowerMenuDump.nodeLine(0, "a.B", null, "x\ny" + new String(new char[100]).replace('\0', 'z'), null, 0, 0, 1, 1, false);
        assertEquals(1, l.split("\n").length);
        assertTrue(l.length() < 100);
    }
    @Test public void theItemPitchIsTheDistanceBetweenNeighbouringEntries() {
        assertEquals(101, PowerMenuDump.pitch(357, 559, 3));
        assertEquals(-1, PowerMenuDump.pitch(357, 357, 3));
        assertEquals(-1, PowerMenuDump.pitch(357, 559, 1));
    }
    @Test public void ourCardIsTheStockPanelMinusItsLastEntry() {
        // the emulator's real panel [485,339][596,642] with a measured pitch of 101: the card ends one pitch above the bottom
        assertArrayEquals(new int[] { 485, 339, 596, 541 }, PowerMenuDump.cardFromPanel(485, 339, 596, 642, 101));
    }
    @Test public void withoutAPitchTheCardIsTwoThirdsOfThePanel() {
        assertArrayEquals(new int[] { 0, 0, 100, 200 }, PowerMenuDump.cardFromPanel(0, 0, 100, 300, -1));
        assertArrayEquals(new int[] { 0, 0, 100, 200 }, PowerMenuDump.cardFromPanel(0, 0, 100, 300, 0));
        assertArrayEquals(new int[] { 0, 0, 100, 200 }, PowerMenuDump.cardFromPanel(0, 0, 100, 300, 999));   // a pitch bigger than the panel cannot be right
    }
}
