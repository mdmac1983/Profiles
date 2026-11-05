# CHECKPOINT 297 — read me first, then TASKS.md

**Written:** 2026-11-05T02:34:19Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-8b1c3755-1tp147` · **builds on:** `12d6e35` (this checkpoint is the commit after it)

## Just done
Run 93 (pf) = 22 PASS 1 FAIL: PF-P1 was a TEST ORDER bug (uiautomator dump unbinds accessibility services, the pencil cover count was read after a dump); run 95 (rb) FAILed because my wait read boot_completed=1 from the still-shutting-down old system (device stuck at FallbackHome, a11y/keep-alive not yet bound because the PIN was never entered): both fixed in tools/emulator_scenario.sh (rb now waits for shutdown, boot, then enters the PIN up to 4 times). CI log reading: new local action .github/actions/results prints the compact RESULTS block in its POST step so it is the last thing before checkout's post (tail ~45 lines is enough from now on)

## Do this next
dispatch pf ; ka,rt,gc ; rb again; read rs3 (run 41955128770) and rs4 logs the long way once (tail ~190) for the BUTTONMAPPER placeholder and OPEN_BUT_HIDDEN doc

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  0a056c3 ckpt 296: Docs written: research/SAFETY, EFFICIENCY, BUTTONMAPPER (section 3 has RS3_KEY
  2bc758c ckpt 295: emulator.yml was INVALID after ckpt 294 (a colon inside a step name made the f
  9143e70 ckpt 294: AB1/AB5: section rb (restart with PIN + hidden status bar + one tile + keep-al
  05ac9f8 ckpt 293: AB3: CI-only :testnotifier module (flavours quick/ctl), emulator.yml builds it
  380d7fc ckpt 292: AB1: A11YON now single-quotes the merged accessibility list (a $ in another se
  e6c72f1 ckpt 291: AB2 efficiency (code, compiled, not yet unit-tested): EventNoise (drops other 
  2fc12ca ckpt 290: Tj's new message recorded as AB1-AB5: safety audit (no factory reset, ever), e
  da09334 ckpt 289: AA1 (ImmersiveProfile: policy_control immersive.status=* per mode, merge/resto
  11306b2 ckpt 288: AA2 built: KeepAliveService (foreground, min-importance silent notification) +
  cbf34e0 ckpt 287: Added emulator research section rs (info lines only): R1 policy_control immers
```

(8 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
