# CHECKPOINT 606 — read me first, then TASKS.md

**Written:** 2026-11-07T17:01:46Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `75f8170` (this checkpoint is the commit after it)

## Just done
emulator #296 read: the tap close is followed along the stock fade-out (close_max 0.038, was 0.20), BACK unchanged, Home key / swipe cannot be filmed on a locked emulator; phase F now closes by the close-system-dialogs broadcast and has two hard BC2 checks; BC1 edge covered (+0.13 level)

## Do this next
dispatch the gate: 3 parallel emulator.yml runs on ccr-823c5a56-yg8voe: {base,x1,e1,v,ac,ps} {n,o,ae,pa,rb} {pt,last} (about 16 min each); read each (list_workflow_jobs latest, get_job_logs tail ~70; pt tail ~125); then bump app/build.gradle.kts to 0.8.4 / code 29, bash ship.sh, android.yml full_build on main, get_release_by_tag v0.8.4, tools/record-release.sh v0.8.4, HANDOFF 0g, tell Tj (also ask how the pull-up-for-PIN close looks)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  daf4f15 ckpt 605: emulator #295 read (BC1 edge covered to 0.1 level; BC2: click + dialog-removal
  e079c40 ckpt 604: merged Tj's 16:13 message (a browser question, captured on main only) into the
  a502af8 ckpt 603: BC2/BC1 emulator proof coded: pt phase F (the menu closed by BACK / a tap in t
  1c33d22 ckpt 602: BC1b + BC2b step 1 coded: PatchRest.coverDisplayEdges (core to the display edg
  c564672 ckpt 601: Tj's new message (16:10 UTC, v0.8.3 installed, video of the sliver): BC1 the w
  d56dd0b ckpt 600: v0.8.3 is SHIPPED and the AN1 question is CLOSED: proving run #294 id 42350538
  ee295be ckpt 599: v0.8.3 SHIPPED: android.yml run 30 id 42350762180 on bf6a931 green (handoff ga
  4f584eb ckpt 598: android.yml full build RUNNING: run 30 id 42350762180 on main bf6a931 (v0.8.3 
  95ccfd5 ckpt 597: Tj said 'Build it now' (07:19 UTC): dispatched android.yml full_build=true on 
  7faba16 ckpt 596: run #293 id 42349158289 (base,x1,v,ac on 8309cc6) AN1 FAILED AGAIN (67 PASS 1 
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
