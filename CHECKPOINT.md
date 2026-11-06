# CHECKPOINT 400 — read me first, then TASKS.md

**Written:** 2026-11-06T01:59:20Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-9ab65fa5-vp812y` · **builds on:** `cc72d4f` (this checkpoint is the commit after it)

## Just done
AD5 coded: gesture unlock resumes the last Quickstep app (ResumeTarget + service wiring + Control switch default ON), unit 316 green, lint ok; AD7a done

## Do this next
AD5 emulator proof + scenario baseline: existing sections that open an app in Quickstep, lock, gesture and expect the STOCK home need resume_qs_app=false in their feature maps (add after startup_open in every map-writing function), then new section 'ad' (AD5 resume + AD7a cover on a lock that began in Quickstep); then AD4 SecretEntrances

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  36a1d70 ckpt 399: AD7a done: lock-screen power-menu cover works whatever the mode flag says (cov
  8f90f10 ckpt 398: handoff-save written (.handoff + CLAUDE/AGENTS learnings); v0.7.4 work starts:
  73b7614 ckpt 397: AD design notes written (cover is custom-only = AD7a root cause; stock home st
  f1bd41a ckpt 396: Tj 7-item message recorded; found the other session already shipped v0.7.2 and
  951e369 ckpt 395: round 3: rt Calendar survives with the fix (run 157 partial: task 34 present a
  7823e42 ckpt 394: gate round 3 dispatched 01:17 on 63f84b7 (v,ac,rt + A + B); docs: root cause o
  63f84b7 ckpt 393: ROOT CAUSE of gate-B rt: helper recentTaskIds matched ANY line so the OLDEST R
  c31fe3f ckpt 392: gate round 2: B run 147 = 107 PASS 1 FAIL (AA3 Calendar target task gone after
  771e441 ckpt 391: ship.sh gates passed for v0.7.3 (handoff, units 299, lint); emulator gate roun
  c027bd7 ckpt 390: gated v0.7.3 (code 20) and pushed it: the handoff self-test, the full unit sui
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
