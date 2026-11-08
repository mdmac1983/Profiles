# CHECKPOINT 685 — read me first, then TASKS.md

**Written:** 2026-11-08T07:21:12Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2r9ji2` · **builds on:** `1cdc6df` (this checkpoint is the commit after it)

## Just done
v0.8.9: W5 (stock home redirect, section legacy) failed twice on main but base,legacy@v0.8.8 (#365) PASSED = candidate regression (BH1e: runReset now opens the shade when something was posted and pauses the stray-home guard 10 s); pt flaky on v0.8.8 too (BA1) but closetap close_max main 11.8/>15/20.3 % vs v0.8.8 3.3 % (1 sample); delta gate #366 hp #367 n,ac,rb,last #368 base,x1,e1,o in flight; W5 diagnostics added, base,legacy@main dispatched 07:21

## Do this next
read the base,legacy@main run (INFO 'W5 before' / 'W5 after' lines): if a RESET ran right before the stock start, BH1e is the cause (decide: test waits for the reset or the guard pause is shortened); then read #366-#368; pt: sample closetap a few times on v0.8.8 vs main before shipping

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  db1c63e ckpt 684: v0.8.9 DELTA GATE 07:14 on 97905e3 (product delta since 9c40e5c = the foreign-
  e5d3cb9 ckpt 683: v0.8.9 gate rerun map 06:58: #359 pt alone 27 PASS 1 FAIL (BC2 closetap) so #3
  f619254 ckpt 682: v0.8.9 GATE READ 06:40: #356 (b6,n,n6,v,pa,rg,cs2,hp,pf,last) 89 PASS 2 FAIL =
  3d26ef4 ckpt 681: v0.8.9 GATE dispatched 06:05 UTC on 9c40e5c (hp alone on it = #354 28 PASS): #
  9c40e5c ckpt 680: v0.8.9: first gate (#351-353 on e60f9ad) CANCELLED at 05:55 UTC on purpose: a 
  fd738df ckpt 679: v0.8.9 GATE dispatched 05:47 UTC on e60f9ad (product code final, version 0.8.9
  e60f9ad ckpt 678: BI1c: hp FULLY GREEN: run #349 (4d78cfb, id 42451187830) 25 PASS; run #350 (01
  4d78cfb ckpt 677: BI1c: hp run #348 (f78001e, id 42450536427) = 21 PASS 2 FAIL: only HP7 (no dra
  f78001e ckpt 676: BI1c: hp run #347 (4a34b83) = 19 PASS 4 FAIL: phase C FIXED (rested after rest
  4a34b83 ckpt 675: BI1c: takeover; hp run #346 (c69041b) = 18 PASS 5 FAIL, phase C never rested a
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
