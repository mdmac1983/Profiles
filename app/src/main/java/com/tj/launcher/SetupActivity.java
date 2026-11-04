package com.tj.launcher;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * S2 (Tj, 2026-11-04: "Make the adb probe an app ... Make it so i can do everything in one session with adb"): the whole adb to-do list as one screen. Each row shows whether it is DONE;
 * "Do everything" applies the missing ones through the shell helper (only ids on ShellProtocol's fixed list), "Collect report" writes the tablet report + copies the system apps
 * (Settings, SystemUI, stock launcher, framework) into the share folder for {@code adb pull}. The only adb line needed first is the one that starts the helper.
 * Opens only in Quickstep mode (like the rest of Launcher control).
 */
public class SetupActivity extends Activity {
    public static final String EXTRA_AUTORUN = "autorun";          // emulator test: do everything + collect, then write probe/autorun-done.txt (only if the pref below is set)
    public static final String PREF_ALLOW_AUTORUN = "allow_setup_autorun";
    private static final int ACCENT = Color.parseColor("#00796B"), INK = Color.parseColor("#202124"), MUTED = Color.parseColor("#5F6368"), BAD = Color.parseColor("#B3261E");

    private LinearLayout page;
    private TextView log;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private float dp;

    /** One adb to-do item: how to see it is done, and which helper job does it (null = nothing to run). */
    private static final class Item {
        final String title, why, job;
        Item(String title, String why, String job) { this.title = title; this.why = why; this.job = job; }
    }

    private final Item[] items = {
        new Item("Permission: change secure settings", "Keyboard switch, tiles per launcher, animation speed-up.", "GRANT_WSS"),
        new Item("Permission: read what is running (DUMP)", "Read-only look at what is open.", "GRANT_DUMP"),
        new Item("Permission: change configuration", "For later features (font size, orientation).", "GRANT_CFG"),
        new Item("Permission: read storage", "Only for the wallpaper-patch power-menu mode and the share folder.", "GRANT_STORAGE"),
        new Item("Usage access", "Which app was used last (read-only).", "OP_USAGE"),
        new Item("Modify system settings", "For later features (brightness, screen timeout).", "OP_WRITE"),
        new Item("Allowed to run in the background", "So Android does not freeze the helper.", "OP_BG"),
        new Item("Allowed to run any time in the background", "Same.", "OP_ANYBG"),
        new Item("Battery: not frozen while the screen is off", "The helper keeps working with the screen off (lock-time clean-up).", "IDLE_WL"),
        new Item("Speed: compile the launcher ahead of time", "Sideloaded apps run half-optimised until Android has time to compile them; this does it now (about a minute) so the launcher starts faster after an unlock. Redo after every update.", "COMPILE_SELF"),
        new Item("Accessibility helper is ON", "Power menu, pencil, lock screen handshakes, return reset.", "A11YON"),
        new Item("Shell helper is running", "Invisible clean-up and everything on this screen. Start it with the adb line below (again after every restart).", null),
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (Mode.isCustom(this)) { finish(); return; }
        dp = getResources().getDisplayMetrics().density;
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Color.WHITE);
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(px(16), px(36), px(16), px(96));
        sv.addView(page, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);
    }

    @Override protected void onResume() {
        super.onResume();
        RecentsGuard.excludeOurTasks(this);
        RecentsGuard.describeAsTarget(this);
        if (Mode.isCustom(this)) { finish(); return; }
        build();
        if (getIntent() != null && getIntent().getBooleanExtra(EXTRA_AUTORUN, false) && Features.get(this, PREF_ALLOW_AUTORUN, false)) {
            getIntent().removeExtra(EXTRA_AUTORUN);
            autorun();
        }
    }

    private int px(int v) { return Math.round(v * dp); }

    private TextView text(String t, int sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(t); v.setTextSize(sp); v.setTextColor(color);
        if (bold) v.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return v;
    }

    // ---------------------------------------------------------------- is it done?
    private boolean done(Item it) {
        try {
            String job = it.job;
            if (job == null) return ShellHelper.reachable();
            PackageManager pm = getPackageManager();
            switch (job) {
                case "GRANT_WSS": return checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED;
                case "GRANT_DUMP": return checkSelfPermission("android.permission.DUMP") == PackageManager.PERMISSION_GRANTED;
                case "GRANT_CFG": return checkSelfPermission("android.permission.CHANGE_CONFIGURATION") == PackageManager.PERMISSION_GRANTED;
                case "GRANT_STORAGE": return checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED;
                case "OP_USAGE": return op("android:get_usage_stats");
                case "OP_WRITE": return android.provider.Settings.System.canWrite(this);
                case "OP_BG": return op("android:run_in_background");
                case "OP_ANYBG": return op("android:run_any_in_background");
                case "IDLE_WL": return ((PowerManager) getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName());
                case "COMPILE_SELF": return Features.getInt(this, "compile_done_code", 0) == getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                case "A11YON": return TjAccessibilityService.get() != null;
                default: return false;
            }
        } catch (Throwable t) { return false; }
    }

    /** Runs one allow-listed job; remembers that the speed compile finished for THIS version (the shell cannot tell us later). */
    private String runJob(String job) {
        String out = ShellHelper.run(job);
        try { if ("COMPILE_SELF".equals(job) && out != null && !out.toLowerCase().contains("error") && !out.toLowerCase().contains("fail"))
            Features.setInt(this, "compile_done_code", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode); } catch (Throwable ignored) { }
        return out;
    }

    private boolean op(String name) {
        AppOpsManager a = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
        return a != null && a.checkOpNoThrow(name, android.os.Process.myUid(), getPackageName()) == AppOpsManager.MODE_ALLOWED;
    }

    // ---------------------------------------------------------------- the page
    private void build() {
        page.removeAllViews();
        page.addView(text("Tablet setup & report", 26, INK, true));
        page.addView(text("Every adb to-do item on one screen. Green = done. The only adb line you ever need first starts the shell helper (below); everything else can be done from here.", 14, MUTED, false));
        boolean helper = ShellHelper.reachable();
        page.addView(text(helper ? "Shell helper: RUNNING" : "Shell helper: NOT running - start it with the adb line (tap Copy), then come back", 15, helper ? ACCENT : BAD, true));
        List<Item> missing = new ArrayList<>();
        for (Item it : items) {
            boolean ok = done(it);
            if (!ok && it.job != null) missing.add(it);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(px(4), px(8), px(4), px(8));
            row.addView(text((ok ? "[done]  " : "[todo]  ") + it.title, 16, ok ? ACCENT : INK, true));
            row.addView(text(it.why, 13, MUTED, false));
            page.addView(row);
        }
        page.addView(button("Copy the helper line for a PHONE / adb app (no 'adb shell')", new Runnable() { public void run() { copy(ShellProtocol.START_DEVICE_LINE); } }));
        page.addView(button("Copy it wrapped in sh -c (for adb apps that do not use a shell)", new Runnable() { public void run() { copy(ShellProtocol.START_DEVICE_SH_C); } }));
        page.addView(button("Copy the helper line for a COMPUTER (adb shell ...)", new Runnable() { public void run() { copy(ShellProtocol.START_ONE_LINER); } }));
        final boolean canRun = helper;
        Button all = button("Do everything that is missing (" + missing.size() + ")", new Runnable() { public void run() { doEverything(); } });
        all.setEnabled(canRun && !missing.isEmpty());
        page.addView(all);
        page.addView(button("Collect the tablet report (small, about a minute)", new Runnable() { public void run() { collect(false); } }));
        Button big = button("Collect EVERYTHING: report + Settings screenshots + system apps (big, 2-3 minutes)", new Runnable() { public void run() { collect(true); } });
        big.setEnabled(canRun);
        page.addView(big);
        Button browser = button("Disable the old Browser app (com.android.browser) - reversible", new Runnable() { public void run() { runJob("BROWSER_OFF"); } });
        browser.setEnabled(canRun);
        page.addView(browser);
        log = text("", 13, MUTED, false);
        log.setPadding(px(4), px(12), px(4), px(12));
        page.addView(log);
        page.addView(text("Everything is published to the folder  Downloads / launcher-probe  on the tablet: open it with any file manager, zip it and send it to me. (From a computer: adb pull " + ShellProtocol.PUBLISH_DIR + " probe-out.) Nothing is sent anywhere by this app.", 12, MUTED, false));
    }

    private Button button(String label, final Runnable r) {
        Button b = new Button(this);
        b.setText(label); b.setAllCaps(false);
        b.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { r.run(); } });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, px(56));
        lp.topMargin = px(10);
        b.setLayoutParams(lp);
        return b;
    }

    private void copy(String s) {
        ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("adb", s));
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
    }

    private void say(final String s) { ui.post(new Runnable() { public void run() { if (log != null) log.append(s + "\n"); } }); }

    private void runJob(final String job) {
        new Thread(new Runnable() { public void run() {
            String out = job.equals("A11YON") ? ShellHelper.a11yOn() : runJob(job);
            say(job + ": " + (out == null ? "FAILED (is the helper running?)" : (out.trim().isEmpty() ? "done" : out.trim())));
            ui.post(new Runnable() { public void run() { build(); } });
        } }, "tj-setup-job").start();
    }

    private void doEverything() {
        final List<Item> todo = new ArrayList<>();
        for (Item it : items) if (it.job != null && !done(it)) todo.add(it);
        new Thread(new Runnable() { public void run() {
            for (Item it : todo) {
                String out = it.job.equals("A11YON") ? ShellHelper.a11yOn() : runJob(it.job);
                say(it.job + ": " + (out == null ? "FAILED" : "done"));
            }
            say("finished: " + todo.size() + " item(s) applied. The list above refreshes.");
            ui.post(new Runnable() { public void run() { build(); } });
        } }, "tj-setup-all").start();
    }

    /** The report (and with {@code everything}: the Settings screenshots and the system apps), published to Downloads/launcher-probe where a file manager can reach it. */
    private void collect(final boolean everything) {
        say("collecting... (the report takes about a minute" + (everything ? ", the screenshots and apps a few more; touch nothing meanwhile" : "") + ")");
        new Thread(new Runnable() { public void run() {
            File f = TabletReport.write(SetupActivity.this);
            say("report written (" + (f.length() / 1024) + " KB)");
            ShellHelper.run("MKDIR_PUBLISH");
            String pub = ShellHelper.run("PUBLISH_REPORT");
            say(pub == null ? "could not publish the report to Downloads (is the shell helper running?): it stays in " + f : "published: " + ShellProtocol.PUBLISH_DIR + "/tablet-report.txt");
            if (everything) {
                String shots = ShellHelper.shots();
                say(shots == null ? "screenshots FAILED (helper not running?)" : "screenshots: " + shots.trim().replace('\n', ' '));
                String apks = ShellHelper.apks();
                say(apks == null ? "system apps FAILED" : "system apps: " + apks.trim().replace('\n', ' '));
            }
            say("DONE. Open Downloads / launcher-probe with a file manager, zip it and send it to me.");
        } }, "tj-setup-report").start();
    }

    /** Emulator test: everything, then the report, then a marker file the test reads. */
    private void autorun() {
        new Thread(new Runnable() { public void run() {
            StringBuilder b = new StringBuilder();
            for (Item it : items) if (it.job != null && !done(it)) {
                String out = it.job.equals("A11YON") ? ShellHelper.a11yOn() : runJob(it.job);
                b.append(it.job).append(out == null ? " FAILED" : " ok").append('\n');
            }
            File f = TabletReport.write(SetupActivity.this);
            b.append("report ").append(f.length()).append(" bytes\n");
            ShellHelper.run("MKDIR_PUBLISH");
            b.append("publish report: ").append(ShellHelper.run("PUBLISH_REPORT") == null ? "FAILED" : "ok").append('\n');
            String shots = ShellHelper.shots();
            b.append("shots: ").append(shots == null ? "FAILED" : shots.trim().replace('\n', ' ')).append('\n');
            String apks = ShellHelper.apks();
            b.append("apks: ").append(apks == null ? "FAILED" : apks.trim().replace('\n', ' ')).append('\n');
            for (Item it : items) b.append("state ").append(it.title).append(": ").append(done(it) ? "done" : "TODO").append('\n');
            try (java.io.FileOutputStream os = new java.io.FileOutputStream(new File(TabletReport.shareDir(SetupActivity.this), "autorun-done.txt"))) { os.write(b.toString().getBytes("UTF-8")); } catch (Throwable ignored) { }
            ui.post(new Runnable() { public void run() { build(); } });
        } }, "tj-setup-autorun").start();
    }
}
