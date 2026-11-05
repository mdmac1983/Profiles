# CHECKPOINT 381 — read me first, then TASKS.md

**Written:** 2026-11-05T22:59:50Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-0e021d44-iyicti` · **builds on:** `1506c74` (this checkpoint is the commit after it)

## Just done
AC1 safety net coded (UsageGap + Mode.sessionRefMs + router/service reconcile), ENTER log lag; unit 287 green locally; ac run 134 (a651ce1) still running

## Do this next
read run 134 (ac) results when done (~23:03); then dispatch the second round (ac again + v + n,o) on the newest commit; v0.7.2 release build run 21 ends ~23:21 -> get_release_by_tag + record-release

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  048fb35 ckpt 380: AC change set applied to main (unit 277, lint ok in the clone); emulator ac ru
  5e77c5e ckpt 379: v0.7.2 gates green (A run 131, B run 133, unit 245, lint); android.yml run 21 
  1079dbb ckpt 378: gated v0.7.2 (code 19) and pushed it: the handoff self-test, the full unit sui
  a51f04a ckpt 377: AC drafts saved in research/ac-drafts (BatterySaver*, LockLog, PowerMenuDump +
  c7cc995 ckpt 376: ka fuse check made conditional (emulator SystemUI never crashes: runs 130,132)
  782ca63 ckpt 375: AC design written (research/AC_DESIGN_2026-11-05.md); gate A run 131 + ka run 
  88e1fb2 ckpt 374: Tj sent 9 requests (AC1-AC9) written to TASKS.md
  90bdfef ckpt 373: REL072: code-review of v0.7.1..HEAD done (8 fixed, 2 declined); unit 245 green
  e860f7e ckpt 372: REL072: KA1+CBR1+UX1 coded, unit 245 green, lint ok; ka scenario extended, CI 
  ce42d62 ckpt 371: Tj asked to release the next version + will an update need adb (answer: no); R
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
