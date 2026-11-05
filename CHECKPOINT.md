# CHECKPOINT 318 — read me first, then TASKS.md

**Written:** 2026-11-05T06:09:00Z · **tests:** all 2 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-3bcc29a7-cuz0x0` · **builds on:** `d7524b7` (this checkpoint is the commit after it)

## Just done
STEP D code: emulator sections g (new guide order, collapse, tick, next-step) and x (real report: state, parser self-check, keytest markers, intake on the real format) rewritten; key test stored before the report in autorun; versions 0.7.1 (18) / guide 1.2 (3)

## Do this next
dispatch emulator.yml on main: sections 'x' and 'g,gc' in parallel (read results via list_workflow_runs filter in_progress, then get_job_logs tail 80); fix what fails; then ship.sh, android.yml full_build, record-release v0.7.1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M app/build.gradle.kts
     M phoneguide/build.gradle.kts

## Last ten checkpoints
```
  6454345 ckpt 317: PG3 intake done: tools/tablet-intake.sh + tablet_intake.py (redact, credential
  aa2b6de ckpt 316: PG2/PG3 part 1 (tablet side, code + unit tests green, emulator proof pending):
  b660de3 ckpt 315: PG1 done: phone guide redesigned for a short cable session. New order: PART 1 
  35fada6 ckpt 314: Tj: close G3d (done) + phone guide: simple, logical order for a short OTG sess
  77726ab ckpt 313: Tj asked what Power menu v2 and the standing items are: answered in chat; his 
  3de52f1 ckpt 312: v0.7.0 RELEASED: https://github.com/tjshea90/Launcher/releases/tag/v0.7.0 (Lau
  245c684 ckpt 311: v0.7.0 gated by ship.sh (handoff 29/29, unit 220 green, lint clean) and pushed
  2a547f6 ckpt 310: gated v0.7.0 (code 17) and pushed it: the handoff self-test, the full unit sui
  92a21c6 ckpt 309: pre-ship: v0.7.0: task-precise Recents clean-up at the lock (only Quickstep's 
  f4adf87 ckpt 308: gate A (run 112) PASSED; gate B 75 PASS 1 FAIL = last after rb (last alone pas
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
