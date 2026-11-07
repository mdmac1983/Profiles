# CHECKPOINT 607 — read me first, then TASKS.md

**Written:** 2026-11-07T17:02:18Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `5e32b9d` (this checkpoint is the commit after it)

## Just done
gate dispatched 17:02 UTC on a8f76b6 (the v0.8.4 candidate: BC1 edge fix + BC2 fade-out following): #297 id 42394250639 (probably base,x1,e1,v,ac,ps), #298 id 42394253079 (n,o,ae,pa,rb), #299 id 42394255486 (pt,last): each prints 'INFO sections:' so the first INFO line says which is which

## Do this next
read the three runs at ~17:19 UTC (list_workflow_jobs latest on each id, then get_job_logs: tail 70 for the two core runs, 125 for the pt run); success = 0 FAIL (the step exit code = FAILS); a FAIL: diagnose first (AN1 is the known timing-race check, now epoch-filtered); then bump 0.8.4 / code 29 in app/build.gradle.kts, bash ship.sh, android.yml full_build on main, get_release_by_tag, record-release, HANDOFF 0g, tell Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  a8f76b6 ckpt 606: emulator #296 read: the tap close is followed along the stock fade-out (close_
  daf4f15 ckpt 605: emulator #295 read (BC1 edge covered to 0.1 level; BC2: click + dialog-removal
  e079c40 ckpt 604: merged Tj's 16:13 message (a browser question, captured on main only) into the
  a502af8 ckpt 603: BC2/BC1 emulator proof coded: pt phase F (the menu closed by BACK / a tap in t
  1c33d22 ckpt 602: BC1b + BC2b step 1 coded: PatchRest.coverDisplayEdges (core to the display edg
  c564672 ckpt 601: Tj's new message (16:10 UTC, v0.8.3 installed, video of the sliver): BC1 the w
  d56dd0b ckpt 600: v0.8.3 is SHIPPED and the AN1 question is CLOSED: proving run #294 id 42350538
  ee295be ckpt 599: v0.8.3 SHIPPED: android.yml run 30 id 42350762180 on bf6a931 green (handoff ga
  4f584eb ckpt 598: android.yml full build RUNNING: run 30 id 42350762180 on main bf6a931 (v0.8.3 
  95ccfd5 ckpt 597: Tj said 'Build it now' (07:19 UTC): dispatched android.yml full_build=true on 
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
