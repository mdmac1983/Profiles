# CHECKPOINT 160 — read me first, then TASKS.md

**Written:** 2026-11-03T20:08:51Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `842bea1` (this checkpoint is the commit after it)

## Just done
Run 29: W3b patch window present at the learned rect, pixel diff vs wallpaper-only = 0.0 (local PIL compare of 50-patch-on vs 51-patch-off; shifting by 2px gives 0.07, so alignment is proven for y/scale; x offset not exercised by a vertical-gradient wallpaper). Scenario: fixed the compare command (-extract), added control (patch gone when mode off).

## Do this next
Dispatch emulator.yml (Y1 lock cover, Y2 anim/reset, Y3 info, W3b control); then bump 0.3.3 (code 9), FIRST_INSTALL, TASKS ticks, ship.sh, android.yml full_build=true, record-release

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  842bea1 ckpt 159: CORRECTION: the previous ckpt claimed Y1/Y2/Y3 emulator checks that had NOT be
  1306cf7 ckpt 158: Y2 built: AnimationSpeedup (animations off during reset, 4 restore failsafes),
  4305b7b ckpt 157: Y3 (number: size x1.063, Light weight 300, +1px x/y) and Y1 (cover on lock scr
  283857f ckpt 156: Recorded Y1 (cover on lock screen), Y2 (faster reset, skip empty shade), Y3 (n
  244acdd ckpt 155: W3b built: power mode 'wallpaper' = permanent wallpaper patch (WallpaperManage
  bfaf6a4 ckpt 154: Recorded W3b (permanent wallpaper-patch overlay over the power-menu area in cu
  57fd564 ckpt 153: Tj set the GitHub default branch to main (confirmed: git remote show origin ->
  d936fd2 ckpt 152: RELEASED v0.3.2 (run 41582445432 green, Release published, signed, 8.96 MB). E
  975b225 ckpt 151: gated v0.3.2 (code 8) and pushed it: the handoff self-test, the full unit suit
  52cd2ad ckpt 150: pre-ship: v0.3.2: Quickstep lock-out (W5), Recents runtime exclude (W2), faste
```
