# CHECKPOINT 281 — read me first, then TASKS.md

**Written:** 2026-11-04T23:12:19Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-8b1c3755-1tp147` · **builds on:** `1f239f5` (this checkpoint is the commit after it)

## Just done
gated v0.6.0 (code 16) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.6.0 "v0.6.0: unlock stopwatch + compile-ahead job (U1/U2), lock-screen power-menu patch as a photo of the real lock screen with a two-button card (V1-V4), extended probe (W1/W2), PhoneGuide phone app (Y2-Y4)"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  1e6ba2a ckpt 280: Run 74 (v): U1/U2/V1/V2-closed PASS; g (run 75) PASS. V4/V2-freed FAIL because
  e91b6a5 ckpt 279: Runs 72 (v) and 73 (g) read from job logs (artifact curl is blocked by the sim
  445eb1a ckpt 278: CUT-OFF SAVE (usage nearly out): HANDOFF.md section 0b written with exact stat
  80e19d2 ckpt 277: HANDOFF.md: phone guide row, Tj's next step, open requests list; ADB_PHONE.txt
  f2fdff4 ckpt 276: Run 71 (v): card area OK ([485,357][596,511] = 2 stock items vs full rect 303 
  498ad58 ckpt 275: Tj asked for a PHONE-side adb guide app (module phoneguide) with copy buttons,
  2d6e5f9 ckpt 274: V3 (patch = learned rect + 12dp margin, card = top two items), V4 (PatchCalibr
  aa72200 ckpt 273: Tj: patch must cover the whole 3-button menu, card = top two buttons only; pat
  e9b3365 ckpt 272: HANDOFF.md written (single entry point for a no-context Claude account; linked
  953311b ckpt 271: fixed section v helper (vfeat sets lock_patch + power_mode)
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
