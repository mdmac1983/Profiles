# CHECKPOINT 301 — read me first, then TASKS.md

**Written:** 2026-11-05T04:00:43Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-3bcc29a7-cuz0x0` · **builds on:** `e7f0834` (this checkpoint is the commit after it)

## Just done
rt rewritten: Contacts task id 8->20 (Android replaced it, code behaved per rules) so the check now tests process identity + a deterministic shared CI test app (quiet extra) = AA3-S0..S3; rb unlock keys-first (run 104: rb 15 PASS); gc stdin-drain bug fixed (ran 2 of ~60 lines) + coverage check + DUMP regrant; AA4 doc written (rs4 DND: no effect)

## Do this next
dispatch emulator.yml ref=main sections=rt and gc; read both (get_job_logs run_id failed_only, tail 60); fix what they show; then full run (sections empty), bump 0.7.0, tick boxes with proof, ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M research/OPEN_BUT_HIDDEN_2026-11-05.md

## Last ten checkpoints
```
  1e7e565 ckpt 300: Docs: research/BUTTONMAPPER section 3 written with REAL AOSP android-10.0.0_r4
  89e5aae ckpt 299: NEW SESSION (branch ccr-3bcc29a7-cuz0x0, hooks installed). Read the 5 pending 
  560d638 ckpt 298: CLAUDE.md: only status in_progress/queued is a safe list_workflow_runs filter;
  8761263 ckpt 297: Run 93 (pf) = 22 PASS 1 FAIL: PF-P1 was a TEST ORDER bug (uiautomator dump unb
  0a056c3 ckpt 296: Docs written: research/SAFETY, EFFICIENCY, BUTTONMAPPER (section 3 has RS3_KEY
  2bc758c ckpt 295: emulator.yml was INVALID after ckpt 294 (a colon inside a step name made the f
  9143e70 ckpt 294: AB1/AB5: section rb (restart with PIN + hidden status bar + one tile + keep-al
  05ac9f8 ckpt 293: AB3: CI-only :testnotifier module (flavours quick/ctl), emulator.yml builds it
  380d7fc ckpt 292: AB1: A11YON now single-quotes the merged accessibility list (a $ in another se
  e6c72f1 ckpt 291: AB2 efficiency (code, compiled, not yet unit-tested): EventNoise (drops other 
```

(7 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
