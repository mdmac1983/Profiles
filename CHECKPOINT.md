# CHECKPOINT 728 — read me first, then TASKS.md

**Written:** 2026-11-08T21:32:06Z · **tests:** all 5 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2ax1r4` · **builds on:** `301999b` (this checkpoint is the commit after it)

## Just done
BL1c step 1 DONE: HomeCurtainRules (pure: switch default, whyNotUse, whyNotCapture, unlockConfirmed, hash fold/blank/changed, file+meta names) + HomeCurtainRulesTest 19 tests green (GRADLE_EXIT=0, XML tests=19 failures=0). Draw listener (TjHooks.watchHomeDraw -> TAS.homeDrew) is installed on every Launcher resume: usable as capture trigger

## Do this next
BL1c step 2: write HomeCurtain.java (store: saveWall/findWallStep/hasWall/loadWall per rot+step, saveCurtain/meta/loadCurtain per rot, in-memory cache, dropCache, invalidateAll; PNG tmp+rename, pref meta, prune on new wallpaper id) modelled on HomeShot.java; then step 3 TAS hooks (save full wall in homeShotObserved before full.recycle(), capture runnable off homeDrew, showResetCover ImageView + touchable at applyUnlockChoice)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  d33c369 ckpt 727: BL1a map + BL1b design done and saved: research/BL1_HOME_CURTAIN_2026-11-08.md
  da3032e ckpt 726: BL1 written into TASKS in Tj's words (21:11 UTC): home-looking curtain instead
  bf0615b ckpt 725: v0.8.11 RELEASED + RECORDED (android.yml #38 id 42522618674, 206b3a8; https://
  cad4ed9 ckpt 724: RELEASE BUILD dispatched ~20:56 UTC: android.yml main full_build=true on 206b3
  38f3671 ckpt 723: gated v0.8.11 (code 36) and pushed it: the handoff self-test, the full unit su
  7ae1ed7 ckpt 722: b6 samples #408 id 42519172669 / #409 id 42519175423 (head 1986107): quickstep
  f945d11 ckpt 721: b6 #407 id 42518553791: BK1b lock phases cover shown at 3 openings in BOTH sto
  f8e6d37 ckpt 720: scenario-only edits on main 540e4e5: cover_resumes helper (UF2 + CS-B5 cover-a
  6e87f60 ckpt 719: TRIAGE DONE. Solo runs on main: uf #400 (UF2 FAIL, same order as TAG v0.8.10 #
  6e0299f ckpt 718: GATE #397 read: 99 PASS 1 FAIL = UF2 (stock HOME .Launcher resumed after the E
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
