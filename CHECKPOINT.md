# CHECKPOINT 468 — read me first, then TASKS.md

**Written:** 2026-11-06T06:47:54Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `c54f4eb` (this checkpoint is the commit after it)

## Just done
intake redacts Recents card titles (v0.7.5 review privacy item); reviews clear, building v0.7.5

## Do this next
dispatch android.yml ref claude/new-session-7a9vpi full_build=true (run id from actions_list in_progress); get_release_by_tag v0.7.5; bash tools/record-release.sh v0.7.5 <note> ONLY after green; ls-remote check; then v0.7.6 per TASKS (review should-fix list in TASKS: CrashGuard order, ReturnReset.java:160 log line, describeCards privacy, patchShow after SCREEN_OFF line 1020, cap counting + plugged-in, SAFE_VALUES, restDim nudge)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  753c633 ckpt 467: gated v0.7.5 (code 22) and pushed it: the handoff self-test, the full unit sui
  307f1a7 ckpt 466: Tj sent 3 side-by-side photos (reference vs ours); card size already matches, 
  5516218 ckpt 465: photo findings: reference number ~15% bigger + heavier; agents running
  7c92edd ckpt 464: Tj clarified photos: blue scribble = reference, others = ours (recorded)
  621baf2 ckpt 463: recovered dead-chat measurements + partial v0.7.5 review into TASKS
  5e56532 ckpt 462: takeover: AL1 lock number size/font recorded, AH3 findings recovered
  609bca1 ckpt 461: AG2 logging added to the v0.7.6 patch (low_power changes + guard actions in th
  1088e21 ckpt 460: AK1 coded (v0.7.6 patch saved, unit 389/0); v0.7.5 gate runs 186/187/188 all G
  9040b7f ckpt 459: AK1 lock screen quiet (no Wi-Fi/BT/notifications from any launcher): requested
  af7c41f ckpt 458: AG2 battery saver TILE in Quickstep does not work: recorded + analysed; need t
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
