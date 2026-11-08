# CHECKPOINT 645 — read me first, then TASKS.md

**Written:** 2026-11-08T00:45:47Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-da0d2646-5yt029` · **builds on:** `b525320` (this checkpoint is the commit after it)

## Just done
BF1 gate read: A,D green; B 3 FAIL (AP1 rg order-sensitive AT2, AN2 x2 leftover state), C 5 FAIL (4 in base after a NATIVE CRASH line, CS-B2); an section hardened (crash prefs, wake, unlock, facts INFO); product code unchanged since 72f12a6

## Do this next
dispatch re-runs: {base}, {cs}, {rg,an}; read each; then ship.sh + android.yml full_build if green

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  c620c81 ckpt 644: BF1 gate on 72f12a6: A #327 {e1,x,lq,uf,rt,pf} SUCCESS (job 128573071749); D #
  f82ba6e ckpt 643: BF1 GATE dispatched 00:15 UTC on 72f12a6: #327 id 42428867199 A{e1,x,lq,uf,rt,
  72f12a6 ckpt 642: BF1 v0.8.6 (code 31) candidate: AnimationHolds state machine extracted + 16 te
  a668ab9 ckpt 641: BF1: emulator run #326 id 42428062827 {an} dispatched 00:04 UTC on 24eeab1 (ck
  24eeab1 ckpt 640: BF1 review fixes in (code-review high: 6 findings, all valid): reconcile at pr
  2063d5d ckpt 639: BF1 built: AnimationPolicy (pure) + holds/level-triggered sync in AnimationSpe
  6ae5170 ckpt 638: BF1 (Tj 23:36: Quickstep-only animations off) written into TASKS.md as BF1a-BF
  cfadb26 ckpt 637: handoff manifest saved by the Stop hook (.handoff/handoff.* + archive 2026-11-
  4123682 ckpt 636: v0.8.5 RELEASED 23:16 UTC (https://github.com/tjshea90/Launcher/releases/tag/v
  3507251 ckpt 635: RELEASE v0.8.5 triggered: android.yml run 32 id 42424362269 on 239af0c; all ga
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
