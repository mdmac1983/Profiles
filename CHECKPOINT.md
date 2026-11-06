# CHECKPOINT 480 — read me first, then TASKS.md

**Written:** 2026-11-06T07:37:51Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `7d1837b` (this checkpoint is the commit after it)

## Just done
v0.7.6 delta gate dispatched on 7d1837b: runs 193 (id 42182087395) legacy,y2,b4,x | 194 (id 42182090690) base,lq,rb; tag run 192 (id 42181402647) legacy on v0.7.5 pending

## Do this next
read 193/194 (job logs tail 60) + 192; if green: bash ship.sh, then android.yml full_build ref claude/new-session-7a9vpi, get_release_by_tag v0.7.6, record-release; AM1 work lives in the LOCAL worktree /home/user/Launcher-am1 (branch am1, not pushed, not autosaved: commit there by hand) until v0.7.6 is dispatched; then merge am1 into this branch

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  7d1837b ckpt 479: v0.7.6: both reviews done (no blockers), fixes applied, home patch back to v0.
  693b097 ckpt 478: v0.7.6 gate read: lq/ae/v/base/o/ac/rt/pf/last green; W3b (home patch) + 3 art
  ce35818 ckpt 477: Tj: stock menu stays stock, patch always in place; photo shows the model patch
  de3d688 ckpt 476: v0.7.6 gate dispatched on 43d6cb4: emulator.yml runs 189 (id 42179486444) lega
  43d6cb4 ckpt 475: version 0.7.6 (code 23) bumped for the gate; unit tests + lint green
  1cbf45c ckpt 474: tools/photo_rectify.py (tested, refuses the repo), HANDOFF state refreshed, CL
  106465d ckpt 473: v0.7.6: patch device profile (tb8168 shift -90 px, +8 %), wallpaper geometry r
  8e6db5b ckpt 472: v0.7.6 WIP: number constants + nudges, card layout, slot-only soft patch; AL1 
  9040fe4 ckpt 471: v0.7.6 WIP: review fixes done (CrashGuard order, saver cap, RESET log, card pr
  013c849 ckpt 470: v0.7.5 RELEASED + RECORDED (run 24, from c0e044d on the branch); v0.7.6 WIP: A
```
