# CHECKPOINT 266 — read me first, then TASKS.md

**Written:** 2026-11-04T21:40:36Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `7b73448` (this checkpoint is the commit after it)

## Just done
Final full run 68: 90 PASS / 0 FAIL, no native crash, but the job was CUT by the scenario's 'timeout 1000' (16.7 min) during section t (O5), so u,s,p,x,last never ran and the run shows failure. Raised scenario timeout to 2100 s and the job to 50 min; CLAUDE.md note

## Do this next
dispatch the FULL run again (empty sections; ~25 min); read via get_workflow_run by id; if 0 FAIL ship v0.5.0 (bash ship.sh, android.yml full_build=true main, get_release_by_tag v0.5.0, record-release)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M .github/workflows/emulator.yml
     M CHECKPOINT.md
     M CLAUDE.md

## Last ten checkpoints
```
  7b73448 ckpt 265: Run 67 (sections e1,y2,b6,b4,n,n6,o,t with fixes #2/#3 + ensurehome): ALL PASS
  92b77fc ckpt 264: Run 66 (short, fix #2): N2 still failed because onPad(false) clears the pad BE
  0b229ab ckpt 263: Run 65 (race fix #1): Enter click now arrives but classifies 'none' (keyguard 
  ee9d8c8 ckpt 262: Run 64 (sections e1,y2,b6,b4,n,n6): only N2 FAILS and it is a real race: the s
  ea69503 ckpt 261: N,N6 alone (run 62): PASS. In the full run N2's service log has NO 'UNLOCKPICK
  a61d580 ckpt 260: Run 61 (full, no native crash this time): 80 PASS / 6 FAIL, all in N sections 
  457cb4a ckpt 259: Two full runs (59,60) each failed after ONE native SIGSEGV of our process in A
  943ee38 ckpt 258: Full run 59 (empty sections, 13bc686): 76 PASS / 19 FAIL, first at W5. Root ca
  13bc686 ckpt 257: Run 58 (x) all PASS (S1,S2,T1); ticked O1-O5,P1,Q1-Q4,R1-R3,S1-S3,T1 with proo
  3df79b6 ckpt 256: Run 57 (t): all PASS incl. Q3 (demo flag follows the mode, no pencil in custom
```
