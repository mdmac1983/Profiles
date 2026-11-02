# CHECKPOINT 89 — read me first, then TASKS.md

**Written:** 2026-11-02T19:51:40Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `162c60a` (this checkpoint is the commit after it)

## Just done
RESET1 (ReturnReset: kill bg apps, clear notifications via launcher listener, Wi-Fi/BT off, open Solitaire by label; hooked into double-Home; Control screen switch+status), GEST1 (swallow workspace scroll while two fingers down, 30 deg, min distance), COVER1 (leading-edge eval, cached card rect shown before node scan, notificationTimeout 0); unit tests + emulator reset checks.

## Do this next
Dispatch emulator.yml; read results (reset checks, 15-card-immediately screenshot); fix; then ship v0.2.3.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  6075955 ckpt 85: Wrote Tj's 3 new requests (COVER1, RESET1, GEST1); dropped LK6 and the kill-swi
  6223b95 ckpt 83: Released v0.2.2 (8 rows/no dock/no caret, two-button power card with real Resta
  4c4db26 ckpt 81: gated v0.2.2 (code 4) and pushed it: the handoff self-test, the full unit suite
  f5c4fff ckpt 80: pre-ship: v0.2.2: 8 rows no dock, two-button power card, double-Home back to cu
  bd56ec9 ckpt 79: Fixed card self-click loop (skip own windows) and router onNewIntent for fast d
  50ef2f3 ckpt 78: Scenario saves the full TjA11y log + input-dispatch windows after the Restart t
  fd34f91 ckpt 77: BACK1: double-press Home in Quickstep mode returns to the custom launcher (rout
  16a2407 ckpt 74: Card buttons rebuilt (icon+label stack, 12sp), touch/click/press logging, emula
  3b4ddc2 ckpt 73: No dock (8 rows, hotseat removed, caret hidden even with accessibility), stock 
  d5cce69 ckpt 66: Released v0.2.1 (Go icon size, 5-col grid/drawer, 50% scrim, opt-in power-menu 
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
