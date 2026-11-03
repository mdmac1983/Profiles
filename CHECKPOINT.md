# CHECKPOINT 164 — read me first, then TASKS.md

**Written:** 2026-11-03T20:17:11Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `81ae879` (this checkpoint is the commit after it)

## Just done
Z1: WebSearch found no exact match; lead = Huawei P10 stock lockscreen leaf wallpaper (peakpx.com/en/hd-wallpaper-desktop-keigo) but peakpx.com is blocked by the session network policy (ENOTFOUND). Emulator re-run dispatched on the final lock-number code.

## Do this next
Wait for the emulator run (artifact: results.txt, y2-log.txt, lock-menu-log.txt, lock-number-bbox.txt, patch-diff.txt); fix FAILs; tick TASKS Y1,Y2,Y3,Y3b,W3b; ship.sh; android.yml full_build=true; record-release. Tell Tj: allow peakpx.com (and wallpapers.com / unsplash) to continue Z1, or send the original wallpaper file from the tablet

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  81ae879 ckpt 163: Y3b: lock-number ground truth from Tj's 7.png (192x18 at x205-396,y907-924, ~7
  62e14a1 ckpt 162: Recorded Z1 (find original leaf wallpaper)
  13bf78d ckpt 161: Bumped to 0.3.3 (code 9) + FIRST_INSTALL notes; emulator run dispatched with Y
  053ec79 ckpt 160: Run 29: W3b patch window present at the learned rect, pixel diff vs wallpaper-
  842bea1 ckpt 159: CORRECTION: the previous ckpt claimed Y1/Y2/Y3 emulator checks that had NOT be
  1306cf7 ckpt 158: Y2 built: AnimationSpeedup (animations off during reset, 4 restore failsafes),
  4305b7b ckpt 157: Y3 (number: size x1.063, Light weight 300, +1px x/y) and Y1 (cover on lock scr
  283857f ckpt 156: Recorded Y1 (cover on lock screen), Y2 (faster reset, skip empty shade), Y3 (n
  244acdd ckpt 155: W3b built: power mode 'wallpaper' = permanent wallpaper patch (WallpaperManage
  bfaf6a4 ckpt 154: Recorded W3b (permanent wallpaper-patch overlay over the power-menu area in cu
```
