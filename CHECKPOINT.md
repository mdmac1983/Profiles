# CHECKPOINT 655 — read me first, then TASKS.md

**Written:** 2026-11-08T02:21:11Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-da0d2646-5yt029` · **builds on:** `eb7a556` (this checkpoint is the commit after it)

## Just done
v0.8.7 gate: {base,rt} #337 41 PASS / 2 FAIL (both in base right after a NATIVE CRASH line; base was clean in #334; rt PASSES now); {x,n6,cs2} #338 stalled ~8 min in cs2 (infra), cancelled: 43 PASS 0 FAIL (recorder line shows a real heartbeat); cs2 re-run alone dispatched

## Do this next
read the cs2 re-run, then bash ship.sh, android.yml full_build=true on the branch, get_release_by_tag v0.8.7, record-release.sh, HANDOFF 0j, answer Tj (BG1 + BG2 + decisions)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  04de4ff ckpt 654: v0.8.7 gate read: B #335 and C #336 SUCCESS; A #334 {base,e1,x,ac,rt} 103 PASS
  00938b2 ckpt 653: v0.8.7 GATE dispatched 01:41 UTC on f39b3db: #334 id 42435300260 {base,e1,x,ac
  f39b3db ckpt 652: v0.8.7 (code 32) candidate for BG1/HLP1: queued LockLog, apply() on the unlock
  4220460 ckpt 651: BG1 groundwork (unreleased): LockLog.add now queued in order (46 callers incl.
  15334c4 ckpt 650: BG1 (fast, invisible prep of the custom launcher from the lock screen, RAM) an
  66f6f86 ckpt 649: v0.8.6 RELEASED 01:00 UTC (https://github.com/tjshea90/Launcher/releases/tag/v
  3894239 ckpt 648: v0.8.6 RELEASE build triggered 00:58 UTC: android.yml run 33 id 42432136845 on
  9e18849 ckpt 647: gated v0.8.6 (code 31) and pushed it: the handoff self-test, the full unit sui
  709626c ckpt 646: BF1 RE-RUNS dispatched 00:46 UTC on 0bded84 (product code = 72f12a6): #331 id 
  0bded84 ckpt 645: BF1 gate read: A,D green; B 3 FAIL (AP1 rg order-sensitive AT2, AN2 x2 leftove
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
