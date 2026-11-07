# CHECKPOINT 632 — read me first, then TASKS.md

**Written:** 2026-11-07T22:26:09Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-fafa2908-h7bsez` · **builds on:** `cde3136` (this checkpoint is the commit after it)

## Just done
final gate read: G3 green; G1 4 FAIL all in pt (0.75/0.80 scrim state + a late click event), pt re-run alone dispatched 22:27; G2 #319 id 42418475793 pending

## Do this next
read the pt rerun (get_job_logs tail 60: FAIL lines only) and #319 (tail 100); if both green: bash ship.sh 'note', android.yml full_build=true on the branch, confirm Release, tools/record-release.sh v0.8.5; if pt fails again alone: compare with #308 (28 PASS on 713f3b2) and look at what changed in the patch path since

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  1028360 ckpt 631: FINAL GATE dispatched 22:00 UTC on 2999c35: #318 id 42418473647 {x1,y2,b4,pt,u
  2999c35 ckpt 630: version 0.8.5 / code 30 (the release candidate); gate groups chosen from tools
  deaa82c ckpt 629: recentsEmpty: deadline in every branch + card press cap (the #313/#314 hang), 
  aee9fd9 ckpt 628: emulator read #1: ae,ps + pt green; cs found the no-helper Clear-all hole (fix
  4e85854 ckpt 627: BE1-BE3 built; emulator runs #307 id 42412739934 {cs,rg,uf}, #308 id 424127423
  713f3b2 ckpt 626: BE1 patch (ACTION_OUTSIDE hide, fast-open fix, precreate at lock, diag), BE2/B
  da2f279 ckpt 625: BE2/BE3 core: RecentsSweep (+11 JVM tests), ReturnReset clean-start (no stand-
  81c5a62 ckpt 624: audit written: research/RECENTS_AND_UNLOCK_AUDIT_2026-11-07.md (routes R1-R5, 
  f8d1db4 ckpt 623: BE1-BE3 written into TASKS (Tj 20:17 UTC: patch flicker/screenshot flash, hidd
  bd37f17 ckpt 622: v0.8.4 SHIPPED: android.yml run 31 id 42400691090 on c6cb66b green, Release v0
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
