# CHECKPOINT 391 — read me first, then TASKS.md

**Written:** 2026-11-06T00:25:20Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-0e021d44-iyicti` · **builds on:** `5fc0ff4` (this checkpoint is the commit after it)

## Just done
ship.sh gates passed for v0.7.3 (handoff, units 299, lint); emulator gate round 2 (A+B on 6729d90, dispatched 00:23) still running

## Do this next
DO NOT BUILD YET: first read gate A and B results (emulator.yml runs dispatched 00:23 on main 6729d90: A = base,legacy,x1,e1,y2,b6,b4,n,n6,o,t,u,s,p,x; B = v,g,ka,ac,rt,im,gc,pf,rb,last; find them with actions_list list_workflow_runs emulator.yml branch main event workflow_dispatch perPage 3). Both green (B rt must pass) -> android.yml full_build=true on main, get_release_by_tag v0.7.3, record-release.sh v0.7.3, tell Tj. A red B rt -> read RT events/lock log INFO lines, product code stays frozen until understood

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  c027bd7 ckpt 390: gated v0.7.3 (code 20) and pushed it: the handoff self-test, the full unit sui
  c78a967 ckpt 389: final gate round 2 on 6729d90 dispatched 00:23 (A + B); pf fix verified alone 
  bcbaa95 ckpt 388: pf root cause fixed: app under lock screen no longer counted as Quickstep-open
  403ef01 ckpt 387: pf/rt regression hunt: pf alone fails (ctl in AppOrigin.quickstep before lock;
  64af798 ckpt 386: pf regression: ctl closed at lock (runs 139/141); DIRTY log + pf diagnostics a
  eb6aec1 ckpt 385: final gate for v0.7.3 running: A = run 138 (id 42128898908), B = run 139 (id 4
  176d362 ckpt 384: v0.7.3 code complete: code-review fixes applied (stale pause, one retry, card 
  b04c417 ckpt 383: v0.7.2 RECORDED + released; version bumped to 0.7.3 (code 20); HANDOFF 0c + SA
  d1b5163 ckpt 382: incremental UsageGap query, short summary keeps 25 lock-log lines, battery obs
  3cbd776 ckpt 381: AC1 safety net coded (UsageGap + Mode.sessionRefMs + router/service reconcile)
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
