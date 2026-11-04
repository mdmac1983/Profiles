# CHECKPOINT 190 — read me first, then TASKS.md

**Written:** 2026-11-04T01:04:06Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `fb9c70d` (this checkpoint is the commit after it)

## Just done
gated v0.3.5 (code 11) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.3.5 "v0.3.5: editable lock-screen number, in-app adb setup guide + permissions declared in advance, Quickstep started as a home-type task (never in Recents)"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  fb9c70d ckpt 189: pre-ship: v0.3.5: editable lock-screen number, in-app adb setup guide + permis
  711e74a ckpt 188: record-release.sh now refuses when build.gradle versionName != the tag (preven
  95e73c5 ckpt 187: BUILDLOG fix: v0.3.4 is code 10 (record-release read the already-bumped 0.3.5/
  5f5a1f4 ckpt 186: RELEASED v0.3.4 (Build APK green, Release published, signed, 9.17 MB; emulator
  7046504 ckpt 185: v0.3.5 coded: C1 EditText for the lock number (+sanitize tests), C2 AdbGuideAc
  9af18f5 ckpt 184: v0.3.4 gated (code 10) and pushed; android.yml full_build dispatched on main. 
  fce25f5 ckpt 183: gated v0.3.4 (code 10) and pushed it: the handoff self-test, the full unit sui
  35c97ca ckpt 182: Research notes for C4/C5/C6/C7 in research/NOTES_2026-11-04.md
  94c2ea0 ckpt 181: C3: found the AOSP ICS leaf wallpapers (phone 960x800, tablet 1920x1280, andro
  9feddd9 ckpt 180: C2 docs: research/ADB_COMMANDS.txt (line by line, sections 0-10 incl. C7 brows
```
