package com.tj.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * "Launcher control" (D2, Tj 2026-11-04: "I can't tell what is on or off. make toggles and improve the overall settings ui"). Sections, a real on/off Switch for every option
 * (green = on, grey = off), one-line explanations, a status card at the top. Only opens while Quickstep is on (Tj: the settings are part of the "other profile").
 * Plain framework views on purpose: nothing that can fail to inflate on a Go device.
 */
public class ControlActivity extends Activity {
    private static final int ACCENT = Color.parseColor("#00796B"), INK = Color.parseColor("#202124"), MUTED = Color.parseColor("#5F6368"), CARD = Color.parseColor("#F1F3F4");
    private boolean unlockOpen = false;    // the lock-screen method drop-down
    private LinearLayout page;
    private ScrollView scroll;
    private float dp;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Tj: the launcher's settings are only available while the stock launcher (Quickstep) is on ("lock" the user into the custom launcher).
        if (Mode.isCustom(this)) { finish(); return; }
        dp = getResources().getDisplayMetrics().density;
        scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.WHITE);
        scroll.setFillViewport(true);
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(px(16), px(36), px(16), px(96));
        scroll.addView(page, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);
    }

    @Override protected void onResume() {
        super.onResume();
        RecentsGuard.excludeOurTasks(this);
        RecentsGuard.describeAsTarget(this);
        if (Mode.isCustom(this)) { finish(); return; }
        build();
    }

    private int px(int v) { return Math.round(v * dp); }

    /** Rebuilds every row from the stored state (keeps the scroll position). */
    private void rebuild() {
        final int y = scroll.getScrollY();
        build();
        scroll.post(new Runnable() { @Override public void run() { scroll.scrollTo(0, y); } });
    }

    // ---------------------------------------------------------------- the page
    private void build() {
        page.removeAllViews();
        title("Launcher control");

        // status card
        boolean helper = TjAccessibilityService.get() != null;
        boolean grant = AnimationSpeedup.permitted(this);
        LinearLayout card = card();
        card.addView(chipLine("Active launcher", "Quickstep (stock)  -  settings only open here", ACCENT));
        card.addView(chipLine("Accessibility helper", helper ? "ON" : "OFF  -  turn it on in Accessibility settings (power menu, reset, lock screen, pencil need it)", helper ? ACCENT : Color.parseColor("#B3261E")));
        card.addView(chipLine("adb permission", grant ? "granted" : "not granted  -  keyboard / tiles / faster reset wait for it (see adb guide below)", grant ? ACCENT : Color.parseColor("#B26A00")));
        page.addView(card);

        section("Launchers");
        action("Switch to the custom launcher", "Also closes the apps you opened from Quickstep.", new Runnable() { public void run() {
            CrashGuard.clearTripped(ControlActivity.this);
            Mode.set(ControlActivity.this, Mode.CUSTOM);
            goHome();
            ReturnReset.run(ControlActivity.this);
        }});
        action("Choose the apps shown in the custom launcher", "Everything else stays hidden there (new installs start hidden).", new Runnable() { public void run() {
            startActivity(new Intent(ControlActivity.this, HiddenAppsActivity.class));
        }});
        toggle("Keep Quickstep away unless I use a handshake", "Quickstep opens ONLY after a secret unlock handshake (Lock screen section). If its home screen appears any other way in custom mode, send me back.", Features.get(this, StrayHomeGuard.KEY, true), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, StrayHomeGuard.KEY, v); } });

        section("Double-Home return  (Quickstep to custom)");
        toggle("Run the return reset", "Master switch for everything in this section.", ReturnReset.enabled(this), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, ReturnReset.KEY, v); } });
        toggle("Open Solitaire when the launcher starts", "Solitaire in front after boot or a launcher restart.", Features.get(this, TjHooks.STARTUP_KEY, true), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, TjHooks.STARTUP_KEY, v); } });
        final EditText appBox = new EditText(this);
        appBox.setSingleLine(true);
        appBox.setText(ReturnReset.appLabel(this));
        appBox.setHint("app to open (blank = none, plain home screen)");
        page.addView(appBox);
        LinearLayout abtn = row();
        abtn.addView(smallButton("Save app name", new Runnable() { public void run() { Features.setString(ControlActivity.this, ReturnReset.APP_KEY, appBox.getText().toString().trim()); rebuild(); } }));
        abtn.addView(smallButton("Plain (no app)", new Runnable() { public void run() { Features.setString(ControlActivity.this, ReturnReset.APP_KEY, ""); rebuild(); } }));
        page.addView(abtn);
        toggle("Clear notifications", "Skipped by itself when the status bar shows none.", ReturnReset.clearNotifications(this), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, ReturnReset.CLEAR_NOTIF_KEY, v); } });
        toggle("Close the apps opened from Quickstep", "Only those apps; apps opened from the custom launcher stay.", ReturnReset.clearRecents(this), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, ReturnReset.CLEAR_RECENTS_KEY, v); } });
        toggle("Black cover while it works", "Hides the shade / Recents flicker.", Features.get(this, ReturnReset.COVER_KEY, true), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, ReturnReset.COVER_KEY, v); } });
        toggle("Animations off during the reset", AnimationSpeedup.permitted(this) ? "Put back exactly as they were afterwards." : "Needs the adb permission (not granted yet).", AnimationSpeedup.enabled(this), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, AnimationSpeedup.KEY, v); } });
        note("Last reset:\n" + Features.getString(this, TjAccessibilityService.RESET_REPORT_KEY, "(not run yet)"));

        section("Recents");
        toggle("Hide the launcher from Recents completely", "Home-type launcher: never listed. If the launcher ever crashes Android asks you to choose the Home app again (adb guide, step 6, fixes it).", Features.get(this, RouterActivity.HOME_TYPE_KEY, true), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, RouterActivity.HOME_TYPE_KEY, v); } });
        toggle("Keep apps from the custom launcher OUT of Recents", "Off = they appear in Recents like normal apps. On = only Solitaire is listed.", RecentsPolicy.enabled(this), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, RecentsPolicy.KEY, v); } });

        section("Power menu");
        final String mode = Features.powerMode(this);
        radio("When you hold the side key in the custom launcher", new String[] { "Leave it alone", "Cover it with my two-button card (no Screenshot)", "Hide it under a wallpaper patch + my card (home screen only)", "Block it completely" },
                new String[] { Features.POWER_OFF, Features.POWER_HIDE_SCREENSHOT, Features.POWER_WALLPAPER, Features.POWER_BLOCK }, mode, new Str() { public void on(String v) { Features.setString(ControlActivity.this, Features.POWER_MODE, v); } });
        action("Allow wallpaper access", "Needed by the wallpaper patch.", new Runnable() { public void run() { requestPermissions(new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE }, 7); } });
        toggle("Lock screen: patch of my lock wallpaper + stock-size two-button menu", "On the lock screen only, while the custom launcher is active and the cover is on: a piece of your LOCK-screen wallpaper hides the real menu and my Power off / Restart menu has the stock item size (the stock menu minus Screenshot). The patch is invisible until the menu opens. Needs the wallpaper permission; if it looks off, use the nudges below.",
                Features.get(this, TjAccessibilityService.LOCK_PATCH_KEY, true), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, TjAccessibilityService.LOCK_PATCH_KEY, v); rebuild(); } });
        if (Features.POWER_WALLPAPER.equals(mode) || Features.get(this, TjAccessibilityService.LOCK_PATCH_KEY, true) && Features.isCover(mode)) {
            note("Wallpaper patch: " + Features.getString(this, TjAccessibilityService.PATCH_STATUS_KEY, "(not reported yet)"));
            note("Lock-screen patch alignment: " + Features.getString(this, TjAccessibilityService.CAL_STATUS_KEY, "(lock the tablet once with the adb helper running; it photographs the lock screen and finds the right part of the wallpaper by itself)"));
            action("Re-calibrate the lock-screen patch", "Forgets the measured alignment; it is measured again the next time the lock screen shows (needs the adb helper).", new Runnable() { public void run() { Features.setString(ControlActivity.this, TjAccessibilityService.LOCK_CAL_KEY, ""); Features.setString(ControlActivity.this, TjAccessibilityService.CAL_STATUS_KEY, "(will be measured at the next lock)"); rebuild(); } });
            stepper("Patch left / right", TjAccessibilityService.PATCH_DX_KEY, 2, -200, 200, " px");
            stepper("Patch up / down", TjAccessibilityService.PATCH_DY_KEY, 2, -200, 200, " px");
            stepper("Patch size", TjAccessibilityService.PATCH_SCALE_KEY, 1, -30, 30, " %");
        }
        note("Last unlock: " + Features.getString(this, TjAccessibilityService.UNLOCK_SPEED_KEY, "(unlock once to measure)"));
        note("Speed of the last menu: " + Features.getString(this, TjAccessibilityService.LATENCY_KEY, "(open the menu once)"));
        action("Open Accessibility settings", "To turn the helper on or off.", new Runnable() { public void run() { startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)); } });

        section("Lock screen");
        toggle("Show my number at the bottom", "Disappears when you pull up the PIN pad.", LockNumber.enabled(this), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, LockNumber.KEY, v); } });
        final EditText numberBox = new EditText(this);
        numberBox.setSingleLine(true);
        numberBox.setText(LockNumber.text(this));
        numberBox.setHint("lock-screen number");
        page.addView(numberBox);
        LinearLayout nbtns = row();
        nbtns.addView(smallButton("Save number", new Runnable() { public void run() { Features.setString(ControlActivity.this, LockNumber.TEXT_KEY, LockNumber.sanitize(numberBox.getText().toString())); rebuild(); } }));
        page.addView(nbtns);

        section("Unlock into Quickstep (secret handshakes)");
        note("The custom launcher is the default: a plain PIN or fingerprint always opens it. Quickstep opens only after one of the handshakes you SELECT below, done on the lock screen BEFORE the PIN. Select as many as you like; at least one always stays selected, so you can never lock yourself out. Which digits you type is never read.");
        final UnlockMethods.Enabled stored = new UnlockMethods.Enabled() { @Override public boolean on(UnlockMethods.Method m) { return Features.get(ControlActivity.this, m.prefKey(), m.defaultOn); } };
        final java.util.List<UnlockMethods.Method> chosen = UnlockMethods.selected(stored);
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < chosen.size(); i++) { if (i == 2 && chosen.size() > 3) { names.append(", +").append(chosen.size() - 2).append(" more"); break; } if (i > 0) names.append(", "); names.append(chosen.get(i).title); }
        // the drop-down: a field that opens / closes the list of methods
        LinearLayout dd = row();
        dd.setBackground(fieldBackground());
        LinearLayout ddText = new LinearLayout(this);
        ddText.setOrientation(LinearLayout.VERTICAL);
        ddText.addView(text("Lock-screen methods: " + chosen.size() + " of " + UnlockMethods.Method.values().length + " selected", 16, INK, true));
        ddText.addView(text(names.toString(), 13, MUTED, false));
        dd.addView(ddText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        dd.addView(text(unlockOpen ? "^" : "v", 22, ACCENT, true));
        dd.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { unlockOpen = !unlockOpen; rebuild(); } });
        page.addView(dd);
        if (unlockOpen) {
            for (final UnlockMethods.Method m : UnlockMethods.Method.values()) {
                boolean on = chosen.contains(m);
                toggle(m.title, m.how, on, new Chg() { public void on(boolean v) {
                    Features.set(ControlActivity.this, m.prefKey(), v);
                    if (!v) {
                        boolean any = false;
                        for (UnlockMethods.Method x : UnlockMethods.Method.values()) if (stored.on(x)) any = true;
                        if (!any) {   // the last one was just switched off: put it back
                            Features.set(ControlActivity.this, m.prefKey(), true);
                            android.widget.Toast.makeText(ControlActivity.this, "At least one method must stay selected", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
                } });
            }
        }
        toggle("Buzz when a handshake is accepted", "A 40 ms vibration so you know it counted.", ProfileSession.buzz(this), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, ProfileSession.BUZZ_KEY, v); } });
        note("The volume / Back methods need the helper's key-event access (it asks for it only while one of them is selected; turn the helper off and on once after selecting one). Shake and knock read the accelerometer only while the lock screen is showing.");
        note("Last unlock: " + Features.getString(this, UnlockPick.LAST_KEY, "(none yet)") + "\nLast lock-screen switch: " + Features.getString(this, ProfileSession.PREARM_REPORT_KEY, "(none yet)"));

        section("Two profiles (custom launcher / Quickstep)");
        toggle("Locking the tablet ends a Quickstep session", "Like logging out: after the tablet locks, a plain PIN gives you the custom launcher again and closes what Quickstep left open.", ProfileSession.endOnLock(this), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, ProfileSession.END_ON_LOCK_KEY, v); } });
        toggle("Close Quickstep's apps and notifications the moment the tablet locks", "Needs the adb helper (Invisible clean-up below): apps, their notifications and their Recents cards are closed at lock, and the custom account is already active when you unlock, so the unlock has nothing left to do. Without the helper the clean-up happens at unlock under a black cover.", ProfileSession.preClean(this), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, ProfileSession.PRECLEAN_KEY, v); } });
        toggle("Switch behind the lock screen", "On waking to the lock screen, Solitaire and the custom home are put in front behind it, so the unlock opens them at once with no delay. (A handshake puts Quickstep there instead.)", ProfileSession.prearm(this), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, ProfileSession.PREARM_KEY, v); } });
        toggle("No Wi-Fi or Bluetooth in the custom launcher", "If something turns them on while the custom launcher is active they are switched off again. Quickstep keeps whatever you set.", ProfileSession.radiosOff(this), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, ProfileSession.RADIOS_KEY, v); } });
        note("Safety: if the Accessibility helper is OFF no handshake can work, so three quick Home presses in the custom launcher let you out to Quickstep. With the helper ON that does nothing.");

        section("Speed");
        toggle("Keep the launcher in memory (faster unlock)", "A do-nothing foreground service stops Android from closing the launcher while the tablet sleeps, so an unlock never has to start it again. Cost: it stays in memory and ONE silent notification (\"Launcher is kept ready\", no status-bar icon, hidden on the lock screen) sits at the bottom of the notification shade. Off = Android may close the launcher when memory is short.", KeepAliveService.enabled(this), new Chg() { public void on(boolean v) {
            Features.set(ControlActivity.this, KeepAlivePolicy.KEY, v);
            KeepAliveService.ensure(ControlActivity.this);
        } });
        note("Keep-alive now: " + (KeepAliveService.running() ? "RUNNING (the launcher process is a foreground service)" : "not running") + ".  Last unlock: " + Features.getString(this, UnlockPick.LAST_KEY, "(none yet)"));

        section("Invisible clean-up (needs adb after every restart)");
        final boolean helperUp = ShellHelper.reachable();
        LinearLayout hc = card();
        hc.addView(chipLine("Shell helper", helperUp ? "RUNNING - the clean-up is invisible" : "not running - the black-cover method is used instead", helperUp ? ACCENT : Color.parseColor("#B26A00")));
        page.addView(hc);
        toggle("Use the invisible clean-up when the helper is running", "Quickstep's apps are force-stopped behind Solitaire: their Recents cards, notifications and processes vanish with no black cover, no notification shade and no Recents screen. Off = always the cover method.", ShellHelper.enabled(this), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, ShellHelper.KEY, v); } });
        action("Copy the adb command that starts the helper", "Run it on the computer with the tablet plugged in. It lasts until the tablet restarts, then run it again. (Two-step version for Windows: adb guide, step 10.)", new Runnable() { public void run() {
            ((android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(android.content.ClipData.newPlainText("adb", ShellProtocol.START_ONE_LINER));
            android.widget.Toast.makeText(ControlActivity.this, "Copied", android.widget.Toast.LENGTH_SHORT).show();
        }});
        note("Link: a local-only connection (127.0.0.1). The launcher declares the Internet permission only so Android lets it open that connection; nothing else in the launcher uses it.");
        note("The helper is a small process the adb command starts as the shell user. It only answers this app, and only knows two commands: \"are you there\" and \"force-stop these apps\" (never the system, the keyboard, this launcher or the stock launcher). Stop it any time with: " + ShellProtocol.STOP_LINE);

        section("Back to the custom launcher (from Quickstep)");
        note("Any one of these ends the Quickstep session: it switches to the custom launcher, closes what Quickstep opened, clears notifications, turns Wi-Fi/Bluetooth off and opens Solitaire. They only work while Quickstep is active. Locking the tablet also ends it.");
        for (final ReturnMethods.Method m : ReturnMethods.Method.values()) {
            if (m == ReturnMethods.Method.HOME2) continue;   // the router reads Home presses itself: shown below
            toggle(m.title, m.how, ReturnMethods.on(this, m), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, m.prefKey(), v); } });
        }
        toggle(ReturnMethods.Method.HOME2.title, ReturnMethods.Method.HOME2.how + "  (This one is always on.)", true, new Chg() { public void on(boolean v) { rebuild(); } });
        note("The \"Back to custom\" icon is in Quickstep's app drawer (arrow icon). Put it on the home screen if you like. The tile is added from the tile editor (pencil) - or tick it under Quick settings below.");

        section("Master failsafe: always reach Quickstep");
        note("Ladder, easiest first: 1) the lock-screen handshakes above; 2) the key code below; 3) the hidden Solitaire hotspot below; 4) if the Accessibility helper is OFF: press Home three times quickly; 5) Android itself: Settings > Apps > Default apps > Home app, or Safe mode (hold Power off), or adb (see the adb guide).");
        toggle("Key code anywhere in the custom launcher", "Volume Up, Up, Down, Down, Back, Back, Up, Down within 20 seconds, screen unlocked. Needs the helper's key access.", FailsafeKeys.konami(this), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, FailsafeKeys.KONAMI_KEY, v); } });
        toggle("Hidden Solitaire hotspot", "An invisible 56 dp square in a corner of Solitaire: tap it 5 times quickly, then touch and HOLD it for a second. The game cannot use that corner while this is on.", FailsafeKeys.hotspot(this), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, FailsafeKeys.HOTSPOT_KEY, v); } });
        radio("Hotspot corner", FailsafeKeys.CORNERS, new String[] { "0", "1", "2", "3" }, String.valueOf(FailsafeKeys.corner(this)), new Str() { public void on(String v) { Features.setInt(ControlActivity.this, FailsafeKeys.CORNER_KEY, Integer.parseInt(v)); } });
        action("Probe Solitaire: what can the helper read?", "Starts a 10-minute probe. Open Solitaire, move a few cards, come back here. Tells us whether a CARD-MOVE code is possible.", new Runnable() { public void run() {
            Features.setString(ControlActivity.this, FailsafeKeys.PROBE_UNTIL_KEY, String.valueOf(System.currentTimeMillis() + 600_000));
            Features.setString(ControlActivity.this, FailsafeKeys.PROBE_REPORT_KEY, "(probe running: open Solitaire and move some cards)");
            rebuild();
        }});
        note("Probe result: " + Features.getString(this, FailsafeKeys.PROBE_REPORT_KEY, "(not run)") + "\nLast failsafe / return: " + Features.getString(this, FailsafeKeys.LAST_KEY, "(none yet)"));

        section("Quick settings");
        toggle("Hide and disable the pencil (Edit)", "Custom launcher only: a white cover over the pencil while the panel is open. Quickstep never has it.", Features.get(this, TjAccessibilityService.PEN_KEY, true), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, TjAccessibilityService.PEN_KEY, v); } });
        toggle("Hide the pencil AND the gear natively (experimental)", DemoModeProfile.permitted(this) ? "No cover, no delay: Android's own panel is drawn without them while the custom launcher is active (uses the system's demo-mode flag, put back for Quickstep). Try the single adb line in research/ADB_TODO.md first. Needs the adb permission." : "Needs the adb permission (not granted yet).", DemoModeProfile.enabled(this), new Chg() { public void on(boolean v) {
            Features.set(ControlActivity.this, DemoModeProfile.KEY, v);
            DemoModeProfile.apply(ControlActivity.this, Mode.get(ControlActivity.this));
        } });
        toggle("Different tiles for each launcher", TileProfile.permitted(this) ? "Applied the moment you switch launcher." : "Needs the adb permission (not granted yet).", TileProfile.enabled(this), new Chg() { public void on(boolean v) { Features.set(ControlActivity.this, TileProfile.KEY, v); } });
        tileChooser("Tiles in the custom launcher", TileProfile.CUSTOM_KEY, TileProfile.DEFAULT_CUSTOM);
        tileChooser("Tiles in Quickstep", TileProfile.QUICKSTEP_KEY, TileProfile.DEFAULT_QUICKSTEP);

        section("Keyboard");
        toggle("Stock keyboard in custom, SwiftKey in Quickstep", KeyboardSwitch.status(this), KeyboardSwitch.enabled(this), new Chg() { public void on(boolean v) {
            Features.set(ControlActivity.this, KeyboardSwitch.KEY, v);
            KeyboardSwitch.apply(ControlActivity.this, Mode.get(ControlActivity.this));
        } });

        section("Advanced");
        action("Tablet setup & report", "Every adb to-do item on one screen (permissions, battery, accessibility, helper), a \"Do everything\" button, and a report + copies of the system apps for Claude.", new Runnable() { public void run() { startActivity(new Intent(ControlActivity.this, SetupActivity.class)); } });
        action("adb setup guide", "Every command, step by step, with copy buttons.", new Runnable() { public void run() { startActivity(new Intent(ControlActivity.this, AdbGuideActivity.class)); } });
        action("Restore everything", "Turns every change of the launcher off and puts the settings back as they were.", new Runnable() { public void run() {
            new AlertDialog.Builder(ControlActivity.this).setTitle("Restore everything?").setMessage("Every launcher feature is switched off and the original system settings are put back. You stay in Quickstep.")
                    .setPositiveButton("Restore", new DialogInterface.OnClickListener() { public void onClick(DialogInterface d, int w) {
                        RestoreStore.restoreAll(ControlActivity.this);
                        Mode.set(ControlActivity.this, Mode.QUICKSTEP);
                        rebuild();
                    } }).setNegativeButton("Cancel", null).show();
        }});
        note("Stray stock home screens sent back: " + Features.getString(this, StrayHomeGuard.COUNT_KEY, "0") + " (last " + Features.getString(this, StrayHomeGuard.LAST_KEY, "never") + ")"

                + "\nWill open: " + describeTarget());
        if (CrashGuard.wasTripped(this)) note("The launcher crashed 3 times in a row, so it switched itself back to the stock launcher and restored everything.");
    }

    private String describeTarget() {
        String app = ReturnReset.findAppLabel(this);
        return app == null ? "(no app matching '" + ReturnReset.appLabel(this) + "' found)" : app;
    }

    // ---------------------------------------------------------------- small UI kit
    private interface Chg { void on(boolean v); }
    private interface Str { void on(String v); }

    private TextView text(String t, int sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(t); v.setTextSize(sp); v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT_BOLD);
        return v;
    }

    private void title(String t) {
        TextView v = text(t, 26, INK, true);
        v.setPadding(px(4), 0, 0, px(12));
        page.addView(v);
    }

    private void section(String t) {
        TextView v = text(t.toUpperCase(java.util.Locale.ROOT), 12, ACCENT, true);
        v.setLetterSpacing(0.08f);
        v.setPadding(px(4), px(26), 0, px(6));
        page.addView(v);
        View line = new View(this);
        line.setBackgroundColor(Color.parseColor("#DADCE0"));
        page.addView(line, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
    }

    private GradientDrawable fieldBackground() {
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE); g.setStroke(px(2), ACCENT); g.setCornerRadius(px(8));
        return g;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable g = new GradientDrawable();
        g.setColor(CARD); g.setCornerRadius(px(12));
        c.setBackground(g);
        c.setPadding(px(14), px(10), px(14), px(10));
        return c;
    }

    private View chipLine(String label, String value, int color) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(0, px(6), 0, px(6));
        l.addView(text(label, 12, MUTED, false));
        l.addView(text(value, 15, color, true));
        return l;
    }

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(px(4), px(10), px(4), px(10));
        return r;
    }

    private void toggle(String title, String desc, boolean on, final Chg c) {
        LinearLayout r = row();
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(text(title, 16, INK, false));
        TextView d = text(desc, 13, MUTED, false);
        left.addView(d);
        r.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        final Switch sw = new Switch(this);
        sw.setChecked(on);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean isChecked) { c.on(isChecked); rebuild(); }
        });
        r.addView(sw);
        r.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { sw.toggle(); } });
        page.addView(r);
    }

    private void action(String title, String desc, final Runnable run) {
        LinearLayout r = row();
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(text(title, 16, ACCENT, true));
        left.addView(text(desc, 13, MUTED, false));
        r.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        r.addView(text(">", 20, ACCENT, true));
        r.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { run.run(); } });
        page.addView(r);
    }

    private void note(String t) {
        TextView v = text(t, 13, MUTED, false);
        v.setPadding(px(4), px(8), px(4), px(8));
        page.addView(v);
    }

    private Button smallButton(String label, final Runnable r) {
        Button b = new Button(this);
        b.setText(label); b.setAllCaps(false);
        b.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { r.run(); } });
        return b;
    }

    private void radio(String title, String[] labels, final String[] values, String current, final Str s) {
        page.addView(text(title, 14, MUTED, false));
        RadioGroup g = new RadioGroup(this);
        g.setOrientation(RadioGroup.VERTICAL);
        for (int i = 0; i < labels.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(labels[i]); rb.setTextSize(15); rb.setTextColor(INK);
            rb.setId(1000 + i);
            rb.setPadding(px(6), px(8), 0, px(8));
            g.addView(rb);
            if (values[i].equals(current)) rb.setChecked(true);
        }
        g.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup group, int id) { int i = id - 1000; if (i >= 0 && i < values.length) { s.on(values[i]); rebuild(); } }
        });
        page.addView(g);
    }

    private void stepper(String label, final String key, final int step, final int min, final int max, final String unit) {
        LinearLayout r = row();
        r.addView(text(label + ":  " + Features.getInt(this, key, 0) + unit, 15, INK, false), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        r.addView(smallButton("-", new Runnable() { public void run() { Features.setInt(ControlActivity.this, key, Math.max(min, Features.getInt(ControlActivity.this, key, 0) - step)); rebuild(); } }));
        r.addView(smallButton("+", new Runnable() { public void run() { Features.setInt(ControlActivity.this, key, Math.min(max, Features.getInt(ControlActivity.this, key, 0) + step)); rebuild(); } }));
        page.addView(r);
    }

    /** A row of tick boxes for the quick-settings tiles of one launcher; stored as the comma list SystemUI wants. */
    private void tileChooser(String title, final String key, String def) {
        page.addView(text(title, 14, MUTED, false));
        String cur = TileProfile.sanitize(Features.getString(this, key, def), def);
        final Set<String> on = new HashSet<>(Arrays.asList(cur.split(",")));
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        LinearLayout line = null;
        for (int i = 0; i < TileProfile.CHOICES.length; i++) {
            if (i % 3 == 0) { line = new LinearLayout(this); line.setOrientation(LinearLayout.HORIZONTAL); wrap.addView(line); }
            final String t = TileProfile.CHOICES[i];
            CheckBox cb = new CheckBox(this);
            cb.setText(t); cb.setTextSize(14);
            cb.setChecked(on.contains(t));
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override public void onCheckedChanged(CompoundButton b, boolean isChecked) {
                    List<String> out = new ArrayList<>();
                    String now = TileProfile.sanitize(Features.getString(ControlActivity.this, key, "rotation"), "rotation");
                    Set<String> sel = new HashSet<>(Arrays.asList(now.split(",")));
                    if (isChecked) sel.add(t); else sel.remove(t);
                    for (String c : TileProfile.CHOICES) if (sel.contains(c)) out.add(c);
                    for (String c : sel) if (!out.contains(c)) out.add(c);   // keep any custom spec that is not in the list
                    Features.setString(ControlActivity.this, key, out.isEmpty() ? "rotation" : android.text.TextUtils.join(",", out));
                    TileProfile.apply(ControlActivity.this, Mode.get(ControlActivity.this));
                }
            });
            line.addView(cb, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        page.addView(wrap);
    }

    private void goHome() {
        startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setPackage(getPackageName())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
