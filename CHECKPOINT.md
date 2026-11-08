# CHECKPOINT 671 — read me first, then TASKS.md

**Written:** 2026-11-08T04:30:41Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-yhyifc` · **builds on:** `e24eeab` (this checkpoint is the commit after it)

## Just done
BI1b step 1: HomePatchRules (pure) + HomePatchRulesTest 19 green

## Do this next
BI1b step 2: HomeShot (store: homeshot_r<rot>_x<step>.png + meta, probe step +-2, prune stale) + HomeLayer (PixelCopy of the Launcher window via TjHooks.launcherRef + SRC_OVER compose); then TjHooks (launcherRef, draw listener on DragLayer, touch down, homeAtRest facts: AbstractFloatingView.getTopOpenView, DragController.isDragging, Workspace.isSwitchingState); then TAS integration

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  1a97981 ckpt 670: BI1a design written (research/HOME_PATCH_2026-11-08.md: wallpaper-only calibra
  1ca0e18 ckpt 669: Tj's 04:00 message written as TASKS BI1 (his go for BG2b (1): the lock-screen 
  314ea77 ckpt 668: v0.8.8 RELEASED 03:49 UTC (android.yml run 35 id 42444134135, https://github.c
  d99d60f ckpt 667: v0.8.8 RELEASE build triggered 03:47 UTC: android.yml run 35 id 42444134135 on
  b1399c9 ckpt 666: gated v0.8.8 (code 33) and pushed it: the handoff self-test, the full unit sui
  a3bc971 ckpt 665: v0.8.8 GATE read 03:41 UTC (head 73e65ff, product code final): #342 id 4244234
  0037ea5 ckpt 664: v0.8.8 GATE dispatched 03:21 UTC on 73e65ff (product code final, nq alone #341
  e09cc2b ckpt 663: BH1: v0.8.8 candidate (versionCode 33): version bump, research/NOTIFICATIONS_Q
  f7b6ee0 ckpt 662: BH1 review fixes (8 findings: helper fact in Quickstep, hold state in a Settin
  0095fec ckpt 661: BH1b step 3: emulator section nq (10 checks NQ0-NQ10) + testnotifier high-impo
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
