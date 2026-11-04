package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.Set;
import org.junit.Test;

public class UnlockMethodsTest {
    private static final UnlockPick.Key N = UnlockPick.Key.DIGIT, D = UnlockPick.Key.DELETE;

    private static UnlockMethods only(final UnlockMethods.Method... ms) {
        final Set<UnlockMethods.Method> on = EnumSet.noneOf(UnlockMethods.Method.class);
        for (UnlockMethods.Method m : ms) on.add(m);
        return new UnlockMethods(new UnlockMethods.Enabled() { @Override public boolean on(UnlockMethods.Method m) { return on.contains(m); } });
    }
    private static void keys(UnlockMethods u, long t, UnlockPick.Key... ks) { for (UnlockPick.Key k : ks) u.onKey(k, t += 300); }

    // ---- ERASE2 (the original)
    @Test public void erase2TwoDeletesBeforeAnyDigit() {
        UnlockMethods u = only(UnlockMethods.Method.ERASE2);
        u.onPad(true, 0); keys(u, 0, D, D, N, N, N, N); u.onEnter(3000);
        assertEquals(UnlockMethods.Method.ERASE2, u.consume(4000));
    }
    @Test public void erase2PlainPinIsNotIt() {
        UnlockMethods u = only(UnlockMethods.Method.ERASE2);
        u.onPad(true, 0); keys(u, 0, N, N, N, N); u.onEnter(3000);
        assertNull(u.consume(4000));
    }
    @Test public void erase2TypoCorrectionNeverCounts() {
        UnlockMethods u = only(UnlockMethods.Method.ERASE2);
        u.onPad(true, 0); keys(u, 0, N, D, D, D, N); u.onEnter(3000);
        assertNull(u.consume(4000));
    }
    @Test public void erase2StaleLatchExpires() {
        UnlockMethods u = only(UnlockMethods.Method.ERASE2);
        u.onPad(true, 0); keys(u, 0, D, D, N); u.onEnter(1000);
        assertNull(u.consume(1000 + UnlockMethods.VALID_PAD_MS + 1));
    }
    @Test public void aWrongPinWithTheTokenThenAPlainRetryIsPlain() {
        UnlockMethods u = only(UnlockMethods.Method.ERASE2);
        u.onPad(true, 0); keys(u, 0, D, D, N); u.onEnter(1000);     // wrong PIN, pad stays up
        keys(u, 1000, N, N); u.onEnter(3000);                        // plain retry
        assertNull(u.consume(3500));
    }
    @Test public void disabledMethodDoesNothing() {
        UnlockMethods u = only(UnlockMethods.Method.VOLUME);
        u.onPad(true, 0); keys(u, 0, D, D, N); u.onEnter(1000);
        assertNull(u.consume(1500));
    }

    // ---- TAP_ERASE
    @Test public void tapEraseThreeTimes() {
        UnlockMethods u = only(UnlockMethods.Method.TAP_ERASE);
        u.onPad(true, 0); keys(u, 0, N, D, N, D, N, D, N, N, N, N); u.onEnter(5000);
        assertEquals(UnlockMethods.Method.TAP_ERASE, u.consume(5500));
    }
    @Test public void tapEraseTwiceIsNotEnough() {
        UnlockMethods u = only(UnlockMethods.Method.TAP_ERASE);
        u.onPad(true, 0); keys(u, 0, N, D, N, D, N, N, N); u.onEnter(5000);
        assertNull(u.consume(5500));
    }
    @Test public void tapEraseNeedsTheEmptyPadFirst() {
        UnlockMethods u = only(UnlockMethods.Method.TAP_ERASE);
        u.onPad(true, 0); keys(u, 0, N, N, D, N, D, N, D); u.onEnter(5000);
        assertNull(u.consume(5500));
    }

    // ---- VOLUME
    @Test public void volumeUpUpDownDownWithinSixSeconds() {
        UnlockMethods u = only(UnlockMethods.Method.VOLUME);
        u.onVolume(true, 1000); u.onVolume(true, 1500); u.onVolume(false, 2000); u.onVolume(false, 2500);
        assertEquals(UnlockMethods.Method.VOLUME, u.armedMethod());
        assertEquals(UnlockMethods.Method.VOLUME, u.consume(10_000));
    }
    @Test public void volumeTooSlowOrWrongOrderDoesNothing() {
        UnlockMethods u = only(UnlockMethods.Method.VOLUME);
        u.onVolume(true, 0); u.onVolume(true, 3000); u.onVolume(false, 6000); u.onVolume(false, 6500);   // 6.5 s > window
        assertNull(u.armedMethod());
        u.onVolume(false, 20_000); u.onVolume(false, 20_100); u.onVolume(true, 20_200); u.onVolume(true, 20_300);   // wrong order
        assertNull(u.armedMethod());
    }
    @Test public void volumeCodeStillWorksAfterANoisyStart() {
        UnlockMethods u = only(UnlockMethods.Method.VOLUME);
        u.onVolume(false, 100); u.onVolume(true, 200); u.onVolume(true, 300); u.onVolume(false, 400); u.onVolume(false, 500);
        assertEquals(UnlockMethods.Method.VOLUME, u.armedMethod());
    }
    @Test public void aFreeLatchSurvivesAWrongPlainPinButNotTheScreenGoingOff() {
        UnlockMethods u = only(UnlockMethods.Method.VOLUME);
        u.onVolume(true, 1000); u.onVolume(true, 1100); u.onVolume(false, 1200); u.onVolume(false, 1300);
        u.onPad(true, 1400); keys(u, 1400, N, N); u.onEnter(3000);      // wrong plain PIN
        assertEquals(UnlockMethods.Method.VOLUME, u.armedMethod());
        u.onScreen(false, 4000);
        assertNull(u.armedMethod());
    }

    // ---- SIDEKEY
    @Test public void fourSideKeyChangesInFourSecondsEndingOn() {
        UnlockMethods u = only(UnlockMethods.Method.SIDEKEY);
        u.onScreen(false, 0); u.onScreen(true, 600); u.onScreen(false, 1200);
        assertNull(u.armedMethod());
        u.onScreen(true, 1800);
        assertEquals(UnlockMethods.Method.SIDEKEY, u.armedMethod());
    }
    @Test public void slowSideKeyPressesDoNotCount() {
        UnlockMethods u = only(UnlockMethods.Method.SIDEKEY);
        u.onScreen(false, 0); u.onScreen(true, 2000); u.onScreen(false, 4000); u.onScreen(true, 6000);
        assertNull(u.armedMethod());
    }
    @Test public void aSingleNormalLockUnlockIsNeverTheFlurry() {
        UnlockMethods u = only(UnlockMethods.Method.SIDEKEY);
        u.onScreen(false, 0); u.onScreen(true, 30_000);
        assertNull(u.armedMethod());
    }

    // ---- PEEK
    @Test public void peekThreeTimesInFifteenSeconds() {
        UnlockMethods u = only(UnlockMethods.Method.PEEK);
        u.onPad(true, 0); u.onPad(false, 1500); u.onPad(true, 3000); u.onPad(false, 4500);
        assertNull(u.armedMethod());
        u.onPad(true, 6000);
        assertEquals(UnlockMethods.Method.PEEK, u.armedMethod());
    }
    @Test public void slowPeeksDoNotCount() {
        UnlockMethods u = only(UnlockMethods.Method.PEEK);
        u.onPad(true, 0); u.onPad(false, 1000); u.onPad(true, 9000); u.onPad(false, 10_000); u.onPad(true, 17_000);
        assertNull(u.armedMethod());
    }

    // ---- VOLUP_ERASE
    @Test public void volumeUpThenDoubleErase() {
        UnlockMethods u = only(UnlockMethods.Method.VOLUP_ERASE);
        u.onVolume(true, 1000);
        u.onPad(true, 2000); keys(u, 2000, D, D, N, N); u.onEnter(6000);
        assertEquals(UnlockMethods.Method.VOLUP_ERASE, u.consume(6500));
    }
    @Test public void doubleEraseWithoutTheVolumeStepIsNotIt() {
        UnlockMethods u = only(UnlockMethods.Method.VOLUP_ERASE);
        u.onPad(true, 2000); keys(u, 2000, D, D, N, N); u.onEnter(6000);
        assertNull(u.consume(6500));
    }
    @Test public void volumeStepTooLongBeforeTheErasesIsNotIt() {
        UnlockMethods u = only(UnlockMethods.Method.VOLUP_ERASE);
        u.onVolume(true, 1000);
        u.onPad(true, 20_000); keys(u, 20_000, D, D, N, N); u.onEnter(25_000);
        assertNull(u.consume(25_500));
    }

    // ---- common
    @Test public void aLatchIsConsumedOnce() {
        UnlockMethods u = only(UnlockMethods.Method.ERASE2);
        u.onPad(true, 0); keys(u, 0, D, D, N); u.onEnter(1000);
        assertTrue(u.consume(1500) != null); assertNull(u.consume(1600));
    }
    @Test public void severalMethodsCanBeOnAtOnce() {
        UnlockMethods u = only(UnlockMethods.Method.ERASE2, UnlockMethods.Method.TAP_ERASE, UnlockMethods.Method.VOLUME);
        u.onPad(true, 0); keys(u, 0, N, D, N, D, N, D, N); u.onEnter(4000);
        assertEquals(UnlockMethods.Method.TAP_ERASE, u.consume(4500));
        u.onVolume(true, 10_000); u.onVolume(true, 10_100); u.onVolume(false, 10_200); u.onVolume(false, 10_300);
        assertEquals(UnlockMethods.Method.VOLUME, u.consume(11_000));
    }
    @Test public void listenerHearsArmAndWithdraw() {
        final int[] armed = {0, 0};
        UnlockMethods u = only(UnlockMethods.Method.ERASE2);
        u.setListener(new UnlockMethods.Listener() {
            @Override public void onArmed(UnlockMethods.Method m, boolean pad) { armed[0]++; }
            @Override public void onDisarmed() { armed[1]++; }
        });
        u.onPad(true, 0); keys(u, 0, D, D, N); u.onEnter(1000);
        assertEquals(1, armed[0]);
        keys(u, 1000, N); u.onEnter(3000);                       // plain retry withdraws it
        assertEquals(1, armed[1]);
    }
    @Test public void everyMethodHasAUniqueIdAndKey() {
        Set<String> ids = new java.util.HashSet<>();
        for (UnlockMethods.Method m : UnlockMethods.Method.values()) {
            assertTrue(ids.add(m.id)); assertTrue(m.prefKey().startsWith("unlock_m_")); assertFalse(m.how.isEmpty());
        }
        assertEquals(UnlockMethods.Method.values().length, ids.size());
        assertEquals(16, ids.size());
    }

    // ---- O1: the ten extra methods
    private static final int UP = KeySeq.VOL_UP, DOWN = KeySeq.VOL_DOWN, BACK = KeySeq.BACK;
    @Test public void bothVolumeKeysTogether() {
        UnlockMethods u = only(UnlockMethods.Method.VOLBOTH);
        u.onHwKey(UP, 0, 5000); u.onHwKey(DOWN, 0, 5100);
        assertEquals(UnlockMethods.Method.VOLBOTH, u.armedMethod());
    }
    @Test public void volumeKeysApartAreNotBoth() {
        UnlockMethods u = only(UnlockMethods.Method.VOLBOTH);
        u.onHwKey(UP, 0, 5000); u.onHwKey(DOWN, 0, 6000);
        assertNull(u.armedMethod());
    }
    @Test public void pingPongSixAlternatingKeys() {
        UnlockMethods u = only(UnlockMethods.Method.PINGPONG);
        int[] k = {UP, DOWN, UP, DOWN, UP, DOWN};
        for (int i = 0; i < k.length; i++) u.onHwKey(k[i], 0, 1000 + i * 500);
        assertEquals(UnlockMethods.Method.PINGPONG, u.armedMethod());
    }
    @Test public void pingPongTooSlowIsNothing() {
        UnlockMethods u = only(UnlockMethods.Method.PINGPONG);
        int[] k = {UP, DOWN, UP, DOWN, UP, DOWN};
        for (int i = 0; i < k.length; i++) u.onHwKey(k[i], 0, 1000 + i * 1500);
        assertNull(u.armedMethod());
    }
    @Test public void holdVolumeDownThenTapUp() {
        UnlockMethods u = only(UnlockMethods.Method.VOLHOLD);
        u.onHwKey(DOWN, 0, 1000);
        for (int r = 1; r <= UnlockMethods.HOLD_REPEATS + 2; r++) u.onHwKey(DOWN, r, 1000 + 500 + r * 50);
        u.onHwKey(UP, 0, 4500);
        assertEquals(UnlockMethods.Method.VOLHOLD, u.armedMethod());
    }
    @Test public void aShortHoldOrALateTapIsNothing() {
        UnlockMethods u = only(UnlockMethods.Method.VOLHOLD);
        for (int r = 1; r <= 10; r++) u.onHwKey(DOWN, r, 1000 + r * 50);
        u.onHwKey(UP, 0, 2000);
        assertNull(u.armedMethod());
        for (int r = 1; r <= UnlockMethods.HOLD_REPEATS + 2; r++) u.onHwKey(DOWN, r, 3000 + r * 50);
        u.onHwKey(UP, 0, 3000 + 60 * 50 + UnlockMethods.HOLD_TAP_MS + 1000);
        assertNull(u.armedMethod());
    }
    @Test public void threeBacksThenVolumeUp() {
        UnlockMethods u = only(UnlockMethods.Method.BACKS_UP);
        u.onHwKey(BACK, 0, 1000); u.onHwKey(BACK, 0, 1300); u.onHwKey(BACK, 0, 1600); u.onHwKey(UP, 0, 1900);
        assertEquals(UnlockMethods.Method.BACKS_UP, u.armedMethod());
    }
    @Test public void eraseLadderDeleteDigitThrice() {
        UnlockMethods u = only(UnlockMethods.Method.LADDER);
        u.onPad(true, 0); keys(u, 0, D, N, D, N, D, N, N, N, N); u.onEnter(5000);
        assertEquals(UnlockMethods.Method.LADDER, u.consume(5500));
    }
    @Test public void ladderIsNotTheTapEraseOrder() {
        UnlockMethods u = only(UnlockMethods.Method.LADDER);
        u.onPad(true, 0); keys(u, 0, N, D, N, D, N, D, N); u.onEnter(5000);
        assertNull(u.consume(5500));
    }
    @Test public void peekFiveTimesInTwentyFiveSeconds() {
        UnlockMethods u = only(UnlockMethods.Method.PEEK5);
        for (int i = 0; i < 4; i++) { u.onPad(true, i * 4000); u.onPad(false, i * 4000 + 1000); }
        assertNull(u.armedMethod());
        u.onPad(true, 17_000);
        assertEquals(UnlockMethods.Method.PEEK5, u.armedMethod());
    }
    @Test public void threePeeksDoNotMakeAPeekFive() {
        UnlockMethods u = only(UnlockMethods.Method.PEEK5);
        for (int i = 0; i < 3; i++) { u.onPad(true, i * 2000); u.onPad(false, i * 2000 + 500); }
        assertNull(u.armedMethod());
    }
    @Test public void chargerPlugUnplugPlug() {
        UnlockMethods u = only(UnlockMethods.Method.CHARGER);
        u.onPower(true, 1000); u.onPower(false, 3000); u.onPower(true, 5000);
        assertEquals(UnlockMethods.Method.CHARGER, u.armedMethod());
    }
    @Test public void chargerTooSlowOrWrongOrderIsNothing() {
        UnlockMethods u = only(UnlockMethods.Method.CHARGER);
        u.onPower(true, 1000); u.onPower(false, 6000); u.onPower(true, 12_000);
        assertNull(u.armedMethod());
        UnlockMethods v = only(UnlockMethods.Method.CHARGER);
        v.onPower(false, 1000); v.onPower(true, 2000); v.onPower(false, 3000);
        assertNull(v.armedMethod());
    }
    @Test public void fourRotationsInEightSeconds() {
        UnlockMethods u = only(UnlockMethods.Method.ROTATE);
        u.onRotation(1000); u.onRotation(2500); u.onRotation(4000); assertNull(u.armedMethod());
        u.onRotation(5500);
        assertEquals(UnlockMethods.Method.ROTATE, u.armedMethod());
    }
    @Test public void slowRotationsAreNothing() {
        UnlockMethods u = only(UnlockMethods.Method.ROTATE);
        for (int i = 0; i < 6; i++) u.onRotation(1000 + i * 4000);
        assertNull(u.armedMethod());
    }
    @Test public void motionMethodsOnlyWhenSelected() {
        UnlockMethods u = only(UnlockMethods.Method.SHAKE);
        u.onMotion(UnlockMethods.Method.KNOCK, 1000);
        assertNull(u.armedMethod());
        u.onMotion(UnlockMethods.Method.SHAKE, 2000);
        assertEquals(UnlockMethods.Method.SHAKE, u.armedMethod());
    }
    @Test public void atLeastOneMethodIsAlwaysSelected() {
        UnlockMethods.Enabled none = new UnlockMethods.Enabled() { @Override public boolean on(UnlockMethods.Method m) { return false; } };
        assertEquals(java.util.Arrays.asList(UnlockMethods.Method.ERASE2), UnlockMethods.selected(none));
        UnlockMethods.Enabled two = new UnlockMethods.Enabled() { @Override public boolean on(UnlockMethods.Method m) { return m == UnlockMethods.Method.PEEK || m == UnlockMethods.Method.VOLUME; } };
        assertEquals(2, UnlockMethods.selected(two).size());
        assertTrue(UnlockMethods.needsKeys(two));
        assertFalse(UnlockMethods.needsMotion(two));
        assertFalse(UnlockMethods.needsKeys(none));
    }
}
