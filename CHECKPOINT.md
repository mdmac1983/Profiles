# CHECKPOINT 484 — read me first, then TASKS.md

**Written:** 2026-11-06T07:53:32Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `f5f7235` (this checkpoint is the commit after it)

## Just done
v0.7.6 RELEASE BUILD RUNNING: android.yml run 42183756211 (Build APK #25) dispatched 07:52Z on branch claude/new-session-7a9vpi, head f5f7235, full_build=true

## Do this next
1) wait for run 42183756211 (actions_get get_workflow_run; typically 8-12 min after 07:52Z), then get_release_by_tag v0.7.6; 2) bash tools/record-release.sh v0.7.6 <the ship note: number matches stock with steppers; lock screen quiet; soft-edge slot patch aligned per tablet; battery-saver cap/breaker + review fixes>; 3) give Tj the Release link as plain text on its own line; 4) AM1: git merge am1 (local worktree branch at /home/user/Launcher-am1, or git apply research/pending/am1.patch), then emulator.yml section ps; 5) AN1 battery saver tile, AO1 wallpaper picker (see TASKS.md tail)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  f5f7235 ckpt 483: gated v0.7.6 (code 23) and pushed it: the handoff self-test, the full unit sui
  13b914b ckpt 482: HANDOFF READY: AM1 saved as research/pending/am1.patch; AN1 battery saver tile
  930e398 ckpt 481: AM1 coded in the local worktree /home/user/Launcher-am1 (421 tests); v0.7.6 de
  274b1f5 ckpt 480: v0.7.6 delta gate dispatched on 7d1837b: runs 193 (id 42182087395) legacy,y2,b
  7d1837b ckpt 479: v0.7.6: both reviews done (no blockers), fixes applied, home patch back to v0.
  693b097 ckpt 478: v0.7.6 gate read: lq/ae/v/base/o/ac/rt/pf/last green; W3b (home patch) + 3 art
  ce35818 ckpt 477: Tj: stock menu stays stock, patch always in place; photo shows the model patch
  de3d688 ckpt 476: v0.7.6 gate dispatched on 43d6cb4: emulator.yml runs 189 (id 42179486444) lega
  43d6cb4 ckpt 475: version 0.7.6 (code 23) bumped for the gate; unit tests + lint green
  1cbf45c ckpt 474: tools/photo_rectify.py (tested, refuses the repo), HANDOFF state refreshed, CL
```
