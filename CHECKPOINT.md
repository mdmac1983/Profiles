# CHECKPOINT 593 — read me first, then TASKS.md

**Written:** 2026-11-07T06:47:16Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `69c97d6` (this checkpoint is the commit after it)

## Just done
gated v0.8.3 (code 28) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.8.3 "v0.8.3: the lock-screen patch now covers the stock card's shadow (no faint line under the hidden card); the stock-menu dimming is measured once per theme, so battery saver no longer causes repeated screenshot flashes; Launcher control says why a measurement started"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  c517c03 ckpt 592: HANDOFF.md 0g refreshed for v0.8.3 + old handoff-launcher.md marked superseded
  fa034b7 ckpt 591: Tj confirmed: it was the camera flashes (BB3 reading a); BB3a per-theme slots 
  4b6dd77 ckpt 590: dispatched on 94671f9 (06:36Z): #291 id 42347732366 and #292 id 42347734404 (p
  94671f9 ckpt 589: final-head runs: #289/#290 green (n,x,ac,rb + base,x1,e1,o,last: AN1 did not r
  89fbc42 ckpt 588: gate runs on 84b3dd1: #286 (e1,o,ae,pa,rb,last) 37 PASS + emulator went offlin
  aaf3b24 ckpt 587: six gate runs in flight: ON 84b3dd1 (06:10Z) #285 id 42345818982, #286 id 4234
  6900074 ckpt 586: BB2 (Tj: 'the lock screen screenshot itself again, not sure why'): the helper 
  35f1518 ckpt 585: dispatched the 3 gate runs on 84b3dd1 at 06:10Z: #285 id 42345818982, #286 id 
  84b3dd1 ckpt 584: BB1b: defaults chosen (core +24 dp, bottom fringe 4 dp), PatchRestTest 24 gree
  f0c3778 ckpt 583: dispatched emulator.yml pt alone: run #284 id 42344459448 on b20679f (05:51Z) 
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
