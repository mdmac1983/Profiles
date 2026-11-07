# CHECKPOINT 584 — read me first, then TASKS.md

**Written:** 2026-11-07T06:10:23Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `48d8643` (this checkpoint is the commit after it)

## Just done
BB1b: defaults chosen (core +24 dp, bottom fringe 4 dp), PatchRestTest 24 green; probe measures the shadow under the emulator's 2nd white card + predicts the line + real checks; run #284 findings in TASKS BB1d

## Do this next
dispatch the 3 gate runs on ccr-823c5a56-yg8voe: {e1,o,ae,pa,rb,last} {n,pt} {base,x1,v,ac,ps} (tools/gate.sh 7b42deb); read pt's PB lines (reach / predicted line / menu entries) first; a FAIL = diagnose, do not just rerun; all green -> bump version 0.8.3 (code 28), ship.sh, android.yml full_build, record-release, tell Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  f0c3778 ckpt 583: dispatched emulator.yml pt alone: run #284 id 42344459448 on b20679f (05:51Z) 
  b20679f ckpt 582: run #283 read: probe works (cores end 2/12/22 px below the panel as designed),
  31f893d ckpt 581: dispatched emulator.yml pt alone: run #283 id 42343234753 on aef44d4 (05:33Z),
  aef44d4 ckpt 580: BB1b step 1: patch core extension coded (PatchRest.slotWindow extra + clampExt
  9b150a1 ckpt 579: BB1 diagnosed from Tj's 2 new v0.8.2 photos: patch interior now right (left ed
  03502f8 ckpt 578: Tj tried v0.8.2: 'almost perfect but a very slight tinted line about where the
  564522c ckpt 577: Tj's screenshot: v0.8.2 measured ON THE TABLET scale 0.800 + offset 6/7/7 (tin
  7b42deb ckpt 576: v0.8.2 RELEASED + RECORDED: android.yml #29 id 42330571999 success, Release v0
  a730d5e ckpt 575: android.yml run #29 id 42330571999 dispatched 02:42:37Z on main 06f59a9 (full_
  06f59a9 ckpt 574: gated v0.8.2 (code 27) and pushed it: the handoff self-test, the full unit sui
```

(9 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
