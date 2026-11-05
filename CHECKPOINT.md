# CHECKPOINT 306 — read me first, then TASKS.md

**Written:** 2026-11-05T04:33:39Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-3bcc29a7-cuz0x0` · **builds on:** `65cf712` (this checkpoint is the commit after it)

## Just done
HANDOFF.md refreshed for v0.7.0 (state block, architecture rows, decisions, recipe; RELEASE_STATE_PLACEHOLDER and GATE_PLACEHOLDER still to fill after the gate); phoneguide 1.1 (code 2). gc run 111 = 6 PASS 0 FAIL (59 of 60 guide lines executed)

## Do this next
READ THE GATE: half A = run 41967538652 (sections base..x, started 04:23Z), half B = run 41968007385 (v,g,ka,rt,im,gc,pf,rb,last, started 04:28Z): get_job_logs(run_id, failed_only=true, tail 60) or list_workflow_jobs -> job id -> get_job_logs(tail 60). Fix/rerun only failed sections. When both green: tick AA1 AA2 AA5 AB1 AB2 AB4 AA6 in TASKS.md, fill the two placeholders in HANDOFF 0b, bash ship.sh, dispatch android.yml full_build=true on main, get_release_by_tag v0.7.0, bash tools/record-release.sh v0.7.0 note

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  dc1c928 ckpt 305: gc harness bug: *PATH* skipped every CLASSPATH= line (helper start + ShellProb
  c8077b8 ckpt 304: gc harness: process evidence for the helper check; rt PASSES (run 108: 18 PASS
  2e195a9 ckpt 303: rt: run 106 showed Android's Recents DROPS the older same-affinity task when a
  7c43805 ckpt 302: Review fixes: ImmersiveProfile owns-only undo + snapshot refresh, KeepAlive st
  33f2d5f ckpt 301: rt rewritten: Contacts task id 8->20 (Android replaced it, code behaved per ru
  1e7e565 ckpt 300: Docs: research/BUTTONMAPPER section 3 written with REAL AOSP android-10.0.0_r4
  89e5aae ckpt 299: NEW SESSION (branch ccr-3bcc29a7-cuz0x0, hooks installed). Read the 5 pending 
  560d638 ckpt 298: CLAUDE.md: only status in_progress/queued is a safe list_workflow_runs filter;
  8761263 ckpt 297: Run 93 (pf) = 22 PASS 1 FAIL: PF-P1 was a TEST ORDER bug (uiautomator dump unb
  0a056c3 ckpt 296: Docs written: research/SAFETY, EFFICIENCY, BUTTONMAPPER (section 3 has RS3_KEY
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
