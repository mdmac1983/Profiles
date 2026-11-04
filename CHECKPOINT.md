# CHECKPOINT 257 — read me first, then TASKS.md

**Written:** 2026-11-04T18:57:58Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `fbe1d56` (this checkpoint is the commit after it)

## Just done
Run 58 (x) all PASS (S1,S2,T1); ticked O1-O5,P1,Q1-Q4,R1-R3,S1-S3,T1 with proofs; bumped to 0.5.0/15

## Do this next
dispatch FULL emulator run (empty sections) on branch; if 0 FAIL: bash ship.sh, android.yml full_build=true on main, get_release_by_tag, record-release v0.5.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md
     M app/build.gradle.kts

## Last ten checkpoints
```
  3df79b6 ckpt 256: Run 57 (t): all PASS incl. Q3 (demo flag follows the mode, no pencil in custom
  e58575b ckpt 255: T1 built: device-shell helper lines (plain / sh -c / two-step) in ShellProtoco
  fe11070 ckpt 254: Wrote T1 (adb from a phone / adb OTG app) into TASKS
  5019f17 ckpt 253: ADB_TODO: FAST PATH on top (one session: tools/adb-all.sh / adb-all.bat, then 
  5a4b7aa ckpt 252: Run 55 read: o,t,p = 20 PASS 0 FAIL (Solitaire in front 1.5 s after unlock = r
  01ef1e0 ckpt 251: S1+S2 built: tools/adb-all.sh/.bat + shared probe list (app/src/main/resources
  51c2ccb ckpt 250: S1: tools/adb-all.sh (+ .bat for Windows, CRLF) + tools/probe-commands.txt (on
  3893c30 ckpt 249: Wrote S1-S3 (one-session adb: PC script + in-app Tablet setup & report via the
  3f46b2f ckpt 248: Run 54 (s) read: pm suspend works (Recents card removed, process stays, card N
  75dc67a ckpt 247: R2 done: research/ADB_TODO.md (every adb command in order: setup, permissions 
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
