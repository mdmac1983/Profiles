package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * BH1: the hold state machine ({@link NotifQuietHold}) over in-memory stores. These are the cases that, wrong, leave Tj's tablet with the pop-up banners off in Quickstep or on in the custom launcher, or
 * overwrite a value Tj set by hand: each rule of the class comment is one test. {@code events} records the ORDER of disk and settings writes (the original must be on disk before the first change).
 */
public class NotifQuietHoldTest {
    private final List<String> events = new ArrayList<>();

    private final class FakeState implements NotifQuietHold.State {
        boolean held; String original; boolean failNextSave;
        @Override public boolean held() { return held; }
        @Override public String original() { return original; }
        @Override public void save(boolean h, String o) {
            if (failNextSave) { failNextSave = false; throw new IllegalStateException("disk full"); }
            held = h; original = o; events.add("save(" + h + "," + o + ")");
        }
    }

    private final class FakeSetting implements NotifQuietHold.Setting {
        String value; boolean failNextPut;
        FakeSetting(String v) { value = v; }
        @Override public String get() { return value; }
        @Override public void put(String v) {
            if (failNextPut) { failNextPut = false; throw new SecurityException("permission revoked"); }
            value = v; events.add("put(" + v + ")");
        }
    }

    private static final NotifQuietHold.Sink QUIET = new NotifQuietHold.Sink() { @Override public void log(String m) { } };

    private boolean set(FakeState st, FakeSetting s, boolean permitted, boolean want) { return NotifQuietHold.set(st, s, permitted, want, QUIET); }

    @Test public void theOriginalIsOnDiskBeforeTheFirstWrite() {
        FakeState st = new FakeState(); FakeSetting s = new FakeSetting("1");
        assertTrue(set(st, s, true, true));
        assertEquals("0", s.value);
        assertTrue(st.held);
        assertEquals("1", st.original);
        assertEquals("[save(true,1), put(0)]", events.toString());
    }

    @Test public void aSettingThatIsAlreadyOffIsLeftAloneAndNothingIsHeld() {
        FakeState st = new FakeState(); FakeSetting s = new FakeSetting("0");
        assertFalse(set(st, s, true, true));
        assertFalse(st.held);
        assertEquals("0", s.value);
        assertTrue(events.isEmpty());
    }

    @Test public void aMissingSettingCountsAsOffAndIsNotWritten() {
        FakeState st = new FakeState(); FakeSetting s = new FakeSetting(null);
        assertFalse(set(st, s, true, true));
        assertNull(s.value);
        assertTrue(events.isEmpty());
    }

    @Test public void aHoldThatIsAlreadyOnRewritesNothing() {
        FakeState st = new FakeState(); FakeSetting s = new FakeSetting("1");
        set(st, s, true, true);
        events.clear();
        assertTrue(set(st, s, true, true));
        assertTrue(events.isEmpty());
    }

    @Test public void endingTheHoldPutsTheOriginalBackAndClearsTheStateAfterTheWrite() {
        FakeState st = new FakeState(); FakeSetting s = new FakeSetting("1");
        set(st, s, true, true);
        events.clear();
        assertFalse(set(st, s, true, false));
        assertEquals("1", s.value);
        assertFalse(st.held);
        assertEquals("[put(1), save(false,null)]", events.toString());
    }

    @Test public void aValueTheOriginalWasIsPutBackExactly() {
        FakeState st = new FakeState(); FakeSetting s = new FakeSetting("2");
        set(st, s, true, true);
        set(st, s, true, false);
        assertEquals("2", s.value);
    }

    @Test public void aValueChangedByHandWhileHeldIsKeptWhenTheHoldEnds() {
        FakeState st = new FakeState(); FakeSetting s = new FakeSetting("1");
        set(st, s, true, true);
        s.value = "1";   // the banners were turned on again by hand in the meantime
        events.clear();
        assertFalse(set(st, s, true, false));
        assertEquals("1", s.value);
        assertEquals("[save(false,null)]", events.toString());
    }

    @Test public void aDeletedSettingIsNotOursToRestore() {
        FakeState st = new FakeState(); FakeSetting s = new FakeSetting("1");
        set(st, s, true, true);
        s.value = null;
        assertFalse(set(st, s, true, false));
        assertNull(s.value);
        assertFalse(st.held);
    }

    @Test public void aHoldIsReassertedWhenSomethingTurnedTheBannersOnWhileItWasWanted() {
        FakeState st = new FakeState(); FakeSetting s = new FakeSetting("1");
        set(st, s, true, true);
        s.value = "1";   // something else wrote the setting
        assertTrue(set(st, s, true, true));
        assertEquals("0", s.value);
        assertEquals("1", st.original);
    }

    @Test public void withoutThePermissionNothingIsTakenAndNothingIsEnded() {
        FakeState st = new FakeState(); FakeSetting s = new FakeSetting("1");
        assertFalse(set(st, s, false, true));
        assertEquals("1", s.value);
        assertTrue(events.isEmpty());
        set(st, s, true, true);   // now with the permission
        events.clear();
        assertTrue("a hold stays when it cannot be ended", set(st, s, false, false));
        assertTrue(st.held);
        assertEquals("0", s.value);
        assertTrue(events.isEmpty());
        assertFalse("the next start with the permission finishes the job", set(st, s, true, false));
        assertEquals("1", s.value);
    }

    @Test public void aWriteThatFailsWhileTakingTheHoldIsUndone() {
        FakeState st = new FakeState(); FakeSetting s = new FakeSetting("1");
        s.failNextPut = true;
        assertFalse(set(st, s, true, true));
        assertFalse("the stored state is back to 'not held'", st.held);
        assertNull(st.original);
        assertEquals("1", s.value);
    }

    @Test public void aSaveThatFailsMeansNoChangeIsMadeAtAll() {
        FakeState st = new FakeState(); FakeSetting s = new FakeSetting("1");
        st.failNextSave = true;
        assertFalse(set(st, s, true, true));
        assertEquals("the setting is never touched without its original on disk", "1", s.value);
        assertFalse(st.held);
    }

    @Test public void aWriteThatFailsWhileEndingTheHoldKeepsItSoTheNextTriggerTriesAgain() {
        FakeState st = new FakeState(); FakeSetting s = new FakeSetting("1");
        set(st, s, true, true);
        s.failNextPut = true;
        assertTrue(set(st, s, true, false));
        assertTrue(st.held);
        assertEquals("0", s.value);
        assertFalse(set(st, s, true, false));   // the retry
        assertEquals("1", s.value);
        assertFalse(st.held);
    }

    @Test public void endingAHoldThatWasNeverTakenDoesNothing() {
        FakeState st = new FakeState(); FakeSetting s = new FakeSetting("1");
        assertFalse(set(st, s, true, false));
        assertEquals("1", s.value);
        assertTrue(events.isEmpty());
    }

    @Test public void aSessionCycleEndsExactlyWhereItStarted() {
        FakeState st = new FakeState(); FakeSetting s = new FakeSetting("1");
        for (int i = 0; i < 3; i++) {   // custom (quiet), Quickstep (not), lock (quiet), unlock to Quickstep (not) ...
            assertTrue(set(st, s, true, true));
            assertEquals("0", s.value);
            assertFalse(set(st, s, true, false));
            assertEquals("1", s.value);
        }
        assertFalse(st.held);
    }
}
