# CHECKPOINT 732 — read me first, then TASKS.md

**Written:** 2026-11-08T21:55:08Z · **tests:** all 5 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2ax1r4` · **builds on:** `913947f` (this checkpoint is the commit after it)

## Just done
BL1d first cu run #418 id 42526557769 (922d886): 13 PASS 1 FAIL (CU5). Wall kept 1 s, curtain saved 3 s at 600x1024 (layer = full window, no 2nd flash), cover was curtain, touches switch + 1 swallowed tap OK, real home at end. MAE vs bare: caps 2,4,5 = 0.000884 (static sliver), cap 3 = 0.014 and the 2nd tap NOT swallowed (cover life 3.0 s). Added diagnostics (diff bbox, noise floor, tap times, show_touches), re-dispatched cu ~21:55

## Do this next
read the 2nd cu run INFO 'CU C: where the differing pixels sit' + 'CU B: touch indicators': locate the sliver (nav bar? status bar?) and the cap-3 anomaly; fix product or threshold with evidence; then cs, uf, b4 solo (black path), /code-review high, gate, ship v0.8.12

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  922d886 ckpt 731: BL1d: emulator section cu written (calibrate with home_patch+home_curtain ON, 
  3791c9b ckpt 730: BL1c steps 3-6 BUILT (compiles; unit suite running): TAS wiring (full wall kep
  b72bb88 ckpt 729: BL1c step 2 DONE: HomeCurtain.java (store: walls per rot+step, composed curtai
  631f26d ckpt 728: BL1c step 1 DONE: HomeCurtainRules (pure: switch default, whyNotUse, whyNotCap
  d33c369 ckpt 727: BL1a map + BL1b design done and saved: research/BL1_HOME_CURTAIN_2026-11-08.md
  da3032e ckpt 726: BL1 written into TASKS in Tj's words (21:11 UTC): home-looking curtain instead
  bf0615b ckpt 725: v0.8.11 RELEASED + RECORDED (android.yml #38 id 42522618674, 206b3a8; https://
  cad4ed9 ckpt 724: RELEASE BUILD dispatched ~20:56 UTC: android.yml main full_build=true on 206b3
  38f3671 ckpt 723: gated v0.8.11 (code 36) and pushed it: the handoff self-test, the full unit su
  7ae1ed7 ckpt 722: b6 samples #408 id 42519172669 / #409 id 42519175423 (head 1986107): quickstep
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
