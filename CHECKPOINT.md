# CHECKPOINT 643 — read me first, then TASKS.md

**Written:** 2026-11-08T00:15:25Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-da0d2646-5yt029` · **builds on:** `72f12a6` (this checkpoint is the commit after it)

## Just done
BF1 GATE dispatched 00:15 UTC on 72f12a6: #327 id 42428867199 A{e1,x,lq,uf,rt,pf} | #328 id 42428869417 B{x1,n,o,ac,bl,rg,an,last} | #329 id 42428871514 C{base,y2,b6,b4,n6,t,bs,cs,cs2,rb} | #330 id 42428873714 D{pt}

## Do this next
read A and D after ~17 min, B and C after ~28 min: list_workflow_jobs resource_id=<run id> -> get_job_logs tail_lines 60 (pt: FAIL lines only). All green -> bash ship.sh then android.yml full_build=true on the branch, get_release_by_tag v0.8.6, record-release.sh

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  72f12a6 ckpt 642: BF1 v0.8.6 (code 31) candidate: AnimationHolds state machine extracted + 16 te
  a668ab9 ckpt 641: BF1: emulator run #326 id 42428062827 {an} dispatched 00:04 UTC on 24eeab1 (ck
  24eeab1 ckpt 640: BF1 review fixes in (code-review high: 6 findings, all valid): reconcile at pr
  2063d5d ckpt 639: BF1 built: AnimationPolicy (pure) + holds/level-triggered sync in AnimationSpe
  6ae5170 ckpt 638: BF1 (Tj 23:36: Quickstep-only animations off) written into TASKS.md as BF1a-BF
  cfadb26 ckpt 637: handoff manifest saved by the Stop hook (.handoff/handoff.* + archive 2026-11-
  4123682 ckpt 636: v0.8.5 RELEASED 23:16 UTC (https://github.com/tjshea90/Launcher/releases/tag/v
  3507251 ckpt 635: RELEASE v0.8.5 triggered: android.yml run 32 id 42424362269 on 239af0c; all ga
  239af0c ckpt 634: gated v0.8.5 (code 30) and pushed it: the handoff self-test, the full unit sui
  016f5e9 ckpt 633: final gate re-run on c4456a9 (app code = 2999c35): #322 id 42422768769 {base,e
```
