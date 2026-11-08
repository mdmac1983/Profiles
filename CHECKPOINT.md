# CHECKPOINT 733 — read me first, then TASKS.md

**Written:** 2026-11-08T22:02:36Z · **tests:** all 5 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2ax1r4` · **builds on:** `42fc2b1` (this checkpoint is the commit after it)

## Just done
BL1d cu #419 id 42527552708 (913947f) = 14 PASS 0 FAIL: wall kept 2 s, curtain saved 3 s 600x1024, cover life 4.1 s, 4 captures inside: MAE vs bare 0.000888 each (noise floor 1e-7), differing pixels = ONE 582x16+9+5 band at the top (status-bar row), negative control 0.379, taps: first before USER_PRESENT passes through, 3 later swallowed, real home at end. #418 glitch (cap 3 MAE 0.014, 2nd tap not swallowed) did not recur, unexplained. Dispatched ~22:05: cu x2 (main + branch ref, with band stats), cs, uf, b4 (black path)

## Do this next
read the 5 runs (~22:12): cu samples (any anomaly? band stats), cs/uf/b4 all pass (black path unchanged); then /code-review high on faf153d..HEAD product diff, bash tools/gate.sh <v0.8.11 target 206b3a8>, ship v0.8.12 + TABLET_CHECKLIST_v0.8.12.md, tick BL1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  66783a5 ckpt 732: BL1d first cu run #418 id 42526557769 (922d886): 13 PASS 1 FAIL (CU5). Wall ke
  922d886 ckpt 731: BL1d: emulator section cu written (calibrate with home_patch+home_curtain ON, 
  3791c9b ckpt 730: BL1c steps 3-6 BUILT (compiles; unit suite running): TAS wiring (full wall kep
  b72bb88 ckpt 729: BL1c step 2 DONE: HomeCurtain.java (store: walls per rot+step, composed curtai
  631f26d ckpt 728: BL1c step 1 DONE: HomeCurtainRules (pure: switch default, whyNotUse, whyNotCap
  d33c369 ckpt 727: BL1a map + BL1b design done and saved: research/BL1_HOME_CURTAIN_2026-11-08.md
  da3032e ckpt 726: BL1 written into TASKS in Tj's words (21:11 UTC): home-looking curtain instead
  bf0615b ckpt 725: v0.8.11 RELEASED + RECORDED (android.yml #38 id 42522618674, 206b3a8; https://
  cad4ed9 ckpt 724: RELEASE BUILD dispatched ~20:56 UTC: android.yml main full_build=true on 206b3
  38f3671 ckpt 723: gated v0.8.11 (code 36) and pushed it: the handoff self-test, the full unit su
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
