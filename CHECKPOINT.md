# CHECKPOINT 604 — read me first, then TASKS.md

**Written:** 2026-11-07T16:31:21Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `816f82e` (this checkpoint is the commit after it)

## Just done
merged Tj's 16:13 message (a browser question, captured on main only) into the branch, main mirrors again (93640d4); TASKS BC3 records it in his words with the answer; emulator pt dispatched at 16:30 UTC on a502af8 (BC1 edge probe + BC2 close films)

## Do this next
read emulator pt (dispatched ~16:30:10 UTC, due ~16:41: find it with list_workflow_runs emulator.yml branch ccr-823c5a56-yg8voe event workflow_dispatch perPage 1, then list_workflow_jobs latest, get_job_logs tail ~140): PB/BC1 (stock edge line vs the patch), PT/BC2 closeback / closetap / closeswipe (is there a K click event for the tap, was 'close start' logged, close_max), the swipe timeline; then tune and add checks; then gate + ship v0.8.4; Tj was told the browser answer and that the two launcher fixes are in test

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  a502af8 ckpt 603: BC2/BC1 emulator proof coded: pt phase F (the menu closed by BACK / a tap in t
  1c33d22 ckpt 602: BC1b + BC2b step 1 coded: PatchRest.coverDisplayEdges (core to the display edg
  c564672 ckpt 601: Tj's new message (16:10 UTC, v0.8.3 installed, video of the sliver): BC1 the w
  d56dd0b ckpt 600: v0.8.3 is SHIPPED and the AN1 question is CLOSED: proving run #294 id 42350538
  ee295be ckpt 599: v0.8.3 SHIPPED: android.yml run 30 id 42350762180 on bf6a931 green (handoff ga
  4f584eb ckpt 598: android.yml full build RUNNING: run 30 id 42350762180 on main bf6a931 (v0.8.3 
  95ccfd5 ckpt 597: Tj said 'Build it now' (07:19 UTC): dispatched android.yml full_build=true on 
  7faba16 ckpt 596: run #293 id 42349158289 (base,x1,v,ac on 8309cc6) AN1 FAILED AGAIN (67 PASS 1 
  8309cc6 ckpt 595: run #292 id 42347734404 (base,x1,v,ac,ps) on 94671f9: 78 PASS, 1 FAIL = AN1 'g
  661d9df ckpt 594: run #291 id 42347732366 (pt) on 94671f9 GREEN, 0 FAIL: PB reach 28 rows (v0.8.
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
