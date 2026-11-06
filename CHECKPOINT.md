# CHECKPOINT 526 — read me first, then TASKS.md

**Written:** 2026-11-06T19:31:21Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-8o8uj9` · **builds on:** `041e4c2` (this checkpoint is the commit after it)

## Just done
Tj chose AW5 = A (auto-calibrate); asked to finish the session for a new one and to FIX why prompts and Claude findings are not saved (TASKS AY1); evidence so far: prompts of this session ARE in INBOX on the branch

## Do this next
AY1: find why a fresh session does not see them (main is stale at v0.7.4?), fix (integrate main + MIRROR_MAIN on), add automatic capture of Claude's replies (OUTBOX), then final CHECKPOINT for the new session

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  e48123d ckpt 525: AX1 audited from the code: the guard cannot undo a tile tap in v0.8.0 (default
  cd79978 ckpt 524: Tj reports v0.8.0: battery saver tile in Quickstep quick settings still does n
  4a20366 ckpt 523: handoff manifest saved (.handoff/handoff.md + archive; CLAUDE.md/AGENTS.md lea
  0d3b054 ckpt 522: AV2 marked answered by Tj's AW request; nothing else changed
  5a2b618 ckpt 521: v0.8.0 released and recorded; AW1 audit of Tj's strict lock screen done (tiles
  c49ac41 ckpt 520: v0.8.0 RELEASED (android.yml run 27 green, Release published 19:14Z, BUILDLOG 
  ebe0b6e ckpt 519: Tj's new request written into TASKS (AW1-AW6): lock screen from ANY launcher, 
  a83fd22 ckpt 518: Tj asked: does the lock screen from any launcher show only the one tile and no
  85d43cf ckpt 517: FULL GATE GREEN for v0.8.0 (code 25): final-code runs 232 x1,e1,x,v,ae | 233 b
  e5f28ea ckpt 516: gated v0.8.0 (code 25) and pushed it: the handoff self-test, the full unit sui
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
