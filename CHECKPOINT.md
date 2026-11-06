# CHECKPOINT 493 — read me first, then TASKS.md

**Written:** 2026-11-06T08:42:51Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `c093476` (this checkpoint is the commit after it)

## Just done
AO1b CODED (builds, unit tests green): the AOSP legacy WallpaperPicker (android-10.0.0_r9) is ported into wallpaperpicker/ WITHOUT the My photos tile (PickImageInfo lines removed), third-party tiles off, wallpapers read from the installed stock picker's resources, own activity declared in the manifest (not exported, excluded from Recents), the long-press Wallpapers entry starts it; emulator section ao extended (resumed activity, no 'My photos', set-wallpaper bar + strip); NOT yet run on the emulator. Runs 201 (base,x1,e1) and 202 (n,x,ac,rb) on ea06d51 (v0.7.7 stopgap, WITHOUT the port) still running

## Do this next
1) read runs 201 and 202 (list_workflow_jobs resource_id=run id then get_job_logs tail 45); 2) if clean: delta gate for the port on the new HEAD: sections base,ao,x (bash tools/gate.sh ea06d51 shows the rest); 3) ship v0.7.7 = AN1 + AO1b (+ the PickerGuard stopgap for the stock picker); 4) AP1 recents (read ReturnReset / AppOrigin / RecentsPolicy first); 5) AM1 merge (git merge tmp-merge-test)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  ceb26c4 ckpt 492: gate reruns: e1 alone (run 198), base,x1,e1 on TAG v0.7.6 (run 199) and rb alo
  ea06d51 ckpt 491: Tj's newest message (battery tile only, Recents whitelist = drawer apps, wallp
  b7a531c ckpt 490: gate on 8fc7662: run 197 (b6,o,t,lq) GREEN; run 196 (base,x1,e1,ao,last) 40 PA
  ef34117 ckpt 489: v0.7.7 (AN1+AO1) in the emulator gate; AM1 merge resolved on local branch tmp-
  169b461 ckpt 488: emulator gate dispatched for AN1+AO1 on 8fc7662: run 195 (id 42186194687) n,x,
  8fc7662 ckpt 487: AN1 + AO1 coded (436 unit tests green): wallpaper picker = one explicit system
  75f75a4 ckpt 486: AN1 coded (420 unit tests green): battery-saver guard acts only at real launch
  13c7616 ckpt 485: v0.7.6 SHIPPED (Release v0.7.6, BUILDLOG recorded); AN1 findings written into 
  92a3016 ckpt 484: v0.7.6 RELEASE BUILD RUNNING: android.yml run 42183756211 (Build APK #25) disp
  f5f7235 ckpt 483: gated v0.7.6 (code 23) and pushed it: the handoff self-test, the full unit sui
```

(6 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
