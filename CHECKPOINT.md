# CHECKPOINT 118 — read me first, then TASKS.md

**Written:** 2026-11-02T20:52:34Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `e3d4abd` (this checkpoint is the commit after it)

## Just done
gated v0.3.0 (code 6) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.3.0 "v0.3.0: hide apps + hidden new installs, lock settings to Quickstep mode, Recents only Solitaire (launch flag), Clear-all switches via accessibility, Solitaire glitch fix"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  e3d4abd ckpt 117: pre-ship: v0.3.0: hide apps + hidden new installs, lock settings to Quickstep 
  c67ddb7 ckpt 116: Emulator 25/26 PASS (hide apps, search, reinstall=hidden, lock, reset2 notific
  c04c44e ckpt 115: SOL1: reset no longer presses HOME (it raced the app launch on the slow tablet
  c88c0ac ckpt 112: SW1: three independent switches in Launcher control (A launch flag, B1 shade C
  8b3859b ckpt 109: REC1 built: RecentsPolicy adds FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS to every lau
  8c9345b ckpt 106: RESET2 built: TjAccessibilityService.runReset (open shade -> press Clear all; 
  ee74728 ckpt 104: Wrote Tj's v0.2.3 report (apps not closed, no notification access on Go) + RES
  50c240f ckpt 102: Researched cloning Settings (AOSP Settings r47: 95 MB, 2,917 files, sharedUser
  33afdd3 ckpt 101: HIDE1/LOCK1: AppVisibility (pure+tests), HiddenApps prefs glue, AppFilter/Sess
  ae2f0ba ckpt 98: Wrote Tj's HIDE1/LOCK1/SET1/PWR2 requests.
```
