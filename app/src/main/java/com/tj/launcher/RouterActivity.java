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
    public static final String HOME_TYPE_KEY = "home_type_launcher";   // B1 switch, default OFF

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
        boolean returnedByDoubleHome = false;
        if (!Mode.isCustom(this) && HomeDoublePress.isDouble(last, now)) {
            CrashGuard.clearTripped(this);
            Mode.set(this, Mode.CUSTOM);
            returnedByDoubleHome = true;
        }
        if (Mode.isCustom(this)) {
            int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;   // V4: also excluded (belt and braces)
            // B1 (switch, default OFF): an IMPLICIT MAIN + SECONDARY_HOME intent (no component) makes Android create a HOME-type task, which is never listed in Recents (not even as the
            // most recent task). COST (found by the crash-breaker emulator test): if the launcher process ever crashes, Android clears our default-home setting
            // (AppErrors -> clearPackagePreferredForHomeActivities) and the next Home press shows the "choose a Home app" dialog. So it is opt-in.
            Intent i = Features.get(this, HOME_TYPE_KEY, false)
                    ? new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_SECONDARY_HOME).setPackage(getPackageName()).addFlags(flags)
                    : new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(this, "com.android.launcher3.Launcher")).addFlags(flags);
            if (TjHooks.launcherWasForeground()) i.putExtra(TjHooks.EXTRA_HOME_WHILE_ON_HOME, true);
            try {
                try {
                    startActivity(i);
                } catch (android.content.ActivityNotFoundException nf) {
                    // the filter is missing (should not happen): fall back to the explicit launch we always used
                    Log.e(TAG, "SECONDARY_HOME launch not resolvable, explicit launch", nf);
                    Intent e = new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(this, "com.android.launcher3.Launcher")).addFlags(flags);
                    if (TjHooks.launcherWasForeground()) e.putExtra(TjHooks.EXTRA_HOME_WHILE_ON_HOME, true);
                    startActivity(e);
                }
                // Tj: coming back by double Home also closes apps, clears notifications, turns Wi-Fi/Bluetooth off and opens Solitaire (ReturnReset).
                if (returnedByDoubleHome) ReturnReset.run(this);
                return;
            } catch (Throwable t) {
                Log.e(TAG, "custom launcher failed to start", t);
                Mode.markFailOpen(this, System.currentTimeMillis() + 20_000);   // the stray-Quickstep guard must not fight this fallback
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
