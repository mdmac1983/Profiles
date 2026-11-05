# CHECKPOINT 377 — read me first, then TASKS.md

**Written:** 2026-11-05T22:44:13Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-0e021d44-iyicti` · **builds on:** `77fcb04` (this checkpoint is the commit after it)

## Just done
AC drafts saved in research/ac-drafts (BatterySaver*, LockLog, PowerMenuDump + tests, scenario section ac); ADB_LOCKMENU_PROBE.md written; gate A run 131 + B run 133 still running

## Do this next
read runs 131 and 133 (tail 60 of the job logs, list_workflow_jobs for the job ids) after ~22:55; both 0 FAIL -> bash ship.sh 'v0.7.2: KA1 CBR1 UX1' -> android.yml full_build=true on main; ONLY AFTER that run started: copy research/ac-drafts into app/ and wire in per research/AC_DESIGN_2026-11-05.md

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  c7cc995 ckpt 376: ka fuse check made conditional (emulator SystemUI never crashes: runs 130,132)
  782ca63 ckpt 375: AC design written (research/AC_DESIGN_2026-11-05.md); gate A run 131 + ka run 
  88e1fb2 ckpt 374: Tj sent 9 requests (AC1-AC9) written to TASKS.md
  90bdfef ckpt 373: REL072: code-review of v0.7.1..HEAD done (8 fixed, 2 declined); unit 245 green
  e860f7e ckpt 372: REL072: KA1+CBR1+UX1 coded, unit 245 green, lint ok; ka scenario extended, CI 
  ce42d62 ckpt 371: Tj asked to release the next version + will an update need adb (answer: no); R
  fa8132b ckpt 370: Tj: cable later; asked if he can use Launcher control now: yes, with cautions 
  c684404 ckpt 369: step 22: all permissions verified after restart (CBL1 closed); answer to 'ever
  8181b49 ckpt 368: step 21: lock number shows on the real lock screen (LK1 number+gesture OK); st
  1b94375 ckpt 367: step 20: Tj turned the lock number ON; step 21 (lock + look for the number) se
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
