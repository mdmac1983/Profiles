package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class UnlockPickTest {
    @Test public void classifiesPinPadKeysByIdOnly() {
        assertEquals(UnlockPick.Key.DIGIT, UnlockPick.classify("com.android.systemui:id/key7"));
        assertEquals(UnlockPick.Key.DIGIT, UnlockPick.classify("com.android.systemui:id/key0"));
        assertEquals(UnlockPick.Key.DELETE, UnlockPick.classify("com.android.systemui:id/delete_button"));
        assertEquals(UnlockPick.Key.ENTER, UnlockPick.classify("com.android.systemui:id/key_enter"));
        assertNull(UnlockPick.classify("com.android.systemui:id/pinEntry"));
        assertNull(UnlockPick.classify("com.android.systemui:id/key12"));
        assertNull(UnlockPick.classify(null));
    }
}
