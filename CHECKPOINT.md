# CHECKPOINT 258 — read me first, then TASKS.md

**Written:** 2026-11-04T19:21:47Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `13bc686` (this checkpoint is the commit after it)

## Just done
Full run 59 (empty sections, 13bc686): 76 PASS / 19 FAIL, first at W5. Root cause candidate: native SIGSEGV in libz inflateEnd/free (heap corruption in Android's own zip code) in launcher-loader at 19:06:23 while loading a default icon -> crash breaker fail-open -> later checks cascade (W3b,E1,Y2,B4,N2,N3,N6,O5,Q3). Same sections all passed in isolation (runs 57,58). Re-running the full gate once to see if it repeats

## Do this next
dispatch full emulator run again (empty sections); if 0 FAIL ship v0.5.0; if the same W5 failure repeats, investigate the crash breaker/fail-open (Mode.failOpenUntil) and the sections order; use actions_get get_workflow_run NOT list_workflow_runs

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  13bc686 ckpt 257: Run 58 (x) all PASS (S1,S2,T1); ticked O1-O5,P1,Q1-Q4,R1-R3,S1-S3,T1 with proo
  3df79b6 ckpt 256: Run 57 (t): all PASS incl. Q3 (demo flag follows the mode, no pencil in custom
  e58575b ckpt 255: T1 built: device-shell helper lines (plain / sh -c / two-step) in ShellProtoco
  fe11070 ckpt 254: Wrote T1 (adb from a phone / adb OTG app) into TASKS
  5019f17 ckpt 253: ADB_TODO: FAST PATH on top (one session: tools/adb-all.sh / adb-all.bat, then 
  5a4b7aa ckpt 252: Run 55 read: o,t,p = 20 PASS 0 FAIL (Solitaire in front 1.5 s after unlock = r
  01ef1e0 ckpt 251: S1+S2 built: tools/adb-all.sh/.bat + shared probe list (app/src/main/resources
  51c2ccb ckpt 250: S1: tools/adb-all.sh (+ .bat for Windows, CRLF) + tools/probe-commands.txt (on
  3893c30 ckpt 249: Wrote S1-S3 (one-session adb: PC script + in-app Tablet setup & report via the
  3f46b2f ckpt 248: Run 54 (s) read: pm suspend works (Recents card removed, process stays, card N
```
