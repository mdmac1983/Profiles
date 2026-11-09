package com.tj.launcher;

import android.content.Context;
import android.util.Log;

import com.tj.launcher.ProfileRules.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * BT1a: the Android half of the three profiles (rules: {@link ProfileRules}). Stores which custom profile (FBOP / Rental) is active, which profile each handshake opens,
 * and the per-profile values of the custom-launcher settings. Rental reads FBOP's value for any setting it has not stored yet ("a new Rental starts as a copy of FBOP").
 * Owner is the stock Quickstep launcher ({@link Mode#QUICKSTEP}); its own settings are the existing Quickstep ones.
 */
public final class Profiles {
    private static final String TAG = "TjProfiles";
    public static final String ACTIVE_KEY = "profile_active";       // the custom profile that is (or was last) active
    public static final String EDIT_KEY = "ctl_edit_profile";       // which custom profile Profiles settings is editing
    private static final String TARGET_PREFIX = "hs_target_";       // + method id: the profile that handshake opens

    private Profiles() {}

    // ---------------------------------------------------------------- the active custom profile
    public static Profile active(Context c) { return ProfileRules.customOrDefault(ProfileRules.parse(Features.getString(c, ACTIVE_KEY, ""), ProfileRules.DEFAULT)); }

    /** True = the active custom profile changed. commit(): the router and the Launcher read it right after an unlock. */
    public static boolean setActive(Context c, Profile p) {
        Profile next = ProfileRules.customOrDefault(p), before = active(c);
        if (next == before && !Features.getString(c, ACTIVE_KEY, "").isEmpty()) return false;
        Features.setString(c, ACTIVE_KEY, next.id);
        Log.i(TAG, "active profile " + before.title + " -> " + next.title);
        return next != before;
    }

    public static Profile editing(Context c) { return ProfileRules.customOrDefault(ProfileRules.parse(Features.getString(c, EDIT_KEY, ""), ProfileRules.DEFAULT)); }
    public static void setEditing(Context c, Profile p) { Features.setString(c, EDIT_KEY, ProfileRules.customOrDefault(p).id); }

    // ---------------------------------------------------------------- per-profile values (Rental falls back to FBOP's)
    public static boolean getBool(Context c, Profile p, String key, boolean def) {
        String k = ProfileRules.scopedKey(key, p);
        if (!k.equals(key) && !Features.has(c, k)) return Features.get(c, key, def);
        return Features.get(c, k, def);
    }
    public static void setBool(Context c, Profile p, String key, boolean v) { Features.set(c, ProfileRules.scopedKey(key, p), v); }

    public static String getString(Context c, Profile p, String key, String def) {
        String k = ProfileRules.scopedKey(key, p);
        if (!k.equals(key) && !Features.has(c, k)) return Features.getString(c, key, def);
        return Features.getString(c, k, def);
    }
    public static void setString(Context c, Profile p, String key, String v) { Features.setString(c, ProfileRules.scopedKey(key, p), v); }

    // ---------------------------------------------------------------- BT1c (v1.2): Rental's restrictions (each a switch, release ON / debug OFF)
    public static boolean rentalOn(Context c, String key) { return Features.get(c, key, RentalRules.defaultOn(ReturnReset.isDebuggable(c))); }

    /** Hidden by a Rental restriction in this profile's drawer (on top of its app list)? */
    public static boolean restrictedInDrawer(Context c, Profile p, String pkg) {
        return p == Profile.RENTAL && RentalRules.hiddenInDrawer(p, pkg, rentalOn(c, RentalRules.SETTINGS_KEY), rentalOn(c, RentalRules.STORE_KEY));
    }

    // ---------------------------------------------------------------- handshake -> profile
    private static Profile storedTarget(Context c, UnlockMethods.Method m) { return ProfileRules.parse(Features.getString(c, TARGET_PREFIX + m.id, ""), Profile.OWNER); }

    /** Is any selected handshake (stored switches) set to open Owner? */
    public static boolean ownerReachable(Context c) {
        List<Boolean> sel = new ArrayList<>(); List<Profile> tg = new ArrayList<>();
        for (UnlockMethods.Method m : UnlockMethods.Method.values()) { sel.add(Features.get(c, m.prefKey(), m.defaultOn)); tg.add(storedTarget(c, m)); }
        return ProfileRules.ownerReachable(sel, tg);
    }

    /** The profile this handshake opens now (the safety method opens Owner while nothing else does). */
    public static Profile target(Context c, UnlockMethods.Method m) {
        return m == null ? ProfileRules.DEFAULT : ProfileRules.effectiveTarget(m.id, storedTarget(c, m), ownerReachable(c));
    }

    /** The stored choice for the settings screen (what the button shows). */
    public static Profile chosenTarget(Context c, UnlockMethods.Method m) { return storedTarget(c, m); }
    public static void setTarget(Context c, UnlockMethods.Method m, Profile p) { Features.setString(c, TARGET_PREFIX + m.id, (p == null ? Profile.OWNER : p).id); }

    // ---------------------------------------------------------------- applying a profile
    /** The custom launcher now shows {@code active(c)}: its apps, tiles, status bar, banners. Cheap calls; each is exception-safe and does nothing without its permission. */
    public static void applyActive(Context c, String why) {
        Context app = c.getApplicationContext();
        if (!Mode.isCustom(app)) return;
        try { TileProfile.apply(app, Mode.CUSTOM); } catch (Throwable t) { Log.e(TAG, "tiles failed", t); }
        try { ImmersiveProfile.apply(app, Mode.CUSTOM); } catch (Throwable t) { Log.e(TAG, "status bar failed", t); }
        try { AnimationSpeedup.syncNow(app, "profile " + active(app).title + " (" + why + ")"); } catch (Throwable t) { Log.e(TAG, "banner sync failed", t); }   // NotifQuiet follows the same sync
        try { HiddenApps.reloadLauncher(app); } catch (Throwable t) { Log.e(TAG, "app list reload failed", t); }
        Log.i(TAG, "applied profile " + active(app).title + " (" + why + ")");
    }

    /** One line for Profiles settings and the tablet report. */
    public static String describe(Context c) {
        StringBuilder b = new StringBuilder("active custom profile ").append(active(c).title).append("; handshakes:");
        for (UnlockMethods.Method m : UnlockMethods.Method.values()) if (Features.get(c, m.prefKey(), m.defaultOn) || ProfileRules.forcedOn(m.id, ownerReachable(c))) b.append(' ').append(m.id).append("->").append(target(c, m).title);
        return b.toString();
    }
}
