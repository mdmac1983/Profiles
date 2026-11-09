package com.tj.launcher;

import java.util.List;

/**
 * BR1 / BT1a (user, 2026-11-09: "name the profiles: Owner, FBOP (plain pin opens this), Rental. I want to be able to set up which handshake opens which profile as well as the
 * customizations for each profile from settings."). The pure rules (JVM-tested, no Android classes) of the three profiles:
 *  - OWNER  = the stock Quickstep launcher (the old "Quickstep session"); the only profile where the settings open.
 *  - FBOP   = the custom launcher, the DEFAULT: a plain PIN always opens it.
 *  - RENTAL = the custom launcher with its own settings; a new Rental starts as a copy of FBOP.
 * Every handshake opens the profile chosen for it (default OWNER, so nothing changes until the user chooses). Owner must always stay reachable: when no selected handshake
 * opens Owner, "Double erase" is selected and opens Owner whatever was stored (the same safety as "at least one handshake always stays selected").
 */
public final class ProfileRules {
    public enum Profile {
        OWNER("owner", "Owner"), FBOP("fbop", "FBOP"), RENTAL("rental", "Rental");
        public final String id, title;
        Profile(String id, String title) { this.id = id; this.title = title; }
        public boolean custom() { return this != OWNER; }
    }

    /** The profile a plain PIN opens. */
    public static final Profile DEFAULT = Profile.FBOP;
    /** The handshake that keeps Owner reachable when the user's choices would not. */
    public static final String SAFETY_METHOD = "erase2";

    private ProfileRules() {}

    public static Profile parse(String id, Profile def) {
        if (id != null) for (Profile p : Profile.values()) if (p.id.equals(id)) return p;
        return def;
    }

    /** The custom profiles only (FBOP when anything else is asked for): what the custom launcher shows. */
    public static Profile customOrDefault(Profile p) { return p != null && p.custom() ? p : DEFAULT; }

    /** Settings: tapping "Opens: X" moves to the next profile. */
    public static Profile next(Profile p) {
        Profile[] v = Profile.values();
        return v[(p == null ? 0 : p.ordinal() + 1) % v.length];
    }

    /** A per-profile setting is stored under this key: FBOP keeps the original key (its values are the old custom-launcher values), Rental gets its own. */
    public static String scopedKey(String key, Profile p) { return customOrDefault(p) == Profile.RENTAL ? key + "@rental" : key; }

    /** Is any SELECTED handshake set to open Owner? {@code selected} and {@code targets} run in parallel. */
    public static boolean ownerReachable(List<Boolean> selected, List<Profile> targets) {
        for (int i = 0; i < selected.size() && i < targets.size(); i++) if (Boolean.TRUE.equals(selected.get(i)) && targets.get(i) == Profile.OWNER) return true;
        return false;
    }

    /** The profile this handshake really opens: the stored choice, except that the safety method opens Owner while nothing else does. */
    public static Profile effectiveTarget(String methodId, Profile stored, boolean ownerReachable) {
        if (!ownerReachable && SAFETY_METHOD.equals(methodId)) return Profile.OWNER;
        return stored == null ? Profile.OWNER : stored;
    }

    /** The safety method stays selected while no other selected handshake opens Owner. */
    public static boolean forcedOn(String methodId, boolean ownerReachable) { return !ownerReachable && SAFETY_METHOD.equals(methodId); }

    /** What an unlock opens: a handshake its profile, a plain PIN the default. */
    public static Profile unlockTarget(boolean handshake, Profile handshakeTarget) {
        return handshake && handshakeTarget != null ? handshakeTarget : DEFAULT;
    }

    /** A custom-launcher unlock that lands in ANOTHER custom profile than the last one starts that profile clean (its own apps, settings and an empty Recents list). */
    public static boolean switchNeedsReset(Profile before, Profile after) {
        return customOrDefault(before) != customOrDefault(after);
    }
}
