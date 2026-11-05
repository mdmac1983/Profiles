# CHECKPOINT 333 — read me first, then TASKS.md

**Written:** 2026-11-05T08:05:25Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `41d5c9b` (this checkpoint is the commit after it)

## Just done
dispatched on main at 41d5c9b: run 127 = legacy (id 41991766360), run 128 = base,legacy (id 41991770161)

## Do this next
read both from ~08:14Z (list_workflow_jobs for the job id): 0 FAIL in at least one = reset2/W5/W3b are timing flakes on unchanged code, then ship.sh, android.yml full_build=true, get_release_by_tag v0.7.1, record-release v0.7.1; a repeat FAIL: read the 'reset2:' INFO line before deciding

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  41d5c9b ckpt 332: run 125 (legacy): 19 PASS 1 FAIL (reset2 notification not cleared in time; W5/
  2b571e9 ckpt 331: workflow step limit 2100 -> 2700 s and docs: the release gate is the two runs 
  bb99c07 ckpt 330: FULL gate run 124 (3398b67): 167 PASS 3 FAIL, cut by the 2100 s timeout in rs4
  4b02f31 ckpt 329: run 123 (x on 3398b67) green: 31 PASS 0 FAIL (setup screen finds all 5 steps, 
  1657930 ckpt 328: dispatched on main at 3398b67: run 123 = x (id 41986024102), run 124 = FULL ga
  3398b67 ckpt 327: review fixes in: keyTestLines separate caps (many input devices cannot drop KE
  b185fe1 ckpt 326: code-review of the v0.7.0..HEAD delta (medium): 9 findings, triaged against th
  55b9791 ckpt 325: dispatched on main at 7ee440a: run 121 (id 41984771050) = x, run 122 (id 41984
  7ee440a ckpt 324: x findings fixed in tools: the intake no longer flags a notification-reader mi
  d8a171b ckpt 323: ship.sh gates passed for v0.7.1 (code 18, unit+lint green, pushed) but NOT REL
```
