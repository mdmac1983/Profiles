# CHECKPOINT 412 — read me first, then TASKS.md

**Written:** 2026-11-06T02:41:20Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-9ab65fa5-vp812y` · **builds on:** `384bcc8` (this checkpoint is the commit after it)

## Just done
PG3 fix coded (TabletReport.cutAtLine/newestWithin/SUMMARY_MAX 11000 + TabletReportTest); local unit+lint running; im (167) and x (168) re-run on main

## Do this next
1) read the local gradle log (scratchpad gradle-pg3.log: EXIT=0, unit count ~351); 2) read runs 167 (im) and 168 (x) on main (job logs tail 62): both 0 FAIL => ship v0.7.3 per TK2; 3) dispatch x on THIS branch to prove the cap (PG3 check)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  3cf26c8 ckpt 411: v0.7.3 reruns read: A 120/1 (PG3 summary size), B 107/1 (AA1 im); rt passes; x
  8e27866 ckpt 410: TK1-TK3 recorded in TASKS (takeover request in Tj's words, v0.7.3 finish, v0.7
  5959584 ckpt 409: AD8: cached stock lookup, measured cost lines in the report (memory, start-up,
  fe924ae ckpt 408: takeover protocol written: HANDOFF 0d, resume.sh sibling NOTE, CLAUDE.md point
  5f2a5f8 ckpt 407: se scenario: charger test starts unplugged; v0.7.3 gate round 3 read (A 2 FAIL
  f39bdf9 ckpt 406: se scenario: rotation toggles in one adb call (rotchain), swipes in one call (
  441a88b ckpt 405: code-review of the AD changes: 8 fixed, 1 kept (MIRROR_MAIN off is intentional
  488c287 ckpt 404: AD8 start: onTrimMemory releases cached wallpaper bitmaps; report prints secre
  434c425 ckpt 403: AD4 coded: SecretEntrances (page flick, rotation tile, charger flick) + Entran
  fca1ebc ckpt 402: emulator section ad written (AD5 resume + AD7a cover from a Quickstep lock + A
```

(6 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
