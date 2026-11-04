# CHECKPOINT 265 — read me first, then TASKS.md

**Written:** 2026-11-04T21:18:28Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `92b77fc` (this checkpoint is the commit after it)

## Just done
Run 67 (sections e1,y2,b6,b4,n,n6,o,t with fixes #2/#3 + ensurehome): ALL PASS (N2, N6, B4, O5 green). Dispatched the FINAL full run

## Do this next
read the final full run (dispatched ~21:20 UTC; find id with actions_list list_workflow_runs workflow_runs_filter created>2026-11-04T21:19:00Z per_page 1 (huge output) then actions_get get_workflow_run; ~20 min); if 0 FAIL ship v0.5.0: bash ship.sh, android.yml full_build=true main, get_release_by_tag v0.5.0, record-release; else fix only FAILs outside a NATIVE CRASH window

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  92b77fc ckpt 264: Run 66 (short, fix #2): N2 still failed because onPad(false) clears the pad BE
  0b229ab ckpt 263: Run 65 (race fix #1): Enter click now arrives but classifies 'none' (keyguard 
  ee9d8c8 ckpt 262: Run 64 (sections e1,y2,b6,b4,n,n6): only N2 FAILS and it is a real race: the s
  ea69503 ckpt 261: N,N6 alone (run 62): PASS. In the full run N2's service log has NO 'UNLOCKPICK
  a61d580 ckpt 260: Run 61 (full, no native crash this time): 80 PASS / 6 FAIL, all in N sections 
  457cb4a ckpt 259: Two full runs (59,60) each failed after ONE native SIGSEGV of our process in A
  943ee38 ckpt 258: Full run 59 (empty sections, 13bc686): 76 PASS / 19 FAIL, first at W5. Root ca
  13bc686 ckpt 257: Run 58 (x) all PASS (S1,S2,T1); ticked O1-O5,P1,Q1-Q4,R1-R3,S1-S3,T1 with proo
  3df79b6 ckpt 256: Run 57 (t): all PASS incl. Q3 (demo flag follows the mode, no pencil in custom
  e58575b ckpt 255: T1 built: device-shell helper lines (plain / sh -c / two-step) in ShellProtoco
```
