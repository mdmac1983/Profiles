# CHECKPOINT 112 — read me first, then TASKS.md

**Written:** 2026-11-02T20:38:11Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `078d841` (this checkpoint is the commit after it)

## Just done
SW1: three independent switches in Launcher control (A launch flag, B1 shade Clear all, B2 Recents Clear all); runReset(notif, recents, done).

## Do this next
Read the in-flight emulator run (REC1 commit): hide apps, lock, reset2, recents-policy checks; then dispatch a final run on this commit (switches), fix, ship v0.3.0.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  8b3859b ckpt 109: REC1 built: RecentsPolicy adds FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS to every lau
  8c9345b ckpt 106: RESET2 built: TjAccessibilityService.runReset (open shade -> press Clear all; 
  ee74728 ckpt 104: Wrote Tj's v0.2.3 report (apps not closed, no notification access on Go) + RES
  50c240f ckpt 102: Researched cloning Settings (AOSP Settings r47: 95 MB, 2,917 files, sharedUser
  33afdd3 ckpt 101: HIDE1/LOCK1: AppVisibility (pure+tests), HiddenApps prefs glue, AppFilter/Sess
  ae2f0ba ckpt 98: Wrote Tj's HIDE1/LOCK1/SET1/PWR2 requests.
  c39bd5e ckpt 96: Released v0.2.3. Emulator 19/19: reset (apps closed 2/3, notifications cleared,
  9b4b4e6 ckpt 94: gated v0.2.3 (code 5) and pushed it: the handoff self-test, the full unit suite
  0615a14 ckpt 93: pre-ship: v0.2.3: return reset on double-Home (close apps, clear notifications,
  b7f9a01 ckpt 92: Reset test: three test apps + oom-adj diagnostics; asserts at least one app act
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
