# CHECKPOINT 420 — read me first, then TASKS.md

**Written:** 2026-11-06T03:07:24Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-22ba09da-s9vh1t` · **builds on:** `76fad6a` (this checkpoint is the commit after it)

## Just done
TK3: gates dispatched on 76fad6a: A=run 169 (id 42154508863), B=run 170 (id 42154512490) incl ad,se; started 03:06Z

## Do this next
at ~03:28Z read both: list_workflow_jobs (resource_id = run id, filter latest, perPage 1) then get_job_logs tail 60/130; rerun only failed sections; code review workflow result goes to TASKS.md; then ship.sh, ff main (MIRROR_MAIN on), android.yml full_build, get_release_by_tag v0.7.4, record-release v0.7.4

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  76fad6a ckpt 419: TK3: merged main (v0.7.3 record), bumped to 0.7.4 / code 21
  1b68379 ckpt 418: TK2 DONE: v0.7.3 released + recorded (main 6e06c39); push.sh/-e .git worktree 
  3d6e2c1 ckpt 417: v0.7.3 PUBLISHED (run 22 id 42153666541 success, both APKs on the Release); NO
  2d41e38 ckpt 416: TK3 prep: ad,se added to gate B lists (CLAUDE.md, HANDOFF.md s9, emulator.yml 
  910ff14 ckpt 415: TK2: merged v0.7.4 branch in, MIRROR_MAIN off; android.yml full_build dispatch
  7d422c9 ckpt 414: handoff #2 written: handoff-launcher.md (repo root, chained from #1); v0.7.3 r
  2d3eead ckpt 413: v0.7.3 gate complete by composition: im (run 167) + x (run 168) green alone on
  21d13e2 ckpt 412: PG3 fix coded (TabletReport.cutAtLine/newestWithin/SUMMARY_MAX 11000 + TabletR
  3cf26c8 ckpt 411: v0.7.3 reruns read: A 120/1 (PG3 summary size), B 107/1 (AA1 im); rt passes; x
  8e27866 ckpt 410: TK1-TK3 recorded in TASKS (takeover request in Tj's words, v0.7.3 finish, v0.7
```
