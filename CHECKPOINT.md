# CHECKPOINT 296 — read me first, then TASKS.md

**Written:** 2026-11-05T02:30:23Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-8b1c3755-1tp147` · **builds on:** `51f5a41` (this checkpoint is the commit after it)

## Just done
Docs written: research/SAFETY, EFFICIENCY, BUTTONMAPPER (section 3 has RS3_KEYRAW_PLACEHOLDER to replace with the rs3 b0 numbers); guide +2 cards; CLAUDE.md: cheap result reading + YAML warning; TASKS progress block; kgAt memo reset in lockReceiver; 218 unit tests green. Emulator runs in flight (main @ ckpt 295): pf 41955118593, ka,rt,gc 41955121787, rb 41955125158, rs3 41955128770, rs4 (newest in_progress). Run 85 (y2,b6,b4,t) PASSED, run 83 (pf) had 1 unknown FAIL (B1-B3 passed), run 84 (ka,rt,gc) FAILED unknown

## Do this next
read the 5 runs with list_workflow_jobs + get_job_logs tail 80 (RESULTS block at the end); fix FAILs; BUTTONMAPPER placeholder; OPEN_BUT_HIDDEN doc from rs4; tick TASKS AA1-AA6/AB1-AB5; bump 0.7.0/17; HANDOFF 0b; full run (empty sections) as gate; ship.sh; android.yml full_build; record-release; tell Tj the OTG recipe

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  2bc758c ckpt 295: emulator.yml was INVALID after ckpt 294 (a colon inside a step name made the f
  9143e70 ckpt 294: AB1/AB5: section rb (restart with PIN + hidden status bar + one tile + keep-al
  05ac9f8 ckpt 293: AB3: CI-only :testnotifier module (flavours quick/ctl), emulator.yml builds it
  380d7fc ckpt 292: AB1: A11YON now single-quotes the merged accessibility list (a $ in another se
  e6c72f1 ckpt 291: AB2 efficiency (code, compiled, not yet unit-tested): EventNoise (drops other 
  2fc12ca ckpt 290: Tj's new message recorded as AB1-AB5: safety audit (no factory reset, ever), e
  da09334 ckpt 289: AA1 (ImmersiveProfile: policy_control immersive.status=* per mode, merge/resto
  11306b2 ckpt 288: AA2 built: KeepAliveService (foreground, min-importance silent notification) +
  cbf34e0 ckpt 287: Added emulator research section rs (info lines only): R1 policy_control immers
  b62dbca ckpt 286: Tj: build #2 (policy_control immersive), #3 (keep-alive service), #4 (helper t
```

(7 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
