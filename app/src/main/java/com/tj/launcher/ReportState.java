package com.tj.launcher;

import android.content.Context;
import android.provider.Settings;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Tj, 2026-11-05: "make sure claude can save and use all necessary information from the tablet". The report used to hold the tablet's facts but nothing about HOW THIS LAUNCHER is doing; Tj had to read
 * "Last unlock" off a screen and type it. Now the state goes into the report by itself.
 *
 * The report is sent to Claude and KEPT in the project, so nothing secret may go in. The rule is an allow-list, not "dump the preferences": every on/off switch (a boolean cannot hold a secret) and a short
 * list of text values that are timings, last results or tile names. The lock-screen number text, handshake settings and anything else are never read. The rule is pure so a JVM test pins it.
 */
public final class ReportState {
    private ReportState() {}

    /** Text / number preferences that are safe to report. Everything not listed here is left out. */
    static final Set<String> SAFE_VALUES = new HashSet<>(Arrays.asList(
            "power_mode", "return_app_label", "qs_tiles_applied_mode", "qs_tiles_custom", "qs_tiles_quickstep",
            "unlock_pick_last", "unlock_speed", "reset_last", "failsafe_last", "profile_last", "power_latency",
            "guard_count", "guard_last", "keyboard_status", "wallpaper_patch_status", "lock_cal_status",
            "compile_done_code", "boot_count_seen", "keytest_summary", "keytest_output", "keytest_menu", "keytest_at", "keep_alive_blocked_why", "battery_saver_last"));

    /** Pure: the "key: value" lines for the preferences in {@code all}: every Boolean, plus the allow-listed text and number values; sorted, values flattened to one line and cut to 600 characters. */
    public static String format(Map<String, ?> all) {
        StringBuilder b = new StringBuilder();
        if (all == null) return "";
        for (Map.Entry<String, Object> e : new TreeMap<String, Object>(all).entrySet()) {
            Object v = e.getValue();
            String k = e.getKey();
            if (k == null || v == null) continue;
            boolean ok = v instanceof Boolean || SAFE_VALUES.contains(k);
            if (!ok) continue;
            String s = String.valueOf(v).replace('\r', ' ').replace('\n', ' ');
            if (s.length() > 600) s = s.substring(0, 600) + "...";
            b.append(k).append(": ").append(s).append('\n');
        }
        return b.toString();
    }

    private static String yes(boolean v) { return v ? "yes" : "no"; }

    private static String setting(String kind, Context c, String name) {
        try {
            String v = "global".equals(kind) ? Settings.Global.getString(c.getContentResolver(), name) : Settings.Secure.getString(c.getContentResolver(), name);
            return v == null ? "(not set)" : v;
        } catch (Throwable t) { return "(unreadable)"; }
    }

    /** The whole "how is the launcher doing" section. Safe to call from any thread. {@code helperReachable} is passed in because asking the helper blocks. */
    public static String section(Context c, boolean helperReachable) { return section(c, helperReachable, true); }

    /** {@code full} = the whole report file (40 lock-log lines, the whole power-menu dump); false = the SHORT summary that gets pasted into a chat (25 lines, a cut dump). */
    public static String section(Context c, boolean helperReachable, boolean full) {
        StringBuilder b = new StringBuilder("\n===== THE LAUNCHER ON THIS TABLET (its state when this report was written)\n");
        b.append("report written: ").append(new java.util.Date()).append('\n');
        try {
            android.content.pm.PackageInfo pi = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            b.append("launcher version: ").append(pi.versionName).append(" (code ").append(pi.versionCode).append(")\n");
        } catch (Throwable t) { b.append("launcher version: (unreadable)\n"); }
        try { b.append("mode: ").append(Mode.get(c)).append('\n'); } catch (Throwable t) { b.append("mode: (unreadable)\n"); }
        b.append("shell helper reachable: ").append(yes(helperReachable)).append('\n');
        b.append("accessibility helper bound: ").append(yes(TjAccessibilityService.get() != null)).append('\n');
        b.append("keep-alive service running: ").append(yes(KeepAliveService.running())).append(" (switch ").append(KeepAliveService.enabled(c) ? "on" : "off").append(")\n");
        b.append("keep-alive crash fuse blown (Android refused its notification, so it stays off until you switch it on again): ").append(yes(KeepAliveService.blocked(c))).append(KeepAliveService.blocked(c) ? "  " + KeepAliveService.blockedWhy(c) : "").append('\n');
        b.append("status-bar hiding: switch ").append(ImmersiveProfile.enabled(c) ? "on" : "off").append(", hidden now ").append(yes(ImmersiveProfile.active(c))).append(", policy_control = ").append(setting("global", c, "policy_control")).append('\n');
        b.append("quick-settings tiles setting now: ").append(setting("secure", c, "sysui_qs_tiles")).append('\n');
        b.append("retail-demo flag (must be 0 or not set): ").append(setting("global", c, "device_demo_mode")).append('\n');
        try { b.append("crash breaker tripped: ").append(yes(CrashGuard.wasTripped(c))).append('\n'); } catch (Throwable t) { /* not available */ }
        try { b.append("automation paused (Restore everything, or the breaker cleared by an update; the lock-screen rules do nothing while paused): ").append(yes(CrashGuard.isPaused(c))).append('\n'); } catch (Throwable t) { /* not available */ }
        try {
            b.append("battery saver guard: switch ").append(BatterySaverGuard.enabled(c) ? "on" : "off").append(", low_power = ").append(setting("global", c, BatterySaverPolicy.SETTING)).append(", low_power_sticky = ").append(setting("global", c, BatterySaverPolicy.STICKY_SETTING))
                    .append(", WRITE_SECURE_SETTINGS ").append(BatterySaverGuard.permitted(c) ? "granted" : "MISSING").append('\n');
        } catch (Throwable t) { /* not available */ }
        b.append("\n----- the launcher's switches and last results (an allow-list: nothing secret is read)\n");
        b.append(format(Features.all(c)));
        try {
            int n = full ? 40 : 25;
            String log = LockLog.tail(c, n);
            b.append("\n----- the lock screen log (last ").append(n).append(" events; key KINDS only, never a digit)\n").append(log.isEmpty() ? "(empty: lock and unlock the tablet once)\n" : log);
            String dump = PowerMenuDump.read(c);
            int cap = full ? 12000 : 3500;
            if (dump.length() > cap) dump = dump.substring(0, cap) + "...\n";
            b.append("\n----- the stock power menu as the accessibility helper saw it (open the power menu once in the custom launcher with the cover on; empty = not seen yet)\n").append(dump.isEmpty() ? "(none yet)\n" : dump);
        } catch (Throwable t) { /* not available */ }
        return b.toString();
    }
}
