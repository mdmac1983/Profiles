# CHECKPOINT 591 — read me first, then TASKS.md

**Written:** 2026-11-07T06:43:05Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `cfe6d9a` (this checkpoint is the commit after it)

## Just done
Tj confirmed: it was the camera flashes (BB3 reading a); BB3a per-theme slots coded + unit tested; waiting on #291 id 42347732366 / #292 id 42347734404 (pt / base,x1,v,ac,ps on 94671f9); Tj says my context is 90% used -> wrap up

## Do this next
RESUME (cold start): 1) list_workflow_jobs latest on runs 42347732366 and 42347734404 (success = green; on failure read get_job_logs tail ~100, or delegate the reading to a subagent: logs are huge); pt must pass the PB reach/interior checks and the PT/BB2 checks (dark flip may be skipped with an INFO line); base,x1,v,ac,ps must pass V5 (now scrim-tolerant) and AN1 (flake; INFO names the line if it recurs). 2) all green -> edit app/build.gradle versionName 0.8.3 / versionCode 28, bash ship.sh 'v0.8.3: ...', android.yml run_workflow ref main full_build=true, get_release_by_tag v0.8.3, bash tools/record-release.sh v0.8.3 '<note>', refresh HANDOFF.md 0g, tell Tj (what is new: the patch covers the stock card's shadow = no faint line; the tablet measures once per THEME so battery saver no longer causes camera flashes; Launcher control now says why a measurement started; link on its own line). 3) then BB1 stays open until Tj looks at the line again + one straight photo (tools/patch_photo_step.py)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  4b6dd77 ckpt 590: dispatched on 94671f9 (06:36Z): #291 id 42347732366 and #292 id 42347734404 (p
  94671f9 ckpt 589: final-head runs: #289/#290 green (n,x,ac,rb + base,x1,e1,o,last: AN1 did not r
  89fbc42 ckpt 588: gate runs on 84b3dd1: #286 (e1,o,ae,pa,rb,last) 37 PASS + emulator went offlin
  aaf3b24 ckpt 587: six gate runs in flight: ON 84b3dd1 (06:10Z) #285 id 42345818982, #286 id 4234
  6900074 ckpt 586: BB2 (Tj: 'the lock screen screenshot itself again, not sure why'): the helper 
  35f1518 ckpt 585: dispatched the 3 gate runs on 84b3dd1 at 06:10Z: #285 id 42345818982, #286 id 
  84b3dd1 ckpt 584: BB1b: defaults chosen (core +24 dp, bottom fringe 4 dp), PatchRestTest 24 gree
  f0c3778 ckpt 583: dispatched emulator.yml pt alone: run #284 id 42344459448 on b20679f (05:51Z) 
  b20679f ckpt 582: run #283 read: probe works (cores end 2/12/22 px below the panel as designed),
  31f893d ckpt 581: dispatched emulator.yml pt alone: run #283 id 42343234753 on aef44d4 (05:33Z),
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
