# CHECKPOINT 279 — read me first, then TASKS.md

**Written:** 2026-11-04T22:54:33Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-8b1c3755-1tp147` · **builds on:** `a97e52a` (this checkpoint is the commit after it)

## Just done
Runs 72 (v) and 73 (g) read from job logs (artifact curl is blocked by the simple-workflow hook): scenario bugs fixed (vfeat only defined in section x; CARD parsers expected Rect() but app logs [l,t][r,b]; section v never set the skew; Y2 now scrolls page by page); PatchCalibrator.lastNote logs why a calibration is kept (spread / best error). NOT yet re-run.

## Do this next
dispatch emulator.yml sections=v and sections=g on branch ccr-8b1c3755-1tp147 (parallel); read results via get_job_logs (job logs show PASS/FAIL/INFO). If V4 still says 'kept', read the [spread ... best error ...] note in the CAL line and fix PatchCalibrator (brightness-normalised compare if the lock screen is dimmed). Then bump 0.6.0/16, ship.sh, android.yml full_build=true, record-release

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  445eb1a ckpt 278: CUT-OFF SAVE (usage nearly out): HANDOFF.md section 0b written with exact stat
  80e19d2 ckpt 277: HANDOFF.md: phone guide row, Tj's next step, open requests list; ADB_PHONE.txt
  f2fdff4 ckpt 276: Run 71 (v): card area OK ([485,357][596,511] = 2 stock items vs full rect 303 
  498ad58 ckpt 275: Tj asked for a PHONE-side adb guide app (module phoneguide) with copy buttons,
  2d6e5f9 ckpt 274: V3 (patch = learned rect + 12dp margin, card = top two items), V4 (PatchCalibr
  aa72200 ckpt 273: Tj: patch must cover the whole 3-button menu, card = top two buttons only; pat
  e9b3365 ckpt 272: HANDOFF.md written (single entry point for a no-context Claude account; linked
  953311b ckpt 271: fixed section v helper (vfeat sets lock_patch + power_mode)
  45e6c46 ckpt 270: V1+V2+U1+U2+W1+W2 coded: lock-screen wallpaper patch (invisible until menu ope
  e5c1581 ckpt 269: gated v0.5.0 (code 15) and pushed it: the handoff self-test, the full unit sui
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
