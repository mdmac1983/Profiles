# CHECKPOINT 647 — read me first, then TASKS.md

**Written:** 2026-11-08T00:57:38Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-da0d2646-5yt029` · **builds on:** `7f5f2a8` (this checkpoint is the commit after it)

## Just done
gated v0.8.6 (code 31) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.8.6 "animations are off ONLY inside an unlocked Quickstep session (the stock home and the apps opened there) and are back on the instant the screen goes off, on the lock screen and its power menu, after a plain PIN and in the custom launcher; put back exactly as they were; new switch in Launcher control > Everyday (default ON)"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  709626c ckpt 646: BF1 RE-RUNS dispatched 00:46 UTC on 0bded84 (product code = 72f12a6): #331 id 
  0bded84 ckpt 645: BF1 gate read: A,D green; B 3 FAIL (AP1 rg order-sensitive AT2, AN2 x2 leftove
  c620c81 ckpt 644: BF1 gate on 72f12a6: A #327 {e1,x,lq,uf,rt,pf} SUCCESS (job 128573071749); D #
  f82ba6e ckpt 643: BF1 GATE dispatched 00:15 UTC on 72f12a6: #327 id 42428867199 A{e1,x,lq,uf,rt,
  72f12a6 ckpt 642: BF1 v0.8.6 (code 31) candidate: AnimationHolds state machine extracted + 16 te
  a668ab9 ckpt 641: BF1: emulator run #326 id 42428062827 {an} dispatched 00:04 UTC on 24eeab1 (ck
  24eeab1 ckpt 640: BF1 review fixes in (code-review high: 6 findings, all valid): reconcile at pr
  2063d5d ckpt 639: BF1 built: AnimationPolicy (pure) + holds/level-triggered sync in AnimationSpe
  6ae5170 ckpt 638: BF1 (Tj 23:36: Quickstep-only animations off) written into TASKS.md as BF1a-BF
  cfadb26 ckpt 637: handoff manifest saved by the Stop hook (.handoff/handoff.* + archive 2026-11-
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
