# CHECKPOINT 478 — read me first, then TASKS.md

**Written:** 2026-11-06T07:33:08Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `ce35818` (this checkpoint is the commit after it)

## Just done
v0.7.6 gate read: lq/ae/v/base/o/ac/rt/pf/last green; W3b (home patch) + 3 artifacts to resolve; AM1 plan stands

## Do this next
1) read the legacy run on tag v0.7.5 (dispatched 07:34; actions_list list_workflow_runs resource_id emulator.yml filter event workflow_dispatch perPage 3, then jobs + get_job_logs tail_lines 70 ONLY: bigger tails are huge); 2) collect reviewers a7e7c5fb1cbb98059 + a716b835fb97dc18c; 3) scope patch geometry/mask/TRANSLUCENT to the lock patch only (patchRect(r, lock), buildPatchMasks only if targetIsLock, lp.format = targetIsLock ? TRANSLUCENT : OPAQUE); 4) delta gate legacy, y2+b4 together, x; 5) ship v0.7.6; 6) AM1 = v0.7.7

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  ce35818 ckpt 477: Tj: stock menu stays stock, patch always in place; photo shows the model patch
  de3d688 ckpt 476: v0.7.6 gate dispatched on 43d6cb4: emulator.yml runs 189 (id 42179486444) lega
  43d6cb4 ckpt 475: version 0.7.6 (code 23) bumped for the gate; unit tests + lint green
  1cbf45c ckpt 474: tools/photo_rectify.py (tested, refuses the repo), HANDOFF state refreshed, CL
  106465d ckpt 473: v0.7.6: patch device profile (tb8168 shift -90 px, +8 %), wallpaper geometry r
  8e6db5b ckpt 472: v0.7.6 WIP: number constants + nudges, card layout, slot-only soft patch; AL1 
  9040fe4 ckpt 471: v0.7.6 WIP: review fixes done (CrashGuard order, saver cap, RESET log, card pr
  013c849 ckpt 470: v0.7.5 RELEASED + RECORDED (run 24, from c0e044d on the branch); v0.7.6 WIP: A
  0cd8ac2 ckpt 469: v0.7.5 build dispatched: android.yml run 24 (id 42176728625) on c0e044d, branc
  c0e044d ckpt 468: intake redacts Recents card titles (v0.7.5 review privacy item); reviews clear
```
