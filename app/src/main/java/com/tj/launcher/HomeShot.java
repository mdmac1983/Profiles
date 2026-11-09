package com.tj.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;

/**
 * BI1: the wallpaper-only crops of the real-pixel patch on the unlocked home screen, the counterpart of {@link PatchShot} (the lock screen's crops). One crop per rotation and wallpaper offset step (the
 * Launcher moves the wallpaper only with two or more pages): a PNG of the screen region around the stock panel's Screenshot slot as it looks with the Launcher's own views hidden (the system wallpaper
 * alone), in the app's private files, plus its meta ({@code signature#left#top}) in a preference. The signature (wallpaper picture, rotation, display size) makes a crop of an old wallpaper invalid at once;
 * the crops of an old wallpaper are deleted when a new one is stored. Only a crop of the wallpaper is kept, never the whole screenshot.
 */
public final class HomeShot {
    private static final String TAG = "TjHomeShot";
    /** Which crops exist: "r0x0,r0x333". */
    private static final String INDEX_KEY = "home_shot_idx";

    private HomeShot() {}

    /** Incremented whenever a crop is stored or forgotten (any thread, any Activity of this process): callers that cache "is there a valid crop" compare it. */
    private static final java.util.concurrent.atomic.AtomicInteger generation = new java.util.concurrent.atomic.AtomicInteger();   // written by the calibration's worker thread and by the main thread (a plain ++ on a volatile loses updates)
    public static int generation() { return generation.get(); }

    private static File file(Context c, int rot, int step) { return new File(c.getFilesDir(), HomePatchRules.fileName(rot, step)); }
    private static String entry(int rot, int step) { return "r" + rot + "x" + step; }

    /** Stores the crop for this rotation and offset step with its signature and the screen position of its top-left corner, then forgets the crops of an older wallpaper. */
    public static boolean save(Context c, int rot, int step, Bitmap crop, String signature, int left, int top, long wallpaperId, int dispW, int dispH) {
        try {
            File f = file(c, rot, step), tmp = new File(f.getPath() + ".tmp");
            FileOutputStream o = new FileOutputStream(tmp);
            boolean written;
            try { written = crop.compress(Bitmap.CompressFormat.PNG, 100, o); } finally { o.close(); }
            if (!written || tmp.length() <= 0) { tmp.delete(); Log.e(TAG, "the home shot could not be written (disk full?)"); return false; }   // a failed write must never become "the crop" (code review 2026-11-08: has() would say yes, load() null, for ever)
            if (!tmp.renameTo(f)) { tmp.delete(); return false; }
            Features.setString(c, HomePatchRules.metaKey(rot, step), signature + "#" + left + "#" + top);
            addToIndex(c, rot, step);
            prune(c, wallpaperId, dispW, dispH);
            generation.incrementAndGet();
            return true;
        } catch (Throwable t) { Log.e(TAG, "saving the home shot failed", t); return false; }
    }

    /** The stored step that serves {@code step} (the same wallpaper position) for this signature, or -1. A cheap check: no decoding. */
    public static int findStep(Context c, int rot, int step, String signature) {
        try {
            for (int d = 0; d <= 2; d++) {
                for (int sign = -1; sign <= 1; sign += 2) {
                    int s = step + sign * d;
                    if (s < 0 || (d == 0 && sign == 1)) continue;
                    String[] p = Features.getString(c, HomePatchRules.metaKey(rot, s), "").split("#");
                    if (p.length == 3 && p[0].equals(signature) && file(c, rot, s).isFile()) return s;
                }
            }
        } catch (Throwable t) { Log.e(TAG, "looking for the home shot failed", t); }
        return -1;
    }

    public static boolean has(Context c, int rot, int step, String signature) { return findStep(c, rot, step, signature) >= 0; }

    /** The stored crop that serves {@code step} for this signature, or null. Decodes the PNG. Never throws. */
    public static PatchShot.Loaded load(Context c, int rot, int step, String signature) {
        try {
            int s = findStep(c, rot, step, signature);
            if (s < 0) return null;
            String[] p = Features.getString(c, HomePatchRules.metaKey(rot, s), "").split("#");
            Bitmap b = BitmapFactory.decodeFile(file(c, rot, s).getPath());
            return b == null ? null : new PatchShot.Loaded(b, Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        } catch (Throwable t) { Log.e(TAG, "loading the home shot failed", t); return null; }
    }

    /** Forgets every stored crop (a calibration of the whole home screen starts again, or the wallpaper picture changed). */
    public static void invalidateAll(Context c) {
        try {
            for (String e : index(c)) {
                int x = e.indexOf('x');
                if (x < 2) continue;
                int rot = Integer.parseInt(e.substring(1, x)), step = Integer.parseInt(e.substring(x + 1));
                file(c, rot, step).delete();
                Features.remove(c, HomePatchRules.metaKey(rot, step));
            }
            Features.remove(c, INDEX_KEY);
        } catch (Throwable t) { Log.e(TAG, "forgetting the home shots failed", t); }
        generation.incrementAndGet();
    }

    /**
     * Is the stored crop that serves {@code step} really CORRUPT (an empty file or an unreadable header), as opposed to merely not loadable right now (a memory spike on a 2 GB tablet)? Reads only the image
     * BOUNDS, so no bitmap is allocated and low memory cannot make a good file look bad. In doubt: not corrupt (nothing is ever deleted on a guess). Code review 2026-11-08.
     */
    public static boolean isCorrupt(Context c, int rot, int step, String signature) {
        try {
            int s = findStep(c, rot, step, signature);
            if (s < 0) return false;
            File f = file(c, rot, s);
            if (f.length() <= 0) return true;
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getPath(), o);
            return o.outWidth <= 0 || o.outHeight <= 0;
        } catch (Throwable t) { return false; }
    }

    /** Forgets the ONE stored crop that serves {@code step} (a corrupt file): the other pages keep theirs. */
    public static void invalidateStep(Context c, int rot, int step, String signature) {
        try {
            int s = findStep(c, rot, step, signature);
            if (s >= 0) {
                file(c, rot, s).delete();
                Features.remove(c, HomePatchRules.metaKey(rot, s));
                String drop = entry(rot, s);
                StringBuilder b = new StringBuilder();
                for (String e : index(c)) { if (e.equals(drop)) continue; if (b.length() > 0) b.append(','); b.append(e); }
                Features.setString(c, INDEX_KEY, b.toString());
            }
        } catch (Throwable t) { Log.e(TAG, "forgetting one home shot failed", t); }
        generation.incrementAndGet();
    }

    /** Deletes the crops whose signature is not this wallpaper picture and display size any more. */
    private static void prune(Context c, long wallpaperId, int dispW, int dispH) {
        try {
            StringBuilder keep = new StringBuilder();
            for (String e : index(c)) {
                int x = e.indexOf('x');
                if (x < 2) continue;
                int rot = Integer.parseInt(e.substring(1, x)), step = Integer.parseInt(e.substring(x + 1));
                String[] p = Features.getString(c, HomePatchRules.metaKey(rot, step), "").split("#");
                if (p.length == 3 && HomePatchRules.sigMatches(p[0], wallpaperId, dispW, dispH)) { if (keep.length() > 0) keep.append(','); keep.append(e); }
                else { file(c, rot, step).delete(); Features.remove(c, HomePatchRules.metaKey(rot, step)); }
            }
            Features.setString(c, INDEX_KEY, keep.toString());
        } catch (Throwable t) { Log.e(TAG, "pruning the home shots failed", t); }
    }

    private static java.util.List<String> index(Context c) {
        java.util.List<String> out = new java.util.ArrayList<>();
        String v = Features.getString(c, INDEX_KEY, "");
        if (v.isEmpty()) return out;
        for (String s : v.split(",")) if (!s.isEmpty()) out.add(s);
        return out;
    }

    private static void addToIndex(Context c, int rot, int step) {
        java.util.List<String> idx = index(c);
        String e = entry(rot, step);
        if (!idx.contains(e)) idx.add(e);
        StringBuilder b = new StringBuilder();
        for (String s : idx) { if (b.length() > 0) b.append(','); b.append(s); }
        Features.setString(c, INDEX_KEY, b.toString());
    }

    /** How many crops are stored (Profiles settings and the tablet report). */
    public static int count(Context c) { return index(c).size(); }
}
