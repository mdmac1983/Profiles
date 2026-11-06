# CHECKPOINT 531 — read me first, then TASKS.md

**Written:** 2026-11-06T21:12:31Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-019b8e69-16dn91` · **builds on:** `b105cf5` (this checkpoint is the commit after it)

## Just done
AZ2 PROVEN on real Android 10 (run #238, section bs: 13 PASS; bug reproduced with the state machine dump mState=1 + Enabled=true; heal works); AZ2a-d ticked

## Do this next
AZ1: write PatchFade.java (pure, JVM tests) then TjAccessibilityService: time-based open fade anchored at the event time, defer the node scan ~330 ms after showOverlay, fast-path before the lock-number work

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  b105cf5 ckpt 530: AZ2 coded: BatterySaverGuard no longer forces ON, heals swallowed turn-ON via 
  3e77c9d ckpt 529: AZ: Tj's 2 new requests written; root causes found in Android 10 source (batte
  60dc4ed ckpt 528: SESSION CLOSED: v0.8.0 released and recorded; Tj chose AW5 = A (auto-calibrate
  dd2d14a ckpt 527: INTEGRATION: tools/MIRROR_MAIN is ON again so main follows every push (main ha
  3a25324 ckpt 526: Tj chose AW5 = A (auto-calibrate); asked to finish the session for a new one a
  e48123d ckpt 525: AX1 audited from the code: the guard cannot undo a tile tap in v0.8.0 (default
  cd79978 ckpt 524: Tj reports v0.8.0: battery saver tile in Quickstep quick settings still does n
  4a20366 ckpt 523: handoff manifest saved (.handoff/handoff.md + archive; CLAUDE.md/AGENTS.md lea
  0d3b054 ckpt 522: AV2 marked answered by Tj's AW request; nothing else changed
  5a2b618 ckpt 521: v0.8.0 released and recorded; AW1 audit of Tj's strict lock screen done (tiles
```
