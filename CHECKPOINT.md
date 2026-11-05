# CHECKPOINT 383 — read me first, then TASKS.md

**Written:** 2026-11-05T23:08:40Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-0e021d44-iyicti` · **builds on:** `e9b8a8a` (this checkpoint is the commit after it)

## Just done
v0.7.2 RECORDED + released; version bumped to 0.7.3 (code 20); HANDOFF 0c + SAFETY section 8 written; ac re-run + v dispatched on a9c9111 (~23:07)

## Do this next
read the ac and v runs (list_workflow_runs emulator.yml main workflow_dispatch perPage 3 -> list_workflow_jobs -> get_job_logs tail 62; v needs tail ~100 for the V5 INFO lines); fix any FAIL; then gate A (base,legacy,x1,e1,y2,b6,b4,n,n6,o,t,u,s,p,x) + B (v,g,ka,ac,rt,im,gc,pf,rb,last) on the FINAL commit

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M research/SAFETY_2026-11-05.md

## Last ten checkpoints
```
  d1b5163 ckpt 382: incremental UsageGap query, short summary keeps 25 lock-log lines, battery obs
  3cbd776 ckpt 381: AC1 safety net coded (UsageGap + Mode.sessionRefMs + router/service reconcile)
  048fb35 ckpt 380: AC change set applied to main (unit 277, lint ok in the clone); emulator ac ru
  5e77c5e ckpt 379: v0.7.2 gates green (A run 131, B run 133, unit 245, lint); android.yml run 21 
  1079dbb ckpt 378: gated v0.7.2 (code 19) and pushed it: the handoff self-test, the full unit sui
  a51f04a ckpt 377: AC drafts saved in research/ac-drafts (BatterySaver*, LockLog, PowerMenuDump +
  c7cc995 ckpt 376: ka fuse check made conditional (emulator SystemUI never crashes: runs 130,132)
  782ca63 ckpt 375: AC design written (research/AC_DESIGN_2026-11-05.md); gate A run 131 + ka run 
  88e1fb2 ckpt 374: Tj sent 9 requests (AC1-AC9) written to TASKS.md
  90bdfef ckpt 373: REL072: code-review of v0.7.1..HEAD done (8 fixed, 2 declined); unit 245 green
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
