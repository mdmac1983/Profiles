# CHECKPOINT 704 — read me first, then TASKS.md

**Written:** 2026-11-08T17:49:46Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-dda19ce3-8dl2cy` · **builds on:** `faf153d` (this checkpoint is the commit after it)

## Just done
v0.8.10 (code 35) RELEASE BUILD triggered 17:49:40 UTC: android.yml full_build=true on main at faf153d (ship.sh green: handoff test, 707+ unit tests, lint; the product code is the gated b0d29fd + the version bump only)

## Do this next
read the android.yml run (workflow android.yml, branch main, event workflow_dispatch, perPage 1): green -> get_release_by_tag v0.8.10 -> bash tools/record-release.sh v0.8.10 '<the ship note>' (only after green) -> tick BJ1 note in TASKS (BJ1e waits on Tj) -> give Tj the Release link as plain text on its own line

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  faf153d ckpt 703: gated v0.8.10 (code 35) and pushed it: the handoff self-test, the full unit su
  2864951 ckpt 702: BJ1 GATE READ: #384 b6,n,o,hp,last SUCCESS; #385 e1,pt SUCCESS; #387 hp,hf on 
  b0d29fd ckpt 701: BJ1: one more guard (code re-read): fastMenuOpen ends a home-flavour wait star
  fcee0dc ckpt 700: BJ1: docs written (HOME_PATCH design doc section with the measured table, TABL
  43a5da9 ckpt 699: BJ1 GATE on head 783b81a dispatched 17:11 UTC: run #384 id 42503687941 = b6,n,
  783b81a ckpt 698: BJ1c: run #383 (id 42502655352, head 825ae3d) VERIFIED the fix: hf 10 PASS 0 F
  5afdc9d ckpt 697: BJ1c: /code-review high (8 findings) handled: fade summary moved out of the la
  825ae3d ckpt 696: BJ1c: run #382 PROVED the cause (home started no wait for a burst's first wind
  22a8038 ckpt 695: BJ1b: per-fade frame statistics (first draw lateness, frames, slow frames, wor
  e321189 ckpt 694: BJ1b: emulator.yml run #382 id 42500697309 (sections=hf, head ae64eec) dispatc
```
