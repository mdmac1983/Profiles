# CHECKPOINT 395 — read me first, then TASKS.md

**Written:** 2026-11-06T01:38:15Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-0e021d44-iyicti` · **builds on:** `8420e03` (this checkpoint is the commit after it)

## Just done
round 3: rt Calendar survives with the fix (run 157 partial: task 34 present after lock; dump ends with RecentTaskInfo list = the cause); emulator-offline watchdog added to scenario

## Do this next
READ round 3 on 63f84b7: B run 159 (id 42142781433? check list), A run 158, validation v,ac,rt re-dispatched 01:35 (the first one hung: emulator offline at the lock). All green -> android.yml full_build=true on main, get_release_by_tag v0.7.3, record-release.sh v0.7.3, tell Tj. Hung run (no RESULTS for 20+ min) = cancel and rerun that run only

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  7823e42 ckpt 394: gate round 3 dispatched 01:17 on 63f84b7 (v,ac,rt + A + B); docs: root cause o
  63f84b7 ckpt 393: ROOT CAUSE of gate-B rt: helper recentTaskIds matched ANY line so the OLDEST R
  c31fe3f ckpt 392: gate round 2: B run 147 = 107 PASS 1 FAIL (AA3 Calendar target task gone after
  771e441 ckpt 391: ship.sh gates passed for v0.7.3 (handoff, units 299, lint); emulator gate roun
  c027bd7 ckpt 390: gated v0.7.3 (code 20) and pushed it: the handoff self-test, the full unit sui
  c78a967 ckpt 389: final gate round 2 on 6729d90 dispatched 00:23 (A + B); pf fix verified alone 
  bcbaa95 ckpt 388: pf root cause fixed: app under lock screen no longer counted as Quickstep-open
  403ef01 ckpt 387: pf/rt regression hunt: pf alone fails (ctl in AppOrigin.quickstep before lock;
  64af798 ckpt 386: pf regression: ctl closed at lock (runs 139/141); DIRTY log + pf diagnostics a
  eb6aec1 ckpt 385: final gate for v0.7.3 running: A = run 138 (id 42128898908), B = run 139 (id 4
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
