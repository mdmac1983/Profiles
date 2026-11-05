# CHECKPOINT 299 — read me first, then TASKS.md

**Written:** 2026-11-05T03:43:04Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-3bcc29a7-cuz0x0` · **builds on:** `4187169` (this checkpoint is the commit after it)

## Just done
NEW SESSION (branch ccr-3bcc29a7-cuz0x0, hooks installed). Read the 5 pending emulator runs. NOTE: the ids in old CHECKPOINT 'job ids' were RUN ids: use get_job_logs(run_id, failed_only=true, return_content, tail_lines 70) or list_workflow_jobs(resource_id=run id) -> job id -> get_job_logs(job_id). Runs were dispatched on ref main (list_workflow_runs filter branch=main + event=workflow_dispatch + perPage works and is small). RESULTS: #98 pf (run 41956102020) 23 PASS 0 FAIL = AB3 notification panel PROVEN (shared app notif gone after lock, custom-only app notif stays, custom tiles=rotation only). #99 ka,rt,gc (run 41956106210) 18 PASS 1 FAIL: FAIL = 'AA3: Contacts, which the custom account had open before Quickstep began, is SPARED (still in Recents)'; log: session start snapshot had 8:com.android.contacts and 2:com.android.settings, cleanup said 'force-stopped 9 of 9 apps; 1 task(s) removed by id; 2 app(s) spared [contacts, settings]' but after the lock Recents has NO contacts and NO settings task (RT custom before: calendar,contacts,launcher3,settings,tj; after lock: calendar,launcher3,tj) => something removes the shared tasks; investigate TaskOrigin.plan / ReturnReset / RMTASKS. KA proof OK (oom_score_adj 100 helper-bound, 50 with KeepAliveService, survives kill-all, Restore stops service). GC: 2 guide lines executed, 0 problems. #100 rb (run 41956109727) 11 PASS 4 FAIL (RB4,RB5b,RB7,RB7b) ALL caused by 'unlock after the restart NOT done; resumed FallbackHome' = the scenario PIN entry after reboot does not work yet (device came back in ~16 s, tiles/policy_control survived the restart). #101 rs4 (run 41956254427) 4 PASS: DISABLE_RECENT (0x1000000) set by shell OK but the Recents key STILL opens RecentsActivity; DND policy probe line truncated in RESULTS (need full text). #102 rs3 (run 41956257923) 4 PASS: b0 KEYRAW empty (even volume-up control), b1 console power press opened NO menu, b2 DISABLE2_GLOBAL_ACTIONS(0x8) set by shell OK, b3 stock menu (Power off/Restart/Screenshot) back after; 'am stack move-task 8 0 true' = IllegalArgumentException (cannot move a task to the home stack 0).

## Do this next
1) read tools/emulator_scenario.sh sections rt and rb + app/src/main TaskOrigin/ReturnReset: why does the shared Contacts task vanish and why does rb unlock fail (screenshot/dumpsys of the keyguard after reboot needs an INFO line); 2) fix, rerun sections rt / rb on main; 3) docs: BUTTONMAPPER section 3 (AOSP: PhoneWindowManager clears ACTION_PASS_TO_USER for KEYCODE_POWER so AccessibilityInputFilter never gets it), OPEN_BUT_HIDDEN (AA4) from rs3/rs4 numbers; 4) tick TASKS, bump 0.7.0, full emulator run, ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
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

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
