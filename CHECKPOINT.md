# CHECKPOINT 81 — read me first, then TASKS.md

**Written:** 2026-11-02T19:17:34Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `f5c4fff` (this checkpoint is the commit after it)

## Just done
gated v0.2.2 (code 4) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.2.2 "v0.2.2: 8 rows no dock, two-button power card, double-Home back to custom launcher"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  f5c4fff ckpt 80: pre-ship: v0.2.2: 8 rows no dock, two-button power card, double-Home back to cu
  bd56ec9 ckpt 79: Fixed card self-click loop (skip own windows) and router onNewIntent for fast d
  50ef2f3 ckpt 78: Scenario saves the full TjA11y log + input-dispatch windows after the Restart t
  fd34f91 ckpt 77: BACK1: double-press Home in Quickstep mode returns to the custom launcher (rout
  16a2407 ckpt 74: Card buttons rebuilt (icon+label stack, 12sp), touch/click/press logging, emula
  3b4ddc2 ckpt 73: No dock (8 rows, hotseat removed, caret hidden even with accessibility), stock 
  d5cce69 ckpt 66: Released v0.2.1 (Go icon size, 5-col grid/drawer, 50% scrim, opt-in power-menu 
  72fedad ckpt 64: gated v0.2.1 (code 3) and pushed it: the handoff self-test, the full unit suite
  8647f99 ckpt 63: pre-ship: v0.2.1: Go icon size, 5-col grid/drawer, 50% drawer scrim (measured),
  69a5177 ckpt 62: Power-menu cover widened to the panel column + colour tuned; scenario reordered
```
