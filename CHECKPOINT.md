# CHECKPOINT 415 — read me first, then TASKS.md

**Written:** 2026-11-06T02:58:58Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-22ba09da-s9vh1t` · **builds on:** `a67f455` (this checkpoint is the commit after it)

## Just done
TK2: merged v0.7.4 branch in, MIRROR_MAIN off; android.yml full_build dispatched on main 02:58Z for v0.7.3

## Do this next
WAIT till ~03:12Z, list android.yml run (branch main, workflow_dispatch, perPage 2), need success; get_release_by_tag v0.7.3; then record on main via worktree: tools/record-release.sh v0.7.3 (BEFORE bump). Do NOT dispatch again.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  7d422c9 ckpt 414: handoff #2 written: handoff-launcher.md (repo root, chained from #1); v0.7.3 r
  2d3eead ckpt 413: v0.7.3 gate complete by composition: im (run 167) + x (run 168) green alone on
  21d13e2 ckpt 412: PG3 fix coded (TabletReport.cutAtLine/newestWithin/SUMMARY_MAX 11000 + TabletR
  3cf26c8 ckpt 411: v0.7.3 reruns read: A 120/1 (PG3 summary size), B 107/1 (AA1 im); rt passes; x
  8e27866 ckpt 410: TK1-TK3 recorded in TASKS (takeover request in Tj's words, v0.7.3 finish, v0.7
  5959584 ckpt 409: AD8: cached stock lookup, measured cost lines in the report (memory, start-up,
  fe924ae ckpt 408: takeover protocol written: HANDOFF 0d, resume.sh sibling NOTE, CLAUDE.md point
  5f2a5f8 ckpt 407: se scenario: charger test starts unplugged; v0.7.3 gate round 3 read (A 2 FAIL
  f39bdf9 ckpt 406: se scenario: rotation toggles in one adb call (rotchain), swipes in one call (
  441a88b ckpt 405: code-review of the AD changes: 8 fixed, 1 kept (MIRROR_MAIN off is intentional
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
