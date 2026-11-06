# CHECKPOINT 492 — read me first, then TASKS.md

**Written:** 2026-11-06T08:38:54Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `ea06d51` (this checkpoint is the commit after it)

## Just done
gate reruns: e1 alone (run 198), base,x1,e1 on TAG v0.7.6 (run 199) and rb alone (run 200) all SUCCESS, so the E1 / RB4 fails of runs 196 / 195 look like multi-section flakes; confirming with runs on the branch: base,x1,e1 and n,x,ac,rb (dispatched ~08:42Z). AOSP legacy WallpaperPicker android-10.0.0_r9 cloned into the scratchpad (aosp-wp/src, 3.4k lines, self-contained): the 'My photos' tile is two lines in WallpaperPickerActivity.init() (new PickImageInfo()); default wallpapers come from R.array.wallpapers of the installed picker app

## Do this next
1) read the two reruns (list runs newest first perPage 2, then list_workflow_jobs + get_job_logs tail 45); if both pass: ship v0.7.7 (ship.sh, android.yml on the branch, record-release) with the stopgap PickerGuard; 2) AO1b: create wallpaperpicker/ (src + res from the clone, English strings only), add the dirs to app/build.gradle.kts sourceSets, rename colliding resources, add 'import com.android.launcher3.R', remove the PickImageInfo lines and the third-party tiles, make getWallpaperArrayResourceId read the installed picker's wallpapers array, declare the activity (exported=false) in app/src/main/AndroidManifest.xml, point OptionsPopupView.startWallpaperPicker at it; 3) AP1 recents; 4) AM1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  ea06d51 ckpt 491: Tj's newest message (battery tile only, Recents whitelist = drawer apps, wallp
  b7a531c ckpt 490: gate on 8fc7662: run 197 (b6,o,t,lq) GREEN; run 196 (base,x1,e1,ao,last) 40 PA
  ef34117 ckpt 489: v0.7.7 (AN1+AO1) in the emulator gate; AM1 merge resolved on local branch tmp-
  169b461 ckpt 488: emulator gate dispatched for AN1+AO1 on 8fc7662: run 195 (id 42186194687) n,x,
  8fc7662 ckpt 487: AN1 + AO1 coded (436 unit tests green): wallpaper picker = one explicit system
  75f75a4 ckpt 486: AN1 coded (420 unit tests green): battery-saver guard acts only at real launch
  13c7616 ckpt 485: v0.7.6 SHIPPED (Release v0.7.6, BUILDLOG recorded); AN1 findings written into 
  92a3016 ckpt 484: v0.7.6 RELEASE BUILD RUNNING: android.yml run 42183756211 (Build APK #25) disp
  f5f7235 ckpt 483: gated v0.7.6 (code 23) and pushed it: the handoff self-test, the full unit sui
  13b914b ckpt 482: HANDOFF READY: AM1 saved as research/pending/am1.patch; AN1 battery saver tile
```
