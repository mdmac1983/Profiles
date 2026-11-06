# CHECKPOINT 494 — read me first, then TASKS.md

**Written:** 2026-11-06T08:47:37Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `f967601` (this checkpoint is the commit after it)

## Just done
AP1 CODED (446 unit tests green): RecentsFilter (new class; I first overwrote the old RecentsGuard by mistake and restored it from git d72774f: HiddenAppsActivity/TjHooks/ControlActivity still use its excludeOurTasks/describeAsTarget) closes the Recents cards of apps that are not in the custom drawer whenever Recents opens in custom mode (hidden apps + Quickstep-opened non-drawer apps, matched by app label only, drawer labels never), Control switch recents_drawer_only default ON, scenario section rg + gate-map row; not yet run on the emulator

## Do this next
1) read runs 201/202 (timer b6y04kcym, jobs 127796566239 and 127796578926): they test ea06d51 = AN1+AO1 stopgap WITHOUT the port; 2) then ONE delta gate on the new HEAD: sections ao (own picker), rg (recents), base (manifest/resources), x (report line) + whatever bash tools/gate.sh ea06d51 lists; 3) ship v0.7.7 = AN1 + AO1b (own picker) + AP1 + PickerGuard stopgap; 4) AM1 after

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  a90b089 ckpt 493: AO1b CODED (builds, unit tests green): the AOSP legacy WallpaperPicker (androi
  ceb26c4 ckpt 492: gate reruns: e1 alone (run 198), base,x1,e1 on TAG v0.7.6 (run 199) and rb alo
  ea06d51 ckpt 491: Tj's newest message (battery tile only, Recents whitelist = drawer apps, wallp
  b7a531c ckpt 490: gate on 8fc7662: run 197 (b6,o,t,lq) GREEN; run 196 (base,x1,e1,ao,last) 40 PA
  ef34117 ckpt 489: v0.7.7 (AN1+AO1) in the emulator gate; AM1 merge resolved on local branch tmp-
  169b461 ckpt 488: emulator gate dispatched for AN1+AO1 on 8fc7662: run 195 (id 42186194687) n,x,
  8fc7662 ckpt 487: AN1 + AO1 coded (436 unit tests green): wallpaper picker = one explicit system
  75f75a4 ckpt 486: AN1 coded (420 unit tests green): battery-saver guard acts only at real launch
  13c7616 ckpt 485: v0.7.6 SHIPPED (Release v0.7.6, BUILDLOG recorded); AN1 findings written into 
  92a3016 ckpt 484: v0.7.6 RELEASE BUILD RUNNING: android.yml run 42183756211 (Build APK #25) disp
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
