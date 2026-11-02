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
        root.addView(button("Restore everything", new Runnable() { public void run() {
            RestoreStore.restoreAll(ControlActivity.this);
            Mode.set(ControlActivity.this, Mode.QUICKSTEP);
            refresh();
        }}));
        setContentView(root);
    }

    @Override protected void onResume() { super.onResume(); refresh(); }

    private void refresh() {
        String s = "Launcher control\n\nMode: " + (Mode.isCustom(this) ? "custom launcher" : "stock launcher (Quickstep)");
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
