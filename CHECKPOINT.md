# CHECKPOINT 687 — read me first, then TASKS.md

**Written:** 2026-11-08T07:43:38Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2r9ji2` · **builds on:** `97af27d` (this checkpoint is the commit after it)

## Just done
EXPERIMENT 07:50 (temporary, debug-only): TjHooks.exp bit mask (Settings.Global tj_exp, written by the scenario from an expN token in the sections list at the start of pt) to find why BC2 closetap reads 6-20 % on main vs 3-6 % on v0.8.8 (rank test p~0.02). REMOVE the exp code before shipping (or keep only if harmless) and re-gate hp + core

## Do this next
add the exp hooks (TjHooks.exp bits 1 no draw listener, 2 onWindowFocus off, 4 onWallpaperOffset off, 8 homeDrew returns, 16 no debug log), the scenario line, compile, push, then dispatch pt with exp1 and exp15 twice each plus one more v0.8.8 baseline; W5 was a flake (passes on main #370 base,legacy 45 PASS with a reset before it): make the test wait 12 s before the stray start

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  b3b55ce ckpt 686: v0.8.9 NOT SHIPPED: 8 CI runs in flight 07:13-07:22: delta gate #366 hp, #367 
  5a7e019 ckpt 685: v0.8.9: W5 (stock home redirect, section legacy) failed twice on main but base
  db1c63e ckpt 684: v0.8.9 DELTA GATE 07:14 on 97905e3 (product delta since 9c40e5c = the foreign-
  e5d3cb9 ckpt 683: v0.8.9 gate rerun map 06:58: #359 pt alone 27 PASS 1 FAIL (BC2 closetap) so #3
  f619254 ckpt 682: v0.8.9 GATE READ 06:40: #356 (b6,n,n6,v,pa,rg,cs2,hp,pf,last) 89 PASS 2 FAIL =
  3d26ef4 ckpt 681: v0.8.9 GATE dispatched 06:05 UTC on 9c40e5c (hp alone on it = #354 28 PASS): #
  9c40e5c ckpt 680: v0.8.9: first gate (#351-353 on e60f9ad) CANCELLED at 05:55 UTC on purpose: a 
  fd738df ckpt 679: v0.8.9 GATE dispatched 05:47 UTC on e60f9ad (product code final, version 0.8.9
  e60f9ad ckpt 678: BI1c: hp FULLY GREEN: run #349 (4d78cfb, id 42451187830) 25 PASS; run #350 (01
  4d78cfb ckpt 677: BI1c: hp run #348 (f78001e, id 42450536427) = 21 PASS 2 FAIL: only HP7 (no dra
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
