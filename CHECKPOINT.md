# CHECKPOINT 633 — read me first, then TASKS.md

**Written:** 2026-11-07T22:54:54Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-fafa2908-h7bsez` · **builds on:** `c4456a9` (this checkpoint is the commit after it)

## Just done
final gate re-run on c4456a9 (app code = 2999c35): #322 id 42422768769 {base,e1,n,n6,o}, #323 id 42422770687 {t,x,ac,se,lq}, #324 {rb,last}, #325 {pt}; G2 #319 was an emulator GL crash (infrastructure)

## Do this next
read #322 #323 #324 #325 when done (~23:15 UTC): FAIL lines only (use a read-only subagent for big logs); if all green: bash ship.sh 'note' then android.yml full_build=true on the branch, confirm the Release, tools/record-release.sh v0.8.5 'note'

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  8f46940 ckpt 632: final gate read: G3 green; G1 4 FAIL all in pt (0.75/0.80 scrim state + a late
  1028360 ckpt 631: FINAL GATE dispatched 22:00 UTC on 2999c35: #318 id 42418473647 {x1,y2,b4,pt,u
  2999c35 ckpt 630: version 0.8.5 / code 30 (the release candidate); gate groups chosen from tools
  deaa82c ckpt 629: recentsEmpty: deadline in every branch + card press cap (the #313/#314 hang), 
  aee9fd9 ckpt 628: emulator read #1: ae,ps + pt green; cs found the no-helper Clear-all hole (fix
  4e85854 ckpt 627: BE1-BE3 built; emulator runs #307 id 42412739934 {cs,rg,uf}, #308 id 424127423
  713f3b2 ckpt 626: BE1 patch (ACTION_OUTSIDE hide, fast-open fix, precreate at lock, diag), BE2/B
  da2f279 ckpt 625: BE2/BE3 core: RecentsSweep (+11 JVM tests), ReturnReset clean-start (no stand-
  81c5a62 ckpt 624: audit written: research/RECENTS_AND_UNLOCK_AUDIT_2026-11-07.md (routes R1-R5, 
  f8d1db4 ckpt 623: BE1-BE3 written into TASKS (Tj 20:17 UTC: patch flicker/screenshot flash, hidd
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
