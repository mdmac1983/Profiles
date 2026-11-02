# CHECKPOINT 9 — read me first, then TASKS.md

**Written:** 2026-11-02T14:27:45Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `18651d0` (this checkpoint is the commit after it)

## Just done
Wrote Tj's launcher spec (L1-L11, device details) into TASKS.md before any code.

## Do this next
Probe whether AOSP Launcher3/Quickstep android10 source is reachable (android.googlesource.com), decide base (port Launcher3 Go vs from scratch), write research/DESIGN doc incl. safety/failsafe architecture and what cannot be done without root/adb; then build step 1 = safe pass-through HOME app.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  d441d13 ckpt 7: android.yml full build verified on GitHub: run green, preview APK artifact (813 
  9b4e49d ckpt 6: Documented the workflow-registration quirk in CLAUDE.md; android.yml full_build 
  a3e4e77 ckpt 5: Registered android.yml with GitHub via a one-off path-filtered push trigger (now
  86b7b54 ckpt 3: Fixed dependabot ignores (AGP/Kotlin/Gradle stay pinned); turned MIRROR_MAIN on 
  f80b634 ckpt 1: Launcher foundation: ladder (inbox/autosave/ckpt/ship), cold-resume briefing, sc
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
