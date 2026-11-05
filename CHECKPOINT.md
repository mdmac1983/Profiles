# CHECKPOINT 332 — read me first, then TASKS.md

**Written:** 2026-11-05T08:04:48Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `36c51a3` (this checkpoint is the commit after it)

## Just done
run 125 (legacy): 19 PASS 1 FAIL (reset2 notification not cleared in time; W5/W3b of run 124 PASSED), run 126 (pf,rb,last) 35 PASS 0 FAIL; reset2 check now polls 20 s more + prints its evidence (the code on that path is byte-identical to v0.7.0)

## Do this next
dispatch legacy and base,legacy on main, read at ~08:17Z: 0 FAIL in at least one = timing flake confirmed, then ship.sh, android.yml full_build=true, get_release_by_tag v0.7.1, record-release v0.7.1; a repeat FAIL: read the new 'reset2:' INFO line (counts + RESET log lines) before deciding

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  2b571e9 ckpt 331: workflow step limit 2100 -> 2700 s and docs: the release gate is the two runs 
  bb99c07 ckpt 330: FULL gate run 124 (3398b67): 167 PASS 3 FAIL, cut by the 2100 s timeout in rs4
  4b02f31 ckpt 329: run 123 (x on 3398b67) green: 31 PASS 0 FAIL (setup screen finds all 5 steps, 
  1657930 ckpt 328: dispatched on main at 3398b67: run 123 = x (id 41986024102), run 124 = FULL ga
  3398b67 ckpt 327: review fixes in: keyTestLines separate caps (many input devices cannot drop KE
  b185fe1 ckpt 326: code-review of the v0.7.0..HEAD delta (medium): 9 findings, triaged against th
  55b9791 ckpt 325: dispatched on main at 7ee440a: run 121 (id 41984771050) = x, run 122 (id 41984
  7ee440a ckpt 324: x findings fixed in tools: the intake no longer flags a notification-reader mi
  d8a171b ckpt 323: ship.sh gates passed for v0.7.1 (code 18, unit+lint green, pushed) but NOT REL
  97b98f9 ckpt 322: gated v0.7.1 (code 18) and pushed it: the handoff self-test, the full unit sui
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
