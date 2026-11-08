# CHECKPOINT 729 — read me first, then TASKS.md

**Written:** 2026-11-08T21:34:05Z · **tests:** all 5 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2ax1r4` · **builds on:** `de6b11b` (this checkpoint is the commit after it)

## Just done
BL1c step 2 DONE: HomeCurtain.java (store: walls per rot+step, composed curtain per rot, memory cache never recycled while maybe shown, preload, invalidateAll) compiles (HomeCurtain.class built)

## Do this next
BL1c step 3: TAS wiring in this order: (A) homeShotObserved keeps full wall via HomeCurtain.saveWall before full.recycle(); (B) maybeHomeCalibrate hasCrop override when wall missing; (C) curtain capture runnable off homeDrew (HomeLayer.copy full window, worker: hash/blank/changed/compose/save/setCache); (D) showResetCover ImageView + touchable only after USER_PRESENT (curtainSwallowTouches in applyUnlockChoice); (E) onTrimMemory dropCache + invalidateAll callers + preload at onServiceConnected; (F) switch/Control line/report key

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  631f26d ckpt 728: BL1c step 1 DONE: HomeCurtainRules (pure: switch default, whyNotUse, whyNotCap
  d33c369 ckpt 727: BL1a map + BL1b design done and saved: research/BL1_HOME_CURTAIN_2026-11-08.md
  da3032e ckpt 726: BL1 written into TASKS in Tj's words (21:11 UTC): home-looking curtain instead
  bf0615b ckpt 725: v0.8.11 RELEASED + RECORDED (android.yml #38 id 42522618674, 206b3a8; https://
  cad4ed9 ckpt 724: RELEASE BUILD dispatched ~20:56 UTC: android.yml main full_build=true on 206b3
  38f3671 ckpt 723: gated v0.8.11 (code 36) and pushed it: the handoff self-test, the full unit su
  7ae1ed7 ckpt 722: b6 samples #408 id 42519172669 / #409 id 42519175423 (head 1986107): quickstep
  f945d11 ckpt 721: b6 #407 id 42518553791: BK1b lock phases cover shown at 3 openings in BOTH sto
  f8e6d37 ckpt 720: scenario-only edits on main 540e4e5: cover_resumes helper (UF2 + CS-B5 cover-a
  6e87f60 ckpt 719: TRIAGE DONE. Solo runs on main: uf #400 (UF2 FAIL, same order as TAG v0.8.10 #
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
