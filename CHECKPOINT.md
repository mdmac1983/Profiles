# CHECKPOINT 458 — read me first, then TASKS.md

**Written:** 2026-11-06T05:57:03Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-22ba09da-s9vh1t` · **builds on:** `9c2baa0` (this checkpoint is the commit after it)

## Just done
AG2 battery saver TILE in Quickstep does not work: recorded + analysed; need the Control 'Battery saver guard' line; v0.7.5 gate runs 186/187/188 + review pending

## Do this next
~06:09 read 186/187/188 + review wf_a559b059-812; add low_power change logging (AG2) with the review fixes; rerun only ac; ship v0.7.5

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  b86027c ckpt 457: v0.7.5 gate dispatched on ea74960: runs 186 (y2,b4,n6,o,pf,rb) 187 (t,v,ae,rt,
  ea74960 ckpt 456: v0.7.5 APPLIED on the branch (patch applied cleanly, version 0.7.5/22, gate-ma
  cc28ba7 ckpt 455: TK3 DONE: v0.7.4 released + recorded (main 14f6334); merged into branch; HANDO
  cdc5ced ckpt 454: AJ1 DONE: tiered emulator gate (tools/gate.sh + gate-map.txt + timing lines + 
  465b421 ckpt 453: v0.7.4: main=979b450, android.yml full_build dispatched
  979b450 ckpt 452: TK3 GATE COMPLETE: v0.7.4 cleared (A split runs 183/184/185 green, B' 181 + 18
  a3852d8 ckpt 451: TK3 gate A run 180 cut at 45 min in section o (W3b x2 after a native crash, O1
  2386d4f ckpt 450: AI1: usage-log merge (QuickstepUsage/AppOrigin.mergeUsage/Mode qsStartAt) + re
  ad8e719 ckpt 449: AH1: Tj thinks wrong wallpaper piece is copied; plan = geometry line + calibra
  5dec87a ckpt 448: AI1 leftover Quickstep app in Recents recorded + analysed (needs app name/path
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
