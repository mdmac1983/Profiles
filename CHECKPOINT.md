# CHECKPOINT 590 — read me first, then TASKS.md

**Written:** 2026-11-07T06:36:30Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `94671f9` (this checkpoint is the commit after it)

## Just done
dispatched on 94671f9 (06:36Z): #291 id 42347732366 and #292 id 42347734404 (pt alone / base,x1,v,ac,ps: order unknown, the INFO sections line says); #285 cancelled (obsolete)

## Do this next
read both ~06:51Z (list_workflow_jobs latest; get_job_logs tail 60-100; 404 = running): pt: PB reach/interior checks + PT/BB2 lines (dark attempt: did the helper see the dark theme? slots?) ; base,x1,v,ac,ps: V5 + AN1 (INFO line names the guard line if it recurs); all green -> bump app/build.gradle versionName 0.8.3 / versionCode 28 (gated head + the 2 version lines), bash ship.sh, android.yml full_build on main, get_release_by_tag v0.8.3, tools/record-release.sh, tell Tj; Tj has not answered which he saw (flashes or Screenshot button)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  94671f9 ckpt 589: final-head runs: #289/#290 green (n,x,ac,rb + base,x1,e1,o,last: AN1 did not r
  89fbc42 ckpt 588: gate runs on 84b3dd1: #286 (e1,o,ae,pa,rb,last) 37 PASS + emulator went offlin
  aaf3b24 ckpt 587: six gate runs in flight: ON 84b3dd1 (06:10Z) #285 id 42345818982, #286 id 4234
  6900074 ckpt 586: BB2 (Tj: 'the lock screen screenshot itself again, not sure why'): the helper 
  35f1518 ckpt 585: dispatched the 3 gate runs on 84b3dd1 at 06:10Z: #285 id 42345818982, #286 id 
  84b3dd1 ckpt 584: BB1b: defaults chosen (core +24 dp, bottom fringe 4 dp), PatchRestTest 24 gree
  f0c3778 ckpt 583: dispatched emulator.yml pt alone: run #284 id 42344459448 on b20679f (05:51Z) 
  b20679f ckpt 582: run #283 read: probe works (cores end 2/12/22 px below the panel as designed),
  31f893d ckpt 581: dispatched emulator.yml pt alone: run #283 id 42343234753 on aef44d4 (05:33Z),
  aef44d4 ckpt 580: BB1b step 1: patch core extension coded (PatchRest.slotWindow extra + clampExt
```
