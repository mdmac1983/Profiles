# CHECKPOINT 94 — read me first, then TASKS.md

**Written:** 2026-11-02T20:20:07Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `0615a14` (this checkpoint is the commit after it)

## Just done
gated v0.2.3 (code 5) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.2.3 "v0.2.3: return reset on double-Home (close apps, clear notifications, Wi-Fi/BT off, open Solitaire), more responsive twist gesture, faster power-menu cover"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  0615a14 ckpt 93: pre-ship: v0.2.3: return reset on double-Home (close apps, clear notifications,
  b7f9a01 ckpt 92: Reset test: three test apps + oom-adj diagnostics; asserts at least one app act
  63579b0 ckpt 90: Emulator: reset checks use Contacts (non-persistent) and active-notification li
  118f0dd ckpt 89: RESET1 (ReturnReset: kill bg apps, clear notifications via launcher listener, W
  6075955 ckpt 85: Wrote Tj's 3 new requests (COVER1, RESET1, GEST1); dropped LK6 and the kill-swi
  6223b95 ckpt 83: Released v0.2.2 (8 rows/no dock/no caret, two-button power card with real Resta
  4c4db26 ckpt 81: gated v0.2.2 (code 4) and pushed it: the handoff self-test, the full unit suite
  f5c4fff ckpt 80: pre-ship: v0.2.2: 8 rows no dock, two-button power card, double-Home back to cu
  bd56ec9 ckpt 79: Fixed card self-click loop (skip own windows) and router onNewIntent for fast d
  50ef2f3 ckpt 78: Scenario saves the full TjA11y log + input-dispatch windows after the Restart t
```
