# CHECKPOINT 77 — read me first, then TASKS.md

**Written:** 2026-11-02T18:56:41Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `032dc02` (this checkpoint is the commit after it)

## Just done
BACK1: double-press Home in Quickstep mode returns to the custom launcher (router rule + tests + emulator check + FIRST_INSTALL).

## Do this next
Check the in-flight emulator run (card/Restart diagnostics), then dispatch again to include the double-Home check; then fix Restart press; ship v0.2.2 with: 8 rows no dock, two-button card, double-Home back.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md
     M app/src/main/java/com/tj/launcher/Mode.java
     M app/src/main/java/com/tj/launcher/RouterActivity.java
     M research/FIRST_INSTALL.md
     M tools/emulator_scenario.sh
    ?? app/src/main/java/com/tj/launcher/HomeDoublePress.java
    ?? app/src/test/java/com/tj/launcher/HomeDoublePressTest.java

## Last ten checkpoints
```
  16a2407 ckpt 74: Card buttons rebuilt (icon+label stack, 12sp), touch/click/press logging, emula
  3b4ddc2 ckpt 73: No dock (8 rows, hotseat removed, caret hidden even with accessibility), stock 
  d5cce69 ckpt 66: Released v0.2.1 (Go icon size, 5-col grid/drawer, 50% scrim, opt-in power-menu 
  72fedad ckpt 64: gated v0.2.1 (code 3) and pushed it: the handoff self-test, the full unit suite
  8647f99 ckpt 63: pre-ship: v0.2.1: Go icon size, 5-col grid/drawer, 50% drawer scrim (measured),
  69a5177 ckpt 62: Power-menu cover widened to the panel column + colour tuned; scenario reordered
  3f157bf ckpt 60: Look fixes from measurement (icon 60dp, text 14sp, drawer scrim 50% white); ser
  ac02e74 ckpt 58: Fixed power-menu service feedback loop (ignore own events, debounce, no re-layo
  1f2e3e9 ckpt 55: Power-menu helper: TjAccessibilityService (cover Screenshot entry / block menu,
  c3955b4 ckpt 52: Look pass 1: grid 5x7 + 5-icon dock (derived from stock photo geometry), no hom
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
