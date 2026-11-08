# CHECKPOINT 684 — read me first, then TASKS.md

**Written:** 2026-11-08T07:14:33Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2r9ji2` · **builds on:** `97905e3` (this checkpoint is the commit after it)

## Just done
v0.8.9 DELTA GATE 07:14 on 97905e3 (product delta since 9c40e5c = the foreign-window report diagnostic): #366 id 42457926021 hp; #367 id 42457928338 n,ac,rb,last; #368 id 42457930591 base,x1,e1,o. Evidence: pt flakes on v0.8.8 too (BA1 x2), hp HP4 = test timing (quiet wait: #362 45 PASS), #361 ps,uf,cs,nq,rb green; W5 baseline #365 base,legacy@v0.8.8 and #364 pt@main pending

## Do this next
read #366-#368 via a subagent (header, FAIL lines, hp INFO), #365 (W5 at v0.8.8: same FAIL = pre-existing) and #364; all green or explained: bash ship.sh note, android.yml full_build=true on claude/new-session-2r9ji2, get_release_by_tag v0.8.9, tools/record-release.sh v0.8.9, Release link to Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M research/TABLET_CHECKLIST_v0.8.9.md

## Last ten checkpoints
```
  e5d3cb9 ckpt 683: v0.8.9 gate rerun map 06:58: #359 pt alone 27 PASS 1 FAIL (BC2 closetap) so #3
  f619254 ckpt 682: v0.8.9 GATE READ 06:40: #356 (b6,n,n6,v,pa,rg,cs2,hp,pf,last) 89 PASS 2 FAIL =
  3d26ef4 ckpt 681: v0.8.9 GATE dispatched 06:05 UTC on 9c40e5c (hp alone on it = #354 28 PASS): #
  9c40e5c ckpt 680: v0.8.9: first gate (#351-353 on e60f9ad) CANCELLED at 05:55 UTC on purpose: a 
  fd738df ckpt 679: v0.8.9 GATE dispatched 05:47 UTC on e60f9ad (product code final, version 0.8.9
  e60f9ad ckpt 678: BI1c: hp FULLY GREEN: run #349 (4d78cfb, id 42451187830) 25 PASS; run #350 (01
  4d78cfb ckpt 677: BI1c: hp run #348 (f78001e, id 42450536427) = 21 PASS 2 FAIL: only HP7 (no dra
  f78001e ckpt 676: BI1c: hp run #347 (4a34b83) = 19 PASS 4 FAIL: phase C FIXED (rested after rest
  4a34b83 ckpt 675: BI1c: takeover; hp run #346 (c69041b) = 18 PASS 5 FAIL, phase C never rested a
  c69041b ckpt 674: BI1c: emulator section hp written (phases A-E: self-calibration, invisible res
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
