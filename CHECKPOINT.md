# CHECKPOINT 401 — read me first, then TASKS.md

**Written:** 2026-11-06T02:00:56Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-9ab65fa5-vp812y` · **builds on:** `aa3ab96` (this checkpoint is the commit after it)

## Just done
Tj's tablet-optimisation request recorded (AD8); the attached report is identical to the first one (duplicate dropped)

## Do this next
emulator section ad for AD5 (resume the Quickstep app after a gesture; switch off = stock home) and AD7a (power-menu cover on a lock that began in Quickstep), modelled on section x1 (power_mode hide_screenshot, lockpad, longpress POWER, grep action=1 in TjA11y log); then AD4 SecretEntrances; AD8 review last

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  TASKS.md

## Last ten checkpoints
```
  0022ef2 ckpt 400: AD5 coded: gesture unlock resumes the last Quickstep app (ResumeTarget + servi
  36a1d70 ckpt 399: AD7a done: lock-screen power-menu cover works whatever the mode flag says (cov
  8f90f10 ckpt 398: handoff-save written (.handoff + CLAUDE/AGENTS learnings); v0.7.4 work starts:
  73b7614 ckpt 397: AD design notes written (cover is custom-only = AD7a root cause; stock home st
  f1bd41a ckpt 396: Tj 7-item message recorded; found the other session already shipped v0.7.2 and
  951e369 ckpt 395: round 3: rt Calendar survives with the fix (run 157 partial: task 34 present a
  7823e42 ckpt 394: gate round 3 dispatched 01:17 on 63f84b7 (v,ac,rt + A + B); docs: root cause o
  63f84b7 ckpt 393: ROOT CAUSE of gate-B rt: helper recentTaskIds matched ANY line so the OLDEST R
  c31fe3f ckpt 392: gate round 2: B run 147 = 107 PASS 1 FAIL (AA3 Calendar target task gone after
  771e441 ckpt 391: ship.sh gates passed for v0.7.3 (handoff, units 299, lint); emulator gate roun
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
