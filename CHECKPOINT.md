# CHECKPOINT 380 — read me first, then TASKS.md

**Written:** 2026-11-05T22:56:46Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-0e021d44-iyicti` · **builds on:** `a651ce1` (this checkpoint is the commit after it)

## Just done
AC change set applied to main (unit 277, lint ok in the clone); emulator ac run dispatched ~22:57; v0.7.2 build run 21 in progress

## Do this next
read the ac run (list_workflow_runs emulator.yml main workflow_dispatch perPage 2, then list_workflow_jobs, get_job_logs tail 60); fix; at ~23:23 get_release_by_tag v0.7.2 then record-release; then bump versionCode 20 / 0.7.3 and run gate A+B

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  5e77c5e ckpt 379: v0.7.2 gates green (A run 131, B run 133, unit 245, lint); android.yml run 21 
  1079dbb ckpt 378: gated v0.7.2 (code 19) and pushed it: the handoff self-test, the full unit sui
  a51f04a ckpt 377: AC drafts saved in research/ac-drafts (BatterySaver*, LockLog, PowerMenuDump +
  c7cc995 ckpt 376: ka fuse check made conditional (emulator SystemUI never crashes: runs 130,132)
  782ca63 ckpt 375: AC design written (research/AC_DESIGN_2026-11-05.md); gate A run 131 + ka run 
  88e1fb2 ckpt 374: Tj sent 9 requests (AC1-AC9) written to TASKS.md
  90bdfef ckpt 373: REL072: code-review of v0.7.1..HEAD done (8 fixed, 2 declined); unit 245 green
  e860f7e ckpt 372: REL072: KA1+CBR1+UX1 coded, unit 245 green, lint ok; ka scenario extended, CI 
  ce42d62 ckpt 371: Tj asked to release the next version + will an update need adb (answer: no); R
  fa8132b ckpt 370: Tj: cable later; asked if he can use Launcher control now: yes, with cautions 
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
