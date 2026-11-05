# CHECKPOINT 313 — read me first, then TASKS.md

**Written:** 2026-11-05T05:32:33Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-3bcc29a7-cuz0x0` · **builds on:** `3d241d8` (this checkpoint is the commit after it)

## Just done
Tj asked what Power menu v2 and the standing items are: answered in chat; his question + the 5 decisions that wait on him recorded in TASKS.md and HANDOFF section 6

## Do this next
Nothing to build until Tj decides. v0.7.0 is released (https://github.com/tjshea90/Launcher/releases/tag/v0.7.0). If he says yes to the v0.7.1 guide card: add a getevent read-test card to phoneguide steps.txt (and research/ADB_PHONE_COMMANDS.txt, identical), run emulator sections gc and g, ship v0.7.1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M HANDOFF.md
     M TASKS.md

## Last ten checkpoints
```
  3de52f1 ckpt 312: v0.7.0 RELEASED: https://github.com/tjshea90/Launcher/releases/tag/v0.7.0 (Lau
  245c684 ckpt 311: v0.7.0 gated by ship.sh (handoff 29/29, unit 220 green, lint clean) and pushed
  2a547f6 ckpt 310: gated v0.7.0 (code 17) and pushed it: the handoff self-test, the full unit sui
  92a21c6 ckpt 309: pre-ship: v0.7.0: task-precise Recents clean-up at the lock (only Quickstep's 
  f4adf87 ckpt 308: gate A (run 112) PASSED; gate B 75 PASS 1 FAIL = last after rb (last alone pas
  7027460 ckpt 307: gate B (run 113): 75 PASS 1 FAIL; the one FAIL is section last 'card Restart p
  61c2d76 ckpt 306: HANDOFF.md refreshed for v0.7.0 (state block, architecture rows, decisions, re
  dc1c928 ckpt 305: gc harness bug: *PATH* skipped every CLASSPATH= line (helper start + ShellProb
  c8077b8 ckpt 304: gc harness: process evidence for the helper check; rt PASSES (run 108: 18 PASS
  2e195a9 ckpt 303: rt: run 106 showed Android's Recents DROPS the older same-affinity task when a
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
