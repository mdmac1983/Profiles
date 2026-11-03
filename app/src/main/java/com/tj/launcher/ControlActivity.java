package com.tj.launcher;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;

/**
 * "Launcher control": the app icon that stays reachable from the stock launcher, so Tj can always switch back to the custom launcher or
 * restore everything without any gesture or adb. Plain framework views (tiny, nothing that can fail to inflate).
 */
public class ControlActivity extends Activity {
    private TextView status;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Tj: the launcher's settings are only available while the stock launcher (Quickstep) is on ("lock" the user into the custom launcher).
        if (Mode.isCustom(this)) { finish(); return; }
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(48, 96, 48, 48);
        root.setBackgroundColor(Color.WHITE);
        status = new TextView(this);
        status.setTextSize(18);
        status.setTextColor(Color.BLACK);
        root.addView(status);
        root.addView(button("Use custom launcher", new Runnable() { public void run() {
            CrashGuard.clearTripped(ControlActivity.this);
            Mode.set(ControlActivity.this, Mode.CUSTOM);
            goHome();
        }}));
        root.addView(button("Use stock launcher (Quickstep)", new Runnable() { public void run() {
            Mode.set(ControlActivity.this, Mode.QUICKSTEP);
            goHome();
        }}));
        root.addView(button("Choose apps shown in the custom launcher", new Runnable() { public void run() {
            startActivity(new Intent(ControlActivity.this, HiddenAppsActivity.class));
        }}));
        root.addView(label("Power menu (needs the helper turned on in Accessibility)"));
        root.addView(button("Power menu: leave alone (default)", new Runnable() { public void run() { setPower(Features.POWER_OFF); }}));
        root.addView(button("Power menu: remove Screenshot (two-button menu)", new Runnable() { public void run() { setPower(Features.POWER_HIDE_SCREENSHOT); }}));
        root.addView(button("Power menu: block it completely", new Runnable() { public void run() { setPower(Features.POWER_BLOCK); }}));
        root.addView(button("Open Accessibility settings", new Runnable() { public void run() {
            startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }}));
        root.addView(label("Return reset (when you double-press Home to come back)"));
        root.addView(button("Return reset: ON / OFF (tap to toggle)", new Runnable() { public void run() {
            Features.set(ControlActivity.this, ReturnReset.KEY, !ReturnReset.enabled(ControlActivity.this)); refresh();
        }}));
        root.addView(label("Keyboard (needs a one-time adb grant)"));
        root.addView(button("Keyboard: stock in custom launcher, SwiftKey in Quickstep: ON / OFF", new Runnable() { public void run() {
            Features.set(ControlActivity.this, KeyboardSwitch.KEY, !KeyboardSwitch.enabled(ControlActivity.this));
            KeyboardSwitch.apply(ControlActivity.this, Mode.get(ControlActivity.this)); refresh();
        }}));
        root.addView(button("Open the launcher at start (Solitaire in front): ON / OFF", new Runnable() { public void run() {
            Features.set(ControlActivity.this, TjHooks.STARTUP_KEY, !Features.get(ControlActivity.this, TjHooks.STARTUP_KEY, true)); refresh();
        }}));
        root.addView(label("Method A - keep Recents clean"));
        root.addView(button("A: Recents shows only Solitaire (launch flag): ON / OFF", new Runnable() { public void run() {
            Features.set(ControlActivity.this, RecentsPolicy.KEY, !RecentsPolicy.enabled(ControlActivity.this)); refresh();
        }}));
        root.addView(label("Method B - press the real Clear all buttons on return (needs the accessibility helper)"));
        root.addView(button("B1: clear notifications (shade Clear all): ON / OFF", new Runnable() { public void run() {
            Features.set(ControlActivity.this, ReturnReset.CLEAR_NOTIF_KEY, !ReturnReset.clearNotifications(ControlActivity.this)); refresh();
        }}));
        root.addView(button("B2: clear Recents (Recents Clear all, closes apps): ON / OFF", new Runnable() { public void run() {
            Features.set(ControlActivity.this, ReturnReset.CLEAR_RECENTS_KEY, !ReturnReset.clearRecents(ControlActivity.this)); refresh();
        }}));
        root.addView(button("Restore everything", new Runnable() { public void run() {
            RestoreStore.restoreAll(ControlActivity.this);
            Mode.set(ControlActivity.this, Mode.QUICKSTEP);
            refresh();
        }}));
        setContentView(root);
    }

    @Override protected void onResume() { super.onResume(); RecentsGuard.excludeOurTasks(this); if (Mode.isCustom(this)) { finish(); return; } refresh(); }

    private void setPower(String m) { Features.setString(this, Features.POWER_MODE, m); refresh(); }

    private TextView label(String t) {
        TextView v = new TextView(this);
        v.setText(t); v.setTextColor(Color.DKGRAY); v.setPadding(0, 32, 0, 0);
        return v;
    }

    private void refresh() {
        String s = "Launcher control\n\nMode: " + (Mode.isCustom(this) ? "custom launcher" : "stock launcher (Quickstep)");
        s += "\nPower menu: " + Features.powerMode(this);
        s += "\nA (launch flag) " + (RecentsPolicy.enabled(this) ? "ON" : "OFF") + " | B1 notifications " + (ReturnReset.clearNotifications(this) ? "ON" : "OFF") + " | B2 recents " + (ReturnReset.clearRecents(this) ? "ON" : "OFF");
        s += "\nRecents shows only the target app: " + (RecentsPolicy.enabled(this) ? "ON (other apps started from the custom launcher stay out of Recents)" : "OFF");
        s += "\nKeyboard switch: " + (KeyboardSwitch.enabled(this) ? "ON" : "OFF") + " | adb grant: " + (KeyboardSwitch.permitted(this) ? "yes" : "NO") + " | " + KeyboardSwitch.status(this);
        s += "\nSolitaire at launcher start: " + (Features.get(this, TjHooks.STARTUP_KEY, true) ? "ON" : "OFF");
        String app = ReturnReset.findAppLabel(this);
        s += "\nReturn reset: " + (ReturnReset.enabled(this) ? "ON" : "OFF")
                + " - will open: " + (app == null ? "(no app matching '" + ReturnReset.appLabel(this) + "' found)" : app)

                + "\nAccessibility helper: " + (TjAccessibilityService.get() != null ? "ON" : "OFF (turn on 'Launcher power-menu helper' in Accessibility: it clears notifications and Recents)")
                + "\nLast reset: " + Features.getString(this, TjAccessibilityService.RESET_REPORT_KEY, "(not run yet)");
        if (CrashGuard.wasTripped(this)) s += "\n\nThe launcher crashed 3 times in a row, so it switched itself back to the stock launcher and restored everything.";
        status.setText(s);
    }

    private Button button(String label, final Runnable r) {
        Button bt = new Button(this);
        bt.setText(label);
        bt.setAllCaps(false);
        bt.setOnClickListener(new android.view.View.OnClickListener() { public void onClick(android.view.View v) { r.run(); } });
        return bt;
    }

    private void goHome() {
        startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setPackage(getPackageName())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
