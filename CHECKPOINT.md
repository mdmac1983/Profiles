# CHECKPOINT 303 — read me first, then TASKS.md

**Written:** 2026-11-05T04:12:34Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-3bcc29a7-cuz0x0` · **builds on:** `c5100a0` (this checkpoint is the commit after it)

## Just done
rt: run 106 showed Android's Recents DROPS the older same-affinity task when a new one is added (8->22, 12->24; removeTask then kills the process): test app gets SecondActivity with own task affinity for a true 2-task case; Contacts only resumed (typical case) with same-task/same-process checks; progress markers added (rt hung ~8 min after the lock, cancelled)

## Do this next
dispatch rt and gc on main; read RESULTS (cancelled/finished runs still print it); if rt hangs again find the last 'RT progress' line

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  7c43805 ckpt 302: Review fixes: ImmersiveProfile owns-only undo + snapshot refresh, KeepAlive st
  33f2d5f ckpt 301: rt rewritten: Contacts task id 8->20 (Android replaced it, code behaved per ru
  1e7e565 ckpt 300: Docs: research/BUTTONMAPPER section 3 written with REAL AOSP android-10.0.0_r4
  89e5aae ckpt 299: NEW SESSION (branch ccr-3bcc29a7-cuz0x0, hooks installed). Read the 5 pending 
  560d638 ckpt 298: CLAUDE.md: only status in_progress/queued is a safe list_workflow_runs filter;
  8761263 ckpt 297: Run 93 (pf) = 22 PASS 1 FAIL: PF-P1 was a TEST ORDER bug (uiautomator dump unb
  0a056c3 ckpt 296: Docs written: research/SAFETY, EFFICIENCY, BUTTONMAPPER (section 3 has RS3_KEY
  2bc758c ckpt 295: emulator.yml was INVALID after ckpt 294 (a colon inside a step name made the f
  9143e70 ckpt 294: AB1/AB5: section rb (restart with PIN + hidden status bar + one tile + keep-al
  05ac9f8 ckpt 293: AB3: CI-only :testnotifier module (flavours quick/ctl), emulator.yml builds it
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
