# CHECKPOINT 482 — read me first, then TASKS.md

**Written:** 2026-11-06T07:50:02Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `19613c6` (this checkpoint is the commit after it)

## Just done
HANDOFF READY: AM1 saved as research/pending/am1.patch; AN1 battery saver tile + AO1 wallpaper picker written down; v0.7.6 final, delta gate 193/194 to read

## Do this next
COLD START (Tj's chat context was full): 1) bash tools/install-hooks.sh; read TASKS.md tail (AM1, AN1, AO1) and HANDOFF.md 0d. 2) SHIP v0.7.6 FIRST: product code is final on this branch (version 0.7.6 code 23); read the two delta emulator runs 193 (id 42182087395: legacy,y2,b4,x) and 194 (id 42182090690: base,lq,rb) on 7d1837b with actions_list list_workflow_jobs (resource_id = run id, filter latest, perPage 1) then get_job_logs tail_lines 40 ONLY (bigger tails are huge). Expected: legacy fails only reset2 (known open item RS1), W3b PASSES (home patch is back on its v0.7.5 path; v0.7.5 itself passes W3b), b4 passes together with y2, x passes (intake accepts UserHandle -1). If green: bash ship.sh <note: number matches the stock one with size/weight/brightness steppers; lock screen quiet; slot patch with soft edges + tablet alignment; battery saver + review fixes>, dispatch android.yml full_build=true ON THIS BRANCH (ref claude/new-session-7a9vpi; main is NOT advanced on purpose), get_release_by_tag v0.7.6, bash tools/record-release.sh v0.7.6 <note>, give Tj the Release link as plain text. 3) AM1 = v0.7.7: git apply research/pending/am1.patch, git rm that patch, bash tools/gradle.sh testDebugUnitTest :app:lintDebug (expect 421 tests green), commit, dispatch emulator.yml sections ps FIRST (never run: the service's own screenshot, MediaStore observer, tap closes the menu), fix what it shows, then bash tools/gate.sh <v0.7.6 commit> for the rest (ae, v, last, x1, base, legacy because older writers now carry power_card true), ship v0.7.7, tell Tj the calibration steps (Launcher control > Calibrate the patch, lock + wake, unlock). 4) AN1 and AO1 per TASKS.md.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M HANDOFF.md
     M TASKS.md
    ?? research/pending/am1.patch

## Last ten checkpoints
```
  930e398 ckpt 481: AM1 coded in the local worktree /home/user/Launcher-am1 (421 tests); v0.7.6 de
  274b1f5 ckpt 480: v0.7.6 delta gate dispatched on 7d1837b: runs 193 (id 42182087395) legacy,y2,b
  7d1837b ckpt 479: v0.7.6: both reviews done (no blockers), fixes applied, home patch back to v0.
  693b097 ckpt 478: v0.7.6 gate read: lq/ae/v/base/o/ac/rt/pf/last green; W3b (home patch) + 3 art
  ce35818 ckpt 477: Tj: stock menu stays stock, patch always in place; photo shows the model patch
  de3d688 ckpt 476: v0.7.6 gate dispatched on 43d6cb4: emulator.yml runs 189 (id 42179486444) lega
  43d6cb4 ckpt 475: version 0.7.6 (code 23) bumped for the gate; unit tests + lint green
  1cbf45c ckpt 474: tools/photo_rectify.py (tested, refuses the repo), HANDOFF state refreshed, CL
  106465d ckpt 473: v0.7.6: patch device profile (tb8168 shift -90 px, +8 %), wallpaper geometry r
  8e6db5b ckpt 472: v0.7.6 WIP: number constants + nudges, card layout, slot-only soft patch; AL1 
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
