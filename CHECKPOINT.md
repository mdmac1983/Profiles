# CHECKPOINT 731 — read me first, then TASKS.md

**Written:** 2026-11-08T21:42:25Z · **tests:** all 5 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2ax1r4` · **builds on:** `81688a8` (this checkpoint is the commit after it)

## Just done
BL1d: emulator section cu written (calibrate with home_patch+home_curtain ON, wait CURTAIN picture saved, Quickstep session, no helper, plain PIN, screenshots inside the cover life vs bare home MAE, negative control, swallowed tap, real home at the end) + gate-map line + workflow list; handoff self-test 38/0, unit suite 726/0. First cu run dispatched next

## Do this next
read the cu run (RESULTS tail ~60; INFO lines 'CU B' / 'CU C'): CU1/CU2 picture kept+saved, CU3 curtain used, CU5 MAE < 0.01, CU8 swallowed tap; fix what the INFO shows; then solo cs, uf, b4 (black path unchanged), then /code-review high

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  3791c9b ckpt 730: BL1c steps 3-6 BUILT (compiles; unit suite running): TAS wiring (full wall kep
  b72bb88 ckpt 729: BL1c step 2 DONE: HomeCurtain.java (store: walls per rot+step, composed curtai
  631f26d ckpt 728: BL1c step 1 DONE: HomeCurtainRules (pure: switch default, whyNotUse, whyNotCap
  d33c369 ckpt 727: BL1a map + BL1b design done and saved: research/BL1_HOME_CURTAIN_2026-11-08.md
  da3032e ckpt 726: BL1 written into TASKS in Tj's words (21:11 UTC): home-looking curtain instead
  bf0615b ckpt 725: v0.8.11 RELEASED + RECORDED (android.yml #38 id 42522618674, 206b3a8; https://
  cad4ed9 ckpt 724: RELEASE BUILD dispatched ~20:56 UTC: android.yml main full_build=true on 206b3
  38f3671 ckpt 723: gated v0.8.11 (code 36) and pushed it: the handoff self-test, the full unit su
  7ae1ed7 ckpt 722: b6 samples #408 id 42519172669 / #409 id 42519175423 (head 1986107): quickstep
  f945d11 ckpt 721: b6 #407 id 42518553791: BK1b lock phases cover shown at 3 openings in BOTH sto
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
