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
    public static final float EM_PER_WIDTH = 0.0429f;   // v0.3.3: ground truth = 7.png (the real number overlapping ours on the tablet): 192 px wide x 18 px high; ours at 0.04045 was 180 x 17
    /** baseline as a fraction of the real display height: (1483.5 px * 600/957 - 3 px crop) / 1024 */
    public static final float BASELINE_PER_HEIGHT = 0.9033f;   // v0.3.3: 7.png: the real digits end at y=925 of 1024 (ours ended at 927)
    /** Light weight: the photo's strokes carry 19 % less ink than Regular at the same size (alpha-mass measurement of 5.png vs 6.png). */
    public static final int WEIGHT = 300;
    /** 7.png, per-pixel solve against the wallpaper just above the text: peak alpha 0.72 on unobstructed stroke pixels; top-bar ink 1.2 px-equivalent = a 1.6 px Light bar (0.062 em x 25.7) at ~0.73 opacity => 186/255 */
    public static final int ALPHA = 186;
    /** +1 px to the right (6.png centre 299.5 vs 300.6 in the photo), as a fraction of the screen width */
    public static final float X_NUDGE_PER_WIDTH = 0.0017f;

    private LockNumber() {}

    /** What Tj may type as the lock-screen text: letters, digits, space - : . / _ ; at most 24 characters; empty = the default number. */
    public static String sanitize(String in) {
        if (in == null) return DEFAULT_TEXT;
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < in.length() && b.length() < 24; i++) {
            char ch = in.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == ' ' || ch == '-' || ch == ':' || ch == '.' || ch == '/' || ch == '_') b.append(ch);
        }
        String t = b.toString().trim();
        return t.isEmpty() ? DEFAULT_TEXT : t;
    }

    public static boolean enabled(Context c) { return Features.get(c, KEY, true); }
    public static String text(Context c) {
        String t = Features.getString(c, TEXT_KEY, DEFAULT_TEXT);
        return t == null || t.trim().isEmpty() ? DEFAULT_TEXT : t.trim();
    }

    /** Pure rule (JVM-testable): shown only on the lock screen itself, screen on, switch on, and never while the PIN/pattern/password pad is up. */
    public static boolean shouldShow(boolean enabled, boolean screenOn, boolean keyguardLocked, boolean bouncerShown) {
        return enabled && screenOn && keyguardLocked && !bouncerShown;
    }

    /** Sets the ORIGINAL leaf photo (Tj sent it 2026-11-03; no number in it) as the LOCK-screen wallpaper. Only ever called from an explicit button. Returns a status line. */
    public static String setLockWallpaper(Context c) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inScaled = false;
            Bitmap b = BitmapFactory.decodeResource(c.getResources(), R.drawable.tj_lock_wallpaper, o);
            if (b == null) return "could not read the bundled photo";
            WallpaperManager.getInstance(c).setBitmap(b, null, true, WallpaperManager.FLAG_LOCK);
            return "lock-screen wallpaper set (original leaf photo, no number)";
        } catch (Throwable t) {
            return "could not set the lock-screen wallpaper: " + t.getClass().getSimpleName();
        }
    }
}
