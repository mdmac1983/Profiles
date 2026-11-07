# CHECKPOINT 638 — read me first, then TASKS.md

**Written:** 2026-11-07T23:41:20Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-da0d2646-5yt029` · **builds on:** `782a404` (this checkpoint is the commit after it)

## Just done
BF1 (Tj 23:36: Quickstep-only animations off) written into TASKS.md as BF1a-BF1j; design = level-triggered AnimationPolicy + holds in AnimationSpeedup; nothing coded yet

## Do this next
read ControlActivity toggle pattern + Features defaults (clean_start pattern), tools/emulator_scenario.sh section y2 (animations proof), then write AnimationPolicy.java (pure) + AnimationPolicyTest

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  cfadb26 ckpt 637: handoff manifest saved by the Stop hook (.handoff/handoff.* + archive 2026-11-
  4123682 ckpt 636: v0.8.5 RELEASED 23:16 UTC (https://github.com/tjshea90/Launcher/releases/tag/v
  3507251 ckpt 635: RELEASE v0.8.5 triggered: android.yml run 32 id 42424362269 on 239af0c; all ga
  239af0c ckpt 634: gated v0.8.5 (code 30) and pushed it: the handoff self-test, the full unit sui
  016f5e9 ckpt 633: final gate re-run on c4456a9 (app code = 2999c35): #322 id 42422768769 {base,e
  8f46940 ckpt 632: final gate read: G3 green; G1 4 FAIL all in pt (0.75/0.80 scrim state + a late
  1028360 ckpt 631: FINAL GATE dispatched 22:00 UTC on 2999c35: #318 id 42418473647 {x1,y2,b4,pt,u
  2999c35 ckpt 630: version 0.8.5 / code 30 (the release candidate); gate groups chosen from tools
  deaa82c ckpt 629: recentsEmpty: deadline in every branch + card press cap (the #313/#314 hang), 
  aee9fd9 ckpt 628: emulator read #1: ae,ps + pt green; cs found the no-helper Clear-all hole (fix
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
