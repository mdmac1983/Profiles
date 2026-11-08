# CHECKPOINT 681 — read me first, then TASKS.md

**Written:** 2026-11-08T06:05:30Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2r9ji2` · **builds on:** `9c40e5c` (this checkpoint is the commit after it)

## Just done
v0.8.9 GATE dispatched 06:05 UTC on 9c40e5c (hp alone on it = #354 28 PASS): #355 id 42453270341 base,e1,o,x,se,ae,pt,rt; #356 id 42453272422 b6,n,n6,v,pa,rg,cs2,hp,pf,last; #357 id 42453274982 legacy,x1,y2,b4,ac,ps,uf,cs,nq,rb

## Do this next
about 06:36 UTC read each run (list_workflow_jobs resource_id=run id, get_job_logs job_id tail 48; 404 = not ended; check the INFO sections line); NATIVE CRASH line = known flake: rerun only the sections that failed within 15 s after it; all green: bash ship.sh note, android.yml full_build=true on branch claude/new-session-2r9ji2, get_release_by_tag v0.8.9, tools/record-release.sh v0.8.9, Release link to Tj. No product code (app/ launcher3/) pushes until then

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  9c40e5c ckpt 680: v0.8.9: first gate (#351-353 on e60f9ad) CANCELLED at 05:55 UTC on purpose: a 
  fd738df ckpt 679: v0.8.9 GATE dispatched 05:47 UTC on e60f9ad (product code final, version 0.8.9
  e60f9ad ckpt 678: BI1c: hp FULLY GREEN: run #349 (4d78cfb, id 42451187830) 25 PASS; run #350 (01
  4d78cfb ckpt 677: BI1c: hp run #348 (f78001e, id 42450536427) = 21 PASS 2 FAIL: only HP7 (no dra
  f78001e ckpt 676: BI1c: hp run #347 (4a34b83) = 19 PASS 4 FAIL: phase C FIXED (rested after rest
  4a34b83 ckpt 675: BI1c: takeover; hp run #346 (c69041b) = 18 PASS 5 FAIL, phase C never rested a
  c69041b ckpt 674: BI1c: emulator section hp written (phases A-E: self-calibration, invisible res
  4b6180c ckpt 673: BI1b step 5: Launcher control rows (switch + status + Calibrate now + Forget) 
  6005aa2 ckpt 672: BI1b steps 2-4 (compiles, nothing tested on the emulator yet): HomeShot + Home
```
