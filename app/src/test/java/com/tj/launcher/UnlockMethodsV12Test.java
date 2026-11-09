package com.tj.launcher;

import static org.junit.Assert.*;

import com.tj.launcher.UnlockMethods.Method;
import com.tj.launcher.UnlockPick.Key;

import org.junit.Test;

/** BT1b (v1.2): the eight new pad / key handshakes. */
public class UnlockMethodsV12Test {
    private static UnlockMethods only(final Method... on) {
        return new UnlockMethods(new UnlockMethods.Enabled() { @Override public boolean on(Method m) { for (Method x : on) if (x == m) return true; return false; } });
    }
    private static void digits(UnlockMethods u, long start, long... gaps) { long t = start; u.onKey(Key.DIGIT, t); for (long g : gaps) { t += g; u.onKey(Key.DIGIT, t); } }

    @Test public void pinRhythmNeedsTheLongPauseAfterTheSecondDigitOnly() {
        UnlockMethods u = only(Method.RHYTHM);
        u.onPad(true, 0); digits(u, 1000, 300, 2500, 300);
        assertEquals(Method.RHYTHM, u.onEnter(6000));
        u.onPad(true, 10_000); digits(u, 11_000, 300, 300, 300);
        assertNull("no pause = a plain PIN", u.onEnter(13_000));
        u.onPad(true, 20_000); digits(u, 21_000, 300, 2500, 2500);
        assertNull("a second long pause is not the rhythm", u.onEnter(28_000));
    }

    @Test public void longPressDeleteCountsOnlyOnAnEmptyPad() {
        UnlockMethods u = only(Method.LONGDEL);
        u.onPad(true, 0); u.onLongDelete(500); digits(u, 1500, 200, 200, 200);
        assertEquals(Method.LONGDEL, u.onEnter(3000));
        u.onPad(true, 10_000); digits(u, 10_500, 200); u.onLongDelete(11_000); digits(u, 12_000, 200, 200, 200);
        assertNull("a long-press that clears typed digits is a correction, not a handshake", u.onEnter(13_000));
    }

    @Test public void slowStartWaitsEightSecondsBeforeTheFirstDigit() {
        UnlockMethods u = only(Method.SLOWSTART);
        u.onPad(true, 0); digits(u, 8_500, 200, 200, 200);
        assertEquals(Method.SLOWSTART, u.onEnter(9_500));
        u.onPad(true, 20_000); digits(u, 23_000, 200, 200, 200);
        assertNull("3 s of hesitation is not the handshake", u.onEnter(24_000));
    }

    @Test public void deleteRightBeforeEnter() {
        UnlockMethods u = only(Method.DELENTER);
        u.onPad(true, 0); digits(u, 500, 200, 200, 200, 200); u.onKey(Key.DELETE, 1700);
        assertEquals(Method.DELENTER, u.onEnter(2000));
        u.onPad(true, 5000); digits(u, 5500, 200, 200); u.onKey(Key.DELETE, 6000); digits(u, 6200, 200);
        assertNull("a typo fixed in the middle ends with a digit: plain PIN", u.onEnter(7000));
    }

    @Test public void holdVolumeUpWhileTappingEnter() {
        UnlockMethods u = only(Method.VOLUP_ENTER);
        u.onPad(true, 0); digits(u, 500, 200, 200, 200);
        u.onHwKey(KeySeq.VOL_UP, 0, 1500); u.onHwKey(KeySeq.VOL_UP, 5, 2000);
        assertEquals(Method.VOLUP_ENTER, u.onEnter(2300));
        u.onPad(true, 10_000); digits(u, 10_500, 200, 200, 200); u.onHwKey(KeySeq.VOL_UP, 0, 11_000);
        assertNull("a short tap of Volume Up is not holding it", u.onEnter(11_500));
    }

    @Test public void volumeUpRightAfterWaking() {
        UnlockMethods u = only(Method.VOLUP_WAKE);
        u.onScreen(false, 0); u.onScreen(true, 5_000);
        for (int r = 1; r <= 12; r++) u.onHwKey(KeySeq.VOL_UP, r, 5_500 + r * 50);
        assertEquals(Method.VOLUP_WAKE, u.armedMethod());
        UnlockMethods v = only(Method.VOLUP_WAKE);
        v.onScreen(false, 0); v.onScreen(true, 5_000);
        for (int r = 1; r <= 12; r++) v.onHwKey(KeySeq.VOL_UP, r, 12_000 + r * 50);
        assertNull("long after waking it does not count", v.armedMethod());
    }

    @Test public void slowWakeCount() {
        UnlockMethods u = only(Method.WAKE3);
        long t = 0;
        for (int i = 0; i < 3; i++) { u.onScreen(true, t); t += 2_000; if (i < 2) { u.onScreen(false, t); t += 2_000; } }
        assertEquals(Method.WAKE3, u.armedMethod());
    }

    @Test public void volumeDownThreeTimesThenTheSideKey() {
        UnlockMethods u = only(Method.VOLDOWN3_SIDE);
        u.onScreen(true, 0);
        u.onHwKey(KeySeq.VOL_DOWN, 0, 1000); u.onHwKey(KeySeq.VOL_DOWN, 0, 1400); u.onHwKey(KeySeq.VOL_DOWN, 0, 1800);
        u.onScreen(false, 3000); u.onScreen(true, 4000);
        assertEquals(Method.VOLDOWN3_SIDE, u.armedMethod());
        UnlockMethods v = only(Method.VOLDOWN3_SIDE);
        v.onScreen(true, 0); v.onHwKey(KeySeq.VOL_DOWN, 0, 1000); v.onScreen(false, 3000); v.onScreen(true, 4000);
        assertNull("one Volume Down is not three", v.armedMethod());
    }

    @Test public void theNewMethodsAreAllOffByDefaultAndKeyMethodsAskForKeys() {
        for (Method m : new Method[] { Method.RHYTHM, Method.LONGDEL, Method.SLOWSTART, Method.DELENTER, Method.VOLUP_ENTER, Method.VOLUP_WAKE, Method.WAKE3, Method.VOLDOWN3_SIDE })
            assertFalse(m.id + " starts off", m.defaultOn);
        assertTrue(UnlockMethods.needsKeys(new UnlockMethods.Enabled() { @Override public boolean on(Method m) { return m == Method.VOLUP_ENTER; } }));
        assertEquals("slow start waits 8 s (user, 2026-11-09)", 8_000, UnlockMethods.SLOWSTART_MS);
    }
}
