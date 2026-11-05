# CHECKPOINT 315 — read me first, then TASKS.md

**Written:** 2026-11-05T05:49:46Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-3bcc29a7-cuz0x0` · **builds on:** `3d624d1` (this checkpoint is the commit after it)

## Just done
PG1 done: phone guide redesigned for a short cable session. New order: PART 1 tablet prep, PART 2 cable (ONE must line: start the helper), PART 3 buttons on the tablet (Do everything, Collect report, Side-key test, Send), then FALLBACK / EXTRAS / UNDO closed until tapped. Guide app: plan box, jump buttons, next-step button, star on must cards, GuideParser (pure) + 7 structure tests

## Do this next
STEP B (tablet capture, v0.7.1): probe-commands.txt new read-only lines (notification/recents samples, power-key device, policy_control, keep-alive services, crash + own log tail); ReportState (safe allow-list) + TabletReport state/parser self-check/own-log; KEYTEST job in ShellProtocol.RUN + SetupActivity labels '1. Do everything / 2. Collect report / 3. Side-key test / 4. Collect EVERYTHING' + answer buttons; unit tests; emulator x asserts. Then STEP C intake (tools/tablet-intake.sh + tablet_facts.py + research/tablet/README.md + test), STEP D prove + ship v0.7.1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  35fada6 ckpt 314: Tj: close G3d (done) + phone guide: simple, logical order for a short OTG sess
  77726ab ckpt 313: Tj asked what Power menu v2 and the standing items are: answered in chat; his 
  3de52f1 ckpt 312: v0.7.0 RELEASED: https://github.com/tjshea90/Launcher/releases/tag/v0.7.0 (Lau
  245c684 ckpt 311: v0.7.0 gated by ship.sh (handoff 29/29, unit 220 green, lint clean) and pushed
  2a547f6 ckpt 310: gated v0.7.0 (code 17) and pushed it: the handoff self-test, the full unit sui
  92a21c6 ckpt 309: pre-ship: v0.7.0: task-precise Recents clean-up at the lock (only Quickstep's 
  f4adf87 ckpt 308: gate A (run 112) PASSED; gate B 75 PASS 1 FAIL = last after rb (last alone pas
  7027460 ckpt 307: gate B (run 113): 75 PASS 1 FAIL; the one FAIL is section last 'card Restart p
  61c2d76 ckpt 306: HANDOFF.md refreshed for v0.7.0 (state block, architecture rows, decisions, re
  dc1c928 ckpt 305: gc harness bug: *PATH* skipped every CLASSPATH= line (helper start + ShellProb
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
