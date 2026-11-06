# CHECKPOINT 389 — read me first, then TASKS.md

**Written:** 2026-11-06T00:23:07Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-0e021d44-iyicti` · **builds on:** `6729d90` (this checkpoint is the commit after it)

## Just done
final gate round 2 on 6729d90 dispatched 00:23 (A + B); pf fix verified alone (run 145: 23 PASS)

## Do this next
wait for A and B (about 00:45); read RESULTS; if all green: ship.sh then android.yml full_build=true then get_release_by_tag v0.7.3 then record-release.sh; if B rt fails again read RT events/lock-log INFO lines

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  bcbaa95 ckpt 388: pf root cause fixed: app under lock screen no longer counted as Quickstep-open
  403ef01 ckpt 387: pf/rt regression hunt: pf alone fails (ctl in AppOrigin.quickstep before lock;
  64af798 ckpt 386: pf regression: ctl closed at lock (runs 139/141); DIRTY log + pf diagnostics a
  eb6aec1 ckpt 385: final gate for v0.7.3 running: A = run 138 (id 42128898908), B = run 139 (id 4
  176d362 ckpt 384: v0.7.3 code complete: code-review fixes applied (stale pause, one retry, card 
  b04c417 ckpt 383: v0.7.2 RECORDED + released; version bumped to 0.7.3 (code 20); HANDOFF 0c + SA
  d1b5163 ckpt 382: incremental UsageGap query, short summary keeps 25 lock-log lines, battery obs
  3cbd776 ckpt 381: AC1 safety net coded (UsageGap + Mode.sessionRefMs + router/service reconcile)
  048fb35 ckpt 380: AC change set applied to main (unit 277, lint ok in the clone); emulator ac ru
  5e77c5e ckpt 379: v0.7.2 gates green (A run 131, B run 133, unit 245, lint); android.yml run 21 
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
