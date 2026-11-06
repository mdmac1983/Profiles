# CHECKPOINT 407 — read me first, then TASKS.md

**Written:** 2026-11-06T02:13:56Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-9ab65fa5-vp812y` · **builds on:** `f39bdf9` (this checkpoint is the commit after it)

## Just done
se scenario: charger test starts unplugged; v0.7.3 gate round 3 read (A 2 FAIL in x1, B 4 FAIL in rt, v,ac,rt green); A and B re-run

## Do this next
dispatch se (and ad) again on this head; read reruns of runs 158/159 (main, v0.7.3) ~02:45-03:05; if they pass = flake: v0.7.3 can ship from main (android.yml full_build on main, record-release v0.7.3) - the other session seems dead; then merge main here, gate the union as v0.7.4

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  f39bdf9 ckpt 406: se scenario: rotation toggles in one adb call (rotchain), swipes in one call (
  441a88b ckpt 405: code-review of the AD changes: 8 fixed, 1 kept (MIRROR_MAIN off is intentional
  488c287 ckpt 404: AD8 start: onTrimMemory releases cached wallpaper bitmaps; report prints secre
  434c425 ckpt 403: AD4 coded: SecretEntrances (page flick, rotation tile, charger flick) + Entran
  fca1ebc ckpt 402: emulator section ad written (AD5 resume + AD7a cover from a Quickstep lock + A
  9a0d2a2 ckpt 401: Tj's tablet-optimisation request recorded (AD8); the attached report is identi
  0022ef2 ckpt 400: AD5 coded: gesture unlock resumes the last Quickstep app (ResumeTarget + servi
  36a1d70 ckpt 399: AD7a done: lock-screen power-menu cover works whatever the mode flag says (cov
  8f90f10 ckpt 398: handoff-save written (.handoff + CLAUDE/AGENTS learnings); v0.7.4 work starts:
  73b7614 ckpt 397: AD design notes written (cover is custom-only = AD7a root cause; stock home st
```
