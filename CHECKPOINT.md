# CHECKPOINT 657 — read me first, then TASKS.md

**Written:** 2026-11-08T02:26:31Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-da0d2646-5yt029` · **builds on:** `95f27b6` (this checkpoint is the commit after it)

## Just done
v0.8.7 RELEASE build triggered 02:26 UTC: android.yml run 34 id 42438532391 on 95f27b6 (full_build=true); gate complete (A #334, B #335, C #336, #337 base,rt, #338 x,n6,cs2 43 PASS, #339 cs2)

## Do this next
when run 34 is green: get_release_by_tag v0.8.7; bash tools/record-release.sh v0.8.7 '<the ship.sh note>'; HANDOFF 0j + TASKS BG1c tick; send Tj the answer to BG1 + BG2 with the Release link

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  95f27b6 ckpt 656: gated v0.8.7 (code 32) and pushed it: the handoff self-test, the full unit sui
  bf90b5a ckpt 655: v0.8.7 gate: {base,rt} #337 41 PASS / 2 FAIL (both in base right after a NATIV
  04de4ff ckpt 654: v0.8.7 gate read: B #335 and C #336 SUCCESS; A #334 {base,e1,x,ac,rt} 103 PASS
  00938b2 ckpt 653: v0.8.7 GATE dispatched 01:41 UTC on f39b3db: #334 id 42435300260 {base,e1,x,ac
  f39b3db ckpt 652: v0.8.7 (code 32) candidate for BG1/HLP1: queued LockLog, apply() on the unlock
  4220460 ckpt 651: BG1 groundwork (unreleased): LockLog.add now queued in order (46 callers incl.
  15334c4 ckpt 650: BG1 (fast, invisible prep of the custom launcher from the lock screen, RAM) an
  66f6f86 ckpt 649: v0.8.6 RELEASED 01:00 UTC (https://github.com/tjshea90/Launcher/releases/tag/v
  3894239 ckpt 648: v0.8.6 RELEASE build triggered 00:58 UTC: android.yml run 33 id 42432136845 on
  9e18849 ckpt 647: gated v0.8.6 (code 31) and pushed it: the handoff self-test, the full unit sui
```
