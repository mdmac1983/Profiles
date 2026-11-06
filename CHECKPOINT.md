# CHECKPOINT 405 — read me first, then TASKS.md

**Written:** 2026-11-06T02:10:52Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-9ab65fa5-vp812y` · **builds on:** `4fdd637` (this checkpoint is the commit after it)

## Just done
code-review of the AD changes: 8 fixed, 1 kept (MIRROR_MAIN off is intentional); ad run 161 = 11 PASS 0 FAIL; se run 162 in flight

## Do this next
read run 162 (se) RESULTS; re-dispatch se and ad on the FIXED commit (both ~5 min); then gate: A and B on this branch with ad,se added to B; integration only after v0.7.3 ships (get_release_by_tag v0.7.3): merge origin/main, resolve CHECKPOINT/TASKS/CLAUDE conflicts, MIRROR_MAIN back to on

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  488c287 ckpt 404: AD8 start: onTrimMemory releases cached wallpaper bitmaps; report prints secre
  434c425 ckpt 403: AD4 coded: SecretEntrances (page flick, rotation tile, charger flick) + Entran
  fca1ebc ckpt 402: emulator section ad written (AD5 resume + AD7a cover from a Quickstep lock + A
  9a0d2a2 ckpt 401: Tj's tablet-optimisation request recorded (AD8); the attached report is identi
  0022ef2 ckpt 400: AD5 coded: gesture unlock resumes the last Quickstep app (ResumeTarget + servi
  36a1d70 ckpt 399: AD7a done: lock-screen power-menu cover works whatever the mode flag says (cov
  8f90f10 ckpt 398: handoff-save written (.handoff + CLAUDE/AGENTS learnings); v0.7.4 work starts:
  73b7614 ckpt 397: AD design notes written (cover is custom-only = AD7a root cause; stock home st
  f1bd41a ckpt 396: Tj 7-item message recorded; found the other session already shipped v0.7.2 and
  951e369 ckpt 395: round 3: rt Calendar survives with the fix (run 157 partial: task 34 present a
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
