package com.tj.launcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * The ONLY activity that answers the HOME intent. Invisible: decides at once where Home goes and finishes.
 * CUSTOM -> our Launcher3 (explicit intent). QUICKSTEP (or anything going wrong) -> the stock launcher.
 * Fail-open rule: if our Launcher cannot be started for any reason, the stock launcher is started instead, so
 * pressing Home can never lead to a dead end.
 */
public class RouterActivity extends Activity {
    private static final String TAG = "TjRouter";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        handleHome();
    }

    /** A second Home press that arrives while this invisible instance is still alive is delivered here, not to onCreate: handle it the same way. */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleHome();
    }

    private void handleHome() {
        try {
            route();
        } catch (Throwable t) {
            Log.e(TAG, "route failed, falling back to stock launcher", t);
            try { startStock(); } catch (Throwable ignored) { }
        }
        finish();
        overridePendingTransition(0, 0);
    }

    private void route() {
        // Double-press Home while the stock launcher is active = back to the custom launcher (Tj: "a quick way to go back").
        // This router answers every Home press, so no permission or accessibility is needed for it.
        long now = System.currentTimeMillis();
        long last = Mode.lastHomePressMs(this);
        Mode.recordHomePress(this, now);
        if (!Mode.isCustom(this) && HomeDoublePress.isDouble(last, now)) {
            CrashGuard.clearTripped(this);
            Mode.set(this, Mode.CUSTOM);
        }
        if (Mode.isCustom(this)) {
            Intent i = new Intent(Intent.ACTION_MAIN)
                    .setComponent(new ComponentName(this, "com.android.launcher3.Launcher"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            if (TjHooks.launcherWasForeground()) i.putExtra(TjHooks.EXTRA_HOME_WHILE_ON_HOME, true);
            try {
                startActivity(i);
                return;
            } catch (Throwable t) {
                Log.e(TAG, "custom launcher failed to start", t);
            }
        }
        startStock();
    }

    private void startStock() {
        ComponentName stock = StockLauncher.find(this);
        if (stock == null) { Log.e(TAG, "no stock launcher found"); return; }
        startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setComponent(stock)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
    }
}
