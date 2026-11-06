# CHECKPOINT 490 — read me first, then TASKS.md

**Written:** 2026-11-06T08:29:04Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `ef34117` (this checkpoint is the commit after it)

## Just done
gate on 8fc7662: run 197 (b6,o,t,lq) GREEN; run 196 (base,x1,e1,ao,last) 40 PASS / 2 FAIL = E1 Delete x2 -> Quickstep (+ stock launcher on screen), ao ALL PASS (3 SET_WALLPAPER handlers on the emulator, one picker opened: com.android.wallpaper.livepicker; cover on a Settings row: spread 133 -> 0, tap swallowed, cover gone after Home); run 195 (n,x,ac,rb) 80 PASS / 1 FAIL = RB4 (Home chooser ResolverActivity after the restart); ac AN1 checks + AD8 line all PASS. Reruns to separate flake from regression: run 198 (id 42187809214) e1 on the branch ef34117; run 199 (id 42187813623) base,x1,e1 on TAG v0.7.6 (control); a third run rb on the branch (id unknown, newest emulator.yml dispatch)

## Do this next
read 198, 199 and the rb run (job logs tail 40). If E1/RB pass on the branch: flakes, go ship v0.7.7. If E1 fails on the branch but passes on the tag: regression from AN1/AO1: suspects in order: EventNoise.needsPowerMenuScan gate in TjAccessibilityService.onAccessibilityEvent, PickerGuard.onEvent (first event runs refreshPackage on the service thread), BatterySaverGuard/Mode.set gating. If both fail on the tag too: pre-existing flake, note it in TASKS and ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  ef34117 ckpt 489: v0.7.7 (AN1+AO1) in the emulator gate; AM1 merge resolved on local branch tmp-
  169b461 ckpt 488: emulator gate dispatched for AN1+AO1 on 8fc7662: run 195 (id 42186194687) n,x,
  8fc7662 ckpt 487: AN1 + AO1 coded (436 unit tests green): wallpaper picker = one explicit system
  75f75a4 ckpt 486: AN1 coded (420 unit tests green): battery-saver guard acts only at real launch
  13c7616 ckpt 485: v0.7.6 SHIPPED (Release v0.7.6, BUILDLOG recorded); AN1 findings written into 
  92a3016 ckpt 484: v0.7.6 RELEASE BUILD RUNNING: android.yml run 42183756211 (Build APK #25) disp
  f5f7235 ckpt 483: gated v0.7.6 (code 23) and pushed it: the handoff self-test, the full unit sui
  13b914b ckpt 482: HANDOFF READY: AM1 saved as research/pending/am1.patch; AN1 battery saver tile
  930e398 ckpt 481: AM1 coded in the local worktree /home/user/Launcher-am1 (421 tests); v0.7.6 de
  274b1f5 ckpt 480: v0.7.6 delta gate dispatched on 7d1837b: runs 193 (id 42182087395) legacy,y2,b
```
