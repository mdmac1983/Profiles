# CHECKPOINT 628 — read me first, then TASKS.md

**Written:** 2026-11-07T21:25:41Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-fafa2908-h7bsez` · **builds on:** `59dab0b` (this checkpoint is the commit after it)

## Just done
emulator read #1: ae,ps + pt green; cs found the no-helper Clear-all hole (fixed: recentsEmpty closes every card); cs split into cs+cs2; re-runs #310 id 42415748756 {cs}, #311 id 42415750616 {cs2}, #312 id 42415753161 {core} on 59dab0b

## Do this next
read #310/#311/#312 (list_workflow_jobs + get_job_logs tail 60 after ~21:45 UTC); fix; then opt groups from gate.sh (n6,x,ao,rt | v,pf,y2,b4,o,se,lq); bump 0.8.5/code 30; code-review; ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  4e85854 ckpt 627: BE1-BE3 built; emulator runs #307 id 42412739934 {cs,rg,uf}, #308 id 424127423
  713f3b2 ckpt 626: BE1 patch (ACTION_OUTSIDE hide, fast-open fix, precreate at lock, diag), BE2/B
  da2f279 ckpt 625: BE2/BE3 core: RecentsSweep (+11 JVM tests), ReturnReset clean-start (no stand-
  81c5a62 ckpt 624: audit written: research/RECENTS_AND_UNLOCK_AUDIT_2026-11-07.md (routes R1-R5, 
  f8d1db4 ckpt 623: BE1-BE3 written into TASKS (Tj 20:17 UTC: patch flicker/screenshot flash, hidd
  bd37f17 ckpt 622: v0.8.4 SHIPPED: android.yml run 31 id 42400691090 on c6cb66b green, Release v0
  36b0efb ckpt 621: confirmation {base,x1,e1} #306 id 42399859690 = success: the #303 e1 failure w
  c6cb66b ckpt 620: HANDOFF 0g refreshed for v0.8.4 (contents, evidence pointers, test lessons, wh
  1231dd8 ckpt 619: core on the final code: #304 {n,o,rb} green; #303 {base,x1,e1,ac} 2 FAIL in e1
  a1c477e ckpt 618: v0.8.4 gated and pushed (code 29); #302 id 42397572681 (v alone) green after t
```

(6 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
