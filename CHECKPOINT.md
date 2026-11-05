# CHECKPOINT 325 — read me first, then TASKS.md

**Written:** 2026-11-05T07:05:54Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `7ee440a` (this checkpoint is the commit after it)

## Just done
dispatched on main at 7ee440a: run 121 (id 41984771050) = x, run 122 (id 41984775245) = FULL emulator gate (empty sections, ~25 min, started 07:05Z)

## Do this next
1) ~07:09Z read run 121: list_workflow_jobs resource_id=41984771050 -> job id -> get_job_logs tail 60 (404 = still running); x must be 0 FAIL, INFO lines show which numbered steps were missing and the real notification pkgs. 2) ~07:32Z read run 122 the same way (tail 175 if the block is not visible); ANY FAIL near an INFO NATIVE CRASH line = rerun only that section. 3) only when 122 has 0 FAIL: mcp__github__actions_run_trigger run_workflow android.yml ref=main inputs full_build=true; confirm Release v0.7.1 with get_release_by_tag; THEN bash tools/record-release.sh v0.7.1 "<note from ship.log>"; give Tj the Release link as plain text on its own line

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  7ee440a ckpt 324: x findings fixed in tools: the intake no longer flags a notification-reader mi
  d8a171b ckpt 323: ship.sh gates passed for v0.7.1 (code 18, unit+lint green, pushed) but NOT REL
  97b98f9 ckpt 322: gated v0.7.1 (code 18) and pushed it: the handoff self-test, the full unit sui
  3029356 ckpt 321: dispatched emulator sections on main at 05c10f9: run 119 (id 41983710605, prob
  05c10f9 ckpt 320: finished the cut-off change: KEYTEST-LINES (getevent silent is not 'key not pr
  181f88b ckpt 319: PG3: short summary as text (TabletReport.summary + setup button 2b + share she
  cc68086 ckpt 318: STEP D code: emulator sections g (new guide order, collapse, tick, next-step) 
  6454345 ckpt 317: PG3 intake done: tools/tablet-intake.sh + tablet_intake.py (redact, credential
  aa2b6de ckpt 316: PG2/PG3 part 1 (tablet side, code + unit tests green, emulator proof pending):
  b660de3 ckpt 315: PG1 done: phone guide redesigned for a short cable session. New order: PART 1 
```
