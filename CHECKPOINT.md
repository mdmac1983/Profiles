# CHECKPOINT 496 — read me first, then TASKS.md

**Written:** 2026-11-06T08:56:25Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `8b14b7f` (this checkpoint is the commit after it)

## Just done
gate on e69d979: ao PASSES in full (own picker opens: com.tj.launcher/com.android.wallpaperpicker.WallpaperPickerActivity, 'Set wallpaper' bar + master_wallpaper_list, no 'My photos', the Settings-row cover machinery works); runs 201 and 202 (the failing E1 / RB4 sequences) both SUCCESS = those were flakes; rg FAILED only because no Recents entries were counted even for the control (calendar=0 contacts=0): diagnostics added (raw dumpsys lines, monkey output) and rerunning rg; picker now in its own process (:wallpaper_picker) and only the main process feeds the crash breaker

## Do this next
1) rerun section rg (dispatched now), read it: the INFO lines show what dumpsys activity recents prints and whether monkey opened the apps; fix the counting or the guard; 2) full delta gate on the final commit (bash tools/gate.sh 8fc7662: 3 runs); 3) ship v0.7.7

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  8ba2f93 ckpt 495: Tj's final polish ask (speed / no flicker / restart survival test, the invisib
  e69d979 ckpt 494: AP1 CODED (446 unit tests green): RecentsFilter (new class; I first overwrote 
  a90b089 ckpt 493: AO1b CODED (builds, unit tests green): the AOSP legacy WallpaperPicker (androi
  ceb26c4 ckpt 492: gate reruns: e1 alone (run 198), base,x1,e1 on TAG v0.7.6 (run 199) and rb alo
  ea06d51 ckpt 491: Tj's newest message (battery tile only, Recents whitelist = drawer apps, wallp
  b7a531c ckpt 490: gate on 8fc7662: run 197 (b6,o,t,lq) GREEN; run 196 (base,x1,e1,ao,last) 40 PA
  ef34117 ckpt 489: v0.7.7 (AN1+AO1) in the emulator gate; AM1 merge resolved on local branch tmp-
  169b461 ckpt 488: emulator gate dispatched for AN1+AO1 on 8fc7662: run 195 (id 42186194687) n,x,
  8fc7662 ckpt 487: AN1 + AO1 coded (436 unit tests green): wallpaper picker = one explicit system
  75f75a4 ckpt 486: AN1 coded (420 unit tests green): battery-saver guard acts only at real launch
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
