# Cloning the stock Settings app — feasibility (2026-11-02)

Tj asked (verbatim): "consider how you can build a cloned settings app which looks and behaves exactly identical to the stock Android 10 go settings already on the tablet, but I can customize it to remove settings and rearrange settings. Maybe download the actual settings app code just like you did with the actual quickstep launcher code?"

## What I checked (AOSP `packages/apps/Settings`, tag android-10.0.0_r47, downloaded 2026-11-02)
- 95 MB, **2,917 Java files**, 225 preference XMLs.
- `AndroidManifest.xml`: `coreApp="true"` and **`android:sharedUserId="android.uid.system"`** = it must be signed with the device's platform key and run as the system user. Our APK is signed with our own key, so Android refuses to install it (shared-UID mismatch), and removing the shared UID makes most pages unable to work.
- Requests dozens of **signature-only permissions** that a normal app can never hold: `WRITE_SECURE_SETTINGS`, `MANAGE_USERS`, `NETWORK_SETTINGS`, `MASTER_CLEAR`, `REBOOT`, `MANAGE_DEVICE_ADMINS`, `MANAGE_USB`, ... Wi-Fi, Bluetooth, storage, accounts, security pages all rely on them.
- 214 files import `com.android.internal.*` (hidden framework internals) and 886 import `com.android.settingslib.*`, a separate library from `frameworks/base` that is not in this download and also depends on internals.
- It is built inside the full AOSP tree (Soong/Make), not Gradle, and the Alps vendor has its own modified Settings (it shows "DuraSpeed", "711") that is not public.
**Verdict: the real Settings code cannot run as an installed app on a locked bootloader. A true clone is impossible; this is a platform limit, not effort.** (The launcher worked because Launcher3's Go build only needs public APIs.)

## What CAN be done (recommended): a "Settings" front door that looks identical and opens the REAL pages
1. **Home list = pixel copy of the stock Settings home** (search box, coloured circle icons, title + summary rows: Network & internet, Connected devices, Apps & notifications, Battery, Display, Sound, Storage, Privacy, Location, Security, Accounts, Accessibility, DuraSpeed, System, About tablet). Reference photos: `research/reference/stock/stock-settings-*.png`.
2. **Each row opens the real stock page** through the public settings intents (`Settings.ACTION_WIFI_SETTINGS`, `ACTION_BLUETOOTH_SETTINGS`, `ACTION_APPLICATION_SETTINGS`, `ACTION_DISPLAY_SETTINGS`, `ACTION_SOUND_SETTINGS`, `ACTION_INTERNAL_STORAGE_SETTINGS`, `ACTION_PRIVACY_SETTINGS`, `ACTION_LOCATION_SOURCE_SETTINGS`, `ACTION_SECURITY_SETTINGS`, `ACTION_SYNC_SETTINGS`, `ACTION_ACCESSIBILITY_SETTINGS`, `ACTION_DEVICE_INFO_SETTINGS`, ...), so every setting really works. Rows whose intent does not resolve on the tablet (e.g. vendor DuraSpeed) are found at runtime or hidden.
3. **Customize (Quickstep mode only, like the app chooser):** hide rows, reorder rows. The stock Settings icon is hidden from the custom drawer and our "Settings" (same gear icon/label) takes its place.
4. **Limits (honest):** inside a real page the stock Settings shows its own menus, so a page you hid on our list can still be reached from another page's "Advanced"/search. Optional hardening: the accessibility helper can close specific Settings pages the moment they open (it already sees windows), with the same fail-open rule. The search box on our list searches OUR list.
5. Alternative considered and rejected: re-implement every page ourselves (Wi-Fi, Bluetooth, ... need system APIs we cannot call), or ship a modified Settings APK (needs the platform key).
