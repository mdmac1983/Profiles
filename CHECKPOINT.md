# CHECKPOINT 626 — read me first, then TASKS.md

**Written:** 2026-11-07T20:47:04Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-fafa2908-h7bsez` · **builds on:** `41bb711` (this checkpoint is the commit after it)

## Just done
BE1 patch (ACTION_OUTSIDE hide, fast-open fix, precreate at lock, diag), BE2/BE3 sweep + clean start + cs scenario section + gate-map; tier0 green before the last 3 review fixes

## Do this next
run: gradle tests again, dispatch emulator groups from tools/gate.sh c6cb66b (cs, pt, ae, rg, uf + core), read results, fix, bump version 0.8.5/code 30, ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  da2f279 ckpt 625: BE2/BE3 core: RecentsSweep (+11 JVM tests), ReturnReset clean-start (no stand-
  81c5a62 ckpt 624: audit written: research/RECENTS_AND_UNLOCK_AUDIT_2026-11-07.md (routes R1-R5, 
  f8d1db4 ckpt 623: BE1-BE3 written into TASKS (Tj 20:17 UTC: patch flicker/screenshot flash, hidd
  bd37f17 ckpt 622: v0.8.4 SHIPPED: android.yml run 31 id 42400691090 on c6cb66b green, Release v0
  36b0efb ckpt 621: confirmation {base,x1,e1} #306 id 42399859690 = success: the #303 e1 failure w
  c6cb66b ckpt 620: HANDOFF 0g refreshed for v0.8.4 (contents, evidence pointers, test lessons, wh
  1231dd8 ckpt 619: core on the final code: #304 {n,o,rb} green; #303 {base,x1,e1,ac} 2 FAIL in e1
  a1c477e ckpt 618: v0.8.4 gated and pushed (code 29); #302 id 42397572681 (v alone) green after t
  43cfe53 ckpt 617: gated v0.8.4 (code 29) and pushed it: the handoff self-test, the full unit sui
  7f3b5d6 ckpt 616: final-head gate read on d530023: #300 (pt,last) green; #301 (ae,ps,pa,x1,v) 43
```

(10 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
