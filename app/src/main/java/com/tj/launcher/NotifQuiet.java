package com.tj.launcher;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

/**
 * BH1 (Tj, 2026-11-08 02:29 UTC: "... Turn all notifications off for both the lock screen and the custom launcher ... should never show any notification at any time ..."). The pop-up banner (heads-up) half:
 * {@code Settings.Global heads_up_notifications_enabled} is held at 0 whenever {@link NotifQuietPolicy} says so (the custom launcher, a dark or locked screen in ANY launcher, a plain PIN still being decided)
 * and is put back exactly as found for an unlocked Quickstep session. Needs WRITE_SECURE_SETTINGS (the one-time adb grant the launcher already holds); without it nothing changes.
 *
 * LEVEL-TRIGGERED, like the animations: {@link #sync} is called from {@link AnimationSpeedup#syncNow} (mode changes, screen off / on, the unlock decision, helper connect / unbind, process start, the 2 Hz
 * window heartbeat in Quickstep, "Restore everything", the switch), re-reads the facts and sets or clears the hold, so no missed event can leave the banners off in Quickstep. The original value is on disk
 * before the first write; the rules are in {@link NotifQuietHold} (JVM-tested).
 *
 * What this does NOT do (said plainly in TASKS BH1a): it hides no notification that is already in the shade, no status-bar icon and no shade entry. The lock screen's own list is {@link LockQuiet}'s (AK1), the
 * clean-up at the start of a custom session is {@code ReturnReset}'s, and this tablet is a Go device (no notification listener, so nothing can see or cancel a notification before SystemUI shows it).
 */
public final class NotifQuiet {
    private static final String TAG = "TjNotifQuiet";
    private static final String PREFS = "tj_notif_quiet";

    private NotifQuiet() {}

    public static boolean permitted(Context c) { return c.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED; }

    /** The switch (shipped default ON, debug builds OFF: {@link NotifQuietPolicy#defaultOn}). */
    public static boolean switchOn(Context c) { return Features.get(c, NotifQuietPolicy.KEY, NotifQuietPolicy.defaultOn(ReturnReset.isDebuggable(c))); }

    private static SharedPreferences p(Context c) { return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    /** The saved state in our preferences. {@code save} throws when the commit failed, so a change is never made without its original on disk. */
    private static final class PrefsState implements NotifQuietHold.State {
        private final SharedPreferences sp;
        PrefsState(SharedPreferences sp) { this.sp = sp; }
        @Override public boolean held() { return sp.getBoolean("held", false); }
        @Override public String original() { return sp.getString("orig", null); }
        @Override public void save(boolean held, String original) {
            SharedPreferences.Editor e = sp.edit().putBoolean("held", held);
            if (original != null) e.putString("orig", original); else e.remove("orig");
            if (!e.commit()) throw new IllegalStateException("could not save the banner state");
        }
    }

    /** The one global setting. */
    private static final class GlobalSetting implements NotifQuietHold.Setting {
        private final ContentResolver cr;
        GlobalSetting(ContentResolver cr) { this.cr = cr; }
        @Override public String get() { return Settings.Global.getString(cr, NotifQuietPolicy.SETTING); }
        @Override public void put(String value) { if (!Settings.Global.putString(cr, NotifQuietPolicy.SETTING, value)) throw new IllegalStateException("could not write " + NotifQuietPolicy.SETTING); }
    }

    private static final NotifQuietHold.Sink SINK = new NotifQuietHold.Sink() { @Override public void log(String message) { Log.i(TAG, message); } };

    public static boolean held(Context c) { return p(c).getBoolean("held", false); }

    /**
     * THE decision. Reads the facts, asks {@link NotifQuietPolicy#why}, sets or clears the hold, and logs a line (lock log + logcat) only when something changed. {@code screenOnNow} / {@code lockUpNow}: what the
     * caller knows better than a service query, or null to ask. Cheap when there is nothing to do: no binder call and no Settings read unless the hold is on or could start. Only called from
     * {@link AnimationSpeedup#syncNow} (serialised); never throws.
     */
    static synchronized void sync(Context c, String trigger, Boolean screenOnNow, Boolean lockUpNow) {
        try {
            final Context app = c.getApplicationContext();
            final boolean before = held(app);
            NotifQuietPolicy.Facts f = new NotifQuietPolicy.Facts();
            f.switchOn = switchOn(app);
            if (!f.switchOn && !before) return;   // nothing held and nothing wanted
            f.grant = permitted(app);
            f.automationOff = CrashGuard.automationOff(app);
            f.quickstepMode = Mode.QUICKSTEP.equals(Mode.get(app));
            // the screen and the lock only matter inside a Quickstep mode: in the custom launcher the answer is "quiet" whatever they say, so no binder call there
            f.screenOn = true; f.lockUp = false; f.unlockUndecided = false;
            if (f.quickstepMode && f.switchOn && f.grant && !f.automationOff) {
                f.screenOn = screenOnNow != null ? screenOnNow.booleanValue() : AnimationSpeedup.interactive(app);
                f.lockUp = f.screenOn && (lockUpNow != null ? lockUpNow.booleanValue() : AnimationSpeedup.keyguardUp(app));
                f.unlockUndecided = AnimationSpeedup.unlockUndecided(f.screenOn, f.lockUp);
            }
            final String why = NotifQuietPolicy.why(f);
            final boolean want = why.isEmpty();
            final boolean after = NotifQuietHold.set(new PrefsState(p(app)), new GlobalSetting(app.getContentResolver()), f.grant, want, SINK);
            if (before != after) {
                String msg = after ? "banners OFF (" + NotifQuietPolicy.quietFor(f) + ", " + trigger + ")" : "banners ON again: " + (why.isEmpty() ? "hold ended" : why) + " (" + trigger + ")";
                Log.i(TAG, msg);
                LockLog.addAsync(app, "QUIET", msg);
                p(app).edit().putString("last", new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date()) + "  " + msg).apply();
            }
        } catch (Throwable t) { Log.e(TAG, "sync failed", t); }
    }

    /** "Restore everything" and the crash breaker: put the setting back now, whatever the switch says. Never throws. */
    public static synchronized void restoreIfHeld(Context c) {
        try {
            final Context app = c.getApplicationContext();
            NotifQuietHold.set(new PrefsState(p(app)), new GlobalSetting(app.getContentResolver()), permitted(app), false, SINK);
        } catch (Throwable t) { Log.e(TAG, "restore failed", t); }
    }

    // ================================================================ what Launcher control and the tablet report show

    /** One line: the switch, the grant, whether the banners are held off right now, the setting, what will be put back, and the last change. Never throws. */
    public static String describe(Context c) {
        try {
            Context app = c.getApplicationContext();
            SharedPreferences sp = p(app);
            StringBuilder b = new StringBuilder();
            b.append("switch ").append(switchOn(app) ? "on" : "off").append(", adb permission ").append(permitted(app) ? "granted" : "MISSING").append(" | now: ");
            b.append(sp.getBoolean("held", false) ? "pop-up banners OFF (held)" : "pop-up banners as Android has them");
            b.append(" | ").append(NotifQuietPolicy.SETTING).append(" = ").append(Settings.Global.getString(app.getContentResolver(), NotifQuietPolicy.SETTING));
            if (sp.getBoolean("held", false)) b.append(" | put back to: ").append(sp.getString("orig", "?"));
            b.append(" | last change: ").append(sp.getString("last", "(none yet)"));
            return b.toString();
        } catch (Throwable t) { return "(unreadable)"; }
    }
}
