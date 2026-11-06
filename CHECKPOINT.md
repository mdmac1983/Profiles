# CHECKPOINT 481 — read me first, then TASKS.md

**Written:** 2026-11-06T07:47:55Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `274b1f5` (this checkpoint is the commit after it)

## Just done
AM1 coded in the local worktree /home/user/Launcher-am1 (421 tests); v0.7.6 delta gate runs 193/194 pending

## Do this next
at ~07:52 read runs 193 (id 42182087395) and 194 (id 42182090690) with job logs tail 40 ONLY; if green: bash ship.sh <note>, dispatch android.yml full_build on the branch, get_release_by_tag v0.7.6, record-release; THEN: git merge am1 (worktree /home/user/Launcher-am1, branch am1) into this branch, push, dispatch emulator.yml sections ps then ae,v,last,x1,base

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  274b1f5 ckpt 480: v0.7.6 delta gate dispatched on 7d1837b: runs 193 (id 42182087395) legacy,y2,b
  7d1837b ckpt 479: v0.7.6: both reviews done (no blockers), fixes applied, home patch back to v0.
  693b097 ckpt 478: v0.7.6 gate read: lq/ae/v/base/o/ac/rt/pf/last green; W3b (home patch) + 3 art
  ce35818 ckpt 477: Tj: stock menu stays stock, patch always in place; photo shows the model patch
  de3d688 ckpt 476: v0.7.6 gate dispatched on 43d6cb4: emulator.yml runs 189 (id 42179486444) lega
  43d6cb4 ckpt 475: version 0.7.6 (code 23) bumped for the gate; unit tests + lint green
  1cbf45c ckpt 474: tools/photo_rectify.py (tested, refuses the repo), HANDOFF state refreshed, CL
  106465d ckpt 473: v0.7.6: patch device profile (tb8168 shift -90 px, +8 %), wallpaper geometry r
  8e6db5b ckpt 472: v0.7.6 WIP: number constants + nudges, card layout, slot-only soft patch; AL1 
  9040fe4 ckpt 471: v0.7.6 WIP: review fixes done (CrashGuard order, saver cap, RESET log, card pr
```
