# CHECKPOINT 486 — read me first, then TASKS.md

**Written:** 2026-11-06T08:05:54Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `f9b2fa4` (this checkpoint is the commit after it)

## Just done
AN1 coded (420 unit tests green): battery-saver guard acts only at real launcher changes + boot (tile is the boss; 'Strict' switch = old behaviour), shade content changes start no power-menu scan, pencil follows by one-node refresh; scenario ac updated (strict + new AN1 checks); NOT yet run on the emulator

## Do this next
1) AO1 (wallpaper picker: stock launcher's wallpaper_picker_package via its resources, explicit component, never a chooser; hide 'My photos' with the helper by node text + dump of the picker for the report); 2) then ONE emulator gate for AN1+AO1: bash tools/gate.sh f5f7235 prints 3 parallel runs (t,x,ac,last | e1,b6,o,rb | base,x1,n) + the new AO1 section; 3) ship v0.7.7; 4) AM1 merge after (research/pending/am1.patch or git merge am1)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  13c7616 ckpt 485: v0.7.6 SHIPPED (Release v0.7.6, BUILDLOG recorded); AN1 findings written into 
  92a3016 ckpt 484: v0.7.6 RELEASE BUILD RUNNING: android.yml run 42183756211 (Build APK #25) disp
  f5f7235 ckpt 483: gated v0.7.6 (code 23) and pushed it: the handoff self-test, the full unit sui
  13b914b ckpt 482: HANDOFF READY: AM1 saved as research/pending/am1.patch; AN1 battery saver tile
  930e398 ckpt 481: AM1 coded in the local worktree /home/user/Launcher-am1 (421 tests); v0.7.6 de
  274b1f5 ckpt 480: v0.7.6 delta gate dispatched on 7d1837b: runs 193 (id 42182087395) legacy,y2,b
  7d1837b ckpt 479: v0.7.6: both reviews done (no blockers), fixes applied, home patch back to v0.
  693b097 ckpt 478: v0.7.6 gate read: lq/ae/v/base/o/ac/rt/pf/last green; W3b (home patch) + 3 art
  ce35818 ckpt 477: Tj: stock menu stays stock, patch always in place; photo shows the model patch
  de3d688 ckpt 476: v0.7.6 gate dispatched on 43d6cb4: emulator.yml runs 189 (id 42179486444) lega
```

(13 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
