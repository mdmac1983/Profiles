# CHECKPOINT 678 — read me first, then TASKS.md

**Written:** 2026-11-08T05:47:16Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2r9ji2` · **builds on:** `ba51853` (this checkpoint is the commit after it)

## Just done
BI1c: hp FULLY GREEN: run #349 (4d78cfb, id 42451187830) 25 PASS; run #350 (015db48, id 42451656366) 28 PASS 0 FAIL incl. HP11 shade (named systemui FrameLayout event), HP12 volume caught, HP13 lock cycle (patch rests 1 s after unlock, no recalibration, crop kept). Version bumped to 0.8.9 / code 34. Product code is final: gating on this commit

## Do this next
dispatch the 3 gate runs on main (same sha, no product change until they finish): run1 e1,b6,x,ac,ae,ps,uf,cs2,hp,rt; run2 base,legacy,x1,y2,b4,o,se,cs,nq,rb; run3 n,n6,v,pa,pt,rg,pf,last; read each tail ~44; then bash ship.sh, android.yml full_build=true on the branch, get_release_by_tag, tools/record-release.sh v0.8.9, give Tj the Release link

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  4d78cfb ckpt 677: BI1c: hp run #348 (f78001e, id 42450536427) = 21 PASS 2 FAIL: only HP7 (no dra
  f78001e ckpt 676: BI1c: hp run #347 (4a34b83) = 19 PASS 4 FAIL: phase C FIXED (rested after rest
  4a34b83 ckpt 675: BI1c: takeover; hp run #346 (c69041b) = 18 PASS 5 FAIL, phase C never rested a
  c69041b ckpt 674: BI1c: emulator section hp written (phases A-E: self-calibration, invisible res
  4b6180c ckpt 673: BI1b step 5: Launcher control rows (switch + status + Calibrate now + Forget) 
  6005aa2 ckpt 672: BI1b steps 2-4 (compiles, nothing tested on the emulator yet): HomeShot + Home
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
