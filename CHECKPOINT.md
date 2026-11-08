# CHECKPOINT 652 — read me first, then TASKS.md

**Written:** 2026-11-08T01:40:51Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-da0d2646-5yt029` · **builds on:** `dee291c` (this checkpoint is the commit after it)

## Just done
v0.8.7 (code 32) candidate for BG1/HLP1: queued LockLog, apply() on the unlock path, ProcessLife, HelperLife + ShellServer flight recorder, report lines + x checks; research/FAST_UNLOCK_AND_HOME_POWERMENU_2026-11-08.md (BG1 answers, BG2 map from the subagent + photo, solution space, recommendation); tier0 640 tests green

## Do this next
dispatch gate groups from gate.sh 9e18849: A base,e1,x,ac,rt | B x1,o,uf,an,last | C n,n6,cs2,rb; read each; then answer Tj (BG1 + BG2 + the decisions), ship.sh + android.yml full_build

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  4220460 ckpt 651: BG1 groundwork (unreleased): LockLog.add now queued in order (46 callers incl.
  15334c4 ckpt 650: BG1 (fast, invisible prep of the custom launcher from the lock screen, RAM) an
  66f6f86 ckpt 649: v0.8.6 RELEASED 01:00 UTC (https://github.com/tjshea90/Launcher/releases/tag/v
  3894239 ckpt 648: v0.8.6 RELEASE build triggered 00:58 UTC: android.yml run 33 id 42432136845 on
  9e18849 ckpt 647: gated v0.8.6 (code 31) and pushed it: the handoff self-test, the full unit sui
  709626c ckpt 646: BF1 RE-RUNS dispatched 00:46 UTC on 0bded84 (product code = 72f12a6): #331 id 
  0bded84 ckpt 645: BF1 gate read: A,D green; B 3 FAIL (AP1 rg order-sensitive AT2, AN2 x2 leftove
  c620c81 ckpt 644: BF1 gate on 72f12a6: A #327 {e1,x,lq,uf,rt,pf} SUCCESS (job 128573071749); D #
  f82ba6e ckpt 643: BF1 GATE dispatched 00:15 UTC on 72f12a6: #327 id 42428867199 A{e1,x,lq,uf,rt,
  72f12a6 ckpt 642: BF1 v0.8.6 (code 31) candidate: AnimationHolds state machine extracted + 16 te
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
