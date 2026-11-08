# Tablet checklist for v0.8.6 (written 2026-11-08)

What changed since v0.8.5, what to try on the tablet (about 5 minutes), what to send back.
Install: open the Release page, download `Launcher-v0.8.6.apk`, install it over the old one (same key: permissions, Home choice and accessibility stay). Nothing new to grant: it uses the same adb permission as the keyboard and tiles.

## What changed (your message of 2026-11-07 23:36 UTC)
**Animations are off ONLY inside Quickstep, and back the instant you leave it.**
* New switch, default ON: Launcher control > Everyday > "Animations off in Quickstep (faster)". Under it a line shows the live state ("animations ON (normal)" or "OFF, held for the Quickstep session") and the three values.
* It is the same thing as Settings > Accessibility > "Remove animations" (the three animation scales set to 0), done for you.
* OFF while: you are in a Quickstep session (the stock home and the apps you open there), the tablet is unlocked and the screen is on.
* ON again, at once: the moment the screen goes off (before the lock screen can show), on the lock screen and its power menu (they need real animations), after a plain PIN (the custom launcher), and the moment the custom launcher takes over (double Home, the Back-to-custom icon or tile, the other return methods).
* Put back EXACTLY as they were. If you change a scale yourself in Developer options during a Quickstep session, that value is left alone.
* Switch OFF = the launcher never touches the animations (except the short one inside the return reset, as before).

## Try this
**A. Quickstep feels faster.** Unlock with your gesture into Quickstep, open and close two or three apps, open Recents: no opening / closing animations. Launcher control > Everyday: the line under the new switch says OFF.
**B. Back on at once on the lock screen.** From Quickstep press the side key and wake the tablet: lock screen. Hold the side key for the power menu: the menu and the wallpaper patch must fade in exactly like in v0.8.5 (this is the part that must not change; it needs the real animations). Later, in the tablet report, the lock log has `ANIM ON again: screen off` for that lock.
**C. Plain PIN.** Unlock with the plain PIN: custom launcher, animations normal (swipe to the drawer, open an app: it animates). The log must show NO new `ANIM OFF` line for that unlock.
**D. Back to custom.** Gesture into Quickstep, then double Home: custom launcher, animations normal.
**E. Optional:** Developer options > Window animation scale: set 1x while in Quickstep, lock and unlock with the gesture: the scale stays 1x for that value (your change wins) and the others go back to what they were.

## Send back
* Whether Quickstep feels faster, and whether anything looks wrong because animations are off (a screen that flickers, an app that misbehaves).
* Whether the lock-screen power menu and the wallpaper patch still look and time exactly as in v0.8.5.
* If something is off: the tablet report right after (Launcher control > Tablet setup & report, then `bash tools/tablet-intake.sh <file>`): it has the line `animations (BF1 ...)` with the switch, the three values and the last change, and the lock log lines `ANIM OFF ...` / `ANIM ON again: <reason> (<trigger>)` say exactly when and why each change happened.
