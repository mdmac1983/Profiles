# CHECKPOINT 157 — read me first, then TASKS.md

**Written:** 2026-11-03T20:02:15Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `3f8d847` (this checkpoint is the commit after it)

## Just done
Y3 (number: size x1.063, Light weight 300, +1px x/y) and Y1 (cover on lock screen; BLOCK stays off it) coded; tests+build green. W3b emulator run 29 pending.

## Do this next
Y2: write AnimationSpeedup (animations off during reset, needs WRITE_SECURE_SETTINGS, restore failsafes), status-bar notification detection to skip the shade, shorter Clear-all timeout; then read run 29 artifact (patch-diff.txt); then emulator run with Y1/Y3 checks; ship v0.3.3

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  283857f ckpt 156: Recorded Y1 (cover on lock screen), Y2 (faster reset, skip empty shade), Y3 (n
  244acdd ckpt 155: W3b built: power mode 'wallpaper' = permanent wallpaper patch (WallpaperManage
  bfaf6a4 ckpt 154: Recorded W3b (permanent wallpaper-patch overlay over the power-menu area in cu
  57fd564 ckpt 153: Tj set the GitHub default branch to main (confirmed: git remote show origin ->
  d936fd2 ckpt 152: RELEASED v0.3.2 (run 41582445432 green, Release published, signed, 8.96 MB). E
  975b225 ckpt 151: gated v0.3.2 (code 8) and pushed it: the handoff self-test, the full unit suit
  52cd2ad ckpt 150: pre-ship: v0.3.2: Quickstep lock-out (W5), Recents runtime exclude (W2), faste
  7dfa7d6 ckpt 149: Run 27 (54a3b53): emulator dies in reset2 again (same spot as run 26, so repro
  54a3b53 ckpt 148: Run 26 (382895b): emulator device vanished after reset2 (adb: device not found
  382895b ckpt 147: Run 25: V4 PASS, W5 3/3 PASS, W2 PASS; reset2 Recents still TIMEOUT. Suspect: 
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
