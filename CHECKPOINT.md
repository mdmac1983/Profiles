# CHECKPOINT 596 — read me first, then TASKS.md

**Written:** 2026-11-07T07:15:39Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `bf1450a` (this checkpoint is the commit after it)

## Just done
run #293 id 42349158289 (base,x1,v,ac on 8309cc6) AN1 FAILED AGAIN (67 PASS 1 FAIL), the diagnostic found it: the ONE counted line 'BATSAVER off (the custom launcher became active)' is stamped 07:06:15.948 but the log was cleared at 07:06:23 and the tap came after: it is the double-Home change's own legitimate switch-off, delivered by logd after the clear (guest clock ran ~16% slow after section v). Product unchanged. AN1 now counts lines by their own epoch stamp (logcat -v epoch vs device date) + INFO with stale/parsed counts + a never-vacuous guard

## Do this next
dispatch emulator.yml sections base,x1,v,ac on ccr-823c5a56-yg8voe again (about 12 min, ~07:15 start); AN1 passing = release: dispatch android.yml full_build on main, get_release_by_tag v0.8.3, tools/record-release.sh v0.8.3, HANDOFF 0g, tell Tj; AN1 failing = read the 'AN1:' INFO lines (they now carry stale count, parsed count and the lock-log timeline)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  8309cc6 ckpt 595: run #292 id 42347734404 (base,x1,v,ac,ps) on 94671f9: 78 PASS, 1 FAIL = AN1 'g
  661d9df ckpt 594: run #291 id 42347732366 (pt) on 94671f9 GREEN, 0 FAIL: PB reach 28 rows (v0.8.
  34cd3da ckpt 593: gated v0.8.3 (code 28) and pushed it: the handoff self-test, the full unit sui
  c517c03 ckpt 592: HANDOFF.md 0g refreshed for v0.8.3 + old handoff-launcher.md marked superseded
  fa034b7 ckpt 591: Tj confirmed: it was the camera flashes (BB3 reading a); BB3a per-theme slots 
  4b6dd77 ckpt 590: dispatched on 94671f9 (06:36Z): #291 id 42347732366 and #292 id 42347734404 (p
  94671f9 ckpt 589: final-head runs: #289/#290 green (n,x,ac,rb + base,x1,e1,o,last: AN1 did not r
  89fbc42 ckpt 588: gate runs on 84b3dd1: #286 (e1,o,ae,pa,rb,last) 37 PASS + emulator went offlin
  aaf3b24 ckpt 587: six gate runs in flight: ON 84b3dd1 (06:10Z) #285 id 42345818982, #286 id 4234
  6900074 ckpt 586: BB2 (Tj: 'the lock screen screenshot itself again, not sure why'): the helper 
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
