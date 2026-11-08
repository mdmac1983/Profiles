# CHECKPOINT 653 — read me first, then TASKS.md

**Written:** 2026-11-08T01:41:44Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-da0d2646-5yt029` · **builds on:** `f39b3db` (this checkpoint is the commit after it)

## Just done
v0.8.7 GATE dispatched 01:41 UTC on f39b3db: #334 id 42435300260 {base,e1,x,ac,rt} | #335 id 42435302613 {x1,o,uf,an,last} | #336 id 42435304467 {n,n6,cs2,rb}; TASKS BG1a/b BG2a ticked, BG1c / BG2b waiting

## Do this next
read the 3 runs in ~14 min (list_workflow_jobs -> job id -> get_job_logs tail 70; FAILs first); then ship.sh, android.yml full_build, get_release_by_tag v0.8.7, record-release.sh, HANDOFF, answer Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  f39b3db ckpt 652: v0.8.7 (code 32) candidate for BG1/HLP1: queued LockLog, apply() on the unlock
  4220460 ckpt 651: BG1 groundwork (unreleased): LockLog.add now queued in order (46 callers incl.
  15334c4 ckpt 650: BG1 (fast, invisible prep of the custom launcher from the lock screen, RAM) an
  66f6f86 ckpt 649: v0.8.6 RELEASED 01:00 UTC (https://github.com/tjshea90/Launcher/releases/tag/v
  3894239 ckpt 648: v0.8.6 RELEASE build triggered 00:58 UTC: android.yml run 33 id 42432136845 on
  9e18849 ckpt 647: gated v0.8.6 (code 31) and pushed it: the handoff self-test, the full unit sui
  709626c ckpt 646: BF1 RE-RUNS dispatched 00:46 UTC on 0bded84 (product code = 72f12a6): #331 id 
  0bded84 ckpt 645: BF1 gate read: A,D green; B 3 FAIL (AP1 rg order-sensitive AT2, AN2 x2 leftove
  c620c81 ckpt 644: BF1 gate on 72f12a6: A #327 {e1,x,lq,uf,rt,pf} SUCCESS (job 128573071749); D #
  f82ba6e ckpt 643: BF1 GATE dispatched 00:15 UTC on 72f12a6: #327 id 42428867199 A{e1,x,lq,uf,rt,
```
