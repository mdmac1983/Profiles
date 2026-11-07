# CHECKPOINT 624 — read me first, then TASKS.md

**Written:** 2026-11-07T20:30:31Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-fafa2908-h7bsez` · **builds on:** `9a801d5` (this checkpoint is the commit after it)

## Just done
audit written: research/RECENTS_AND_UNLOCK_AUDIT_2026-11-07.md (routes R1-R5, unlock time chain, decisions D1-D5, patch causes P1-P4)

## Do this next
code order: 1) RecentsSweep.java pure plan + JVM test; 2) clean_start switch (no Solitaire at prearm/unlock/return, cover ends at custom home); 3) sweep triggers (lock, unlock, Recents open, fg leave); 4) patch: ACTION_OUTSIDE hide, fastDecline fix, precreate at lock, LockLog diag; 5) tests+gate+ship v0.8.5

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  f8d1db4 ckpt 623: BE1-BE3 written into TASKS (Tj 20:17 UTC: patch flicker/screenshot flash, hidd
  bd37f17 ckpt 622: v0.8.4 SHIPPED: android.yml run 31 id 42400691090 on c6cb66b green, Release v0
  36b0efb ckpt 621: confirmation {base,x1,e1} #306 id 42399859690 = success: the #303 e1 failure w
  c6cb66b ckpt 620: HANDOFF 0g refreshed for v0.8.4 (contents, evidence pointers, test lessons, wh
  1231dd8 ckpt 619: core on the final code: #304 {n,o,rb} green; #303 {base,x1,e1,ac} 2 FAIL in e1
  a1c477e ckpt 618: v0.8.4 gated and pushed (code 29); #302 id 42397572681 (v alone) green after t
  43cfe53 ckpt 617: gated v0.8.4 (code 29) and pushed it: the handoff self-test, the full unit sui
  7f3b5d6 ckpt 616: final-head gate read on d530023: #300 (pt,last) green; #301 (ae,ps,pa,x1,v) 43
  253981c ckpt 615: final-head runs dispatched 17:22 UTC on d530023: #300 id 42396001116 (pt,last)
  d530023 ckpt 614: gate read: #297 and #298 green on a8f76b6, #299 (pt,last) 3 FAIL = the noisy o
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
