# CHECKPOINT 131 — read me first, then TASKS.md

**Written:** 2026-11-02T23:29:08Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `9892812` (this checkpoint is the commit after it)

## Just done
gated v0.3.1 (code 7) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.3.1 "v0.3.1: more responsive twist, keyboard switch (adb grant), faster reset, launcher out of Recents, Solitaire at launcher start"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  9892812 ckpt 130: pre-ship: v0.3.1: more responsive twist, keyboard switch (adb grant), faster r
  bb1a50c ckpt 129: Fixes: double-Home window 0.9 s, router sets EXCLUDE_FROM_RECENTS on our Launc
  09e27d4 ckpt 128: V1-V5 built: gesture 18 deg/looser pinch/min 80px; keyboard switch (KeyboardPo
  0bdbd14 ckpt 122: Wrote Tj's v0.3.0 feedback V1-V5.
  81dda79 ckpt 120: Released v0.3.0. Emulator 25/26 before release (only the Recents check, whose 
  10c8d10 ckpt 118: gated v0.3.0 (code 6) and pushed it: the handoff self-test, the full unit suit
  e3d4abd ckpt 117: pre-ship: v0.3.0: hide apps + hidden new installs, lock settings to Quickstep 
  c67ddb7 ckpt 116: Emulator 25/26 PASS (hide apps, search, reinstall=hidden, lock, reset2 notific
  c04c44e ckpt 115: SOL1: reset no longer presses HOME (it raced the app launch on the slow tablet
  c88c0ac ckpt 112: SW1: three independent switches in Launcher control (A launch flag, B1 shade C
```
