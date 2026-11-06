# CHECKPOINT 397 — read me first, then TASKS.md

**Written:** 2026-11-06T01:47:39Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-9ab65fa5-vp812y` · **builds on:** `f1bd41a` (this checkpoint is the commit after it)

## Just done
AD design notes written (cover is custom-only = AD7a root cause; stock home start = AD5 root cause; entrances plan); branch separate from main

## Do this next
reply to Tj with the map (v0.7.2 out, v0.7.3 in last test round by the other session, v0.7.4 = AD4/AD5/AD7); then build on this branch: 1) AD7a cover world = custom || keyguardLocked (PowerMenuRule + service conditions + JVM test) 2) AD5 resume last Quickstep app 3) AD4 SecretEntrances (page flick, rotate tile, charger) + Control section + JVM tests + emulator section 'se'; never push to main; before integration check get_release_by_tag v0.7.3

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  f1bd41a ckpt 396: Tj 7-item message recorded; found the other session already shipped v0.7.2 and
  951e369 ckpt 395: round 3: rt Calendar survives with the fix (run 157 partial: task 34 present a
  7823e42 ckpt 394: gate round 3 dispatched 01:17 on 63f84b7 (v,ac,rt + A + B); docs: root cause o
  63f84b7 ckpt 393: ROOT CAUSE of gate-B rt: helper recentTaskIds matched ANY line so the OLDEST R
  c31fe3f ckpt 392: gate round 2: B run 147 = 107 PASS 1 FAIL (AA3 Calendar target task gone after
  771e441 ckpt 391: ship.sh gates passed for v0.7.3 (handoff, units 299, lint); emulator gate roun
  c027bd7 ckpt 390: gated v0.7.3 (code 20) and pushed it: the handoff self-test, the full unit sui
  c78a967 ckpt 389: final gate round 2 on 6729d90 dispatched 00:23 (A + B); pf fix verified alone 
  bcbaa95 ckpt 388: pf root cause fixed: app under lock screen no longer counted as Quickstep-open
  403ef01 ckpt 387: pf/rt regression hunt: pf alone fails (ctl in AppOrigin.quickstep before lock;
```
