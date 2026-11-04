# CHECKPOINT 273 — read me first, then TASKS.md

**Written:** 2026-11-04T22:31:47Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `f3b4c86` (this checkpoint is the commit after it)

## Just done
Tj: patch must cover the whole 3-button menu, card = top two buttons only; patch may show the wrong part of the wallpaper -> auto-calibrate; keep Quickstep apps open but invisible if ever possible. Written as V3, V4, P2 in TASKS

## Do this next
implement: PatchCalibrator (pure, JVM test) + CAL_SHOT shell job + service integration + patch rect expanded; then P2 research from AOSP source; then section v test with skew, bump 0.6.0, ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  e9b3365 ckpt 272: HANDOFF.md written (single entry point for a no-context Claude account; linked
  953311b ckpt 271: fixed section v helper (vfeat sets lock_patch + power_mode)
  45e6c46 ckpt 270: V1+V2+U1+U2+W1+W2 coded: lock-screen wallpaper patch (invisible until menu ope
  e5c1581 ckpt 269: gated v0.5.0 (code 15) and pushed it: the handoff self-test, the full unit sui
  19a29e4 ckpt 268: Tj explained 'the probe' = the adb commands that show Claude everything about 
  560038e ckpt 267: Tj sent 2 new requests (lock-screen launcher delay; lock-screen wallpaper-patc
  feaae74 ckpt 266: Final full run 68: 90 PASS / 0 FAIL, no native crash, but the job was CUT by t
  7b73448 ckpt 265: Run 67 (sections e1,y2,b6,b4,n,n6,o,t with fixes #2/#3 + ensurehome): ALL PASS
  92b77fc ckpt 264: Run 66 (short, fix #2): N2 still failed because onPad(false) clears the pad BE
  0b229ab ckpt 263: Run 65 (race fix #1): Enter click now arrives but classifies 'none' (keyguard 
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
