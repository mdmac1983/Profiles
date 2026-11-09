package com.tj.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;

/**
 * ACC1 (found 2026-11-05 on the real tablet, root cause TASKS 491): "accessibility helper bound: no" although it is enabled. After a crash loop Android can leave the service listed in
 * {@code enabled_accessibility_services} but stuck in a stale "binding" state, so the power-menu patch, the lock number and the lock-screen rules silently do nothing. Control and the tablet report only said ON / OFF,
 * which sent Tj to the wrong place. This says the third thing plainly: enabled but NOT connected, and what helps (restart the tablet once).
 * Pure rules (JVM-tested) + two small Android readers.
 */
public final class HelperStatus {
    public static final int OFF = 0, CONNECTED = 1, ENABLED_NOT_CONNECTED = 2;

    private HelperStatus() {}

    /** Pure: the state from the three facts. */
    public static int of(boolean listedInSettings, boolean masterSwitchOn, boolean connected) {
        if (connected) return CONNECTED;
        return listedInSettings && masterSwitchOn ? ENABLED_NOT_CONNECTED : OFF;
    }

    /** Pure: is our service in the colon-separated list Android keeps? Accepts the long and the short form of the component name. */
    public static boolean listed(String enabledServices, String pkg, String cls) {
        if (enabledServices == null || enabledServices.isEmpty() || pkg == null || cls == null) return false;
        for (String part : enabledServices.split(":")) {
            String p = part.trim();
            if (p.isEmpty()) continue;
            ComponentName cn = ComponentName.unflattenFromString(p);
            if (cn != null && pkg.equals(cn.getPackageName()) && cls.equals(cn.getClassName())) return true;
            if (p.equals(pkg + "/" + cls) || p.equals(pkg + "/." + cls.substring(cls.lastIndexOf('.') + 1))) return true;   // plain-JVM tests have no ComponentName parser
        }
        return false;
    }

    /** The plain words for Profiles settings and the tablet report. */
    public static String describe(int state) {
        switch (state) {
            case CONNECTED: return "ON";
            case ENABLED_NOT_CONNECTED: return "enabled but NOT connected  -  restart the tablet once (after a crash Android can leave the service stuck); until then the power-menu patch, the lock number and the lock-screen rules do nothing";
            default: return "OFF  -  turn it on in Accessibility settings (power menu, reset, lock screen, pencil need it)";
        }
    }

    /** The state right now. Never throws. */
    public static int now(Context c) {
        try {
            boolean connected = TjAccessibilityService.get() != null;
            String list = Settings.Secure.getString(c.getContentResolver(), "enabled_accessibility_services");
            boolean master = Settings.Secure.getInt(c.getContentResolver(), "accessibility_enabled", 0) == 1;
            return of(listed(list, c.getPackageName(), TjAccessibilityService.class.getName()), master, connected);
        } catch (Throwable t) { return TjAccessibilityService.get() != null ? CONNECTED : OFF; }
    }
}
