# CHECKPOINT 276 — read me first, then TASKS.md

**Written:** 2026-11-04T22:43:58Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `c0e1b37` (this checkpoint is the commit after it)

## Just done
Run 71 (v): card area OK ([485,357][596,511] = 2 stock items vs full rect 303 high), U1 13 ms, compile ok; BUT the lock patch stayed VISIBLE with the menu closed (opaque window ignores alpha 0): it covered notifications and the calibration screenshot photographed the patch itself (error 0.0000). Fix: TRANSLUCENT window for the lock patch + pull cal.png into the artifact. Phone guide app (module phoneguide, steps.txt, android.yml builds+attaches PhoneGuide APK, guide tests + handoff checks, emulator section g)

## Do this next
dispatch sections v,g (parallel strings: 'v' and 'g'); read: patch invisible when closed MAE<0.5%, V4 calibrated with skew, freed-slot MAE; guide opens; then bump 0.6.0 (versionCode 16) and ship; HANDOFF final

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M app/src/main/java/com/tj/launcher/TjAccessibilityService.java
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  498ad58 ckpt 275: Tj asked for a PHONE-side adb guide app (module phoneguide) with copy buttons,
  2d6e5f9 ckpt 274: V3 (patch = learned rect + 12dp margin, card = top two items), V4 (PatchCalibr
  aa72200 ckpt 273: Tj: patch must cover the whole 3-button menu, card = top two buttons only; pat
  e9b3365 ckpt 272: HANDOFF.md written (single entry point for a no-context Claude account; linked
  953311b ckpt 271: fixed section v helper (vfeat sets lock_patch + power_mode)
  45e6c46 ckpt 270: V1+V2+U1+U2+W1+W2 coded: lock-screen wallpaper patch (invisible until menu ope
  e5c1581 ckpt 269: gated v0.5.0 (code 15) and pushed it: the handoff self-test, the full unit sui
  19a29e4 ckpt 268: Tj explained 'the probe' = the adb commands that show Claude everything about 
  560038e ckpt 267: Tj sent 2 new requests (lock-screen launcher delay; lock-screen wallpaper-patc
  feaae74 ckpt 266: Final full run 68: 90 PASS / 0 FAIL, no native crash, but the job was CUT by t
```

(6 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
