# CHECKPOINT 298 — read me first, then TASKS.md

**Written:** 2026-11-05T02:36:25Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-8b1c3755-1tp147` · **builds on:** `b7ed707` (this checkpoint is the commit after it)

## Just done
CLAUDE.md: only status in_progress/queued is a safe list_workflow_runs filter; log tail note. rs3 run 96: b0 KEYRAW empty (even the volume-up control) and b1 console power press opened NO menu => emulator console key injection is not a valid power-key test; b2 DISABLE2_GLOBAL_ACTIONS set by shell = OK [0,8], b3 stock menu back after. A subagent is fetching the AOSP PhoneWindowManager/AccessibilityInputFilter lines to document why a11y never sees KEYCODE_POWER

## Do this next
read runs (job ids via list_workflow_jobs): #98 pf=41956102020, #99 ka,rt,gc=41956106210, #100 rb=41956109727, #101 rs4=41956254427, #102 rs3=41956257923 (tail_lines 60: the compact block is last). Then BUTTONMAPPER placeholder (section 3: use the subagent's AOSP quotes; b0 is inconclusive because the volume control also logged nothing), OPEN_BUT_HIDDEN doc from rs4, tick TASKS, bump 0.7.0, full run, ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  8761263 ckpt 297: Run 93 (pf) = 22 PASS 1 FAIL: PF-P1 was a TEST ORDER bug (uiautomator dump unb
  0a056c3 ckpt 296: Docs written: research/SAFETY, EFFICIENCY, BUTTONMAPPER (section 3 has RS3_KEY
  2bc758c ckpt 295: emulator.yml was INVALID after ckpt 294 (a colon inside a step name made the f
  9143e70 ckpt 294: AB1/AB5: section rb (restart with PIN + hidden status bar + one tile + keep-al
  05ac9f8 ckpt 293: AB3: CI-only :testnotifier module (flavours quick/ctl), emulator.yml builds it
  380d7fc ckpt 292: AB1: A11YON now single-quotes the merged accessibility list (a $ in another se
  e6c72f1 ckpt 291: AB2 efficiency (code, compiled, not yet unit-tested): EventNoise (drops other 
  2fc12ca ckpt 290: Tj's new message recorded as AB1-AB5: safety audit (no factory reset, ever), e
  da09334 ckpt 289: AA1 (ImmersiveProfile: policy_control immersive.status=* per mode, merge/resto
  11306b2 ckpt 288: AA2 built: KeepAliveService (foreground, min-importance silent notification) +
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
