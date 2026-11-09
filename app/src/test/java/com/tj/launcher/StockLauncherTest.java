package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class StockLauncherTest {
    private ActivityInfo home(String pkg, String cls, boolean system) {
        ActivityInfo a = new ActivityInfo();
        a.packageName = pkg; a.name = cls;
        a.applicationInfo = new ApplicationInfo();
        a.applicationInfo.flags = system ? ApplicationInfo.FLAG_SYSTEM : 0;
        return a;
    }

    @Test public void neverPicksOurselves() {
        assertNull(StockLauncher.choose(Collections.singletonList(home("app.orionmd.profiles", "R", false)), "app.orionmd.profiles"));
    }

    @Test public void prefersSystemQuickstep() {
        ActivityInfo c = StockLauncher.choose(Arrays.asList(
                home("app.orionmd.profiles", "R", false), home("com.nova", "N", false),
                home("com.android.launcher3", "com.android.launcher3.uioverrides.QuickstepLauncher", true)), "app.orionmd.profiles");
        assertEquals("com.android.launcher3", c.packageName);
    }

    @Test public void fallsBackToAnyOtherHome() {
        ActivityInfo c = StockLauncher.choose(Arrays.asList(home("app.orionmd.profiles", "R", false), home("com.other", "O", false)), "app.orionmd.profiles");
        assertEquals("com.other", c.packageName);
    }
}
