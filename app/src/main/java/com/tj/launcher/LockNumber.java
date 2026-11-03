package com.tj.launcher;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.android.launcher3.R;

/**
 * X1 (Tj, 2026-11-03, L10 lock-screen rule relaxed by Tj for THIS feature only): a number at the bottom of the lock screen that disappears when the PIN pad is pulled up.
 * The number is drawn by the accessibility helper as a non-touchable, non-focusable overlay (it can never take a touch or a key, so it cannot block the PIN pad).
 * Look = measured from Tj's photo (research/uploads/2026-11-03/lockscreen-wallpaper-with-number.png): white Roboto digits, 14 characters 304 px wide on a 957 px wide photo shown
 * full-width, cap height 27.5 px, centred, baseline at 90.53 % of the screen height.
 */
public final class LockNumber {
    public static final String KEY = "lock_number";             // switch, default ON ("Restore everything" turns it OFF)
    public static final String TEXT_KEY = "lock_number_text";
    public static final String DEFAULT_TEXT = "71125011907547";
    /** text size (em) as a fraction of the screen width: 38.7 px em on a 957 px photo = 0.04045 */
    public static final float EM_PER_WIDTH = 0.0430f;   // v0.3.3: measured on Tj's tablet screenshot (6.png): 180 px wide vs 190.6 px for the photo (5.png) => x1.063
    /** baseline as a fraction of the real display height: (1483.5 px * 600/957 - 3 px crop) / 1024 */
    public static final float BASELINE_PER_HEIGHT = 0.9063f;   // v0.3.3: +1 px (6.png sat 1 px high)
    /** Light weight: the photo's strokes carry 19 % less ink than Regular at the same size (alpha-mass measurement of 5.png vs 6.png). */
    public static final int WEIGHT = 300;
    /** +1 px to the right (6.png centre 299.5 vs 300.6 in the photo), as a fraction of the screen width */
    public static final float X_NUDGE_PER_WIDTH = 0.0017f;

    private LockNumber() {}

    public static boolean enabled(Context c) { return Features.get(c, KEY, true); }
    public static String text(Context c) {
        String t = Features.getString(c, TEXT_KEY, DEFAULT_TEXT);
        return t == null || t.trim().isEmpty() ? DEFAULT_TEXT : t.trim();
    }

    /** Pure rule (JVM-testable): shown only on the lock screen itself, screen on, switch on, and never while the PIN/pattern/password pad is up. */
    public static boolean shouldShow(boolean enabled, boolean screenOn, boolean keyguardLocked, boolean bouncerShown) {
        return enabled && screenOn && keyguardLocked && !bouncerShown;
    }

    /** Sets the CLEAN photo (old number removed) as the LOCK-screen wallpaper. Only ever called from an explicit button. Returns a status line. */
    public static String setLockWallpaper(Context c) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inScaled = false;
            Bitmap b = BitmapFactory.decodeResource(c.getResources(), R.drawable.tj_lock_wallpaper, o);
            if (b == null) return "could not read the bundled photo";
            WallpaperManager.getInstance(c).setBitmap(b, null, true, WallpaperManager.FLAG_LOCK);
            return "lock-screen wallpaper set (clean photo, no number baked in)";
        } catch (Throwable t) {
            return "could not set the lock-screen wallpaper: " + t.getClass().getSimpleName();
        }
    }
}
