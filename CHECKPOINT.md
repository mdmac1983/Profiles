# CHECKPOINT 12 — read me first, then TASKS.md

**Written:** 2026-11-02T14:30:22Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `86350aa` (this checkpoint is the commit after it)

## Just done
Researched feasibility: AOSP Launcher3 android-10.0.0_r47 reachable + pinned (tools/fetch_launcher3.sh); wrote research/DESIGN_2026-11-02.md (port plan, honest feature table, safety architecture, staged releases), updated BRIEF + TASKS plan; saved Tj's 7 screenshots.

## Do this next
P1: new Gradle module for the Launcher3 Go port: run tools/fetch_launcher3.sh, build plugin_core from SystemUI-plugin src, protos via gradle protobuf plugin, stub @hide APIs until l3goWithoutQuickstep compiles; run heavy builds in background / on CI. Ask Tj the OPEN questions in BRIEF.md (gesture, adb/PC access, power-menu expectations, keystore).

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M BRIEF.md
     M CHECKPOINT.md
     M TASKS.md
    ?? research/DESIGN_2026-11-02.md

## Last ten checkpoints
```
  d62f513 ckpt 9: Wrote Tj's launcher spec (L1-L11, device details) into TASKS.md before any code.
  d441d13 ckpt 7: android.yml full build verified on GitHub: run green, preview APK artifact (813 
  9b4e49d ckpt 6: Documented the workflow-registration quirk in CLAUDE.md; android.yml full_build 
  a3e4e77 ckpt 5: Registered android.yml with GitHub via a one-off path-filtered push trigger (now
  86b7b54 ckpt 3: Fixed dependabot ignores (AGP/Kotlin/Gradle stay pinned); turned MIRROR_MAIN on 
  f80b634 ckpt 1: Launcher foundation: ladder (inbox/autosave/ckpt/ship), cold-resume briefing, sc
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
