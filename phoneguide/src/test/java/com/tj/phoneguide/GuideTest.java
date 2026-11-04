package com.tj.phoneguide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.junit.Test;

public class GuideTest {
    @Test public void switchesOnWhenNothingIsOn() {
        assertEquals("settings put secure enabled_accessibility_services " + A11yLine.COMPONENT, A11yLine.build("null"));
        assertEquals("settings put secure enabled_accessibility_services " + A11yLine.COMPONENT, A11yLine.build(""));
        assertEquals("settings put secure enabled_accessibility_services " + A11yLine.COMPONENT, A11yLine.build(null));
    }
    @Test public void keepsTheServicesThatAreAlreadyOn() {
        assertEquals("settings put secure enabled_accessibility_services a.b/c.D:" + A11yLine.COMPONENT, A11yLine.build("a.b/c.D"));
        assertEquals("settings put secure enabled_accessibility_services a.b/c.D:e.f/g.H:" + A11yLine.COMPONENT, A11yLine.build(" a.b/c.D:e.f/g.H \n"));
    }
    @Test public void doesNotAddItTwice() {
        String on = "x.y/z.Z:" + A11yLine.COMPONENT;
        assertEquals("settings put secure enabled_accessibility_services " + on, A11yLine.build(on));
    }

    private static java.util.List<String> lines() throws Exception {
        java.util.List<String> out = new java.util.ArrayList<>();
        File f = new File("src/main/assets/steps.txt");
        BufferedReader r = new BufferedReader(new FileReader(f));
        for (String l; (l = r.readLine()) != null; ) out.add(l);
        r.close();
        return out;
    }
    @Test public void everyCommandIsTheBareDeviceShellForm() throws Exception {
        int n = 0;
        for (String l : lines()) {
            if (!(l.startsWith("cmd:") || l.startsWith("alt:"))) continue;
            String c = l.substring(4).trim(); n++;
            assertFalse(l, c.isEmpty());
            assertFalse("no adb prefix in the OTG form: " + l, c.equals("adb") || c.startsWith("adb "));
        }
        assertTrue("the list has real content", n >= 40);
    }
    @Test public void noCommandIsDestructive() throws Exception {
        String[] banned = { "pm uninstall", "pm clear", "wipe", "factory", "rm -", "reboot", "setprop", "locksettings", "pm hide", "pm suspend", "svc wifi", "svc data", "airplane", "adb_enabled", "format" };
        for (String l : lines()) {
            if (!(l.startsWith("cmd:") || l.startsWith("alt:") || l.startsWith("undo:"))) continue;
            for (String b : banned) assertFalse(l + " contains " + b, l.contains(b));
        }
    }
    @Test public void coversEverythingTjAskedFor() throws Exception {
        StringBuilder all = new StringBuilder();
        for (String l : lines()) all.append(l).append('\n');
        String s = all.toString();
        String[] must = { "WRITE_SECURE_SETTINGS", "DUMP", "CHANGE_CONFIGURATION", "READ_EXTERNAL_STORAGE", "GET_USAGE_STATS", "WRITE_SETTINGS", "RUN_IN_BACKGROUND", "RUN_ANY_IN_BACKGROUND",
                "deviceidle whitelist +com.tj.launcher", "enabled_accessibility_services", "accessibility_enabled", "com.tj.launcher.ShellServer", "cmd package compile -m speed -f com.tj.launcher",
                "pm disable-user --user 0 com.android.browser", "pm get-max-users", "pm list users", "ro.crypto.type", "power_menu_actions", "device_demo_mode", "pkill -f com.tj.launcher.ShellServer", "pm enable com.android.browser" };
        for (String m : must) assertTrue("missing: " + m, s.contains(m));
    }
}
