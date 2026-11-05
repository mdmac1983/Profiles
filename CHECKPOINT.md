# CHECKPOINT 300 — read me first, then TASKS.md

**Written:** 2026-11-05T03:52:44Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-3bcc29a7-cuz0x0` · **builds on:** `26b8f1f` (this checkpoint is the commit after it)

## Just done
Docs: research/BUTTONMAPPER section 3 written with REAL AOSP android-10.0.0_r47 quotes (PhoneWindowManager KEYCODE_POWER clears ACTION_PASS_TO_USER; AccessibilityInputFilter skips events without FLAG_PASS_TO_USER; GlobalActionsImpl returns early on DISABLE2_GLOBAL_ACTIONS) => PWR2 answered: accessibility can NOT see/stop the power key (source-based; rs3 b0 failed its own control so it is NOT measured; native InputDispatcher order from memory, said so). Section 4 attribution fixed (rs2 a1 + rs3 b3 use input keyevent --longpress; console key injection is invalid). Diagnostics added for the two real failures: TaskOrigin.finish logs 'session end: base|now|stop|remove by id|spared' (TjTasks), scenario rt prints task ids before/at-lock/after + plan + helper log; scenario rb now alternates pad taps and 'input text 1234'+ENTER (6 tries) and prints user0 state/ce_available/keyguard/UI text after the restart and after each failed try; rs4 DND line now prints counts first. Unit tests green (218).

## Do this next
dispatch emulator.yml ref=main sections=rt ; rb ; rs4 (3 parallel); find run ids with list_workflow_runs(resource_id emulator.yml, filter branch=main,event=workflow_dispatch,perPage 4); read with get_job_logs(run_id, failed_only=true, tail_lines 90) or list_workflow_jobs(run id)->job id->get_job_logs(job_id, tail 90). Then: fix AA3 Contacts (use the RT task ids + plan lines) and rb unlock (use RB try lines); write research/OPEN_BUT_HIDDEN_2026-11-05.md (AA4) from rs3 a (move-task into stack 0 = IllegalArgumentException), rs4 a (DISABLE_RECENT flag set OK but Recents key still opens RecentsActivity) and rs4 b; tick PWR2/AA*/AB* only with proof; bump 0.7.0; full emulator run; ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  89e5aae ckpt 299: NEW SESSION (branch ccr-3bcc29a7-cuz0x0, hooks installed). Read the 5 pending 
  560d638 ckpt 298: CLAUDE.md: only status in_progress/queued is a safe list_workflow_runs filter;
  8761263 ckpt 297: Run 93 (pf) = 22 PASS 1 FAIL: PF-P1 was a TEST ORDER bug (uiautomator dump unb
  0a056c3 ckpt 296: Docs written: research/SAFETY, EFFICIENCY, BUTTONMAPPER (section 3 has RS3_KEY
  2bc758c ckpt 295: emulator.yml was INVALID after ckpt 294 (a colon inside a step name made the f
  9143e70 ckpt 294: AB1/AB5: section rb (restart with PIN + hidden status bar + one tile + keep-al
  05ac9f8 ckpt 293: AB3: CI-only :testnotifier module (flavours quick/ctl), emulator.yml builds it
  380d7fc ckpt 292: AB1: A11YON now single-quotes the merged accessibility list (a $ in another se
  e6c72f1 ckpt 291: AB2 efficiency (code, compiled, not yet unit-tested): EventNoise (drops other 
  2fc12ca ckpt 290: Tj's new message recorded as AB1-AB5: safety audit (no factory reset, ever), e
```

(9 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
