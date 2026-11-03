# CHECKPOINT 156 — read me first, then TASKS.md

**Written:** 2026-11-03T19:57:21Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `9bf623c` (this checkpoint is the commit after it)

## Just done
Recorded Y1 (cover on lock screen), Y2 (faster reset, skip empty shade), Y3 (number identical to 5.png); both screenshots saved in research/uploads/2026-11-03

## Do this next
Y3 first: measure digits in 5-reference... vs 6-tablet-lockscreen (stroke, height, width, alpha, font); then Y1 (allow cover on lock screen in PowerMenuRule/earlyCover/warmUp), then Y2; check emulator run 29 (W3b patch pixel diff) results too

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  TASKS.md
    A  research/uploads/2026-11-03/5-reference-wallpaper-with-old-number.png
    A  research/uploads/2026-11-03/6-tablet-lockscreen-v0.3.2.png

## Last ten checkpoints
```
  244acdd ckpt 155: W3b built: power mode 'wallpaper' = permanent wallpaper patch (WallpaperManage
  bfaf6a4 ckpt 154: Recorded W3b (permanent wallpaper-patch overlay over the power-menu area in cu
  57fd564 ckpt 153: Tj set the GitHub default branch to main (confirmed: git remote show origin ->
  d936fd2 ckpt 152: RELEASED v0.3.2 (run 41582445432 green, Release published, signed, 8.96 MB). E
  975b225 ckpt 151: gated v0.3.2 (code 8) and pushed it: the handoff self-test, the full unit suit
  52cd2ad ckpt 150: pre-ship: v0.3.2: Quickstep lock-out (W5), Recents runtime exclude (W2), faste
  7dfa7d6 ckpt 149: Run 27 (54a3b53): emulator dies in reset2 again (same spot as run 26, so repro
  54a3b53 ckpt 148: Run 26 (382895b): emulator device vanished after reset2 (adb: device not found
  382895b ckpt 147: Run 25: V4 PASS, W5 3/3 PASS, W2 PASS; reset2 Recents still TIMEOUT. Suspect: 
  bf3405d ckpt 146: Emulator scenario: X1 lock-screen number checks (PIN set/cleared)
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
