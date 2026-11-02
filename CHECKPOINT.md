# CHECKPOINT 41 — read me first, then TASKS.md

**Written:** 2026-11-02T14:58:28Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `b772b25` (this checkpoint is the commit after it)

## Just done
Emulator scenario now asserts safety: manifest has no admin/overlay/keyguard/boot/secure-settings perms, router is HOME, custom<->quickstep routing, crash breaker after 3 crashes, uninstall leaves stock launcher. Earlier run showed the port running on API 29 with the stock-style drawer.

## Do this next
Dispatch emulator.yml on the branch, read results.txt + screenshots, fix failures. Then: tools/adb-grant.sh + adb-restore.sh, look pass (3x4 home / 5-col drawer / no search bar: need Tj's display density), kill switch, release v0.2.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  b772b25 ckpt 40: P3 core: RouterActivity (only HOME activity, fail-open to stock), Mode, StockLa
  799512e ckpt 35: Wrote DESIGN section 8: no-adb feature map, router-activity pass-through design
  e5ce4aa ckpt 32: Added emulator.yml (manual API29 emulator run, screenshots artifact) + tools/em
  e4d2b52 ckpt 31: P1: AOSP Launcher3 Go (r47, no Quickstep) imported under launcher3/, builds as 
  debbfb4 ckpt 16: Keystore generated, committed as signing/launcher-keystore.b64 (+ fingerprint p
  7090841 ckpt 14: Recorded Tj's 4 answers + gesture/power-menu/keyboard/signing tasks (G1-G5).
  0d0b4ac ckpt 12: Researched feasibility: AOSP Launcher3 android-10.0.0_r47 reachable + pinned (t
  d62f513 ckpt 9: Wrote Tj's launcher spec (L1-L11, device details) into TASKS.md before any code.
  d441d13 ckpt 7: android.yml full build verified on GitHub: run green, preview APK artifact (813 
  9b4e49d ckpt 6: Documented the workflow-registration quirk in CLAUDE.md; android.yml full_build 
```
