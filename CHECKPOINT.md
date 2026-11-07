# CHECKPOINT 605 — read me first, then TASKS.md

**Written:** 2026-11-07T16:46:02Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `fa717d7` (this checkpoint is the commit after it)

## Just done
emulator #295 read (BC1 edge covered to 0.1 level; BC2: click + dialog-removal signs work on the emulator, but phase F ran with animations OFF and the swipe did nothing): service now also starts the fade-out on ACTION_CLOSE_SYSTEM_DIALOGS (not recentapps / globalactions); phase F re-asserts animations, closes by BACK / tap / HOME; notes in TASKS BC1b/BC2b

## Do this next
dispatch emulator pt again (about 11 min) and read closeback / closetap / closehome: animation scales must read 1.0 1.0 1.0; tap and home: close start logged, close_max with the curve followed (target <= 0.08), PTCLOSE series; if good add the hard checks (tap and home close_max <= 0.08, closeback <= 0.21) then run the core gate sections (tools/gate.sh), ship v0.8.4

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  e079c40 ckpt 604: merged Tj's 16:13 message (a browser question, captured on main only) into the
  a502af8 ckpt 603: BC2/BC1 emulator proof coded: pt phase F (the menu closed by BACK / a tap in t
  1c33d22 ckpt 602: BC1b + BC2b step 1 coded: PatchRest.coverDisplayEdges (core to the display edg
  c564672 ckpt 601: Tj's new message (16:10 UTC, v0.8.3 installed, video of the sliver): BC1 the w
  d56dd0b ckpt 600: v0.8.3 is SHIPPED and the AN1 question is CLOSED: proving run #294 id 42350538
  ee295be ckpt 599: v0.8.3 SHIPPED: android.yml run 30 id 42350762180 on bf6a931 green (handoff ga
  4f584eb ckpt 598: android.yml full build RUNNING: run 30 id 42350762180 on main bf6a931 (v0.8.3 
  95ccfd5 ckpt 597: Tj said 'Build it now' (07:19 UTC): dispatched android.yml full_build=true on 
  7faba16 ckpt 596: run #293 id 42349158289 (base,x1,v,ac on 8309cc6) AN1 FAILED AGAIN (67 PASS 1 
  8309cc6 ckpt 595: run #292 id 42347734404 (base,x1,v,ac,ps) on 94671f9: 78 PASS, 1 FAIL = AN1 'g
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
