# CHECKPOINT 394 — read me first, then TASKS.md

**Written:** 2026-11-06T01:17:36Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-0e021d44-iyicti` · **builds on:** `63f84b7` (this checkpoint is the commit after it)

## Just done
gate round 3 dispatched 01:17 on 63f84b7 (v,ac,rt + A + B); docs: root cause of rt failure

## Do this next
READ the 3 runs (list_workflow_runs emulator.yml branch main event workflow_dispatch perPage 4): v,ac,rt must PASS (AA3 Calendar), A and B green. Then android.yml full_build=true on main, get_release_by_tag v0.7.3, record-release.sh v0.7.3, tell Tj. Product code frozen until android.yml has checked out. An emulator that goes 'device offline' is infrastructure: cancel and rerun that run

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M HANDOFF.md
     M TASKS.md

## Last ten checkpoints
```
  63f84b7 ckpt 393: ROOT CAUSE of gate-B rt: helper recentTaskIds matched ANY line so the OLDEST R
  c31fe3f ckpt 392: gate round 2: B run 147 = 107 PASS 1 FAIL (AA3 Calendar target task gone after
  771e441 ckpt 391: ship.sh gates passed for v0.7.3 (handoff, units 299, lint); emulator gate roun
  c027bd7 ckpt 390: gated v0.7.3 (code 20) and pushed it: the handoff self-test, the full unit sui
  c78a967 ckpt 389: final gate round 2 on 6729d90 dispatched 00:23 (A + B); pf fix verified alone 
  bcbaa95 ckpt 388: pf root cause fixed: app under lock screen no longer counted as Quickstep-open
  403ef01 ckpt 387: pf/rt regression hunt: pf alone fails (ctl in AppOrigin.quickstep before lock;
  64af798 ckpt 386: pf regression: ctl closed at lock (runs 139/141); DIRTY log + pf diagnostics a
  eb6aec1 ckpt 385: final gate for v0.7.3 running: A = run 138 (id 42128898908), B = run 139 (id 4
  176d362 ckpt 384: v0.7.3 code complete: code-review fixes applied (stale pause, one retry, card 
```
