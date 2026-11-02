# CHECKPOINT 79 — read me first, then TASKS.md

**Written:** 2026-11-02T19:09:24Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `50ef2f3` (this checkpoint is the commit after it)

## Just done
Fixed card self-click loop (skip own windows) and router onNewIntent for fast double Home; harness uses 0.3 s gap.

## Do this next
Dispatch emulator.yml; expect double-Home PASS and Restart pressed the real button (press 'Restart' -> true); then compare 01-home/03-drawer with STOCK and ship v0.2.2.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M app/src/main/java/com/tj/launcher/RouterActivity.java
     M app/src/main/java/com/tj/launcher/TjAccessibilityService.java
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  50ef2f3 ckpt 78: Scenario saves the full TjA11y log + input-dispatch windows after the Restart t
  fd34f91 ckpt 77: BACK1: double-press Home in Quickstep mode returns to the custom launcher (rout
  16a2407 ckpt 74: Card buttons rebuilt (icon+label stack, 12sp), touch/click/press logging, emula
  3b4ddc2 ckpt 73: No dock (8 rows, hotseat removed, caret hidden even with accessibility), stock 
  d5cce69 ckpt 66: Released v0.2.1 (Go icon size, 5-col grid/drawer, 50% scrim, opt-in power-menu 
  72fedad ckpt 64: gated v0.2.1 (code 3) and pushed it: the handoff self-test, the full unit suite
  8647f99 ckpt 63: pre-ship: v0.2.1: Go icon size, 5-col grid/drawer, 50% drawer scrim (measured),
  69a5177 ckpt 62: Power-menu cover widened to the panel column + colour tuned; scenario reordered
  3f157bf ckpt 60: Look fixes from measurement (icon 60dp, text 14sp, drawer scrim 50% white); ser
  ac02e74 ckpt 58: Fixed power-menu service feedback loop (ignore own events, debounce, no re-layo
```
