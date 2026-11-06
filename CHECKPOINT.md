# CHECKPOINT 473 — read me first, then TASKS.md

**Written:** 2026-11-06T07:10:32Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `cb2a3e4` (this checkpoint is the commit after it)

## Just done
v0.7.6: patch device profile (tb8168 shift -90 px, +8 %), wallpaper geometry report line; 412 tests green; 2 reviews running

## Do this next
collect reviewers a7e7c5fb1cbb98059 (lock safety + overlay) and a716b835fb97dc18c (geometry/logic/tests) on /tmp/.../scratchpad/v076.diff; fix real findings; then bump version 0.7.6 code 23 (app/build.gradle.kts) LAST; bash tools/gate.sh c0e044d (3 runs: legacy,e1,y2,n6,lq,rb | base,x1,b4,n,v,ae | o,x,ac,rt,pf,last, ref = branch claude/new-session-7a9vpi); ship.sh; android.yml on the branch; record-release

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  8e6db5b ckpt 472: v0.7.6 WIP: number constants + nudges, card layout, slot-only soft patch; AL1 
  9040fe4 ckpt 471: v0.7.6 WIP: review fixes done (CrashGuard order, saver cap, RESET log, card pr
  013c849 ckpt 470: v0.7.5 RELEASED + RECORDED (run 24, from c0e044d on the branch); v0.7.6 WIP: A
  0cd8ac2 ckpt 469: v0.7.5 build dispatched: android.yml run 24 (id 42176728625) on c0e044d, branc
  c0e044d ckpt 468: intake redacts Recents card titles (v0.7.5 review privacy item); reviews clear
  753c633 ckpt 467: gated v0.7.5 (code 22) and pushed it: the handoff self-test, the full unit sui
  307f1a7 ckpt 466: Tj sent 3 side-by-side photos (reference vs ours); card size already matches, 
  5516218 ckpt 465: photo findings: reference number ~15% bigger + heavier; agents running
  7c92edd ckpt 464: Tj clarified photos: blue scribble = reference, others = ours (recorded)
  621baf2 ckpt 463: recovered dead-chat measurements + partial v0.7.5 review into TASKS
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
