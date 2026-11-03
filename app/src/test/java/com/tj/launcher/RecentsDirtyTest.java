package com.tj.launcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class RecentsDirtyTest {
    private static boolean m(String pkg, boolean other) { return RecentsDirty.shouldMark(pkg, "com.tj.launcher", "com.android.launcher3", "com.swiftkey", "com.solitaire", other); }

    @Test public void anotherAppMarksRecentsDirty() { assertTrue(m("com.brave.browser", true)); }
    @Test public void solitaireIsExpectedInRecents() { assertFalse(m("com.solitaire", true)); }
    @Test public void systemAndLauncherWindowsDoNotMark() {
        assertFalse(m("com.android.systemui", true)); assertFalse(m("com.android.launcher3", true)); assertFalse(m("com.tj.launcher", true));
        assertFalse(m("com.swiftkey", true)); assertFalse(m("android", true));
    }
    @Test public void appsLaunchedFromTheCustomLauncherAreExcludedSoTheyDoNotMark() { assertFalse(m("com.brave.browser", false)); }
    @Test public void emptyPackageDoesNotMark() { assertFalse(m(null, true)); assertFalse(m("", true)); }
}
