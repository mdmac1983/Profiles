# CHECKPOINT 311 — read me first, then TASKS.md

**Written:** 2026-11-05T05:02:58Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-3bcc29a7-cuz0x0` · **builds on:** `2a547f6` (this checkpoint is the commit after it)

## Just done
v0.7.0 gated by ship.sh (handoff 29/29, unit 220 green, lint clean) and pushed; android.yml full_build=true dispatched on main ~05:05Z. Gate: A run 112 106 PASS 0 FAIL; B run 113 75 PASS + 1 test-env FAIL fixed (run 116 rb,last 16 PASS)

## Do this next
WHEN android.yml is green (list_workflow_runs resource_id android.yml branch main perPage 1; get_job_logs run_id failed_only if red): mcp__github__get_release_by_tag v0.7.0 (assets Launcher-v0.7.0.apk + PhoneGuide-v0.7.0.apk), then bash tools/record-release.sh v0.7.0 with the note from ship.sh output (BUILDLOG), replace RELEASE_STATE text in HANDOFF 0b, tick AA6 in TASKS, give Tj the Release link as plain text + the one-cable recipe (HANDOFF 0b)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  2a547f6 ckpt 310: gated v0.7.0 (code 17) and pushed it: the handoff self-test, the full unit sui
  92a21c6 ckpt 309: pre-ship: v0.7.0: task-precise Recents clean-up at the lock (only Quickstep's 
  f4adf87 ckpt 308: gate A (run 112) PASSED; gate B 75 PASS 1 FAIL = last after rb (last alone pas
  7027460 ckpt 307: gate B (run 113): 75 PASS 1 FAIL; the one FAIL is section last 'card Restart p
  61c2d76 ckpt 306: HANDOFF.md refreshed for v0.7.0 (state block, architecture rows, decisions, re
  dc1c928 ckpt 305: gc harness bug: *PATH* skipped every CLASSPATH= line (helper start + ShellProb
  c8077b8 ckpt 304: gc harness: process evidence for the helper check; rt PASSES (run 108: 18 PASS
  2e195a9 ckpt 303: rt: run 106 showed Android's Recents DROPS the older same-affinity task when a
  7c43805 ckpt 302: Review fixes: ImmersiveProfile owns-only undo + snapshot refresh, KeepAlive st
  33f2d5f ckpt 301: rt rewritten: Contacts task id 8->20 (Android replaced it, code behaved per ru
```
