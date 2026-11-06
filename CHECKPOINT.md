# CHECKPOINT 411 — read me first, then TASKS.md

**Written:** 2026-11-06T02:39:06Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-9ab65fa5-vp812y` · **builds on:** `11eba7f` (this checkpoint is the commit after it)

## Just done
v0.7.3 reruns read: A 120/1 (PG3 summary size), B 107/1 (AA1 im); rt passes; x and im re-run alone on main 02:41; x run 165 + ad run 166 green on this branch

## Do this next
read the x and im runs on main (list in_progress/queued then perPage runs of emulator.yml branch main; job logs tail 62); both green => ship v0.7.3 (android.yml full_build on main, get_release_by_tag, record on MAIN); either red => investigate, no release. Meanwhile: cap the short summary (TabletReport.summary) on this branch

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  8e27866 ckpt 410: TK1-TK3 recorded in TASKS (takeover request in Tj's words, v0.7.3 finish, v0.7
  5959584 ckpt 409: AD8: cached stock lookup, measured cost lines in the report (memory, start-up,
  fe924ae ckpt 408: takeover protocol written: HANDOFF 0d, resume.sh sibling NOTE, CLAUDE.md point
  5f2a5f8 ckpt 407: se scenario: charger test starts unplugged; v0.7.3 gate round 3 read (A 2 FAIL
  f39bdf9 ckpt 406: se scenario: rotation toggles in one adb call (rotchain), swipes in one call (
  441a88b ckpt 405: code-review of the AD changes: 8 fixed, 1 kept (MIRROR_MAIN off is intentional
  488c287 ckpt 404: AD8 start: onTrimMemory releases cached wallpaper bitmaps; report prints secre
  434c425 ckpt 403: AD4 coded: SecretEntrances (page flick, rotation tile, charger flick) + Entran
  fca1ebc ckpt 402: emulator section ad written (AD5 resume + AD7a cover from a Quickstep lock + A
  9a0d2a2 ckpt 401: Tj's tablet-optimisation request recorded (AD8); the attached report is identi
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
