# CHECKPOINT 538 — read me first, then TASKS.md

**Written:** 2026-11-06T22:17:37Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-019b8e69-16dn91` · **builds on:** `aecdf65` (this checkpoint is the commit after it)

## Just done
quietWork cancelled in cancelPatchWork; TASKS AZ1 FLICKER FINDINGS written (runs 243/245/246 analysed)

## Do this next
read the pt run dispatched on ckpt 537 (job log tail ~110): check 'fast open ... ms old' (should be < 60), the P curve (dims within ~100 ms of S onset), pick PatchFade.LEAD_MS, tighten pt checks; then reviewer findings (agent aeb60740f2743b069 -> scratchpad/review-v081.md), delta gate, ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  ce7e0db ckpt 537: pt run 245 analysed: main-thread stall (PIN-pad scan + power-menu scan at the 
  6232c7e ckpt 536: pt fixes: fast path now reacts to the dialog's own window-state event (no getW
  c62f08c ckpt 535: pt shows the patch darkens BEFORE the scrim is visible (dev -26% at onset, bot
  4b8eabb ckpt 534: TASKS: AZ STATUS block; AW2 and AW3 ticked (run #239); AW4 check in lq; runs p
  58c4d76 ckpt 533: AW5 auto-calibration + AU2/AU4/AU6/AU7/AU8 coded; sections pa and pt added; fa
  d40e485 ckpt 532: AZ1 core coded (PatchFade + service fade + scan deferral + fast path), AW2 til
  3fca9b6 ckpt 531: AZ2 PROVEN on real Android 10 (run #238, section bs: 13 PASS; bug reproduced w
  b105cf5 ckpt 530: AZ2 coded: BatterySaverGuard no longer forces ON, heals swallowed turn-ON via 
  3e77c9d ckpt 529: AZ: Tj's 2 new requests written; root causes found in Android 10 source (batte
  60dc4ed ckpt 528: SESSION CLOSED: v0.8.0 released and recorded; Tj chose AW5 = A (auto-calibrate
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
