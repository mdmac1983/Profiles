# CHECKPOINT 129 — read me first, then TASKS.md

**Written:** 2026-11-02T23:17:37Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `09e27d4` (this checkpoint is the commit after it)

## Just done
Fixes: double-Home window 0.9 s, router sets EXCLUDE_FROM_RECENTS on our Launcher, Recents poll timeout 1.8 s, keyboard test via the in-app Mode.set path.

## Do this next
Dispatch emulator.yml; expect double Home PASS, launcher not in Recents, keyboard log line; then ship v0.3.1.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M app/src/main/java/com/tj/launcher/HomeDoublePress.java
     M app/src/main/java/com/tj/launcher/RouterActivity.java
     M app/src/main/java/com/tj/launcher/TjAccessibilityService.java
     M app/src/test/java/com/tj/launcher/HomeDoublePressTest.java
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  09e27d4 ckpt 128: V1-V5 built: gesture 18 deg/looser pinch/min 80px; keyboard switch (KeyboardPo
  0bdbd14 ckpt 122: Wrote Tj's v0.3.0 feedback V1-V5.
  81dda79 ckpt 120: Released v0.3.0. Emulator 25/26 before release (only the Recents check, whose 
  10c8d10 ckpt 118: gated v0.3.0 (code 6) and pushed it: the handoff self-test, the full unit suit
  e3d4abd ckpt 117: pre-ship: v0.3.0: hide apps + hidden new installs, lock settings to Quickstep 
  c67ddb7 ckpt 116: Emulator 25/26 PASS (hide apps, search, reinstall=hidden, lock, reset2 notific
  c04c44e ckpt 115: SOL1: reset no longer presses HOME (it raced the app launch on the slow tablet
  c88c0ac ckpt 112: SW1: three independent switches in Launcher control (A launch flag, B1 shade C
  8b3859b ckpt 109: REC1 built: RecentsPolicy adds FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS to every lau
  8c9345b ckpt 106: RESET2 built: TjAccessibilityService.runReset (open shade -> press Clear all; 
```
