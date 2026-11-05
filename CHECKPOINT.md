# CHECKPOINT 382 — read me first, then TASKS.md

**Written:** 2026-11-05T23:02:37Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-0e021d44-iyicti` · **builds on:** `69dcc74` (this checkpoint is the commit after it)

## Just done
incremental UsageGap query, short summary keeps 25 lock-log lines, battery observer main process only; unit 287 green

## Do this next
ac run 134 result at ~23:06 (job 127589010068, tail 80), then redispatch ac + v on the newest main; release run 21 ends ~23:21

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  3cbd776 ckpt 381: AC1 safety net coded (UsageGap + Mode.sessionRefMs + router/service reconcile)
  048fb35 ckpt 380: AC change set applied to main (unit 277, lint ok in the clone); emulator ac ru
  5e77c5e ckpt 379: v0.7.2 gates green (A run 131, B run 133, unit 245, lint); android.yml run 21 
  1079dbb ckpt 378: gated v0.7.2 (code 19) and pushed it: the handoff self-test, the full unit sui
  a51f04a ckpt 377: AC drafts saved in research/ac-drafts (BatterySaver*, LockLog, PowerMenuDump +
  c7cc995 ckpt 376: ka fuse check made conditional (emulator SystemUI never crashes: runs 130,132)
  782ca63 ckpt 375: AC design written (research/AC_DESIGN_2026-11-05.md); gate A run 131 + ka run 
  88e1fb2 ckpt 374: Tj sent 9 requests (AC1-AC9) written to TASKS.md
  90bdfef ckpt 373: REL072: code-review of v0.7.1..HEAD done (8 fixed, 2 declined); unit 245 green
  e860f7e ckpt 372: REL072: KA1+CBR1+UX1 coded, unit 245 green, lint ok; ka scenario extended, CI 
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
