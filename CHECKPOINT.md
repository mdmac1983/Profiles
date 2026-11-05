# CHECKPOINT 295 — read me first, then TASKS.md

**Written:** 2026-11-05T02:25:25Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-8b1c3755-1tp147` · **builds on:** `e6d58bf` (this checkpoint is the commit after it)

## Just done
emulator.yml was INVALID after ckpt 294 (a colon inside a step name made the file unparsable, so GitHub reported 'Workflow does not have workflow_dispatch trigger'): fixed; tools/test_handoff.sh now parses every workflow and requires workflow_dispatch on emulator.yml and android.yml (29 checks)

## Do this next
dispatch emulator sections pf ; ka,rt,gc ; rb ; rs3 and read ONLY the RESULTS block at the end of each log (tail ~70)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  9143e70 ckpt 294: AB1/AB5: section rb (restart with PIN + hidden status bar + one tile + keep-al
  05ac9f8 ckpt 293: AB3: CI-only :testnotifier module (flavours quick/ctl), emulator.yml builds it
  380d7fc ckpt 292: AB1: A11YON now single-quotes the merged accessibility list (a $ in another se
  e6c72f1 ckpt 291: AB2 efficiency (code, compiled, not yet unit-tested): EventNoise (drops other 
  2fc12ca ckpt 290: Tj's new message recorded as AB1-AB5: safety audit (no factory reset, ever), e
  da09334 ckpt 289: AA1 (ImmersiveProfile: policy_control immersive.status=* per mode, merge/resto
  11306b2 ckpt 288: AA2 built: KeepAliveService (foreground, min-importance silent notification) +
  cbf34e0 ckpt 287: Added emulator research section rs (info lines only): R1 policy_control immers
  b62dbca ckpt 286: Tj: build #2 (policy_control immersive), #3 (keep-alive service), #4 (helper t
  a0d9170 ckpt 285: Wrote research/REEXAMINE_2026-11-05.md re-examining every goal with fresh idea
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
