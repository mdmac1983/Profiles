# Tablet checklist for v0.8.0 (written 2026-11-06)

What changed since v0.7.7, what to try on the tablet (about 10 minutes), what to send back.
Install: open the Release page, download `Launcher-v0.8.0.apk`, install it over the old version. Same signing key, so the permissions, the Home choice and the accessibility setting stay.

## What changed
1. **The Screenshot button in the lock-screen power menu is hidden by an invisible patch.** The stock menu is not changed at all (Power off and Restart stay stock). The patch is cut from ONE real screenshot of your own lock screen, so it matches the picture pixel for pixel and sits there from the first frame of the menu (no flash). It needs one calibration (below), and again after you change the lock wallpaper or turn the tablet.
2. **Launcher control is regrouped** into 10 collapsible groups (Everyday, Lock screen, Unlock into Quickstep (secret handshakes), Power menu (side key), Recents & wallpaper, Return to the custom launcher (from Quickstep), Battery, radios & speed, Quick settings & keyboard, Safety & secret ways to Quickstep, Advanced & diagnostics). Each header shows "n of m switches on", the open/closed state is remembered, and **Restore everything** is a fixed footer. Nothing was deleted: `research/SETTINGS_AUDIT.md` lists where every switch went.
3. **Unlock and restart were measured** on the emulator (research/PERF_AUDIT.md): a plain PIN in a custom session resumes only our launcher; the handshake goes to Quickstep at once; no overlay window can stay behind.
4. Small hardening: the resting patch checks itself again 0.4 s and 1.6 s after it appears; the Recents guard looks a few more times over 4 s on a slow tablet.

## Try this
**A. Calibrate the patch (once)**
1. On the lock screen hold the side key once (so the menu spot is known), then close the menu.
2. Unlock into Quickstep, open **Launcher control**, group **Power menu (side key)**, tap **Calibrate the patch (one screenshot of the lock screen)**. If the top line says "needs permission", tap **Allow wallpaper access** first.
3. Lock the tablet and wake it. The lock screen takes the screenshot by itself (or press Power + Volume Down yourself).
4. Unlock. The line **Lock-screen patch** at the top should say CALIBRATED.

**B. Look at the lock screen**
- Closed lock screen: no visible seam, block or colour change near the bottom of where the power menu sits.
- Hold the side key: the menu shows Power off + Restart only, nothing flashes when it opens or closes.
- Tap where the Screenshot button used to be: the menu closes and no screenshot is taken.

**C. Launcher control**: open it, open and close a few groups, check that Restore everything stays at the bottom.

**D. Daily use**: unlock with the plain PIN (it should land on the custom launcher with no Quickstep screen showing), open two or three apps, press Home twice (return reset), open Recents (only apps from the custom drawer).

## Send back
- Anything that flashes, blinks or looks different from the stock lock screen (say when it happens).
- The text of the **Lock-screen patch** line.
- Photos only in the chat, never in the repo (blur anything private as before).
- If something is off: Launcher control > Advanced & diagnostics > the tablet report, then send `tablet-report.txt`.
