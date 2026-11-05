# CHECKPOINT 302 — read me first, then TASKS.md

**Written:** 2026-11-05T04:09:44Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-3bcc29a7-cuz0x0` · **builds on:** `d5a26e5` (this checkpoint is the commit after it)

## Just done
Review fixes: ImmersiveProfile owns-only undo + snapshot refresh, KeepAlive stop on failed foreground, Control text truthful, EventNoise keeps 'android', target cache negative TTL + uninstall check, plan() consistent; gc harness: start forms from stopped helper + pkill rc; guide compile undo -> verify (reset refused to shell). 220 unit tests green. rt run 106 hung ~8 min (cancelled)

## Do this next
read cancelled rt run 41965112013 RESULTS block (job 127040219129) to see how far it got; if it hung before the lock, find the blocking line in the rt section; rerun rt and gc; then full run, bump 0.7.0, ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M research/SAFETY_2026-11-05.md

## Last ten checkpoints
```
  33f2d5f ckpt 301: rt rewritten: Contacts task id 8->20 (Android replaced it, code behaved per ru
  1e7e565 ckpt 300: Docs: research/BUTTONMAPPER section 3 written with REAL AOSP android-10.0.0_r4
  89e5aae ckpt 299: NEW SESSION (branch ccr-3bcc29a7-cuz0x0, hooks installed). Read the 5 pending 
  560d638 ckpt 298: CLAUDE.md: only status in_progress/queued is a safe list_workflow_runs filter;
  8761263 ckpt 297: Run 93 (pf) = 22 PASS 1 FAIL: PF-P1 was a TEST ORDER bug (uiautomator dump unb
  0a056c3 ckpt 296: Docs written: research/SAFETY, EFFICIENCY, BUTTONMAPPER (section 3 has RS3_KEY
  2bc758c ckpt 295: emulator.yml was INVALID after ckpt 294 (a colon inside a step name made the f
  9143e70 ckpt 294: AB1/AB5: section rb (restart with PIN + hidden status bar + one tile + keep-al
  05ac9f8 ckpt 293: AB3: CI-only :testnotifier module (flavours quick/ctl), emulator.yml builds it
  380d7fc ckpt 292: AB1: A11YON now single-quotes the merged accessibility list (a $ in another se
```

(11 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
