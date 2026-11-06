# CHECKPOINT 489 — read me first, then TASKS.md

**Written:** 2026-11-06T08:17:03Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `c4e0f20` (this checkpoint is the commit after it)

## Just done
v0.7.7 (AN1+AO1) in the emulator gate; AM1 merge resolved on local branch tmp-merge-test (875c769, 444 tests green); TASKS/HANDOFF refreshed

## Do this next
1) READ runs 195 (id 42186194687: n,x,ac,rb), 196 (id 42186197950: base,x1,e1,ao,last), 197 (id 42186201233: b6,o,t,lq), all on 8fc7662: actions_list list_workflow_jobs (resource_id=run id, workflow_jobs_filter latest, perPage 1) then get_job_logs job_id tail_lines 45. Expected: only reset2 (known RS1) may fail. New checks to watch: ac (AN1 x5 + strict ones), ao (AO1 x5), x (AD8 line regex), b6 (pencil cover after the one-node refresh change), x1/n/e1/o/last/base (power menu scan gate). 2) fix real fails, rerun only the failing run; 3) bash ship.sh <v0.7.7 note: battery saver tile no longer fought by the guard + lighter helper in the shade; Wallpapers opens one system picker and hides My photos>; android.yml full_build ON THE BRANCH; get_release_by_tag v0.7.7; bash tools/record-release.sh v0.7.7 <note>; link to Tj. 4) AM1: in /home/user/Launcher run git merge tmp-merge-test (or git apply --3way research/pending/am1.patch and keep BOTH sides of the 3 conflicts), emulator.yml sections ps first, then bash tools/gate.sh <v0.7.7 commit>, ship v0.7.8. 5) ask Tj after v0.7.7: tap the battery tile a few times (Quickstep and custom) and send a tablet report; open the wallpaper picker once, send a screenshot (kept out of the repo) and a report

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  169b461 ckpt 488: emulator gate dispatched for AN1+AO1 on 8fc7662: run 195 (id 42186194687) n,x,
  8fc7662 ckpt 487: AN1 + AO1 coded (436 unit tests green): wallpaper picker = one explicit system
  75f75a4 ckpt 486: AN1 coded (420 unit tests green): battery-saver guard acts only at real launch
  13c7616 ckpt 485: v0.7.6 SHIPPED (Release v0.7.6, BUILDLOG recorded); AN1 findings written into 
  92a3016 ckpt 484: v0.7.6 RELEASE BUILD RUNNING: android.yml run 42183756211 (Build APK #25) disp
  f5f7235 ckpt 483: gated v0.7.6 (code 23) and pushed it: the handoff self-test, the full unit sui
  13b914b ckpt 482: HANDOFF READY: AM1 saved as research/pending/am1.patch; AN1 battery saver tile
  930e398 ckpt 481: AM1 coded in the local worktree /home/user/Launcher-am1 (421 tests); v0.7.6 de
  274b1f5 ckpt 480: v0.7.6 delta gate dispatched on 7d1837b: runs 193 (id 42182087395) legacy,y2,b
  7d1837b ckpt 479: v0.7.6: both reviews done (no blockers), fixes applied, home patch back to v0.
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
