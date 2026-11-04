# CHECKPOINT 261 — read me first, then TASKS.md

**Written:** 2026-11-04T20:20:46Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `79bb927` (this checkpoint is the commit after it)

## Just done
N,N6 alone (run 62): PASS. In the full run N2's service log has NO 'UNLOCKPICK enter tapped' line = the a11y service did not see the PIN-pad clicks (Enter) in that state. Added UNLOCKCLICK class diagnostics (kind only) + n2-a11y-state/n2-features dumps

## Do this next
dispatch BOTH: full run (empty) and sections 'e1,y2,b6,b4,n,n6' in parallel; read n2-taperase-log (UNLOCKCLICK lines), n2-a11y-state.txt; find why clicks are missing in the full run; fix; then ship v0.5.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M app/src/main/java/com/tj/launcher/TjAccessibilityService.java
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  a61d580 ckpt 260: Run 61 (full, no native crash this time): 80 PASS / 6 FAIL, all in N sections 
  457cb4a ckpt 259: Two full runs (59,60) each failed after ONE native SIGSEGV of our process in A
  943ee38 ckpt 258: Full run 59 (empty sections, 13bc686): 76 PASS / 19 FAIL, first at W5. Root ca
  13bc686 ckpt 257: Run 58 (x) all PASS (S1,S2,T1); ticked O1-O5,P1,Q1-Q4,R1-R3,S1-S3,T1 with proo
  3df79b6 ckpt 256: Run 57 (t): all PASS incl. Q3 (demo flag follows the mode, no pencil in custom
  e58575b ckpt 255: T1 built: device-shell helper lines (plain / sh -c / two-step) in ShellProtoco
  fe11070 ckpt 254: Wrote T1 (adb from a phone / adb OTG app) into TASKS
  5019f17 ckpt 253: ADB_TODO: FAST PATH on top (one session: tools/adb-all.sh / adb-all.bat, then 
  5a4b7aa ckpt 252: Run 55 read: o,t,p = 20 PASS 0 FAIL (Solitaire in front 1.5 s after unlock = r
  01ef1e0 ckpt 251: S1+S2 built: tools/adb-all.sh/.bat + shared probe list (app/src/main/resources
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
