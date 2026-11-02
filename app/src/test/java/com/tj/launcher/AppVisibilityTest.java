package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class AppVisibilityTest {
    private static Set<String> set(String... s) { return new HashSet<>(Arrays.asList(s)); }
    private static final String SELF = "com.tj.launcher";

    @Test public void baselineKeepsEverythingInstalledButNotUs() {
        Set<String> b = AppVisibility.baseline(set("a", "b", SELF), SELF);
        assertEquals(set("a", "b"), b);
    }
    @Test public void appsInstalledAfterTheBaselineAreHidden() {
        Set<String> allowed = AppVisibility.baseline(set("a", "b"), SELF);
        assertTrue(AppVisibility.isShown(allowed, "a", "A", SELF));
        assertFalse(AppVisibility.isShown(allowed, "newapp", "N", SELF));
    }
    @Test public void tjCanAllowOneAndHideAnother() {
        Set<String> allowed = AppVisibility.baseline(set("a", "b"), SELF);
        allowed = AppVisibility.withShown(allowed, "newapp", true);
        allowed = AppVisibility.withShown(allowed, "b", false);
        assertTrue(AppVisibility.isShown(allowed, "newapp", "N", SELF));
        assertFalse(AppVisibility.isShown(allowed, "b", "B", SELF));
        assertTrue(AppVisibility.isShown(allowed, "a", "A", SELF));
    }
    @Test public void ourOwnIconsNeverShowInTheCustomDrawer() {
        assertFalse(AppVisibility.isShown(set(SELF, "a"), SELF, "com.tj.launcher.ControlActivity", SELF));
        assertFalse(AppVisibility.isShown(set(SELF), SELF, null, SELF));
    }
    @Test public void aReinstalledAppCountsAsNewAndIsHidden() {
        Set<String> allowed = AppVisibility.baseline(set("a", "b"), SELF);
        allowed = AppVisibility.withoutRemoved(allowed, "b");   // uninstalled
        assertFalse(AppVisibility.isShown(allowed, "b", "B", SELF));   // reinstalled: hidden until allowed
    }
}
