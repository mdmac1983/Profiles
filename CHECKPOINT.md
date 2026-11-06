# CHECKPOINT 470 — read me first, then TASKS.md

**Written:** 2026-11-06T06:52:57Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `c43c3c9` (this checkpoint is the commit after it)

## Just done
v0.7.5 RELEASED + RECORDED (run 24, from c0e044d on the branch); v0.7.6 WIP: AK1 applied + 4 review fixes

## Do this next
v0.7.6 TODO in order: (a) unit tests + lint green for the WIP (bash tools/gradle.sh testDebugUnitTest :app:lintDebug), (b) review items 5-10 listed in TASKS (patchShow after SCREEN_OFF ~line 1020, restDim nudge, SAFE_VALUES, scenario guards, AI1 window rule test, resting patch hides under other windows), (c) AH3 slot-only feathered patch + brightness from agent a1090ac7c4068a97a, (d) AL1 number constants + Control nudges from agent ad1dc2155919e11f1, (e) bump version 0.7.6 code 23 LAST, (f) bash tools/gate.sh c0e044d, ship.sh, dispatch android.yml on the branch, record

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  0cd8ac2 ckpt 469: v0.7.5 build dispatched: android.yml run 24 (id 42176728625) on c0e044d, branc
  c0e044d ckpt 468: intake redacts Recents card titles (v0.7.5 review privacy item); reviews clear
  753c633 ckpt 467: gated v0.7.5 (code 22) and pushed it: the handoff self-test, the full unit sui
  307f1a7 ckpt 466: Tj sent 3 side-by-side photos (reference vs ours); card size already matches, 
  5516218 ckpt 465: photo findings: reference number ~15% bigger + heavier; agents running
  7c92edd ckpt 464: Tj clarified photos: blue scribble = reference, others = ours (recorded)
  621baf2 ckpt 463: recovered dead-chat measurements + partial v0.7.5 review into TASKS
  5e56532 ckpt 462: takeover: AL1 lock number size/font recorded, AH3 findings recovered
  609bca1 ckpt 461: AG2 logging added to the v0.7.6 patch (low_power changes + guard actions in th
  1088e21 ckpt 460: AK1 coded (v0.7.6 patch saved, unit 389/0); v0.7.5 gate runs 186/187/188 all G
```

(13 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
