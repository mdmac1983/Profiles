# CHECKPOINT 388 — read me first, then TASKS.md

**Written:** 2026-11-06T00:16:23Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-0e021d44-iyicti` · **builds on:** `f8cec4e` (this checkpoint is the commit after it)

## Just done
pf root cause fixed: app under lock screen no longer counted as Quickstep-opened (windowIsLockLeftover), units 299 green

## Do this next
dispatch pf on main to verify; read ac,rt run 144 for the Calendar-task loss (events/lock log INFO lines); then full gate A+B on final commit

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  403ef01 ckpt 387: pf/rt regression hunt: pf alone fails (ctl in AppOrigin.quickstep before lock;
  64af798 ckpt 386: pf regression: ctl closed at lock (runs 139/141); DIRTY log + pf diagnostics a
  eb6aec1 ckpt 385: final gate for v0.7.3 running: A = run 138 (id 42128898908), B = run 139 (id 4
  176d362 ckpt 384: v0.7.3 code complete: code-review fixes applied (stale pause, one retry, card 
  b04c417 ckpt 383: v0.7.2 RECORDED + released; version bumped to 0.7.3 (code 20); HANDOFF 0c + SA
  d1b5163 ckpt 382: incremental UsageGap query, short summary keeps 25 lock-log lines, battery obs
  3cbd776 ckpt 381: AC1 safety net coded (UsageGap + Mode.sessionRefMs + router/service reconcile)
  048fb35 ckpt 380: AC change set applied to main (unit 277, lint ok in the clone); emulator ac ru
  5e77c5e ckpt 379: v0.7.2 gates green (A run 131, B run 133, unit 245, lint); android.yml run 21 
  1079dbb ckpt 378: gated v0.7.2 (code 19) and pushed it: the handoff self-test, the full unit sui
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
