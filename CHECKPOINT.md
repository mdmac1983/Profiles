# CHECKPOINT 308 — read me first, then TASKS.md

**Written:** 2026-11-05T04:53:32Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-3bcc29a7-cuz0x0` · **builds on:** `7027460` (this checkpoint is the commit after it)

## Just done
gate A (run 112) PASSED; gate B 75 PASS 1 FAIL = last after rb (last alone passes, run 115): after the restart the last step finds no power menu/card log. Suspect: the restart drops the display overrides (wm size/density/overscan/font scale) the scenario sets once; rb now prints them and re-applies them at its end; last prints its own state

## Do this next
dispatch 'rb,last' again on main and read; if green, the gate is complete (A=112 PASS, B=113 all but last, last ok): tick AA1 AA2 AA5 AB1 AB2 AB4 AA6, fill HANDOFF placeholders, ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  7027460 ckpt 307: gate B (run 113): 75 PASS 1 FAIL; the one FAIL is section last 'card Restart p
  61c2d76 ckpt 306: HANDOFF.md refreshed for v0.7.0 (state block, architecture rows, decisions, re
  dc1c928 ckpt 305: gc harness bug: *PATH* skipped every CLASSPATH= line (helper start + ShellProb
  c8077b8 ckpt 304: gc harness: process evidence for the helper check; rt PASSES (run 108: 18 PASS
  2e195a9 ckpt 303: rt: run 106 showed Android's Recents DROPS the older same-affinity task when a
  7c43805 ckpt 302: Review fixes: ImmersiveProfile owns-only undo + snapshot refresh, KeepAlive st
  33f2d5f ckpt 301: rt rewritten: Contacts task id 8->20 (Android replaced it, code behaved per ru
  1e7e565 ckpt 300: Docs: research/BUTTONMAPPER section 3 written with REAL AOSP android-10.0.0_r4
  89e5aae ckpt 299: NEW SESSION (branch ccr-3bcc29a7-cuz0x0, hooks installed). Read the 5 pending 
  560d638 ckpt 298: CLAUDE.md: only status in_progress/queued is a safe list_workflow_runs filter;
```
