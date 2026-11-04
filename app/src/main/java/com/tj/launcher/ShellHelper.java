package com.tj.launcher;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;

/** N6 client: talks to {@link ShellServer} if the adb command was run since the last boot. Every call is bounded and never throws. */
public final class ShellHelper {
    public static final String KEY = "invisible_cleanup";     // switch, default ON (does nothing unless the helper is running)
    private ShellHelper() {}

    public static boolean enabled(android.content.Context c) { return Features.get(c, KEY, true); }

    /** Why the last attempt failed (shown in the log / Control), e.g. "unix: Permission denied; tcp: Connection refused". */
    public static volatile String lastError = "";

    private static String talk(java.io.InputStream is, java.io.OutputStream os, String line) throws java.io.IOException {
        PrintWriter w = new PrintWriter(new OutputStreamWriter(os), true);
        w.println(line);
        return new BufferedReader(new InputStreamReader(is)).readLine();
    }

    private static String ask(String line, int timeoutMs) {
        StringBuilder err = new StringBuilder();
        LocalSocket s = new LocalSocket();
        try {
            s.connect(new LocalSocketAddress(ShellProtocol.SOCKET, LocalSocketAddress.Namespace.ABSTRACT));
            s.setSoTimeout(timeoutMs);
            String r = talk(s.getInputStream(), s.getOutputStream(), line);
            if (r != null) { lastError = ""; return r; }
            err.append("unix: no answer; ");
        } catch (Throwable t) {
            err.append("unix: ").append(t.getClass().getSimpleName()).append(" ").append(t.getMessage()).append("; ");
        } finally {
            try { s.close(); } catch (Throwable ignored) { }
        }
        for (int port = ShellProtocol.TCP_PORT_FIRST; port <= ShellProtocol.TCP_PORT_LAST; port++) {
            java.net.Socket t = new java.net.Socket();
            try {
                t.connect(new java.net.InetSocketAddress("127.0.0.1", port), 300);
                t.setSoTimeout(timeoutMs);
                String r = talk(t.getInputStream(), t.getOutputStream(), line);
                if (r != null && r.startsWith("OK")) { lastError = ""; return r; }
                err.append("tcp ").append(port).append(": ").append(r).append("; ");
            } catch (Throwable e) {
                err.append("tcp ").append(port).append(": ").append(e.getClass().getSimpleName()).append(" ").append(e.getMessage()).append("; ");
                if (e instanceof java.net.ConnectException) break;   // nothing listening on the first port: no helper
            } finally {
                try { t.close(); } catch (Throwable ignored) { }
            }
        }
        lastError = err.toString();
        return null;
    }

    /**
     * Is the helper reachable? Safe to call from the MAIN thread: Android throws NetworkOnMainThreadException for a TCP connect there (that is what made run 45 report "tcp ...: null"),
     * so the PING runs on its own short-lived thread and the caller waits at most ~700 ms (a refused connection answers in a few ms).
     */
    public static boolean reachable() {
        final boolean[] r = {false};
        Thread t = new Thread(new Runnable() { @Override public void run() { r[0] = running(); } }, "tj-shell-ping");
        t.start();
        try { t.join(700); } catch (InterruptedException ignored) { }
        return r[0];
    }

    /** Everything the helper printed after its "OK" line (for RUN / PROBE / A11YON), or null if it could not be reached / refused. Blocking, long timeout: call off the main thread. */
    private static String askLong(String line, int timeoutMs) {
        for (int port = ShellProtocol.TCP_PORT_FIRST; port <= ShellProtocol.TCP_PORT_LAST; port++) {
            java.net.Socket t = new java.net.Socket();
            try {
                t.connect(new java.net.InetSocketAddress("127.0.0.1", port), 400);
                t.setSoTimeout(timeoutMs);
                PrintWriter w = new PrintWriter(new OutputStreamWriter(t.getOutputStream()), true);
                w.println(line);
                BufferedReader r = new BufferedReader(new InputStreamReader(t.getInputStream()));
                String first = r.readLine();
                if (first == null || !first.startsWith("OK")) { lastError = "refused: " + first; return null; }
                StringBuilder sb = new StringBuilder(); String l;
                while ((l = r.readLine()) != null) sb.append(l).append('\n');
                return sb.toString();
            } catch (java.net.ConnectException e) {
                // nothing on this port: try the next one (the helper takes the first free port of the range)
            } catch (Throwable e) { lastError = e.getClass().getSimpleName() + " " + e.getMessage(); return null; }
            finally { try { t.close(); } catch (Throwable ignored) { } }
        }
        return null;
    }

    /** Runs one allow-listed job (ShellProtocol.RUN) and returns its output, or null. */
    public static String run(String jobId) { return askLong("RUN " + jobId, 60_000); }
    /** Photographs the stock Settings pages + dumps their screen structure into Downloads/launcher-probe/shots (about 1.5 minutes). */
    public static String shots() { return askLong("SHOTS", 300_000); }
    /** Copies the tablet's Settings / SystemUI / stock launcher / framework APKs into Downloads/launcher-probe/apk. */
    public static String apks() { return askLong("APKS", 300_000); }
    /** Runs the whole read-only probe list bundled in the APK. */
    public static String probe() { return askLong("PROBE", 300_000); }
    /** Adds the accessibility helper to the enabled services. */
    public static String a11yOn() { return askLong("A11YON", 20_000); }

    /** Is the shell helper running right now? (a refused connection fails at once, so this is cheap) */
    public static boolean running() {
        String r = ask("PING", 1500);
        boolean up = r != null && r.startsWith("OK");
        if (!up) android.util.Log.i("TjReset", "shell helper not reachable: " + lastError);
        return up;
    }

    /** Force-stops the packages (the helper re-checks its deny-list). @return how many it stopped, or -1 if the helper could not be reached. Blocking: call off the main thread. */
    public static int forceStop(Collection<String> pkgs) {
        if (pkgs == null || pkgs.isEmpty()) return 0;
        String r = ask("FORCESTOP " + android.text.TextUtils.join(" ", pkgs), 25_000);
        if (r == null || !r.startsWith("OK")) return -1;
        try { return Integer.parseInt(r.substring(2).trim().split("\\s+")[0]); } catch (NumberFormatException e) { return 0; }
    }
}
