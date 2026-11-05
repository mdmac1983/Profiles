# CHECKPOINT 307 — read me first, then TASKS.md

**Written:** 2026-11-05T04:44:18Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-3bcc29a7-cuz0x0` · **builds on:** `e1adcf4` (this checkpoint is the commit after it)

## Just done
gate B (run 113): 75 PASS 1 FAIL; the one FAIL is section last 'card Restart pressed the real Restart button' (single log read 0.6 s after the tap; evidence file never printed): last now polls for the press line and prints what it found. Gate A (run 112) still running

## Do this next
dispatch 'rb,last' and 'last' on main to see if last fails alone or only after rb; read them; read gate A (run 41967538652); then tick, fill HANDOFF placeholders, ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  61c2d76 ckpt 306: HANDOFF.md refreshed for v0.7.0 (state block, architecture rows, decisions, re
  dc1c928 ckpt 305: gc harness bug: *PATH* skipped every CLASSPATH= line (helper start + ShellProb
  c8077b8 ckpt 304: gc harness: process evidence for the helper check; rt PASSES (run 108: 18 PASS
  2e195a9 ckpt 303: rt: run 106 showed Android's Recents DROPS the older same-affinity task when a
  7c43805 ckpt 302: Review fixes: ImmersiveProfile owns-only undo + snapshot refresh, KeepAlive st
  33f2d5f ckpt 301: rt rewritten: Contacts task id 8->20 (Android replaced it, code behaved per ru
  1e7e565 ckpt 300: Docs: research/BUTTONMAPPER section 3 written with REAL AOSP android-10.0.0_r4
  89e5aae ckpt 299: NEW SESSION (branch ccr-3bcc29a7-cuz0x0, hooks installed). Read the 5 pending 
  560d638 ckpt 298: CLAUDE.md: only status in_progress/queued is a safe list_workflow_runs filter;
  8761263 ckpt 297: Run 93 (pf) = 22 PASS 1 FAIL: PF-P1 was a TEST ORDER bug (uiautomator dump unb
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
