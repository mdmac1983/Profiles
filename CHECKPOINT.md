# CHECKPOINT 104 — read me first, then TASKS.md

**Written:** 2026-11-02T20:30:53Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `4b47164` (this checkpoint is the commit after it)

## Just done
Wrote Tj's v0.2.3 report (apps not closed, no notification access on Go) + RESET2/REC1.

## Do this next
Read hide-apps emulator run; then implement RESET2: TjAccessibilityService.runReset (shade Clear all -> recents Clear all -> home), router uses it when the service is on, remove notification-access UI; emulator check for the shade clear + recents clear; ship v0.3.0.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  50c240f ckpt 102: Researched cloning Settings (AOSP Settings r47: 95 MB, 2,917 files, sharedUser
  33afdd3 ckpt 101: HIDE1/LOCK1: AppVisibility (pure+tests), HiddenApps prefs glue, AppFilter/Sess
  ae2f0ba ckpt 98: Wrote Tj's HIDE1/LOCK1/SET1/PWR2 requests.
  c39bd5e ckpt 96: Released v0.2.3. Emulator 19/19: reset (apps closed 2/3, notifications cleared,
  9b4b4e6 ckpt 94: gated v0.2.3 (code 5) and pushed it: the handoff self-test, the full unit suite
  0615a14 ckpt 93: pre-ship: v0.2.3: return reset on double-Home (close apps, clear notifications,
  b7f9a01 ckpt 92: Reset test: three test apps + oom-adj diagnostics; asserts at least one app act
  63579b0 ckpt 90: Emulator: reset checks use Contacts (non-persistent) and active-notification li
  118f0dd ckpt 89: RESET1 (ReturnReset: kill bg apps, clear notifications via launcher listener, W
  6075955 ckpt 85: Wrote Tj's 3 new requests (COVER1, RESET1, GEST1); dropped LK6 and the kill-swi
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
