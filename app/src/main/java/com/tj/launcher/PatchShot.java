package com.tj.launcher;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;

/**
 * AM1 (Tj, 2026-11-06, with a photo of the resting patch: "You can see the patch clearly. It doesn't blend in"; and: "If the patch of wallpaper matches perfectly and can't be seen, it will automatically cover the
 * screenshot button ... the power menu can stay completely stock"). No MODEL of the wallpaper can be perfect (the keyguard darkens it unevenly, the real engine places it about 90 px differently), so the patch
 * draws the REAL pixels: one real screenshot of the lock screen (menu closed, our patch hidden) is cropped around the Screenshot slot and kept, per rotation, in the app's private files. The patch then shows
 * exactly what the lock screen shows there (proved on the emulator by section ae: resting patch vs bare lock screen MAE 3e-6). Valid for one wallpaper picture and one display size; a changed wallpaper
 * invalidates it (the signature) and the patch simply stays hidden at rest until Tj calibrates again.
 *
 * Flow: Profiles settings "Calibrate the patch" opens a {@link #WINDOW_MS} window (the resting patch is hidden meanwhile); Tj locks and wakes the tablet and takes a screenshot (Power + Volume Down), or the
 * helper asks the system for one; a MediaStore observer hands the newest "Screenshot..." image of the window to {@link TjAccessibilityService}, which crops and stores it here. Only a crop of the wallpaper is
 * kept (no owner info, no clock), never the whole screenshot, and the screenshot itself is left alone.
 */
public final class PatchShot {
    private static final String TAG = "TjPatchShot";
    /** The calibration window: epoch ms as text; while it runs the resting patch is hidden so it can never be inside the screenshot. */
    public static final String CAL_UNTIL_KEY = "patch_cal_until";
    /** What happened last (Profiles settings and the tablet report). */
    public static final String STATUS_KEY = "patch_shot_status";
    public static final long WINDOW_MS = 4 * 60_000L;
    /** Pixels kept around the patch window, so a window that moves a few pixels later still lies inside the crop. */
    public static final int MARGIN_PX = 24;

    private PatchShot() {}

    // ---------------------------------------------------------------- pure rules (JVM-tested)

    /** Pure: the index of the newest screenshot added inside the window (names like "Screenshot_2026...png", date added in SECONDS), or -1. A small slack covers clock rounding. */
    public static int pickNewest(String[] names, long[] addedSec, long windowStartMs) {
        int best = -1;
        if (names == null || addedSec == null) return -1;
        for (int i = 0; i < names.length && i < addedSec.length; i++) {
            String n = names[i] == null ? "" : names[i].toLowerCase(Locale.ROOT);
            if (!n.startsWith("screenshot")) continue;
            if (addedSec[i] * 1000L < windowStartMs - 2000L) continue;
            if (best < 0 || addedSec[i] > addedSec[best]) best = i;
        }
        return best;
    }

    /** Pure: the rectangle {left, top, right, bottom} to keep: the patch window plus {@code margin}, clamped to the display. */
    public static int[] cropRect(int wl, int wt, int wr, int wb, int margin, int dispW, int dispH) {
        return new int[] { Math.max(0, wl - margin), Math.max(0, wt - margin), Math.min(dispW, wr + margin), Math.min(dispH, wb + margin) };
    }

    /**
     * Pure: does the mean colour of the slot region look like the stock power menu's panel (bright and almost colourless: the panel is about #EFF1F3)? Then the screenshot was taken with the menu open and
     * the Screenshot entry is in it: it must be refused. A bright but COLOURFUL wallpaper passes.
     */
    public static boolean looksLikeMenu(double meanR, double meanG, double meanB) {
        double lum = (meanR + meanG + meanB) / 3.0;
        double spread = Math.max(meanR, Math.max(meanG, meanB)) - Math.min(meanR, Math.min(meanG, meanB));
        return lum > 190.0 && spread < 30.0;
    }

    /** Review AU4, pure: does the stored crop (top-left {@code cl},{@code ct}, size {@code cw} x {@code ch}) contain the whole patch window? If the learned menu moved (font size, another entry) part of the window would be transparent. */
    public static boolean covers(int cl, int ct, int cw, int ch, int wl, int wt, int wr, int wb) {
        return cw > 0 && ch > 0 && wl >= cl && wt >= ct && wr <= cl + cw && wb <= ct + ch;
    }

    /** Pure: what a stored crop is valid for: one wallpaper picture, one rotation, one display size. */
    public static String sig(long wallpaperId, int rotation, int dispW, int dispH) { return wallpaperId + "|r" + rotation + "|" + dispW + "x" + dispH; }

    /** Pure: is the calibration window still running at {@code now}? */
    public static boolean calibrating(String untilText, long now) {
        try { return untilText != null && now < Long.parseLong(untilText.trim()); } catch (NumberFormatException e) { return false; }
    }

    // ---------------------------------------------------------------- Android side

    /** Result of {@link #load}: the stored crop and where its top-left corner lies on the screen. */
    public static final class Loaded {
        public final Bitmap bmp; public final int left, top;
        Loaded(Bitmap b, int l, int t) { bmp = b; left = l; top = t; }
    }

    private static File file(Context c, int rot) { return new File(c.getFilesDir(), "patchshot_r" + rot + ".png"); }
    private static String metaKey(int rot) { return "patch_shot_r" + rot; }

    /** A screenshot found in the media store: where it is, when it was added (epoch SECONDS) and which app wrote it (null when the column is empty). */
    public static final class Shot {
        public final Uri uri; public final long addedSec; public final String owner;
        Shot(Uri u, long a, String o) { uri = u; addedSec = a; owner = o; }
    }

    /** The newest "Screenshot..." image added since the window started, or null. Needs READ_EXTERNAL_STORAGE (the wallpaper access). Never throws. */
    public static Uri newestScreenshot(Context c, long windowStartMs) {
        Shot s = newestShot(c, windowStartMs);
        return s == null ? null : s.uri;
    }

    /** Review AU2: like {@link #newestScreenshot} but also tells who wrote the file and when, so the caller can refuse another app's "Screenshot_x.png" and a screenshot taken during the menu / the PIN pad. */
    public static Shot newestShot(Context c, long windowStartMs) {
        Cursor q = null;
        try {
            Uri base = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] sel = new String[] { String.valueOf(Math.max(0, windowStartMs / 1000L - 5)) };
            try {
                q = c.getContentResolver().query(base, new String[] { MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED, MediaStore.MediaColumns.OWNER_PACKAGE_NAME },
                        MediaStore.Images.Media.DATE_ADDED + ">=?", sel, MediaStore.Images.Media.DATE_ADDED + " DESC");
            } catch (Throwable noOwnerColumn) { q = null; }
            boolean withOwner = q != null;
            if (q == null) q = c.getContentResolver().query(base, new String[] { MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED },
                    MediaStore.Images.Media.DATE_ADDED + ">=?", sel, MediaStore.Images.Media.DATE_ADDED + " DESC");
            if (q == null) return null;
            long[] ids = new long[20], added = new long[20]; String[] names = new String[20], owners = new String[20]; int n = 0;
            while (q.moveToNext() && n < 20) { ids[n] = q.getLong(0); names[n] = q.getString(1); added[n] = q.getLong(2); owners[n] = withOwner ? q.getString(3) : null; n++; }
            String[] nn = new String[n]; long[] aa = new long[n];
            System.arraycopy(names, 0, nn, 0, n); System.arraycopy(added, 0, aa, 0, n);
            int i = pickNewest(nn, aa, windowStartMs);
            return i < 0 ? null : new Shot(ContentUris.withAppendedId(base, ids[i]), added[i], owners[i]);
        } catch (Throwable t) { Log.e(TAG, "screenshot query failed", t); return null; }
        finally { if (q != null) try { q.close(); } catch (Throwable ignored) { } }
    }

    /**
     * Best effort (AW5): remove the calibration screenshot again so the gallery does not fill up. Android 10 only lets an app delete another app's media with the storage WRITE permission this app does not
     * hold, so on most tablets this simply fails; it is never an error. True when the file was removed.
     */
    public static boolean deleteQuietly(Context c, Uri uri) {
        try { return uri != null && c.getContentResolver().delete(uri, null, null) > 0; } catch (Throwable t) { return false; }
    }

    /** Decodes the image behind {@code uri}; null on any failure. */
    public static Bitmap decode(Context c, Uri uri) {
        InputStream in = null;
        try { in = c.getContentResolver().openInputStream(uri); return in == null ? null : BitmapFactory.decodeStream(in); }
        catch (Throwable t) { Log.e(TAG, "screenshot decode failed", t); return null; }
        finally { if (in != null) try { in.close(); } catch (Throwable ignored) { } }
    }

    /** Stores the crop (PNG, private files) for this rotation with its signature and the screen position of its top-left corner. */
    public static boolean save(Context c, int rot, Bitmap crop, String signature, int left, int top) {
        try {
            File f = file(c, rot), tmp = new File(f.getPath() + ".tmp");
            FileOutputStream o = new FileOutputStream(tmp);
            try { crop.compress(Bitmap.CompressFormat.PNG, 100, o); } finally { o.close(); }
            if (!tmp.renameTo(f)) return false;
            Features.setString(c, metaKey(rot), signature + "#" + left + "#" + top);
            return true;
        } catch (Throwable t) { Log.e(TAG, "saving the patch shot failed", t); return false; }
    }

    /** The stored crop for this rotation if it is valid for {@code signature} (same wallpaper picture, rotation and display size), else null. Never throws. */
    public static Loaded load(Context c, int rot, String signature) {
        try {
            String[] p = Features.getString(c, metaKey(rot), "").split("#");
            if (p.length != 3 || !p[0].equals(signature)) return null;
            File f = file(c, rot);
            if (!f.isFile()) return null;
            Bitmap b = BitmapFactory.decodeFile(f.getPath());
            return b == null ? null : new Loaded(b, Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        } catch (Throwable t) { Log.e(TAG, "loading the patch shot failed", t); return null; }
    }

    /** True when a crop is stored for this rotation AND is valid for {@code signature}: a cheap check (no decoding) so the lock-screen patch need not decode the whole lock wallpaper (the MODEL path) when real pixels exist. Never throws. */
    public static boolean has(Context c, int rot, String signature) {
        try {
            String[] p = Features.getString(c, metaKey(rot), "").split("#");
            return p.length == 3 && p[0].equals(signature) && file(c, rot).isFile();
        } catch (Throwable t) { return false; }
    }

    /** Review of v0.8.1 (AU4 follow-up): the stored crop of THIS rotation no longer fits the patch window (the menu moved: font size, another entry). Forget just that one, so every check ({@link #has}, {@link #summary}, the rest decision, the automatic calibration) agrees that this lock screen has to be calibrated again; otherwise the rest recheck loaded the same crop 400 ms later. */
    public static void invalidate(Context c, int rot) {
        try { file(c, rot).delete(); Features.setString(c, metaKey(rot), ""); } catch (Throwable ignored) { }
    }

    /** Forgets every stored crop. */
    public static void forget(Context c) {
        for (int rot = 0; rot < 4; rot++) {
            try { file(c, rot).delete(); Features.setString(c, metaKey(rot), ""); } catch (Throwable ignored) { }
        }
        Features.setString(c, STATUS_KEY, "calibration forgotten");
    }

    /** The signature of the lock screen as it is NOW (wallpaper picture, rotation, display size): what a stored crop must have been taken for. Never throws. */
    public static String currentSig(Context c) {
        long id = 0;
        try { android.app.WallpaperManager wm = android.app.WallpaperManager.getInstance(c);
              id = wm.getWallpaperId(android.app.WallpaperManager.FLAG_LOCK); if (id <= 0) id = wm.getWallpaperId(android.app.WallpaperManager.FLAG_SYSTEM); } catch (Throwable ignored) { }
        int rot = 0, w = 0, h = 0;
        try {
            android.view.WindowManager wmg = (android.view.WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
            wmg.getDefaultDisplay().getRealMetrics(dm); rot = wmg.getDefaultDisplay().getRotation(); w = dm.widthPixels; h = dm.heightPixels;
        } catch (Throwable ignored) { }
        return sig(id, rot, w, h);
    }

    /** One line for Profiles settings: which rotations hold a crop, and (review AU7) whether the one for the CURRENT lock screen is still valid for the current wallpaper and screen. */
    public static String summary(Context c) {
        StringBuilder b = new StringBuilder();
        String[] names = { "portrait", "landscape", "portrait (upside down)", "landscape (other way)" };
        for (int rot = 0; rot < 4; rot++) if (Features.getString(c, metaKey(rot), "").length() > 0 && file(c, rot).isFile()) { if (b.length() > 0) b.append(", "); b.append(names[rot]); }
        if (b.length() == 0) return "not calibrated yet";
        int rotNow = 0;
        try { rotNow = ((android.view.WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation(); } catch (Throwable ignored) { }
        boolean valid = has(c, rotNow, currentSig(c));
        return "calibrated for " + b + (valid ? " (valid for the lock screen as it is now)" : " - NOT valid for the current wallpaper / screen: the patch stays hidden until it is calibrated again (automatically on the next plain lock screen)");
    }
}
