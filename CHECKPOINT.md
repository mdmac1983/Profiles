# CHECKPOINT 540 — read me first, then TASKS.md

**Written:** 2026-11-06T22:33:43Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-019b8e69-16dn91` · **builds on:** `3234c56` (this checkpoint is the commit after it)

## Just done
emulator.yml: ffmpeg install step has retries + 4 min timeout (run 248 hung 6 min on the apt mirror, cancelled); pt close analysis anchors on half-recovery of the surround

## Do this next
dispatch pt (sections=pt) again and read: first dim of the patch vs S onset, open_max fade vs jump, close curves; choose PatchFade.LEAD_MS

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  39c5ade ckpt 539: pt run 247: dialog event now handled in 2-17 ms (was 80-280) but the first dim
  a52dfe7 ckpt 538: quietWork cancelled in cancelPatchWork; TASKS AZ1 FLICKER FINDINGS written (ru
  ce7e0db ckpt 537: pt run 245 analysed: main-thread stall (PIN-pad scan + power-menu scan at the 
  6232c7e ckpt 536: pt fixes: fast path now reacts to the dialog's own window-state event (no getW
  c62f08c ckpt 535: pt shows the patch darkens BEFORE the scrim is visible (dev -26% at onset, bot
  4b8eabb ckpt 534: TASKS: AZ STATUS block; AW2 and AW3 ticked (run #239); AW4 check in lq; runs p
  58c4d76 ckpt 533: AW5 auto-calibration + AU2/AU4/AU6/AU7/AU8 coded; sections pa and pt added; fa
  d40e485 ckpt 532: AZ1 core coded (PatchFade + service fade + scan deferral + fast path), AW2 til
  3fca9b6 ckpt 531: AZ2 PROVEN on real Android 10 (run #238, section bs: 13 PASS; bug reproduced w
  b105cf5 ckpt 530: AZ2 coded: BatterySaverGuard no longer forces ON, heals swallowed turn-ON via 
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
