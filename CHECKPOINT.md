# CHECKPOINT 264 — read me first, then TASKS.md

**Written:** 2026-11-04T20:59:18Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `306f852` (this checkpoint is the commit after it)

## Just done
Run 66 (short, fix #2): N2 still failed because onPad(false) clears the pad BEFORE USER_PRESENT, so hasPendingPad() was false; N6/B4 failures in that run = section-only runs had no default home (ResolverActivity chooser). Fix #3: UnlockMethods remembers the pad dismissed <=3 s before the unlock (finishPendingPad) + 2 JVM tests (unit suite green); scenario ensurehome() at the start of every section except base/legacy/last

## Do this next
dispatch sections e1,y2,b6,b4,n,n6,o,t (~12 min) to confirm N2/N6/B4/O5; then FINAL full run; ship v0.5.0 only if green. Always read runs with actions_get get_workflow_run by id; list_workflow_runs is huge

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  0b229ab ckpt 263: Run 65 (race fix #1): Enter click now arrives but classifies 'none' (keyguard 
  ee9d8c8 ckpt 262: Run 64 (sections e1,y2,b6,b4,n,n6): only N2 FAILS and it is a real race: the s
  ea69503 ckpt 261: N,N6 alone (run 62): PASS. In the full run N2's service log has NO 'UNLOCKPICK
  a61d580 ckpt 260: Run 61 (full, no native crash this time): 80 PASS / 6 FAIL, all in N sections 
  457cb4a ckpt 259: Two full runs (59,60) each failed after ONE native SIGSEGV of our process in A
  943ee38 ckpt 258: Full run 59 (empty sections, 13bc686): 76 PASS / 19 FAIL, first at W5. Root ca
  13bc686 ckpt 257: Run 58 (x) all PASS (S1,S2,T1); ticked O1-O5,P1,Q1-Q4,R1-R3,S1-S3,T1 with proo
  3df79b6 ckpt 256: Run 57 (t): all PASS incl. Q3 (demo flag follows the mode, no pencil in custom
  e58575b ckpt 255: T1 built: device-shell helper lines (plain / sh -c / two-step) in ShellProtoco
  fe11070 ckpt 254: Wrote T1 (adb from a phone / adb OTG app) into TASKS
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
