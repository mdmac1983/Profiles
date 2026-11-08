# CHECKPOINT 675 — read me first, then TASKS.md

**Written:** 2026-11-08T05:02:52Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2r9ji2` · **builds on:** `41ba772` (this checkpoint is the commit after it)

## Just done
BI1c: takeover; hp run #346 (c69041b) = 18 PASS 5 FAIL, phase C never rested after restart; root cause = wallpaperX 0.5 default then 0 report, no re-evaluation; fix in onWallpaperOffset + maybeHomeCalibrate, HP C info line; review fixes kept (compile)

## Do this next
wait for the background gradle test (HomePatchRulesTest + NotifSightingsTest) in scratchpad/gradle-hp.log (GRADLE_EXIT=0); then dispatch emulator.yml sections=hp alone on ref main (read RESULTS tail ~80; INFO HP C line first); then /code-review high on diff vs v0.8.8, full unit suite + lint, tiered gate, ship v0.8.9

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  c69041b ckpt 674: BI1c: emulator section hp written (phases A-E: self-calibration, invisible res
  4b6180c ckpt 673: BI1b step 5: Launcher control rows (switch + status + Calibrate now + Forget) 
  6005aa2 ckpt 672: BI1b steps 2-4 (compiles, nothing tested on the emulator yet): HomeShot + Home
```

(30 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
