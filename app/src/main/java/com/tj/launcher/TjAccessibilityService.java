package com.tj.launcher;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.List;
import com.android.launcher3.R;

/**
 * Power-menu helper (L4 / G3b). Only active in custom-launcher mode, only when Tj chose a power mode, never on the lock screen.
 * HIDE_SCREENSHOT: covers the "Screenshot" entry with a panel-coloured overlay (TYPE_ACCESSIBILITY_OVERLAY, needs no overlay permission).
 * BLOCK: closes the whole power menu (Back). Fails open: any problem -> the menu is simply left alone and the overlay removed.
 */
public class TjAccessibilityService extends AccessibilityService {
    private static final String TAG = "TjA11y";
    private static final int PANEL_COLOR = Color.parseColor("#E8F0F1");   // v0.7.6: was #EFF1F3 (AOSP emulator). Tj's side-by-side photo: the stock card is about 4 % less red than ours in the same photo (low reliability: screens differ), so only half way: #E8F0F1

    private static volatile TjAccessibilityService instance;
    /** The running helper, or null when Tj has not turned it on in Accessibility settings. */
    public static TjAccessibilityService get() { return instance; }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private View overlay;
    private final Runnable recheck = new Runnable() { @Override public void run() { evaluate(); } };

    private boolean pending = false;
    private final Rect shownAt = new Rect();
    private final Runnable debounced = new Runnable() {
        @Override public void run() {
            pending = false;
            try { evaluate(); } catch (Throwable t) { Log.e(TAG, "evaluate failed", t); removeOverlay(); }
        }
    };

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        evSeen++;
        try { if (event != null && EventNoise.windowsJustChanged(event.getEventType())) { lastWinEventMs = event.getEventTime(); fastMenuOpen(event); } } catch (Throwable t) { Log.e(TAG, "fast menu check failed", t); }   // AZ1: FIRST, before any other work of this event
        try { trackForeground(event); } catch (Throwable t) { Log.e(TAG, "foreground tracking failed", t); }
        try { picker().onEvent(event); } catch (Throwable t) { Log.e(TAG, "picker guard failed", t); }
        try { recents().onEvent(event); } catch (Throwable t) { Log.e(TAG, "recents guard failed", t); }   // AP1: whenever Recents opens in the custom launcher, the cards of apps that are not in the drawer are closed   // AO1: no "My photos" tile in the system wallpaper picker (one string compare for every other app's event)
        // Never react to our own overlay (adding it fires events: that would be a feedback loop), and coalesce bursts of events into one check.
        if (event != null && getPackageName().contentEquals(event.getPackageName() == null ? "" : event.getPackageName())) return;
        if (event != null && EventNoise.isNoise(event.getEventType(), event.getPackageName())) {   // AB2: another app's content change: it cannot open the power menu, the shade or the PIN pad
            evNoise++;
            if (probeWanted()) { try { probeEvent(event); } catch (Throwable t) { Log.e(TAG, "probe failed", t); } }   // only the (rare, 10-minute) Solitaire probe wants those
            return;
        }
        if (event != null && EventNoise.windowsJustChanged(event.getEventType())) fullScanUntilMs = android.os.SystemClock.uptimeMillis() + 300;   // AB2: look in every window for the next 300 ms (a power menu is a new window)
        scheduleLockUpdate();
        scheduleLayerRefresh();
        schedulePencil(event);
        trackDirty(event);
        trackUnlockKey(event);
        if (probeWanted()) { try { probeEvent(event); } catch (Throwable t) { Log.e(TAG, "probe failed", t); } }
        if (event != null) { try { guardStrayHome(event); } catch (Throwable t) { Log.e(TAG, "guard failed", t); } }
        if (Features.POWER_OFF.equals(Features.powerMode(this)) || !coverWorld()) {
            if (overlay != null || overlayShown) removeOverlay();
            return;   // nothing to do and nothing scanned: zero cost when the feature is off
        }
        // Leading edge: evaluate on the FIRST event at once (the cover must appear as fast as possible), then once more shortly after to catch late content.
        long now = android.os.SystemClock.uptimeMillis();
        // AN1 (Tj: the battery saver tile in the shade is slow / unresponsive): a content change of the shade or the status bar (a tile redrawing, the clock) cannot OPEN the power menu, so it starts no scan unless a menu is
        // already up or a window just changed. Every scan is a pair of whole-tree searches run by SystemUI's own UI thread, the thread that handles the tap on the tile (EventNoise.needsPowerMenuScan).
        if (!EventNoise.needsPowerMenuScan(event == null ? 0 : event.getEventType(), overlayShown || now - menuUpAtMs < 2500, now < fullScanUntilMs)) { evSkipped++; return; }
        if (now - lastEvalMs >= 40) {
            lastEvalMs = now;
            try { evaluate(); } catch (Throwable t) { Log.e(TAG, "evaluate failed", t); removeOverlay(); }
        }
        if (!pending) { pending = true; handler.postDelayed(debounced, 80); }
    }

    /**
     * AZ1: the earliest possible reaction to the stock power menu opening on the lock screen. Everything else {@link #onAccessibilityEvent} does for an event (the lock-number update with its PIN-pad
     * search, the layer refresh, the pencil, the handshake bookkeeping) used to run BEFORE the menu was looked for, and the node scan that followed blocked the main thread until it was over; the patch's
     * new brightness was only drawn after that. Now: the lock-screen patch is resting, a window event arrives, the window list holds the stock dialog and its geometry is known (it is, after the first use):
     * switch the patch to its open state at once and let the fade start. The node scan then waits for the end of the stock fade ({@link PatchFade#SETTLE_MS}). Fail-open: anything else (no geometry yet, the
     * list does not show the dialog yet, the switch is off) leaves everything to the normal path below.
     */
    private void fastMenuOpen(AccessibilityEvent event) {
        if (!patchFadeOn() || patchView == null || !patchLockMode || !patchResting || overlayShown) return;
        long now = android.os.SystemClock.uptimeMillis();
        if (now - fastTryMs < 20) return;   // a burst of window events: one look per 20 ms
        fastTryMs = now;
        String mode = Features.powerMode(this);
        if (!Features.isCover(mode) || !coverWorld()) return;
        Rect cached = cachedCard();
        if (cached == null) return;         // the menu's geometry is learned by the first scan: the normal path
        if (!dialogWindowPresent(getWindows())) return;
        if (menuSeenMs == 0) menuSeenMs = now;
        menuUpAtMs = now;
        Log.i(TAG, "PATCH fast open: window event " + Integer.toHexString(event.getEventType()) + " is " + Math.max(0, now - event.getEventTime()) + " ms old");
        showOverlay(cached);
        deferScan(now);
    }

    /** AZ1: no node scan until the stock fade is over (and one more look then): it blocks this thread for tens of ms, and the patch has to be redrawn on every frame of the fade. */
    private void deferScan(long now) {
        if (!fading) return;   // nothing to follow (switch off, or the scrim was complete already): scan as before
        scanNotBeforeMs = now + PatchFade.SETTLE_MS;
        handler.removeCallbacks(recheck);
        handler.postDelayed(recheck, PatchFade.SETTLE_MS + 20);
    }

    private long lastEvalMs = 0;
    private long fullScanUntilMs = 0;
    /** AM1: a patch window that was just created re-decides whether it RESTS after 400 ms and 1.6 s (the stored crop, the keyguard and the PIN pad may only be settled by then). */
    private final Runnable restRecheck = new Runnable() { @Override public void run() {
        try {
            if (patchView == null || !patchLockMode) return;
            if (patchShotBmp == null && PatchShot.has(TjAccessibilityService.this, rotation(), shotSig())) loadStoredShot();
            patchShow(overlayShown);
            restInfo("restRecheck rest=" + patchResting + " menuOpen=" + overlayShown);   // the diagnostic is written here (twice per new window), not on every menu open / close
        } catch (Throwable t) { Log.e(TAG, "rest recheck failed", t); }
    } };
    private PickerGuard pickerGuard;   // AO1
    private PickerGuard picker() { if (pickerGuard == null) pickerGuard = new PickerGuard(this, handler); return pickerGuard; }
    private RecentsFilter recentsGuard;   // AP1
    private RecentsFilter recents() { if (recentsGuard == null) recentsGuard = new RecentsFilter(this, handler); return recentsGuard; }
    String stockPackageOrNull() { return stockPkg(); }
    boolean keyguardLockedNow() { return keyguardLockedFast(); }
    /** AP1: the return reset (or another automation) is driving Recents itself right now: the guard stays out of its way. */
    boolean resetDrivesRecents() { return resetCover != null || System.currentTimeMillis() < guardPausedUntil; }

    /** AP1: presses "Close" on the first Recents card whose description starts with one of {@code blockedLower} (lower-case app labels); returns that label, or null. Main thread. */
    String closeOneRecentsCard(java.util.Set<String> blockedLower) {
        for (AccessibilityNodeInfo card : findTaskCards()) {
            CharSequence d = card.getContentDescription();
            String hit = RecentsFilter.matchingLabel(d == null ? null : d.toString(), blockedLower);
            if (hit == null) continue;
            for (AccessibilityNodeInfo.AccessibilityAction act : card.getActionList()) {
                if (act.getLabel() != null && "close".equalsIgnoreCase(act.getLabel().toString().trim()) && card.performAction(act.getId())) return hit;
            }
        }
        return null;
    }

    /** AB2: is the Solitaire probe running? Read from the preferences at most once a second instead of on every event. */
    private long probeUntilCache = 0, probeCacheAt = -10_000;
    private boolean probeWanted() {
        long up = android.os.SystemClock.uptimeMillis();
        if (up - probeCacheAt > 1000) {
            probeCacheAt = up;
            try { probeUntilCache = Long.parseLong(Features.getString(this, FailsafeKeys.PROBE_UNTIL_KEY, "0")); } catch (NumberFormatException e) { probeUntilCache = 0; }
        }
        return probeUntilCache != 0;
    }

    private static final String CARD_KEY = "power_card_rect";

    private int rotation() { return ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRotation(); }
    private String cardKey() { return CARD_KEY + "_r" + rotation(); }   // the menu sits somewhere else in landscape

    private Rect cachedCard() {
        String v = Features.getString(this, cardKey(), "");
        String[] p = v.split(",");
        if (p.length != 4) return null;
        try { return new Rect(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3])); }
        catch (NumberFormatException e) { return null; }
    }

    private boolean dialogWindowPresent(List<AccessibilityWindowInfo> windows) {
        if (windows == null) return false;
        for (AccessibilityWindowInfo w : windows) {
            CharSequence t = w.getTitle();
            if (t != null && t.toString().contains("ActionsDialog")) return true;
        }
        return false;
    }

    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        connectedAtMs = android.os.SystemClock.elapsedRealtime(); evSeen = 0; evNoise = 0; scanCount = 0; scanTotalMs = 0; scanMaxMs = 0; evSkipped = 0; penFull = 0; penFullMs = 0; penFullMax = 0; penFast = 0;   // AD8: the report's load numbers count from this connect
        try { warmUp(); } catch (Throwable t) { Log.e(TAG, "warm failed", t); }
        try { startLockNumber(); } catch (Throwable t) { Log.e(TAG, "lock number failed", t); }
        try { updateKeyFilter(); } catch (Throwable t) { Log.e(TAG, "key filter failed", t); }
        try { updateMotion(); } catch (Throwable t) { Log.e(TAG, "motion failed", t); }
        try { startTileGuard(); } catch (Throwable t) { Log.e(TAG, "tile guard failed", t); }
        try { startRadioGuard(); } catch (Throwable t) { Log.e(TAG, "radio guard failed", t); }
        try { startPatchWatch(); } catch (Throwable t) { Log.e(TAG, "patch watch failed", t); }
        try { DemoModeProfile.cleanupOnce(this); } catch (Throwable t) { Log.e(TAG, "demo cleanup failed", t); }   // AB1: the retired retail-demo option never stays switched on
        try { KeepAliveService.ensure(this); } catch (Throwable t) { Log.e(TAG, "keep-alive failed", t); }   // AA2: after a restart / update the helper reconnects first: bring the service back
        Log.i(TAG, "connected; power mode=" + Features.powerMode(this) + " custom=" + Mode.isCustom(this));
        LockLog.add(this, "SERVICE", "connected, mode " + Mode.get(this) + (CrashGuard.automationOff(this) ? " PAUSED" : ""));
        handler.postDelayed(new Runnable() { @Override public void run() { reconcileAfterGap("the helper (re)connected"); } }, 1500);   // AC1: was the helper away at an unlock? (after the connect has settled)
        try { BatterySaverGuard.start(this); } catch (Throwable t) { Log.e(TAG, "battery saver guard failed", t); }
        try { LockQuiet.applyNotificationsAsync(this); } catch (Throwable t) { Log.e(TAG, "lock quiet failed", t); }   // AK1
        try { if (calibrating()) calibrationChanged(); } catch (Throwable t) { Log.e(TAG, "calibration resume failed", t); }   // AM1
    }

    // ---------------------------------------------------------------- W3b: wallpaper patch under the replacement card (Tj: permanent, custom launcher only)
    public static final String PATCH_STATUS_KEY = "wallpaper_patch_status";
    public static final String PATCH_DX_KEY = "patch_dx", PATCH_DY_KEY = "patch_dy", PATCH_SCALE_KEY = "patch_scale_pct";   // alignment nudges in px / percent
    /** AC6: brightness nudge of the lock-screen patch in percent points (Launcher control). The wallpaper MODEL is drawn darker by {@link #DIM_KEYGUARD} because the real keyguard darkens its wallpaper. */
    public static final String PATCH_DIM_KEY = "patch_dim_pct";
    /** AC6: how much of its brightness the real lock screen keeps: MEASURED on the Android 10 emulator (run 74: (173,96,20) on screen vs (229,122,26) in the wallpaper = 0.755 / 0.787 / 0.77). A first estimate for the tablet's MtkSystemUI. */
    static final float DIM_KEYGUARD = 0.77f;
    /** AC6 / V5: how much brightness the screen around the open stock power menu keeps (the dialog's own scrim). MEASURED on the Android 10 emulator, run 136: the strip next to the menu keeps 0.791 / 0.791 / 0.789 of the closed lock screen. Our patch sits ABOVE that scrim, so it is dimmed the same. */
    static final float DIM_OPEN = 0.79f;
    /** The brightness factor of the lock-screen patch: the photo already has the keyguard's dim (only the dialog's scrim is missing), the wallpaper MODEL has neither. Tj's nudge (Launcher control) scales it. */
    private float lockPatchDim(boolean photo) {
        float open = openScrim();
        float k = (photo ? open : DIM_KEYGUARD * open) * (1f + (Features.getInt(this, PATCH_DIM_KEY, 0) + (photo ? 0 : PatchProfile.lockDimPct(android.os.Build.DEVICE))) / 100f);   // v0.7.6 AH3: + the per-device lift of the MODEL (not of a photo)
        return Math.max(0.3f, Math.min(1f, k));
    }
    private View patchView;
    private android.graphics.Bitmap wallpaperBmp;
    /** V1: the LOCK-screen wallpaper (may differ from the home wallpaper), only held while the lock screen is up. */
    private android.graphics.Bitmap lockBmp;
    private boolean patchLockMode, targetIsLock;
    /** AE3 / AD7b: the resting state of the lock-screen patch (see {@link PatchRest}): visible over the Screenshot slot while the lock screen is up and the menu is closed; the switch is in Launcher control, default OFF. */
    /** AH1: Tj's nudge (percent points) of how much the stock dialog's scrim darkens the screen around the OPEN menu; the patch is dimmed the same (lock and home). */
    public static final String PATCH_SCRIM_KEY = "patch_scrim_pct";
    private volatile boolean patchOpenHome = false;   // AH1: the HOME-screen patch is dimmed like the dialog's scrim only while the menu is open (closed = invisible seam)
    private float openScrim() { return PatchRest.openScrim(DIM_OPEN, Features.getInt(this, PATCH_SCRIM_KEY, 0)); }
    private volatile boolean patchResting = false;
    /**
     * AZ1 (Tj, 2026-11-06 20:40: "a slight flicker when I press the side key ... can the darkening to match the background be faster or fade"): the patch FADES to its open-menu brightness along the stock dialog's
     * own fade-in instead of jumping (see {@link PatchFade}); Control switch, default ON (OFF = the v0.8.0 jump).
     */
    public static final String PATCH_FADE_KEY = "patch_fade";
    private volatile long lastWinEventMs = 0;          // AZ1: event time (uptime ms) of the newest window-state / windows-changed event: the stock fade began about then
    private boolean fading = false;                    // AZ1: the open-menu brightness is still following the stock fade (main thread only)
    private long fadeStartMs = 0, fadeLagLast = -1;    // AZ1: uptime ms at which the stock scrim began (estimated); the lag the last fade started with (the tablet report)
    private boolean patchOpenFlag = false;             // AZ1: the last patchShow() call said "menu open"
    private long scanNotBeforeMs = 0, fastTryMs = 0;   // AZ1: the node scan of a freshly opened menu waits for the fade; one fast look per 20 ms
    private boolean patchFadeOn() { return Features.get(this, PATCH_FADE_KEY, true); }
    private int patchRestTop = 0;   // where the RESTING patch starts to paint, in the patch window's coordinates (the bottom of our card = the top of the Screenshot slot)
    /** AM1 (Tj, 2026-11-06): switch, default OFF: draw my own Power off / Restart card over the stock entries (the old cover). OFF = the stock menu stays completely stock; the patch alone hides the Screenshot spot. */
    public static final String POWER_CARD_KEY = "power_card";
    private android.graphics.Bitmap patchShotBmp; private int patchShotLeft, patchShotTop;   // AM1: the stored REAL-pixel crop of the lock screen for this rotation, and where its top-left corner lies on the screen
    private boolean patchTouchable = false;                                                    // AM1: only while the stock menu is open, the patch swallows a tap on the hidden Screenshot spot
    private android.database.ContentObserver shotObserver;
    private long lastShotId = -1;
    private int autoShotTries = 0;
    private boolean autoShotDone = false;
    private android.graphics.Bitmap patchMaskOpen, patchMaskRest;   // AH3: the soft-edge masks of the slot patch (menu open: hard top under the card; resting: soft top); null = the old hard-edged drawing
    /** What kind of wallpaper the lock screen shows ("still picture" / "LIVE wallpaper <component>"): an animated wallpaper cannot be matched exactly by a still patch. Shown in Launcher control and in the tablet report. */
    public static final String WALLPAPER_KIND_KEY = "wallpaper_kind";
    /**
     * AM1: cardless only on the LOCK screen, where the real-pixel patch hides the Screenshot spot. The HOME screen has no patch in "hide_screenshot" mode (the card stretched over the whole panel is what hides the
     * Screenshot entry there), and the "wallpaper" mode IS "patch + my card": both keep the card, as before. The switch forces the card everywhere.
     */
    private boolean cardWanted() {
        if (Features.get(this, POWER_CARD_KEY, false)) return true;
        if (Features.POWER_WALLPAPER.equals(Features.powerMode(this))) return true;
        return !keyguardLockedFast();
    }
    /** Debug builds only (the emulator tests): the resting patch may also rest on the wallpaper MODEL, which is otherwise forbidden because a model patch is a visible block. */
    private boolean modelRestAllowed() { return (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0 && Features.get(this, "patch_rest_model", false); }
    private boolean calibrating() { return PatchShot.calibrating(Features.getString(this, PatchShot.CAL_UNTIL_KEY, "0"), System.currentTimeMillis()); }
    /**
     * AM1: the patch RESTS over the Screenshot spot (switch default ON) only when it can be INVISIBLE: it needs real pixels of this lock screen (a stored crop from a real screenshot, or the helper's fresh photo),
     * and never while a calibration screenshot is being taken. Without them it stays hidden at rest (a model patch is a visible block: Tj's photo of 2026-11-06).
     */
    private String lastRestInfo = "";
    /** AM1: the inputs of the resting decision, newest only, kept in a preference (Launcher control / tablet report / emulator INFO): why does the patch rest, or not? */
    private void restInfo(String what) {
        try {
            String v = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date()) + "  " + what + " [always=" + lockPatchAlways() + " switch=" + Features.get(this, PatchRest.KEY, true) + " wanted=" + lockPatchWanted() + " calibrating=" + calibrating() + " shot=" + (patchShotBmp != null) + " bouncer=" + lastBouncer + "]";
            String key = v.substring(10);
            if (!key.equals(lastRestInfo)) { lastRestInfo = key; Features.setStringAsync(this, "patch_rest_info", v); }
        } catch (Throwable ignored) { }
    }
    private boolean lockPatchAlways() { return Features.get(this, PatchRest.KEY, true) && Features.get(this, LOCK_PATCH_KEY, true) && Features.isCover(Features.powerMode(this)) && keyguardLocked() && !calibrating() && (patchShotBmp != null || lockShot != null || modelRestAllowed()); }   // AM1 (run 218): a FRESH keyguard read, not the 250 ms cache: a stale 'unlocked' left the new window hidden until the menu opened
    /** AE3: brightness of the RESTING patch: no dialog scrim (the menu is closed), so the photo path keeps its own keyguard dim (k = 1) and the wallpaper MODEL gets only the keyguard's dim; Tj's brightness nudge still applies. */
    private float restDim(boolean photo) {
        if (photo) return 1f;   // v0.7.6 (review of v0.7.5): the photo already has the real keyguard brightness and no dialog scrim is open: exactly 1, whatever the brightness nudge says (it corrects the MODEL's keyguard dim only)
        float k = DIM_KEYGUARD * (1f + (Features.getInt(this, PATCH_DIM_KEY, 0) + PatchProfile.lockDimPct(android.os.Build.DEVICE)) / 100f);   // v0.7.6 AH3: + the per-device lift of the MODEL
        return Math.max(0.3f, Math.min(1f, k));
    }
    public static final String LOCK_PATCH_KEY = "lock_patch";   // Tj V1: wallpaper patch + short card on the lock screen (default ON; needs the wallpaper permission)
    private long lastOffTop = -1, lastItemH = -1;
    private int lastPitch = -1;   // AC6: distance between the tops of neighbouring menu entries (Power off -> Restart), measured on every scan
    private int lastCut = -1;     // AC6: where the Screenshot entry begins (its top), measured on every scan
    // V4: auto-calibration of the lock-screen patch against a real screenshot (shell helper). Final lock-patch scale = lockCalM * (1 + Tj's nudge %); shifts add.
    public static final String LOCK_CAL_KEY = "lock_cal";
    public static final String CAL_STATUS_KEY = "lock_cal_status";
    private float lockCalM = 1f; private int lockCalDx = 0, lockCalDy = 0; private String lockCalSig = "";
    private volatile boolean calBusy = false;
    /** V4, second design (run 74): the keyguard darkens the wallpaper by about 23 %, so no model of the wallpaper can match the lock screen. The patch is therefore a PHOTO of the real lock screen
     *  (shell-helper screenshot with the menu closed, cropped to the patch rectangle): exact by construction (dimming, position, zoom). The wallpaper model below stays as the fallback until the first photo exists. */
    private android.graphics.Bitmap lockShot; private int lockShotDx, lockShotDy; private volatile boolean lockShotFresh = false; private long lastShotTry = 0;
    private final Runnable calTrigger = new Runnable() { @Override public void run() { try { maybeCalibrateLock(); } catch (Throwable t) { Log.e(TAG, "calibrate trigger failed", t); } } };
    private float testSkew() { return 1f + Features.getInt(this, "patch_test_skew", 0) / 100f; }   // emulator test only: a deliberately wrong model the calibration must correct
    private String itemKey() { return "power_geo_r" + rotation(); }   // AC6 (v0.7.3): "pitch/cut" of the stock panel; the old "power_item_r<N>" ("top,height") is no longer read or written
    private final Rect patchAt = new Rect();
    private int patchRot = -1;
    private boolean patchWatching = false;
    private final Runnable layerRefresh = new Runnable() { @Override public void run() { layerRefreshPending = false; refreshLayers(); } };
    private boolean layerRefreshPending = false;
    private long lastLayerMs = 0;
    private final Runnable patchRedraw = new Runnable() { @Override public void run() { if (patchView != null) patchView.invalidate(); } };
    private final android.content.SharedPreferences.OnSharedPreferenceChangeListener prefWatch = new android.content.SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override public void onSharedPreferenceChanged(android.content.SharedPreferences sp, String key) {
            if ("mode".equals(key) || Features.POWER_MODE.equals(key)) { handler.post(layerRefresh); handler.post(lockEval); }
            if (PatchShot.CAL_UNTIL_KEY.equals(key)) handler.post(new Runnable() { @Override public void run() { try { calibrationChanged(); } catch (Throwable t) { Log.e(TAG, "calibration change failed", t); } } });   // AM1
            if (POWER_CARD_KEY.equals(key)) handler.post(new Runnable() { @Override public void run() { try { removeOverlay(); refreshLayers(); } catch (Throwable t) { Log.e(TAG, "card switch failed", t); } } });   // AM1: the card appears / disappears now
            if (LockQuiet.KEY.equals(key)) { try { LockQuiet.applyNotificationsAsync(getApplicationContext()); } catch (Throwable ignored) { } }   // AK1: the switch changed: hide / put back the lock-screen notification setting now
            if (PatchRest.KEY.equals(key) || LOCK_PATCH_KEY.equals(key) || PATCH_DIM_KEY.equals(key)) handler.post(new Runnable() { @Override public void run() { layerRefresh.run(); patchShow(overlayShown); } });   // AE3: the always-on switch / dim nudge changed: apply it now
            if ("mode".equals(key)) handler.post(new Runnable() { @Override public void run() { updateHotspot(); } });
            if (key != null && (key.startsWith("unlock_m_") || key.startsWith("return_m_") || "mode".equals(key))) handler.post(new Runnable() { @Override public void run() { updateMotion(); } });
            if (key != null && (key.startsWith("unlock_m_") || key.startsWith("return_m_") || "mode".equals(key) || FailsafeKeys.KONAMI_KEY.equals(key))) handler.post(new Runnable() { @Override public void run() { updateKeyFilter(); } });
            if (FailsafeKeys.HOTSPOT_KEY.equals(key) || FailsafeKeys.CORNER_KEY.equals(key)) handler.post(new Runnable() { @Override public void run() { removeHotspot(); updateHotspot(); } });
        }
    };
    private final android.content.BroadcastReceiver wallpaperReceiver = new android.content.BroadcastReceiver() {
        @Override public void onReceive(android.content.Context c, android.content.Intent i) { wallpaperBmp = null; lockBmp = null; lockCalSig = ""; lockShot = null; lockShotFresh = false; handler.post(new Runnable() { @Override public void run() { removeOverlay(); removePatch(); refreshLayers(); } }); }
    };

    private void startPatchWatch() {
        if (patchWatching) return;
        getSharedPreferences("tj_mode", MODE_PRIVATE).registerOnSharedPreferenceChangeListener(prefWatch);
        getSharedPreferences("tj_features", MODE_PRIVATE).registerOnSharedPreferenceChangeListener(prefWatch);
        registerReceiver(wallpaperReceiver, new android.content.IntentFilter(android.content.Intent.ACTION_WALLPAPER_CHANGED));
        patchWatching = true;
        refreshLayers();
    }

    private void stopPatchWatch() {
        if (!patchWatching) return;
        try { getSharedPreferences("tj_mode", MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(prefWatch); } catch (Throwable ignored) { }
        try { getSharedPreferences("tj_features", MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(prefWatch); } catch (Throwable ignored) { }
        try { unregisterReceiver(wallpaperReceiver); } catch (Throwable ignored) { }
        patchWatching = false;
    }

    /** Called by the launcher (same process) whenever it moves the wallpaper: redraw the patch with the new slice (debounced). */
    /** Called by the launcher (state changes, resume, pause): re-evaluate the patch at once. */
    public void refreshLayersSoon() { handler.removeCallbacks(layerRefresh); handler.post(layerRefresh); }

    public void onWallpaperOffset() { handler.removeCallbacks(patchRedraw); handler.postDelayed(patchRedraw, 40); }

    private void scheduleLayerRefresh() {
        if (!Features.POWER_WALLPAPER.equals(Features.powerMode(this)) && patchView == null && !lockPatchWanted()) return;   // not in this mode and nothing to clean up: free
        long now = android.os.SystemClock.uptimeMillis();
        if (now - lastLayerMs >= 150) { lastLayerMs = now; refreshLayers(); }
        if (!layerRefreshPending) { layerRefreshPending = true; handler.postDelayed(layerRefresh, 250); }
    }

    /** AD7a: see {@link PowerMenuRule#coverWorld}. Cheap: the keyguard answer is cached for 250 ms. */
    private boolean coverWorld() { return PowerMenuRule.coverWorld(Mode.isCustom(this), keyguardLockedFast()); }

    private void patchStatus(String s) {
        if (!s.equals(Features.getString(this, PATCH_STATUS_KEY, ""))) Features.setString(this, PATCH_STATUS_KEY, s);
    }

    /** All the reasons NOT to cover the system menu with a wallpaper patch; null = go. Each reason is shown in Launcher control. */
    private boolean lockPatchWanted() {
        return Features.get(this, LOCK_PATCH_KEY, true) && Features.isCover(Features.powerMode(this)) && keyguardLockedFast();   // AD7a: any lock screen, whatever the mode flag says
    }

    private Rect patchTarget() {
        targetIsLock = false;
        if (keyguardLocked()) {   // V1: the lock screen has its own rules (the wallpaper there may be another picture, and nothing may be painted over the clock / PIN pad)
            if (!Features.get(this, LOCK_PATCH_KEY, true)) { patchStatus("off: lock-screen patch switched off"); return null; }
            if (!Features.isCover(Features.powerMode(this))) { patchStatus("off: lock screen, power menu cover off"); return null; }   // AD7a: no mode condition on the lock screen
            Rect r = cachedCard();
            if (r == null) { patchStatus("waiting: open the power menu once on the lock screen so the helper learns where it sits"); return null; }
            if (loadLockWallpaper() == null) { patchStatus("waiting: the lock-screen wallpaper could not be read (Launcher control > Allow wallpaper access)"); return null; }
            targetIsLock = true;
            patchStatus("lock screen: patch window wanted at " + r.toShortString());
            return patchRect(r, true);
        }
        if (!Features.POWER_WALLPAPER.equals(Features.powerMode(this))) return null;
        if (!Mode.isCustom(this)) { patchStatus("off: Quickstep mode"); return null; }
        if (!TjHooks.homeScreenShowing()) { patchStatus("off: only shown on the home screen (not over apps, not in All Apps)"); return null; }
        Rect r = cachedCard();
        if (r == null) { patchStatus("waiting: open the power menu once so the helper learns where it sits (this screen orientation)"); return null; }
        if (loadWallpaper() == null) return null;
        return patchRect(r, false);
    }

    /**
     * V3 (before v0.7.6): the patch covered the ENTIRE 3-button menu: the learned rectangle plus a 12 px ring with hard edges. AH3 (v0.7.6, Tj's side-by-side photos: our card is the right size and flush, but a flat
     * bluish block around it shows where the patch does not match the live lock screen): the patch now covers ONLY the Screenshot slot (below our card) plus a soft fringe, see {@link PatchRest#slotWindow}.
     * If the learned geometry is nonsense (the card would be the whole panel) the window is a thin strip at the panel's bottom and the card covers the whole panel, so no Screenshot entry can show.
     */
    private Rect patchRect(Rect r, boolean lock) {
        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
        if (!lock) {   // v0.7.6: the HOME-screen patch keeps its v0.7.5 geometry (the whole menu rectangle plus a 12 px ring); only the lock-screen patch became a soft slot patch
            int m = (int) (12 * dm.density);
            return new Rect(Math.max(0, r.left - m), Math.max(0, r.top - m), Math.min(dm.widthPixels, r.right + m), Math.min(dm.heightPixels, r.bottom + m));
        }
        int[] w = PatchRest.slotWindow(r.left, cardAreaGeom(r).bottom, r.right, r.bottom, Math.round(PatchRest.EDGE_DP * dm.density), Math.round(PatchRest.FEATHER_DP * dm.density), dm.widthPixels, dm.heightPixels);
        return new Rect(w[0], w[1], w[2], w[3]);
    }

    private static int[] rectArr(Rect r) { return new int[] { r.left, r.top, r.right, r.bottom }; }

    // ---------------------------------------------------------------- V4: make the lock-screen patch show the RIGHT part of the wallpaper
    private String lockCalSigFor(Rect r) {
        long id = 0;
        try { android.app.WallpaperManager wm = android.app.WallpaperManager.getInstance(this);
              id = wm.getWallpaperId(android.app.WallpaperManager.FLAG_LOCK); if (id <= 0) id = wm.getWallpaperId(android.app.WallpaperManager.FLAG_SYSTEM); } catch (Throwable ignored) { }
        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
        return id + "|r" + rotation() + "|" + dm.widthPixels + "x" + dm.heightPixels + "|" + r.toShortString() + "|k" + Features.getInt(this, "patch_test_skew", 0);
    }

    private void calStatus(String s) { if (!s.equals(Features.getString(this, CAL_STATUS_KEY, ""))) Features.setString(this, CAL_STATUS_KEY, s); }

    /** Runs once per wallpaper / rotation / rectangle: a real screenshot (shell helper) of the lock screen with the menu closed is compared with the wallpaper picture. */
    private void maybeCalibrateLock() {
        if (!patchLockMode || patchView == null || calBusy || lockBmp == null) return;
        if (overlayShown) { handler.postDelayed(calTrigger, 2000); return; }   // never photograph the screen while our menu is up
        final Rect r = new Rect(patchAt);
        final String sig = lockCalSigFor(r);
        if (sig.equals(lockCalSig) && lockShotFresh) return;    // already photographed during this lock
        String saved = Features.getString(this, LOCK_CAL_KEY, "");
        String[] p = saved.split("#");
        if (p.length == 4 && p[0].equals(sig) && !sig.equals(lockCalSig)) {     // model fit measured before for exactly this wallpaper / rotation / rectangle (fallback while there is no photo)
            try { lockCalM = Float.parseFloat(p[1]); lockCalDx = Integer.parseInt(p[2]); lockCalDy = Integer.parseInt(p[3]); lockCalSig = sig; patchView.invalidate(); } catch (NumberFormatException ignored) { }
        }
        android.os.PowerManager pm = (android.os.PowerManager) getSystemService(POWER_SERVICE);
        if (pm == null || !pm.isInteractive()) return;
        if (!ShellHelper.reachable()) { calStatus("no photo of the lock screen: start the adb helper (Launcher control > Tablet setup); the wallpaper model is used meanwhile"); return; }
        if (android.os.SystemClock.elapsedRealtime() - lastShotTry < 4000) { handler.postDelayed(calTrigger, 4000); return; }   // never a screenshot storm if a photo keeps failing
        lastShotTry = android.os.SystemClock.elapsedRealtime();
        calBusy = true;
        final boolean hideRest = patchResting;   // AE3: a RESTING patch must not photograph itself: hide it for the screenshot, restore it afterwards
        if (hideRest && patchView != null) patchView.setAlpha(0f);
        final android.graphics.Bitmap wall = lockBmp;
        final float skew = testSkew();
        new Thread(new Runnable() { @Override public void run() {
            String result = "failed";
            try {
                ShellHelper.run("MKDIR_PUBLISH");
                if (hideRest) { try { Thread.sleep(250); } catch (InterruptedException ignored) { } }   // let the window really disappear before the shot
                String out = ShellHelper.run("CAL_SHOT");
                if (out == null) throw new IllegalStateException("the screenshot job did not answer");
                android.graphics.Bitmap shot = android.graphics.BitmapFactory.decodeFile("/sdcard/Download/launcher-probe/cal.png");
                if (shot == null) throw new IllegalStateException("the screenshot could not be read");
                int W = shot.getWidth(), H = shot.getHeight();
                int[] sp = new int[W * H]; shot.getPixels(sp, 0, W, 0, 0, W, H); shot.recycle();
                final int cl = Math.max(0, r.left), ct = Math.max(0, r.top), cr = Math.min(W, r.right), cb = Math.min(H, r.bottom);
                android.graphics.Bitmap crop = null; String mean = "";
                if (cr - cl >= 12 && cb - ct >= 12) {
                    crop = android.graphics.Bitmap.createBitmap(cr - cl, cb - ct, android.graphics.Bitmap.Config.ARGB_8888);
                    crop.setPixels(sp, ct * W + cl, W, 0, 0, cr - cl, cb - ct);
                    long sr = 0, sg = 0, sb = 0; int n = 0;
                    for (int y = ct; y < cb; y += 3) for (int x = cl; x < cr; x += 3) { int q = sp[y * W + x]; sr += (q >> 16) & 255; sg += (q >> 8) & 255; sb += q & 255; n++; }
                    if (n > 0) mean = (sr / n) + "," + (sg / n) + "," + (sb / n);
                }
                final android.graphics.Bitmap photo = crop; final int photoDx = cl - r.left, photoDy = ct - r.top;
                int bw = wall.getWidth(), bh = wall.getHeight();
                int[] wp = new int[bw * bh]; wall.getPixels(wp, 0, bw, 0, 0, bw, bh);
                PatchCalibrator.Result res = PatchCalibrator.solve(wp, bw, bh, sp, W, H, r.left, r.top, r.right, r.bottom, 0.5f, skew);
                final float m = res == null ? 1f : res.scale; final int dx = res == null ? 0 : res.dx, dy = res == null ? 0 : res.dy;
                final String model = res == null ? "wallpaper model: default alignment kept" : "wallpaper model: " + res;
                result = photo != null ? "PHOTO of the lock screen taken (" + photo.getWidth() + "x" + photo.getHeight() + ", mean colour " + mean + "); " + model : model;
                handler.post(new Runnable() { @Override public void run() {
                    lockCalM = m; lockCalDx = dx; lockCalDy = dy; lockCalSig = sig;
                    Features.setString(TjAccessibilityService.this, LOCK_CAL_KEY, sig + "#" + m + "#" + dx + "#" + dy);
                    if (photo != null && patchView != null && patchLockMode && patchAt.equals(r) && keyguardLocked()) { lockShot = photo; lockShotDx = photoDx; lockShotDy = photoDy; lockShotFresh = true; }
                    if (patchView != null) patchView.invalidate();
                } });
                Log.i(TAG, "CAL lock patch " + result + " [" + PatchCalibrator.lastNote + "]");
            } catch (Throwable t) { result = "failed: " + t; Log.e(TAG, "lock patch calibration failed", t); }
            finally {
                calBusy = false; calStatus(now() + "  " + result);
                if (hideRest) handler.post(new Runnable() { @Override public void run() { patchShow(overlayShown); } });   // AE3: back to resting (or whatever the state is by now)
            }
        } }, "tj-patch-cal").start();
    }

    /** V1: the picture the lock screen shows: a separate lock wallpaper if Tj set one, else the home wallpaper. Down-sampled to the display so a Go tablet keeps its memory. */
    private android.graphics.Bitmap loadLockWallpaper() {
        if (lockBmp != null) return lockBmp;
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            patchStatus("needs permission: Launcher control > Allow wallpaper access"); return null;
        }
        try {
            android.app.WallpaperManager wm = android.app.WallpaperManager.getInstance(this);
            try {   // AE3 / AE8: is the wallpaper an animated LIVE wallpaper? A still patch can never match one exactly (the specks and streaks move). Shown in Launcher control and in the tablet report (Features dump).
                android.app.WallpaperInfo wi = wm.getWallpaperInfo();
                String kind = wi == null ? "still picture" : "LIVE wallpaper " + wi.getComponent().flattenToShortString() + " (animated: a still patch cannot match it exactly)";
                if (!kind.equals(Features.getString(this, WALLPAPER_KIND_KEY, ""))) Features.setString(this, WALLPAPER_KIND_KEY, kind);
            } catch (Throwable ignored) { }
            android.os.ParcelFileDescriptor pfd = wm.getWallpaperId(android.app.WallpaperManager.FLAG_LOCK) > 0 ? wm.getWallpaperFile(android.app.WallpaperManager.FLAG_LOCK) : null;
            if (pfd != null) {
                try {
                    android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
                    ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
                    android.graphics.BitmapFactory.Options o = new android.graphics.BitmapFactory.Options();
                    o.inJustDecodeBounds = true;
                    android.graphics.BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor(), null, o);
                    int sample = 1;
                    while (o.outWidth / (sample * 2) >= dm.widthPixels && o.outHeight / (sample * 2) >= dm.heightPixels) sample *= 2;
                    o = new android.graphics.BitmapFactory.Options(); o.inSampleSize = sample;
                    lockBmp = android.graphics.BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor(), null, o);
                } finally { try { pfd.close(); } catch (Throwable ignored) { } }
            }
            if (lockBmp == null) lockBmp = loadWallpaper();   // no separate lock picture: the lock screen shows the home wallpaper
            try {   // v0.7.6 AH3: the numbers the patch model works with, for the tablet report (so the real engine's placement can be worked out from facts, not photos)
                android.util.DisplayMetrics gdm = new android.util.DisplayMetrics();
                ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(gdm);
                if (lockBmp != null) {
                    float gs = Math.max((float) gdm.heightPixels / lockBmp.getHeight(), (float) gdm.widthPixels / lockBmp.getWidth());
                    String geo = "bitmap " + lockBmp.getWidth() + "x" + lockBmp.getHeight() + ", desired " + wm.getDesiredMinimumWidth() + "x" + wm.getDesiredMinimumHeight() + ", display " + gdm.widthPixels + "x" + gdm.heightPixels
                            + ", cover scale " + Math.round(gs * 1000) / 1000f + " -> shown " + Math.round(lockBmp.getWidth() * gs) + "x" + Math.round(lockBmp.getHeight() * gs) + ", x offset 0.5, profile shift " + PatchProfile.lockShiftPx(android.os.Build.DEVICE, gdm.widthPixels) + " px";
                    if (!geo.equals(Features.getString(this, "wallpaper_geometry", ""))) Features.setString(this, "wallpaper_geometry", geo);
                }
            } catch (Throwable ignored) { }
        } catch (Throwable t) { Log.e(TAG, "lock wallpaper read failed", t); }
        if (lockBmp == null) patchStatus("off: the lock wallpaper could not be read");
        return lockBmp;
    }

    private android.graphics.Bitmap loadWallpaper() {
        if (wallpaperBmp != null) return wallpaperBmp;
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            patchStatus("needs permission: Launcher control > Allow wallpaper access"); return null;
        }
        try {
            android.graphics.drawable.Drawable d = android.app.WallpaperManager.getInstance(this).getDrawable();
            if (d instanceof android.graphics.drawable.BitmapDrawable) wallpaperBmp = ((android.graphics.drawable.BitmapDrawable) d).getBitmap();
            else if (d != null && d.getIntrinsicWidth() > 0) {
                android.graphics.Bitmap b = android.graphics.Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), android.graphics.Bitmap.Config.ARGB_8888);
                d.setBounds(0, 0, b.getWidth(), b.getHeight()); d.draw(new android.graphics.Canvas(b)); wallpaperBmp = b;
            }
        } catch (Throwable t) { Log.e(TAG, "wallpaper read failed", t); }
        if (wallpaperBmp == null) patchStatus("off: the wallpaper could not be read");
        return wallpaperBmp;
    }

    /** Brings the patch (and, above it, the card) in line with the current mode / lock state / rotation. Cheap when nothing changed. */
    private void refreshLayers() {
        try {
            Rect want = patchTarget();
            if (want == null) {
                if (patchView != null) { removeOverlay(); removePatch(); }
                if (!Features.POWER_WALLPAPER.equals(Features.powerMode(this))) wallpaperBmp = null;
                if (!keyguardLocked()) lockBmp = null;   // V1: not needed off the lock screen (memory)
                if (Features.isCover(Features.powerMode(this)) && coverWorld()) warmUp();   // plain cover mode keeps its pre-built card
                return;
            }
            if (patchView != null && (!PatchRest.sameWindow(rectArr(patchAt), rectArr(want), 2) || patchRot != rotation() || patchLockMode != targetIsLock)) { removeOverlay(); removePatch(); }   // AH3: the slot window follows the card's bottom edge, measured on every scan: a 1-2 px wobble must not rebuild the patch
            if (patchView == null) { removeOverlay(); addPatch(want); warmUp(); }
            if (patchLockMode) { handler.removeCallbacks(calTrigger); handler.postDelayed(calTrigger, 1500); }
        } catch (Throwable t) { Log.e(TAG, "layer refresh failed", t); removeOverlay(); removePatch(); }
    }

    /** Used by the card creation paths: guarantees the patch exists BEFORE the card is added (later windows sit on top). */
    private void ensurePatch() {
        if (patchView != null) return;
        Rect want = patchTarget();
        if (want != null) addPatch(want);
    }

    private void addPatch(final Rect r) {
        final android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
        final Rect rect = new Rect(r);
        if (targetIsLock) buildPatchMasks(rect, dm); else { patchMaskOpen = null; patchMaskRest = null; }
        if (targetIsLock) loadStoredShot(); else patchShotBmp = null;   // AM1: the real pixels, if this lock screen was calibrated (must be loaded BEFORE the resting state is decided below)   // AH3: the soft-edge masks belong to the lock patch; the home patch is drawn exactly as in v0.7.5
        View v = new View(this) {
            private final android.graphics.Paint p = new android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG);
            private final android.graphics.Matrix m = new android.graphics.Matrix();
            private final android.graphics.Paint maskPaint = newMaskPaint();
            @Override protected void onDraw(android.graphics.Canvas c) {
                boolean rest = patchResting && patchLockMode;   // AE3: the RESTING patch paints only the Screenshot slot (below the card) and without the dialog's scrim
                android.graphics.Bitmap mask = rest ? patchMaskRest : patchMaskOpen;
                if (mask != null && mask.getWidth() == getWidth() && mask.getHeight() == getHeight()) {   // AH3: paint into a layer and fade its edges with the mask (DST_IN), so a residual mismatch is a soft gradient, not a frame
                    int sc = c.saveLayer(0, 0, getWidth(), getHeight(), null);
                    try { paintPatch(c, rest); c.drawBitmap(mask, 0, 0, maskPaint); } finally { c.restoreToCount(sc); }
                    if (fading) postInvalidateOnAnimation();   // AZ1: the stock menu is still fading in: draw the next frame
                    return;
                }
                if (rest) { c.save(); c.clipRect(0, patchRestTop, getWidth(), getHeight()); }
                try { paintPatch(c, rest); } finally { if (rest) c.restore(); }
                if (fading) postInvalidateOnAnimation();   // AZ1
            }
            private void paintPatch(android.graphics.Canvas c, boolean rest) {
                android.graphics.Bitmap photo = !patchLockMode ? null : lockShot != null ? lockShot : patchShotBmp;   // AM1: the helper's fresh photo wins, else the stored REAL-pixel crop
                if (photo != null) {
                    int px = lockShot != null ? lockShotDx : patchShotLeft - rect.left, py = lockShot != null ? lockShotDy : patchShotTop - rect.top;
                    setDim(p, rest ? restDim(true) : PatchFade.mix(restDim(true), lockPatchDim(true), fadeProgress())); c.drawBitmap(photo, px, py, p); return;   // AZ1: the open brightness follows the stock fade-in (1 = complete)
                }   // V4: the photo of the real lock screen (AC6: the paint is shared with the model path, whose dim filter must not darken the photo: run 136 caught that)
                android.graphics.Bitmap b = patchLockMode ? lockBmp : wallpaperBmp;
                if (b == null) return;
                // AC6: the real lock screen is darker than its wallpaper (keyguard scrim), so the MODEL patch is drawn darker too (the photo path above already has the real colours)
                if (patchLockMode) setDim(p, rest ? restDim(false) : lockPatchDim(false)); else setDim(p, patchOpenHome ? openScrim() : 1f);   // AH1: the home patch was always drawn UNDIMMED = a lighter block while the stock dialog dims the screen
                // How Android 10's wallpaper engine places the picture: scaled to COVER the display, centred vertically, shifted sideways by the launcher's offset.
                float s = Math.max((float) dm.heightPixels / b.getHeight(), (float) dm.widthPixels / b.getWidth());
                float dw = b.getWidth() * s, dh = b.getHeight() * s;
                float cal = 1f + Features.getInt(TjAccessibilityService.this, PATCH_SCALE_KEY, 0) / 100f;   // Tj's alignment nudges (Launcher control)
                if (patchLockMode) cal *= testSkew() * lockCalM;                                           // V4: the measured fit (1 until calibrated)
                s *= cal; dw = b.getWidth() * s; dh = b.getHeight() * s;
                m.setScale(s, s);
                m.postTranslate(-(dw - dm.widthPixels) * (patchLockMode ? 0.5f : TjHooks.wallpaperX) - rect.left + Features.getInt(TjAccessibilityService.this, PATCH_DX_KEY, 0) + (patchLockMode ? lockCalDx + (lockCalDx == 0 && lockCalDy == 0 && lockCalM == 1f ? PatchProfile.lockShiftPx(android.os.Build.DEVICE, dm.widthPixels) : 0) : 0),   // v0.7.6 AH3: + the per-device shift of the model (Tj's MediaTek tablet: -90 px)
                        -(dh - dm.heightPixels) * 0.5f - rect.top + Features.getInt(TjAccessibilityService.this, PATCH_DY_KEY, 0) + (patchLockMode ? lockCalDy : 0));
                c.drawBitmap(b, m, p);
            }
        };
        WindowManager.LayoutParams lp = overlayParams(r, false);
        lp.format = targetIsLock ? PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE;   // V1: an OPAQUE window ignores alpha 0 (run 71: the lock patch stayed visible over the notifications and was even photographed by the calibration); TRANSLUCENT can really be hidden. The home patch keeps OPAQUE (v0.7.5 behaviour)
        lp.setTitle("TjWallpaperPatch");
        ((WindowManager) getSystemService(WINDOW_SERVICE)).addView(v, lp);
        patchLockMode = targetIsLock; patchTouchable = false;
        v.setOnTouchListener(new View.OnTouchListener() {   // AM1: the window is NOT_TOUCHABLE except while the stock menu is open: then a tap on the hidden Screenshot spot closes the menu (like a tap outside) instead of taking a screenshot
            @Override public boolean onTouch(View view, android.view.MotionEvent e) {
                if (!patchTouchable) return false;
                if (e.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) { try { performGlobalAction(GLOBAL_ACTION_BACK); } catch (Throwable ignored) { } }
                return true;
            }
        });
        if (patchLockMode) { patchResting = !overlayShown && PatchRest.visible(false, lockPatchAlways(), true, lastBouncer); v.setAlpha(overlayShown || patchResting ? 1f : 0f); handler.removeCallbacks(restRecheck); handler.postDelayed(restRecheck, 400); handler.postDelayed(restRecheck, 1600); if (patchResting) Log.i(TAG, "PATCH rest on (new window) top=" + patchRestTop); Log.i(TAG, "PATCH window created: resting=" + patchResting + " [always=" + lockPatchAlways() + " switch=" + Features.get(this, PatchRest.KEY, true) + " wanted=" + lockPatchWanted() + " calibrating=" + calibrating() + " shot=" + (patchShotBmp != null) + " bouncer=" + lastBouncer + " overlayShown=" + overlayShown + "]"); }   // V1: on the lock screen it is only visible while the power menu is open (it must never paint over the clock / PIN pad); AE3: or RESTING (switch on, PIN pad not up)
        patchView = v; patchAt.set(r); patchRot = rotation();
        try { Rect cc = cachedCard(); if (cc != null) { Rect ca = cardArea(cc); patchRestTop = PatchRest.restTop(r.top, ca.bottom, r.bottom); } } catch (Throwable ignored) { }   // AE3: the resting patch paints only the part BELOW the card = the Screenshot slot
        patchStatus("active over " + r.toShortString());
        Log.i(TAG, "PATCH shown " + r.toShortString());
    }

    /** AC6: draws with brightness {@code k} (1 = unchanged): a black SRC_ATOP layer of alpha 1-k. */
    private float dimKCache = -1f;
    private android.graphics.ColorFilter dimFilterCache;
    private void setDim(android.graphics.Paint p, float k) {
        if (k >= 0.999f) { p.setColorFilter(null); return; }
        if (dimFilterCache == null || Math.abs(k - dimKCache) > 0.0005f) {   // one filter per brightness, not one per draw (a Go tablet's UI thread)
            dimFilterCache = new android.graphics.PorterDuffColorFilter(Color.argb(Math.round((1f - k) * 255), 0, 0, 0), android.graphics.PorterDuff.Mode.SRC_ATOP);
            dimKCache = k;
        }
        p.setColorFilter(dimFilterCache);
    }

    private static android.graphics.Paint newMaskPaint() {
        android.graphics.Paint mp = new android.graphics.Paint();
        mp.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
        return mp;
    }

    /**
     * AH3: the two soft-edge masks of the slot patch for the window {@code r}: OPEN menu = the top edge is hard and reaches just under our card (the card hides it, so the stock panel can never show through a
     * fade), RESTING = the top edge fades too (nothing sits above it). The opaque core = the learned panel plus a couple of px; the fringe fades outward on the other sides. Any failure leaves the masks null = the
     * old hard-edged drawing, never a crash.
     */
    private void buildPatchMasks(Rect r, android.util.DisplayMetrics dm) {
        patchMaskOpen = null; patchMaskRest = null;
        try {
            Rect panel = cachedCard();
            int w = r.width(), h = r.height();
            if (panel == null || w <= 0 || h <= 0 || (long) w * h > 500L * 500L) return;   // a slot window is about 130 x 130 px
            int e = Math.round(PatchRest.EDGE_DP * dm.density), f = Math.round(PatchRest.FEATHER_DP * dm.density), ov = Math.max(1, Math.round(PatchRest.OVERLAP_DP * dm.density));
            int cb = cardAreaGeom(panel).bottom;
            int cl = panel.left - e - r.left, cr = panel.right + e - r.left, cbot = panel.bottom + e - r.top;
            int fl = Math.min(f, Math.max(0, cl)), fr = Math.min(f, Math.max(0, w - cr)), fb = Math.min(f, Math.max(0, h - cbot));
            patchMaskOpen = alphaBitmap(PatchRest.mask(w, h, cl, cb - ov - r.top, cr, cbot, fl, 0, fr, fb), w, h);
            patchMaskRest = alphaBitmap(PatchRest.mask(w, h, cl, cb - r.top, cr, cbot, fl, Math.min(f, Math.max(0, cb - r.top)), fr, fb), w, h);
        } catch (Throwable t) { patchMaskOpen = null; patchMaskRest = null; Log.e(TAG, "patch masks failed", t); }
    }

    private static android.graphics.Bitmap alphaBitmap(byte[] a, int w, int h) {
        int[] px = new int[w * h];
        for (int i = 0; i < px.length; i++) px[i] = (a[i] & 0xff) << 24;
        return android.graphics.Bitmap.createBitmap(px, w, h, android.graphics.Bitmap.Config.ARGB_8888);
    }

    private void removePatch() {
        if (patchView == null) return;
        try { ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(patchView); } catch (Throwable ignored) { }
        patchView = null; patchAt.setEmpty(); patchRot = -1; patchLockMode = false; patchResting = false; patchOpenHome = false; lockShot = null; lockShotFresh = false; patchMaskOpen = null; patchMaskRest = null; patchShotBmp = null; patchTouchable = false;
        Log.i(TAG, "PATCH removed");
    }

    // ---------------------------------------------------------------- E1 / N2 / N3: the secret handshake picks the launcher; the lock screen covers all the switching
    // Watches WHICH TYPE of PIN-pad key is tapped (by view id), never which digit. Everything is started BEHIND the keyguard, so the unlock itself shows the right screen at once.
    private final UnlockMethods unlock = new UnlockMethods(new UnlockMethods.Enabled() {
        @Override public boolean on(UnlockMethods.Method m) { return methodOn(m); }
    });
    private final Runnable unlockApply = new Runnable() { @Override public void run() { applyUnlockChoice(); } };
    private boolean preArmedThisLock = false;       // the custom stack was already put in front during this lock
    private boolean flippedByHandshake = false;     // Mode was flipped to Quickstep by a handshake that has not unlocked yet
    private boolean lastBouncer = false;
    private final java.util.concurrent.atomic.AtomicBoolean listenerSet = new java.util.concurrent.atomic.AtomicBoolean(false);

    /** A method is on when its switch says so; if Tj switched every one off, the original (double erase) stays on so Quickstep can never become unreachable. */
    private boolean methodOn(UnlockMethods.Method m) {
        boolean any = false;
        for (UnlockMethods.Method x : UnlockMethods.Method.values()) if (Features.get(this, x.prefKey(), x.defaultOn)) { any = true; break; }
        if (!any) return m == UnlockMethods.Method.ERASE2;
        return Features.get(this, m.prefKey(), m.defaultOn);
    }

    /** The stored switches (no fallback): the fallback to double erase lives in {@link UnlockMethods#selected}. */
    private final UnlockMethods.Enabled storedEnabled = new UnlockMethods.Enabled() {
        @Override public boolean on(UnlockMethods.Method m) { return Features.get(TjAccessibilityService.this, m.prefKey(), m.defaultOn); }
    };

    private void ensureUnlockListener() {
        if (!listenerSet.compareAndSet(false, true)) return;
        unlock.setListener(new UnlockMethods.Listener() {
            @Override public void onArmed(UnlockMethods.Method m, boolean padMethodAtEnter) { try { secretAccepted(m); } catch (Throwable t) { Log.e(TAG, "secret accepted failed", t); } }
            @Override public void onDisarmed() { try { secretWithdrawn(); } catch (Throwable t) { Log.e(TAG, "secret withdrawn failed", t); } }
        });
    }

    private boolean keyguardLocked() {
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        return km != null && km.isKeyguardLocked();
    }

    /** AB2: the same answer, remembered for 250 ms. Only for the per-event early-outs (each real call is a binder round trip to system_server); screen on/off and unlock broadcasts re-check at once. */
    private long kgAt = -10_000; private boolean kgVal = false;
    private boolean keyguardLockedFast() {
        long up = android.os.SystemClock.uptimeMillis();
        if (up - kgAt < 250) return kgVal;
        kgVal = keyguardLocked(); kgAt = up;
        return kgVal;
    }

    private long lastPadKeyAt = 0;

    private void trackUnlockKey(AccessibilityEvent e) {
        try {
            if (e == null || e.getEventType() != AccessibilityEvent.TYPE_VIEW_CLICKED) return;
            CharSequence pkg = e.getPackageName();
            if (pkg == null || !"com.android.systemui".contentEquals(pkg)) return;
            // The Enter tap unlocks the tablet, and its click event can be delivered AFTER the keyguard already reports "unlocked" (race seen in emulator runs 63/64: ten pad taps
            // reached us, the final Enter did not, so the handshake was never completed). A click of the PIN pad within 3 s of the previous pad tap still counts.
            long nowMs = System.currentTimeMillis();
            if (!keyguardLocked() && nowMs - lastPadKeyAt > 3000) return;
            ensureUnlockListener();
            AccessibilityNodeInfo n = e.getSource();
            UnlockPick.Key k = null;
            for (int i = 0; i < 3 && n != null && k == null; i++) { k = UnlockPick.classify(n.getViewIdResourceName()); n = n.getParent(); }   // ids only: nothing else of the key is read
            Log.i(TAG, "UNLOCKCLICK class=" + (k == null ? "none" : k.name()));   // diagnostics (full emulator runs 59-61): the KIND of key only, never which digit
            if (k == null) return;
            long now = System.currentTimeMillis();
            lastPadKeyAt = now;
            if (k == UnlockPick.Key.ENTER) {
                String lead = unlock.padLead();   // read BEFORE onEnter clears the pad: the first two key KINDS only (never a digit, never the length)
                lastEnterAt = now;
                UnlockMethods.Method m = unlock.onEnter(now);
                Log.i(TAG, "UNLOCKPICK enter tapped, handshake=" + (m == null ? "none" : m.id));
                LockLog.add(getApplicationContext(), "ENTER", "lag=" + Math.max(0, android.os.SystemClock.uptimeMillis() - e.getEventTime()) + "ms lead=" + (lead.isEmpty() ? "-" : lead) + " handshake=" + (m == null ? "none" : m.id) + (m == null && unlock.armedMethod() != null ? " (earlier gesture kept for this retry: " + unlock.armedMethod().id + ")" : ""));
                if (m == null) coverAtEnter();
            } else unlock.onKey(k, now);
        } catch (Throwable t) { Log.e(TAG, "unlock key tracking failed", t); }
    }

    private final KeySeq returnVolCode = new KeySeq(ReturnMethods.SEQ_WINDOW_MS, ReturnMethods.VOLCODE);
    private final KeySeq returnBacks = new KeySeq(ReturnMethods.SEQ_WINDOW_MS, ReturnMethods.BACKS);
    private final KeySeq konami = new KeySeq(FailsafeKeys.WINDOW_MS, FailsafeKeys.KONAMI);
    private long lastVolUpKeyAt = 0, lastVolDownKeyAt = 0;

    /**
     * Hardware keys, never consumed. On the lock screen: the volume keys feed the unlock handshakes. Unlocked in QUICKSTEP: the "back to custom" key methods (N4).
     * Unlocked in CUSTOM: the Konami master failsafe (N5). Only key-down, only Back and the volume keys are looked at; nothing is stored.
     */
    @Override protected boolean onKeyEvent(android.view.KeyEvent ev) {
        try {
            if (ev != null && Features.get(this, "raw_key_log", false)) Log.i(TAG, "KEYRAW " + android.view.KeyEvent.keyCodeToString(ev.getKeyCode()) + " action=" + ev.getAction());   // research only (AB5, default off): which keys does Android's key filter ever hand a service?
            if (ev == null || ev.getAction() != android.view.KeyEvent.ACTION_DOWN) return false;
            int rc = ev.getRepeatCount();
            int code = ev.getKeyCode();
            boolean vol = code == android.view.KeyEvent.KEYCODE_VOLUME_UP || code == android.view.KeyEvent.KEYCODE_VOLUME_DOWN;
            if (!vol && code != android.view.KeyEvent.KEYCODE_BACK) return false;
            long now = System.currentTimeMillis();
            android.content.Context app = getApplicationContext();
            if (rc == 0) Log.i(TAG, "KEY " + (code == android.view.KeyEvent.KEYCODE_BACK ? "back" : code == android.view.KeyEvent.KEYCODE_VOLUME_UP ? "vol-up" : "vol-down") + " locked=" + keyguardLocked() + " custom=" + Mode.isCustom(app));   // only Back / volume keys are ever looked at
            if (keyguardLocked()) {
                ensureUnlockListener();
                if (rc == 0 && vol && (methodOn(UnlockMethods.Method.VOLUME) || methodOn(UnlockMethods.Method.VOLUP_ERASE))) unlock.onVolume(code == android.view.KeyEvent.KEYCODE_VOLUME_UP, now);
                unlock.onHwKey(code, rc, now);      // O1: both-volume, ping-pong, hold-then-tap, three Backs + Volume Up
                return false;
            }
            if (rc != 0) return false;
            if (Mode.isCustom(app)) {
                if (FailsafeKeys.konami(app) && konami.onKey(code, now)) { buzz(); ProfileSession.goToQuickstep(app, "Konami key code"); }
            } else {
                boolean hit = false;
                if (ReturnMethods.on(app, ReturnMethods.Method.VOLCODE) && returnVolCode.onKey(code, now)) hit = true;
                if (ReturnMethods.on(app, ReturnMethods.Method.BACKS) && returnBacks.onKey(code, now)) hit = true;
                if (vol) {
                    if (code == android.view.KeyEvent.KEYCODE_VOLUME_UP) lastVolUpKeyAt = now; else lastVolDownKeyAt = now;
                    if (ReturnMethods.on(app, ReturnMethods.Method.VOLBOTH) && ReturnMethods.bothVolume(lastVolUpKeyAt, lastVolDownKeyAt, now)) { hit = true; lastVolUpKeyAt = lastVolDownKeyAt = 0; }
                }
                if (hit) { buzz(); ProfileSession.returnToCustom(app, "key code"); }
            }
        } catch (Throwable t) { Log.e(TAG, "key event failed", t); }
        return false;
    }

    /** The key filter is switched on only while a volume handshake is on (it needs the capability declared in tj_accessibility.xml). */
    private void updateKeyFilter() {
        try {
            android.accessibilityservice.AccessibilityServiceInfo info = getServiceInfo();
            if (info == null) return;
            android.content.Context app = getApplicationContext();
            boolean want = UnlockMethods.needsKeys(storedEnabled)
                    || (Mode.isCustom(app) ? FailsafeKeys.konami(app) : ReturnMethods.needsKeys(app));
            boolean has = (info.flags & android.accessibilityservice.AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS) != 0;
            Log.i(TAG, "key filter want=" + want + " has=" + has + " custom=" + Mode.isCustom(app));
            if (want == has) return;
            if (want) info.flags |= android.accessibilityservice.AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
            else info.flags &= ~android.accessibilityservice.AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
            setServiceInfo(info);
            Log.i(TAG, "key filter " + (want ? "ON" : "OFF"));
        } catch (Throwable t) { Log.e(TAG, "key filter update failed", t); }
    }

    private void buzz() {
        try {
            if (!ProfileSession.buzz(this)) return;
            android.os.Vibrator v = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v != null) v.vibrate(android.os.VibrationEffect.createOneShot(40, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
        } catch (Throwable ignored) { }
    }

    private String now() { return new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date()); }

    private void profileNote(String s) {
        Features.setString(this, ProfileSession.PREARM_REPORT_KEY, now() + "  " + s);
        Log.i(TAG, "PROFILE " + s);
    }

    /** A handshake was accepted while the lock screen is up: Quickstep is the target. Flip the mode and put the stock home in front BEHIND the keyguard. */
    private void secretAccepted(UnlockMethods.Method m) {
        buzz();
        android.content.Context app = getApplicationContext();
        if (Mode.isCustom(app)) { Mode.set(app, Mode.QUICKSTEP); flippedByHandshake = true; qsApp = null; resumedPkg = null; }   // review RV1: the lock began in custom = no live Quickstep session, so nothing to resume (a stale app would be cold-started)
        if (ProfileSession.stockBehindLockOnHandshake()) startQuickstepFront("handshake");   // AD5 (the app that was open, else the stock home) / AC9: Quickstep in front NOW, whatever the pre-arm switch says: the custom stack may already be in front (prepared at the wake) and must be replaced before the unlock reveals it
        preArmedThisLock = true;
        Features.setString(this, UnlockPick.LAST_KEY, now() + "  handshake '" + m.title + "' accepted -> Quickstep");
        profileNote("handshake '" + m.title + "' accepted: Quickstep in front behind the lock screen");
        Mode.confirmQuickstep(app, System.currentTimeMillis());   // AC1: dates this session after the lock it came from, even if the helper dies before the unlock is processed
        LockLog.add(app, "HANDSHAKE", m.id + " accepted, mode now " + Mode.get(app));
    }

    /** The handshake was withdrawn (plain retry after a wrong PIN, or the screen went dark): back to the custom account. */
    private void secretWithdrawn() {
        if (!flippedByHandshake) return;
        flippedByHandshake = false;
        android.content.Context app = getApplicationContext();
        if (!Mode.isCustom(app) && !CrashGuard.automationOff(app)) Mode.set(app, Mode.CUSTOM);
        preArmedThisLock = false;
        LockLog.add(app, "HANDSHAKE", "withdrawn (screen off or a plain retry too late), mode " + Mode.get(app));
        if (keyguardLocked()) prearmCustom("handshake withdrawn");
    }

    private void startRouter(String which) {
        Log.i(TAG, "ROUTER start " + which);
        LockLog.add(this, "START", "router " + which);
        try {
            startActivity(new android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME).setPackage(getPackageName())
                    .putExtra(RouterActivity.EXTRA_PREARM, which)
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION));
        } catch (Throwable t) { Log.e(TAG, "router start (" + which + ") failed", t); }
    }

    /** Custom home + the target app (Solitaire) in front, behind the keyguard. Mode is left alone: the unlock decides. */
    private void prearmCustom(String why) {
        try {
            android.content.Context app = getApplicationContext();
            startRouter("custom");            // the router starts the custom home and THEN the target app (same activity, fixed order)
            preArmedThisLock = true;
            profileNote("lock screen (" + why + "): custom home" + (ReturnReset.autoOpenWanted(app) ? " + " + ReturnReset.appLabel(app) : " (Solitaire NOT started: \"Open Solitaire automatically\" is off)") + " put in front behind the keyguard");
        } catch (Throwable t) { Log.e(TAG, "prearm failed", t); }
    }

    private boolean preCleanedThisLock = false;       // O4: Quickstep's apps were closed at lock during this lock
    private final Runnable lockPrepareLater = new Runnable() { @Override public void run() { lockPrepare("screen off, locked"); } };

    /**
     * The tablet is LOCKED (at screen-off if it locks at once, or at the next wake). Do everything that does not need the screen NOW, so the unlock has nothing left to do:
     *  - O4: with the shell helper, a Quickstep session is closed here (apps + notifications + Recents cards) and Mode flips to custom behind the lock screen (tiles / keyboard follow),
     *  - O3: the custom home + Solitaire are put in front, behind the keyguard.
     * Never when the tablet is NOT locked (a plain wake must keep Quickstep's apps) and never while a handshake is armed.
     */
    private void lockPrepare(String why) {
        try {
            android.content.Context app = getApplicationContext();
            boolean locked = keyguardLocked();
            boolean armed = unlock.armedMethod() != null;
            boolean paused = CrashGuard.automationOff(app);
            LockLog.add(app, "LOCKPREP", why + ": locked=" + locked + " armed=" + armed + " mode=" + Mode.get(app) + (paused ? " PAUSED" : ""));
            if (locked && !paused) LockQuiet.onLockAsync(app);   // AK1: on the lock screen Wi-Fi and Bluetooth are off, from any launcher (what was on is remembered for a gesture unlock)
            if (locked && !armed && ProfileSession.preCleanAtLock(ProfileSession.preClean(app), Mode.isCustom(app), paused, true, false, ReturnReset.invisibleReady(app), preCleanedThisLock)) {
                if (ReturnReset.cleanAtLock(app)) {
                    preCleanedThisLock = true;
                    qsApp = null;     // AD5: the helper closed the apps: nothing is left to come back to
                    Mode.set(app, Mode.CUSTOM);
                    profileNote("lock (" + why + "): Quickstep's apps + notifications closed, custom account active behind the lock screen");
                }
            }
            if (ProfileSession.prearmCustomStackOnLockedWake(ProfileSession.prearm(app), locked, armed, preArmedThisLock, paused)) prearmCustom(why);
        } catch (Throwable t) { Log.e(TAG, "lock prepare failed", t); }
    }

    /** SCREEN_ON: the lock screen is what the screen shows (if it was not locked at screen-off, this is the first chance). */
    private void handleLockedWake() { lockPrepare("woke locked"); }

    private final ReturnMethods.Flurry returnFlurry = new ReturnMethods.Flurry();

    /** N4: side-key flurry while Quickstep is active = back to custom. (On a locked screen the lock-screen rules already end the session.) */
    private void flurryChange(boolean on, long t) {
        try {
            android.content.Context app = getApplicationContext();
            if (Mode.isCustom(app) || !ReturnMethods.on(app, ReturnMethods.Method.FLURRY)) { returnFlurry.reset(); return; }
            if (returnFlurry.onChange(on, t)) { buzz(); ProfileSession.returnToCustom(app, "side-key flurry"); }
        } catch (Throwable e) { Log.e(TAG, "flurry failed", e); }
    }

    private final Runnable coverCheck = new Runnable() { @Override public void run() {
        if (keyguardLocked()) hideResetCover();    // the PIN was wrong: the pad is still there, so the cover must not stay
    } };

    /** A PLAIN Enter tap while a Quickstep session is current: raise the black cover now, so the unlock animation never shows what Quickstep left behind. */
    private void coverAtEnter() {
        try {
            android.content.Context app = getApplicationContext();
            if (!ProfileSession.coverAtEnter(Mode.isCustom(app), CrashGuard.automationOff(app), false, ReturnReset.mayHaveWork(app), Features.get(app, ReturnReset.COVER_KEY, true))) return;
            showResetCover();
            handler.removeCallbacks(coverCheck);
            handler.postDelayed(coverCheck, 1800);
        } catch (Throwable t) { Log.e(TAG, "cover at enter failed", t); }
    }

    /** The unlock succeeded (USER_PRESENT). Handshake given = Quickstep (already in front); plain = the custom account (a Quickstep session ends here). */
    private void applyUnlockChoice() {
        try {
            android.content.Context app = getApplicationContext();
            handler.removeCallbacks(coverCheck);
            lastUnlockAt = System.currentTimeMillis();
            ensureUnlockListener();
            if (unlock.hasPendingPad()) {   // the tablet unlocked but the Enter click never classified (emulator runs 63-66): the unlock is the Enter
                UnlockMethods.Method pm = unlock.finishPendingPad(System.currentTimeMillis());
                Log.i(TAG, "UNLOCKPICK Enter click not recognised; pending pad entry evaluated at the unlock: " + (pm == null ? "none" : pm.id));
            }
            UnlockMethods.Method m = unlock.consume(System.currentTimeMillis());
            preArmedThisLock = false; preCleanedThisLock = false;
            boolean wasFlipped = flippedByHandshake;
            flippedByHandshake = false;
            unlock.reset();
            long sinceEnter = lastEnterAt > 0 ? System.currentTimeMillis() - lastEnterAt : -1;   // AC7: how long the PIN check took (the log is how a slow tablet shows itself)
            if (m != null) {
                if (Mode.isCustom(app)) Mode.set(app, Mode.QUICKSTEP);
                // The stock home was started behind the lock screen; only if the pre-start did not land (not in front now) press the router's Home.
                ComponentName stock = StockLauncher.find(this);
                boolean front = (stock != null && isForeground(stock.getPackageName())) || (resumedPkg != null && System.currentTimeMillis() - resumedAt < 15_000 && isForeground(resumedPkg));   // AD5: the resumed app counts (for this unlock only)
                if (!front) startQuickstepFront("after the unlock");
                Features.setString(this, UnlockPick.LAST_KEY, now() + "  Quickstep (handshake '" + m.title + "')" + (front ? "" : ", started after the unlock"));
                Log.i(TAG, "UNLOCKPICK quickstep via " + m.id + " front=" + front);
                Mode.confirmQuickstep(app, System.currentTimeMillis());   // AC1: this lock ended in a handshake, so the safety net below must not end this Quickstep session
                LockLog.add(app, "UNLOCK", "handshake " + m.id + " -> Quickstep, stock in front=" + front + ", Enter to unlock " + sinceEnter + " ms");
                LockQuiet.onUnlockAsync(app, true);   // AK1: a gesture unlock into Quickstep puts Wi-Fi / Bluetooth back as they were before the lock
                verifyQuickstepFront(0);         // AC9: a late start of the custom home / Solitaire stand-in must not stay on top of the gesture's choice
                return;
            }
            qsApp = null; resumedPkg = null;   // AD5: a plain unlock ends the session: no app to come back to
            LockQuiet.onUnlockAsync(app, false);   // AK1: a plain unlock (custom launcher): the radios stay off; forget the memory of this lock
            boolean ends = ProfileSession.plainUnlockEndsQuickstep(Mode.isCustom(app), CrashGuard.automationOff(app));
            if (ends) {
                Mode.set(app, Mode.CUSTOM);
                startRouter("custom");           // quiet: the stack is already (or about to be) in front; no Home press, no double-Home
                ReturnReset.run(app);            // closes what Quickstep opened, clears the shade, radios off, Solitaire last (the cover is usually up already)
                Features.setString(this, UnlockPick.LAST_KEY, now() + "  custom launcher (plain unlock ended the Quickstep session)");
                Log.i(TAG, "UNLOCKPICK custom (session ended)");
                LockLog.add(app, "UNLOCK", "plain -> custom (a Quickstep session ended), Enter to unlock " + sinceEnter + " ms");
            } else {
                hideResetCover();
                Features.setString(this, UnlockPick.LAST_KEY, now() + "  custom launcher (plain unlock)" + (wasFlipped ? " after a withdrawn handshake" : ""));
                Log.i(TAG, "UNLOCKPICK custom");
                LockLog.add(app, "UNLOCK", "plain, mode " + Mode.get(app) + (CrashGuard.automationOff(app) ? " (automation paused: the mode is left alone)" : "") + ", Enter to unlock " + sinceEnter + " ms");
            }
        } catch (Throwable t) { Log.e(TAG, "unlock choice failed", t); }
    }

    /**
     * AC1 safety net ({@link UsageGap}): if the helper was not running when the tablet was unlocked, nothing saw the plain PIN and Android showed Quickstep. Now that it is back: the keyguard was shown after the
     * Quickstep session began and no handshake was confirmed, so the session ended at that unlock - the custom launcher takes the tablet, exactly like a plain unlock does. Cheap when nothing is wrong
     * (a few preference reads; the usage query only runs in Quickstep mode).
     */
    private void reconcileAfterGap(String why) {
        try {
            android.content.Context app = getApplicationContext();
            if (!UsageGap.sessionEndedWhileAway(app)) return;
            LockLog.add(app, "RECONCILE", why + ": the keyguard was shown after the Quickstep session began and no handshake was confirmed: ending the session like a plain unlock");
            qsApp = null; resumedPkg = null;
            Mode.set(app, Mode.CUSTOM);
            startRouter("custom");
            ReturnReset.run(app);
            Features.setString(this, UnlockPick.LAST_KEY, now() + "  custom launcher (the helper was away at the unlock; corrected when it came back)");
        } catch (Throwable t) { Log.e(TAG, "reconcile failed", t); }
    }

    private long lastEnterAt = 0;   // when the last PIN-pad Enter was tapped (for the lock log's "Enter to unlock" time)
    private long lastUnlockAt = 0;  // when the last unlock was handled (applyUnlockChoice): windows that appear in the next moments are what was under the lock screen (ProfileSession.windowIsLockLeftover)

    /**
     * AC9 (Tj: "even though the custom launcher is prepared to open on the lock screen, if I do the gesture for quickstep, always open quickstep"): after an unlock that chose Quickstep, look again at
     * {@link ProfileSession#VERIFY_DELAYS_MS} - if OUR launcher or the Solitaire stand-in is the window in front (a start made at the wake finished late), start Quickstep's home again. Bounded (two looks),
     * and it never touches any OTHER app: someone who opened an app within a second of the unlock is left alone.
     */
    private void verifyQuickstepFront(final int attempt) {
        if (attempt >= ProfileSession.VERIFY_DELAYS_MS.length) return;
        handler.postDelayed(new Runnable() { @Override public void run() {
            try {
                android.content.Context app = getApplicationContext();
                if (Mode.isCustom(app) || keyguardLocked()) return;   // the choice changed meanwhile (a return method, a new lock): nothing to enforce
                String target = ReturnReset.targetPackage(app);
                boolean ours = isForeground(getPackageName()), tgt = target != null && isForeground(target);
                if (ProfileSession.restartQuickstepAfterUnlock(ours, tgt, attempt)) {
                    LockLog.add(app, "VERIFY", "attempt " + (attempt + 1) + ": " + (ours ? "the custom launcher" : "Solitaire") + " is in front after a Quickstep choice: starting Quickstep's home again");
                    startQuickstepFront("verify " + (attempt + 1));
                    verifyQuickstepFront(attempt + 1);
                } else LockLog.add(app, "VERIFY", "attempt " + (attempt + 1) + ": Quickstep (or another app) is in front, nothing to do");
            } catch (Throwable t) { Log.e(TAG, "quickstep verify failed", t); }
        } }, ProfileSession.VERIFY_DELAYS_MS[attempt]);
    }

    // ---------------------------------------------------------------- B4: has another app been opened since Recents was last cleared?
    private void trackDirty(AccessibilityEvent e) {
        try {
            if (e == null || e.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || e.getPackageName() == null) return;
            if (Mode.isCustom(this)) return;   // D5: only apps whose window appears while QUICKSTEP is on count as Quickstep-opened (the custom launcher marks its own launches itself)
            String ime = null;
            String cur = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.DEFAULT_INPUT_METHOD);
            if (cur != null && cur.contains("/")) ime = cur.substring(0, cur.indexOf('/'));
            ComponentName stock = stockCached();   // AD8: cached (one lookup a minute), not a package-manager call per window change
            if (RecentsDirty.shouldMark(e.getPackageName().toString(), getPackageName(), stock == null ? null : stock.getPackageName(), ime, ReturnReset.targetPackage(this), true)) {
                // AC2 follow-up: the app that was in front BEHIND the lock screen shows for a moment while the gesture's unlock goes away. Tj did not open it in Quickstep: it stays the custom account's.
                if (ProfileSession.windowIsLockLeftover(keyguardLockedFast(), unlock.armedMethod() != null, System.currentTimeMillis(), lastUnlockAt)) {
                    LockLog.add(getApplicationContext(), "WINDOW", e.getPackageName() + " appeared under the lock screen / during the unlock: NOT counted as opened in Quickstep (window " + e.getClassName() + ")");
                    return;
                }
                RecentsDirty.mark(this); AppOrigin.markQuickstep(this, e.getPackageName().toString());
                LockLog.add(getApplicationContext(), "DIRTY", e.getPackageName() + " was marked as opened in Quickstep (window " + e.getClassName() + ")");
            }
        } catch (Throwable t) { Log.e(TAG, "dirty tracking failed", t); }
    }

    // ---------------------------------------------------------------- B4: black cover while the reset drives the shade / Recents (never touchable, always removed)
    private View resetCover;
    private final Runnable coverFailsafe = new Runnable() { @Override public void run() { hideResetCover(); } };

    /** Full-screen black, untouchable window: hides the shade / Recents flicker. Removed when Solitaire is verified in front, and by a 4.5 s failsafe. */
    public void showResetCover() {
        try {
            if (resetCover != null) return;
            android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
            ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
            View v = new View(this);
            v.setBackgroundColor(Color.BLACK);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(dm.widthPixels, dm.heightPixels, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.OPAQUE);
            lp.gravity = Gravity.TOP | Gravity.START;
            lp.setTitle("TjResetCover");
            ((WindowManager) getSystemService(WINDOW_SERVICE)).addView(v, lp);
            resetCover = v;
            handler.removeCallbacks(coverFailsafe);
            handler.postDelayed(coverFailsafe, 4500);
            Log.i(TAG, "RESET cover shown");
        } catch (Throwable t) { Log.e(TAG, "cover failed", t); resetCover = null; }
    }

    public void hideResetCover() {
        handler.removeCallbacks(coverFailsafe);
        if (resetCover == null) return;
        try { ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(resetCover); } catch (Throwable ignored) { }
        resetCover = null;
        Log.i(TAG, "RESET cover removed");
    }

    /** Presses Home (used by the reset when there is no app to open, so Recents / the shade are not left on screen). */
    public void goHome() { performGlobalAction(GLOBAL_ACTION_HOME); }

    /** Public wrapper for the reset: TRUE = notification icons in the status bar, FALSE = none, null = cannot tell. */
    public Boolean hasNotificationIcons() { return notificationIconsPresent(); }

    // ---------------------------------------------------------------- B6: the "pencil" (Edit order of settings) in the quick-settings panel
    public static final String PEN_KEY = "hide_qs_pencil";   // switch, default ON
    private View penView;
    private final Rect penAt = new Rect();
    private long lastPenMs = 0;
    private boolean penPending = false;
    /** AN1: the pencil found last time (kept un-recycled): following it is ONE node refresh, a whole-tree search in SystemUI's shade window is the expensive part and is spaced out. */
    private AccessibilityNodeInfo penNode;
    private boolean penSearched = false;
    private final Runnable penEval = new Runnable() { @Override public void run() {
        penPending = false;
        long wait = EventNoise.minPencilSearchGapMs(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) - (android.os.SystemClock.uptimeMillis() - lastPenFullMs);
        if (penNode == null && wait > 0) { penPending = true; handler.postDelayed(this, wait); return; }   // no known pencil to follow and a full search is not due yet: look again when it is
        updatePencil(true);
    } };

    /** Look at SystemUI events only (and keep checking while a cover is up, so it follows the panel and goes with it). Throttled. */
    private void schedulePencil(AccessibilityEvent e) {
        if (!Mode.isCustom(this) || !Features.get(this, PEN_KEY, true)) { if (penView != null) removePencil(); return; }   // N3: Quickstep needs no cover over the pencil
        boolean sysui = e != null && e.getPackageName() != null && "com.android.systemui".contentEquals(e.getPackageName());
        if (!sysui && penView == null) return;
        if (DemoModeProfile.active(this)) { if (penView != null) removePencil(); return; }   // something else already turned the retail flag on (the pencil is gone natively); read only for SystemUI events (AB2)
        long now = android.os.SystemClock.uptimeMillis();
        int type = e == null ? 0 : e.getEventType();
        if (now - lastPenMs >= 40) { lastPenMs = now; updatePencil(now - lastPenFullMs >= EventNoise.minPencilSearchGapMs(type)); }   // AN1: following a known pencil every 40 ms is cheap; a full tree search is spaced out in a burst of redraws
        if (!penPending) { penPending = true; handler.postDelayed(penEval, 90); }
    }

    /** {@code allowFull}: a whole-tree search may run now if the known pencil cannot be followed. Without it a failed refresh just leaves the cover as it is until the next allowed search. */
    private void updatePencil(boolean allowFull) {
        try {
            penSearched = false;
            Rect r = findPencil(allowFull);
            if (r == null) { if (penSearched) hidePencil(); return; }
            showPencil(r);
        } catch (Throwable t) { Log.e(TAG, "pencil update failed", t); removePencil(); }
    }

    private void dropPenNode() {
        AccessibilityNodeInfo n = penNode;
        penNode = null;
        if (n != null) { try { n.recycle(); } catch (Throwable ignored) { } }
    }

    private static boolean pencilBoundsOk(Rect b) { return b.width() > 0 && b.height() > 0 && b.width() < 400 && b.height() < 400; }

    /** Bounds of the visible Edit pencil, or null. AN1: first the pencil found last time is refreshed (one node); only when that fails (and a search is allowed) the shade window is searched. */
    private Rect findPencil(boolean allowFull) {
        AccessibilityNodeInfo known = penNode;
        if (known != null) {
            try {
                if (known.refresh() && known.isVisibleToUser()) {
                    Rect b = new Rect(); known.getBoundsInScreen(b);
                    if (pencilBoundsOk(b)) { penFast++; penSearched = true; return b; }
                }
            } catch (Throwable ignored) { }
            dropPenNode();   // gone, hidden or moved to a bad place: search again (below) when allowed
        }
        if (!allowFull) return null;
        penSearched = true;
        long t0 = android.os.SystemClock.uptimeMillis(); lastPenFullMs = t0;
        try { return findPencilFull(); }
        finally { long d = android.os.SystemClock.uptimeMillis() - t0; penFull++; penFullMs += d; if (d > penFullMax) penFullMax = d; }
    }

    /** The whole-tree search: bounds of the visible Edit pencil (framework id android:id/edit, description "Edit order of settings."), or null. Remembers the node it found (see {@link #penNode}). */
    private Rect findPencilFull() {
        List<AccessibilityWindowInfo> windows = getWindows();
        if (windows == null) return null;
        for (AccessibilityWindowInfo w : windows) {
            if (!EventNoise.isSystemWindow(w.getType())) continue;   // AB2: the pencil is in SystemUI's shade window; never wake an app (or the keyboard) just to read its package name
            AccessibilityNodeInfo root = w.getRoot();
            if (root == null) continue;
            try {
                CharSequence pkg = root.getPackageName();
                if (pkg == null || !"com.android.systemui".contentEquals(pkg)) continue;
                List<AccessibilityNodeInfo> hit = root.findAccessibilityNodeInfosByViewId("android:id/edit");
                if (hit == null) continue;
                for (AccessibilityNodeInfo n : hit) {
                    CharSequence cd = n.getContentDescription();
                    if (!n.isVisibleToUser() || cd == null || !cd.toString().toLowerCase(java.util.Locale.ROOT).contains("edit")) continue;
                    Rect b = new Rect(); n.getBoundsInScreen(b);
                    if (pencilBoundsOk(b)) { dropPenNode(); try { penNode = AccessibilityNodeInfo.obtain(n); } catch (Throwable ignored) { penNode = null; } return b; }
                }
            } finally { root.recycle(); }
        }
        return null;
    }

    private boolean penShown = false;

    private WindowManager.LayoutParams penParams(Rect r, boolean touchable) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(r.width(), r.height(), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | (touchable ? 0 : WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE),   // touchable when shown on purpose: it swallows taps meant for the pencil
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = r.left; lp.y = r.top;
        lp.setTitle("TjPencilCover");
        return lp;
    }

    private void showPencil(Rect r) {
        // D4: the cover window stays attached (invisible, untouchable) once it exists, so showing it is one redraw + one layout update, not a window creation.
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (penView != null) {
            if (!penShown) { penView.setAlpha(1f); penShown = true; wm.updateViewLayout(penView, penParams(r, true)); penAt.set(r); Log.i(TAG, "PENCIL cover shown " + r.toShortString()); }
            else if (!penAt.equals(r)) { wm.updateViewLayout(penView, penParams(r, true)); penAt.set(r); }
            return;
        }
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(r.width(), r.height(), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = r.left; lp.y = r.top;
        lp.setTitle("TjPencilCover");
        if (penView == null) {
            View v = new View(this);
            v.setBackgroundColor(Color.WHITE);   // the stock panel is pure white (255,255,255) in Tj's photo
            v.setOnTouchListener(new View.OnTouchListener() { @Override public boolean onTouch(View view, android.view.MotionEvent ev) { return true; } });
            wm.addView(v, lp);
            penView = v; penAt.set(r); penShown = true;
            Log.i(TAG, "PENCIL cover shown " + r.toShortString());
        } else if (!penAt.equals(r)) {
            wm.updateViewLayout(penView, lp);
            penAt.set(r);
        }
    }

    /** Panel closed: hide the cover but keep the window (warm) so the next open is instant; a real removal happens on removePencil(). */
    private void hidePencil() {
        if (penView == null || !penShown) return;
        try {
            penView.setAlpha(0f); penShown = false;
            ((WindowManager) getSystemService(WINDOW_SERVICE)).updateViewLayout(penView, penParams(penAt, false));
            Log.i(TAG, "PENCIL cover hidden (kept warm)");
        } catch (Throwable t) { removePencil(); }
    }

    private void removePencil() {
        dropPenNode();
        if (penView == null) return;
        try { ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(penView); } catch (Throwable ignored) { }
        penView = null; penAt.setEmpty(); penShown = false;
        Log.i(TAG, "PENCIL cover removed");
    }

    // ---------------------------------------------------------------- X1: the number on the lock screen (Tj relaxed the lock-screen rule for this)
    private View lockView;
    private boolean lockReceiverOn = false;
    private long lastLockEvalMs = 0;
    private boolean lockEvalPending = false;
    private final Runnable lockEval = new Runnable() { @Override public void run() { lockEvalPending = false; updateLockNumber(); } };
    private final android.content.BroadcastReceiver lockReceiver = new android.content.BroadcastReceiver() {
        @Override public void onReceive(android.content.Context c, android.content.Intent i) {
            kgAt = -10_000;   // AB2: screen on/off and unlock change the keyguard: never answer these from the 250 ms memo
            handler.post(lockEval); handler.postDelayed(lockEval, 400);
            handler.post(layerRefresh);
            String act = i == null ? null : i.getAction();
            long t = System.currentTimeMillis();
            ensureUnlockListener();
            if (android.content.Intent.ACTION_SCREEN_OFF.equals(act) || android.content.Intent.ACTION_SCREEN_ON.equals(act) || android.content.Intent.ACTION_USER_PRESENT.equals(act))
                LockLog.add(getApplicationContext(), android.content.Intent.ACTION_SCREEN_OFF.equals(act) ? "SCREEN_OFF" : android.content.Intent.ACTION_SCREEN_ON.equals(act) ? "SCREEN_ON" : "USER_PRESENT", "locked=" + keyguardLocked() + " mode=" + Mode.get(getApplicationContext()));
            if (android.content.Intent.ACTION_SCREEN_OFF.equals(act)) {
                lockShotFresh = false;     // a new lock session: photograph the lock screen again after the next wake (notifications may have changed)
                qsAppAt = ResumeTarget.refreshedAt(qsApp, fgPkg, qsAppAt, t);   // review RV2: the app still in front when the screen goes off is the one Tj had open: its 6 h clock starts now
                resumedPkg = null; unlock.onScreen(false, t); preArmedThisLock = false; preCleanedThisLock = false; lastBouncer = false; flurryChange(false, t); updateMotion();
                try { patchShow(overlayShown); } catch (Throwable ignored) { }   // v0.7.6 (review of v0.7.5): lastBouncer is reset here, so a resting patch that was hidden for the PIN pad must come back now; the next wake sees bouncer == lastBouncer and would never re-show it
                handler.removeCallbacks(lockPrepareLater);
                handler.postDelayed(lockPrepareLater, 700);      // O3/O4: if the tablet locks at once, get everything ready while the screen is still dark
                handler.postDelayed(lockPrepareLater, 6500);     // ... and after the default 5 s "lock after" delay
            }
            if (android.content.Intent.ACTION_SCREEN_ON.equals(act)) { unlock.onScreen(true, t); handler.removeCallbacks(lockPrepareLater); handleLockedWake(); if (calibrating()) { autoShotTries = 0; handler.removeCallbacks(autoShot); handler.postDelayed(autoShot, 2500); } flurryChange(true, t); updateMotion(); handler.postDelayed(layerRefresh, 400); }
            if (android.content.Intent.ACTION_USER_PRESENT.equals(act)) { handler.removeCallbacks(lockPrepareLater); startUnlockSpeed(); handler.post(unlockApply); updateMotion(); handler.postDelayed(layerRefresh, 400); }
            if (android.content.Intent.ACTION_POWER_CONNECTED.equals(act) || android.content.Intent.ACTION_POWER_DISCONNECTED.equals(act)) {
                try { if (keyguardLocked()) unlock.onPower(android.content.Intent.ACTION_POWER_CONNECTED.equals(act), t); } catch (Throwable e) { Log.e(TAG, "power event failed", e); }
            }
        }
    };

    private void startLockNumber() {
        if (!lockReceiverOn) {
            android.content.IntentFilter f = new android.content.IntentFilter();
            f.addAction(android.content.Intent.ACTION_SCREEN_ON);
            f.addAction(android.content.Intent.ACTION_SCREEN_OFF);
            f.addAction(android.content.Intent.ACTION_USER_PRESENT);
            f.addAction(android.content.Intent.ACTION_POWER_CONNECTED);
            f.addAction(android.content.Intent.ACTION_POWER_DISCONNECTED);
            registerReceiver(lockReceiver, f);
            lockReceiverOn = true;
        }
        updateLockNumber();
    }

    private void stopLockNumber() {
        if (lockReceiverOn) { try { unregisterReceiver(lockReceiver); } catch (Throwable ignored) { } lockReceiverOn = false; }
        removeLockNumber();
    }

    /** Called on every accessibility event: leading edge at once (the number must go the moment the PIN pad appears), plus one trailing check. */
    private void scheduleLockUpdate() {
        if (!LockNumber.enabled(this) && lockView == null && !keyguardLockedFast()) return;   // number off, nothing on screen and not on the lock screen: zero cost
        long now = android.os.SystemClock.uptimeMillis();
        if (now - lastLockEvalMs >= 60) { lastLockEvalMs = now; updateLockNumber(); }
        if (!lockEvalPending) { lockEvalPending = true; handler.postDelayed(lockEval, 120); }
    }

    private void updateLockNumber() {
        try {
            boolean enabled = LockNumber.enabled(this);
            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(POWER_SERVICE);
            KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            boolean on = pm != null && pm.isInteractive();
            boolean locked = km != null && km.isKeyguardLocked();
            boolean bouncer = (enabled || locked) && on && locked && bouncerShown();   // also needed by the handshakes (a fresh pad, the peek count), not just the number
            if (bouncer != lastBouncer) { lastBouncer = bouncer; ensureUnlockListener(); unlock.onPad(bouncer, System.currentTimeMillis()); LockLog.add(getApplicationContext(), "PAD", bouncer ? "shown" : "hidden"); patchShow(overlayShown); }   // AE3: the resting patch hides while the PIN pad is up and comes back after
            if (LockNumber.shouldShow(enabled, on, locked, bouncer)) showLockNumber(); else removeLockNumber();
        } catch (Throwable t) { Log.e(TAG, "lock number update failed", t); removeLockNumber(); }
    }

    private static final String[] BOUNCER_IDS = {"keyguard_pin_view", "keyguard_password_view", "keyguard_pattern_view", "pinEntry", "passwordEntry", "lockPatternView"};
    private static final String[] BOUNCER_TEXTS = {"enter your pin", "enter your password", "draw your pattern", "enter pin", "enter password"};

    /** True when the PIN / password / pattern pad of the lock screen is visible (read-only look at SystemUI's windows). */
    private boolean bouncerShown() {
        List<AccessibilityWindowInfo> windows = getWindows();
        if (windows == null) return false;
        for (AccessibilityWindowInfo w : windows) {
            AccessibilityNodeInfo root = w.getRoot();
            if (root == null) continue;
            try {
                CharSequence pkg = root.getPackageName();
                if (pkg == null || !"com.android.systemui".contentEquals(pkg)) continue;
                for (String id : BOUNCER_IDS) {
                    List<AccessibilityNodeInfo> hit = root.findAccessibilityNodeInfosByViewId("com.android.systemui:id/" + id);
                    if (hit != null) for (AccessibilityNodeInfo n : hit) if (n.isVisibleToUser()) return true;
                }
                for (String t : BOUNCER_TEXTS) {
                    List<AccessibilityNodeInfo> hit = root.findAccessibilityNodeInfosByText(t);
                    if (hit != null) for (AccessibilityNodeInfo n : hit) if (n.isVisibleToUser()) return true;
                }
            } finally { root.recycle(); }
        }
        return false;
    }

    private View buildLockView(final String text, final float em, final float baselineInView) {
        return new View(this) {
            private final android.graphics.Paint p = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG | android.graphics.Paint.SUBPIXEL_TEXT_FLAG);
            { p.setColor(Color.argb(LockNumber.alphaFor(Features.getInt(TjAccessibilityService.this, LockNumber.ALPHA_KEY, 0)), 255, 255, 255)); p.setTextSize(em);
              p.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, LockNumber.weightFor(Features.getInt(TjAccessibilityService.this, LockNumber.WEIGHT_KEY, 0)), false)); p.setTextAlign(android.graphics.Paint.Align.CENTER); }   // v0.7.6: Tj's nudges on the measured look
            @Override protected void onDraw(android.graphics.Canvas c) { c.drawText(text, getWidth() / 2f + getWidth() * LockNumber.X_NUDGE_PER_WIDTH, baselineInView, p); }
        };
    }

    private void showLockNumber() {
        if (lockView != null) return;
        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
        float em = LockNumber.emFor(dm.widthPixels, Features.getInt(this, LockNumber.SIZE_KEY, 0));   // v0.7.6: the measured size, times Tj's size nudge
        float baselineY = dm.heightPixels * LockNumber.BASELINE_PER_HEIGHT;
        int h = (int) Math.ceil(em * 2f);
        float baselineInView = em * 1.3f;
        lockView = buildLockView(LockNumber.text(this), em, baselineInView);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                dm.widthPixels, h, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = 0; lp.y = Math.round(baselineY - baselineInView);
        lp.setTitle("TjLockNumber");
        ((WindowManager) getSystemService(WINDOW_SERVICE)).addView(lockView, lp);
        Log.i(TAG, "LOCKNUM shown");
    }

    private void removeLockNumber() {
        if (lockView == null) return;
        try { ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(lockView); } catch (Throwable ignored) { }
        lockView = null;
        Log.i(TAG, "LOCKNUM removed");
    }

    // ---------------------------------------------------------------- W5: keep the stock launcher's home screen away unless Tj did the gesture
    private ComponentName stockCache;
    private long stockCacheAt = 0;

    /** AD8: the stock launcher, looked up at most once a minute (a package-manager binder call per window change is too much for a Go tablet); a failed lookup is retried at the next call, never remembered. */
    private ComponentName stockCached() {
        long now = android.os.SystemClock.elapsedRealtime();
        if (stockCache == null || now - stockCacheAt > 60_000) { stockCache = StockLauncher.find(this); stockCacheAt = now; }
        return stockCache;
    }
    private final long[] guardHist = new long[StrayHomeGuard.MAX];
    private int guardIdx = 0;
    private long standDownUntil = 0, lastGuardMs = 0, guardPausedUntil = 0;

    private void guardStrayHome(AccessibilityEvent e) {
        if (e.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence pk = e.getPackageName(), cl = e.getClassName();
        if (pk == null || cl == null) return;
        if (!Mode.isCustom(this) || !Features.get(this, StrayHomeGuard.KEY, true)) return;
        long now = System.currentTimeMillis();
        if (now < guardPausedUntil || now - Mode.changedAt(this) < 2500) return;   // a deliberate mode change or the return reset is running: never fight it
        ComponentName stockHome = stockCached();
        if (stockHome == null) return;
        if (!StrayHomeGuard.isStockHome(pk.toString(), cl.toString(), stockHome.getPackageName(), stockHome.getClassName())) return;
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        boolean locked = km != null && km.isKeyguardLocked();
        if (!StrayHomeGuard.shouldRedirect(true, true, locked, true, now, Mode.failOpenUntil(this), standDownUntil, guardHist)) {
            if (StrayHomeGuard.countInWindow(guardHist, now) >= StrayHomeGuard.MAX) standDownUntil = now + StrayHomeGuard.COOLDOWN_MS;
            return;
        }
        if (now - lastGuardMs < 700) return;   // one stock-home appearance can raise two events
        lastGuardMs = now;
        guardHist[guardIdx++ % guardHist.length] = now;
        int n = bump(StrayHomeGuard.COUNT_KEY);
        Features.setString(this, StrayHomeGuard.LAST_KEY, new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date(now)));
        Log.i(TAG, "STRAY stock home shown in custom mode -> pressing Home (#" + n + ")");
        performGlobalAction(GLOBAL_ACTION_HOME);   // the router answers with our Launcher
    }

    private int bump(String key) {
        int n = 1;
        try { n = Integer.parseInt(Features.getString(this, key, "0")) + 1; } catch (NumberFormatException ignored) { }
        Features.setString(this, key, String.valueOf(n));
        return n;
    }

    // ---------------------------------------------------------------- D3: how fast is the card? (measured, shown in Launcher control)
    // There is NO earlier signal on Android 10: the side key is consumed before the key filter (never reaches a service), and PhoneWindowManager no longer broadcasts
    // ACTION_CLOSE_SYSTEM_DIALOGS("globalactions") when it opens the power menu (checked in the android-10.0.0_r47 source; only the shutdown path still does).
    // The first thing an accessibility service can see is the menu window itself, so speed comes from the pre-built (warm) card.
    public static final String LATENCY_KEY = "power_latency";
    private long signalMs = 0, menuSeenMs = 0, cardShownMs = 0;

    /** Called when the card becomes visible: how long after the broadcast / after the real menu window appeared? (negative = the card came FIRST) */
    private void recordLatency() {
        if (cardShownMs == 0) return;
        String line = "card shown " + (signalMs > 0 ? (cardShownMs - signalMs) + " ms after the system broadcast" : "(no broadcast seen)")
                + (menuSeenMs > 0 ? ", " + (cardShownMs - menuSeenMs) + " ms after the real menu appeared (negative = card first)" : ", real menu not seen before the card");
        Features.setString(this, LATENCY_KEY, line);
        Log.i(TAG, "POWERLAT " + line);
    }

    public static final String PWR_COUNT_KEY = "power_key_count", PWR_LAST_KEY = "power_key_last";   // kept so old settings files stay readable; the side key never reaches a service

    // ---------------------------------------------------------------- N5: which app is in front (for the hidden hotspot and the probe)
    private String fgPkg = "";

    // ---------------------------------------------------------------- U1 (Tj: "significant delay when loading either launcher from the lock screen"): measure it on the tablet itself
    public static final String UNLOCK_SPEED_KEY = "unlock_speed";
    private long unlockAtMs = 0;
    private final Runnable unlockSpeedTimeout = new Runnable() { @Override public void run() {
        if (unlockAtMs > 0) { unlockAtMs = 0; Log.i(TAG, "UNLOCKSPEED no new window within 3 s: the target was already in front behind the lock screen"); Features.setString(TjAccessibilityService.this, UNLOCK_SPEED_KEY, now() + "  already in front (nothing to load)"); }
    } };

    /** USER_PRESENT: start the stopwatch. */
    private void startUnlockSpeed() { unlockAtMs = android.os.SystemClock.uptimeMillis(); handler.removeCallbacks(unlockSpeedTimeout); handler.postDelayed(unlockSpeedTimeout, 3000); }

    /** The first real app window after the unlock: how long the launcher / app took to appear. */
    private void noteUnlockSpeed(String pk) {
        if (unlockAtMs <= 0) return;
        long d = android.os.SystemClock.uptimeMillis() - unlockAtMs;
        unlockAtMs = 0; handler.removeCallbacks(unlockSpeedTimeout);
        Log.i(TAG, "UNLOCKSPEED first window after the unlock: " + pk + " +" + d + " ms");
        Features.setString(this, UNLOCK_SPEED_KEY, now() + "  " + pk + " appeared +" + d + " ms after the unlock");
    }

    private void trackForeground(AccessibilityEvent e) {
        if (e == null || e.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || e.getPackageName() == null || e.getClassName() == null) return;
        String pk = e.getPackageName().toString();
        if ("com.android.systemui".equals(pk) || pk.startsWith("com.google.android.inputmethod") || pk.contains("inputmethod") || pk.contains("keyboard") || pk.contains("swiftkey")) return;
        noteUnlockSpeed(pk);
        if (pk.equals(fgPkg)) return;
        fgPkg = pk;
        try { rememberQuickstepApp(pk); } catch (Throwable t) { Log.e(TAG, "remember app failed", t); }
        updateHotspot();
    }

    // ---------------------------------------------------------------- AD5: a gesture unlock comes back to the app that was open (see ResumeTarget)
    private String qsApp = null;          // the last app window of the Quickstep session while unlocked ("" / null = none: the stock home was the last thing, or no session)
    private long qsAppAt = 0;
    private String resumedPkg = null;     // the app started for the current gesture (so "is Quickstep in front?" accepts it too)
    private long resumedAt = 0;           // ... and only for a few seconds: a stale value must never decide a LATER unlock

    private String stockPkg() { ComponentName s = stockCached(); return s == null ? null : s.getPackageName(); }

    private void rememberQuickstepApp(String pk) {
        android.content.Context app = getApplicationContext();
        if (Mode.isCustom(app)) { qsApp = null; return; }   // review RV1: a custom session means there is no Quickstep session to come back to; a stale app must never be started by a later gesture
        // what appears under the lock screen (or right after the unlock) is the pre-arm / the custom account's last app, not Tj's app (review RV3: the same unit-tested rule trackDirty uses)
        if (ProfileSession.windowIsLockLeftover(keyguardLockedFast(), unlock.armedMethod() != null, System.currentTimeMillis(), lastUnlockAt)) return;
        String stock = stockPkg();
        if (ResumeTarget.isStockHome(pk, stock)) { qsApp = null; return; }    // he went home (or to Recents): there is no app to come back to
        if (!ResumeTarget.remember(pk, getPackageName(), stock, false, false)) return;
        if (!isLaunchable(pk)) return;        // a dialog or system window without an icon (permission controller, installer) is not "the app Tj had open": it neither replaces nor clears it
        qsApp = pk; qsAppAt = System.currentTimeMillis();
    }

    private final java.util.HashMap<String, Boolean> launchableCache = new java.util.HashMap<>();
    private boolean isLaunchable(String pk) {
        Boolean b = launchableCache.get(pk);
        if (b == null) {
            boolean v = false;
            try { v = getPackageManager().getLaunchIntentForPackage(pk) != null; } catch (Throwable ignored) { }
            if (launchableCache.size() > 64) launchableCache.clear();
            launchableCache.put(pk, v); b = v;
        }
        return b;
    }

    /**
     * AD5: Quickstep in front. The Quickstep app that was open when the tablet locked is started like a tap on its icon (its task comes forward as it was: no RESET_TASK_IF_NEEDED); nothing to come back to, the
     * switch off, or any failure = the stock home, as before.
     */
    private void startQuickstepFront(String why) {
        android.content.Context app = getApplicationContext();
        String pkg = ResumeTarget.pick(Features.get(app, ResumeTarget.KEY, true), qsApp, qsAppAt, System.currentTimeMillis());
        if (pkg != null) {
            try {
                android.content.Intent li = getPackageManager().getLaunchIntentForPackage(pkg);
                if (li != null) {
                    li.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    resumedPkg = pkg; resumedAt = System.currentTimeMillis();
                    startActivity(li);
                    Log.i(TAG, "ROUTER resume " + pkg + " (" + why + ")");
                    LockLog.add(app, "START", "resume " + pkg + " (" + why + ")");
                    return;
                }
            } catch (Throwable t) { Log.e(TAG, "resume start failed, stock home instead", t); }
        }
        if (pkg == null) {   // say WHY the stock home came up (the report and the emulator gate show it): switch off / nothing remembered / too old
            String reason = ResumeTarget.whyNot(Features.get(app, ResumeTarget.KEY, true), qsApp, qsAppAt, System.currentTimeMillis()) + " [qsApp=" + qsApp + " fg=" + fgPkg + " mode=" + Mode.get(app) + "]";
            Log.i(TAG, "ROUTER stock home, no resume (" + why + "): " + reason);
            LockLog.add(app, "START", "stock home, no resume: " + reason);
        }
        resumedPkg = null;
        startRouter("stock");
    }

    // ---------------------------------------------------------------- N5: hidden hotspot in Solitaire: 5 quick taps then a hold = master failsafe into Quickstep
    private View hotView;
    private final TapCode tapCode = new TapCode();
    private final Runnable hotHold = new Runnable() { @Override public void run() {
        try { tapCode.reset(); buzz(); removeHotspot(); ProfileSession.goToQuickstep(getApplicationContext(), "Solitaire hotspot"); } catch (Throwable t) { Log.e(TAG, "hotspot failsafe failed", t); }
    } };

    private void updateHotspot() {
        try {
            android.content.Context app = getApplicationContext();
            String target = ReturnReset.targetPackage(app);
            boolean want = FailsafeKeys.hotspot(app) && Mode.isCustom(app) && target != null && target.equals(fgPkg) && !keyguardLocked();
            if (want && hotView == null) showHotspot(); else if (!want && hotView != null) removeHotspot();
        } catch (Throwable t) { Log.e(TAG, "hotspot update failed", t); removeHotspot(); }
    }

    private void showHotspot() {
        float dp = getResources().getDisplayMetrics().density;
        int size = Math.round(56 * dp);
        View v = new View(this) {
            @Override public boolean onTouchEvent(android.view.MotionEvent m) {
                long t = android.os.SystemClock.uptimeMillis();
                switch (m.getActionMasked()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        if (tapCode.onDown(t)) handler.postDelayed(hotHold, TapCode.HOLD_MS);
                        return true;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacks(hotHold);
                        tapCode.onUp(t);
                        return true;
                    default: return true;
                }
            }
        };
        int corner = FailsafeKeys.corner(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(size, size, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT);
        lp.gravity = (corner < 2 ? Gravity.TOP : Gravity.BOTTOM) | (corner % 2 == 0 ? Gravity.START : Gravity.END);
        lp.setTitle("TjHotspot");
        ((WindowManager) getSystemService(WINDOW_SERVICE)).addView(v, lp);
        hotView = v;
        tapCode.reset();
        Log.i(TAG, "HOTSPOT shown corner=" + FailsafeKeys.CORNERS[corner]);
    }

    private void removeHotspot() {
        handler.removeCallbacks(hotHold);
        if (hotView == null) return;
        try { ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(hotView); } catch (Throwable ignored) { }
        hotView = null;
        Log.i(TAG, "HOTSPOT removed");
    }

    // ---------------------------------------------------------------- N5 research: what can the helper READ inside Solitaire? (decides whether a card-move code is possible)
    private final java.util.LinkedHashSet<String> probeSamples = new java.util.LinkedHashSet<>();
    private int probeEvents = 0, probeClicks = 0, probeNodes = 0, probeReadable = 0;
    private long probeLastWalk = 0, probeLastWrite = 0;

    private void probeEvent(AccessibilityEvent e) {
        if (e == null) return;
        long wall = System.currentTimeMillis();
        long until = Long.parseLong(Features.getString(this, FailsafeKeys.PROBE_UNTIL_KEY, "0"));
        if (wall > until) { if (until != 0) { Features.setString(this, FailsafeKeys.PROBE_UNTIL_KEY, "0"); probeFlush(true); } return; }
        CharSequence pk = e.getPackageName();
        String target = ReturnReset.targetPackage(this);
        if (pk == null || target == null || !target.contentEquals(pk)) return;
        probeEvents++;
        if (e.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) probeClicks++;
        AccessibilityNodeInfo src = e.getSource();
        if (src != null) probeSample(src);
        long now = android.os.SystemClock.uptimeMillis();
        if (now - probeLastWalk > 1500) {
            probeLastWalk = now;
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) { try { probeNodes = 0; probeReadable = 0; probeWalk(root, 0); } finally { root.recycle(); } }
        }
        probeFlush(false);
    }

    private void probeSample(AccessibilityNodeInfo n) {
        CharSequence d = n.getContentDescription(), t = n.getText();
        String id = n.getViewIdResourceName();
        if ((d == null || d.length() == 0) && (t == null || t.length() == 0) && id == null) return;
        if (probeSamples.size() >= 25) return;
        String x = String.valueOf(n.getClassName()).replace("android.widget.", "").replace("android.view.", "") + "|" + (id == null ? "-" : id.substring(id.indexOf('/') + 1)) + "|"
                + (d == null ? "" : cut(d)) + "|" + (t == null ? "" : cut(t));
        probeSamples.add(x);
    }

    private static String cut(CharSequence c) { String s = c.toString(); return s.length() > 24 ? s.substring(0, 24) : s; }

    private void probeWalk(AccessibilityNodeInfo n, int depth) {
        if (n == null || depth > 14 || probeNodes > 400) return;
        probeNodes++;
        CharSequence d = n.getContentDescription(), t = n.getText();
        if ((d != null && d.length() > 0) || (t != null && t.length() > 0)) { probeReadable++; probeSample(n); }
        for (int i = 0; i < n.getChildCount(); i++) {
            AccessibilityNodeInfo c = n.getChild(i);
            if (c != null) { try { probeWalk(c, depth + 1); } finally { c.recycle(); } }
        }
    }

    private void probeFlush(boolean force) {
        long now = android.os.SystemClock.uptimeMillis();
        if (!force && now - probeLastWrite < 2000) return;
        probeLastWrite = now;
        Features.setString(this, FailsafeKeys.PROBE_REPORT_KEY, "events=" + probeEvents + ", clicks seen=" + probeClicks + ", nodes in window=" + probeNodes + ", nodes with text/description=" + probeReadable
                + (probeSamples.isEmpty() ? "\nNOTHING readable: the game draws its cards itself, so a card-move code cannot be read (use the hotspot or the key code)." : "\nreadable samples (class|id|description|text):\n" + android.text.TextUtils.join("\n", probeSamples)));
    }

    // ---------------------------------------------------------------- O1: rotation twirl + shake / knock (accelerometer, only while the lock screen is up and one of them is selected)
    private int lastOrientation = -1;

    @Override public void onConfigurationChanged(android.content.res.Configuration c) {
        super.onConfigurationChanged(c);
        try {
            int o = c == null ? -1 : c.orientation;
            boolean first = lastOrientation == -1;
            if (o != lastOrientation) {
                lastOrientation = o;
                if (!first && keyguardLocked()) { ensureUnlockListener(); unlock.onRotation(System.currentTimeMillis()); }
            }
        } catch (Throwable t) { Log.e(TAG, "rotation event failed", t); }
    }

    private final MotionDetector motion = new MotionDetector();
    private boolean motionOn = false;
    private final android.hardware.SensorEventListener sensorListener = new android.hardware.SensorEventListener() {
        @Override public void onSensorChanged(android.hardware.SensorEvent e) {
            try {
                MotionDetector.Kind k = motion.onSample(e.values[0], e.values[1], e.values[2], e.timestamp / 1_000_000L);
                if (k == MotionDetector.Kind.NONE) return;
                android.content.Context app = getApplicationContext();
                if (keyguardLocked()) {
                    ensureUnlockListener();
                    unlock.onMotion(k == MotionDetector.Kind.SHAKE ? UnlockMethods.Method.SHAKE : UnlockMethods.Method.KNOCK, System.currentTimeMillis());
                } else if (!Mode.isCustom(app) && ReturnMethods.on(app, k == MotionDetector.Kind.SHAKE ? ReturnMethods.Method.SHAKE : ReturnMethods.Method.KNOCK)) {
                    buzz();
                    ProfileSession.returnToCustom(app, k == MotionDetector.Kind.SHAKE ? "shake" : "double knock");   // R1: the accelerometer way back to the custom launcher
                }
            } catch (Throwable t) { Log.e(TAG, "motion sample failed", t); }
        }
        @Override public void onAccuracyChanged(android.hardware.Sensor s, int a) { }
    };

    /** The accelerometer is read ONLY while the lock screen is showing with the screen on and Triple shake / Double knock is selected; unregistered otherwise (no battery cost). */
    private void updateMotion() {
        try {
            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(POWER_SERVICE);
            android.content.Context app = getApplicationContext();
            boolean interactive = pm != null && pm.isInteractive();
            boolean lockWant = UnlockMethods.needsMotion(storedEnabled) && keyguardLocked();
            boolean returnWant = !Mode.isCustom(app) && ReturnMethods.needsMotion(app) && !keyguardLocked();     // R1: only while Quickstep is active
            boolean want = interactive && (lockWant || returnWant);
            android.hardware.SensorManager sm = (android.hardware.SensorManager) getSystemService(SENSOR_SERVICE);
            if (sm == null) return;
            if (want && !motionOn) {
                android.hardware.Sensor a = sm.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
                if (a != null) { motion.reset(); sm.registerListener(sensorListener, a, android.hardware.SensorManager.SENSOR_DELAY_GAME); motionOn = true; Log.i(TAG, "MOTION sensor on"); }
            } else if (!want && motionOn) {
                sm.unregisterListener(sensorListener); motionOn = false; Log.i(TAG, "MOTION sensor off");
            }
        } catch (Throwable t) { Log.e(TAG, "motion sensor update failed", t); }
    }

    // ---------------------------------------------------------------- O5: the custom account keeps its ONE tile (SystemUI's auto-added tiles - hotspot, saver, night light ... - are taken out again)
    private final long[] tileHist = new long[3];
    private int tileIdx = 0;
    private boolean tileGuardOn = false;
    private final android.database.ContentObserver tileObserver = new android.database.ContentObserver(handler) {
        @Override public void onChange(boolean selfChange) {
            try {
                android.content.Context app = getApplicationContext();
                if (!Mode.isCustom(app) || !TileProfile.enabled(app) || !TileProfile.permitted(app)) return;
                String cur = android.provider.Settings.Secure.getString(getContentResolver(), TileProfile.SETTING);
                if (TileProfile.tilesFor(app, Mode.CUSTOM).equals(cur)) return;
                long now = System.currentTimeMillis();
                if (!ProfileSession.enforceRadios(true, true, now, tileHist)) return;     // same "3 tries a minute, never a loop" rule
                tileHist[tileIdx++ % tileHist.length] = now;
                Log.i(TAG, "TILES changed while the custom account is active (" + cur + ") -> putting its list back");
                TileProfile.apply(app, Mode.CUSTOM);
            } catch (Throwable t) { Log.e(TAG, "tile guard failed", t); }
        }
    };

    private void startTileGuard() {
        if (tileGuardOn) return;
        getContentResolver().registerContentObserver(android.provider.Settings.Secure.getUriFor(TileProfile.SETTING), false, tileObserver);
        tileGuardOn = true;
    }

    private void stopTileGuard() {
        if (!tileGuardOn) return;
        try { getContentResolver().unregisterContentObserver(tileObserver); } catch (Throwable ignored) { }
        tileGuardOn = false;
    }

    // ---------------------------------------------------------------- N3: the custom account has no Wi-Fi / Bluetooth (Tj: "no wifi or bluetooth enabled")
    private final long[] lqHist = new long[LockQuiet.MAX_WRITES];   // AK1: when the lock-screen rule last switched a radio off
    private int lqIdx = 0;
    private final long[] radioHist = new long[3];
    private int radioIdx = 0;
    private boolean radioGuardOn = false;
    private final android.content.BroadcastReceiver radioReceiver = new android.content.BroadcastReceiver() {
        @Override public void onReceive(android.content.Context c, android.content.Intent i) {
            try {
                String act = i == null ? null : i.getAction();
                boolean turnedOn, fullyOn;   // v0.7.6 (review of AK1): fullyOn = the settled state: the lock-screen rule must not spend a slot on ENABLING and another on ENABLED
                if (android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION.equals(act)) {
                    int st = i.getIntExtra(android.net.wifi.WifiManager.EXTRA_WIFI_STATE, -1);
                    turnedOn = st == android.net.wifi.WifiManager.WIFI_STATE_ENABLED || st == android.net.wifi.WifiManager.WIFI_STATE_ENABLING; fullyOn = st == android.net.wifi.WifiManager.WIFI_STATE_ENABLED;
                } else if (android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED.equals(act)) {
                    int st = i.getIntExtra(android.bluetooth.BluetoothAdapter.EXTRA_STATE, -1);
                    turnedOn = st == android.bluetooth.BluetoothAdapter.STATE_ON || st == android.bluetooth.BluetoothAdapter.STATE_TURNING_ON; fullyOn = st == android.bluetooth.BluetoothAdapter.STATE_ON;
                } else return;
                if (!turnedOn) return;
                long now = System.currentTimeMillis();
                if (!ProfileSession.enforceRadios(ProfileSession.radiosOff(TjAccessibilityService.this), Mode.isCustom(TjAccessibilityService.this), now, radioHist)) {
                    // AK1: not the custom account's rule: is the tablet on the LOCK SCREEN? Then a radio that comes on is switched off again, whatever launcher is active
                    if (!fullyOn || !LockQuiet.enforceRadios(LockQuiet.enabled(TjAccessibilityService.this), keyguardLockedFast(), CrashGuard.automationOff(TjAccessibilityService.this), now, lqHist)) return;
                    lqHist[lqIdx++ % lqHist.length] = now;
                } else radioHist[radioIdx++ % radioHist.length] = now;
                final android.content.Context app = getApplicationContext();
                new Thread(new Runnable() { @Override public void run() { try { ReturnReset.radiosOff(app); } catch (Throwable t) { Log.e(TAG, "radio guard failed", t); } } }, "tj-radio-guard").start();
                Log.i(TAG, "RADIOS switched on in the custom account -> switching off");
            } catch (Throwable t) { Log.e(TAG, "radio receiver failed", t); }
        }
    };

    private void startRadioGuard() {
        if (radioGuardOn) return;
        android.content.IntentFilter f = new android.content.IntentFilter();
        f.addAction(android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION);
        f.addAction(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(radioReceiver, f);
        radioGuardOn = true;
    }

    private void stopRadioGuard() {
        if (!radioGuardOn) return;
        try { unregisterReceiver(radioReceiver); } catch (Throwable ignored) { }
        radioGuardOn = false;
    }

    @Override public void onInterrupt() { removeHotspot(); removeOverlay(); removePatch(); removePencil(); hideResetCover(); removeLockNumber(); picker().remove("interrupted"); }

    @Override public boolean onUnbind(android.content.Intent i) { instance = null; LockLog.add(this, "SERVICE", "unbound"); removeOverlay(); removePatch(); removePencil(); hideResetCover(); picker().remove("unbound"); stopPatchWatch(); stopLockNumber(); stopRadioGuard(); stopTileGuard(); removeHotspot(); try { ((android.hardware.SensorManager) getSystemService(SENSOR_SERVICE)).unregisterListener(sensorListener); motionOn = false; } catch (Throwable ignored) { } return super.onUnbind(i); }

    /**
     * AD8 (Go discipline, Tj: "optimized for my tablet": 2 GB RAM, memory class 128 MB): when Android reports memory pressure, drop the cached wallpaper / lock-screen bitmaps that nothing is drawing right now
     * (a few MB each at 600 x 973); they are rebuilt on demand. Never touches them while the patch window exists (its view draws from them).
     * HONEST SCOPE (review RV5, 2026-11-06): this only drops the references THIS service holds. The home wallpaper bitmap usually aliases WallpaperManager's own cache and the lock bitmaps exist only while the patch window
     * does, so on most devices little memory is actually freed. It is cheap insurance, NOT a measured saving: the tablet report's memory lines (AD8) decide whether anything bigger is worth doing.
     */
    @Override public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        try {
            // real memory pressure only: RUNNING_LOW (10), RUNNING_CRITICAL (15) and the background levels (40+); NOT UI_HIDDEN (20), which fires whenever the launcher's UI goes away
            boolean pressure = level == android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW || level == android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL || level >= android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND;
            if (!pressure) return;
            handler.post(new Runnable() { @Override public void run() {
                launchableCache.clear();
                if (patchView == null) { wallpaperBmp = null; lockBmp = null; lockShot = null; lockShotFresh = false; Log.i(TAG, "TRIM released the cached wallpaper bitmaps"); }
            } });
        } catch (Throwable ignored) { }
    }

    @Override public void onDestroy() { instance = null; removeOverlay(); removePatch(); removePencil(); hideResetCover(); picker().remove("destroyed"); stopPatchWatch(); stopLockNumber(); stopRadioGuard(); stopTileGuard(); removeHotspot(); super.onDestroy(); }

    // ---------------------------------------------------------------- AD8: what the helper costs on THIS tablet (plain counters on the main thread; read by the report)
    private long connectedAtMs = 0, evSeen = 0, evNoise = 0, scanCount = 0, scanTotalMs = 0, scanMaxMs = 0;
    /** AN1: shade events that started NO scan; the pencil: full tree searches (count / total ms / slowest) and one-node refreshes; when a power menu was last seen up (uptime ms). */
    private long evSkipped = 0, penFull = 0, penFullMs = 0, penFullMax = 0, penFast = 0, menuUpAtMs = -100_000, lastPenFullMs = -100_000;

    /** One line for the tablet report: how long the helper has been connected, how many events it got, how many were dropped at the door, and how long a power-menu scan takes here. A torn read is only a slightly stale number. */
    String perfLine() {
        long up = connectedAtMs == 0 ? 0 : (android.os.SystemClock.elapsedRealtime() - connectedAtMs) / 60_000;
        long n = scanCount;
        String pen = "; pencil cover: " + penFull + " tree searches" + (penFull == 0 ? "" : " (avg " + (penFullMs / penFull) + " ms, max " + penFullMax + " ms)") + ", " + penFast + " one-node refreshes";   // AN1
        return "connected " + up + " min, " + evSeen + " events (" + evNoise + " dropped as noise, " + evSkipped + " shade redraws without a scan), " + n + " power-menu scans" + (n == 0 ? "" : ", avg " + (scanTotalMs / n) + " ms, max " + scanMaxMs + " ms") + pen;
    }

    private void evaluate() {
        long t0 = android.os.SystemClock.uptimeMillis();
        try { evaluateScan(); }
        finally { long d = android.os.SystemClock.uptimeMillis() - t0; scanCount++; scanTotalMs += d; if (d > scanMaxMs) scanMaxMs = d; }
    }

    private void evaluateScan() {
        String mode = Features.powerMode(this);
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        boolean locked = km != null && km.isKeyguardLocked();
        boolean custom = PowerMenuRule.coverWorld(Mode.isCustom(this), locked);   // AD7a: "custom" here = the cover world (custom launcher or ANY lock screen)
        Rect shot = new Rect();
        boolean menuOpen = false, found = false;
        if (custom && !Features.POWER_OFF.equals(mode) && !(locked && Features.POWER_BLOCK.equals(mode))) {   // Y1: the cover also works on the lock screen; BLOCK still stays off it
            List<AccessibilityWindowInfo> windows = getWindows();
            final boolean dialogUp = dialogWindowPresent(windows);
            final boolean everyWindow = android.os.SystemClock.uptimeMillis() < fullScanUntilMs;
            if (menuSeenMs == 0 && dialogUp) menuSeenMs = android.os.SystemClock.uptimeMillis();
            if (dialogUp) menuUpAtMs = android.os.SystemClock.uptimeMillis();   // AN1: a menu is up: its content changes are followed
            if (dialogUp && overlayShown && fading && android.os.SystemClock.uptimeMillis() < scanNotBeforeMs) {   // AZ1: the stock menu is still fading in and the patch follows it: no node scan now (it would block the redraws); a closed menu (dialogUp false) is handled at once
                handler.removeCallbacks(recheck);
                handler.postDelayed(recheck, Math.max(20, scanNotBeforeMs - android.os.SystemClock.uptimeMillis() + 20));
                return;
            }
            if (!overlayShown && Features.isCover(mode)) {
                Rect cached = cachedCard();
                if (cached != null && dialogUp) {
                    showOverlay(cached);   // instant: no node scan yet
                    if (fading) { deferScan(android.os.SystemClock.uptimeMillis()); return; }   // AZ1: the fade is running: the scan (and everything after it) waits for its end
                }
            }
            if (windows != null) {
                for (AccessibilityWindowInfo w : windows) {
                    CharSequence wt = w.getTitle();
                    if (!EventNoise.mayHoldPowerMenu(dialogUp, wt != null && wt.toString().contains("ActionsDialog"), w.getType(), everyWindow)) continue;   // AB2: never fetch + search the foreground app's own tree
                    AccessibilityNodeInfo root = w.getRoot();
                    if (root == null) continue;
                    if (getPackageName().contentEquals(root.getPackageName() == null ? "" : root.getPackageName())) { root.recycle(); continue; }
                    try {
                        List<AccessibilityNodeInfo> off = root.findAccessibilityNodeInfosByText("Power off");
                        List<AccessibilityNodeInfo> sc = root.findAccessibilityNodeInfosByText("Screenshot");
                        if (off != null && !off.isEmpty() && sc != null && !sc.isEmpty()) {
                            menuOpen = true; menuUpAtMs = android.os.SystemClock.uptimeMillis();
                            // the card = the smallest ancestor of "Power off" that also contains the Screenshot entry
                            AccessibilityNodeInfo offItem = clickableAncestor(off.get(0));
                            Rect scr = new Rect(), c = new Rect(), ob = new Rect();
                            offItem.getBoundsInScreen(ob); lastOffTop = ob.top; lastItemH = ob.height();   // V1: the stock item size, so our menu is "stock minus Screenshot"
                            clickableAncestor(sc.get(0)).getBoundsInScreen(scr);
                            // AC6: the entry PITCH (not the height of one clickable node: the entries have margins), so our two-entry card is "the stock panel minus its last entry"
                            lastPitch = -1; lastCut = scr.top > ob.top ? scr.top : -1;
                            List<AccessibilityNodeInfo> rst = root.findAccessibilityNodeInfosByText("Restart");
                            if (rst != null && !rst.isEmpty()) { Rect rb = new Rect(); clickableAncestor(rst.get(0)).getBoundsInScreen(rb); if (rb.top > ob.top) lastPitch = rb.top - ob.top; }
                            if (rst != null) for (AccessibilityNodeInfo rn : rst) { try { rn.recycle(); } catch (Throwable ignored) { } }
                            if (lastPitch <= 0 && scr.top > ob.top) lastPitch = (scr.top - ob.top) / 2;
                            AccessibilityNodeInfo t = offItem;
                            for (int i = 0; i < 6 && t != null; i++) {
                                t.getBoundsInScreen(c);
                                if (c.contains(scr.centerX(), scr.centerY()) && c.height() > scr.height()) { shot.set(c); break; }
                                t = t.getParent();
                            }
                            found = !shot.isEmpty();
                            if (found) dumpMenu(root, locked, shot);   // AC6: what the stock menu really looks like on THIS tablet, for the report (cable-free)
                            break;
                        }
                    } finally { root.recycle(); }
                }
            }
        }
        int action = PowerMenuRule.decide(mode, custom, locked, menuOpen, found);
        if (menuOpen || action != PowerMenuRule.NOTHING) Log.i(TAG, "menuOpen=" + menuOpen + " found=" + found + " bounds=" + shot + " action=" + action);
        if (action == PowerMenuRule.COVER_SCREENSHOT) {
            Features.setString(this, cardKey(), shot.left + "," + shot.top + "," + shot.right + "," + shot.bottom);   // AC6: stored BEFORE the card is built, so the very first open already uses the measured panel and pitch
            Features.setString(this, itemKey(), lastPitch > 0 || lastCut > 0 ? lastPitch + "/" + lastCut : "");   // a failed measurement CLEARS the stored value: a stale one from another layout must never be reused
            showOverlay(shot);
            handler.removeCallbacks(recheck);
            if ((Features.POWER_WALLPAPER.equals(mode) || locked) && patchView == null) refreshLayers();
            handler.postDelayed(recheck, 1500);   // evaluate() again: removes the cover if the menu is gone   // if the menu closed without an event we still clean up
        } else {
            // Menu not (or no longer) open: keep the invisible, pre-built card ready (W3/W4) when the cover mode is on, else drop it.
            if (custom && Features.isCover(mode)) hideOverlay(); else removeOverlay();
            if (action == PowerMenuRule.CLOSE_MENU) performGlobalAction(GLOBAL_ACTION_BACK);
        }
    }


    // ---------------------------------------------------------------- return reset (RESET2): press "Clear all" in the REAL notification shade and the REAL Recents screen
    public static final String RESET_REPORT_KEY = "reset_last";
    private final StringBuilder resetReport = new StringBuilder();

    /**
     * Runs on the main thread. Calls {@code done} when finished (always, even if nothing could be pressed). Fail-open: a missing button just means that step is skipped.
     * SPEED (V3): no fixed sleeps. After opening the shade / Recents it polls every 40 ms and presses "Clear all" the moment it appears; an EMPTY shade / Recents
     * ("No notifications" / "No recent items") ends that step at once.
     */
    public void runReset(final boolean clearNotifications, final boolean clearRecents, final Runnable done) { runReset(clearNotifications, clearRecents, null, done); }

    /** @param closeLabels null = press "Clear all"; otherwise dismiss ONLY the Recents cards of these app labels (D5: Quickstep-opened apps), falling back to Clear all if the cards cannot be found. */
    public void runReset(final boolean clearNotifications, final boolean clearRecents, final java.util.Set<String> closeLabels, final Runnable done) {
        resetReport.setLength(0);
        guardPausedUntil = System.currentTimeMillis() + 10_000;   // Recents / the shade are opened on purpose during the reset
        removeOverlay();
        final long t0 = android.os.SystemClock.uptimeMillis();
        Log.i(TAG, "RESET start notifications=" + clearNotifications + " recents=" + clearRecents);
        final Runnable afterShade = new Runnable() { @Override public void run() {
            if (clearRecents) resetRecents(t0, done, closeLabels); else { resetReport.append("recents: skipped (nothing opened from Quickstep, or the switch is OFF)\n"); finishReset(t0, done); }
        } };
        if (!clearNotifications) {
            resetReport.append("notifications: switch is OFF\n");
            afterShade.run();
            return;
        }
        // Y2a: look at the status bar first. No notification icons there = nothing to clear = the whole shade step is skipped. Unknown (bar hidden / not found) = do the step as before.
        Boolean any = notificationIconsPresent();
        if (any != null && !any) {
            resetReport.append("notifications: none in the status bar, step skipped (+").append(android.os.SystemClock.uptimeMillis() - t0).append(" ms)\n");
            Log.i(TAG, "RESET notifications skipped: status bar shows none");
            afterShade.run();
            return;
        }
        performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
        pollPress("clear all", new String[] {"no notifications"}, new String[] {"com.android.systemui"}, 1000, false, new PollResult() {
            @Override public void done(int result) {
                resetReport.append("notifications: ").append(result == PRESSED ? "cleared" : result == EMPTY ? "none to clear" : "no Clear all button found")
                        .append(" (+").append(android.os.SystemClock.uptimeMillis() - t0).append(" ms)\n");
                Log.i(TAG, "RESET notifications result=" + result + " +" + (android.os.SystemClock.uptimeMillis() - t0) + "ms");
                performGlobalAction(GLOBAL_ACTION_BACK);
                handler.postDelayed(afterShade, ReturnReset.fastActive ? 40 : 90);   // 30 ms was too short on the emulator (Recents opened while the shade was still collapsing); with animations off 40 ms is enough
            }
        });
    }

    /** TRUE = the status bar shows at least one notification icon, FALSE = it shows none, null = cannot tell (then the shade step runs as before). */
    private Boolean notificationIconsPresent() {
        try {
            List<AccessibilityWindowInfo> windows = getWindows();
            if (windows == null) return null;
            for (AccessibilityWindowInfo w : windows) {
                AccessibilityNodeInfo root = w.getRoot();
                if (root == null) continue;
                try {
                    CharSequence pkg = root.getPackageName();
                    if (pkg == null || !"com.android.systemui".contentEquals(pkg)) continue;
                    List<AccessibilityNodeInfo> hit = root.findAccessibilityNodeInfosByViewId("com.android.systemui:id/notificationIcons");
                    if (hit == null || hit.isEmpty()) continue;
                    AccessibilityNodeInfo box = hit.get(0);
                    if (!box.isVisibleToUser()) return null;   // status bar hidden (full-screen app): the empty list means nothing
                    int n = 0;
                    for (int i = 0; i < box.getChildCount(); i++) {
                        AccessibilityNodeInfo ch = box.getChild(i);
                        if (ch == null) continue;
                        if (!KeepAlivePolicy.isOurIcon(ch.getContentDescription())) n++;   // AA2: our own do-nothing notification is not "something to clear"
                        ch.recycle();
                    }
                    return n > 0;
                } finally { root.recycle(); }
            }
        } catch (Throwable t) { Log.e(TAG, "notification icon scan failed", t); }
        return null;
    }

    private static final int PRESSED = 1, EMPTY = 2, TIMEOUT = 3;
    private interface PollResult { void done(int result); }

    /** Polls every 20 ms (first look immediately): press {@code label} if it is there; end with EMPTY if one of {@code emptyTexts} is visible instead; scroll now and then if asked; TIMEOUT after maxMs. */
    private void pollPress(final String label, final String[] emptyTexts, final String[] packages, final long maxMs, final boolean scroll, final PollResult cb) {
        final long deadline = android.os.SystemClock.uptimeMillis() + maxMs;
        final int[] tick = {0};
        handler.post(new Runnable() { @Override public void run() {
            try {
                if (pressInSystemUi(label, packages)) { cb.done(PRESSED); return; }
                if (anyTextPresent(emptyTexts, packages)) { cb.done(EMPTY); return; }
                tick[0]++;
                if (scroll && tick[0] % 5 == 0) scrollSomething(packages);
            } catch (Throwable t) { Log.e(TAG, "poll failed", t); }
            if (android.os.SystemClock.uptimeMillis() >= deadline) { cb.done(TIMEOUT); return; }
            handler.postDelayed(this, 20);
        } });
    }

    private boolean anyTextPresent(String[] texts, String[] packages) {
        if (texts == null || texts.length == 0) return false;
        try {
            List<AccessibilityWindowInfo> windows = getWindows();
            if (windows == null) return false;
            for (AccessibilityWindowInfo w : windows) {
                AccessibilityNodeInfo root = w.getRoot();
                if (root == null) continue;
                try {
                    String pkg = root.getPackageName() == null ? "" : root.getPackageName().toString();
                    boolean ok = false;
                    for (String p : packages) if (p.equals(pkg)) ok = true;
                    if (!ok) continue;
                    for (String t : texts) {
                        List<AccessibilityNodeInfo> hit = root.findAccessibilityNodeInfosByText(t);
                        if (hit != null && !hit.isEmpty()) return true;
                    }
                } finally { root.recycle(); }
            }
        } catch (Throwable t) { Log.e(TAG, "anyTextPresent failed", t); }
        return false;
    }

    private void resetRecents(final long t0, final Runnable done, final java.util.Set<String> closeLabels) {
        performGlobalAction(GLOBAL_ACTION_RECENTS);
        if (closeLabels == null || closeLabels.isEmpty()) { recentsClearAll(t0, done); return; }
        // D5: dismiss only the cards of apps opened from Quickstep (each Quickstep card carries a "Close" accessibility action); anything unexpected falls back to Clear all.
        final long deadline = android.os.SystemClock.uptimeMillis() + 2500;
        final int[] closed = {0}, idle = {0};
        final boolean[] sawCards = {false};
        final String[] seen = {null};   // AI1: what the Recents cards looked like (for the lock log)
        handler.post(new Runnable() { @Override public void run() {
            try {
                List<AccessibilityNodeInfo> cards = findTaskCards();
                if (cards.size() > 0) { sawCards[0] = true; if (seen[0] == null) seen[0] = cards.size() + " card(s): " + describeCards(cards, closeLabels); }
                boolean did = false;
                for (AccessibilityNodeInfo card : cards) {
                    if (!AppOrigin.cardMatches(card.getContentDescription() == null ? null : card.getContentDescription().toString(), closeLabels)) continue;
                    for (AccessibilityNodeInfo.AccessibilityAction act : card.getActionList()) {
                        if (act.getLabel() != null && "close".equalsIgnoreCase(act.getLabel().toString().trim()) && card.performAction(act.getId())) { closed[0]++; did = true; break; }
                    }
                    if (did) break;   // the list changed: look again
                }
                if (did) { idle[0] = 0; handler.postDelayed(this, 30); return; }
                if (sawCards[0] && ++idle[0] >= 3) {   // cards are there and none of ours is left
                    AppOrigin.clearQuickstep(TjAccessibilityService.this); AppOrigin.setTracked(TjAccessibilityService.this);
                    resetReport.append("recents: closed ").append(closed[0]).append(" card(s) of apps opened from Quickstep, others kept (+").append(android.os.SystemClock.uptimeMillis() - t0).append(" ms)\n");
                    Log.i(TAG, "RESET recents selective closed=" + closed[0]);
                    LockLog.add(getApplicationContext(), "RESET", "recents: closed " + closed[0] + " card(s) of " + closeLabels + "; saw " + seen[0]);
                    finishReset(t0, done);
                    return;
                }
            } catch (Throwable t) { Log.e(TAG, "selective recents failed", t); }
            if (android.os.SystemClock.uptimeMillis() >= deadline) {
                if (!sawCards[0]) { Log.i(TAG, "RESET recents: cards not recognised, falling back to Clear all"); LockLog.add(getApplicationContext(), "RESET", "recents: no card with a 'Close' action found for " + closeLabels + " (this Recents screen is not recognised): falling back to Clear all"); recentsClearAll(t0, done); }
                else { resetReport.append("recents: stopped after ").append(closed[0]).append(" card(s)\n"); LockLog.add(getApplicationContext(), "RESET", "recents: stopped after " + closed[0] + " card(s) of " + closeLabels + "; saw " + seen[0]); finishReset(t0, done); }
                return;
            }
            handler.postDelayed(this, 40);
        } });
    }

    /**
     * AI1: what this tablet's Recents cards look like to accessibility, for the lock log. v0.7.6 (review of v0.7.5, privacy): WITHOUT any text we did not already know. A card description is the app label plus
     * possibly a page title or a conversation name, and the lock log goes into the tablet report. So up to five cards are described only by the matching label from {@code known} (the apps we mean to close),
     * a "+" when the description had more text than that label, or "(other, N chars)" when it matches none.
     */
    private static String describeCards(List<AccessibilityNodeInfo> cards, java.util.Set<String> known) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < cards.size() && i < 5; i++) {
            CharSequence d = cards.get(i).getContentDescription();
            String s = d == null ? "" : d.toString().replace('\n', ' ').trim();
            String hit = null;
            if (known != null) for (String k : known) {
                if (k != null && !k.isEmpty() && s.toLowerCase(java.util.Locale.ROOT).contains(k.toLowerCase(java.util.Locale.ROOT))) { hit = k; break; }
            }
            if (i > 0) b.append(" | ");
            b.append(d == null ? "(no description)" : hit == null ? "(other, " + s.length() + " chars)" : hit + (s.length() > hit.length() ? "+" : ""));
        }
        return b.toString();
    }

    /** Recents task cards: nodes (in the stock launcher / SystemUI windows) that offer a "Close" action. */
    private List<AccessibilityNodeInfo> findTaskCards() {
        List<AccessibilityNodeInfo> out = new java.util.ArrayList<>();
        List<AccessibilityWindowInfo> windows = getWindows();
        if (windows == null) return out;
        for (AccessibilityWindowInfo w : windows) {
            AccessibilityNodeInfo root = w.getRoot();
            if (root == null) continue;
            CharSequence pkg = root.getPackageName();
            if (pkg == null || !(pkg.toString().startsWith("com.android.launcher3") || "com.android.systemui".contentEquals(pkg))) continue;
            collectCards(root, 0, out);
        }
        return out;
    }

    private void collectCards(AccessibilityNodeInfo n, int depth, List<AccessibilityNodeInfo> out) {
        if (n == null || depth > 8) return;
        for (AccessibilityNodeInfo.AccessibilityAction a : n.getActionList()) {
            if (a.getLabel() != null && "close".equalsIgnoreCase(a.getLabel().toString().trim())) { out.add(n); return; }
        }
        for (int i = 0; i < n.getChildCount(); i++) collectCards(n.getChild(i), depth + 1, out);
    }

    private void recentsClearAll(final long t0, final Runnable done) {
        pollPress("clear all", new String[] {"no recent items"}, new String[] {"com.android.launcher3", "com.android.systemui"}, 1800, true, new PollResult() {
            @Override public void done(int result) {
                if (result == PRESSED || result == EMPTY) { RecentsDirty.clear(TjAccessibilityService.this); AppOrigin.clearQuickstep(TjAccessibilityService.this); AppOrigin.setTracked(TjAccessibilityService.this); }   // Recents is clean again
                resetReport.append("recents: ").append(result == PRESSED ? "cleared (apps closed)" : result == EMPTY ? "already empty" : "no Clear all button found")
                        .append(" (+").append(android.os.SystemClock.uptimeMillis() - t0).append(" ms)\n");
                Log.i(TAG, "RESET recents result=" + result + " +" + (android.os.SystemClock.uptimeMillis() - t0) + "ms");
                LockLog.add(getApplicationContext(), "RESET", "recents: Clear all " + (result == PRESSED ? "pressed" : result == EMPTY ? "found the list empty" : "button NOT FOUND: nothing was cleared"));   // AI1: the lock log says what really happened
                finishReset(t0, done);
            }
        });
    }

    private void finishReset(final long t0, final Runnable done) {
        // NO Home press here: on the slow tablet it landed AFTER the app start and covered it.
        handler.postDelayed(new Runnable() { @Override public void run() {
            resetReport.append("took ").append(android.os.SystemClock.uptimeMillis() - t0).append(" ms\n");
            Features.setString(TjAccessibilityService.this, RESET_REPORT_KEY, resetReport.toString());
            try { done.run(); } catch (Throwable t) { Log.e(TAG, "reset done failed", t); }
        } }, ReturnReset.fastActive ? 15 : 40);
    }

    /** True if a window of this package is the active (front) one. */
    public boolean isForeground(String pkg) {
        try {
            AccessibilityNodeInfo r = getRootInActiveWindow();
            if (r == null) return false;
            try { return pkg.contentEquals(r.getPackageName() == null ? "" : r.getPackageName()); } finally { r.recycle(); }
        } catch (Throwable t) { return false; }
    }

    public void appendReport(String line) {
        resetReport.append(line).append("\n");
        Features.setString(this, RESET_REPORT_KEY, resetReport.toString());
    }

    /** Presses the first clickable node whose text starts with {@code label}, but ONLY inside windows of the given system packages (never in another app). */
    private boolean pressInSystemUi(String label, String... packages) {
        try {
            List<AccessibilityWindowInfo> windows = getWindows();
            if (windows == null) return false;
            for (AccessibilityWindowInfo w : windows) {
                AccessibilityNodeInfo root = w.getRoot();
                if (root == null) continue;
                try {
                    String pkg = root.getPackageName() == null ? "" : root.getPackageName().toString();
                    boolean ok = false;
                    for (String p : packages) if (p.equals(pkg)) ok = true;
                    if (!ok) continue;
                    List<AccessibilityNodeInfo> hit = root.findAccessibilityNodeInfosByText(label);
                    if (hit == null) continue;
                    for (AccessibilityNodeInfo n : hit) {
                        CharSequence t = n.getText() != null ? n.getText() : n.getContentDescription();
                        if (t == null || !t.toString().trim().toLowerCase(java.util.Locale.ROOT).startsWith(label)) continue;
                        if (clickableAncestor(n).performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true;
                    }
                } finally { root.recycle(); }
            }
        } catch (Throwable t) { Log.e(TAG, "pressInSystemUi failed", t); }
        return false;
    }

    private void scrollSomething(String... packages) {
        try {
            List<AccessibilityWindowInfo> windows = getWindows();
            if (windows == null) return;
            for (AccessibilityWindowInfo w : windows) {
                AccessibilityNodeInfo root = w.getRoot();
                if (root == null) continue;
                try {
                    String pkg = root.getPackageName() == null ? "" : root.getPackageName().toString();
                    boolean ok = false;
                    for (String p : packages) if (p.equals(pkg)) ok = true;
                    if (!ok) continue;
                    AccessibilityNodeInfo sc = findScrollable(root, 0);
                    if (sc != null && sc.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) return;
                } finally { root.recycle(); }
            }
        } catch (Throwable t) { Log.e(TAG, "scroll failed", t); }
    }

    private AccessibilityNodeInfo findScrollable(AccessibilityNodeInfo n, int depth) {
        if (n == null || depth > 8) return null;
        if (n.isScrollable()) return n;
        for (int i = 0; i < n.getChildCount(); i++) {
            AccessibilityNodeInfo r = findScrollable(n.getChild(i), depth + 1);
            if (r != null) return r;
        }
        return null;
    }

    /** AC6: writes the stock menu's node tree + the numbers used into the report file (at most once per 20 s per lock state). Never throws. */
    private void dumpMenu(AccessibilityNodeInfo root, boolean locked, Rect panel) {
        try {
            if (!PowerMenuDump.due(locked)) return;
            android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
            ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
            int[] card = PowerMenuDump.cardFromPanel(panel.left, panel.top, panel.right, panel.bottom, lastPitch, lastCut);
            StringBuilder h = new StringBuilder();
            h.append("written ").append(now()).append(" in the ").append(Mode.isCustom(this) ? "CUSTOM" : "Quickstep").append(" launcher, ").append(locked ? "on the LOCK SCREEN" : "unlocked").append('\n');
            h.append("display ").append(dm.widthPixels).append('x').append(dm.heightPixels).append(" px, rotation ").append(rotation()).append(", density ").append(dm.density).append(" (").append(dm.densityDpi).append(" dpi), font scale ").append(getResources().getConfiguration().fontScale).append('\n');
            h.append("stock panel ").append(panel.toShortString()).append(" (").append(panel.width()).append('x').append(panel.height()).append("), first entry top ").append(lastOffTop).append(" height ").append(lastItemH).append(", entry pitch ").append(lastPitch).append(", Screenshot entry top ").append(lastCut).append('\n');
            h.append("our card would be [").append(card[0]).append(',').append(card[1]).append("][").append(card[2]).append(',').append(card[3]).append("] (").append(card[2] - card[0]).append('x').append(card[3] - card[1]).append(")\n");
            h.append("patch: ").append(Features.getString(this, PATCH_STATUS_KEY, "?")).append(" | calibration: ").append(Features.getString(this, CAL_STATUS_KEY, "?")).append(" | brightness nudge ").append(Features.getInt(this, PATCH_DIM_KEY, 0)).append(" % | patch nudges dx ").append(Features.getInt(this, PATCH_DX_KEY, 0)).append(" dy ").append(Features.getInt(this, PATCH_DY_KEY, 0)).append(" size ").append(Features.getInt(this, PATCH_SCALE_KEY, 0)).append(" %\n");
            h.append("tree of the menu window (class #id 'label' [left,top][right,bottom] click):\n");
            PowerMenuDump.write(this, locked, h.toString(), PowerMenuDump.walk(root));
        } catch (Throwable t) { Log.e(TAG, "power menu dump failed", t); }
    }

    private AccessibilityNodeInfo clickableAncestor(AccessibilityNodeInfo n) {
        AccessibilityNodeInfo t = n;
        for (int i = 0; i < 4 && t != null && !t.isClickable(); i++) t = t.getParent();
        return t != null && t.isClickable() ? t : n;
    }

    /** Presses the real system button with this label (found fresh, never a stale node). Returns false if it could not. */
    private boolean pressReal(String label) {
        try {
            List<AccessibilityWindowInfo> windows = getWindows();
            if (windows == null) return false;
            for (AccessibilityWindowInfo w : windows) {
                AccessibilityNodeInfo root = w.getRoot();
                if (root == null) continue;
                // never "press" our own card: it is the topmost window and carries the same labels (that was an endless self-click)
                if (getPackageName().contentEquals(root.getPackageName() == null ? "" : root.getPackageName())) { root.recycle(); continue; }
                try {
                    List<AccessibilityNodeInfo> hit = root.findAccessibilityNodeInfosByText(label);
                    if (hit != null && !hit.isEmpty()) {
                        boolean ok = clickableAncestor(hit.get(0)).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.i(TAG, "press '" + label + "' -> " + ok);
                        return ok;
                    }
                } finally { root.recycle(); }
            }
        } catch (Throwable t) { Log.e(TAG, "press failed", t); }
        return false;
    }

    /**
     * AH3 (v0.7.6): the layout INSIDE the card follows the stock menu, measured on Tj's side-by-side photos (stock card 110.8 x 208.0 px = 105 x 196 dp): 16 dp top padding, then two entries of 90 dp; in each the
     * icon (24 dp, its glyph 2 dp inside) starts 20 dp down and the 12 dp label starts 5 dp below the icon. The stock label ignores the font scale (its "Power off" is 54 px wide, ours was 64). Before: two equal
     * halves with centred content, which put the first icon about 11 px too high, the entries about 9 px too far apart and the labels 16 % too big. {@code k} scales everything with the card's height.
     */
    private View cardButton(String label, int iconRes, final String realLabel, float d, float k) {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setGravity(Gravity.CENTER_HORIZONTAL);
        b.setPadding(0, Math.round(20 * d * k), 0, 0);
        b.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(90 * d * k)));
        android.widget.ImageView icon = new android.widget.ImageView(this);
        icon.setImageDrawable(getDrawable(iconRes));
        b.addView(icon, new LinearLayout.LayoutParams(Math.round(24 * d * k), Math.round(24 * d * k)));
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, 12 * d * k);   // dp, not sp: the stock label does not follow the font scale
        t.setIncludeFontPadding(false);
        t.setTextColor(Color.parseColor("#FF5F6368"));
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, Math.round(5 * d * k), 0, 0);
        b.addView(t);
        b.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, android.view.MotionEvent e) {
                Log.i(TAG, "card touch " + realLabel + " action=" + e.getActionMasked());
                return false;
            }
        });
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Log.i(TAG, "card click " + realLabel);
                // fail open: if the real button cannot be pressed, take our card away so the original menu is usable
                if (!pressReal(realLabel)) { Log.i(TAG, "press '" + realLabel + "' -> false (revealing original menu)"); removeOverlay(); }
            }
        });
        return b;
    }

    /** {@code heightPx} = the card window's height; the layout (16 dp + 2 x 90 dp = 196 dp at the stock size) scales with it between 0.8 and 1.25, so another panel size still fits. */
    private View buildCard(int heightPx) {
        float d = getResources().getDisplayMetrics().density;
        float k = Math.max(0.8f, Math.min(1.25f, heightPx / (196f * d)));
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(PANEL_COLOR);
        card.setPadding(0, Math.round(16 * d * k), 0, 0);
        card.addView(cardButton("Power off", R.drawable.tj_ic_power, "Power off", d, k));
        card.addView(cardButton("Restart", R.drawable.tj_ic_restart, "Restart", d, k));
        return card;
    }

    private boolean overlayShown = false, overlayTouchable = false;

    private WindowManager.LayoutParams overlayParams(Rect r, boolean touchable) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                r.width(), r.height(), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | (touchable ? 0 : WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE),
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = r.left; lp.y = r.top;
        return lp;
    }

    /** V1: on the lock screen the patch is invisible until the power menu opens (and again afterwards). */
    private void patchShow(boolean on) {
        if (patchView == null) return;
        if (!patchLockMode) { boolean ch = patchOpenHome != on; patchOpenHome = on; if (ch) patchView.invalidate(); return; }   // AH1: the home patch dims like the stock dialog's scrim while the menu is open
        // AE3: "on" = our cover is up for an open menu (the full, dimmed patch); otherwise the patch RESTS visible over the Screenshot slot if the switch is on and the PIN pad is not up, else it stays invisible
        boolean vis = PatchRest.visible(on, lockPatchAlways(), true, lastBouncer);
        boolean rest = vis && !on;
        boolean changed = rest != patchResting;
        patchResting = rest;
        patchView.setAlpha(vis ? 1f : 0f);
        if (!on) setPatchTouchable(false);   // review of v0.8.0: whenever our menu is NOT open the patch can never swallow a touch, whichever path closed the menu (no disk write and no extra binder call in this time-critical path any more)
        if (changed) { patchView.invalidate(); Log.i(TAG, "PATCH rest " + (rest ? "on" : "off") + (lastBouncer ? " (PIN pad up)" : "") + " top=" + patchRestTop); }
    }

    /**
     * V1 (Tj): our menu is "the stock menu minus the Screenshot button": same panel, Power off + Restart only. The wallpaper patch underneath fills what the rest of the stock menu
     * (Screenshot, Emergency ...) would have covered. Without a patch below we keep the whole rectangle so a real Screenshot button can never show.
     * AC6 (Tj, 2026-11-05: "the buttons and card are too small"): the card is the PANEL minus one entry pitch ({@link PowerMenuDump#cardFromPanel}); it used to start at the top of the first entry and be
     * twice the height of ONE clickable node, which ignores the panel's padding and the gaps between entries (about a quarter shorter than the stock two-entry area).
     */
    private Rect cardArea(Rect full) {
        if (patchView == null) return full;
        return cardAreaGeom(full);
    }

    /** AH3: the card geometry WITHOUT the "is a patch below" guard of {@link #cardArea}: the patch window itself is derived from it, before any patch exists. */
    private Rect cardAreaGeom(Rect full) {
        try {
            String v = Features.getString(this, itemKey(), "");
            int sl = v.indexOf('/'), pitch = -1, cut = -1;
            if (sl > 0) { pitch = Integer.parseInt(v.substring(0, sl).trim()); cut = Integer.parseInt(v.substring(sl + 1).trim()); }
            int[] c = PowerMenuDump.cardFromPanel(full.left, full.top, full.right, full.bottom, pitch, cut);
            if (c[3] - c[1] < full.height() / 2) return full;                                  // nonsense geometry: keep the whole rectangle
            return new Rect(c[0], c[1], c[2], c[3]);
        } catch (NumberFormatException e) { return full; }
    }

    /**
     * AM1 (Tj, 2026-11-06: "the power menu doesn't need to be rebuilt or changed at all ... it can stay completely stock"): CARDLESS. The stock Power off / Restart entries stay untouched. The patch over the
     * Screenshot spot, resting there since the lock screen came up, only switches to its open-menu brightness and swallows a tap on the hidden Screenshot spot (a tap there closes the menu, like a tap outside).
     * The "CARD area" log line is kept: the emulator sections parse it for the geometry.
     */
    private void showStock(Rect full) {
        ensurePatch();
        if (!overlayShown) {
            Log.i(TAG, "CARD area=" + cardAreaGeom(full).toShortString() + " full=" + full.toShortString() + " lockPatch=" + patchLockMode + " stock=true");
            overlayShown = true; cardShownMs = android.os.SystemClock.uptimeMillis(); recordLatency();
        }
        patchShow(true);
        setPatchTouchable(true);
    }

    private WindowManager.LayoutParams patchParams(boolean touchable) {
        WindowManager.LayoutParams lp = overlayParams(patchAt, touchable);
        lp.format = patchLockMode ? PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE;
        lp.setTitle("TjWallpaperPatch");
        return lp;
    }

    private void setPatchTouchable(boolean t) {
        if (patchView == null || t == patchTouchable || !patchLockMode) return;   // the home patch never swallows a touch
        try { ((WindowManager) getSystemService(WINDOW_SERVICE)).updateViewLayout(patchView, patchParams(t)); patchTouchable = t; } catch (Throwable ignored) { }
    }

    // ---------------------------------------------------------------- AM1: the patch from a REAL screenshot of the lock screen (see PatchShot)
    private void loadStoredShot() {
        patchShotBmp = null;
        try {
            PatchShot.Loaded l = PatchShot.load(this, rotation(), shotSig());
            if (l != null) { patchShotBmp = l.bmp; patchShotLeft = l.left; patchShotTop = l.top; }
        } catch (Throwable t) { Log.e(TAG, "stored patch shot failed", t); }
    }

    /** What a stored crop is valid for: this wallpaper picture, this rotation, this display size. */
    private String shotSig() {
        long id = 0;
        try { android.app.WallpaperManager wm = android.app.WallpaperManager.getInstance(this);
              id = wm.getWallpaperId(android.app.WallpaperManager.FLAG_LOCK); if (id <= 0) id = wm.getWallpaperId(android.app.WallpaperManager.FLAG_SYSTEM); } catch (Throwable ignored) { }
        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
        return PatchShot.sig(id, rotation(), dm.widthPixels, dm.heightPixels);
    }

    private void shotStatus(String s) { try { Features.setString(this, PatchShot.STATUS_KEY, s); } catch (Throwable ignored) { } }

    private long calibrationUntilMs() { try { return Long.parseLong(Features.getString(this, PatchShot.CAL_UNTIL_KEY, "0").trim()); } catch (NumberFormatException e) { return 0; } }

    private final Runnable shotIngest = new Runnable() { @Override public void run() { try { ingestShot(); } catch (Throwable t) { Log.e(TAG, "patch shot ingest failed", t); } } };
    private final Runnable shotEnd = new Runnable() { @Override public void run() {
        try {
            if (calibrating()) return;
            if (Features.getString(TjAccessibilityService.this, PatchShot.CAL_UNTIL_KEY, "0").length() > 1) { shotStatus(now() + "  the calibration window ended without a usable screenshot"); Features.setString(TjAccessibilityService.this, PatchShot.CAL_UNTIL_KEY, "0"); }
            calibrationChanged();
        } catch (Throwable t) { Log.e(TAG, "calibration end failed", t); }
    } };
    private final Runnable autoShot = new Runnable() { @Override public void run() {
        try {
            if (!calibrating() || autoShotDone) return;
            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(POWER_SERVICE);
            boolean ready = pm != null && pm.isInteractive() && keyguardLockedFast() && !lastBouncer && !overlayShown && patchView != null;
            if (!ready) { if (++autoShotTries < 12) handler.postDelayed(this, 2000); return; }   // wait for the lock screen to be up, the menu closed, no PIN pad
            autoShotDone = true;
            Log.i(TAG, "PATCHSHOT asking the system for a screenshot of the lock screen");
            shotStatus(now() + "  taking the screenshot of the lock screen ...");
            performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT);
        } catch (Throwable t) { Log.e(TAG, "auto screenshot failed", t); }
    } };

    /** The calibration window opened or closed (Launcher control, the end of the window, a service connect): hide / bring back the resting patch, watch the media store, schedule the screenshot. */
    private void calibrationChanged() {
        if (calibrating()) {
            startShotWatch();
            autoShotDone = false; autoShotTries = 0;
            handler.removeCallbacks(autoShot); handler.postDelayed(autoShot, 2500);
            handler.removeCallbacks(shotEnd); handler.postDelayed(shotEnd, Math.max(1000, calibrationUntilMs() - System.currentTimeMillis() + 500));
        } else { stopShotWatch(); handler.removeCallbacks(autoShot); handler.removeCallbacks(shotEnd); }
        layerRefresh.run();
        patchShow(overlayShown);   // the resting patch is hidden while calibrating (it must not be inside the screenshot) and comes back afterwards
    }

    private void startShotWatch() {
        if (shotObserver != null) return;
        shotObserver = new android.database.ContentObserver(handler) {
            @Override public void onChange(boolean selfChange) { handler.removeCallbacks(shotIngest); handler.postDelayed(shotIngest, 900); }
        };
        try { getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, shotObserver); } catch (Throwable t) { Log.e(TAG, "media watch failed", t); shotObserver = null; }
    }

    private void stopShotWatch() {
        if (shotObserver == null) return;
        try { getContentResolver().unregisterContentObserver(shotObserver); } catch (Throwable ignored) { }
        shotObserver = null;
    }

    /** A screenshot appeared while calibrating: crop the patch window out of it (worker thread) and keep the crop. Every refusal says why in Launcher control. */
    private void ingestShot() {
        if (!calibrating()) return;
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) { shotStatus(now() + "  needs permission: Launcher control > Allow wallpaper access"); return; }
        final android.content.Context app = getApplicationContext();
        final long start = calibrationUntilMs() - PatchShot.WINDOW_MS;
        final boolean locked = keyguardLockedFast();
        final Rect win = patchView != null && patchLockMode ? new Rect(patchAt) : null;
        final int rot = rotation(); final String sig = shotSig();
        final android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
        new Thread(new Runnable() { @Override public void run() {
            try {
                android.net.Uri u = PatchShot.newestScreenshot(app, start);
                if (u == null) return;
                long id = android.content.ContentUris.parseId(u);
                if (id == lastShotId) return;
                lastShotId = id;
                if (!locked) { shotStatus(now() + "  that screenshot was NOT taken on the lock screen: lock the tablet, wake it and take it there"); return; }
                if (win == null) { shotStatus(now() + "  the power menu rectangle is not learned yet: hold the side key once on the lock screen, then calibrate again"); return; }
                android.graphics.Bitmap full = PatchShot.decode(app, u);
                if (full == null) { shotStatus(now() + "  the screenshot could not be read"); return; }
                int W = full.getWidth(), H = full.getHeight();
                if (W != dm.widthPixels || H != dm.heightPixels) { shotStatus(now() + "  the screenshot is " + W + "x" + H + " but the screen is " + dm.widthPixels + "x" + dm.heightPixels + ": take it in the same orientation"); full.recycle(); return; }
                long sr = 0, sg = 0, sb = 0; int n = 0;
                for (int y = Math.max(0, win.top); y < Math.min(H, win.bottom); y += 3) for (int x = Math.max(0, win.left); x < Math.min(W, win.right); x += 3) { int q = full.getPixel(x, y); sr += (q >> 16) & 255; sg += (q >> 8) & 255; sb += q & 255; n++; }
                if (n > 0 && PatchShot.looksLikeMenu(sr / (double) n, sg / (double) n, sb / (double) n)) { shotStatus(now() + "  the power menu was open in that screenshot: take it with the lock screen showing and the menu closed"); full.recycle(); return; }
                int[] cr = PatchShot.cropRect(win.left, win.top, win.right, win.bottom, PatchShot.MARGIN_PX, W, H);
                android.graphics.Bitmap crop = android.graphics.Bitmap.createBitmap(full, cr[0], cr[1], cr[2] - cr[0], cr[3] - cr[1]);
                final boolean saved = PatchShot.save(app, rot, crop, sig, cr[0], cr[1]);
                final String dims = crop.getWidth() + "x" + crop.getHeight();
                full.recycle();
                handler.post(new Runnable() { @Override public void run() { try {
                    if (!saved) { shotStatus(now() + "  the crop could not be stored"); return; }
                    Features.setString(TjAccessibilityService.this, PatchShot.CAL_UNTIL_KEY, "0");
                    removeOverlay(); removePatch(); refreshLayers();   // rebuild the patch with the stored real pixels
                    calibrationChanged();
                    shotStatus(now() + "  PATCH CALIBRATED (" + dims + "): the patch now shows your real lock screen at the Screenshot spot");
                    Log.i(TAG, "PATCHSHOT stored a " + dims + " crop for rotation " + rot);
                } catch (Throwable t) { Log.e(TAG, "patch shot apply failed", t); } } });
            } catch (Throwable t) { Log.e(TAG, "patch shot ingest failed", t); shotStatus(now() + "  failed: " + t); }
        } }, "tj-patchshot").start();
    }

    /** W3/W4: the card window can stay attached but invisible and untouchable (alpha 0 + NOT_TOUCHABLE) so showing it costs one redraw, not a window creation. */
    private void warmUp() {
        if (overlay != null || !coverWorld() || !Features.isCover(Features.powerMode(this))) return;
        Rect cached = cachedCard();
        if (cached == null) return;
        ensurePatch();   // the wallpaper patch must be added BEFORE the card so the card is on top
        if (!cardWanted()) return;   // AM1 (Tj): the stock menu stays completely stock: the patch alone hides the Screenshot spot
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Rect area = cardArea(cached);
        overlay = buildCard(area.height());
        overlay.setAlpha(0f);
        wm.addView(overlay, overlayParams(area, false));
        shownAt.set(area);
        overlayShown = false; overlayTouchable = false;
    }

    private void showOverlay(Rect full) {
        if (!cardWanted()) { showStock(full); return; }
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (overlay == null) {
            ensurePatch();
            Rect r0 = cardArea(full);
            Log.i(TAG, "CARD area=" + r0.toShortString() + " full=" + full.toShortString() + " lockPatch=" + patchLockMode);
            patchShow(true);
            overlay = buildCard(r0.height());
            wm.addView(overlay, overlayParams(r0, true));
            shownAt.set(r0);
            overlayShown = true; overlayTouchable = true;
            cardShownMs = android.os.SystemClock.uptimeMillis(); recordLatency();
            return;
        }
        Rect r = cardArea(full);
        if (!overlayShown) Log.i(TAG, "CARD area=" + r.toShortString() + " full=" + full.toShortString() + " lockPatch=" + patchLockMode);
        patchShow(true);
        if (!overlayShown) { overlay.setAlpha(1f); overlayShown = true; cardShownMs = android.os.SystemClock.uptimeMillis(); recordLatency(); }          // visible first (one redraw) ...
        if (!overlayTouchable || !shownAt.equals(r)) {                              // ... then touchable / moved
            wm.updateViewLayout(overlay, overlayParams(r, true));
            shownAt.set(r); overlayTouchable = true;
        }
    }

    /** Back to the warm state: still attached, invisible, untouchable. */
    private void hideOverlay() {
        handler.removeCallbacks(recheck);
        if (overlay == null && !cardWanted()) {   // AM1: cardless: the patch goes back to resting (or hidden)
            if (overlayShown) { overlayShown = false; patchShow(false); setPatchTouchable(false); if (patchLockMode) { handler.removeCallbacks(calTrigger); handler.postDelayed(calTrigger, 1200); } }
            else warmUp();
            return;
        }
        if (overlay == null) { warmUp(); return; }
        if (!overlayShown) return;
        patchShow(false);
        if (patchLockMode) { handler.removeCallbacks(calTrigger); handler.postDelayed(calTrigger, 1200); }
        overlay.setAlpha(0f);
        overlayShown = false;
        try {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).updateViewLayout(overlay, overlayParams(shownAt, false));
            overlayTouchable = false;
        } catch (Throwable t) { removeOverlay(); }
    }

    private void removeOverlay() {
        handler.removeCallbacks(recheck);
        if (overlay == null) { if (overlayShown) { overlayShown = false; try { patchShow(false); setPatchTouchable(false); } catch (Throwable ignored) { } } return; }   // AM1: cardless cover state
        patchShow(false);
        try { ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(overlay); } catch (Throwable ignored) { }
        overlay = null;
        overlayShown = false; overlayTouchable = false;
        shownAt.setEmpty();
    }
}
