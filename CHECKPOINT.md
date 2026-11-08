# CHECKPOINT 658 — read me first, then TASKS.md

**Written:** 2026-11-08T02:48:09Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-yhyifc` · **builds on:** `95756cd` (this checkpoint is the commit after it)

## Just done
v0.8.7 RECORDED (run 34 id 42438532391 green, Release published); took over from the cut-off session; Tj's 02:29 notification message written as TASKS BH1/BH2 + HANDOFF 0j

## Do this next
BH1b build (v0.8.8): model a heads-up hold on AnimationSpeedup (grep 'AnimationSpeedup\.' for the trigger sites): new pure NotifQuietPolicy + JVM test, executor writes Settings.Global heads_up_notifications_enabled via RestoreStore snapshot/restore; then accessibility TYPE_NOTIFICATION_STATE_CHANGED sightings (check the accessibility config xml + EventNoise), report lines, Launcher control switch (release ON, debug OFF), emulator section nq. Then reply to Tj with the v0.8.7 link + BH1c questions

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  6d26926 ckpt 657: v0.8.7 RELEASE build triggered 02:26 UTC: android.yml run 34 id 42438532391 on
  95f27b6 ckpt 656: gated v0.8.7 (code 32) and pushed it: the handoff self-test, the full unit sui
  bf90b5a ckpt 655: v0.8.7 gate: {base,rt} #337 41 PASS / 2 FAIL (both in base right after a NATIV
  04de4ff ckpt 654: v0.8.7 gate read: B #335 and C #336 SUCCESS; A #334 {base,e1,x,ac,rt} 103 PASS
  00938b2 ckpt 653: v0.8.7 GATE dispatched 01:41 UTC on f39b3db: #334 id 42435300260 {base,e1,x,ac
  f39b3db ckpt 652: v0.8.7 (code 32) candidate for BG1/HLP1: queued LockLog, apply() on the unlock
  4220460 ckpt 651: BG1 groundwork (unreleased): LockLog.add now queued in order (46 callers incl.
  15334c4 ckpt 650: BG1 (fast, invisible prep of the custom launcher from the lock screen, RAM) an
  66f6f86 ckpt 649: v0.8.6 RELEASED 01:00 UTC (https://github.com/tjshea90/Launcher/releases/tag/v
  3894239 ckpt 648: v0.8.6 RELEASE build triggered 00:58 UTC: android.yml run 33 id 42432136845 on
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
