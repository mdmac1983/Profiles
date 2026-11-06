package com.tj.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * AP1 (Tj, 2026-11-06: "The custom launcher occasionally had apps in the recent apps list which were from quickstep. If it makes it any more effective, make it so the only apps that can ever show up in the recent
 * apps list for the custom launcher are those apps in the custom launcher app drawer. No hidden apps should ever show up in the recent apps list for the custom launcher.")
 * The return clean-up ({@link ReturnReset}) removes what it KNOWS was opened in Quickstep; an app it missed (an event that never arrived, a usage log that lagged) stayed in Recents. This is the second, independent
 * line: whenever the Recents screen opens in the custom launcher, the cards of every app that is NOT in the custom drawer (the hidden apps, and apps opened in Quickstep that have no drawer entry) are closed by
 * their own "Close" action, whatever the tracking says. Cards are matched by the APP LABEL of known non-drawer apps only (a card that matches nothing is left alone: a browser card is titled with the page, and
 * closing it by mistake would be worse than leaving it). Fail-open: any problem leaves Recents as it is.
 */
final class RecentsFilter {
    private static final String TAG = "TjRecentsFilter";
    static final String KEY = "recents_drawer_only";        // switch, default ON
    static final String STATUS_KEY = "recents_guard_status"; // what the guard did last (Launcher control and the tablet report)
    private static final long LABELS_TTL_MS = 60_000;

    private final TjAccessibilityService svc;
    private final android.os.Handler handler;
    private Set<String> blocked = new HashSet<>();
    private long blockedAt = -LABELS_TTL_MS;
    private boolean pending = false;
    private int rounds = 0, looks = 0;
    private static final long[] LOOK_DELAYS_MS = { 450, 700, 1200, 1500 };   // after the first look at 150 ms: looks at about 0.6 s, 1.3 s, 2.5 s and 4 s
    private long closedTotal = 0;

    RecentsFilter(TjAccessibilityService svc, android.os.Handler handler) { this.svc = svc; this.handler = handler; }

    // ---------------------------------------------------------------- pure rules (JVM-tested)

    /** Pure: does the guard work now? Only in the custom launcher, with its switch on, the tablet unlocked, the crash breaker not tripped and no return reset driving Recents itself. */
    static boolean wanted(boolean custom, boolean switchOn, boolean locked, boolean automationOff, boolean resetRunning) {
        return custom && switchOn && !locked && !automationOff && !resetRunning;
    }

    /**
     * Pure: the labels whose Recents cards must go = the hidden apps' labels plus the labels of apps opened in Quickstep that are not in the drawer, MINUS every label a drawer app also has (a card titled like a drawer
     * app is never closed).
     */
    static Set<String> blockedLabels(Set<String> hiddenLabels, Set<String> quickstepLabels, Set<String> drawerLabels) {
        Set<String> out = new HashSet<>();
        if (hiddenLabels != null) out.addAll(lower(hiddenLabels));
        if (quickstepLabels != null) out.addAll(lower(quickstepLabels));
        if (drawerLabels != null) out.removeAll(lower(drawerLabels));
        return out;
    }

    private static Set<String> lower(Set<String> in) {
        Set<String> r = new HashSet<>();
        for (String s : in) if (s != null && !s.trim().isEmpty()) r.add(s.trim().toLowerCase(Locale.ROOT));
        return r;
    }

    /** Pure: which blocked label does this card description start with (the same matching as the return clean-up), or null. */
    static String matchingLabel(String description, Set<String> blockedLower) {
        if (description == null || blockedLower == null) return null;
        String d = description.trim().toLowerCase(Locale.ROOT);
        if (d.isEmpty()) return null;
        String best = null;
        for (String x : blockedLower) {
            if (x == null || x.isEmpty()) continue;
            if (d.equals(x) || d.startsWith(x + ",") || d.startsWith(x + " ")) { if (best == null || x.length() > best.length()) best = x; }
        }
        return best;
    }

    /**
     * Pure: can this event mean "the Recents screen just opened"? A window change of the stock launcher (it hosts Quickstep's Recents), or of SystemUI ONLY when the window says "Recents": SystemUI also owns the
     * shade and the quick settings, and a scan there (a tree walk run by SystemUI's own UI thread) is exactly what must not happen while Tj taps a tile (AN1).
     */
    static boolean mayOpenRecents(int eventType, String pkg, String stockPkg, CharSequence className) {
        if (pkg == null) return false;
        boolean windowChange = eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED;
        if (!windowChange) return false;
        if (pkg.equals(stockPkg) || pkg.startsWith("com.android.launcher3")) return true;
        return "com.android.systemui".equals(pkg) && className != null && className.toString().toLowerCase(Locale.ROOT).contains("recents");
    }

    // ---------------------------------------------------------------- Android side

    /** Called for every accessibility event; cheap (a few comparisons) unless a window of the stock launcher / SystemUI changed. Never throws. */
    void onEvent(AccessibilityEvent e) {
        try {
            if (e == null || pending) return;
            CharSequence pkg = e.getPackageName();
            if (pkg == null || !mayOpenRecents(e.getEventType(), pkg.toString(), svc.stockPackageOrNull(), e.getClassName())) return;
            if (!wanted(Mode.isCustom(svc), Features.get(svc, KEY, true), svc.keyguardLockedNow(), CrashGuard.automationOff(svc), svc.resetDrivesRecents())) return;
            schedule(150);
            rounds = 0; looks = 0;
        } catch (Throwable t) { Log.e(TAG, "recents guard event failed", t); }
    }

    private void schedule(long delayMs) {
        pending = true;
        handler.postDelayed(run, delayMs);
    }

    private final Runnable run = new Runnable() { @Override public void run() {
        pending = false;
        try {
            if (!wanted(Mode.isCustom(svc), Features.get(svc, KEY, true), svc.keyguardLockedNow(), CrashGuard.automationOff(svc), svc.resetDrivesRecents())) return;
            long now = android.os.SystemClock.uptimeMillis();
            if (now - blockedAt > LABELS_TTL_MS) { blocked = blockedNow(svc); blockedAt = now; }
            if (blocked.isEmpty()) return;
            String closed = svc.closeOneRecentsCard(blocked);
            if (closed != null) {
                closedTotal++;
                String line = stamp() + "  closed a Recents card of '" + closed + "' (not in the drawer); " + closedTotal + " card(s) closed since the helper connected";
                Features.setString(svc, STATUS_KEY, line);
                LockLog.add(svc.getApplicationContext(), "RECENTS", "closed a card of '" + closed + "': not an app of the custom drawer");
                if (++rounds < 12) schedule(70);          // the card animates away; look again for the next one
            } else if (looks < LOOK_DELAYS_MS.length) {
                schedule(LOOK_DELAYS_MS[looks++]);        // the cards of a freshly opened Recents screen can arrive late (a busy tablet / emulator): a few more looks over about 4 s, then stop
            }
        } catch (Throwable t) { Log.e(TAG, "recents guard run failed", t); }
    } };

    /** The labels (app label and every launcher-activity label) of the apps that are NOT in the custom drawer, never a drawer app's own label. Cheap enough to be refreshed once a minute. */
    static Set<String> blockedNow(Context c) {
        Set<String> hidden = new HashSet<>(), drawer = new HashSet<>();
        try {
            PackageManager pm = c.getPackageManager();
            for (ResolveInfo ri : pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)) {
                String pkg = ri.activityInfo.packageName;
                if (pkg.equals(c.getPackageName())) continue;
                Set<String> into = HiddenApps.isPackageShown(c, pkg) ? drawer : hidden;
                into.add(String.valueOf(ri.loadLabel(pm)));
                try { into.add(String.valueOf(pm.getApplicationLabel(ri.activityInfo.applicationInfo))); } catch (Throwable ignored) { }
            }
        } catch (Throwable t) { Log.e(TAG, "could not list the apps", t); }
        Set<String> qs = new HashSet<>();
        try {
            PackageManager pm = c.getPackageManager();
            for (String p : AppOrigin.quickstep(c)) {
                try { qs.add(String.valueOf(pm.getApplicationLabel(pm.getApplicationInfo(p, 0)))); } catch (Throwable ignored) { }
            }
        } catch (Throwable ignored) { }
        return blockedLabels(hidden, qs, drawer);
    }

    private static String stamp() { return new java.text.SimpleDateFormat("HH:mm:ss", Locale.US).format(new java.util.Date()); }
}
