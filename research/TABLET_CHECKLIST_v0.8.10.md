# Tablet checklist for v0.8.10 (written 2026-11-08; the fix is proven on the CI emulator, NOT on the real tablet yet)

One change since v0.8.9. Install: Release page, `Launcher-v0.8.10.apk`, over the old one (same key: permissions, Home choice, accessibility stay). The adb helper does not survive an install; the accessibility service reconnects by itself.

## What changed (your message of 2026-11-08 16:21 UTC: "The home screen also must match the dimming just as the lock screen did. Right now the patch looks correct except the dimming speed makes the patch appear")
**On the custom home screen, the patch over the hidden Screenshot button now follows the stock menu's dimming the way the lock screen's patch does.**
* What was wrong (measured, not guessed: emulator runs #382 / #383, `research/HOME_PATCH_2026-11-08.md` has the numbers): on the home screen the helper did its heavy checking of the first window event of the menu opening BEFORE it looked at the menu's own event. That check took 50-200 ms on the emulator, so the patch only started to dim when the stock fade was nearly over: it stayed bright (up to +24 % lighter than the dimming screen around it) and then jumped dark. The lock screen already waited for the menu's own event first; now the home screen does too.
* After the fix, on the emulator the patch follows the fade from its first frame (5-28 ms after the stock dimming starts, 14-18 frames, almost none late) and the patch opened by the fast path in 10 of 10 films (v0.8.9: 1 of 10).

## Do this
**A. Install and use the custom home screen as usual.** Nothing to set up. The patch and its calibration are as in v0.8.9.
**B. Press the side key on the home screen a handful of times** (not right after touching the screen: a finger within the last 0.7 s makes the old card appear on purpose). Watch the square where the Screenshot button was while the screen dims. It should be invisible during the whole dimming, as on the lock screen. Also watch the other direction: tap the empty middle of the screen to close the menu.
**C. If a square is still visible, say WHEN:** at the very start (a lighter square before the screen dims), in the middle, or at the end; and whether it is lighter or darker than the screen around it. That tells me whether the timing needs a nudge.
**D. Optional nudge (only if C shows a steady lead or lag):** Launcher control > "patch fade timing" moves the patch's dimming earlier (+) or later (-) in milliseconds. It applies to the lock screen AND the home screen, so tell me before you use it if the lock screen is still perfect for you.

## Send back
* The tablet report (as in v0.8.9). New line to read: `last fade seen from the patch (BJ1)`. It says for the LAST menu opening: home or lock, how many ms after the stock dimming started the patch drew its first frame, how many frames it drew, how many came later than 34 ms after the one before (a frame the main thread was too busy to draw), the worst gap, and **the system's animator scale** (1.0 is normal; 0 means "Remove animations" is on: then the stock dimming is ONE frame and no patch fade can match it).
* A word on B and C. Photos stay out of the repo as agreed (view-only for me).

## If something is wrong
* Launcher control > the home patch switch OFF puts back my card on the home screen at once. "Restore everything" resets all switches.
* Nothing else changes: the lock screen, the drawer, the Recents rules, the notification rules and the animation rules of v0.8.9 are as they were.
