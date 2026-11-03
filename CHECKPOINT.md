# CHECKPOINT 167 — read me first, then TASKS.md

**Written:** 2026-11-03T20:31:39Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `4bcc516` (this checkpoint is the commit after it)

## Just done
Z1b: Tj's ORIGINAL leaf wallpaper (960x853, no number) replaces the AI-edited one in the APK; Control button text updated; tests+build green

## Do this next
Read emulator run 31 results (artifact of run 41711142486): Y1 lock cover, Y2 anim/reset, Y3 info, W3b; fix FAILs; then ship v0.3.3 (ship.sh, android.yml full_build=true, record-release)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  46b7aa0 ckpt 166: Z1 closed: Tj found the original wallpaper himself
  0b3fc64 ckpt 165: A2: measured the correct number's opacity per pixel against the wallpaper abov
  f65af65 ckpt 164: Z1: WebSearch found no exact match; lead = Huawei P10 stock lockscreen leaf wa
  81ae879 ckpt 163: Y3b: lock-number ground truth from Tj's 7.png (192x18 at x205-396,y907-924, ~7
  62e14a1 ckpt 162: Recorded Z1 (find original leaf wallpaper)
  13bf78d ckpt 161: Bumped to 0.3.3 (code 9) + FIRST_INSTALL notes; emulator run dispatched with Y
  053ec79 ckpt 160: Run 29: W3b patch window present at the learned rect, pixel diff vs wallpaper-
  842bea1 ckpt 159: CORRECTION: the previous ckpt claimed Y1/Y2/Y3 emulator checks that had NOT be
  1306cf7 ckpt 158: Y2 built: AnimationSpeedup (animations off during reset, 4 restore failsafes),
  4305b7b ckpt 157: Y3 (number: size x1.063, Light weight 300, +1px x/y) and Y1 (cover on lock scr
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
