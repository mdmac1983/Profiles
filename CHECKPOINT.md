# CHECKPOINT 532 — read me first, then TASKS.md

**Written:** 2026-11-06T21:24:03Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-019b8e69-16dn91` · **builds on:** `b22a75c` (this checkpoint is the commit after it)

## Just done
AZ1 core coded (PatchFade + service fade + scan deferral + fast path), AW2 tiles at lock, AW3 radios retry, early boot hook; tests green; lq extended

## Do this next
dispatch emulator lq and v,ae,ps on branch, read RESULTS; then AW5 auto-calibration + AU2-AU8, pt timing section

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  3fca9b6 ckpt 531: AZ2 PROVEN on real Android 10 (run #238, section bs: 13 PASS; bug reproduced w
  b105cf5 ckpt 530: AZ2 coded: BatterySaverGuard no longer forces ON, heals swallowed turn-ON via 
  3e77c9d ckpt 529: AZ: Tj's 2 new requests written; root causes found in Android 10 source (batte
  60dc4ed ckpt 528: SESSION CLOSED: v0.8.0 released and recorded; Tj chose AW5 = A (auto-calibrate
  dd2d14a ckpt 527: INTEGRATION: tools/MIRROR_MAIN is ON again so main follows every push (main ha
  3a25324 ckpt 526: Tj chose AW5 = A (auto-calibrate); asked to finish the session for a new one a
  e48123d ckpt 525: AX1 audited from the code: the guard cannot undo a tile tap in v0.8.0 (default
  cd79978 ckpt 524: Tj reports v0.8.0: battery saver tile in Quickstep quick settings still does n
  4a20366 ckpt 523: handoff manifest saved (.handoff/handoff.md + archive; CLAUDE.md/AGENTS.md lea
  0d3b054 ckpt 522: AV2 marked answered by Tj's AW request; nothing else changed
```

(21 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
