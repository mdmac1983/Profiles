# CHECKPOINT 73 — read me first, then TASKS.md

**Written:** 2026-11-02T18:46:30Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `a6948a7` (this checkpoint is the commit after it)

## Just done
No dock (8 rows, hotseat removed, caret hidden even with accessibility), stock reference photos stored permanently (research/reference/stock/INDEX.md + CLAUDE.md + resume line), power-menu TWO-BUTTON replacement card (Power off/Restart press the real buttons; fail-open), scenario last step taps Restart.

## Do this next
Dispatch emulator.yml; compare 01-home/03-drawer (rows 113 px, no dock) with stock-home/stock-drawer; check 11/14 card screenshots + press-restart.txt; then ship v0.2.2.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  d5cce69 ckpt 66: Released v0.2.1 (Go icon size, 5-col grid/drawer, 50% scrim, opt-in power-menu 
  72fedad ckpt 64: gated v0.2.1 (code 3) and pushed it: the handoff self-test, the full unit suite
  8647f99 ckpt 63: pre-ship: v0.2.1: Go icon size, 5-col grid/drawer, 50% drawer scrim (measured),
  69a5177 ckpt 62: Power-menu cover widened to the panel column + colour tuned; scenario reordered
  3f157bf ckpt 60: Look fixes from measurement (icon 60dp, text 14sp, drawer scrim 50% white); ser
  ac02e74 ckpt 58: Fixed power-menu service feedback loop (ignore own events, debounce, no re-layo
  1f2e3e9 ckpt 55: Power-menu helper: TjAccessibilityService (cover Screenshot entry / block menu,
  c3955b4 ckpt 52: Look pass 1: grid 5x7 + 5-icon dock (derived from stock photo geometry), no hom
  8495c8f ckpt 49: Saved Tj's v0.2.0 report + 8 photos (density 170, font scale 1.2, stock 3x4 hom
  271eb4a ckpt 47: Released v0.2.0 (signed by GitHub Actions, 8.6 MB), recorded in BUILDLOG.
```

(6 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
