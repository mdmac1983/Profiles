# CHECKPOINT 263 — read me first, then TASKS.md

**Written:** 2026-11-04T20:48:18Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `4a96fa2` (this checkpoint is the commit after it)

## Just done
Run 65 (race fix #1): Enter click now arrives but classifies 'none' (keyguard key already torn down). Fix #2: UnlockMethods.hasPendingPad(); at USER_PRESENT (applyUnlockChoice) a pending pad entry is evaluated as the Enter (service logs 'Enter click not recognised'); JVM test added, unit suite green

## Do this next
dispatch sections e1,y2,b6,b4,n,n6 to confirm N2; meanwhile read FULL run 63 (id 41905740966) FAILs for anything besides N2; then FINAL full run; then ship v0.5.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  ee9d8c8 ckpt 262: Run 64 (sections e1,y2,b6,b4,n,n6): only N2 FAILS and it is a real race: the s
  ea69503 ckpt 261: N,N6 alone (run 62): PASS. In the full run N2's service log has NO 'UNLOCKPICK
  a61d580 ckpt 260: Run 61 (full, no native crash this time): 80 PASS / 6 FAIL, all in N sections 
  457cb4a ckpt 259: Two full runs (59,60) each failed after ONE native SIGSEGV of our process in A
  943ee38 ckpt 258: Full run 59 (empty sections, 13bc686): 76 PASS / 19 FAIL, first at W5. Root ca
  13bc686 ckpt 257: Run 58 (x) all PASS (S1,S2,T1); ticked O1-O5,P1,Q1-Q4,R1-R3,S1-S3,T1 with proo
  3df79b6 ckpt 256: Run 57 (t): all PASS incl. Q3 (demo flag follows the mode, no pencil in custom
  e58575b ckpt 255: T1 built: device-shell helper lines (plain / sh -c / two-step) in ShellProtoco
  fe11070 ckpt 254: Wrote T1 (adb from a phone / adb OTG app) into TASKS
  5019f17 ckpt 253: ADB_TODO: FAST PATH on top (one session: tools/adb-all.sh / adb-all.bat, then 
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
