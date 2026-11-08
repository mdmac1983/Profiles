# CHECKPOINT 669 — read me first, then TASKS.md

**Written:** 2026-11-08T03:59:56Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-yhyifc` · **builds on:** `68e1075` (this checkpoint is the commit after it)

## Just done
Tj's 04:00 message written as TASKS BI1 (his go for BG2b (1): the lock-screen logic + wallpaper patch for the UNLOCKED HOME SCREEN); v0.8.8 released earlier (run 35 id 42444134135)

## Do this next
BI1a: read the lock-screen patch code (PatchRest, PatchShot, PatchFade, ScrimFit/ScrimMeasure, AutoCalibration, PatchCalibrator, PatchProfile, cardWanted() in TjAccessibilityService; use a read-only subagent with a NEUTRAL brief for the big read, keep only file:line facts), then write research/HOME_PATCH_2026-11-08.md (design for the unlocked home screen), then build in small switchable steps

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  314ea77 ckpt 668: v0.8.8 RELEASED 03:49 UTC (android.yml run 35 id 42444134135, https://github.c
  d99d60f ckpt 667: v0.8.8 RELEASE build triggered 03:47 UTC: android.yml run 35 id 42444134135 on
  b1399c9 ckpt 666: gated v0.8.8 (code 33) and pushed it: the handoff self-test, the full unit sui
  a3bc971 ckpt 665: v0.8.8 GATE read 03:41 UTC (head 73e65ff, product code final): #342 id 4244234
  0037ea5 ckpt 664: v0.8.8 GATE dispatched 03:21 UTC on 73e65ff (product code final, nq alone #341
  e09cc2b ckpt 663: BH1: v0.8.8 candidate (versionCode 33): version bump, research/NOTIFICATIONS_Q
  f7b6ee0 ckpt 662: BH1 review fixes (8 findings: helper fact in Quickstep, hold state in a Settin
  0095fec ckpt 661: BH1b step 3: emulator section nq (10 checks NQ0-NQ10) + testnotifier high-impo
  75f3927 ckpt 660: BH1b step 2: Launcher control switch + report lines + NotifSightings (10 JVM t
  afef3c6 ckpt 659: BH1b step 1: NotifQuietPolicy + NotifQuietHold (pure) + 29 JVM tests green; No
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
