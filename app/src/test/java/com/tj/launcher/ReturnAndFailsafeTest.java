package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ReturnAndFailsafeTest {
    // ---- KeySeq
    @Test public void volumeCodeMatchesInsideTheWindow() {
        KeySeq k = new KeySeq(ReturnMethods.SEQ_WINDOW_MS, ReturnMethods.VOLCODE);
        assertFalse(k.onKey(KeySeq.VOL_DOWN, 1000)); assertFalse(k.onKey(KeySeq.VOL_DOWN, 1500)); assertFalse(k.onKey(KeySeq.VOL_UP, 2000));
        assertTrue(k.onKey(KeySeq.VOL_UP, 2500));
    }
    @Test public void tooSlowOrInterruptedDoesNotMatch() {
        KeySeq k = new KeySeq(ReturnMethods.SEQ_WINDOW_MS, ReturnMethods.VOLCODE);
        k.onKey(KeySeq.VOL_DOWN, 1000); k.onKey(KeySeq.VOL_DOWN, 4000); k.onKey(KeySeq.VOL_UP, 6000);
        assertFalse(k.onKey(KeySeq.VOL_UP, 8000));                         // 7 s from the first
        k.reset();
        k.onKey(KeySeq.VOL_DOWN, 100); k.onKey(KeySeq.VOL_DOWN, 200); k.onKey(KeySeq.BACK, 300); k.onKey(KeySeq.VOL_UP, 400);
        assertFalse(k.onKey(KeySeq.VOL_UP, 500));                          // a Back in the middle broke it
    }
    @Test public void aNoisyStartStillMatchesBecauseTheWindowSlides() {
        KeySeq k = new KeySeq(ReturnMethods.SEQ_WINDOW_MS, ReturnMethods.VOLCODE);
        k.onKey(KeySeq.VOL_UP, 1000); k.onKey(KeySeq.VOL_DOWN, 1100); k.onKey(KeySeq.VOL_DOWN, 1200); k.onKey(KeySeq.VOL_UP, 1300);
        assertTrue(k.onKey(KeySeq.VOL_UP, 1400));
    }
    @Test public void threeBacksThenVolumeDown() {
        KeySeq k = new KeySeq(ReturnMethods.SEQ_WINDOW_MS, ReturnMethods.BACKS);
        k.onKey(KeySeq.BACK, 1000); k.onKey(KeySeq.BACK, 1200); k.onKey(KeySeq.BACK, 1400);
        assertTrue(k.onKey(KeySeq.VOL_DOWN, 1600));
    }
    @Test public void konamiNeedsTheWholeShapeAndClearsAfterwards() {
        int[] konami = FailsafeKeys.KONAMI;
        KeySeq k = new KeySeq(FailsafeKeys.WINDOW_MS, konami);
        boolean hit = false;
        for (int i = 0; i < konami.length; i++) hit = k.onKey(konami[i], 1000 + i * 300);
        assertTrue(hit);
        assertFalse(k.onKey(KeySeq.VOL_UP, 9000));                        // cleared: one more key is not a second match
        assertEquals(8, konami.length);
    }
    @Test public void konamiMissingOneKeyDoesNothing() {
        int[] konami = FailsafeKeys.KONAMI;
        KeySeq k = new KeySeq(FailsafeKeys.WINDOW_MS, konami);
        boolean hit = false;
        for (int i = 0; i < konami.length; i++) if (i != 4) hit |= k.onKey(konami[i], 1000 + i * 300);
        assertFalse(hit);
    }

    // ---- both volume keys, flurry
    @Test public void bothVolumeKeysAtTheSameMoment() {
        assertTrue(ReturnMethods.bothVolume(1000, 1100, 1100));
        assertFalse(ReturnMethods.bothVolume(1000, 1600, 1600));          // 600 ms apart
        assertFalse(ReturnMethods.bothVolume(0, 1100, 1100));             // only one pressed
    }
    @Test public void flurryNeedsFourChangesEndingOn() {
        ReturnMethods.Flurry f = new ReturnMethods.Flurry();
        assertFalse(f.onChange(false, 1000)); assertFalse(f.onChange(true, 1500)); assertFalse(f.onChange(false, 2000));
        assertTrue(f.onChange(true, 2500));
        assertFalse(f.onChange(false, 9000));
    }

    // ---- hidden hotspot
    @Test public void fiveTapsThenAHoldIsTheCode() {
        TapCode t = new TapCode();
        long now = 1000;
        for (int i = 0; i < TapCode.TAPS; i++) { assertFalse(t.onDown(now)); t.onUp(now + 100); now += 400; }
        assertTrue(t.onDown(now));                                          // the sixth touch must be held
    }
    @Test public void fourTapsAreNotEnough() {
        TapCode t = new TapCode();
        long now = 1000;
        for (int i = 0; i < 4; i++) { t.onDown(now); t.onUp(now + 100); now += 400; }
        assertFalse(t.onDown(now));
    }
    @Test public void aSlowGapStartsOver() {
        TapCode t = new TapCode();
        long now = 1000;
        for (int i = 0; i < 3; i++) { t.onDown(now); t.onUp(now + 100); now += 400; }
        now += 2000;
        for (int i = 0; i < 2; i++) { t.onDown(now); t.onUp(now + 100); now += 400; }
        assertFalse(t.onDown(now));                                         // only 2 taps counted since the pause
    }
    @Test public void aLongPressBeforeTheEndResetsIt() {
        TapCode t = new TapCode();
        long now = 1000;
        for (int i = 0; i < TapCode.TAPS; i++) { t.onDown(now); t.onUp(now + 100); now += 400; }
        t.onDown(now); t.onUp(now + 500);                                   // held, but let go before the timer: the caller never fired
        assertEquals(0, t.taps());
    }

    @Test public void everyReturnMethodHasAUniqueIdAndKey() {
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (ReturnMethods.Method m : ReturnMethods.Method.values()) { assertTrue(ids.add(m.id)); assertTrue(m.prefKey().startsWith("return_m_")); }
        assertEquals(9, ids.size());
    }
}
