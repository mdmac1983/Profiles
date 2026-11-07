package com.tj.launcher;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

/**
 * The three global animation scales (window / transition / animator duration = what the Accessibility "Remove animations" switch writes). Needs WRITE_SECURE_SETTINGS (the same one-time adb grant as the keyboard
 * switch); without it nothing changes. Two independent REASONS may hold them at 0, kept as a bit mask ({@link AnimationPolicy#HOLD_RESET}, {@link AnimationPolicy#HOLD_QUICKSTEP}); the scales go back as soon as
 * the LAST reason ends:
 * <ul>
 * <li><b>The return reset</b> (Y2b, Tj 2026-11-03 "as fast as possible, instant if possible"): most of the return reset is animation time (shade opens, Recents opens, cards fly off); {@link #begin} / {@link #restoreLater}.</li>
 * <li><b>The Quickstep session</b> (BF1, Tj 2026-11-07 23:36 "turn off all animations ... Animations should only ever be turned off inside quickstep launcher ... turned back on automatically and immediately on both the
 * lock screen and in the custom launcher"): LEVEL-TRIGGERED. {@link #syncNow} re-reads the facts ({@link AnimationPolicy#why}) at every trigger and sets or clears the hold, so no missed event can leave the animations
 * off: {@code Mode.set}, screen off / on, the unlock decision, the helper connecting / going away, the start of the process, a window-change heartbeat ({@link #tick}) and "Restore everything".</li>
 * </ul>
 * Safety (L10, now for a LONG hold): the original values are written to our prefs BEFORE anything is changed; a scale is put back only if it is still at OUR zero (a value Tj changed by hand in Developer options
 * meanwhile is his), and only when its original was not zero itself (Tj's own permanent setting is never touched); the holds are restored (1) when the last reason ends, (2) by the reset's 10 s failsafe, (3) at the next
 * start of the main process (a reset never outlives its process; the Quickstep hold is re-decided from the facts) and when the helper is not connected, (4) by "Restore everything".
 */
public final class AnimationSpeedup {
    private static final String TAG = "TjAnim";
    public static final String KEY = "reset_animations_off";   // the RESET's switch, default ON (only does anything when the adb grant exists)
    private static final String PREFS = "tj_anim";
    private static final String[] NAMES = { "window_animation_scale", "transition_animation_scale", "animator_duration_scale" };
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private AnimationSpeedup() {}

    public static boolean permitted(Context c) { return c.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED; }
    public static boolean enabled(Context c) { return Features.get(c, KEY, true); }
    /** BF1: the Quickstep switch (shipped default ON, debug builds OFF: {@link AnimationPolicy#defaultOn}). */
    public static boolean quickstepSwitch(Context c) { return Features.get(c, AnimationPolicy.KEY, AnimationPolicy.defaultOn(ReturnReset.isDebuggable(c))); }
    private static SharedPreferences p(Context c) { return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    // ================================================================ the holds and the writes (the only code that touches the scales)

    /**
     * Sets or clears ONE reason and does what follows from the mask: the first reason saves the originals (disk first) and writes the zeros; further reasons rewrite nothing; the last one ending puts the originals
     * back. Returns the mask afterwards. Never throws. Serialised with everything else in this class.
     */
    private static synchronized int setHold(Context c, int bit, boolean on) {
        Context app = c.getApplicationContext();
        SharedPreferences sp = p(app);
        int holds = sp.getInt("holds", 0);
        try {
            int next = AnimationPolicy.withHold(holds, bit, on);
            boolean pending = sp.getBoolean("pending", false);
            android.content.ContentResolver cr = app.getContentResolver();
            if (next != 0) {
                if (!permitted(app)) return holds;
                final boolean first = holds == 0 || !pending;   // nobody held them before: this reason makes them zero. A reason that JOINS one already holding rewrites nothing: a scale Tj changed by hand since stays his
                try {
                    if (!pending) {   // the first reason: the originals and the hold are on disk BEFORE the first change
                        SharedPreferences.Editor e = sp.edit();
                        for (String n : NAMES) e.putString(n, AnimationPolicy.originalOf(Settings.Global.getString(cr, n)));
                        e.putBoolean("pending", true).putInt("holds", next).commit();
                    } else if (next != holds) {
                        sp.edit().putInt("holds", next).commit();
                    }
                    if (first) {   // a scale that already is zero is left alone (Tj's own permanent setting)
                        for (String n : NAMES) if (AnimationPolicy.needsZero(Settings.Global.getString(cr, n))) Settings.Global.putString(cr, n, "0");
                    }
                    return next;
                } catch (Throwable t) {   // a write failed half way: put back what may already be zero and forget this hold, so the stored mask never claims a hold that does not exist
                    Log.e(TAG, "could not turn animations off", t);
                    if (first) {
                        try { for (String n : NAMES) { String w = AnimationPolicy.restoreValue(Settings.Global.getString(cr, n), sp.getString(n, "1.0")); if (w != null) Settings.Global.putString(cr, n, w); } } catch (Throwable ignored) { }
                    }
                    try { sp.edit().putInt("holds", holds).putBoolean("pending", pending).commit(); } catch (Throwable ignored) { }
                    return holds;
                }
            }
            if (holds == 0 && !pending) return 0;   // nothing was ever held
            if (pending) {
                if (!permitted(app)) { sp.edit().putInt("holds", 0).commit(); return 0; }   // cannot put them back now: `pending` stays, the next start with the grant does
                for (String n : NAMES) {
                    String w = AnimationPolicy.restoreValue(Settings.Global.getString(cr, n), sp.getString(n, "1.0"));
                    if (w != null) Settings.Global.putString(cr, n, w);
                }
                Log.i(TAG, "animations restored");
                sp.edit().putBoolean("pending", false).putInt("holds", 0).commit();   // cleared only AFTER the writes: a death in between is repaired by the next start
            } else {
                sp.edit().putInt("holds", 0).commit();
            }
            return 0;
        } catch (Throwable t) {
            Log.e(TAG, "animation hold change failed", t);
            return holds;
        }
    }

    private static int holds(Context c) { return p(c).getInt("holds", 0); }

    // ================================================================ the RETURN RESET's hold (Y2b)

    /** Turns the animations off for the reset. Returns true if they are now off (so the caller can shorten its waits). */
    public static synchronized boolean begin(final Context c) {
        try {
            if (!enabled(c) || !permitted(c)) return false;
            int after = setHold(c, AnimationPolicy.HOLD_RESET, true);
            if (!AnimationPolicy.has(after, AnimationPolicy.HOLD_RESET)) return false;
            MAIN.postDelayed(new Runnable() { @Override public void run() { releaseReset(c, "failsafe"); } }, 10_000);   // failsafe
            Log.i(TAG, "animations off for the reset");
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "could not turn animations off", t);
            try { releaseReset(c, "error"); } catch (Throwable ignored) { }
            return false;
        }
    }

    public static void restoreLater(final Context c, long delayMs) {
        MAIN.postDelayed(new Runnable() { @Override public void run() { releaseReset(c, "reset finished"); } }, delayMs);
    }

    /** BF1: the reset no longer needs the animations off (the custom home is in front, or it finished): its reason ends; the scales return unless the Quickstep session still holds them. Safe to repeat. */
    public static void releaseReset(Context c, String why) {
        try { setHold(c, AnimationPolicy.HOLD_RESET, false); } catch (Throwable t) { Log.e(TAG, "release failed", t); }
    }

    /** "Restore everything" and every last-resort path: ALL reasons end, the scales go back (if they are still ours). Safe to call any time, from any process of ours. */
    public static synchronized void restoreIfPending(Context c) {
        setHold(c, AnimationPolicy.HOLD_RESET | AnimationPolicy.HOLD_QUICKSTEP, false);
    }

    // ================================================================ the QUICKSTEP SESSION's hold (BF1)

    /** The lock screen was (or may be) up since the last unlock decision, so the stored mode can still be the OLD session's. Set at screen off, cleared when the unlock choice is applied (or no lock came). */
    private static volatile boolean lockCycle = false;
    /** Uptime at which the keyguard was first seen GONE during a lock cycle: the safety valve for an unlock handler that never answers. */
    private static volatile long unlockSeenAt = 0;

    /** The screen went off: the lock screen is next. The animations come back NOW, before it can show. */
    public static void onScreenOff(Context c) { lockCycle = true; unlockSeenAt = 0; syncNow(c, "screen off", Boolean.FALSE); }

    /** The screen came on. {@code lockUp}: the lock screen is what it shows (stays real); otherwise the session simply continues. */
    public static void onScreenOn(Context c, boolean lockUp) { if (!lockUp) { lockCycle = false; unlockSeenAt = 0; } syncNow(c, lockUp ? "screen on (lock screen)" : "screen on", Boolean.TRUE); }

    /** The unlock choice was applied (plain PIN = custom, gesture = Quickstep): from here the stored mode is the truth. */
    public static void onUnlockDecided(Context c) {
        lockCycle = false; unlockSeenAt = 0;
        syncNow(c, "unlock decided", null);
        // Android reports the keyguard gone a moment after the unlock handler can run: two cheap follow-up looks (the window heartbeat is the third) so a gesture unlock never waits for the next window change
        final Context app = c.getApplicationContext();
        Worker.H.postDelayed(new Runnable() { @Override public void run() { try { syncNow(app, "after the unlock", null); } catch (Throwable t) { Log.e(TAG, "sync failed", t); } } }, 400);
        Worker.H.postDelayed(new Runnable() { @Override public void run() { try { syncNow(app, "after the unlock", null); } catch (Throwable t) { Log.e(TAG, "sync failed", t); } } }, 1500);
    }

    /** Like {@link #syncNow} but on a background thread (coalesced: the run that follows reads the newest facts). For triggers on the app's start path. */
    public static void syncSoon(final Context c, final String trigger) {
        final Context app = c.getApplicationContext();
        if (!SOON_PENDING.compareAndSet(false, true)) return;
        Worker.H.post(new Runnable() { @Override public void run() { SOON_PENDING.set(false); try { syncNow(app, trigger, null); } catch (Throwable t) { Log.e(TAG, "sync failed", t); } } });
    }
    private static final java.util.concurrent.atomic.AtomicBoolean SOON_PENDING = new java.util.concurrent.atomic.AtomicBoolean(false);
    private static final class Worker { static final Handler H; static { HandlerThread t = new HandlerThread("tj-anim"); t.start(); H = new Handler(t.getLooper()); } }

    /** The heartbeat (called for every accessibility event, at most twice a second does anything): heals a missed trigger, e.g. a lock screen that appeared after the screen came on. Free in the custom launcher. */
    public static void tick(Context c) {
        long now = SystemClock.uptimeMillis();
        if (now - tickAt < 500) return;
        tickAt = now;
        Context app = c.getApplicationContext();
        if (!AnimationPolicy.has(holds(app), AnimationPolicy.HOLD_QUICKSTEP) && !Mode.QUICKSTEP.equals(Mode.get(app))) return;   // nothing held and nothing that could start: no work at all
        syncSoon(app, "window change");
    }
    private static volatile long tickAt = 0;

    /** Main process start: a reset never outlives its process, so that reason ends; the Quickstep reason is decided from the facts like at any other trigger. Waits 2.5 s so a helper that is about to reconnect is not mistaken for a missing one. */
    public static void reconcileAsync(final Context c) {
        final Context app = c.getApplicationContext();
        Worker.H.postDelayed(new Runnable() { @Override public void run() {
            try { setHold(app, AnimationPolicy.HOLD_RESET, false); syncNow(app, "process start", null); } catch (Throwable t) { Log.e(TAG, "reconcile failed", t); }
        } }, 2500);
    }

    public static void syncNow(Context c, String trigger) { syncNow(c, trigger, null); }

    /**
     * THE decision. Reads the facts, asks {@link AnimationPolicy#why}, sets or clears the Quickstep reason, and logs a line (lock log + logcat) only when something changed. {@code screenOnNow}: what the caller
     * knows better than a service query (the broadcast that says so), or null to ask. Cheap when there is nothing to do: no binder call unless the hold is on or could start.
     */
    static synchronized void syncNow(Context c, String trigger, Boolean screenOnNow) {
        try {
            final Context app = c.getApplicationContext();
            final int before = holds(app);
            final boolean holding = AnimationPolicy.has(before, AnimationPolicy.HOLD_QUICKSTEP);
            AnimationPolicy.Facts f = new AnimationPolicy.Facts();
            f.switchOn = quickstepSwitch(app);
            f.grant = permitted(app);
            f.helperUp = TjAccessibilityService.get() != null;
            f.quickstepMode = Mode.QUICKSTEP.equals(Mode.get(app));
            f.automationOff = CrashGuard.automationOff(app);
            // stage 1, no binder call: with the screen and the lock assumed fine, is anything else against it? (this runs at every screen off and every mode change)
            f.screenOn = true; f.lockUp = false; f.unlockUndecided = false;
            String why = AnimationPolicy.why(f);
            if (!why.isEmpty() && !holding) return;   // nothing held and nothing could start
            if (why.isEmpty()) {   // stage 2: the screen and the lock screen
                f.screenOn = screenOnNow != null ? screenOnNow.booleanValue() : interactive(app);
                f.lockUp = f.screenOn && keyguardUp(app);
                if (lockCycle) {
                    if (!f.screenOn || f.lockUp) unlockSeenAt = 0;   // still dark or locked: the unlock is still to come
                    else {
                        long now = SystemClock.uptimeMillis();
                        if (unlockSeenAt == 0) unlockSeenAt = now;
                        if (now - unlockSeenAt > AnimationPolicy.UNDECIDED_MAX_MS) { lockCycle = false; unlockSeenAt = 0; }   // the handler never answered: stop waiting for it
                        else f.unlockUndecided = true;
                    }
                }
                why = AnimationPolicy.why(f);
            }
            final boolean want = why.isEmpty();
            final int after = setHold(app, AnimationPolicy.HOLD_QUICKSTEP, want);
            if (AnimationPolicy.has(before, AnimationPolicy.HOLD_QUICKSTEP) != AnimationPolicy.has(after, AnimationPolicy.HOLD_QUICKSTEP)) {
                String msg = want ? "OFF in the Quickstep session (" + trigger + ")" : "ON again: " + why + " (" + trigger + ")";
                Log.i(TAG, "animations " + msg);
                LockLog.addAsync(app, "ANIM", msg);
                p(app).edit().putString("last", new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date()) + "  " + msg).apply();
            }
        } catch (Throwable t) { Log.e(TAG, "sync failed", t); }
    }

    private static boolean interactive(Context app) {
        try { PowerManager pm = (PowerManager) app.getSystemService(Context.POWER_SERVICE); return pm != null && pm.isInteractive(); } catch (Throwable t) { return false; }
    }

    /** Unknown counts as "lock screen up": the safe side (the animations stay real). */
    private static boolean keyguardUp(Context app) {
        try { KeyguardManager km = (KeyguardManager) app.getSystemService(Context.KEYGUARD_SERVICE); return km == null || km.isKeyguardLocked(); } catch (Throwable t) { return true; }
    }

    // ================================================================ what Launcher control and the tablet report show

    /** One line: the switch, the grant, whether the animations are held right now (and why), the three scales, what will be put back, and the last change. Never throws. */
    public static String describe(Context c) {
        try {
            Context app = c.getApplicationContext();
            SharedPreferences sp = p(app);
            int h = sp.getInt("holds", 0);
            android.content.ContentResolver cr = app.getContentResolver();
            StringBuilder b = new StringBuilder();
            b.append("Quickstep switch ").append(quickstepSwitch(app) ? "on" : "off").append(", adb permission ").append(permitted(app) ? "granted" : "MISSING").append(" | now: ");
            if (h == 0) b.append("animations ON (normal)");
            else b.append("animations OFF, held for ").append(AnimationPolicy.has(h, AnimationPolicy.HOLD_QUICKSTEP) ? "the Quickstep session" : "").append(AnimationPolicy.has(h, AnimationPolicy.HOLD_QUICKSTEP) && AnimationPolicy.has(h, AnimationPolicy.HOLD_RESET) ? " and " : "").append(AnimationPolicy.has(h, AnimationPolicy.HOLD_RESET) ? "the return reset" : "");
            b.append(" | scales (window / transition / animator): ");
            for (int i = 0; i < NAMES.length; i++) b.append(i == 0 ? "" : " / ").append(Settings.Global.getString(cr, NAMES[i]));
            if (sp.getBoolean("pending", false)) {
                b.append(" | put back to: ");
                for (int i = 0; i < NAMES.length; i++) b.append(i == 0 ? "" : " / ").append(sp.getString(NAMES[i], "1.0"));
            }
            b.append(" | last change: ").append(sp.getString("last", "(none yet)"));
            return b.toString();
        } catch (Throwable t) { return "(unreadable)"; }
    }
}
