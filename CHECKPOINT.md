# CHECKPOINT 500 — read me first, then TASKS.md

**Written:** 2026-11-06T09:16:11Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `d3b49fb` (this checkpoint is the commit after it)

## Just done
v0.7.7 SHIPPED and recorded; AM1 merged into the working branch (d3b49fb); emulator runs ps and ae,v,x1,last dispatched 09:19Z

## Do this next
1) read the two runs (list newest 2 emulator.yml runs; list_workflow_jobs then get_job_logs tail 45): ps has NEVER run (service screenshot via GLOBAL_ACTION_TAKE_SCREENSHOT, MediaStore observer, tap-closes-menu, patch_cal_until edit): fix what fails; 2) AM1 gate: bash tools/gate.sh 3e7fbb5 (legacy because older writers carry power_card true, ae, v, ps, x1, base, last ...); 3) meanwhile AR1: read ControlActivity (486 lines, sections listed in TASKS AR1) and regroup it; AQ1: section uf (unlock flow timing via logcat -b events am_resume_activity), then ship v0.8.0 = AM1 + AQ1 + AR1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  97f6123 ckpt 499: v0.7.7 (code 24) gated (all gate runs 204-207 green on 75748c5: rg, o/x/lq, n/
  4f54346 ckpt 498: gated v0.7.7 (code 24) and pushed it: the handoff self-test, the full unit sui
  8e4bc91 ckpt 497: v0.7.7 FULL DELTA GATE dispatched at 09:00Z on 75748c5: rg alone (earlier, dia
  75748c5 ckpt 496: gate on e69d979: ao PASSES in full (own picker opens: com.tj.launcher/com.andr
  8ba2f93 ckpt 495: Tj's final polish ask (speed / no flicker / restart survival test, the invisib
  e69d979 ckpt 494: AP1 CODED (446 unit tests green): RecentsFilter (new class; I first overwrote 
  a90b089 ckpt 493: AO1b CODED (builds, unit tests green): the AOSP legacy WallpaperPicker (androi
  ceb26c4 ckpt 492: gate reruns: e1 alone (run 198), base,x1,e1 on TAG v0.7.6 (run 199) and rb alo
  ea06d51 ckpt 491: Tj's newest message (battery tile only, Recents whitelist = drawer apps, wallp
  b7a531c ckpt 490: gate on 8fc7662: run 197 (b6,o,t,lq) GREEN; run 196 (base,x1,e1,ao,last) 40 PA
```
