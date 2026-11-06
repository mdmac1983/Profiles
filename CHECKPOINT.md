# CHECKPOINT 390 — read me first, then TASKS.md

**Written:** 2026-11-06T00:24:27Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-0e021d44-iyicti` · **builds on:** `c78a967` (this checkpoint is the commit after it)

## Just done
gated v0.7.3 (code 20) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.7.3 "v0.7.3: plain PIN always opens custom (even after a breaker wipe), Solitaire switch gates the lock-screen prep, battery saver off in custom, gesture always wins, lock-menu card from the real panel, lock log"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  c78a967 ckpt 389: final gate round 2 on 6729d90 dispatched 00:23 (A + B); pf fix verified alone 
  bcbaa95 ckpt 388: pf root cause fixed: app under lock screen no longer counted as Quickstep-open
  403ef01 ckpt 387: pf/rt regression hunt: pf alone fails (ctl in AppOrigin.quickstep before lock;
  64af798 ckpt 386: pf regression: ctl closed at lock (runs 139/141); DIRTY log + pf diagnostics a
  eb6aec1 ckpt 385: final gate for v0.7.3 running: A = run 138 (id 42128898908), B = run 139 (id 4
  176d362 ckpt 384: v0.7.3 code complete: code-review fixes applied (stale pause, one retry, card 
  b04c417 ckpt 383: v0.7.2 RECORDED + released; version bumped to 0.7.3 (code 20); HANDOFF 0c + SA
  d1b5163 ckpt 382: incremental UsageGap query, short summary keeps 25 lock-log lines, battery obs
  3cbd776 ckpt 381: AC1 safety net coded (UsageGap + Mode.sessionRefMs + router/service reconcile)
  048fb35 ckpt 380: AC change set applied to main (unit 277, lint ok in the clone); emulator ac ru
```
