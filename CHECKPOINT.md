# CHECKPOINT 659 — read me first, then TASKS.md

**Written:** 2026-11-08T02:55:58Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-yhyifc` · **builds on:** `98a5882` (this checkpoint is the commit after it)

## Just done
BH1b step 1: NotifQuietPolicy + NotifQuietHold (pure) + 29 JVM tests green; NotifQuiet adapter hooked into AnimationSpeedup.syncNow (shared unlockUndecided), RestoreStore restores it

## Do this next
BH1b step 2: Launcher control toggle + note (ControlActivity near line 143, release ON / debug OFF), ReportState line, then NotifSightings (tj_accessibility.xml typeNotificationStateChanged + early return in TjAccessibilityService.onAccessibilityEvent so no power-menu scan runs for it), ReturnReset shade step on a sighting, then emulator section nq

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  d2136fb ckpt 658: v0.8.7 RECORDED (run 34 id 42438532391 green, Release published); took over fr
  6d26926 ckpt 657: v0.8.7 RELEASE build triggered 02:26 UTC: android.yml run 34 id 42438532391 on
  95f27b6 ckpt 656: gated v0.8.7 (code 32) and pushed it: the handoff self-test, the full unit sui
  bf90b5a ckpt 655: v0.8.7 gate: {base,rt} #337 41 PASS / 2 FAIL (both in base right after a NATIV
  04de4ff ckpt 654: v0.8.7 gate read: B #335 and C #336 SUCCESS; A #334 {base,e1,x,ac,rt} 103 PASS
  00938b2 ckpt 653: v0.8.7 GATE dispatched 01:41 UTC on f39b3db: #334 id 42435300260 {base,e1,x,ac
  f39b3db ckpt 652: v0.8.7 (code 32) candidate for BG1/HLP1: queued LockLog, apply() on the unlock
  4220460 ckpt 651: BG1 groundwork (unreleased): LockLog.add now queued in order (46 callers incl.
  15334c4 ckpt 650: BG1 (fast, invisible prep of the custom launcher from the lock screen, RAM) an
  66f6f86 ckpt 649: v0.8.6 RELEASED 01:00 UTC (https://github.com/tjshea90/Launcher/releases/tag/v
```

(11 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
