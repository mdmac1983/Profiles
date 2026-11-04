package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class LockNumberSanitizeTest {
    @Test public void keepsDigits() { assertEquals("71125011907547", LockNumber.sanitize("71125011907547")); }
    @Test public void trimsAndStripsOddCharacters() { assertEquals("AB-12 3", LockNumber.sanitize("  AB-12 3<>\n")); }
    @Test public void emptyMeansTheDefault() { assertEquals(LockNumber.DEFAULT_TEXT, LockNumber.sanitize("")); assertEquals(LockNumber.DEFAULT_TEXT, LockNumber.sanitize(null)); assertEquals(LockNumber.DEFAULT_TEXT, LockNumber.sanitize("<<>>")); }
    @Test public void atMost24Characters() { assertEquals(24, LockNumber.sanitize("123456789012345678901234567890").length()); }
}
