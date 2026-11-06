# Tablet checklist for v0.8.1 (written 2026-11-06 night)

What changed since v0.8.0, what to try on the tablet (about 10 minutes), what to send back.
Install: open the Release page, download `Launcher-v0.8.1.apk`, install it over the old one (same key: permissions, Home choice and accessibility stay).

## What changed
1. **Battery saver works again, everywhere (the real cause was found).** Android 10 keeps its own memory of whether the saver was turned on "by hand". Every time the launcher wrote the saver on or off directly (v0.7.3 "off in the custom launcher", v0.7.5 "on in Quickstep") that memory went out of step, and from then on the tile **and** the Battery saver screen in Settings were silently ignored ("it says it turned off but it never does"). It heals itself only when the tablet is plugged in or restarted - that is why it "sometimes worked". Proved on a real Android 10 with the real Settings screen (research/AOSP10_BATTERYSAVER_AND_POWERMENU_2026-11-06.md).
   Now: **Quickstep is 100 % stock** (the launcher never switches the saver on). The custom launcher still switches it off when it takes over. If a tap on "turn on" is ever ignored after that, the launcher notices and carries it out for you. A saver that the old versions had forced on is cleared once, right after you install this.
2. **The lock-screen patch no longer flickers when the power menu opens.** The stock menu darkens the screen around it with a 0.3 second fade. The patch used to jump to the dark value in one step (and, because the launcher was busy searching the menu at that moment, a moment late). It now follows the same fade from the same start and reacts before anything else. The darkening of the lock screen itself cannot be switched off from outside (it is drawn by the stock menu); an animation setting would only make it instant.
3. **Strict lock screen, from any launcher**: the quick settings on the lock screen show the **rotation tile only** (your Quickstep tiles come back when you unlock into Quickstep); **Wi-Fi and Bluetooth stay off the whole time** the lock screen is up (a radio that keeps coming back is switched off again every few seconds instead of being given up on after three tries); the lock number is drawn in both modes; notifications stay hidden. Also right after a restart, before the helper has connected.
4. **The patch calibrates itself.** The first time a plain lock screen shows without a valid calibration (new wallpaper, rotation or screen size), the helper takes one screenshot of it and keeps only the crop around the Screenshot spot. No step for you. (Every try leaves one screenshot in the gallery; at most 3 per lock and 8 per day. A screenshot taken while the power menu or the PIN pad is showing is refused.)
5. Control shows "enabled but NOT connected" when the accessibility helper is switched on but stuck (restart the tablet once); the patch line is no longer green when the stored calibration does not fit the current wallpaper.

## Try this
**A. Battery saver (the main one)**
1. In Quickstep pull the notification panel down and tap the **Battery saver** tile: it should switch on at once; tap again: off at once.
2. Open **Settings > Battery > Battery Saver**: "Turn on now" then "Turn off now" must both really work (the page shows the new state).
3. Turn it on in Quickstep, lock, unlock with the plain PIN (custom launcher): it is off. Unlock into Quickstep again and tap the tile to turn it on: it should work.
4. If something is still odd: Launcher control > Advanced & diagnostics > tablet report. The line `battery saver guard:` shows `self-heals so far` and the last thing the launcher did.

**B. The patch**
- Lock the tablet, wake it, wait a few seconds on the plain lock screen (no PIN pad). Launcher control top line **Lock-screen patch** should become "calibrated ... (valid for the lock screen as it is now)" by itself. (If it says it is waiting to learn the menu's place: hold the side key once, close the menu.)
- Hold the side key: the menu should open with **no light/dark flash** over the Screenshot spot, and again when you close it. If you still see one, tell me **when** (opening or closing) and how it looks (a lighter or a darker block). In Launcher control > Power menu there is a switch "Fade the patch together with the stock menu" and a **Fade timing** step (- = the patch darkens later, + = earlier): try a step or two if it flashes.
- Pull the notification panel down on the lock screen: nothing lighter should show in the lower middle-right.

**C. The strict lock screen**
- Lock from Quickstep: open the quick settings on the lock screen: only the rotation tile. Turn Wi-Fi on from somewhere while locked: it goes off again. Unlock with a gesture into Quickstep: your tiles and Wi-Fi are back.
- Restart the tablet: the first lock screen after the restart should already show the rotation tile only.

## Send back
- Anything that flashes, blinks or looks different (say when).
- The `battery saver guard:` and `accessibility helper load (AD8)` lines of the tablet report (or the whole report).
- Photos only in the chat, never in the repo.
