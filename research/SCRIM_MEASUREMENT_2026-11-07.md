# The stock menu's scrim, measured on the tablet (BA1, v0.8.2) — how to read the result

Written 2026-11-07 for the session that gets Tj's tablet report. Code: `ScrimFit.java` (maths), `ScrimMeasure.java` (policy, sampling grid), `TjAccessibilityService` (`maybeMeasureScrim`, `scrimStep`, `scrimIngest`, `setOpenLook`). Tests: `ScrimFitTest`, `ScrimMeasureTest`, emulator section `pt` phase C.

## The model
The stock power dialog's background is `ScrimDrawable`: a flat colour at alpha `a` (0.2 at the end of the 300 ms fade) drawn over the lock screen. Per colour channel:
`shown = (1 - a) * before + a * C  =  s * before + o`, with `s = 1 - a` and `o = a * C` (C = the colour extractor's neutral colour).
* AOSP emulator: C is black -> s = 0.80, o = 0. That is all v0.8.0 (0.79) and v0.8.1 (0.80) could model.
* Tj's MediaTek tablet: v0.8.0 -> v0.8.1 made the end-of-open square fainter ("very slightly too dark ... barely") but never gone, and `PatchProfile` records that the MODEL patch was 3-25 % too dark there with red most. A scalar cannot be exact for a coloured C (an offset), which is why v0.8.2 measures `s` and `o` per channel on the tablet.
* Both `s` and `o` are linear in `a`, so the patch interpolates identity -> (s, o) with the SAME `PatchFade` progress as the stock fade: exact, not an approximation.

## What the helper does (once per lock screen = wallpaper | rotation | size | theme)
1. Plain lock screen only (screen on >= 3.5 s, no PIN pad, no menu, no gesture armed, a valid patch crop, menu geometry known). 2 tries per lock, 4 per day, 45 s apart.
2. Bare frame (`GLOBAL_ACTION_TAKE_SCREENSHOT`), sample grid: every 4th pixel outside the status/nav bands, the stock panel + 18 dp shadow, the patch window, our lock number; only FLAT pixels (centre vs neighbours: edges AND one-pixel strokes are dropped).
3. 2.4 s later (the first screenshot's animation is over) `GLOBAL_ACTION_POWER_DIALOG`; when the helper's own detection says the menu is up, wait `OPEN_SETTLE_MS` (600 ms: the fade is complete), second frame.
4. `GLOBAL_ACTION_BACK` (retried up to 5x, the first one even if the menu was not detected) until the window is gone. EVERY exit path closes a menu the helper opened.
5. `ScrimFit`: per channel a median-binned first guess, then trimmed least squares; gates: >= 3000 samples, rms <= 3 levels, spread >= 12 levels, >= 55 % inliers, 0.55 <= s <= 0.98, |o| <= 45, the three `s` within 0.05 of each other.
6. Stored as `patch_scrim_r<rotation>` = `sig#d|n#s,s,s#o,o,o#samples#rms#when` (n = dark theme). Valid only for the same lock screen signature and theme.

## Where to read it
* Launcher control > Power menu > "Menu dimming measurement": the note (`MEASURED scale ... offset ...` or why not) and the stored line.
* Tablet report: lines `menu dimming measurement (BA1): ...` and `battery saver on the lock screen (BA2): ...`.
* Lock log / `TjA11y` logcat tag lines starting `SCRIM` (started, asked for a screenshot, opened the stock power menu, finished: <outcome>).

## What a failure note means
| note | meaning | next step |
| :- | :- | :- |
| waiting: hold the side key once ... | the menu's place is not learned yet | Tj holds the side key once on the lock screen |
| waiting: the patch has no real-pixel picture yet | the automatic calibration has not produced a crop | see `patch_auto_note` |
| stopped: the power menu did not open | `GLOBAL_ACTION_POWER_DIALOG` was refused or the helper's menu detection did not fire within 4 s | check the `SCRIM opened the stock power menu (accepted=...)` log line; the MediaTek ROM may route the action differently |
| stopped: no screenshot appeared | the system's screenshot did not reach the media store in 8 s | same cause as a failed automatic calibration |
| not measured: the fit is not tight enough (rms X) | S1 is NOT an affine function of S0: a vignette / gradient / blur / live wallpaper | a non-uniform scrim: add a position term (fit y = s*x + o + g*(row)), or store a per-region map |
| not measured: the three colours disagree about the scale | not a flat scrim | same as above |
| not measured: the wallpaper is too flat ... | too little brightness range to tell s from o | none needed: a flat wallpaper hides the difference anyway |
| MEASURED scale ~0.80 offset ~0 | the tablet's scrim IS the AOSP one | then the remaining mismatch is NOT the scrim: compare a fresh bare frame with the stored crop (the lock screen may have changed since calibration: dark theme, keyguard dim, notification cards), see AZ4 |

## Why the stage is OFF in debug builds
`ScrimMeasure.defaultOn(debuggable)`: the emulator sections that do not test it must never see a power menu open by itself. `pt` phase C switches it on (`ptkey scrim_measure true`).

## FIRST REAL MEASUREMENT: Tj's tablet, v0.8.2 (screenshot of Launcher control, tablet clock 12:07 am = about 05:07 UTC on 2026-11-07, first plain lock screen after the install)
`Menu dimming measurement: 00:07:16 measured for this lock screen: scale 0.800 / 0.800 / 0.800, offset 6.0 / 7.0 / 7.0 (R/G/B; 30782 samples, rms 0.3); stored: scale 0.7999 / 0.8001 / 0.8002, offset 6.0097 / 6.9858 / 6.9799 for 30|r0|600x1024, 30782 samples, rms 0.2846`.
* **The tablet's scrim is the AOSP one in scale (0.800, three channels equal to 0.0003) PLUS an additive part of +6 / +7 / +7 levels of 255**: `shown = 0.80 * before + (6, 7, 7)`. rms 0.28 levels over 30782 samples = a perfectly flat layer. The scrim colour is `offset / 0.2` = about (30, 35, 35): a dark grey-teal (the colour extractor's neutral colour for this wallpaper), not black. The emulator's is black (offset 0.0-0.1), which is why 0.80 was exact there and never on the tablet.
* **What v0.8.0 (0.79) and v0.8.1 (0.80, offset 0) therefore did:** the square ended darker than the lock screen around it by exactly (6, 7, 7) levels at EVERY brightness (the stock scrim lifts every pixel by that much; the patch did not). A scalar can only trade level for slope, so each step "made it fainter, never gone", as Tj saw.
* **The camera photos predicted this before the measurement (TASKS BA4a, 02:14 UTC):** `tools/patch_photo_step.py` on Tj's v0.8.1 photos read the patch darker by +7.4 (whole-tablet photo, luminance; per channel R 6.8, G 7.6, B 7.0), +5.9 and +5.4 (close-ups), nearly the same at wallpaper levels 38 to 143 (= additive). True luminance offset from the tablet's own fit: 0.2126 * 6.0 + 0.7152 * 7.0 + 0.0722 * 7.0 = 6.8; the whole-tablet photo read 7.4, so the photo tool overstates by about 9 % (camera gain near 1.1): good enough as a before / after check (after v0.8.2 it should read about 0).
* **Expected effect:** the patch's open look is now the measured transform (`setOpenLook`, scale 0.80 and +6 / +7 / +7), interpolated from identity along the stock fade, so at the end of the open it should match the lock screen around it to within rounding (about 0.5 level). Waiting for Tj's eyes / a straight photo (TASKS BA4c).
