# CHECKPOINT 49 — read me first, then TASKS.md

**Written:** 2026-11-02T17:46:09Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `b9f19f0` (this checkpoint is the commit after it)

## Just done
Saved Tj's v0.2.0 report + 8 photos (density 170, font scale 1.2, stock 3x4 home/5-col translucent drawer, power menu photo); added LK1-LK4, G3b tasks.

## Do this next
Look pass: read DeviceProfile/InvariantDeviceProfile for allAppsNumCols + hotseat + QSB flag + drawer scrim; edit go/res device_profiles (3x4, 170dpi dp size), disable QSB + hotseat, 5-col drawer, translucent scrim; emulator at density 170 + font_scale 1.2 and compare with STOCK photos.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md
    ?? research/uploads/2026-11-02b/

## Last ten checkpoints
```
  271eb4a ckpt 47: Released v0.2.0 (signed by GitHub Actions, 8.6 MB), recorded in BUILDLOG.
  0efe959 ckpt 45: gated v0.2.0 (code 2) and pushed it: the handoff self-test, the full unit suite
  921312e ckpt 44: Emulator safety scenario fully green. Prepared v0.2.0 (code 2): R8 off, FIRST_I
  54a4b86 ckpt 43: Emulator scenario harness fixes (force-stop after editing prefs; home between c
  eff4fa4 ckpt 41: Emulator scenario now asserts safety: manifest has no admin/overlay/keyguard/bo
  b772b25 ckpt 40: P3 core: RouterActivity (only HOME activity, fail-open to stock), Mode, StockLa
  799512e ckpt 35: Wrote DESIGN section 8: no-adb feature map, router-activity pass-through design
  e5ce4aa ckpt 32: Added emulator.yml (manual API29 emulator run, screenshots artifact) + tools/em
  e4d2b52 ckpt 31: P1: AOSP Launcher3 Go (r47, no Quickstep) imported under launcher3/, builds as 
  debbfb4 ckpt 16: Keystore generated, committed as signing/launcher-keystore.b64 (+ fingerprint p
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
