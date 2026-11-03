# CHECKPOINT 169 — read me first, then TASKS.md

**Written:** 2026-11-03T20:35:42Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `60c81ad` (this checkpoint is the commit after it)

## Just done
v0.3.3 gated (code 9) and pushed (60c81ad on main); android.yml full_build dispatched on main. Emulator run 31: 40 PASS, 1 FAIL = runner lacked ImageMagick (W3b compare); local PIL compare of the same screenshots = 0.0 diff; workflow now installs ImageMagick.

## Do this next
Wait ~4 min, then get_release_by_tag v0.3.3; when the Build APK run is green run: bash tools/record-release.sh v0.3.3 "<note>"; give Tj the Release link as plain text; remind: grant adb (optional), toggle helper off/on, tap Allow wallpaper access + open power menu once for wallpaper mode, send a new lock-screen screenshot for the number

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  60c81ad ckpt 168: pre-ship: v0.3.3: power-menu cover on the lock screen (Y1), faster double-Home
  66aa558 ckpt 167: Z1b: Tj's ORIGINAL leaf wallpaper (960x853, no number) replaces the AI-edited 
  46b7aa0 ckpt 166: Z1 closed: Tj found the original wallpaper himself
  0b3fc64 ckpt 165: A2: measured the correct number's opacity per pixel against the wallpaper abov
  f65af65 ckpt 164: Z1: WebSearch found no exact match; lead = Huawei P10 stock lockscreen leaf wa
  81ae879 ckpt 163: Y3b: lock-number ground truth from Tj's 7.png (192x18 at x205-396,y907-924, ~7
  62e14a1 ckpt 162: Recorded Z1 (find original leaf wallpaper)
  13bf78d ckpt 161: Bumped to 0.3.3 (code 9) + FIRST_INSTALL notes; emulator run dispatched with Y
  053ec79 ckpt 160: Run 29: W3b patch window present at the learned rect, pixel diff vs wallpaper-
  842bea1 ckpt 159: CORRECTION: the previous ckpt claimed Y1/Y2/Y3 emulator checks that had NOT be
```
