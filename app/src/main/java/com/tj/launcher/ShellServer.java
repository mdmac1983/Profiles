package com.tj.launcher;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * N6: the shell-user helper. NOT part of the app's own process: started by `adb shell ... app_process ... com.tj.launcher.ShellServer` (see {@link ShellProtocol#START_ONE_LINER}),
 * so it runs with the shell's powers (force-stop). It dies with the next reboot (nothing here survives a restart: that is Android's rule for adb-started processes) and can be stopped with
 * {@link ShellProtocol#STOP_LINE}. Only our app may talk to it: every connection's peer uid is compared with our app's uid, and the commands are the two in {@link ShellProtocol}.
 * No Context, no Looper: plain sockets and `am`.
 */
public final class ShellServer {
    private ShellServer() {}

    public static void main(String[] args) throws Exception {
        final int appUid = lookupUid();
        if (appUid < 0) { System.out.println("tj-shell: cannot find the uid of " + ShellProtocol.SELF + " (is it installed?)"); return; }
        boolean any = false;
        try {
            final LocalServerSocket server = new LocalServerSocket(ShellProtocol.SOCKET);
            any = true;
            System.out.println("tj-shell: unix socket up for uid " + appUid);
            new Thread(new Runnable() { @Override public void run() {
                try { while (true) { final LocalSocket c = server.accept(); new Thread(new Runnable() { @Override public void run() { serveUnix(c, appUid); } }, "tj-shell-conn").start(); } }
                catch (Throwable t) { System.out.println("tj-shell: unix accept ended: " + t); }
            } }, "tj-shell-unix").start();
        } catch (java.io.IOException e) { System.out.println("tj-shell: unix socket not available (" + e.getMessage() + ")"); }
        java.net.ServerSocket tcp = null;
        for (int port = ShellProtocol.TCP_PORT_FIRST; port <= ShellProtocol.TCP_PORT_LAST && tcp == null; port++) {
            try { tcp = new java.net.ServerSocket(port, 4, java.net.InetAddress.getByName("127.0.0.1")); } catch (java.io.IOException ignored) { }
        }
        if (tcp != null) { any = true; System.out.println("tj-shell: tcp up on 127.0.0.1:" + tcp.getLocalPort()); }
        if (!any) { System.out.println("tj-shell: nothing to listen on (already running?)"); return; }
        if (tcp == null) { Thread.sleep(Long.MAX_VALUE); return; }
        while (true) {
            final java.net.Socket c = tcp.accept();
            final int sport = tcp.getLocalPort();
            new Thread(new Runnable() { @Override public void run() { serveTcp(c, appUid, sport); } }, "tj-shell-tcp").start();
        }
    }

    private static void serveUnix(LocalSocket c, int appUid) {
        try {
            c.setSoTimeout(5000);
            int peer = c.getPeerCredentials().getUid();
            handle(c.getInputStream(), c.getOutputStream(), peer == appUid);
        } catch (Throwable t) {
            System.out.println("tj-shell: unix connection failed: " + t);
        } finally {
            try { c.close(); } catch (Throwable ignored) { }
        }
    }

    /** Loopback TCP has no peer-credential call; the owner uid of the client socket is read from /proc/net/tcp(6), which only the shell user can read on Android 10. */
    private static void serveTcp(java.net.Socket c, int appUid, int serverPort) {
        try {
            c.setSoTimeout(5000);
            int uid = ShellProtocol.uidForPorts(readFile("/proc/net/tcp") + "\n" + readFile("/proc/net/tcp6"), c.getPort(), serverPort);
            handle(c.getInputStream(), c.getOutputStream(), uid == appUid);
        } catch (Throwable t) {
            System.out.println("tj-shell: tcp connection failed: " + t);
        } finally {
            try { c.close(); } catch (Throwable ignored) { }
        }
    }

    private static String readFile(String path) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new java.io.FileReader(path));
            String l;
            while ((l = r.readLine()) != null) sb.append(l).append('\n');
            r.close();
            return sb.toString();
        } catch (Throwable t) { return ""; }
    }

    private static void handle(java.io.InputStream is, java.io.OutputStream os, boolean authorised) throws Exception {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(os), true);
        if (!authorised) { out.println("ERR denied"); return; }
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line = in.readLine();
        if (ShellProtocol.isPing(line)) { out.println("OK tj-shell 1"); return; }
        if (ShellProtocol.isQuit(line)) { out.println("OK"); out.flush(); System.exit(0); return; }
        String id = ShellProtocol.runId(line);
        if (id != null) {
            String sh = ShellProtocol.runLine(id);
            if (sh == null) { out.println("ERR unknown job"); return; }
            out.println("OK"); out.print(runShell(sh, 20_000)); out.flush(); return;
        }
        if (ShellProtocol.isA11yOn(line)) {
            String cur = runShell("settings get secure enabled_accessibility_services", 5_000).trim();
            String merged = ShellProtocol.mergeA11y(cur);
            runShell("settings put secure enabled_accessibility_services " + merged, 5_000);
            runShell("settings put secure accessibility_enabled 1", 5_000);
            out.println("OK"); out.println("accessibility services now: " + runShell("settings get secure enabled_accessibility_services", 5_000).trim()); out.flush(); return;
        }
        if (ShellProtocol.isShots(line)) {
            out.println("OK");
            runShell("mkdir -p " + ShellProtocol.PUBLISH_DIR + "/shots", 5_000);
            int n = 0;
            for (String page : ShellProtocol.SHOT_PAGES) {
                runShell("am start -a android.settings." + page, 8_000);
                try { Thread.sleep(2500); } catch (InterruptedException ignored) { }
                runShell("screencap -p " + ShellProtocol.PUBLISH_DIR + "/shots/" + page + ".png", 15_000);
                runShell("uiautomator dump " + ShellProtocol.PUBLISH_DIR + "/shots/" + page + ".xml", 15_000);
                out.println("shot " + page); out.flush(); n++;
            }
            runShell("input keyevent KEYCODE_HOME", 5_000);
            out.println("done: " + n + " pages"); out.flush(); return;
        }
        if (ShellProtocol.isApks(line)) {
            out.println("OK");
            runShell("mkdir -p " + ShellProtocol.PUBLISH_DIR + "/apk", 5_000);
            for (String pkg : ShellProtocol.APK_PACKAGES) {
                String path = runShell("pm path " + pkg + " | head -1 | cut -d: -f2", 8_000).trim();
                if (path.isEmpty()) { out.println("no apk for " + pkg); continue; }
                String r = runShell("cp " + path + " " + ShellProtocol.PUBLISH_DIR + "/apk/" + pkg + ".apk", 60_000).trim();
                out.println("copied " + pkg + (r.isEmpty() ? "" : " (" + r + ")")); out.flush();
            }
            out.println("done"); out.flush(); return;
        }
        if (ShellProtocol.isProbe(line)) {
            out.println("OK");
            java.io.InputStream ris = ShellServer.class.getClassLoader().getResourceAsStream("probe-commands.txt");
            StringBuilder sb = new StringBuilder();
            if (ris != null) { BufferedReader rr = new BufferedReader(new InputStreamReader(ris)); String l; while ((l = rr.readLine()) != null) sb.append(l).append('\n'); rr.close(); }
            for (ShellProtocol.ProbeLine pl : ShellProtocol.parseProbeList(sb.toString())) {
                if (pl.title) out.println("\n===== " + pl.text);
                else { out.println("$ " + pl.text); out.print(runShell(pl.text, 15_000)); }
            }
            out.flush(); return;
        }
        if (ShellProtocol.isListTasks(line)) {
            out.println("OK");
            for (java.util.Map.Entry<Integer, String> e : ShellProtocol.parseTasks(recentsDump()).entrySet()) out.println(e.getKey() + " " + e.getValue());
            out.flush(); return;
        }
        List<Integer> rmIds = ShellProtocol.removeTaskIds(line);
        if (rmIds != null) {
            // the deny-list is enforced HERE, on the package the system itself reports for each id: an id can never be used to close the system, the keyboard, this app or the stock launcher
            java.util.Map<Integer, String> now = ShellProtocol.parseTasks(recentsDump());
            int removed = 0, skipped = 0;
            for (int id : rmIds) {
                String pkg = now.get(id);
                if (pkg == null || !ShellProtocol.allowed(pkg)) { skipped++; continue; }
                if (removeTask(id)) removed++; else skipped++;
            }
            out.println("OK " + removed + " " + skipped); return;
        }
        List<String> pkgs = ShellProtocol.forceStopTargets(line);
        if (pkgs == null) { out.println("ERR unknown command"); return; }
        int stopped = forceStop(pkgs);
        int removed = removeRecentTasks(pkgs);
        out.println("OK " + stopped + " " + removed);
    }

    private static Object atmService;
    private static boolean atmFailedOnce = false;

    /** Removes the Recents cards of these packages: ids from `dumpsys activity recents` (the shell user may run it), removed through IActivityTaskManager.removeTask (the shell holds REMOVE_TASKS). */
    private static synchronized int removeRecentTasks(List<String> pkgs) {
        int n = 0;
        try {
            for (int id : ShellProtocol.recentTaskIds(recentsDump(), pkgs)) if (removeTask(id)) n++;
        } catch (Throwable t) { System.out.println("tj-shell: removing recents failed: " + t); }
        return n;
    }

    /** The text of `dumpsys activity recents` (the shell user may run it). Empty on any problem. */
    private static String recentsDump() {
        try {
            Process pr = new ProcessBuilder("dumpsys", "activity", "recents").redirectErrorStream(true).start();
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String l;
            while ((l = r.readLine()) != null) sb.append(l).append('\n');
            pr.waitFor();
            return sb.toString();
        } catch (Throwable t) { System.out.println("tj-shell: dumpsys failed: " + t); return ""; }
    }

    private static boolean removeTask(int id) {
        if (atmFailedOnce) return false;
        try {
            if (atmService == null) {
                Class<?> sm = Class.forName("android.os.ServiceManager");
                android.os.IBinder b = (android.os.IBinder) sm.getMethod("getService", String.class).invoke(null, "activity_task");
                Class<?> stub = Class.forName("android.app.IActivityTaskManager$Stub");
                atmService = stub.getMethod("asInterface", android.os.IBinder.class).invoke(null, b);
            }
            Object ok = atmService.getClass().getMethod("removeTask", int.class).invoke(atmService, id);
            return !(ok instanceof Boolean) || ((Boolean) ok).booleanValue();
        } catch (Throwable t) {
            atmFailedOnce = true;
            System.out.println("tj-shell: removeTask unavailable (" + t + ")");
            return false;
        }
    }

    /** Runs one FIXED shell line (never built from caller input) and returns its output, capped at 60 KB and cut off after the timeout. */
    private static String runShell(String sh, long timeoutMs) {
        try {
            Process pr = new ProcessBuilder("/system/bin/sh", "-c", sh).redirectErrorStream(true).start();
            final StringBuilder sb = new StringBuilder();
            final BufferedReader r = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            Thread t = new Thread(new Runnable() { @Override public void run() {
                try { String l; while ((l = r.readLine()) != null) { if (sb.length() < 60_000) sb.append(l).append('\n'); } } catch (Throwable ignored) { }
            } });
            t.start();
            t.join(timeoutMs);
            if (t.isAlive()) { pr.destroy(); sb.append("(cut off after ").append(timeoutMs / 1000).append(" s)\n"); }
            return sb.toString();
        } catch (Throwable e) { return "(failed: " + e + ")\n"; }
    }

    private static Object amService;
    private static boolean binderFailedOnce = false;

    /** Force-stops through the activity manager's binder (no new JVM per app: cheap on a low-RAM tablet). The shell user holds FORCE_STOP_PACKAGES. False if the hidden API is not callable. */
    private static synchronized boolean viaBinder(String pkg) {
        if (binderFailedOnce) return false;
        try {
            if (amService == null) {
                Class<?> sm = Class.forName("android.os.ServiceManager");
                android.os.IBinder b = (android.os.IBinder) sm.getMethod("getService", String.class).invoke(null, "activity");
                Class<?> stub = Class.forName("android.app.IActivityManager$Stub");
                amService = stub.getMethod("asInterface", android.os.IBinder.class).invoke(null, b);
            }
            amService.getClass().getMethod("forceStopPackage", String.class, int.class).invoke(amService, pkg, 0);
            return true;
        } catch (Throwable t) {
            binderFailedOnce = true;
            System.out.println("tj-shell: binder force-stop unavailable, using `am` (" + t + ")");
            return false;
        }
    }

    /** One package at a time (a low-RAM tablet must not start several JVMs at once): the binder call first, `am force-stop` as the fallback. */
    private static int forceStop(List<String> pkgs) {
        int done = 0;
        for (String p : pkgs) {
            try {
                if (viaBinder(p)) { done++; continue; }
                Process pr = new ProcessBuilder("/system/bin/am", "force-stop", p).redirectErrorStream(true).start();
                pr.getInputStream().close();
                if (pr.waitFor() == 0) done++;
            } catch (Throwable ignored) { }
        }
        return done;
    }

    private static int lookupUid() {
        try {
            Process pr = new ProcessBuilder("cmd", "package", "list", "packages", "-U", ShellProtocol.SELF).redirectErrorStream(true).start();
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String l;
            while ((l = r.readLine()) != null) sb.append(l).append('\n');
            pr.waitFor();
            return ShellProtocol.parseUid(sb.toString());
        } catch (Throwable t) { return -1; }
    }
}
