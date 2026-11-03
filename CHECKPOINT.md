# CHECKPOINT 155 — read me first, then TASKS.md

**Written:** 2026-11-03T19:50:20Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `6ccf08e` (this checkpoint is the commit after it)

## Just done
W3b built: power mode 'wallpaper' = permanent wallpaper patch (WallpaperManager.getDrawable, needs READ_EXTERNAL_STORAGE grant) under the pre-built card, per-rotation menu rect, launcher offset hook (WallpaperOffsetInterpolator patch), never on lock screen, status line in Control. Unit tests + debug build green. Emulator pixel-diff check added (compare MAE < 0.5%).

## Do this next
Dispatch emulator.yml; read patch-diff.txt + 50-patch-on.png vs 51-patch-off.png. If MAE is high: calibrate the scaling/offset in TjAccessibilityService.addPatch (cover scale, x offset = TjHooks.wallpaperX, y 0.5). Only then ship (opt-in mode, default OFF)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  bfaf6a4 ckpt 154: Recorded W3b (permanent wallpaper-patch overlay over the power-menu area in cu
  57fd564 ckpt 153: Tj set the GitHub default branch to main (confirmed: git remote show origin ->
  d936fd2 ckpt 152: RELEASED v0.3.2 (run 41582445432 green, Release published, signed, 8.96 MB). E
  975b225 ckpt 151: gated v0.3.2 (code 8) and pushed it: the handoff self-test, the full unit suit
  52cd2ad ckpt 150: pre-ship: v0.3.2: Quickstep lock-out (W5), Recents runtime exclude (W2), faste
  7dfa7d6 ckpt 149: Run 27 (54a3b53): emulator dies in reset2 again (same spot as run 26, so repro
  54a3b53 ckpt 148: Run 26 (382895b): emulator device vanished after reset2 (adb: device not found
  382895b ckpt 147: Run 25: V4 PASS, W5 3/3 PASS, W2 PASS; reset2 Recents still TIMEOUT. Suspect: 
  bf3405d ckpt 146: Emulator scenario: X1 lock-screen number checks (PIN set/cleared)
  33b0b04 ckpt 145: X1 built: LockNumber overlay (a11y, NOT_TOUCHABLE) shows 71125011907547 at mea
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
