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
        LocalServerSocket server;
        try { server = new LocalServerSocket(ShellProtocol.SOCKET); }
        catch (java.io.IOException e) { System.out.println("tj-shell: already running (" + e.getMessage() + ")"); return; }
        System.out.println("tj-shell: up for uid " + appUid);
        while (true) {
            final LocalSocket c = server.accept();
            new Thread(new Runnable() { @Override public void run() { serve(c, appUid); } }, "tj-shell-conn").start();
        }
    }

    private static void serve(LocalSocket c, int appUid) {
        try {
            c.setSoTimeout(5000);
            int peer = c.getPeerCredentials().getUid();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(c.getOutputStream()), true);
            if (peer != appUid) { out.println("ERR denied"); return; }
            BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
            String line = in.readLine();
            if (ShellProtocol.isPing(line)) { out.println("OK tj-shell 1"); return; }
            if (ShellProtocol.isQuit(line)) { out.println("OK"); out.flush(); System.exit(0); return; }
            List<String> pkgs = ShellProtocol.forceStopTargets(line);
            if (pkgs == null) { out.println("ERR unknown command"); return; }
            out.println("OK " + forceStop(pkgs));
        } catch (Throwable t) {
            System.out.println("tj-shell: connection failed: " + t);
        } finally {
            try { c.close(); } catch (Throwable ignored) { }
        }
    }

    /** `am force-stop` for each package, three at a time (each `am` is a small JVM start; the last cost is ~1 s on a slow tablet). */
    private static int forceStop(List<String> pkgs) throws InterruptedException {
        final int[] done = {0};
        List<Thread> ts = new ArrayList<>();
        for (int i = 0; i < pkgs.size(); i += 1) {
            final String p = pkgs.get(i);
            Thread t = new Thread(new Runnable() { @Override public void run() {
                try {
                    Process pr = new ProcessBuilder("/system/bin/am", "force-stop", p).redirectErrorStream(true).start();
                    pr.getInputStream().close();
                    if (pr.waitFor() == 0) synchronized (done) { done[0]++; }
                } catch (Throwable ignored) { }
            } }, "tj-shell-stop");
            t.start(); ts.add(t);
            if (ts.size() >= 3) { ts.remove(0).join(8000); }
        }
        for (Thread t : ts) t.join(8000);
        return done[0];
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
