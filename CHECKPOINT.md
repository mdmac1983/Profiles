# CHECKPOINT 588 — read me first, then TASKS.md

**Written:** 2026-11-07T06:29:31Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `79bee29` (this checkpoint is the commit after it)

## Just done
gate runs on 84b3dd1: #286 (e1,o,ae,pa,rb,last) 37 PASS + emulator went offline in rb (infra; e1 o ae pa passed, PA 2 MAE 9e-7 with the new geometry); #287 (base,x1,v,ac,ps) 77 PASS 2 FAIL = V5 (scrim read 0.750 after x1: same hard-coded-0.80 assumption, now tolerance grows with the gap) + AN1 (a 'BATSAVER off' line in the 3 s window; app code since v0.8.2 touches only PatchRest/TjAccessibilityService geometry, so not the guard; now prints the line); #285 (n,pt) still running

## Do this next
read #285 id 42345818982 (n,pt on 84b3dd1: PB lines) and #288 id 42346297361 / #289 id 42346299293 / #290 id 42346301196 (final head 6900074) around 06:33Z; then re-run on the head: rb,last (infra) and ac with x1,v before it (AN1 line); the rest per gate.sh

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  aaf3b24 ckpt 587: six gate runs in flight: ON 84b3dd1 (06:10Z) #285 id 42345818982, #286 id 4234
  6900074 ckpt 586: BB2 (Tj: 'the lock screen screenshot itself again, not sure why'): the helper 
  35f1518 ckpt 585: dispatched the 3 gate runs on 84b3dd1 at 06:10Z: #285 id 42345818982, #286 id 
  84b3dd1 ckpt 584: BB1b: defaults chosen (core +24 dp, bottom fringe 4 dp), PatchRestTest 24 gree
  f0c3778 ckpt 583: dispatched emulator.yml pt alone: run #284 id 42344459448 on b20679f (05:51Z) 
  b20679f ckpt 582: run #283 read: probe works (cores end 2/12/22 px below the panel as designed),
  31f893d ckpt 581: dispatched emulator.yml pt alone: run #283 id 42343234753 on aef44d4 (05:33Z),
  aef44d4 ckpt 580: BB1b step 1: patch core extension coded (PatchRest.slotWindow extra + clampExt
  9b150a1 ckpt 579: BB1 diagnosed from Tj's 2 new v0.8.2 photos: patch interior now right (left ed
  03502f8 ckpt 578: Tj tried v0.8.2: 'almost perfect but a very slight tinted line about where the
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
