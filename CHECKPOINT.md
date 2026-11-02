# CHECKPOINT 32 — read me first, then TASKS.md

**Written:** 2026-11-02T14:48:44Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `e4d2b52` (this checkpoint is the commit after it)

## Just done
Added emulator.yml (manual API29 emulator run, screenshots artifact) + tools/emulator_scenario.sh.

## Do this next
Register emulator.yml with GitHub (temporary path-filtered push trigger, see CLAUDE.md), dispatch it, download/inspect screenshots vs Tj's, fix look/crash issues; then release+R8 keep rules; then P3 safety core.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    ?? .github/workflows/emulator.yml
    ?? tools/emulator_scenario.sh

## Last ten checkpoints
```
  e4d2b52 ckpt 31: P1: AOSP Launcher3 Go (r47, no Quickstep) imported under launcher3/, builds as 
  debbfb4 ckpt 16: Keystore generated, committed as signing/launcher-keystore.b64 (+ fingerprint p
  7090841 ckpt 14: Recorded Tj's 4 answers + gesture/power-menu/keyboard/signing tasks (G1-G5).
  0d0b4ac ckpt 12: Researched feasibility: AOSP Launcher3 android-10.0.0_r47 reachable + pinned (t
  d62f513 ckpt 9: Wrote Tj's launcher spec (L1-L11, device details) into TASKS.md before any code.
  d441d13 ckpt 7: android.yml full build verified on GitHub: run green, preview APK artifact (813 
  9b4e49d ckpt 6: Documented the workflow-registration quirk in CLAUDE.md; android.yml full_build 
  a3e4e77 ckpt 5: Registered android.yml with GitHub via a one-off path-filtered push trigger (now
  86b7b54 ckpt 3: Fixed dependabot ignores (AGP/Kotlin/Gradle stay pinned); turned MIRROR_MAIN on 
  f80b634 ckpt 1: Launcher foundation: ladder (inbox/autosave/ckpt/ship), cold-resume briefing, sc
```
