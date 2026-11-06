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
 * Flow: Launcher control "Calibrate the patch" opens a {@link #WINDOW_MS} window (the resting patch is hidden meanwhile); Tj locks and wakes the tablet and takes a screenshot (Power + Volume Down), or the
 * helper asks the system for one; a MediaStore observer hands the newest "Screenshot..." image of the window to {@link TjAccessibilityService}, which crops and stores it here. Only a crop of the wallpaper is
 * kept (no owner info, no clock), never the whole screenshot, and the screenshot itself is left alone.
 */
public final class PatchShot {
    private static final String TAG = "TjPatchShot";
    /** The calibration window: epoch ms as text; while it runs the resting patch is hidden so it can never be inside the screenshot. */
    public static final String CAL_UNTIL_KEY = "patch_cal_until";
    /** What happened last (Launcher control and the tablet report). */
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

    /** The newest "Screenshot..." image added since the window started, or null. Needs READ_EXTERNAL_STORAGE (the wallpaper access). Never throws. */
    public static Uri newestScreenshot(Context c, long windowStartMs) {
        Cursor q = null;
        try {
            Uri base = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            q = c.getContentResolver().query(base, new String[] { MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED },
                    MediaStore.Images.Media.DATE_ADDED + ">=?", new String[] { String.valueOf(Math.max(0, windowStartMs / 1000L - 5)) }, MediaStore.Images.Media.DATE_ADDED + " DESC");
            if (q == null) return null;
            long[] ids = new long[20], added = new long[20]; String[] names = new String[20]; int n = 0;
            while (q.moveToNext() && n < 20) { ids[n] = q.getLong(0); names[n] = q.getString(1); added[n] = q.getLong(2); n++; }
            String[] nn = new String[n]; long[] aa = new long[n];
            System.arraycopy(names, 0, nn, 0, n); System.arraycopy(added, 0, aa, 0, n);
            int i = pickNewest(nn, aa, windowStartMs);
            return i < 0 ? null : ContentUris.withAppendedId(base, ids[i]);
        } catch (Throwable t) { Log.e(TAG, "screenshot query failed", t); return null; }
        finally { if (q != null) try { q.close(); } catch (Throwable ignored) { } }
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

    /** Forgets every stored crop. */
    public static void forget(Context c) {
        for (int rot = 0; rot < 4; rot++) {
            try { file(c, rot).delete(); Features.setString(c, metaKey(rot), ""); } catch (Throwable ignored) { }
        }
        Features.setString(c, STATUS_KEY, "calibration forgotten");
    }

    /** One line for Launcher control: which rotations hold a crop. */
    public static String summary(Context c) {
        StringBuilder b = new StringBuilder();
        String[] names = { "portrait", "landscape", "portrait (upside down)", "landscape (other way)" };
        for (int rot = 0; rot < 4; rot++) if (Features.getString(c, metaKey(rot), "").length() > 0 && file(c, rot).isFile()) { if (b.length() > 0) b.append(", "); b.append(names[rot]); }
        return b.length() == 0 ? "not calibrated yet" : "calibrated for " + b;
    }
}
