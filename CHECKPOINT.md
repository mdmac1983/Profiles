# CHECKPOINT 330 — read me first, then TASKS.md

**Written:** 2026-11-05T07:55:57Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `cc13be4` (this checkpoint is the commit after it)

## Just done
FULL gate run 124 (3398b67): 167 PASS 3 FAIL, cut by the 2100 s timeout in rs4 (pf, rb, last never ran); the 3 FAILs (W5 x2, W3b) are all in section legacy, after an INFO NATIVE CRASH line = suspected known flake, NOT yet proven

## Do this next
read run 125 = legacy (id 41990611991) and run 126 = pf,rb,last (id 41990616136) at ~08:01Z (job id via list_workflow_jobs): legacy 0 FAIL proves the flake (product code is identical to 3398b67), else W5/W3b are real: investigate; pf,rb,last must be 0 FAIL; then ship.sh, android.yml full_build=true, get_release_by_tag v0.7.1, record-release v0.7.1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  4b02f31 ckpt 329: run 123 (x on 3398b67) green: 31 PASS 0 FAIL (setup screen finds all 5 steps, 
  1657930 ckpt 328: dispatched on main at 3398b67: run 123 = x (id 41986024102), run 124 = FULL ga
  3398b67 ckpt 327: review fixes in: keyTestLines separate caps (many input devices cannot drop KE
  b185fe1 ckpt 326: code-review of the v0.7.0..HEAD delta (medium): 9 findings, triaged against th
  55b9791 ckpt 325: dispatched on main at 7ee440a: run 121 (id 41984771050) = x, run 122 (id 41984
  7ee440a ckpt 324: x findings fixed in tools: the intake no longer flags a notification-reader mi
  d8a171b ckpt 323: ship.sh gates passed for v0.7.1 (code 18, unit+lint green, pushed) but NOT REL
  97b98f9 ckpt 322: gated v0.7.1 (code 18) and pushed it: the handoff self-test, the full unit sui
  3029356 ckpt 321: dispatched emulator sections on main at 05c10f9: run 119 (id 41983710605, prob
  05c10f9 ckpt 320: finished the cut-off change: KEYTEST-LINES (getevent silent is not 'key not pr
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
