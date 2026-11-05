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
    private static final int PANEL_COLOR = Color.parseColor("#EFF1F3");   // measured on the AOSP Android 10 power menu; Tj's panel may differ slightly

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
        try { trackForeground(event); } catch (Throwable t) { Log.e(TAG, "foreground tracking failed", t); }
        // Never react to our own overlay (adding it fires events: that would be a feedback loop), and coalesce bursts of events into one check.
        if (event != null && getPackageName().contentEquals(event.getPackageName() == null ? "" : event.getPackageName())) return;
        if (event != null && EventNoise.isNoise(event.getEventType(), event.getPackageName())) {   // AB2: another app's content change: it cannot open the power menu, the shade or the PIN pad
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
        if (Features.POWER_OFF.equals(Features.powerMode(this)) || !Mode.isCustom(this)) {
            if (overlay != null) removeOverlay();
            return;   // nothing to do and nothing scanned: zero cost when the feature is off
        }
        // Leading edge: evaluate on the FIRST event at once (the cover must appear as fast as possible), then once more shortly after to catch late content.
        long now = android.os.SystemClock.uptimeMillis();
        if (now - lastEvalMs >= 40) {
            lastEvalMs = now;
            try { evaluate(); } catch (Throwable t) { Log.e(TAG, "evaluate failed", t); removeOverlay(); }
        }
        if (!pending) { pending = true; handler.postDelayed(debounced, 80); }
    }

    private long lastEvalMs = 0;
    private long fullScanUntilMs = 0;

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
    }

    // ---------------------------------------------------------------- W3b: wallpaper patch under the replacement card (Tj: permanent, custom launcher only)
    public static final String PATCH_STATUS_KEY = "wallpaper_patch_status";
    public static final String PATCH_DX_KEY = "patch_dx", PATCH_DY_KEY = "patch_dy", PATCH_SCALE_KEY = "patch_scale_pct";   // alignment nudges in px / percent
    /** AC6: brightness nudge of the lock-screen patch in percent points (Launcher control). The wallpaper MODEL is drawn darker by {@link #DIM_KEYGUARD} because the real keyguard darkens its wallpaper. */
    public static final String PATCH_DIM_KEY = "patch_dim_pct";
    /** AC6: how much of its brightness the real lock screen keeps: MEASURED on the Android 10 emulator (run 74: (173,96,20) on screen vs (229,122,26) in the wallpaper = 0.755 / 0.787 / 0.77). A first estimate for the tablet's MtkSystemUI. */
    static final float DIM_KEYGUARD = 0.77f;
    private View patchView;
    private android.graphics.Bitmap wallpaperBmp;
    /** V1: the LOCK-screen wallpaper (may differ from the home wallpaper), only held while the lock screen is up. */
    private android.graphics.Bitmap lockBmp;
    private boolean patchLockMode, targetIsLock;
    public static final String LOCK_PATCH_KEY = "lock_patch";   // Tj V1: wallpaper patch + short card on the lock screen (default ON; needs the wallpaper permission)
    private long lastOffTop = -1, lastItemH = -1;
    private int lastPitch = -1;   // AC6: distance between the tops of neighbouring menu entries (Power off -> Restart), measured on every scan
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
    private String itemKey() { return "power_item_r" + rotation(); }
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
            if ("mode".equals(key)) handler.post(new Runnable() { @Override public void run() { updateHotspot(); BatterySaverGuard.check(TjAccessibilityService.this, "the mode changed"); } });
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

    private void patchStatus(String s) {
        if (!s.equals(Features.getString(this, PATCH_STATUS_KEY, ""))) Features.setString(this, PATCH_STATUS_KEY, s);
    }

    /** All the reasons NOT to cover the system menu with a wallpaper patch; null = go. Each reason is shown in Launcher control. */
    private boolean lockPatchWanted() {
        return Features.get(this, LOCK_PATCH_KEY, true) && Features.isCover(Features.powerMode(this)) && Mode.isCustom(this) && keyguardLockedFast();
    }

    private Rect patchTarget() {
        targetIsLock = false;
        if (keyguardLocked()) {   // V1: the lock screen has its own rules (the wallpaper there may be another picture, and nothing may be painted over the clock / PIN pad)
            if (!Features.get(this, LOCK_PATCH_KEY, true)) { patchStatus("off: lock-screen patch switched off"); return null; }
            if (!Features.isCover(Features.powerMode(this)) || !Mode.isCustom(this)) { patchStatus("off: lock screen, power menu cover off or Quickstep mode"); return null; }
            Rect r = cachedCard();
            if (r == null) { patchStatus("waiting: open the power menu once on the lock screen so the helper learns where it sits"); return null; }
            if (loadLockWallpaper() == null) return null;
            targetIsLock = true;
            return patchRect(r);
        }
        if (!Features.POWER_WALLPAPER.equals(Features.powerMode(this))) return null;
        if (!Mode.isCustom(this)) { patchStatus("off: Quickstep mode"); return null; }
        if (!TjHooks.homeScreenShowing()) { patchStatus("off: only shown on the home screen (not over apps, not in All Apps)"); return null; }
        Rect r = cachedCard();
        if (r == null) { patchStatus("waiting: open the power menu once so the helper learns where it sits (this screen orientation)"); return null; }
        if (loadWallpaper() == null) return null;
        return patchRect(r);
    }

    /** V3: the patch covers the ENTIRE 3-button menu: the learned rectangle plus the panel's margin / shadow. */
    private Rect patchRect(Rect r) {
        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
        int m = (int) (12 * dm.density);
        return new Rect(Math.max(0, r.left - m), Math.max(0, r.top - m), Math.min(dm.widthPixels, r.right + m), Math.min(dm.heightPixels, r.bottom + m));
    }

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
        final android.graphics.Bitmap wall = lockBmp;
        final float skew = testSkew();
        new Thread(new Runnable() { @Override public void run() {
            String result = "failed";
            try {
                ShellHelper.run("MKDIR_PUBLISH");
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
            finally { calBusy = false; calStatus(now() + "  " + result); }
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
                if (Features.isCover(Features.powerMode(this)) && Mode.isCustom(this)) warmUp();   // plain cover mode keeps its pre-built card
                return;
            }
            if (patchView != null && (!patchAt.equals(want) || patchRot != rotation() || patchLockMode != targetIsLock)) { removeOverlay(); removePatch(); }
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
        View v = new View(this) {
            private final android.graphics.Paint p = new android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG);
            private final android.graphics.Matrix m = new android.graphics.Matrix();
            @Override protected void onDraw(android.graphics.Canvas c) {
                if (patchLockMode && lockShot != null) { c.drawBitmap(lockShot, lockShotDx, lockShotDy, p); return; }   // V4: the photo of the real lock screen
                android.graphics.Bitmap b = patchLockMode ? lockBmp : wallpaperBmp;
                if (b == null) return;
                // AC6: the real lock screen is darker than its wallpaper (keyguard scrim), so the MODEL patch is drawn darker too (the photo path above already has the real colours)
                if (patchLockMode) { float k = Math.max(0.3f, Math.min(1f, DIM_KEYGUARD * (1f + Features.getInt(TjAccessibilityService.this, PATCH_DIM_KEY, 0) / 100f))); p.setColorFilter(new android.graphics.PorterDuffColorFilter(Color.argb(Math.round((1f - k) * 255), 0, 0, 0), android.graphics.PorterDuff.Mode.SRC_ATOP)); }
                else p.setColorFilter(null);
                // How Android 10's wallpaper engine places the picture: scaled to COVER the display, centred vertically, shifted sideways by the launcher's offset.
                float s = Math.max((float) dm.heightPixels / b.getHeight(), (float) dm.widthPixels / b.getWidth());
                float dw = b.getWidth() * s, dh = b.getHeight() * s;
                float cal = 1f + Features.getInt(TjAccessibilityService.this, PATCH_SCALE_KEY, 0) / 100f;   // Tj's alignment nudges (Launcher control)
                if (patchLockMode) cal *= testSkew() * lockCalM;                                           // V4: the measured fit (1 until calibrated)
                s *= cal; dw = b.getWidth() * s; dh = b.getHeight() * s;
                m.setScale(s, s);
                m.postTranslate(-(dw - dm.widthPixels) * (patchLockMode ? 0.5f : TjHooks.wallpaperX) - rect.left + Features.getInt(TjAccessibilityService.this, PATCH_DX_KEY, 0) + (patchLockMode ? lockCalDx : 0),
                        -(dh - dm.heightPixels) * 0.5f - rect.top + Features.getInt(TjAccessibilityService.this, PATCH_DY_KEY, 0) + (patchLockMode ? lockCalDy : 0));
                c.drawBitmap(b, m, p);
            }
        };
        WindowManager.LayoutParams lp = overlayParams(r, false);
        lp.format = targetIsLock ? PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE;   // V1: an OPAQUE window ignores alpha 0 (run 71: the lock patch stayed visible over the notifications and was even photographed by the calibration); TRANSLUCENT can really be hidden
        lp.setTitle("TjWallpaperPatch");
        ((WindowManager) getSystemService(WINDOW_SERVICE)).addView(v, lp);
        patchLockMode = targetIsLock;
        if (patchLockMode) v.setAlpha(0f);   // V1: on the lock screen it is only visible while the power menu is open (it must never paint over the clock / PIN pad)
        patchView = v; patchAt.set(r); patchRot = rotation();
        patchStatus("active over " + r.toShortString());
        Log.i(TAG, "PATCH shown " + r.toShortString());
    }

    private void removePatch() {
        if (patchView == null) return;
        try { ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(patchView); } catch (Throwable ignored) { }
        patchView = null; patchAt.setEmpty(); patchRot = -1; patchLockMode = false; lockShot = null; lockShotFresh = false;
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
        if (Mode.isCustom(app)) { Mode.set(app, Mode.QUICKSTEP); flippedByHandshake = true; }
        if (ProfileSession.stockBehindLockOnHandshake()) startRouter("stock");   // AC9: Quickstep in front NOW, whatever the pre-arm switch says: the custom stack may already be in front (prepared at the wake) and must be replaced before the unlock reveals it
        preArmedThisLock = true;
        Features.setString(this, UnlockPick.LAST_KEY, now() + "  handshake '" + m.title + "' accepted -> Quickstep");
        profileNote("handshake '" + m.title + "' accepted: Quickstep in front behind the lock screen");
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
            if (locked && !armed && ProfileSession.preCleanAtLock(ProfileSession.preClean(app), Mode.isCustom(app), paused, true, false, ReturnReset.invisibleReady(app), preCleanedThisLock)) {
                if (ReturnReset.cleanAtLock(app)) {
                    preCleanedThisLock = true;
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
                boolean front = stock != null && isForeground(stock.getPackageName());
                if (!front) startRouter("stock");
                Features.setString(this, UnlockPick.LAST_KEY, now() + "  Quickstep (handshake '" + m.title + "')" + (front ? "" : ", started after the unlock"));
                Log.i(TAG, "UNLOCKPICK quickstep via " + m.id + " front=" + front);
                Mode.confirmQuickstep(app, System.currentTimeMillis());   // AC1: this lock ended in a handshake, so the safety net below must not end this Quickstep session
                LockLog.add(app, "UNLOCK", "handshake " + m.id + " -> Quickstep, stock in front=" + front + ", Enter to unlock " + sinceEnter + " ms");
                verifyQuickstepFront(0);         // AC9: a late start of the custom home / Solitaire stand-in must not stay on top of the gesture's choice
                return;
            }
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
            Mode.set(app, Mode.CUSTOM);
            startRouter("custom");
            ReturnReset.run(app);
            Features.setString(this, UnlockPick.LAST_KEY, now() + "  custom launcher (the helper was away at the unlock; corrected when it came back)");
        } catch (Throwable t) { Log.e(TAG, "reconcile failed", t); }
    }

    private long lastEnterAt = 0;   // when the last PIN-pad Enter was tapped (for the lock log's "Enter to unlock" time)

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
                    startRouter("stock");
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
            ComponentName stock = StockLauncher.find(this);
            if (RecentsDirty.shouldMark(e.getPackageName().toString(), getPackageName(), stock == null ? null : stock.getPackageName(), ime, ReturnReset.targetPackage(this), true)) { RecentsDirty.mark(this); AppOrigin.markQuickstep(this, e.getPackageName().toString()); }
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
    private final Runnable penEval = new Runnable() { @Override public void run() { penPending = false; updatePencil(); } };

    /** Look at SystemUI events only (and keep checking while a cover is up, so it follows the panel and goes with it). Throttled. */
    private void schedulePencil(AccessibilityEvent e) {
        if (!Mode.isCustom(this) || !Features.get(this, PEN_KEY, true)) { if (penView != null) removePencil(); return; }   // N3: Quickstep needs no cover over the pencil
        boolean sysui = e != null && e.getPackageName() != null && "com.android.systemui".contentEquals(e.getPackageName());
        if (!sysui && penView == null) return;
        if (DemoModeProfile.active(this)) { if (penView != null) removePencil(); return; }   // something else already turned the retail flag on (the pencil is gone natively); read only for SystemUI events (AB2)
        long now = android.os.SystemClock.uptimeMillis();
        if (now - lastPenMs >= 40) { lastPenMs = now; updatePencil(); }
        if (!penPending) { penPending = true; handler.postDelayed(penEval, 90); }
    }

    private void updatePencil() {
        try {
            Rect r = findPencil();
            if (r == null) { hidePencil(); return; }
            showPencil(r);
        } catch (Throwable t) { Log.e(TAG, "pencil update failed", t); removePencil(); }
    }

    /** Bounds of the visible Edit pencil (framework id android:id/edit, description "Edit order of settings."), or null. */
    private Rect findPencil() {
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
                    if (b.width() > 0 && b.height() > 0 && b.width() < 400 && b.height() < 400) return b;
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
                unlock.onScreen(false, t); preArmedThisLock = false; preCleanedThisLock = false; lastBouncer = false; flurryChange(false, t); updateMotion();
                handler.removeCallbacks(lockPrepareLater);
                handler.postDelayed(lockPrepareLater, 700);      // O3/O4: if the tablet locks at once, get everything ready while the screen is still dark
                handler.postDelayed(lockPrepareLater, 6500);     // ... and after the default 5 s "lock after" delay
            }
            if (android.content.Intent.ACTION_SCREEN_ON.equals(act)) { unlock.onScreen(true, t); handler.removeCallbacks(lockPrepareLater); handleLockedWake(); flurryChange(true, t); updateMotion(); handler.postDelayed(layerRefresh, 400); }
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
            if (bouncer != lastBouncer) { lastBouncer = bouncer; ensureUnlockListener(); unlock.onPad(bouncer, System.currentTimeMillis()); LockLog.add(getApplicationContext(), "PAD", bouncer ? "shown" : "hidden"); }
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
            { p.setColor(Color.argb(LockNumber.ALPHA, 255, 255, 255)); p.setTextSize(em); p.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, LockNumber.WEIGHT, false)); p.setTextAlign(android.graphics.Paint.Align.CENTER); }
            @Override protected void onDraw(android.graphics.Canvas c) { c.drawText(text, getWidth() / 2f + getWidth() * LockNumber.X_NUDGE_PER_WIDTH, baselineInView, p); }
        };
    }

    private void showLockNumber() {
        if (lockView != null) return;
        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
        float em = dm.widthPixels * LockNumber.EM_PER_WIDTH;
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
        if (stockCache == null || now - stockCacheAt > 60_000) { stockCache = StockLauncher.find(this); stockCacheAt = now; }
        if (stockCache == null) return;
        if (!StrayHomeGuard.isStockHome(pk.toString(), cl.toString(), stockCache.getPackageName(), stockCache.getClassName())) return;
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
        updateHotspot();
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
    private final long[] radioHist = new long[3];
    private int radioIdx = 0;
    private boolean radioGuardOn = false;
    private final android.content.BroadcastReceiver radioReceiver = new android.content.BroadcastReceiver() {
        @Override public void onReceive(android.content.Context c, android.content.Intent i) {
            try {
                String act = i == null ? null : i.getAction();
                boolean turnedOn;
                if (android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION.equals(act)) {
                    int st = i.getIntExtra(android.net.wifi.WifiManager.EXTRA_WIFI_STATE, -1);
                    turnedOn = st == android.net.wifi.WifiManager.WIFI_STATE_ENABLED || st == android.net.wifi.WifiManager.WIFI_STATE_ENABLING;
                } else if (android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED.equals(act)) {
                    int st = i.getIntExtra(android.bluetooth.BluetoothAdapter.EXTRA_STATE, -1);
                    turnedOn = st == android.bluetooth.BluetoothAdapter.STATE_ON || st == android.bluetooth.BluetoothAdapter.STATE_TURNING_ON;
                } else return;
                if (!turnedOn) return;
                long now = System.currentTimeMillis();
                if (!ProfileSession.enforceRadios(ProfileSession.radiosOff(TjAccessibilityService.this), Mode.isCustom(TjAccessibilityService.this), now, radioHist)) return;
                radioHist[radioIdx++ % radioHist.length] = now;
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

    @Override public void onInterrupt() { removeHotspot(); removeOverlay(); removePatch(); removePencil(); hideResetCover(); removeLockNumber(); }

    @Override public boolean onUnbind(android.content.Intent i) { instance = null; LockLog.add(this, "SERVICE", "unbound"); removeOverlay(); removePatch(); removePencil(); hideResetCover(); stopPatchWatch(); stopLockNumber(); stopRadioGuard(); stopTileGuard(); removeHotspot(); try { ((android.hardware.SensorManager) getSystemService(SENSOR_SERVICE)).unregisterListener(sensorListener); motionOn = false; } catch (Throwable ignored) { } return super.onUnbind(i); }

    @Override public void onDestroy() { instance = null; removeOverlay(); removePatch(); removePencil(); hideResetCover(); stopPatchWatch(); stopLockNumber(); stopRadioGuard(); stopTileGuard(); removeHotspot(); super.onDestroy(); }

    private void evaluate() {
        String mode = Features.powerMode(this);
        boolean custom = Mode.isCustom(this);
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        boolean locked = km != null && km.isKeyguardLocked();
        Rect shot = new Rect();
        boolean menuOpen = false, found = false;
        if (custom && !Features.POWER_OFF.equals(mode) && !(locked && Features.POWER_BLOCK.equals(mode))) {   // Y1: the cover also works on the lock screen; BLOCK still stays off it
            List<AccessibilityWindowInfo> windows = getWindows();
            final boolean dialogUp = dialogWindowPresent(windows);
            final boolean everyWindow = android.os.SystemClock.uptimeMillis() < fullScanUntilMs;
            if (menuSeenMs == 0 && dialogUp) menuSeenMs = android.os.SystemClock.uptimeMillis();
            if (!overlayShown && Features.isCover(mode)) {
                Rect cached = cachedCard();
                if (cached != null && dialogUp) showOverlay(cached);   // instant: no node scan yet
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
                            menuOpen = true;
                            // the card = the smallest ancestor of "Power off" that also contains the Screenshot entry
                            AccessibilityNodeInfo offItem = clickableAncestor(off.get(0));
                            Rect scr = new Rect(), c = new Rect(), ob = new Rect();
                            offItem.getBoundsInScreen(ob); lastOffTop = ob.top; lastItemH = ob.height();   // V1: the stock item size, so our menu is "stock minus Screenshot"
                            clickableAncestor(sc.get(0)).getBoundsInScreen(scr);
                            // AC6: the entry PITCH (not the height of one clickable node: the entries have margins), so our two-entry card is "the stock panel minus its last entry"
                            lastPitch = -1;
                            List<AccessibilityNodeInfo> rst = root.findAccessibilityNodeInfosByText("Restart");
                            if (rst != null && !rst.isEmpty()) { Rect rb = new Rect(); clickableAncestor(rst.get(0)).getBoundsInScreen(rb); if (rb.top > ob.top) lastPitch = rb.top - ob.top; }
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
            if (lastPitch > 0) Features.setString(this, itemKey(), String.valueOf(lastPitch));
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
        handler.post(new Runnable() { @Override public void run() {
            try {
                List<AccessibilityNodeInfo> cards = findTaskCards();
                if (cards.size() > 0) sawCards[0] = true;
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
                    finishReset(t0, done);
                    return;
                }
            } catch (Throwable t) { Log.e(TAG, "selective recents failed", t); }
            if (android.os.SystemClock.uptimeMillis() >= deadline) {
                if (!sawCards[0]) { Log.i(TAG, "RESET recents: cards not recognised, falling back to Clear all"); recentsClearAll(t0, done); }
                else { resetReport.append("recents: stopped after ").append(closed[0]).append(" card(s)\n"); finishReset(t0, done); }
                return;
            }
            handler.postDelayed(this, 40);
        } });
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
            int[] card = PowerMenuDump.cardFromPanel(panel.left, panel.top, panel.right, panel.bottom, lastPitch);
            StringBuilder h = new StringBuilder();
            h.append("written ").append(now()).append(" in the ").append(Mode.isCustom(this) ? "CUSTOM" : "Quickstep").append(" launcher, ").append(locked ? "on the LOCK SCREEN" : "unlocked").append('\n');
            h.append("display ").append(dm.widthPixels).append('x').append(dm.heightPixels).append(" px, rotation ").append(rotation()).append(", density ").append(dm.density).append(" (").append(dm.densityDpi).append(" dpi), font scale ").append(getResources().getConfiguration().fontScale).append('\n');
            h.append("stock panel ").append(panel.toShortString()).append(" (").append(panel.width()).append('x').append(panel.height()).append("), first entry top ").append(lastOffTop).append(" height ").append(lastItemH).append(", entry pitch ").append(lastPitch).append('\n');
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

    private View cardButton(String label, int iconRes, final String realLabel) {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setGravity(Gravity.CENTER);
        b.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        android.widget.ImageView icon = new android.widget.ImageView(this);
        icon.setImageDrawable(getDrawable(iconRes));
        b.addView(icon, new LinearLayout.LayoutParams((int) (24 * getResources().getDisplayMetrics().density), (int) (24 * getResources().getDisplayMetrics().density)));
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(12);
        t.setTextColor(Color.parseColor("#FF5F6368"));
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, (int) (8 * getResources().getDisplayMetrics().density), 0, 0);
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

    private View buildCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(PANEL_COLOR);
        card.addView(cardButton("Power off", R.drawable.tj_ic_power, "Power off"));
        card.addView(cardButton("Restart", R.drawable.tj_ic_restart, "Restart"));
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
        if (patchView != null && patchLockMode) patchView.setAlpha(on ? 1f : 0f);
    }

    /**
     * V1 (Tj): our menu is "the stock menu minus the Screenshot button": same panel, Power off + Restart only. The wallpaper patch underneath fills what the rest of the stock menu
     * (Screenshot, Emergency ...) would have covered. Without a patch below we keep the whole rectangle so a real Screenshot button can never show.
     * AC6 (Tj, 2026-11-05: "the buttons and card are too small"): the card is the PANEL minus one entry pitch ({@link PowerMenuDump#cardFromPanel}); it used to start at the top of the first entry and be
     * twice the height of ONE clickable node, which ignores the panel's padding and the gaps between entries (about a quarter shorter than the stock two-entry area).
     */
    private Rect cardArea(Rect full) {
        if (patchView == null) return full;
        try {
            String v = Features.getString(this, itemKey(), "");
            int pitch = v.isEmpty() || v.contains(",") ? -1 : Integer.parseInt(v.trim());   // an old "top,height" value from before v0.7.3 is ignored: the fallback is one third of the panel
            int[] c = PowerMenuDump.cardFromPanel(full.left, full.top, full.right, full.bottom, pitch);
            if (c[3] - c[1] < full.height() / 2) return full;                                  // nonsense geometry: keep the whole rectangle
            return new Rect(c[0], c[1], c[2], c[3]);
        } catch (NumberFormatException e) { return full; }
    }

    /** W3/W4: the card window can stay attached but invisible and untouchable (alpha 0 + NOT_TOUCHABLE) so showing it costs one redraw, not a window creation. */
    private void warmUp() {
        if (overlay != null || !Mode.isCustom(this) || !Features.isCover(Features.powerMode(this))) return;
        Rect cached = cachedCard();
        if (cached == null) return;
        ensurePatch();   // the wallpaper patch must be added BEFORE the card so the card is on top
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Rect area = cardArea(cached);
        overlay = buildCard();
        overlay.setAlpha(0f);
        wm.addView(overlay, overlayParams(area, false));
        shownAt.set(area);
        overlayShown = false; overlayTouchable = false;
    }

    private void showOverlay(Rect full) {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (overlay == null) {
            ensurePatch();
            Rect r0 = cardArea(full);
            Log.i(TAG, "CARD area=" + r0.toShortString() + " full=" + full.toShortString() + " lockPatch=" + patchLockMode);
            patchShow(true);
            overlay = buildCard();
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
        if (overlay == null) return;
        patchShow(false);
        try { ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(overlay); } catch (Throwable ignored) { }
        overlay = null;
        overlayShown = false; overlayTouchable = false;
        shownAt.setEmpty();
    }
}
