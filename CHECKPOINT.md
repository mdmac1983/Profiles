# CHECKPOINT 682 — read me first, then TASKS.md

**Written:** 2026-11-08T06:40:32Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2r9ji2` · **builds on:** `17cebf9` (this checkpoint is the commit after it)

## Just done
v0.8.9 GATE READ 06:40: #356 (b6,n,n6,v,pa,rg,cs2,hp,pf,last) 89 PASS 2 FAIL = only HP4 (patch 'not resting' at the open in a long run; hp alone passes: foreign window?); #355 (base,e1,o,x,se,ae,pt,rt) 130 PASS 3 FAIL = pt PB checks (patch scale 0.7497 vs stock 0.800 = -6.6 levels) with an INFO NATIVE CRASH line; #357 (legacy,x1,y2,b4,ac,ps,uf,cs,nq,rb) job 128646832646 still running. Reruns: #358 id 42455628922 n,cs2,hp (HP4 diagnostics added), #359 id 42455684349 pt alone, both on 17cebf9

## Do this next
read #358 and #359 (list_workflow_jobs resource_id=run id, get_job_logs tail 46 for hp, a subagent for pt) and #357 (job 128646832646, tail 60); HP4: if the INFO 'HP B tail' / 'HP4 log' names a foreign window (pkg cls) decide whether the rule should ignore it, else make the test wait longer; pt: pass = flake (native crash), fail = compare against b1399c9 before touching the lock patch; then ship only when every section is green on the final product commit

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  3d26ef4 ckpt 681: v0.8.9 GATE dispatched 06:05 UTC on 9c40e5c (hp alone on it = #354 28 PASS): #
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

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
