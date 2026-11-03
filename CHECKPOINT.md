# CHECKPOINT 158 — read me first, then TASKS.md

**Written:** 2026-11-03T20:05:13Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `810b13f` (this checkpoint is the commit after it)

## Just done
Y2 built: AnimationSpeedup (animations off during reset, 4 restore failsafes), status-bar notification skip, shorter polls/waits; emulator scenario has Y1 (cover on lock screen), Y2 (anim off/restore, reset), Y3 (number box info) checks. Unit tests + build green.

## Do this next
Read run 29 artifact (W3b patch-diff), then dispatch a new emulator run with Y1/Y2/Y3 checks, fix, bump to 0.3.3 (versionCode 9) + FIRST_INSTALL notes, tick TASKS Y1-Y3/W3b, ship.sh, android.yml full_build=true, record-release

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  4305b7b ckpt 157: Y3 (number: size x1.063, Light weight 300, +1px x/y) and Y1 (cover on lock scr
  283857f ckpt 156: Recorded Y1 (cover on lock screen), Y2 (faster reset, skip empty shade), Y3 (n
  244acdd ckpt 155: W3b built: power mode 'wallpaper' = permanent wallpaper patch (WallpaperManage
  bfaf6a4 ckpt 154: Recorded W3b (permanent wallpaper-patch overlay over the power-menu area in cu
  57fd564 ckpt 153: Tj set the GitHub default branch to main (confirmed: git remote show origin ->
  d936fd2 ckpt 152: RELEASED v0.3.2 (run 41582445432 green, Release published, signed, 8.96 MB). E
  975b225 ckpt 151: gated v0.3.2 (code 8) and pushed it: the handoff self-test, the full unit suit
  52cd2ad ckpt 150: pre-ship: v0.3.2: Quickstep lock-out (W5), Recents runtime exclude (W2), faste
  7dfa7d6 ckpt 149: Run 27 (54a3b53): emulator dies in reset2 again (same spot as run 26, so repro
  54a3b53 ckpt 148: Run 26 (382895b): emulator device vanished after reset2 (adb: device not found
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
