# CHECKPOINT 47 — read me first, then TASKS.md

**Written:** 2026-11-02T15:16:01Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `6c35ead` (this checkpoint is the commit after it)

## Just done
Released v0.2.0 (signed by GitHub Actions, 8.6 MB), recorded in BUILDLOG.

## Do this next
Wait for Tj's report from the tablet (home look, recents button, router, gestures). Meanwhile: look pass (needs his display density: Settings > Display > Display size, or adb shell wm density), then L2/L6/L3 (hide apps, hide new installs, clone Settings) -> v0.3; kill switch (button + CCW gesture + volume keys); power-menu Screenshot hide; keyboard switch.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  0efe959 ckpt 45: gated v0.2.0 (code 2) and pushed it: the handoff self-test, the full unit suite
  921312e ckpt 44: Emulator safety scenario fully green. Prepared v0.2.0 (code 2): R8 off, FIRST_I
  54a4b86 ckpt 43: Emulator scenario harness fixes (force-stop after editing prefs; home between c
  eff4fa4 ckpt 41: Emulator scenario now asserts safety: manifest has no admin/overlay/keyguard/bo
  b772b25 ckpt 40: P3 core: RouterActivity (only HOME activity, fail-open to stock), Mode, StockLa
  799512e ckpt 35: Wrote DESIGN section 8: no-adb feature map, router-activity pass-through design
  e5ce4aa ckpt 32: Added emulator.yml (manual API29 emulator run, screenshots artifact) + tools/em
  e4d2b52 ckpt 31: P1: AOSP Launcher3 Go (r47, no Quickstep) imported under launcher3/, builds as 
  debbfb4 ckpt 16: Keystore generated, committed as signing/launcher-keystore.b64 (+ fingerprint p
  7090841 ckpt 14: Recorded Tj's 4 answers + gesture/power-menu/keyboard/signing tasks (G1-G5).
```
