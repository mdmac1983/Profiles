# CHECKPOINT 154 — read me first, then TASKS.md

**Written:** 2026-11-03T19:45:03Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `51858a6` (this checkpoint is the commit after it)

## Just done
Recorded W3b (permanent wallpaper-patch overlay over the power-menu area in custom mode, custom card on top on long press)

## Do this next
Research: how Launcher3 sets wallpaper offsets (grep setWallpaperOffsets in launcher3/src), how ImageWallpaper of Android 10 scales/crops (cover scale, x offset), WallpaperManager.getDrawable needs READ_EXTERNAL_STORAGE; then build WallpaperPatch in TjAccessibilityService as new power mode 'wallpaper'; emulator pixel-diff test

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  TASKS.md

## Last ten checkpoints
```
  57fd564 ckpt 153: Tj set the GitHub default branch to main (confirmed: git remote show origin ->
  d936fd2 ckpt 152: RELEASED v0.3.2 (run 41582445432 green, Release published, signed, 8.96 MB). E
  975b225 ckpt 151: gated v0.3.2 (code 8) and pushed it: the handoff self-test, the full unit suit
  52cd2ad ckpt 150: pre-ship: v0.3.2: Quickstep lock-out (W5), Recents runtime exclude (W2), faste
  7dfa7d6 ckpt 149: Run 27 (54a3b53): emulator dies in reset2 again (same spot as run 26, so repro
  54a3b53 ckpt 148: Run 26 (382895b): emulator device vanished after reset2 (adb: device not found
  382895b ckpt 147: Run 25: V4 PASS, W5 3/3 PASS, W2 PASS; reset2 Recents still TIMEOUT. Suspect: 
  bf3405d ckpt 146: Emulator scenario: X1 lock-screen number checks (PIN set/cleared)
  33b0b04 ckpt 145: X1 built: LockNumber overlay (a11y, NOT_TOUCHABLE) shows 71125011907547 at mea
  9ff7be8 ckpt 144: Recorded X1 (lock-screen number) with Tj's explicit L10 relaxation; photo save
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
