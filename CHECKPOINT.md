# CHECKPOINT 314 — read me first, then TASKS.md

**Written:** 2026-11-05T05:40:13Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-3bcc29a7-cuz0x0` · **builds on:** `19d37e4` (this checkpoint is the commit after it)

## Just done
Tj: close G3d (done) + phone guide: simple, logical order for a short OTG session, add what is needed, and make sure Claude can save and use everything from the tablet. Written into TASKS.md as PG0-PG4

## Do this next
audit first: read phoneguide/src/main/assets/steps.txt fully, the phoneguide Activity code, app/src/main/resources/probe-commands.txt, TabletReport/SetupActivity (what 'Do everything' and 'Collect EVERYTHING' save), tools/adb-all.sh; list what Claude still cannot answer without the tablet; then restructure steps.txt (copy to research/ADB_PHONE_COMMANDS.txt), add cards/probe lines, write the intake script + doc, prove with test_handoff + emulator gc,g,y2,x, ship v0.7.1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  77726ab ckpt 313: Tj asked what Power menu v2 and the standing items are: answered in chat; his 
  3de52f1 ckpt 312: v0.7.0 RELEASED: https://github.com/tjshea90/Launcher/releases/tag/v0.7.0 (Lau
  245c684 ckpt 311: v0.7.0 gated by ship.sh (handoff 29/29, unit 220 green, lint clean) and pushed
  2a547f6 ckpt 310: gated v0.7.0 (code 17) and pushed it: the handoff self-test, the full unit sui
  92a21c6 ckpt 309: pre-ship: v0.7.0: task-precise Recents clean-up at the lock (only Quickstep's 
  f4adf87 ckpt 308: gate A (run 112) PASSED; gate B 75 PASS 1 FAIL = last after rb (last alone pas
  7027460 ckpt 307: gate B (run 113): 75 PASS 1 FAIL; the one FAIL is section last 'card Restart p
  61c2d76 ckpt 306: HANDOFF.md refreshed for v0.7.0 (state block, architecture rows, decisions, re
  dc1c928 ckpt 305: gc harness bug: *PATH* skipped every CLASSPATH= line (helper start + ShellProb
  c8077b8 ckpt 304: gc harness: process evidence for the helper check; rt PASSES (run 108: 18 PASS
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
