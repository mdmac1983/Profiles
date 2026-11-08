# Tablet checklist for v0.8.11 (written 2026-11-08; NOTHING in this release has run on the real tablet yet, only on the CI emulator)

What changed since v0.8.10 and what to do. Install: Release page, `Launcher-v0.8.11.apk`, install over the old one (same key: permissions, Home choice, accessibility stay). The adb helper does not survive an install. The accessibility service restarts by itself after the update; if Android asks again, switch it on in Accessibility settings.

## What changed (your message of 2026-11-08 18:16 UTC, after you tried v0.8.10: "The home screen power menu looks good now ...")
1. **The first side-key press on the lock screen no longer flickers the patch (BK1a).** I reproduced it on the emulator: it only happens the FIRST time after the app process has started (after an install, a reboot or a restart of the app), when the patch has to draw the "menu is open" look for the very first time (graphics programs and code that load once). Later presses were always fine, as you saw. Now the patch draws that look once in the dark, invisibly (about 1/255 opacity for one frame), as soon as the lock screen is up, so the first real press already finds everything loaded. Emulator: the first fade had 8 frames and a worst gap of 107 ms instead of 3 frames and 278 ms.
2. **The pencil in the notification panel on the lock screen is covered (BK1b).** The white cover that already hid it on the unlocked quick settings did not appear on the lock screen after a Quickstep session (the app did not count that lock screen as "custom"). Now it does. Known limit: the cover is plain white, so with the dark theme it will not match.
3. **Unlocking the custom launcher is a bit faster, and now it is measured (BK1c).** In the no-helper case (PIN after a Quickstep session) the Recents and notification clean-up looked at the screen three times per step; now once. Emulator: 1865 ms down to 1538 ms from the PIN's Enter tap to the custom home. A new line records every unlock from start to finish, so your tablet can tell us where the time really goes. **This does NOT make the unlock instant** (see the question below).

## Do this
**A. First press (BK1a).** Install, then restart the tablet once (that is the "fresh" case). Unlock, lock, wake it, and press the side key ONCE on the lock screen. Did the patch flicker? Press again, and a third time. Photograph or film the first press if it still flickers.
**B. Pencil (BK1b).** On the lock screen, pull the notification panel down. The little pencil (edit) button should be hidden under a white cover. Do it after a Quickstep session (use the stock launcher a minute, lock, wake, pull down) and after a custom-launcher session.
**C. Unlock (BK1c).** Unlock with the PIN twice: once coming from the custom launcher, once coming from a Quickstep session. For each: about how many seconds from the last PIN tap until the custom home is usable, and what is on the screen in between (black, the stock launcher, Wi-Fi icons, notifications)?

## Send back
* The tablet report. Three lines matter: `FIRST fade since this process started (BK1a)` (did the first press stall?), `last fade seen from the patch (BJ1)`, and `Last unlock, start to finish (BK1c)` (also shown in Launcher control).
* ONE short answer for C: which unlock is slow, what you see during the wait, about how many seconds.
* **A decision only you can make:** a home-looking curtain during the unlock would make it LOOK instant (the no-helper wait is about 1.5 s of clean-up that hides Wi-Fi, apps and notifications), but touches during that second would be swallowed. Yes or no?

## If something is wrong
* There are no new switches. "Restore everything" in Launcher control resets all switches as before. Nothing else changed: the lock screen's real-pixel patch, the home-screen patch (v0.8.9/0.8.10), the drawer, the Recents rules and the notification rules are as they were.
* Details and every number: `research/BK1_LOCK_FIRST_PRESS_PENCIL_UNLOCK_2026-11-08.md`.
