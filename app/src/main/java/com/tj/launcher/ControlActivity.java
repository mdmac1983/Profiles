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
        root.addView(label("Power menu (needs the helper turned on in Accessibility)"));
        root.addView(button("Power menu: leave alone (default)", new Runnable() { public void run() { setPower(Features.POWER_OFF); }}));
        root.addView(button("Power menu: hide Screenshot button", new Runnable() { public void run() { setPower(Features.POWER_HIDE_SCREENSHOT); }}));
        root.addView(button("Power menu: block it completely", new Runnable() { public void run() { setPower(Features.POWER_BLOCK); }}));
        root.addView(button("Open Accessibility settings", new Runnable() { public void run() {
            startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }}));
        root.addView(button("Restore everything", new Runnable() { public void run() {
            RestoreStore.restoreAll(ControlActivity.this);
            Mode.set(ControlActivity.this, Mode.QUICKSTEP);
            refresh();
        }}));
        setContentView(root);
    }

    @Override protected void onResume() { super.onResume(); refresh(); }

    private void setPower(String m) { Features.setString(this, Features.POWER_MODE, m); refresh(); }

    private TextView label(String t) {
        TextView v = new TextView(this);
        v.setText(t); v.setTextColor(Color.DKGRAY); v.setPadding(0, 32, 0, 0);
        return v;
    }

    private void refresh() {
        String s = "Launcher control\n\nMode: " + (Mode.isCustom(this) ? "custom launcher" : "stock launcher (Quickstep)");
        s += "\nPower menu: " + Features.powerMode(this);
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
