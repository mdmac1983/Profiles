# CHECKPOINT 259 — read me first, then TASKS.md

**Written:** 2026-11-04T19:49:40Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `7084a77` (this checkpoint is the commit after it)

## Just done
Two full runs (59,60) each failed after ONE native SIGSEGV of our process in Android system libs (libz inflate / libhwui; run 51 had none): B1 clears default home after a crash -> cascade of ~19 FAILs. Scenario heal(): detects the crash (logcat -b all 'Fatal signal.*(com.tj.launcher)'), logs INFO NATIVE CRASH, saves native-crash-N.txt, re-asserts home + rebind; CLAUDE.md note

## Do this next
dispatch full emulator run (empty sections) with heal(); read with actions_get get_workflow_run (NOT list_workflow_runs); if 0 FAIL or FAILs only inside a NATIVE CRASH window -> rerun the affected sections; then ship v0.5.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M CLAUDE.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  943ee38 ckpt 258: Full run 59 (empty sections, 13bc686): 76 PASS / 19 FAIL, first at W5. Root ca
  13bc686 ckpt 257: Run 58 (x) all PASS (S1,S2,T1); ticked O1-O5,P1,Q1-Q4,R1-R3,S1-S3,T1 with proo
  3df79b6 ckpt 256: Run 57 (t): all PASS incl. Q3 (demo flag follows the mode, no pencil in custom
  e58575b ckpt 255: T1 built: device-shell helper lines (plain / sh -c / two-step) in ShellProtoco
  fe11070 ckpt 254: Wrote T1 (adb from a phone / adb OTG app) into TASKS
  5019f17 ckpt 253: ADB_TODO: FAST PATH on top (one session: tools/adb-all.sh / adb-all.bat, then 
  5a4b7aa ckpt 252: Run 55 read: o,t,p = 20 PASS 0 FAIL (Solitaire in front 1.5 s after unlock = r
  01ef1e0 ckpt 251: S1+S2 built: tools/adb-all.sh/.bat + shared probe list (app/src/main/resources
  51c2ccb ckpt 250: S1: tools/adb-all.sh (+ .bat for Windows, CRLF) + tools/probe-commands.txt (on
  3893c30 ckpt 249: Wrote S1-S3 (one-session adb: PC script + in-app Tablet setup & report via the
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
