# CHECKPOINT 479 — read me first, then TASKS.md

**Written:** 2026-11-06T07:36:41Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `bb69129` (this checkpoint is the commit after it)

## Just done
v0.7.6: both reviews done (no blockers), fixes applied, home patch back to v0.7.5 path, intake + gate fixes; 413 tests green

## Do this next
1) read legacy run 192 (id 42181402647, on tag v0.7.5, ~07:41): does W3b pass on v0.7.5? (job logs tail 60 only); 2) delta gate on the final commit: runs {legacy,y2,b4,x} and {lq,rb,base} (+ae,v only if the lock patch code changes again); 3) ship.sh, android.yml full_build on the branch, record-release v0.7.6; 4) then AM1 = v0.7.7

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  693b097 ckpt 478: v0.7.6 gate read: lq/ae/v/base/o/ac/rt/pf/last green; W3b (home patch) + 3 art
  ce35818 ckpt 477: Tj: stock menu stays stock, patch always in place; photo shows the model patch
  de3d688 ckpt 476: v0.7.6 gate dispatched on 43d6cb4: emulator.yml runs 189 (id 42179486444) lega
  43d6cb4 ckpt 475: version 0.7.6 (code 23) bumped for the gate; unit tests + lint green
  1cbf45c ckpt 474: tools/photo_rectify.py (tested, refuses the repo), HANDOFF state refreshed, CL
  106465d ckpt 473: v0.7.6: patch device profile (tb8168 shift -90 px, +8 %), wallpaper geometry r
  8e6db5b ckpt 472: v0.7.6 WIP: number constants + nudges, card layout, slot-only soft patch; AL1 
  9040fe4 ckpt 471: v0.7.6 WIP: review fixes done (CrashGuard order, saver cap, RESET log, card pr
  013c849 ckpt 470: v0.7.5 RELEASED + RECORDED (run 24, from c0e044d on the branch); v0.7.6 WIP: A
  0cd8ac2 ckpt 469: v0.7.5 build dispatched: android.yml run 24 (id 42176728625) on c0e044d, branc
```

(6 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
