# First install on the tablet — checklist (v0.2.x "safe core")

What v0.2 is: Launcher3 Go port (home, drawer, folders, search, wallpapers) + the safety core. It is NOT yet the finished product: the look is AOSP Go defaults
(4x4 home with search bar and dock, 4-column drawer) until the grid is matched to your stock layout, and the special features (hide apps, clone Settings,
power-menu screenshot hide, kill switch, keyboard switch) are not in yet. It changes NO system setting, needs NO permission beyond launcher basics
(call shortcuts, wallpaper, widgets), has no boot receiver and no lock-screen code.
Proven so far (Android 10 emulator, GitHub Actions run, every commit): HOME goes through an invisible router; custom<->Quickstep routing; 3 crashes in 2 minutes
switch itself back to Quickstep; uninstall leaves the stock launcher as Home; the manifest contains no device-admin/overlay/keyguard/boot/secure-settings permissions.
NOT proven (needs your tablet): the recents button with our app as Home (R3), vendor quirks, the real home layout/density.

Before installing: have a PC with `adb` ready if you can (not required): `bash tools/adb-restore.sh` puts everything back in one command.
1. Download the APK from the GitHub Release and install it (Settings may ask to allow "install unknown apps" for your browser/file manager).
2. Settings > Apps & notifications > Default apps > Home app > choose **Launcher**. Press Home: you should see the new launcher. (If you ever see a chooser, pick "Launcher" > Always.)
3. Test: Home button; long-press empty space (Home settings / Wallpapers); swipe up for the drawer; open an app; press Recents (square button): the recent apps screen should appear.
   Two-finger CLOCKWISE twist on the home screen -> goes to the stock launcher. Open the app icon **Launcher control** (in the stock launcher's drawer) -> "Use custom launcher".
4. If anything is wrong: Launcher control > **Restore everything** > or Settings > Apps > Default apps > Home app > Quickstep > or uninstall Launcher.
   Safe mode (hold power, long-press "Power off", OK) disables all user-installed apps and shows the stock launcher.
5. Tell Claude what you see (screenshots help): the home layout, the drawer, and whether Recents works.
