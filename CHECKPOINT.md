# CHECKPOINT 398 — read me first, then TASKS.md

**Written:** 2026-11-06T01:53:41Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-9ab65fa5-vp812y` · **builds on:** `73b7614` (this checkpoint is the commit after it)

## Just done
handoff-save written (.handoff + CLAUDE/AGENTS learnings); v0.7.4 work starts: AD7a first

## Do this next
implement AD7a (cover works whenever keyguard is up) in PowerMenuRule + TjAccessibilityService conditions + JVM test; then AD5; then AD4; remember: never push to main, MIRROR_MAIN off here, check get_release_by_tag v0.7.3 before integration

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M .handoff/handoff.json
     M .handoff/handoff.md
     M .handoff/handoffs/index.md
     M AGENTS.md
     M CHECKPOINT.md
     M CLAUDE.md
    ?? .handoff/handoffs/2026-11-06/

## Last ten checkpoints
```
  73b7614 ckpt 397: AD design notes written (cover is custom-only = AD7a root cause; stock home st
  f1bd41a ckpt 396: Tj 7-item message recorded; found the other session already shipped v0.7.2 and
  951e369 ckpt 395: round 3: rt Calendar survives with the fix (run 157 partial: task 34 present a
  7823e42 ckpt 394: gate round 3 dispatched 01:17 on 63f84b7 (v,ac,rt + A + B); docs: root cause o
  63f84b7 ckpt 393: ROOT CAUSE of gate-B rt: helper recentTaskIds matched ANY line so the OLDEST R
  c31fe3f ckpt 392: gate round 2: B run 147 = 107 PASS 1 FAIL (AA3 Calendar target task gone after
  771e441 ckpt 391: ship.sh gates passed for v0.7.3 (handoff, units 299, lint); emulator gate roun
  c027bd7 ckpt 390: gated v0.7.3 (code 20) and pushed it: the handoff self-test, the full unit sui
  c78a967 ckpt 389: final gate round 2 on 6729d90 dispatched 00:23 (A + B); pf fix verified alone 
  bcbaa95 ckpt 388: pf root cause fixed: app under lock screen no longer counted as Quickstep-open
```
