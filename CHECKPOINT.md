# CHECKPOINT 676 — read me first, then TASKS.md

**Written:** 2026-11-08T05:22:28Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2r9ji2` · **builds on:** `e7820a5` (this checkpoint is the commit after it)

## Just done
BI1c: hp run #347 (4a34b83) = 19 PASS 4 FAIL: phase C FIXED (rested after restart, HP5/HP6 pass); HP4+HP7 failed because homeForeignWindow took nameless WINDOWS_CHANGED as foreign (cause: log 'fast skip: patch not resting'). /code-review high b1399c9..HEAD = 10 findings, verified+fixed: foreign window named-only, lock calibration guard on home, undecodable-crop loop, shotIsOurs 1 s, quietBurst honours open-menu deferral, draw listener flag, wallpaperReported + interpolator first report, teardown ends calibration, homeDrew rebuild gap; full unit suite 702 + lint green; checklist v0.8.9 written

## Do this next
dispatch emulator.yml sections=hp alone on main (RESULTS tail ~42; HP4/HP7 + the new INFO lines HP C and HP7 log); an Explore subagent audits patchLockMode uses on the home flavour (result arrives as a notification: fix what it finds, rerun hp); then tiered gate bash tools/gate.sh b1399c9 (3 parallel runs ~29 min), ship v0.8.9 (research/TABLET_CHECKLIST_v0.8.9.md exists)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  4a34b83 ckpt 675: BI1c: takeover; hp run #346 (c69041b) = 18 PASS 5 FAIL, phase C never rested a
  c69041b ckpt 674: BI1c: emulator section hp written (phases A-E: self-calibration, invisible res
  4b6180c ckpt 673: BI1b step 5: Launcher control rows (switch + status + Calibrate now + Forget) 
  6005aa2 ckpt 672: BI1b steps 2-4 (compiles, nothing tested on the emulator yet): HomeShot + Home
```

(28 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
