package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class KeyboardPolicyTest {
    private static final KeyboardPolicy.Ime LATIN = new KeyboardPolicy.Ime("com.android.inputmethod.latin/.LatinIME", "com.android.inputmethod.latin", true);
    private static final KeyboardPolicy.Ime GBOARD = new KeyboardPolicy.Ime("com.google.android.inputmethod.latin/x", "com.google.android.inputmethod.latin", true);
    private static final KeyboardPolicy.Ime SWIFT = new KeyboardPolicy.Ime("com.touchtype.swiftkey/com.touchtype.KeyboardService", "com.touchtype.swiftkey", false);
    private static final KeyboardPolicy.Ime OTHER = new KeyboardPolicy.Ime("com.other/.Ime", "com.other", false);

    @Test public void customModeUsesTheStockSystemKeyboard() {
        List<KeyboardPolicy.Ime> l = Arrays.asList(SWIFT, LATIN, OTHER);
        assertEquals(LATIN.id, KeyboardPolicy.target(Mode.CUSTOM, l, SWIFT.id));
    }
    @Test public void quickstepModeUsesSwiftKey() {
        assertEquals(SWIFT.id, KeyboardPolicy.target(Mode.QUICKSTEP, Arrays.asList(LATIN, SWIFT), LATIN.id));
    }
    @Test public void quickstepFallsBackToTheOriginalWhenSwiftKeyIsNotEnabled() {
        assertEquals(LATIN.id, KeyboardPolicy.target(Mode.QUICKSTEP, Arrays.asList(LATIN), LATIN.id));
    }
    @Test public void neverPicksAKeyboardThatIsNotEnabled() {
        assertNull(KeyboardPolicy.target(Mode.CUSTOM, Arrays.asList(SWIFT, OTHER), SWIFT.id));
        assertNull(KeyboardPolicy.target(Mode.QUICKSTEP, Arrays.asList(LATIN), "gone/.Ime"));
    }
    @Test public void prefersTheAospLatinKeyboard() {
        assertEquals(LATIN.id, KeyboardPolicy.pickStock(Arrays.asList(GBOARD, LATIN)));
    }
}
