# CHECKPOINT 280 — read me first, then TASKS.md

**Written:** 2026-11-04T23:02:53Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-8b1c3755-1tp147` · **builds on:** `0e360a3` (this checkpoint is the commit after it)

## Just done
Run 74 (v): U1/U2/V1/V2-closed PASS; g (run 75) PASS. V4/V2-freed FAIL because the real keyguard dims the wallpaper ~23 % (screenshot (173,96,20) vs wallpaper (229,122,26)) so no wallpaper model can match. New design: the lock patch is a PHOTO (CAL_SHOT screenshot cropped to the patch rect, re-taken each lock session); model is the fallback. Scenario V4 check now greps 'CAL lock patch PHOTO'; ring-MAE info line added. Compiles; not yet run on the emulator.

## Do this next
dispatch emulator.yml sections=v on branch ccr-8b1c3755-1tp147; read via get_job_logs tail_lines=135: expect 'V4 ... PHOTO', 'V2 freed slot MAE < 0.005', ring MAE info (0 = menu does not dim surroundings; if > 0 the open menu has a backdrop dim the photo must be darkened by). Then bump 0.6.0/16, ship.sh, android.yml full_build=true, record-release

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  e91b6a5 ckpt 279: Runs 72 (v) and 73 (g) read from job logs (artifact curl is blocked by the sim
  445eb1a ckpt 278: CUT-OFF SAVE (usage nearly out): HANDOFF.md section 0b written with exact stat
  80e19d2 ckpt 277: HANDOFF.md: phone guide row, Tj's next step, open requests list; ADB_PHONE.txt
  f2fdff4 ckpt 276: Run 71 (v): card area OK ([485,357][596,511] = 2 stock items vs full rect 303 
  498ad58 ckpt 275: Tj asked for a PHONE-side adb guide app (module phoneguide) with copy buttons,
  2d6e5f9 ckpt 274: V3 (patch = learned rect + 12dp margin, card = top two items), V4 (PatchCalibr
  aa72200 ckpt 273: Tj: patch must cover the whole 3-button menu, card = top two buttons only; pat
  e9b3365 ckpt 272: HANDOFF.md written (single entry point for a no-context Claude account; linked
  953311b ckpt 271: fixed section v helper (vfeat sets lock_patch + power_mode)
  45e6c46 ckpt 270: V1+V2+U1+U2+W1+W2 coded: lock-screen wallpaper patch (invisible until menu ope
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
