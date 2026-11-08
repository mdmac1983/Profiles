# CHECKPOINT 677 — read me first, then TASKS.md

**Written:** 2026-11-08T05:32:34Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2r9ji2` · **builds on:** `4672baa` (this checkpoint is the commit after it)

## Just done
BI1c: hp run #348 (f78001e, id 42450536427) = 21 PASS 2 FAIL: only HP7 (no draw seen after long press). Audit of patchLockMode on home flavour (Explore subagent, 44 uses): fixed lock-log flooding (logPatch), lock-calibration completion race (discard when unlocked/home), pinPadCameUp on home (could expose the Screenshot entry); added debug draw-burst log, HP11 shade check + HP12 volume data, HP7 drawer open/close as a deterministic draw

## Do this next
dispatch emulator.yml sections=hp alone on main; read RESULTS tail ~40: HP7 (stale/built + 'draw bursts seen' + HP7 log), HP11 shade (away line + patch back), HP12 volume (INFO); if draws are never seen the listener is the bug (TjHooks.watchHomeDraw); then bash tools/gate.sh b1399c9 (3 parallel runs), ship v0.8.9

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  f78001e ckpt 676: BI1c: hp run #347 (4a34b83) = 19 PASS 4 FAIL: phase C FIXED (rested after rest
  4a34b83 ckpt 675: BI1c: takeover; hp run #346 (c69041b) = 18 PASS 5 FAIL, phase C never rested a
  c69041b ckpt 674: BI1c: emulator section hp written (phases A-E: self-calibration, invisible res
  4b6180c ckpt 673: BI1b step 5: Launcher control rows (switch + status + Calibrate now + Forget) 
  6005aa2 ckpt 672: BI1b steps 2-4 (compiles, nothing tested on the emulator yet): HomeShot + Home
```

(7 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
