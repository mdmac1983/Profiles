# CHECKPOINT 491 — read me first, then TASKS.md

**Written:** 2026-11-06T08:36:13Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `ab0621c` (this checkpoint is the commit after it)

## Just done
Tj's newest message (battery tile only, Recents whitelist = drawer apps, wallpaper picker must be the reference WITHOUT My photos, source may be fetched online) written into TASKS.md as AN1b / AP1 / AO1b; gate reruns 198 199 + rb still running

## Do this next
1) read reruns 198 (id 42187809214 e1 branch), 199 (id 42187813623 base,x1,e1 on tag v0.7.6), rb run (newest emulator.yml dispatch); 2) test whether the AOSP WallpaperPicker source is reachable (git ls-remote / add_repo aosp-mirror) and port it WITHOUT the My photos tile (AO1b); 3) read ReturnReset/AppOrigin/RecentsPolicy and design AP1 (drawer apps are the only allowed Recents); 4) ship v0.7.7 (AN1 + AO1 stage 1) once the reruns are understood

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  b7a531c ckpt 490: gate on 8fc7662: run 197 (b6,o,t,lq) GREEN; run 196 (base,x1,e1,ao,last) 40 PA
  ef34117 ckpt 489: v0.7.7 (AN1+AO1) in the emulator gate; AM1 merge resolved on local branch tmp-
  169b461 ckpt 488: emulator gate dispatched for AN1+AO1 on 8fc7662: run 195 (id 42186194687) n,x,
  8fc7662 ckpt 487: AN1 + AO1 coded (436 unit tests green): wallpaper picker = one explicit system
  75f75a4 ckpt 486: AN1 coded (420 unit tests green): battery-saver guard acts only at real launch
  13c7616 ckpt 485: v0.7.6 SHIPPED (Release v0.7.6, BUILDLOG recorded); AN1 findings written into 
  92a3016 ckpt 484: v0.7.6 RELEASE BUILD RUNNING: android.yml run 42183756211 (Build APK #25) disp
  f5f7235 ckpt 483: gated v0.7.6 (code 23) and pushed it: the handoff self-test, the full unit sui
  13b914b ckpt 482: HANDOFF READY: AM1 saved as research/pending/am1.patch; AN1 battery saver tile
  930e398 ckpt 481: AM1 coded in the local worktree /home/user/Launcher-am1 (421 tests); v0.7.6 de
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
