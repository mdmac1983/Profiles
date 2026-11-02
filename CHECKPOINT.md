# CHECKPOINT 116 — read me first, then TASKS.md

**Written:** 2026-11-02T20:51:34Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `c04c44e` (this checkpoint is the commit after it)

## Just done
Emulator 25/26 PASS (hide apps, search, reinstall=hidden, lock, reset2 notification cleared, Solitaire/Calendar opened). Fixed the Recents test (stale tasks): uses never-launched apps Messaging/Clock.

## Do this next
Dispatch emulator.yml; if the recents checks pass ship v0.3.0; power-key diagnostic is inconclusive (injected keys bypass the accessibility filter).

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  c04c44e ckpt 115: SOL1: reset no longer presses HOME (it raced the app launch on the slow tablet
  c88c0ac ckpt 112: SW1: three independent switches in Launcher control (A launch flag, B1 shade C
  8b3859b ckpt 109: REC1 built: RecentsPolicy adds FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS to every lau
  8c9345b ckpt 106: RESET2 built: TjAccessibilityService.runReset (open shade -> press Clear all; 
  ee74728 ckpt 104: Wrote Tj's v0.2.3 report (apps not closed, no notification access on Go) + RES
  50c240f ckpt 102: Researched cloning Settings (AOSP Settings r47: 95 MB, 2,917 files, sharedUser
  33afdd3 ckpt 101: HIDE1/LOCK1: AppVisibility (pure+tests), HiddenApps prefs glue, AppFilter/Sess
  ae2f0ba ckpt 98: Wrote Tj's HIDE1/LOCK1/SET1/PWR2 requests.
  c39bd5e ckpt 96: Released v0.2.3. Emulator 19/19: reset (apps closed 2/3, notifications cleared,
  9b4b4e6 ckpt 94: gated v0.2.3 (code 5) and pushed it: the handoff self-test, the full unit suite
```
