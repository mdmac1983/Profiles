package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class AppOriginTest {
    private static Set<String> s(String... a) { return new HashSet<>(Arrays.asList(a)); }

    @Test public void onlyQuickstepOpenedAppsAreClosed() { assertEquals(s("brave"), AppOrigin.pending(s("clock"), s("brave"))); }
    @Test public void anAppTheCustomLauncherAlsoStartedIsKept() { assertEquals(s(), AppOrigin.pending(s("brave"), s("brave"))); }
    @Test public void nothingPendingWhenQuickstepOpenedNothing() { assertTrue(AppOrigin.pending(s("clock"), s()).isEmpty()); }
    @Test public void cardMatchesTheAppLabel() { assertTrue(AppOrigin.cardMatches("Brave", s("Brave"))); assertTrue(AppOrigin.cardMatches("brave, 5 minutes left today", s("Brave"))); }
    @Test public void cardDoesNotMatchOtherApps() { assertFalse(AppOrigin.cardMatches("Braver Rabbit", s("Brave"))); assertFalse(AppOrigin.cardMatches("Solitaire", s("Brave"))); assertFalse(AppOrigin.cardMatches(null, s("Brave"))); assertFalse(AppOrigin.cardMatches("", s("Brave"))); }
}
