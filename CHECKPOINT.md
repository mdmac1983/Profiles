# CHECKPOINT 467 — read me first, then TASKS.md

**Written:** 2026-11-06T06:46:01Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `fc336b7` (this checkpoint is the commit after it)

## Just done
gated v0.7.5 (code 22) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.7.5 "v0.7.5: long press on the home screen shows Wallpapers only (AE7); battery saver follows the launcher, on and staying on in Quickstep, off in custom (AG1); an app opened in Quickstep is found from Android's usage log and closed when you return to custom (AI1); the home-screen power-menu patch dims like the stock menu, with a Menu dimming nudge (AH1); optional resting lock-screen patch switch, off by default (AE3)"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  307f1a7 ckpt 466: Tj sent 3 side-by-side photos (reference vs ours); card size already matches, 
  5516218 ckpt 465: photo findings: reference number ~15% bigger + heavier; agents running
  7c92edd ckpt 464: Tj clarified photos: blue scribble = reference, others = ours (recorded)
  621baf2 ckpt 463: recovered dead-chat measurements + partial v0.7.5 review into TASKS
  5e56532 ckpt 462: takeover: AL1 lock number size/font recorded, AH3 findings recovered
  609bca1 ckpt 461: AG2 logging added to the v0.7.6 patch (low_power changes + guard actions in th
  1088e21 ckpt 460: AK1 coded (v0.7.6 patch saved, unit 389/0); v0.7.5 gate runs 186/187/188 all G
  9040b7f ckpt 459: AK1 lock screen quiet (no Wi-Fi/BT/notifications from any launcher): requested
  af7c41f ckpt 458: AG2 battery saver TILE in Quickstep does not work: recorded + analysed; need t
  b86027c ckpt 457: v0.7.5 gate dispatched on ea74960: runs 186 (y2,b4,n6,o,pf,rb) 187 (t,v,ae,rt,
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
