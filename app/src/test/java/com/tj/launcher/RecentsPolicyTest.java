package com.tj.launcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class RecentsPolicyTest {
    @Test public void otherAppsAreExcludedFromRecents() { assertTrue(RecentsPolicy.shouldExclude(true, "com.brave.browser", "com.solitaire", "com.tj.launcher")); }
    @Test public void theTargetAppStaysInRecents() { assertFalse(RecentsPolicy.shouldExclude(true, "com.solitaire", "com.solitaire", "com.tj.launcher")); }
    @Test public void switchedOffMeansStock() { assertFalse(RecentsPolicy.shouldExclude(false, "com.brave.browser", "com.solitaire", "com.tj.launcher")); }
    @Test public void noTargetInstalledStillExcludesOthers() { assertTrue(RecentsPolicy.shouldExclude(true, "com.brave.browser", null, "com.tj.launcher")); }
    @Test public void unknownPackageIsLeftAlone() { assertFalse(RecentsPolicy.shouldExclude(true, null, "x", "com.tj.launcher")); }
}
