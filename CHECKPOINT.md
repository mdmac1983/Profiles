# CHECKPOINT 589 — read me first, then TASKS.md

**Written:** 2026-11-07T06:35:51Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `55e6a82` (this checkpoint is the commit after it)

## Just done
final-head runs: #289/#290 green (n,x,ac,rb + base,x1,e1,o,last: AN1 did not recur = flake, rb offline in #286 = infra); #288 pt FAILED only in MY new checks: PBSIM confused by the emulator's Emergency card (4th entry, bounds [502,713][579,731]; reach 3 -> 28 rows, interior +0.02 = geometry proven), dark flip via 'cmd uimode night yes' does nothing on this emulator; now: prediction check only with a clean edge, flip via battery saver too, detection through the helper's note; V5 tolerance grows with the scrim gap

## Do this next
dispatch on the new head: pt alone and base,x1,v,ac,ps; read PB / BB2 lines + V5/AN1; #285 (n,pt on 84b3dd1) is obsolete: cancel it if still running; all green -> bump 0.8.3 code 28, ship.sh, android.yml full_build, record-release, tell Tj; Tj still has not said which he saw (flashes or the Screenshot button)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  89fbc42 ckpt 588: gate runs on 84b3dd1: #286 (e1,o,ae,pa,rb,last) 37 PASS + emulator went offlin
  aaf3b24 ckpt 587: six gate runs in flight: ON 84b3dd1 (06:10Z) #285 id 42345818982, #286 id 4234
  6900074 ckpt 586: BB2 (Tj: 'the lock screen screenshot itself again, not sure why'): the helper 
  35f1518 ckpt 585: dispatched the 3 gate runs on 84b3dd1 at 06:10Z: #285 id 42345818982, #286 id 
  84b3dd1 ckpt 584: BB1b: defaults chosen (core +24 dp, bottom fringe 4 dp), PatchRestTest 24 gree
  f0c3778 ckpt 583: dispatched emulator.yml pt alone: run #284 id 42344459448 on b20679f (05:51Z) 
  b20679f ckpt 582: run #283 read: probe works (cores end 2/12/22 px below the panel as designed),
  31f893d ckpt 581: dispatched emulator.yml pt alone: run #283 id 42343234753 on aef44d4 (05:33Z),
  aef44d4 ckpt 580: BB1b step 1: patch core extension coded (PatchRest.slotWindow extra + clampExt
  9b150a1 ckpt 579: BB1 diagnosed from Tj's 2 new v0.8.2 photos: patch interior now right (left ed
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
