# CHECKPOINT 90 — read me first, then TASKS.md

**Written:** 2026-11-02T20:02:00Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `118f0dd` (this checkpoint is the commit after it)

## Just done
Emulator: reset checks use Contacts (non-persistent) and active-notification list only; cover appears within 0.3 s of the long-press.

## Do this next
Dispatch emulator.yml; if all PASS ship v0.2.3 (return reset, gesture swallow, faster cover).

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  118f0dd ckpt 89: RESET1 (ReturnReset: kill bg apps, clear notifications via launcher listener, W
  6075955 ckpt 85: Wrote Tj's 3 new requests (COVER1, RESET1, GEST1); dropped LK6 and the kill-swi
  6223b95 ckpt 83: Released v0.2.2 (8 rows/no dock/no caret, two-button power card with real Resta
  4c4db26 ckpt 81: gated v0.2.2 (code 4) and pushed it: the handoff self-test, the full unit suite
  f5c4fff ckpt 80: pre-ship: v0.2.2: 8 rows no dock, two-button power card, double-Home back to cu
  bd56ec9 ckpt 79: Fixed card self-click loop (skip own windows) and router onNewIntent for fast d
  50ef2f3 ckpt 78: Scenario saves the full TjA11y log + input-dispatch windows after the Restart t
  fd34f91 ckpt 77: BACK1: double-press Home in Quickstep mode returns to the custom launcher (rout
  16a2407 ckpt 74: Card buttons rebuilt (icon+label stack, 12sp), touch/click/press logging, emula
  3b4ddc2 ckpt 73: No dock (8 rows, hotseat removed, caret hidden even with accessibility), stock 
```
