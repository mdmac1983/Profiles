package com.tj.launcher;

import android.content.Context;
import android.view.accessibility.AccessibilityNodeInfo;
import java.io.File;
import java.io.FileOutputStream;

/**
 * AC6 (Tj, 2026-11-05: "the lock screen power menu overlay doesn't look right at all ... the buttons and card are too small. How can I get Claude to make it exact?").
 * The code cannot know the real menu's layout on the tablet's MtkSystemUI. The accessibility helper SEES it every time the menu opens, so it writes what it sees (the menu window's node tree: class, view id,
 * label, bounds, clickable; plus the numbers it used and the display facts) into a small file. {@link ReportState} adds the file to the tablet report, so one report after one power-menu press carries the
 * exact geometry, no cable. Nothing private is in it: labels of the menu entries ("Power off", "Restart", "Screenshot", "Emergency"...), pixel rectangles and display numbers.
 */
public final class PowerMenuDump {
    static final String FILE = "power-menu-dump.txt";
    static final int MAX_NODES = 80, MAX_DEPTH = 9, MAX_CHARS = 12 * 1024;
    private static final Object LOCK = new Object();
    private static long lastWriteLockedMs = 0, lastWriteOpenMs = 0;

    private PowerMenuDump() {}

    /** Pure: one node as a line: depth-indented "Class #id 'text' [l,t][r,b] click". Labels cut to 40 characters, newlines flattened. */
    public static String nodeLine(int depth, String cls, String viewId, CharSequence text, CharSequence desc, int l, int t, int r, int b, boolean clickable) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < depth && i < MAX_DEPTH; i++) s.append("  ");
        String c = cls == null ? "?" : cls.substring(cls.lastIndexOf('.') + 1);
        s.append(c);
        if (viewId != null && !viewId.isEmpty()) s.append(" #").append(viewId.substring(viewId.lastIndexOf('/') + 1));
        String label = flat(text != null && text.length() > 0 ? text : desc);
        if (!label.isEmpty()) s.append(" '").append(label).append('\'');
        s.append(" [").append(l).append(',').append(t).append("][").append(r).append(',').append(b).append(']');
        if (clickable) s.append(" click");
        return s.append('\n').toString();
    }

    private static String flat(CharSequence cs) {
        if (cs == null) return "";
        String s = cs.toString().replace('\r', ' ').replace('\n', ' ').trim();
        return s.length() > 40 ? s.substring(0, 40) : s;
    }

    /** Pure: item pitch (distance between the tops of two neighbouring entries) from the tops of the first and the last of {@code n} equally spaced entries; -1 when it cannot be told. */
    public static int pitch(int firstTop, int lastTop, int n) { return n >= 2 && lastTop > firstTop ? (lastTop - firstTop) / (n - 1) : -1; }

    /**
     * Pure: the card of OUR menu from the stock panel = everything ABOVE the Screenshot entry. {@code cut} is the top of the Screenshot entry (-1 = unknown): when it lies inside the panel (between 40 % and 87.5 % of its
     * height) the card ends there, which also stays right if the panel has more entries below Screenshot. Otherwise the panel minus one entry {@code pitch} (a pitch outside 20-40 % of the panel is not believed),
     * otherwise two thirds of the panel.
     */
    public static int[] cardFromPanel(int left, int top, int right, int bottom, int pitch, int cut) {
        int h = bottom - top;
        int cardBottom;
        if (cut > top + h * 2 / 5 && cut < bottom - h / 8) cardBottom = cut;
        else cardBottom = bottom - (pitch >= h / 5 && pitch <= h * 2 / 5 ? pitch : h / 3);
        return new int[] { left, top, right, cardBottom };
    }

    /** Walks {@code root} (the power menu's window) breadth-limited and returns the text block; never throws. */
    public static String walk(AccessibilityNodeInfo root) {
        StringBuilder out = new StringBuilder();
        try { walk(root, 0, out, new int[] { 0 }); } catch (Throwable t) { out.append("(walk failed: ").append(t).append(")\n"); }
        return out.toString();
    }

    private static void walk(AccessibilityNodeInfo n, int depth, StringBuilder out, int[] count) {
        if (n == null || count[0] >= MAX_NODES || depth > MAX_DEPTH) return;
        count[0]++;
        android.graphics.Rect r = new android.graphics.Rect();
        n.getBoundsInScreen(r);
        out.append(nodeLine(depth, String.valueOf(n.getClassName()), n.getViewIdResourceName(), n.getText(), n.getContentDescription(), r.left, r.top, r.right, r.bottom, n.isClickable()));
        for (int i = 0; i < n.getChildCount() && count[0] < MAX_NODES; i++) {
            AccessibilityNodeInfo ch = n.getChild(i);
            if (ch != null) { try { walk(ch, depth + 1, out, count); } finally { ch.recycle(); } }
        }
    }

    /** True when a dump of this lock state is due (the newest per state wins, at most one per 20 s), so the tree is only walked when it will be written. */
    public static boolean due(boolean locked) {
        long now = android.os.SystemClock.elapsedRealtime();
        synchronized (LOCK) { long last = locked ? lastWriteLockedMs : lastWriteOpenMs; return last == 0 || now - last >= 20_000; }
    }

    /** Writes the dump (header + tree). The file holds the LAST dump of each state, lock screen first. Safe from any thread. */
    public static void write(Context c, boolean locked, String header, String tree) {
        try {
            long now = android.os.SystemClock.elapsedRealtime();
            synchronized (LOCK) {
                if (locked) lastWriteLockedMs = now; else lastWriteOpenMs = now;
                String mine = "=== " + (locked ? "LOCK SCREEN" : "UNLOCKED") + "\n" + header + tree;
                if (mine.length() > MAX_CHARS / 2) mine = mine.substring(0, MAX_CHARS / 2);
                String other = read(c);
                String keepOther = "";
                int a = other.indexOf("=== " + (locked ? "UNLOCKED" : "LOCK SCREEN"));
                if (a >= 0) { int b = other.indexOf("=== ", a + 4); keepOther = b < 0 ? other.substring(a) : other.substring(a, b); }
                String all = locked ? mine + (keepOther.isEmpty() ? "" : "\n" + keepOther) : (keepOther.isEmpty() ? "" : keepOther + "\n") + mine;
                FileOutputStream o = new FileOutputStream(c.getApplicationContext().getFileStreamPath(FILE), false);
                try { o.write(all.getBytes("UTF-8")); } finally { o.close(); }
            }
        } catch (Throwable ignored) { }
    }

    /** The stored dump, or "" (for the report). */
    public static String read(Context c) {
        try {
            File f = c.getApplicationContext().getFileStreamPath(FILE);
            if (!f.exists()) return "";
            java.io.FileInputStream in = new java.io.FileInputStream(f);
            try { java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream(); byte[] b = new byte[4096]; int n; while ((n = in.read(b)) > 0) bo.write(b, 0, n); return bo.toString("UTF-8"); }
            finally { in.close(); }
        } catch (Throwable t) { return ""; }
    }
}
