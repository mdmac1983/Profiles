# CHECKPOINT 642 — read me first, then TASKS.md

**Written:** 2026-11-08T00:14:35Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-da0d2646-5yt029` · **builds on:** `8148eeb` (this checkpoint is the commit after it)

## Just done
BF1 v0.8.6 (code 31) candidate: AnimationHolds state machine extracted + 16 tests, reconcile thread hop, AN5/AN7 stable reads, #326 {an} 17 PASS on 24eeab1; tier0 622 tests 0 fail

## Do this next
dispatch gate on this head: A e1,x,lq,uf,rt,pf | B x1,n,o,ac,bl,rg,an,last | C base,y2,b6,b4,n6,t,bs,cs,cs2,rb | D pt solo; read each (get_job_logs tail 60); then ship.sh + android.yml full_build

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  a668ab9 ckpt 641: BF1: emulator run #326 id 42428062827 {an} dispatched 00:04 UTC on 24eeab1 (ck
  24eeab1 ckpt 640: BF1 review fixes in (code-review high: 6 findings, all valid): reconcile at pr
  2063d5d ckpt 639: BF1 built: AnimationPolicy (pure) + holds/level-triggered sync in AnimationSpe
  6ae5170 ckpt 638: BF1 (Tj 23:36: Quickstep-only animations off) written into TASKS.md as BF1a-BF
  cfadb26 ckpt 637: handoff manifest saved by the Stop hook (.handoff/handoff.* + archive 2026-11-
  4123682 ckpt 636: v0.8.5 RELEASED 23:16 UTC (https://github.com/tjshea90/Launcher/releases/tag/v
  3507251 ckpt 635: RELEASE v0.8.5 triggered: android.yml run 32 id 42424362269 on 239af0c; all ga
  239af0c ckpt 634: gated v0.8.5 (code 30) and pushed it: the handoff self-test, the full unit sui
  016f5e9 ckpt 633: final gate re-run on c4456a9 (app code = 2999c35): #322 id 42422768769 {base,e
  8f46940 ckpt 632: final gate read: G3 green; G1 4 FAIL all in pt (0.75/0.80 scrim state + a late
```

(13 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
