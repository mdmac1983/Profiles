# CHECKPOINT 40 — read me first, then TASKS.md

**Written:** 2026-11-02T14:57:25Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `d2275ea` (this checkpoint is the commit after it)

## Just done
P3 core: RouterActivity (only HOME activity, fail-open to stock), Mode, StockLauncher, CrashGuard (3 crashes/2 min -> Quickstep + restore), RestoreStore, Features, TwoFingerRotation (CW -> Quickstep), ControlActivity, Launcher3 hooks. 12 JVM tests green, debug APK builds (11.5 MB).

## Do this next
Check emulator run (dispatched on the branch) for screenshots; add guard test (no admin/keyguard/overlay/lock settings in manifest+sources), tools/adb-grant.sh + adb-restore.sh, extend emulator_scenario.sh (router, mode flip, forced crash x3, restore).

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md
     M launcher3/PATCHES.md

## Last ten checkpoints
```
  799512e ckpt 35: Wrote DESIGN section 8: no-adb feature map, router-activity pass-through design
  e5ce4aa ckpt 32: Added emulator.yml (manual API29 emulator run, screenshots artifact) + tools/em
  e4d2b52 ckpt 31: P1: AOSP Launcher3 Go (r47, no Quickstep) imported under launcher3/, builds as 
  debbfb4 ckpt 16: Keystore generated, committed as signing/launcher-keystore.b64 (+ fingerprint p
  7090841 ckpt 14: Recorded Tj's 4 answers + gesture/power-menu/keyboard/signing tasks (G1-G5).
  0d0b4ac ckpt 12: Researched feasibility: AOSP Launcher3 android-10.0.0_r47 reachable + pinned (t
  d62f513 ckpt 9: Wrote Tj's launcher spec (L1-L11, device details) into TASKS.md before any code.
  d441d13 ckpt 7: android.yml full build verified on GitHub: run green, preview APK artifact (813 
  9b4e49d ckpt 6: Documented the workflow-registration quirk in CLAUDE.md; android.yml full_build 
  a3e4e77 ckpt 5: Registered android.yml with GitHub via a one-off path-filtered push trigger (now
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
