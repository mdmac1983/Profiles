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

## v0.2.1 additions (2026-11-02)
- Look: 5-column grid + 5-column drawer, Go icon size (60 dp), drawer scrim = 50% white (measured from your stock photos). Row pitch is ~7% taller than stock (needs your answer: does the stock home have a dock?).
- **Power-menu helper (optional, OFF by default).** Open the "Launcher control" app icon > choose "Power menu: hide Screenshot button" (or "block it completely") > "Open Accessibility settings"
  > find "Launcher power-menu helper" > turn it on. It only acts while the custom launcher is active and never on the lock screen; switch back to Quickstep (or choose "leave alone") and the normal power menu returns.
  Proven on an Android 10 emulator (Screenshot entry fully hidden). Your tablet's panel colour may differ a little: send a photo if you can see a patch.
- If Android ever shows "Select a Home app": choose Launcher > Always (or Quickstep to go back). It is only a chooser.

## Getting back to the custom launcher from Quickstep (v0.2.2)
- **Press Home twice quickly** (within ~0.7 s) while the stock launcher is showing: you are back on the custom launcher. No settings or permissions needed.
- Or open the "Launcher control" app icon (in the stock drawer) > "Use custom launcher".

## v0.2.2 (2026-11-02)
- Home screen: 8 rows, NO dock, no "^" caret (matches your stock photos). Drawer unchanged from v0.2.1 (it matched).
- Power menu: "remove Screenshot (two-button menu)" now covers the whole system card with Power off + Restart (no gray gap); tapping them presses the real buttons. If a press ever fails the original menu is revealed.
- **Double-press Home** in the stock launcher = back to the custom launcher.

## v0.2.3 (2026-11-02)
- **Return reset.** When you double-press Home to come back from the stock launcher, the launcher now: asks Android to close every app (apps with their own running service may stay), clears all notifications,
  turns Wi-Fi and Bluetooth off, and opens your Solitaire app (found by name; Launcher control shows which app it found). Master switch: Launcher control > "Return reset: ON / OFF".
  For notifications to clear, turn on **Notification access** for "Launcher" (Launcher control has a button that opens that page). Recents thumbnails can remain (clearing the Recents list needs a system permission).
- **Twist gesture** is more responsive: the home screen no longer scrolls while two fingers are down, and it fires after 30 degrees (was 40).
- **Faster power-menu cover**: acts on the first event and remembers where the card is (a very brief flash may remain; Android creates the menu before we can react).

## v0.3.0 (2026-11-02) — hide apps, lock, Recents, reset fixes
- **Hide apps / new installs.** Quickstep mode > Launcher control > "Choose apps shown in the custom launcher": tick = shown in the custom drawer AND its search. Everything you already have is ticked the first time the new launcher runs;
  apps installed (or uninstalled and reinstalled) later start UNticked and stay hidden (also kept off the home screen) until you tick them. Quickstep always shows everything.
- **Lock.** Launcher control and the app chooser do not open and have no icon while the custom launcher is on; use the twist (or Home-Home is for the other direction) to reach Quickstep first.
- **Reset on double-Home** (Quickstep -> custom): no more Home press (that caused the Solitaire glitch: Solitaire was in Recents but the home screen covered it). Solitaire is started last and re-started up to 3 times until it is actually in front; Launcher control > "Last reset" says what happened.
- **Three independent switches** (Launcher control, Quickstep mode): A = apps launched from the custom launcher stay out of Recents except Solitaire; B1 = press "Clear all" in the notification shade; B2 = press "Clear all" in Recents (closes those apps). B1/B2 need the accessibility helper. Try them alone and together and tell me which works best.
- Notification access does not exist on Android Go, so the old notification route is gone.

## v0.3.1 (2026-11-02)
- **Twist gesture** fires after 18 degrees (was 30), with looser pinch tolerance: more responsive. **Double-press Home** window is now 0.9 s (was 0.7 s).
- **Solitaire in front whenever the custom launcher starts** (boot, or if Android restarts it) and on return from Quickstep. Switch: Launcher control > "Open the launcher at start". Normal Home presses stay stock.
- **Our launcher never appears in Recents** (it lost its home status when the router took over; fixed with an exclude flag).
- **Faster reset:** polls every 40 ms for the real "Clear all" buttons instead of waiting; an empty shade/Recents ends instantly; "Last reset" shows how many ms it took.
- **Keyboard switch (needs one adb command):** `adb shell pm grant com.tj.launcher android.permission.WRITE_SECURE_SETTINGS`, then Launcher control > "Keyboard: stock in custom launcher, SwiftKey in Quickstep: ON". It only switches to a keyboard that is already enabled, and "Restore everything" puts your original back.

## v0.3.2 (2026-11-03)
- **After installing, turn the helper off and on once** (Settings > Accessibility > "Launcher power-menu helper"): it now also asks to see the side (power) key, and Android re-reads that when the service restarts.
- **Quickstep lock-out (W5).** In custom mode, if the stock home screen shows up without your twist (e.g. after closing the last app) the helper presses Home and the router sends you back to the custom launcher. Needs the helper ON. It stands down if the custom launcher cannot start, and after 4 redirects in 8 s (no loops). Launcher control > "Quickstep lock-out" switch + a counter of how many times it fired. The twist still works.
- **Recents (W2).** Our launcher and its screens are now marked "excluded from Recents" at runtime (the old flag only worked on newly created tasks, so an old Launcher task kept showing). Fallback: if a card still appears it is named and iconed like Solitaire.
- **Faster double-Home reset (W1).** Closing apps / Wi-Fi / Bluetooth now run in the background, Clear all is polled every 20 ms from the first instant, shorter waits, Solitaire checked sooner. "Last reset" in Launcher control lists the ms for each step: send me that line.
- **Power menu (W3/W4).** The replacement card is now kept ready (invisible, untouchable) and just made visible when the menu opens; and when the side key goes DOWN the helper shows it at once, before the long-press menu exists. Launcher control shows "Side key seen by the helper: N times": if it stays 0 after you press the key, the hardware does not pass that key to accessibility and the early show cannot work on this tablet.
