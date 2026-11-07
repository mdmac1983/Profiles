# CHECKPOINT 598 — read me first, then TASKS.md

**Written:** 2026-11-07T07:19:33Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `95ccfd5` (this checkpoint is the commit after it)

## Just done
android.yml full build RUNNING: run 30 id 42350762180 on main bf6a931 (v0.8.3 code 28), started 07:18:58 UTC at Tj's word 'Build it now'

## Do this next
read run 42350762180 (list_workflow_jobs latest; get_job_logs tail 60 only after it ended, 404 = still running); green -> get_release_by_tag v0.8.3 (needs Launcher-v0.8.3.apk), bash tools/record-release.sh v0.8.3 'v0.8.3: patch covers the stock card shadow (no faint line); stock-menu dimming measured once per theme (no repeated screenshot flashes with battery saver); Launcher control says why a measurement started', refresh HANDOFF 0g, tell Tj with the Release link on its own line; also the emulator re-run base,x1,v,ac (07:16 start) decides AN1 (BB4)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  95ccfd5 ckpt 597: Tj said 'Build it now' (07:19 UTC): dispatched android.yml full_build=true on 
  7faba16 ckpt 596: run #293 id 42349158289 (base,x1,v,ac on 8309cc6) AN1 FAILED AGAIN (67 PASS 1 
  8309cc6 ckpt 595: run #292 id 42347734404 (base,x1,v,ac,ps) on 94671f9: 78 PASS, 1 FAIL = AN1 'g
  661d9df ckpt 594: run #291 id 42347732366 (pt) on 94671f9 GREEN, 0 FAIL: PB reach 28 rows (v0.8.
  34cd3da ckpt 593: gated v0.8.3 (code 28) and pushed it: the handoff self-test, the full unit sui
  c517c03 ckpt 592: HANDOFF.md 0g refreshed for v0.8.3 + old handoff-launcher.md marked superseded
  fa034b7 ckpt 591: Tj confirmed: it was the camera flashes (BB3 reading a); BB3a per-theme slots 
  4b6dd77 ckpt 590: dispatched on 94671f9 (06:36Z): #291 id 42347732366 and #292 id 42347734404 (p
  94671f9 ckpt 589: final-head runs: #289/#290 green (n,x,ac,rb + base,x1,e1,o,last: AN1 did not r
  89fbc42 ckpt 588: gate runs on 84b3dd1: #286 (e1,o,ae,pa,rb,last) 37 PASS + emulator went offlin
```
