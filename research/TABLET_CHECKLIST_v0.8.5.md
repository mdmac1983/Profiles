# Tablet checklist for v0.8.5 (written 2026-11-07, released 23:16 UTC)

What changed since v0.8.4, what to try on the tablet (about 10 minutes), what to send back.
Install: open the Release page, download `Launcher-v0.8.5.apk`, install it over the old one (same key: permissions, Home choice and accessibility stay).

## What changed (your message of 2026-11-07 20:17 UTC)
1. **The custom launcher now starts clean** (new switch, default ON: Launcher control > Everyday > "Start the custom launcher clean (no Solitaire)").
   * No Solitaire (or any app) is opened for you any more. After a plain PIN you see the custom home screen alone.
   * "Completely clear the recent apps list every time the custom launcher starts" = whenever a custom session STARTS: after a Quickstep session ends (plain PIN, double Home, the return methods) and at the first unlock after the tablet was restarted. Everything in Recents is closed then, including apps you had opened from the custom launcher before. It does NOT clear at every unlock of a custom-only session (you keep the apps you are using) and not when Android restarts the launcher process by itself.
   * Switch OFF = the old behaviour exactly (Solitaire in front, only the apps opened in Quickstep closed).
2. **A hidden app can no longer sit in the custom Recents** (new switch, default ON: "Keep hidden apps out of Recents (invisible helper)"). With the adb helper running, the Recents tasks of every app that is NOT in the custom drawer are removed in the background: when the tablet locks, a moment after a plain unlock, when the Recents screen opens, and 0.4 s after such an app leaves the screen. The decision is "is this app in the drawer?", not "which launcher opened it" (that guess is what let Telegram and Claude through).
   Without the helper the old on-screen filter still closes the cards after the Recents screen opened (a little faster than before, all cards in one pass), and the clean start's "Clear all" runs behind the black cover.
3. **The unlock into the custom launcher is faster.** The long black screen was the cover staying up until Solitaire had been started cold (seconds on this tablet) and verified. Now the cover goes away the moment the custom home is in front.
   * With the helper running (the adb line after each restart): the clean-up happens WHILE THE TABLET IS LOCKED (screen off), so the unlock has nothing left to do: no cover at all.
   * Without the helper: the cover is still needed while the shade and Recents are cleared on screen, but only as long as that takes.
4. **The wallpaper patch** (two things you reported):
   * The flicker when you pull up for the PIN: the patch now disappears at the moment your finger touches the screen, before the PIN pad can darken the wallpaper under it (it used to wait for the lock screen's own "PIN pad is coming" signal, which arrives later when the tablet is busy).
   * The Screenshot button that showed once: if the patch was "away" (after a touch or a PIN pad) when the power menu opened, the old code took the slow path and the stock Screenshot slot could show for a moment. Now the patch comes back at the menu's own first signal. Also the patch window is created while the screen is still dark, not during the wake-up.

## Try this
**A. Plain PIN after Quickstep (the main one)**
1. Unlock with your gesture into Quickstep, open two or three apps (include one that is hidden in the custom launcher), lock the tablet.
2. Unlock with the plain PIN. You should see the custom home screen, with no Solitaire and no long black screen. Open Recents: it must be empty.
3. Launcher control > Report: the lock log (`RESET`, `HOME custom home in front +N ms`, `CLEANLOCK`, `SWEEP`) shows what happened and how long it took. If the helper was not running the log says so (`LOCKPREP no shell helper`).
**B. With the helper** (run the adb line once after a restart): repeat A. The cover should not appear at all, and the lock log shows `CLEANLOCK complete clear` at the lock.
**C. A hidden app in custom mode**: open Telegram some other way (a notification), press Home, open Recents: it must not be there (with the helper). Without the helper the card is closed right after the Recents screen opens.
**D. The patch**: pull up from the bottom for the PIN several times, once with the power menu open and once without. Look for a flicker of the patch. Then press the side key right after dismissing the PIN pad (swipe it away): the Screenshot button must not show.

## Send back
* How long the black screen lasts now (a rough number is fine), whether Solitaire is gone, whether Recents is empty after the unlock.
* The tablet report right after a slow unlock (Launcher control > Tablet setup & report), so the lock log lines above can be read: `bash tools/tablet-intake.sh <file>`.
* Any flicker of the patch or Screenshot flash, with when it happened (the log lines `PATCH ...` show the order of finger, PIN pad and menu).
