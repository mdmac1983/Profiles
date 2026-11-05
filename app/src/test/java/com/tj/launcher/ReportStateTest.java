package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/** Tj, 2026-11-05: the report goes to Claude and is KEPT in the project, so the launcher's state may only come out through an allow-list. */
public class ReportStateTest {
    @Test public void everySwitchIsReportedButOnlyAllowListedTextIs() {
        Map<String, Object> m = new HashMap<>();
        m.put("keep_alive", true); m.put("return_reset", false);
        m.put("unlock_pick_last", "10:00:01  custom launcher (plain unlock)");
        m.put("lock_number_text", "SECRET 4711"); m.put("lock_number", 12345); m.put("failsafe_konami_seq", "abcd"); m.put("target_pkg_for_solitaire", "com.example.game");
        String out = ReportState.format(m);
        assertTrue(out, out.contains("keep_alive: true"));
        assertTrue(out, out.contains("return_reset: false"));
        assertTrue(out, out.contains("unlock_pick_last: 10:00:01  custom launcher (plain unlock)"));
        assertFalse(out, out.contains("SECRET") || out.contains("4711") || out.contains("12345") || out.contains("abcd") || out.contains("com.example.game"));
        assertFalse(out, out.contains("lock_number"));
    }
    @Test public void valuesAreOneLineAndCut() {
        Map<String, Object> m = new HashMap<>();
        StringBuilder big = new StringBuilder("first\nsecond\r\n");
        for (int i = 0; i < 100; i++) big.append("0123456789");
        m.put("reset_last", big.toString());
        String out = ReportState.format(m);
        assertEquals("one line", 1, out.trim().split("\n").length);
        assertTrue(out, out.contains("first second"));
        assertTrue(out, out.trim().endsWith("..."));
        assertTrue(out.length() < 700);
    }
    @Test public void isSortedAndNullSafe() {
        Map<String, Object> m = new HashMap<>();
        m.put("b_switch", true); m.put("a_switch", true); m.put("c_null", null);
        assertEquals("a_switch: true\nb_switch: true\n", ReportState.format(m));
        assertEquals("", ReportState.format(null));
    }
    @Test public void noAllowListedKeyLooksLikeASecret() {
        for (String k : ReportState.SAFE_VALUES) {
            String l = k.toLowerCase(java.util.Locale.ROOT);
            assertFalse(k, l.contains("pin") || l.contains("tap_code") || l.contains("lock_code") || l.contains("unlock_code") || l.contains("seq") || l.contains("digit") || l.contains("secret") || l.contains("pass") || l.contains("number") || l.contains("konami") || l.contains("corner") || l.contains("hotspot"));   // compile_done_code is a version number, not a code
        }
        assertTrue(ReportState.SAFE_VALUES.contains("keytest_output"));
    }
}
