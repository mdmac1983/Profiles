# CHECKPOINT 495 — read me first, then TASKS.md

**Written:** 2026-11-06T08:49:46Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `9e4e691` (this checkpoint is the commit after it)

## Just done
Tj's final polish ask (speed / no flicker / restart survival test, the invisible lock-screen patch, a settings audit + UI re-organisation, one polished release) written into TASKS.md as AQ1 / AM1 / AR1 / AS1 to start AFTER v0.7.7 (AN1 AO1b AP1) and AM1; emulator runs 201 202 and ao,rg still running

## Do this next
finish v0.7.7 first: read runs 201 (job 127796566239) and 202 (job 127796578926) and the ao,rg run (newest emulator.yml dispatch), then the full delta gate (bash tools/gate.sh 8fc7662: 3 runs), ship v0.7.7; THEN AM1 (git merge tmp-merge-test or git apply --3way research/pending/am1.patch, keep both sides of the 3 conflicts; run section ps), THEN AQ1 / AR1 / AS1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  e69d979 ckpt 494: AP1 CODED (446 unit tests green): RecentsFilter (new class; I first overwrote 
  a90b089 ckpt 493: AO1b CODED (builds, unit tests green): the AOSP legacy WallpaperPicker (androi
  ceb26c4 ckpt 492: gate reruns: e1 alone (run 198), base,x1,e1 on TAG v0.7.6 (run 199) and rb alo
  ea06d51 ckpt 491: Tj's newest message (battery tile only, Recents whitelist = drawer apps, wallp
  b7a531c ckpt 490: gate on 8fc7662: run 197 (b6,o,t,lq) GREEN; run 196 (base,x1,e1,ao,last) 40 PA
  ef34117 ckpt 489: v0.7.7 (AN1+AO1) in the emulator gate; AM1 merge resolved on local branch tmp-
  169b461 ckpt 488: emulator gate dispatched for AN1+AO1 on 8fc7662: run 195 (id 42186194687) n,x,
  8fc7662 ckpt 487: AN1 + AO1 coded (436 unit tests green): wallpaper picker = one explicit system
  75f75a4 ckpt 486: AN1 coded (420 unit tests green): battery-saver guard acts only at real launch
  13c7616 ckpt 485: v0.7.6 SHIPPED (Release v0.7.6, BUILDLOG recorded); AN1 findings written into 
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
