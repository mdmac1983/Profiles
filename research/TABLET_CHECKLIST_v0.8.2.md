# Tablet checklist for v0.8.2 (written 2026-11-07)

What changed since v0.8.1, what to try on the tablet (about 10 minutes), what to send back.
Install: open the Release page, download `Launcher-v0.8.2.apk`, install it over the old one (same key: permissions, Home choice and accessibility stay).

## What changed
1. **The patch is now dimmed with the stock menu's REAL dimming, measured on your tablet.** Your report: the end of the open is "very slightly too dark" and the square shows "but barely". The stock power menu darkens the lock screen with a flat dark layer: for every colour `shown = scale x before + offset`. On the Android emulator that layer is pure black at 20 % (scale 0.80, offset 0), which is what v0.8.0 / v0.8.1 assumed (0.79, then 0.80). Your tablet's layer is evidently not exactly that (each step made the square fainter but never gone; earlier photos also showed a colour offset, red most). No guess can be 100 % identical, so now the helper **measures it once on the tablet**:
   - on the first plain lock screen after a new wallpaper / rotation / theme (and right after you install this version, because the patch picture already exists) the helper takes a frame of the plain lock screen, **opens the stock power menu by itself for about two seconds**, takes a second frame, and **closes the menu again**;
   - from the two frames it works out the scale and the colour offset for red, green and blue and keeps them (Launcher control > Power menu > "Menu dimming measurement" shows what it found);
   - from then on the patch follows the stock fade with exactly that dimming (the fade timing you called perfect is untouched).
   You will see **two camera flashes and the power menu appear and go by itself, once**. It only starts on a plain lock screen (screen on, no PIN pad, no gesture in progress), stops at the first sign of anything else, and tries at most twice per lock and four times per day. Switch: Launcher control > Power menu > "Measure the menu's real dimming on this tablet". If anything looks wrong, tap nothing: it closes the menu itself.
2. **Battery saver is always OFF while the lock screen is up, from any launcher** (the dark theme and the stock menu's fade then never depend on it):
   - locking from Quickstep with the saver on: it is switched off at the lock moment (while the screen is dark), and if something switches it on again while the lock screen is up it is switched off again within a moment;
   - unlocking into the custom launcher: it stays off (as since v0.7.3);
   - **unlocking into Quickstep with your gesture: the saver goes back to what it was the last time you were in Quickstep** (also after a detour through the custom launcher). It is only switched back on when Android cannot have lost track of it: not while charging, not if the tablet was plugged in or charged meanwhile, not after a restart. In those cases it stays off and the tile in the shade is one tap away. Why: switching it on from outside when Android's own memory was reset is exactly what once made "Turn off" do nothing; this version never does that (proved on the real Settings screen: after the restore, "Turn off now" really turns it off).
   - Switches: Launcher control > Battery, radios & speed: "Battery saver off on the lock screen" and "Bring Quickstep's battery saver back after a gesture unlock".

## Try this
**A. The patch (the main one)**
1. Install, lock the tablet, wake it, wait on the plain lock screen (no PIN pad): about 10-20 seconds later you should see two flashes and the power menu open and close by itself. Launcher control > Power menu > "Menu dimming measurement" should then say `MEASURED scale ... offset ...` (send me those numbers).
2. Hold the side key: the menu opens. At the **end** of the opening (after the fade) the square over the Screenshot spot should now be invisible. Look at it in a bright and a dark part of your wallpaper if you can. If you still see an edge, tell me **darker or lighter**, and whether it is the whole square or only its border.
3. If the measurement did not run: Launcher control shows why in plain words under "Menu dimming measurement" (for example waiting for the side key to be held once on the lock screen). Send that line.

**B. Battery saver**
1. In Quickstep turn the saver ON with the tile. Lock the tablet. Look at the lock screen: the saver icon is gone and the quick settings show the rotation tile only.
2. Unlock with your gesture into Quickstep: the saver is on again (the status bar shows it), and the tile or Settings > Battery > Battery Saver can turn it off again at once.
3. Same, but unlock with the plain PIN (custom launcher): it stays off. Then lock again and unlock into Quickstep with the gesture: it comes back on (it was on the last time in Quickstep).
4. Plug in the charger while the tablet is locked, unplug, unlock into Quickstep: the saver stays off (by design), the tile works.

## Send back
- The `Menu dimming measurement` line (Launcher control) and, if you can, the tablet report lines starting `menu dimming measurement (BA1)` and `battery saver on the lock screen (BA2)`.
- What you still see at the end of the open (darker / lighter, whole square or edge), and anything that flashed.
- Photos only in the chat, never in the repo. A new photo of the open menu is measured with `python3 -I tools/patch_photo_step.py <photo>` (2026-11-07: the same command on Tj's v0.8.1 photos said the square under Restart was 5 to 7 units of 255 darker than the lock screen around it; after v0.8.2 it should say NO CLEAR STEP). Best photo: straight on, the open menu AND the area under it in the frame (about 1.75 button heights below Restart), sharp, flash off, exposure locked, taken after the 300 ms fade.
