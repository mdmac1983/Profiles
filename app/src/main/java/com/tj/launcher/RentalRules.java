package com.tj.launcher;

/**
 * BT1c (user, 2026-11-09: "yes add restrictions -- add options in settings"), v1.2: the pure rules (JVM-tested) of the Rental profile's restrictions. Each is its own switch,
 * release default ON, debug default OFF (the project rule for new behaviour). They act ONLY while the custom launcher shows Rental; FBOP and Owner are never touched.
 *  - Settings: the Settings app is hidden from Rental's drawer, and any Settings screen that still opens (the gear in the shade, "App info") is sent Home.
 *  - Play Store: hidden from Rental's drawer and sent Home if it opens anyway.
 *  - Uninstall: Android's "Uninstall this app?" screen is closed (Back) before anything can be confirmed.
 */
public final class RentalRules {
    public static final String SETTINGS_KEY = "rental_block_settings", STORE_KEY = "rental_block_store", UNINSTALL_KEY = "rental_block_uninstall";
    public static final String SETTINGS_PKG = "com.android.settings", STORE_PKG = "com.android.vending";
    public static final int NOTHING = 0, GO_HOME = 1, GO_BACK = 2;

    private RentalRules() {}

    public static boolean defaultOn(boolean debuggable) { return !debuggable; }

    /** Hidden from the drawer of THIS profile? (On top of the profile's own app list.) */
    public static boolean hiddenInDrawer(ProfileRules.Profile profile, String pkg, boolean blockSettings, boolean blockStore) {
        if (profile != ProfileRules.Profile.RENTAL || pkg == null) return false;
        return (blockSettings && SETTINGS_PKG.equals(pkg)) || (blockStore && STORE_PKG.equals(pkg));
    }

    /** Android's uninstall confirmation (AOSP and Google package installers). */
    public static boolean isUninstallScreen(String pkg, String cls) {
        if (pkg == null || cls == null) return false;
        boolean installer = "com.android.packageinstaller".equals(pkg) || "com.google.android.packageinstaller".equals(pkg);
        return installer && cls.contains("Uninstall");
    }

    /** A window of {@code pkg}/{@code cls} just came to the front: what to do. Only in the custom launcher with Rental active, never on the lock screen. */
    public static int windowAction(boolean customMode, boolean locked, ProfileRules.Profile active, String pkg, String cls,
                                   boolean blockSettings, boolean blockStore, boolean blockUninstall) {
        if (!customMode || locked || active != ProfileRules.Profile.RENTAL || pkg == null) return NOTHING;
        if (blockUninstall && isUninstallScreen(pkg, cls)) return GO_BACK;
        if (blockSettings && SETTINGS_PKG.equals(pkg)) return GO_HOME;
        if (blockStore && STORE_PKG.equals(pkg)) return GO_HOME;
        return NOTHING;
    }
}
