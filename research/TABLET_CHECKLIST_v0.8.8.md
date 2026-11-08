# Tablet checklist for v0.8.8 (written 2026-11-08)

What changed since v0.8.7 and what to do. Install: Release page, `Launcher-v0.8.8.apk`, install over the old one (same key: permissions, Home choice, accessibility stay). The adb helper does not survive an install. The accessibility service restarts by itself after the update; if Android asks again, switch it on in Accessibility settings.

## What changed (your message of 2026-11-08 02:29 UTC: "turn all notifications off for both the lock screen and the custom launcher")
1. **No pop-up banners outside Quickstep.** While the custom launcher is in front, or the tablet is dark or locked (in ANY launcher, so also the lock screen of a Quickstep session), a notification that arrives does not pop up at the top of the screen. In an unlocked Quickstep session pop-ups work as usual and the setting is put back exactly as it was. New switch: Launcher control > Everyday > "No pop-up notifications outside Quickstep" (shipped ON), with a live line under it. It uses the same one-time adb permission as the animation switch.
2. **The start-of-session clean-up looks in the shade when something was posted** since the last clean-up, even when the status bar shows no icon (a silent notification has none, but the shade lists it).
3. **The tablet report now says which app posted a notification, when, and in which state** (custom / Quickstep, dark / locked, in the custom drawer or not). Package names only: never the title or text. New report lines: `pop-up banners (BH1 ...)` and `notifications seen (BH1 ...)`. The accessibility helper now also receives the "a notification was posted" event; its description in Accessibility settings says so.
4. Unchanged, and NOT hidden by this release: a notification that is already in the shade, the small icon in the status bar, and the list in the pulled-down shade. The lock screen's own list was already hidden since v0.7.x (switch "lock screen quiet").

## Do this
**A. Use the tablet as usual for a day**, including a plain PIN unlock into the custom launcher while a Quickstep app (a chat app, Gmail) is receiving something. Note what you SEE: a banner at the top? a small icon in the status bar? something in the shade? on the lock screen?
**B. Send the tablet report.** The two new lines answer which app and when. Also the line `lock screen quiet: ... lock_screen_show_notifications = <value>`: it must read 0.

## Send back
* The tablet report (`bash tools/tablet-intake.sh <file>` on my side).
* Where exactly the notification appeared (banner / status-bar icon / shade / lock screen) and from which app, if you saw one again.
* Anything that now pops up in the custom launcher although it should not, or anything that stopped popping up in Quickstep although it should.

## If something is wrong
* Launcher control > the new switch OFF puts the setting back at once. "Restore everything" puts it back too.
* By hand (adb): `adb shell settings put global heads_up_notifications_enabled 1` and `adb shell settings delete global tj_banners_original`.
