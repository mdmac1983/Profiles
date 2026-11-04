package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class UnlockPickTest {
    private static void tap(UnlockPick.Token t, UnlockPick.Key... keys) { for (UnlockPick.Key k : keys) t.onKey(k); }

    @Test public void classifiesPinPadKeysByIdOnly() {
        assertEquals(UnlockPick.Key.DIGIT, UnlockPick.classify("com.android.systemui:id/key7"));
        assertEquals(UnlockPick.Key.DIGIT, UnlockPick.classify("com.android.systemui:id/key0"));
        assertEquals(UnlockPick.Key.DELETE, UnlockPick.classify("com.android.systemui:id/delete_button"));
        assertEquals(UnlockPick.Key.ENTER, UnlockPick.classify("com.android.systemui:id/key_enter"));
        assertNull(UnlockPick.classify("com.android.systemui:id/pinEntry"));
        assertNull(UnlockPick.classify("com.android.systemui:id/key12"));
        assertNull(UnlockPick.classify(null));
    }
    @Test public void twoDeletesBeforeAnyDigitIsTheToken() {
        UnlockPick.Token t = new UnlockPick.Token();
        tap(t, UnlockPick.Key.DELETE, UnlockPick.Key.DELETE, UnlockPick.Key.DIGIT, UnlockPick.Key.DIGIT, UnlockPick.Key.DIGIT, UnlockPick.Key.DIGIT);
        t.onEnter(1000);
        assertTrue(t.consume(2000));
    }
    @Test public void aPlainPinIsNotTheToken() {
        UnlockPick.Token t = new UnlockPick.Token();
        tap(t, UnlockPick.Key.DIGIT, UnlockPick.Key.DIGIT, UnlockPick.Key.DIGIT, UnlockPick.Key.DIGIT);
        t.onEnter(1000);
        assertFalse(t.consume(2000));
    }
    @Test public void correctingATypoNeverTriggers() {
        UnlockPick.Token t = new UnlockPick.Token();
        tap(t, UnlockPick.Key.DIGIT, UnlockPick.Key.DELETE, UnlockPick.Key.DELETE, UnlockPick.Key.DELETE, UnlockPick.Key.DIGIT);
        t.onEnter(1000);
        assertFalse(t.consume(2000));
    }
    @Test public void oneDeleteIsNotEnough() {
        UnlockPick.Token t = new UnlockPick.Token();
        tap(t, UnlockPick.Key.DELETE, UnlockPick.Key.DIGIT);
        t.onEnter(1000);
        assertFalse(t.consume(2000));
    }
    @Test public void aStaleTokenExpires() {
        UnlockPick.Token t = new UnlockPick.Token();
        tap(t, UnlockPick.Key.DELETE, UnlockPick.Key.DELETE, UnlockPick.Key.DIGIT);
        t.onEnter(1000);
        assertFalse(t.consume(1000 + UnlockPick.VALID_MS + 1));
    }
    @Test public void tokenIsConsumedOnce() {
        UnlockPick.Token t = new UnlockPick.Token();
        tap(t, UnlockPick.Key.DELETE, UnlockPick.Key.DELETE, UnlockPick.Key.DIGIT);
        t.onEnter(1000);
        assertTrue(t.consume(1500)); assertFalse(t.consume(1600));
    }
    @Test public void aWrongPinThenAPlainOneIsPlain() {
        UnlockPick.Token t = new UnlockPick.Token();
        tap(t, UnlockPick.Key.DELETE, UnlockPick.Key.DELETE, UnlockPick.Key.DIGIT);
        t.onEnter(1000);                                  // wrong PIN with the token: pad stays locked
        tap(t, UnlockPick.Key.DIGIT, UnlockPick.Key.DIGIT);
        t.onEnter(3000);                                  // plain second attempt unlocks
        assertFalse(t.consume(3500));
    }
}
