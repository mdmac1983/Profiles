# CHECKPOINT 497 — read me first, then TASKS.md

**Written:** 2026-11-06T08:57:44Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `75748c5` (this checkpoint is the commit after it)

## Just done
v0.7.7 FULL DELTA GATE dispatched at 09:00Z on 75748c5: rg alone (earlier, diagnostics), then three runs o,x,lq | n,ac,rb,last | base,x1,e1,ao (newest four emulator.yml dispatches); AM1 scratch branch tmp-merge-test f8d85bd = working branch 75748c5 + AM1, builds, 454 tests green (local only, worktree /home/user/Launcher-merge-test)

## Do this next
read the four runs (actions_list list_workflow_runs emulator.yml event workflow_dispatch branch claude/new-session-7a9vpi perPage 4, then list_workflow_jobs + get_job_logs tail 40 for each non-green one); expected only reset2 not in these sections; rg may need a fix (see its INFO lines); if all green: bash ship.sh <v0.7.7 note>, android.yml full_build on the branch, record-release, tell Tj; THEN merge tmp-merge-test into the branch (git merge tmp-merge-test), run section ps, AM1 gate, v0.7.8

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  75748c5 ckpt 496: gate on e69d979: ao PASSES in full (own picker opens: com.tj.launcher/com.andr
  8ba2f93 ckpt 495: Tj's final polish ask (speed / no flicker / restart survival test, the invisib
  e69d979 ckpt 494: AP1 CODED (446 unit tests green): RecentsFilter (new class; I first overwrote 
  a90b089 ckpt 493: AO1b CODED (builds, unit tests green): the AOSP legacy WallpaperPicker (androi
  ceb26c4 ckpt 492: gate reruns: e1 alone (run 198), base,x1,e1 on TAG v0.7.6 (run 199) and rb alo
  ea06d51 ckpt 491: Tj's newest message (battery tile only, Recents whitelist = drawer apps, wallp
  b7a531c ckpt 490: gate on 8fc7662: run 197 (b6,o,t,lq) GREEN; run 196 (base,x1,e1,ao,last) 40 PA
  ef34117 ckpt 489: v0.7.7 (AN1+AO1) in the emulator gate; AM1 merge resolved on local branch tmp-
  169b461 ckpt 488: emulator gate dispatched for AN1+AO1 on 8fc7662: run 195 (id 42186194687) n,x,
  8fc7662 ckpt 487: AN1 + AO1 coded (436 unit tests green): wallpaper picker = one explicit system
```
