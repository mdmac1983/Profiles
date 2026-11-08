package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * BF1: the hold state machine ({@link AnimationHolds}) over in-memory stores. These are the cases that, wrong, leave Tj's tablet with animations off on the lock screen or in the custom launcher, or overwrite a value
 * he set himself: each rule of the class comment is one test.
 */
public class AnimationHoldsTest {
    private static final String[] N = { "w", "t", "a" };
    private static final int QS = AnimationPolicy.HOLD_QUICKSTEP, RESET = AnimationPolicy.HOLD_RESET;

    private static final class FakeState implements AnimationHolds.State {
        int holds; boolean pending; final Map<String, String> orig = new HashMap<>(); int saves; boolean failNextSave;
        @Override public int holds() { return holds; }
        @Override public boolean pending() { return pending; }
        @Override public boolean hasOriginal(String s) { return orig.containsKey(s); }
        @Override public String original(String s) { return orig.containsKey(s) ? orig.get(s) : "1.0"; }
        @Override public void save(int h, boolean p, Map<String, String> o) {
            if (failNextSave) { failNextSave = false; throw new IllegalStateException("disk full"); }
            holds = h; pending = p; if (o != null) orig.putAll(o); saves++;
        }
    }

    private static final class FakeScales implements AnimationHolds.Scales {
        final Map<String, String> v = new HashMap<>(); final List<String> writes = new ArrayList<>(); int failOnWrite = -1; int count = 0;
        FakeScales(String w, String t, String a) { v.put("w", w); v.put("t", t); v.put("a", a); }
        @Override public String get(String s) { return v.get(s); }
        @Override public void put(String s, String x) {
            if (failOnWrite == count++) throw new SecurityException("permission revoked");
            v.put(s, x); writes.add(s + "=" + x);
        }
        String all() { return v.get("w") + " " + v.get("t") + " " + v.get("a"); }
    }

    private final List<String> logs = new ArrayList<>();
    private final AnimationHolds.Sink log = new AnimationHolds.Sink() { @Override public void log(String m) { logs.add(m); } };

    private int set(FakeState st, FakeScales sc, int bit, boolean on) { return AnimationHolds.set(st, sc, N, true, bit, on, log); }

    @Test public void theFirstReasonSavesTheOriginalsThenWritesZeros() {
        FakeState st = new FakeState(); FakeScales sc = new FakeScales("1.0", "1.0", "1.0");
        assertEquals(QS, set(st, sc, QS, true));
        assertEquals(QS, st.holds); assertTrue(st.pending);
        assertEquals("1.0", st.orig.get("w")); assertEquals("1.0", st.orig.get("t")); assertEquals("1.0", st.orig.get("a"));
        assertEquals("0 0 0", sc.all());
    }

    @Test public void theLastReasonEndingPutsTheOriginalsBackExactly() {
        FakeState st = new FakeState(); FakeScales sc = new FakeScales("0.5", "0.5", "2");
        set(st, sc, QS, true);
        assertEquals("0 0 0", sc.all());
        assertEquals(0, set(st, sc, QS, false));
        assertEquals("not a fixed 1.0: exactly what was there", "0.5 0.5 2", sc.all());
        assertEquals(0, st.holds); assertFalse(st.pending);
        assertTrue(logs.contains("animations restored"));
    }

    @Test public void aReasonThatJoinsRewritesNothingAndTheFirstToEndLeavesTheZeros() {
        FakeState st = new FakeState(); FakeScales sc = new FakeScales("1.0", "1.0", "1.0");
        set(st, sc, QS, true);
        sc.v.put("w", "1.5");   // Tj changes one scale in Developer options during the session
        int writes = sc.writes.size();
        assertEquals(QS | RESET, set(st, sc, RESET, true));
        assertEquals("joining writes nothing: his 1.5 stays", writes, sc.writes.size());
        assertEquals("1.5 0 0", sc.all());
        assertEquals("the reset ending must not give the animations back inside the Quickstep session", QS, set(st, sc, RESET, false));
        assertEquals("1.5 0 0", sc.all());
        set(st, sc, QS, false);
        assertEquals("his value stays, the others return to their originals", "1.5 1.0 1.0", sc.all());
    }

    @Test public void theQuickstepReasonEndingFirstKeepsTheResetsZeros() {
        FakeState st = new FakeState(); FakeScales sc = new FakeScales("1.0", "1.0", "1.0");
        set(st, sc, RESET, true); set(st, sc, QS, true);
        assertEquals(RESET, set(st, sc, QS, false));
        assertEquals("0 0 0", sc.all());
        set(st, sc, RESET, false);
        assertEquals("1.0 1.0 1.0", sc.all());
    }

    @Test public void aScaleThatIsAlreadyOffIsNeverWrittenOrRestored() {
        FakeState st = new FakeState(); FakeScales sc = new FakeScales("0", "1.0", "1.0");   // Tj's own permanent "window animation off"
        set(st, sc, QS, true);
        assertEquals("only the two that were on are written", 2, sc.writes.size());
        sc.writes.clear();
        set(st, sc, QS, false);
        assertEquals("0 1.0 1.0", sc.all());
        assertFalse("the one that was off before stays off and is not rewritten", sc.writes.contains("w=1.0"));
    }

    @Test public void anUnsetScaleIsRememberedAsOneAndPutBackAsOne() {
        FakeState st = new FakeState(); FakeScales sc = new FakeScales("1.0", "1.0", null);
        set(st, sc, QS, true);
        assertEquals("1.0", st.orig.get("a")); assertEquals("0", sc.v.get("a"));
        set(st, sc, QS, false);
        assertEquals("1.0", sc.v.get("a"));
    }

    @Test public void withoutThePermissionAnAcquireDoesNothing() {
        FakeState st = new FakeState(); FakeScales sc = new FakeScales("1.0", "1.0", "1.0");
        assertEquals(0, AnimationHolds.set(st, sc, N, false, QS, true, log));
        assertEquals(0, st.saves); assertTrue(sc.writes.isEmpty()); assertEquals("1.0 1.0 1.0", sc.all());
    }

    @Test public void withoutThePermissionAReleaseKeepsPendingSoTheNextStartWithItFinishesTheJob() {
        FakeState st = new FakeState(); FakeScales sc = new FakeScales("1.0", "1.0", "1.0");
        set(st, sc, QS, true);
        assertEquals(0, AnimationHolds.set(st, sc, N, false, QS, false, log));
        assertEquals(0, st.holds); assertTrue("still pending", st.pending);
        assertEquals("nothing could be written", "0 0 0", sc.all());
        assertEquals(0, set(st, sc, RESET, false));   // the next start, permission back (any release finishes it)
        assertEquals("1.0 1.0 1.0", sc.all()); assertFalse(st.pending);
    }

    @Test public void staleOriginalsAreNotReusedForAScaleTjHasChangedSince() {
        FakeState st = new FakeState();
        st.pending = true; st.holds = 0; st.orig.put("w", "1.0"); st.orig.put("t", "1.0"); st.orig.put("a", "1.0");   // an old restore never finished
        FakeScales sc = new FakeScales("0", "0.5", "0");   // w and a are still OUR zeros, t is Tj's own new value
        set(st, sc, QS, true);
        assertEquals("the scale he changed is captured as it is NOW", "0.5", st.orig.get("t"));
        assertEquals("a scale still at our zero keeps the original saved then", "1.0", st.orig.get("w"));
        assertEquals("1.0", st.orig.get("a"));
        set(st, sc, QS, false);
        assertEquals("his 0.5 comes back, not the stale 1.0; ours return to 1.0", "1.0 0.5 1.0", sc.all());
    }

    @Test public void aMaskWithoutPendingIsJustClearedOnRelease() {
        FakeState st = new FakeState(); st.holds = QS; st.pending = false;
        FakeScales sc = new FakeScales("1.0", "1.0", "1.0");
        assertEquals(0, set(st, sc, QS, false));
        assertEquals(0, st.holds); assertTrue(sc.writes.isEmpty());
    }

    @Test public void aMaskWithoutPendingIsRecapturedAndZeroedOnTheNextAcquire() {
        FakeState st = new FakeState(); st.holds = QS; st.pending = false;
        FakeScales sc = new FakeScales("1.0", "1.0", "1.0");
        assertEquals(QS | RESET, set(st, sc, RESET, true));
        assertEquals("0 0 0", sc.all()); assertTrue(st.pending);
    }

    @Test public void aWriteThatFailsHalfWayIsUndoneAndTheMaskStaysAsItWas() {
        FakeState st = new FakeState(); FakeScales sc = new FakeScales("1.0", "1.0", "1.0");
        sc.failOnWrite = 1;   // the first zero is written, the second throws
        assertEquals("the mask is unchanged", 0, set(st, sc, QS, true));
        assertEquals("what was already zeroed is put back", "1.0 1.0 1.0", sc.all());
        assertEquals(0, st.holds); assertFalse("no phantom hold", st.pending);
        boolean said = false; for (String l : logs) if (l.startsWith("could not turn animations off")) said = true;
        assertTrue("never silent", said);
    }

    @Test public void aFailedSaveMeansNoChangeAtAll() {
        FakeState st = new FakeState(); FakeScales sc = new FakeScales("1.0", "1.0", "1.0");
        st.failNextSave = true;   // the originals could not be put on disk: nothing may be changed without them
        assertEquals(0, set(st, sc, QS, true));
        assertTrue(sc.writes.isEmpty()); assertEquals("1.0 1.0 1.0", sc.all());
        assertEquals(0, st.holds); assertFalse(st.pending);
    }

    @Test public void aFailureWhilePuttingTheAnimationsBackClearsNothingSoTheNextTriggerTriesAgain() {
        FakeState st = new FakeState(); FakeScales sc = new FakeScales("1.0", "1.0", "1.0");
        set(st, sc, QS, true);
        sc.failOnWrite = sc.count;   // the very next write throws
        try { set(st, sc, QS, false); fail("the failure must reach the caller"); } catch (SecurityException expected) { /* AnimationSpeedup catches it and reports the mask unchanged */ }
        assertEquals("still held in the stored state", QS, st.holds); assertTrue(st.pending);
        sc.failOnWrite = -1;
        assertEquals(0, set(st, sc, QS, false));   // the next trigger finishes it
        assertEquals("1.0 1.0 1.0", sc.all()); assertFalse(st.pending);
    }

    @Test public void endingAReasonTwiceOrWithNothingHeldWritesNothing() {
        FakeState st = new FakeState(); FakeScales sc = new FakeScales("1.0", "1.0", "1.0");
        assertEquals(0, set(st, sc, QS, false));
        assertEquals(0, st.saves); assertTrue(sc.writes.isEmpty());
        set(st, sc, QS, true); set(st, sc, QS, false);
        int saves = st.saves, writes = sc.writes.size();
        assertEquals(0, set(st, sc, QS, false));
        assertEquals(saves, st.saves); assertEquals(writes, sc.writes.size());
    }

    @Test public void takingTheSameReasonTwiceChangesNothing() {
        FakeState st = new FakeState(); FakeScales sc = new FakeScales("1.0", "1.0", "1.0");
        set(st, sc, QS, true);
        int saves = st.saves, writes = sc.writes.size();
        assertEquals(QS, set(st, sc, QS, true));
        assertEquals(saves, st.saves); assertEquals(writes, sc.writes.size());
    }
}
