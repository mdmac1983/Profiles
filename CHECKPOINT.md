# CHECKPOINT 31 — read me first, then TASKS.md

**Written:** 2026-11-02T14:48:11Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `f6d17d7` (this checkpoint is the commit after it)

## Just done
P1: AOSP Launcher3 Go (r47, no Quickstep) imported under launcher3/, builds as :app (applicationId com.tj.launcher, targetSdk 28), protos via protobuf plugin, plugin_core sources, merged manifest (no INSTALL_SHORTCUT decl, no boot permission). assembleDebug OK -> 10 MB APK. Signed R8 release path verified earlier.

## Do this next
Inspect built manifest with aapt (package, permissions, provider authorities), run release+R8 (proguard keep rules for Launcher3), then P2: emulator screenshots vs Tj's; add CI emulator job. Then P3 safety core.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md
     M app/build.gradle.kts

## Last ten checkpoints
```
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

(14 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
