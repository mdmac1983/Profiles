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

    private static String ask(String line, int timeoutMs) {
        LocalSocket s = new LocalSocket();
        try {
            s.connect(new LocalSocketAddress(ShellProtocol.SOCKET, LocalSocketAddress.Namespace.ABSTRACT));
            s.setSoTimeout(timeoutMs);
            PrintWriter w = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
            w.println(line);
            BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
            return r.readLine();
        } catch (Throwable t) {
            return null;
        } finally {
            try { s.close(); } catch (Throwable ignored) { }
        }
    }

    /** Is the shell helper running right now? (a refused connection fails at once, so this is cheap) */
    public static boolean running() {
        String r = ask("PING", 500);
        return r != null && r.startsWith("OK");
    }

    /** Force-stops the packages (the helper re-checks its deny-list). @return how many it stopped, or -1 if the helper could not be reached. Blocking: call off the main thread. */
    public static int forceStop(Collection<String> pkgs) {
        if (pkgs == null || pkgs.isEmpty()) return 0;
        String r = ask("FORCESTOP " + android.text.TextUtils.join(" ", pkgs), 25_000);
        if (r == null || !r.startsWith("OK")) return -1;
        try { return Integer.parseInt(r.substring(2).trim()); } catch (NumberFormatException e) { return 0; }
    }
}
