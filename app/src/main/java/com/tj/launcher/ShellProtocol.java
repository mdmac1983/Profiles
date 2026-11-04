package com.tj.launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * N6 (Tj, 2026-11-04): "make it totally invisible once I have the adb commands entered". The only thing that can remove another app's Recents card AND its notifications without driving any
 * screen is {@code am force-stop}, which only the shell user may run. {@link ShellServer} is a tiny process started ONCE per boot by an adb command; it runs as the shell user, listens on a
 * local socket, and accepts exactly two commands from OUR app only (peer uid is checked). This class is the pure, JVM-tested part: parsing and the deny-list.
 *   PING                      -> OK tj-shell 1
 *   FORCESTOP pkg [pkg ...]   -> OK <n stopped> (packages that fail the deny-list / name check are skipped, never run)
 *   QUIT                      -> OK (the process ends)
 */
public final class ShellProtocol {
    public static final String SOCKET = "tj_shell_helper";
    public static final String SELF = "com.tj.launcher", STOCK = "com.android.launcher3";
    public static final int MAX_PACKAGES = 40;
    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+$");

    private ShellProtocol() {}

    /** Hard deny-list, enforced INSIDE the shell process whatever the app asks for: never stop the system, the phone, providers, the keyboards, this app or the stock launcher. */
    public static boolean allowed(String pkg) {
        if (pkg == null || pkg.length() > 150 || !NAME.matcher(pkg).matches()) return false;
        if (!ResetPolicy.shouldKill(pkg, SELF, STOCK, null)) return false;
        return !(pkg.startsWith("com.android.providers.") || pkg.startsWith("com.google.android.gms") || pkg.startsWith("com.google.android.gsf")
                || pkg.startsWith("com.android.server.") || pkg.equals("com.android.bluetooth") || pkg.equals("com.android.nfc") || pkg.startsWith("com.mediatek.")
                || pkg.equals("com.android.keychain") || pkg.equals("com.android.shell") || pkg.equals("com.android.permissioncontroller"));
    }

    /** The packages of a FORCESTOP line that pass {@link #allowed}, de-duplicated, at most MAX_PACKAGES. Null if the line is not a FORCESTOP. */
    public static List<String> forceStopTargets(String line) {
        if (line == null) return null;
        String[] w = line.trim().split("\\s+");
        if (w.length < 2 || !"FORCESTOP".equals(w[0])) return null;
        List<String> out = new ArrayList<>();
        for (int i = 1; i < w.length && out.size() < MAX_PACKAGES; i++) if (allowed(w[i]) && !out.contains(w[i])) out.add(w[i]);
        return out;
    }

    public static boolean isPing(String line) { return line != null && line.trim().equals("PING"); }
    public static boolean isQuit(String line) { return line != null && line.trim().equals("QUIT"); }

    /** The uid in the output of {@code cmd package list packages -U com.tj.launcher} ("package:com.tj.launcher uid:10123"), or -1. */
    public static int parseUid(String listOutput) {
        if (listOutput == null) return -1;
        for (String l : listOutput.split("\\n")) {
            int i = l.indexOf("uid:");
            if (l.contains("package:" + SELF) && i >= 0) {
                try { return Integer.parseInt(l.substring(i + 4).trim().split("[^0-9]")[0]); } catch (NumberFormatException ignored) { }
            }
        }
        return -1;
    }

    /** The one adb line that starts the helper (single quotes: the DEVICE shell expands the $(...)). */
    public static final String START_ONE_LINER =
            "adb shell 'CLASSPATH=$(pm path " + SELF + " | head -1 | cut -d: -f2) setsid nohup app_process /system/bin com.tj.launcher.ShellServer >/dev/null 2>&1 &'";
    public static final String START_STEP1 = "adb shell pm path " + SELF;
    public static final String START_STEP2 = "adb shell \"CLASSPATH=<the path printed above, without 'package:'> setsid nohup app_process /system/bin com.tj.launcher.ShellServer >/dev/null 2>&1 &\"";
    public static final String STOP_LINE = "adb shell pkill -f com.tj.launcher.ShellServer";
}
