# CHECKPOINT 648 — read me first, then TASKS.md

**Written:** 2026-11-08T00:58:21Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-da0d2646-5yt029` · **builds on:** `9e18849` (this checkpoint is the commit after it)

## Just done
v0.8.6 RELEASE build triggered 00:58 UTC: android.yml run 33 id 42432136845 on 9e18849 (full_build=true); gate complete on the same product code

## Do this next
when run 33 is green: get_release_by_tag v0.8.6; bash tools/record-release.sh v0.8.6 '<the note ship.sh printed>' ; HANDOFF 0i + TASKS BF1h; tell Tj with the Release link

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  9e18849 ckpt 647: gated v0.8.6 (code 31) and pushed it: the handoff self-test, the full unit sui
  709626c ckpt 646: BF1 RE-RUNS dispatched 00:46 UTC on 0bded84 (product code = 72f12a6): #331 id 
  0bded84 ckpt 645: BF1 gate read: A,D green; B 3 FAIL (AP1 rg order-sensitive AT2, AN2 x2 leftove
  c620c81 ckpt 644: BF1 gate on 72f12a6: A #327 {e1,x,lq,uf,rt,pf} SUCCESS (job 128573071749); D #
  f82ba6e ckpt 643: BF1 GATE dispatched 00:15 UTC on 72f12a6: #327 id 42428867199 A{e1,x,lq,uf,rt,
  72f12a6 ckpt 642: BF1 v0.8.6 (code 31) candidate: AnimationHolds state machine extracted + 16 te
  a668ab9 ckpt 641: BF1: emulator run #326 id 42428062827 {an} dispatched 00:04 UTC on 24eeab1 (ck
  24eeab1 ckpt 640: BF1 review fixes in (code-review high: 6 findings, all valid): reconcile at pr
  2063d5d ckpt 639: BF1 built: AnimationPolicy (pure) + holds/level-triggered sync in AnimationSpe
  6ae5170 ckpt 638: BF1 (Tj 23:36: Quickstep-only animations off) written into TASKS.md as BF1a-BF
```
