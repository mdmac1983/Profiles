# CHECKPOINT 627 — read me first, then TASKS.md

**Written:** 2026-11-07T20:48:47Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-fafa2908-h7bsez` · **builds on:** `713f3b2` (this checkpoint is the commit after it)

## Just done
BE1-BE3 built; emulator runs #307 id 42412739934 {cs,rg,uf}, #308 id 42412742359 {pt}, #309 id 42412744586 {ae,ps} on 713f3b2 dispatched 20:47 UTC

## Do this next
read the three runs (list_workflow_jobs resource_id=run id, get_job_logs tail 80 after they end ~21:00-21:05 UTC); fix FAILs; then core+opt groups from gate.sh c6cb66b (n6,x,ao,rt | base,x1,e1,n,v,pf | y2,b4,o,ac,se,lq,rb,last); bump version 0.8.5 code 30; ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md
     M app/src/main/java/com/tj/launcher/TjAccessibilityService.java

## Last ten checkpoints
```
  713f3b2 ckpt 626: BE1 patch (ACTION_OUTSIDE hide, fast-open fix, precreate at lock, diag), BE2/B
  da2f279 ckpt 625: BE2/BE3 core: RecentsSweep (+11 JVM tests), ReturnReset clean-start (no stand-
  81c5a62 ckpt 624: audit written: research/RECENTS_AND_UNLOCK_AUDIT_2026-11-07.md (routes R1-R5, 
  f8d1db4 ckpt 623: BE1-BE3 written into TASKS (Tj 20:17 UTC: patch flicker/screenshot flash, hidd
  bd37f17 ckpt 622: v0.8.4 SHIPPED: android.yml run 31 id 42400691090 on c6cb66b green, Release v0
  36b0efb ckpt 621: confirmation {base,x1,e1} #306 id 42399859690 = success: the #303 e1 failure w
  c6cb66b ckpt 620: HANDOFF 0g refreshed for v0.8.4 (contents, evidence pointers, test lessons, wh
  1231dd8 ckpt 619: core on the final code: #304 {n,o,rb} green; #303 {base,x1,e1,ac} 2 FAIL in e1
  a1c477e ckpt 618: v0.8.4 gated and pushed (code 29); #302 id 42397572681 (v alone) green after t
  43cfe53 ckpt 617: gated v0.8.4 (code 29) and pushed it: the handoff self-test, the full unit sui
```
