# CHECKPOINT 537 — read me first, then TASKS.md

**Written:** 2026-11-06T22:16:28Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-019b8e69-16dn91` · **builds on:** `5abb30d` (this checkpoint is the commit after it)

## Just done
pt run 245 analysed: main-thread stall (PIN-pad scan + power-menu scan at the first window event) delayed the dialog's event 80-280 ms and recordLatency committed prefs on the fast path; fixed: quiet burst (heavy work waits for the dialog event, FAST_WAIT_MS 120), menu-open scan first at close, recordLatency async, showStock patch first; pt probes S = max of two boxes

## Do this next
dispatch pt again (emulator.yml sections=pt), read events/curves: check S event age (<60 ms), P dims within ~100 ms of S onset; then choose lead and tighten pt checks

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  6232c7e ckpt 536: pt fixes: fast path now reacts to the dialog's own window-state event (no getW
  c62f08c ckpt 535: pt shows the patch darkens BEFORE the scrim is visible (dev -26% at onset, bot
  4b8eabb ckpt 534: TASKS: AZ STATUS block; AW2 and AW3 ticked (run #239); AW4 check in lq; runs p
  58c4d76 ckpt 533: AW5 auto-calibration + AU2/AU4/AU6/AU7/AU8 coded; sections pa and pt added; fa
  d40e485 ckpt 532: AZ1 core coded (PatchFade + service fade + scan deferral + fast path), AW2 til
  3fca9b6 ckpt 531: AZ2 PROVEN on real Android 10 (run #238, section bs: 13 PASS; bug reproduced w
  b105cf5 ckpt 530: AZ2 coded: BatterySaverGuard no longer forces ON, heals swallowed turn-ON via 
  3e77c9d ckpt 529: AZ: Tj's 2 new requests written; root causes found in Android 10 source (batte
  60dc4ed ckpt 528: SESSION CLOSED: v0.8.0 released and recorded; Tj chose AW5 = A (auto-calibrate
  dd2d14a ckpt 527: INTEGRATION: tools/MIRROR_MAIN is ON again so main follows every push (main ha
```

(10 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
