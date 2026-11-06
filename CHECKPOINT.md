# CHECKPOINT 513 — read me first, then TASKS.md

**Written:** 2026-11-06T18:21:27Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-8o8uj9` · **builds on:** `0888e76` (this checkpoint is the commit after it)

## Just done
final gate in flight on 0888e76: G1 run 227 id 42269849507 (base,legacy,e1,y2,b6,b4,n,n6,o,t,x), G2 run 228 id 42269854118 (ka,ac,ad,se,rt,im), G3 run 229 id 42269858942 (lq,ao,pf,rb); started 18:20-18:21Z, expect G3 ~18:36, G2 ~18:38, G1 ~18:41

## Do this next
read the 3 runs: list_workflow_jobs (resource_id = run id) then get_job_logs tail 80 (404 = still running); all green -> bump 0.8.0 code 25, ship.sh, android.yml full_build, record-release, tell Tj; meanwhile research/TABLET_CHECKLIST_v0.8.0.md is being drafted

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  0888e76 ckpt 512: takeover done: ps control fixed and PROVEN (run 226 15/0), rg alone green (run
  130c89a ckpt 511: ps red-crop control FIXED (test-only, tools/emulator_scenario.sh): bare rebind
  9ff5695 ckpt 510: TAKEOVER by branch claude/new-session-8o8uj9 (ff to 7a9vpi tip 024ede6; old ch
  024ede6 ckpt 509: runs 219 (e1,y2,b4,n,o,x,ad,lq) GREEN; run 222 ps: resting decision rest=true 
  0ac789f ckpt 508: ps run 218: after a helper restart + lock the patch window EXISTS (3 dumpsys l
  f5e5278 ckpt 507: ps run 217: the helper log is empty because the calibration happened BEFORE th
  14ad080 ckpt 506: ps run 216: positive control still fails (red crop NOT visible at rest) and th
  274f0b7 ckpt 505: ps positive control (run 215) FAILED: with a solid RED crop the resting patch 
  994ba9b ckpt 504: runs 213/214 read: base GREEN (Control page: every group header + Restore foot
  24420bb ckpt 503: runs 210/211/212 read: uf WORKS on the emulator (plain PIN in custom: one resu
```
