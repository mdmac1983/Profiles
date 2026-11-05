# CHECKPOINT 305 — read me first, then TASKS.md

**Written:** 2026-11-05T04:23:55Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-3bcc29a7-cuz0x0` · **builds on:** `2de785e` (this checkpoint is the commit after it)

## Just done
gc harness bug: *PATH* skipped every CLASSPATH= line (helper start + ShellProbe never ran) -> now only CLASSPATH=PATH is skipped. TASKS: AA3/AA4/AB3/AB5 ticked with proof. Version bumped to 0.7.0 (code 17). Release gate split in two parallel runs (one run would exceed the 35 min scenario timeout): A = base,legacy,x1,e1,y2,b6,b4,n,n6,o,t,u,s,p,x ; B = v,g,ka,rt,im,gc,pf,rb,last (research rs* skipped)

## Do this next
gate half A (sections base..x) and a gc rerun were dispatched ~04:25Z on main: read with list_workflow_runs(resource_id emulator.yml, branch main, event workflow_dispatch, perPage 3) then get_job_logs(run_id, failed_only, tail 60); when gc is green dispatch half B (v,g,ka,rt,im,gc,pf,rb,last); all green -> tick AA1 AA2 AA5 AB1 AB2 AB4, HANDOFF 0b v0.7.0 block, bash ship.sh, android.yml full_build=true, record-release

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  c8077b8 ckpt 304: gc harness: process evidence for the helper check; rt PASSES (run 108: 18 PASS
  2e195a9 ckpt 303: rt: run 106 showed Android's Recents DROPS the older same-affinity task when a
  7c43805 ckpt 302: Review fixes: ImmersiveProfile owns-only undo + snapshot refresh, KeepAlive st
  33f2d5f ckpt 301: rt rewritten: Contacts task id 8->20 (Android replaced it, code behaved per ru
  1e7e565 ckpt 300: Docs: research/BUTTONMAPPER section 3 written with REAL AOSP android-10.0.0_r4
  89e5aae ckpt 299: NEW SESSION (branch ccr-3bcc29a7-cuz0x0, hooks installed). Read the 5 pending 
  560d638 ckpt 298: CLAUDE.md: only status in_progress/queued is a safe list_workflow_runs filter;
  8761263 ckpt 297: Run 93 (pf) = 22 PASS 1 FAIL: PF-P1 was a TEST ORDER bug (uiautomator dump unb
  0a056c3 ckpt 296: Docs written: research/SAFETY, EFFICIENCY, BUTTONMAPPER (section 3 has RS3_KEY
  2bc758c ckpt 295: emulator.yml was INVALID after ckpt 294 (a colon inside a step name made the f
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
