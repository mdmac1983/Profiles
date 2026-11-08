# CHECKPOINT 680 — read me first, then TASKS.md

**Written:** 2026-11-08T05:57:50Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2r9ji2` · **builds on:** `43d060b` (this checkpoint is the commit after it)

## Just done
v0.8.9: first gate (#351-353 on e60f9ad) CANCELLED at 05:55 UTC on purpose: a 2nd /code-review high on the delta found real issues, fixed: unreadable crop wiped ALL pages on a transient low-memory failure (now isCorrupt by bounds only + invalidateStep + bounded 5 s retry), pinPadCameUp ignored pad signs while locked with a stale home patch (now ignores only while unlocked) + home patch hidden at SCREEN_OFF, no 3 s poll for 'position not known', quietBurst on home only while our menu is up, interpolator reports once, stale javadoc. Full suite + lint green

## Do this next
bash tools/gate.sh e60f9ad shows what the delta touches; dispatch emulator.yml sections=hp alone on main first (expect 28 PASS), then the FULL tiered gate on the final commit: bash tools/gate.sh b1399c9 (3 parallel runs), then ship v0.8.9

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  fd738df ckpt 679: v0.8.9 GATE dispatched 05:47 UTC on e60f9ad (product code final, version 0.8.9
  e60f9ad ckpt 678: BI1c: hp FULLY GREEN: run #349 (4d78cfb, id 42451187830) 25 PASS; run #350 (01
  4d78cfb ckpt 677: BI1c: hp run #348 (f78001e, id 42450536427) = 21 PASS 2 FAIL: only HP7 (no dra
  f78001e ckpt 676: BI1c: hp run #347 (4a34b83) = 19 PASS 4 FAIL: phase C FIXED (rested after rest
  4a34b83 ckpt 675: BI1c: takeover; hp run #346 (c69041b) = 18 PASS 5 FAIL, phase C never rested a
  c69041b ckpt 674: BI1c: emulator section hp written (phases A-E: self-calibration, invisible res
  4b6180c ckpt 673: BI1b step 5: Launcher control rows (switch + status + Calibrate now + Forget) 
  6005aa2 ckpt 672: BI1b steps 2-4 (compiles, nothing tested on the emulator yet): HomeShot + Home
```

(13 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
