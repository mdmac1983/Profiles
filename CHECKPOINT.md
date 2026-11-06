# CHECKPOINT 529 — read me first, then TASKS.md

**Written:** 2026-11-06T20:58:28Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-019b8e69-16dn91` · **builds on:** `466f4fe` (this checkpoint is the commit after it)

## Just done
AZ: Tj's 2 new requests written; root causes found in Android 10 source (battery saver state machine desync; stock menu scrim 300 ms fade)

## Do this next
AZ2a/b: BatterySaverGuard (stop forcing ON, heal via low_power_manual_activation_count); AZ1a: new PatchFade.java + tests; then AW2-AW5

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  60dc4ed ckpt 528: SESSION CLOSED: v0.8.0 released and recorded; Tj chose AW5 = A (auto-calibrate
  dd2d14a ckpt 527: INTEGRATION: tools/MIRROR_MAIN is ON again so main follows every push (main ha
  3a25324 ckpt 526: Tj chose AW5 = A (auto-calibrate); asked to finish the session for a new one a
  e48123d ckpt 525: AX1 audited from the code: the guard cannot undo a tile tap in v0.8.0 (default
  cd79978 ckpt 524: Tj reports v0.8.0: battery saver tile in Quickstep quick settings still does n
  4a20366 ckpt 523: handoff manifest saved (.handoff/handoff.md + archive; CLAUDE.md/AGENTS.md lea
  0d3b054 ckpt 522: AV2 marked answered by Tj's AW request; nothing else changed
  5a2b618 ckpt 521: v0.8.0 released and recorded; AW1 audit of Tj's strict lock screen done (tiles
  c49ac41 ckpt 520: v0.8.0 RELEASED (android.yml run 27 green, Release published 19:14Z, BUILDLOG 
  ebe0b6e ckpt 519: Tj's new request written into TASKS (AW1-AW6): lock screen from ANY launcher, 
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
