# CHECKPOINT 328 — read me first, then TASKS.md

**Written:** 2026-11-05T07:16:35Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `3398b67` (this checkpoint is the commit after it)

## Just done
dispatched on main at 3398b67: run 123 = x (id 41986024102), run 124 = FULL gate (id 41986028741)

## Do this next
read 123 at ~07:21Z (list_workflow_jobs for the job id; 404 = running): x must have 0 FAIL; read 124 at ~07:42Z (tail 175); then ship.sh, android.yml full_build=true on main, get_release_by_tag v0.7.1, record-release v0.7.1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  3398b67 ckpt 327: review fixes in: keyTestLines separate caps (many input devices cannot drop KE
  b185fe1 ckpt 326: code-review of the v0.7.0..HEAD delta (medium): 9 findings, triaged against th
  55b9791 ckpt 325: dispatched on main at 7ee440a: run 121 (id 41984771050) = x, run 122 (id 41984
  7ee440a ckpt 324: x findings fixed in tools: the intake no longer flags a notification-reader mi
  d8a171b ckpt 323: ship.sh gates passed for v0.7.1 (code 18, unit+lint green, pushed) but NOT REL
  97b98f9 ckpt 322: gated v0.7.1 (code 18) and pushed it: the handoff self-test, the full unit sui
  3029356 ckpt 321: dispatched emulator sections on main at 05c10f9: run 119 (id 41983710605, prob
  05c10f9 ckpt 320: finished the cut-off change: KEYTEST-LINES (getevent silent is not 'key not pr
  181f88b ckpt 319: PG3: short summary as text (TabletReport.summary + setup button 2b + share she
  cc68086 ckpt 318: STEP D code: emulator sections g (new guide order, collapse, tick, next-step) 
```
