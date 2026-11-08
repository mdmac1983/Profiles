# CHECKPOINT 679 — read me first, then TASKS.md

**Written:** 2026-11-08T05:47:59Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2r9ji2` · **builds on:** `e60f9ad` (this checkpoint is the commit after it)

## Just done
v0.8.9 GATE dispatched 05:47 UTC on e60f9ad (product code final, version 0.8.9 code 34): #351 id 42452132982 = e1,b6,x,ac,ae,ps,uf,cs2,hp,rt; #352 id 42452135020 = base,legacy,x1,y2,b4,o,se,cs,nq,rb; #353 id 42452136967 = n,n6,v,pa,pt,rg,pf,last

## Do this next
about 06:17 UTC: read each run (list_workflow_jobs resource_id=run id -> get_job_logs tail 44; 404 = not ended; confirm the INFO sections line); a FAIL within 15 s after an INFO NATIVE CRASH line is the crash, rerun only those sections; if all 3 are green: bash ship.sh 'note', android.yml full_build=true on the branch, get_release_by_tag v0.8.9, tools/record-release.sh v0.8.9, Release link to Tj as plain text; do NOT push product code (app/ launcher3/) while the gate runs

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  e60f9ad ckpt 678: BI1c: hp FULLY GREEN: run #349 (4d78cfb, id 42451187830) 25 PASS; run #350 (01
  4d78cfb ckpt 677: BI1c: hp run #348 (f78001e, id 42450536427) = 21 PASS 2 FAIL: only HP7 (no dra
  f78001e ckpt 676: BI1c: hp run #347 (4a34b83) = 19 PASS 4 FAIL: phase C FIXED (rested after rest
  4a34b83 ckpt 675: BI1c: takeover; hp run #346 (c69041b) = 18 PASS 5 FAIL, phase C never rested a
  c69041b ckpt 674: BI1c: emulator section hp written (phases A-E: self-calibration, invisible res
  4b6180c ckpt 673: BI1b step 5: Launcher control rows (switch + status + Calibrate now + Forget) 
  6005aa2 ckpt 672: BI1b steps 2-4 (compiles, nothing tested on the emulator yet): HomeShot + Home
```
