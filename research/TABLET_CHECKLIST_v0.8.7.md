# Tablet checklist for v0.8.7 (written 2026-11-08)

What changed since v0.8.6, what to do (almost nothing is a test: this release MEASURES), and what to send back.
Install: Release page, `Launcher-v0.8.7.apk`, install over the old one (same key: permissions, Home choice, accessibility stay). The adb helper does not survive an install (it needs its start line again, see C).

## What changed (your message of 2026-11-08 01:10 UTC)
1. **A little less waiting at the unlock.** The accessibility helper and the launcher share one thread; the helper used to write several small files on that thread at the moment of an unlock. Now they are written in the background.
2. **Two new lines in the tablet report (Launcher control > Tablet setup & report):**
   * `process life (BG1 ...)`: how many times Android started the launcher process in the last 24 hours / hour, how old this one is, and the process's priority every time the tablet locked. This answers "can it be kept in RAM?" with numbers.
   * `shell helper flight recorder (HLP1 ...)`: the adb helper dies a few minutes after it is started and nobody knows why. It now writes down how long it lived, how it ended (shutdown, error, or silently killed) and the system's kill messages just before. This is only filled after the helper has run.
3. Nothing else looks different. The power menu and the lock screen are untouched.

## Do this
**A. Nothing, for a day.** Use the tablet as usual (lock, unlock, Quickstep now and then). Then send the tablet report (see "Send back"). The `process life` line then covers a whole day.
**B. Optional (RAM):** Launcher control > Battery, radios & speed > turn on "Keep the launcher in memory". Use it for a day and send the report again. If the process start count drops and the priority while locked stays low, that switch is worth keeping.
**C. When you have the cable (the one session that decides most of the next steps):**
1. Start the helper with the usual star line (the phone guide, PART 2).
2. Run the side-key test (setup button "3. Side-key test": hold the side key about 1.5 seconds inside the 8 seconds).
3. Unplug, and LEAVE THE TABLET ALONE for about one hour (use it normally if you like, lock and unlock it).
4. Send the tablet report. Its `shell helper flight recorder` line says how long the helper lived and why it stopped.

## Send back
* The tablet report (as before: `bash tools/tablet-intake.sh <file>` on my side).
* Which power-menu mode is selected (Launcher control > Power menu): "Hide Screenshot" or "Wallpaper"? (The report's `power_mode` line shows it too.)
* An answer to the one question about the custom launcher's power menu (see my message): go / no-go for the lock-screen-style patch on the home screen.
* If you open the power menu on the custom home screen a few times: the report's "Speed of the last menu" line says how long our cover takes there; that is the number to cut.
