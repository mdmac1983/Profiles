# CHECKPOINT 734 — read me first, then TASKS.md

**Written:** 2026-11-08T22:11:46Z · **tests:** all 5 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2ax1r4` · **builds on:** `c184dc0` (this checkpoint is the commit after it)

## Just done
BL1d evidence: cu #419/#420 id 42528148176/#421 id 42528151261 all PASS (3 green in a row; #418 glitch not recurred), cs #422, uf #423 PASS = black path unchanged. b4 #424 FAILED FOR NOTHING: solo b4 greps y2-log.txt (gate.sh BIND b4->y2), my dispatch error; gate.sh vs 206b3a8 plans se,hp,rb,last / e1,n,o,x,cu / base,x1,ac,uf,hf (b4 not selected). A stray b4 run on tag v0.8.11 is moot

## Do this next
run /code-review high on the product diff 206b3a8..HEAD (HomeCurtain*, HomeLayer, TjHooks, TAS, ControlActivity, ReportState); fix findings; then bump versionCode 37 / 0.8.12, bash tools/gate.sh 206b3a8 -> dispatch the 3 groups, ship, TABLET_CHECKLIST_v0.8.12.md

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  559db2a ckpt 733: BL1d cu #419 id 42527552708 (913947f) = 14 PASS 0 FAIL: wall kept 2 s, curtain
  66783a5 ckpt 732: BL1d first cu run #418 id 42526557769 (922d886): 13 PASS 1 FAIL (CU5). Wall ke
  922d886 ckpt 731: BL1d: emulator section cu written (calibrate with home_patch+home_curtain ON, 
  3791c9b ckpt 730: BL1c steps 3-6 BUILT (compiles; unit suite running): TAS wiring (full wall kep
  b72bb88 ckpt 729: BL1c step 2 DONE: HomeCurtain.java (store: walls per rot+step, composed curtai
  631f26d ckpt 728: BL1c step 1 DONE: HomeCurtainRules (pure: switch default, whyNotUse, whyNotCap
  d33c369 ckpt 727: BL1a map + BL1b design done and saved: research/BL1_HOME_CURTAIN_2026-11-08.md
  da3032e ckpt 726: BL1 written into TASKS in Tj's words (21:11 UTC): home-looking curtain instead
  bf0615b ckpt 725: v0.8.11 RELEASED + RECORDED (android.yml #38 id 42522618674, 206b3a8; https://
  cad4ed9 ckpt 724: RELEASE BUILD dispatched ~20:56 UTC: android.yml main full_build=true on 206b3
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
