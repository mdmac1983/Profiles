# CHECKPOINT 316 — read me first, then TASKS.md

**Written:** 2026-11-05T05:55:28Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-3bcc29a7-cuz0x0` · **builds on:** `393a9ec` (this checkpoint is the commit after it)

## Just done
PG2/PG3 part 1 (tablet side, code + unit tests green, emulator proof pending): report now holds launcher state (ReportState allow-list), PARSER SELF-CHECK, own log; probe list +samples (notification/recents/power-key device/policy_control/keep-alive/crash+log); KEYTEST fixed routine in the helper + '3. Side-key test' button with answer buttons; setup buttons numbered 1-4

## Do this next
STEP C: write tools/tablet_intake.py (zip/folder/file in -> redact -> caps -> research/tablet/<date>/ + FACTS.md + LATEST.md) + tools/tablet-intake.sh wrapper + research/tablet/README.md + sample fixture + test in tools/test_handoff.sh; CLAUDE.md/HANDOFF pointer. Then STEP D: emulator x (assert state/self-check/keytest), gc, g, y2; bump app 0.7.1 (code 18) + phoneguide 1.2 (code 3); ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  b660de3 ckpt 315: PG1 done: phone guide redesigned for a short cable session. New order: PART 1 
  35fada6 ckpt 314: Tj: close G3d (done) + phone guide: simple, logical order for a short OTG sess
  77726ab ckpt 313: Tj asked what Power menu v2 and the standing items are: answered in chat; his 
  3de52f1 ckpt 312: v0.7.0 RELEASED: https://github.com/tjshea90/Launcher/releases/tag/v0.7.0 (Lau
  245c684 ckpt 311: v0.7.0 gated by ship.sh (handoff 29/29, unit 220 green, lint clean) and pushed
  2a547f6 ckpt 310: gated v0.7.0 (code 17) and pushed it: the handoff self-test, the full unit sui
  92a21c6 ckpt 309: pre-ship: v0.7.0: task-precise Recents clean-up at the lock (only Quickstep's 
  f4adf87 ckpt 308: gate A (run 112) PASSED; gate B 75 PASS 1 FAIL = last after rb (last alone pas
  7027460 ckpt 307: gate B (run 113): 75 PASS 1 FAIL; the one FAIL is section last 'card Restart p
  61c2d76 ckpt 306: HANDOFF.md refreshed for v0.7.0 (state block, architecture rows, decisions, re
```

(8 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
