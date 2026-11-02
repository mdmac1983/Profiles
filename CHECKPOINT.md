# CHECKPOINT 98 — read me first, then TASKS.md

**Written:** 2026-11-02T20:26:20Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `eb0c4e8` (this checkpoint is the commit after it)

## Just done
Wrote Tj's HIDE1/LOCK1/SET1/PWR2 requests.

## Do this next
Implement HiddenApps (pure AppVisibility + prefs), AppFilter hook, SessionCommitReceiver + promise-app + remove hooks, HiddenAppsActivity chooser (quickstep-only), ControlActivity guard, tests, emulator check; add key-event diagnostic to the a11y service.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  c39bd5e ckpt 96: Released v0.2.3. Emulator 19/19: reset (apps closed 2/3, notifications cleared,
  9b4b4e6 ckpt 94: gated v0.2.3 (code 5) and pushed it: the handoff self-test, the full unit suite
  0615a14 ckpt 93: pre-ship: v0.2.3: return reset on double-Home (close apps, clear notifications,
  b7f9a01 ckpt 92: Reset test: three test apps + oom-adj diagnostics; asserts at least one app act
  63579b0 ckpt 90: Emulator: reset checks use Contacts (non-persistent) and active-notification li
  118f0dd ckpt 89: RESET1 (ReturnReset: kill bg apps, clear notifications via launcher listener, W
  6075955 ckpt 85: Wrote Tj's 3 new requests (COVER1, RESET1, GEST1); dropped LK6 and the kill-swi
  6223b95 ckpt 83: Released v0.2.2 (8 rows/no dock/no caret, two-button power card with real Resta
  4c4db26 ckpt 81: gated v0.2.2 (code 4) and pushed it: the handoff self-test, the full unit suite
  f5c4fff ckpt 80: pre-ship: v0.2.2: 8 rows no dock, two-button power card, double-Home back to cu
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
