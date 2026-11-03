# CHECKPOINT 163 — read me first, then TASKS.md

**Written:** 2026-11-03T20:16:14Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `5f480a1` (this checkpoint is the commit after it)

## Just done
Y3b: lock-number ground truth from Tj's 7.png (192x18 at x205-396,y907-924, ~71% white, Light): EM 0.0429, baseline 0.9033, ALPHA 181, Light, x+1px. Tests+build green. v0.3.3 emulator run (older code for Y3) is still pending; Z1 wallpaper search not started yet.

## Do this next
Read emulator run (Y1/Y2/W3b results), then WebSearch for the original green leaf wallpaper (Z1) and send it; then rebuild, push, re-run the emulator (the number change is small), ship v0.3.3

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  TASKS.md
    M  app/src/main/java/com/tj/launcher/LockNumber.java
    M  app/src/main/java/com/tj/launcher/TjAccessibilityService.java
    M  app/src/test/java/com/tj/launcher/LockNumberTest.java

## Last ten checkpoints
```
  62e14a1 ckpt 162: Recorded Z1 (find original leaf wallpaper)
  13bf78d ckpt 161: Bumped to 0.3.3 (code 9) + FIRST_INSTALL notes; emulator run dispatched with Y
  053ec79 ckpt 160: Run 29: W3b patch window present at the learned rect, pixel diff vs wallpaper-
  842bea1 ckpt 159: CORRECTION: the previous ckpt claimed Y1/Y2/Y3 emulator checks that had NOT be
  1306cf7 ckpt 158: Y2 built: AnimationSpeedup (animations off during reset, 4 restore failsafes),
  4305b7b ckpt 157: Y3 (number: size x1.063, Light weight 300, +1px x/y) and Y1 (cover on lock scr
  283857f ckpt 156: Recorded Y1 (cover on lock screen), Y2 (faster reset, skip empty shade), Y3 (n
  244acdd ckpt 155: W3b built: power mode 'wallpaper' = permanent wallpaper patch (WallpaperManage
  bfaf6a4 ckpt 154: Recorded W3b (permanent wallpaper-patch overlay over the power-menu area in cu
  57fd564 ckpt 153: Tj set the GitHub default branch to main (confirmed: git remote show origin ->
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
