# CHECKPOINT 516 — read me first, then TASKS.md

**Written:** 2026-11-06T19:03:40Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-8o8uj9` · **builds on:** `3e9daae` (this checkpoint is the commit after it)

## Just done
gated v0.8.0 (code 25) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.8.0 "v0.8.0: the Screenshot button in the lock-screen power menu is hidden by an invisible patch cut from one real screenshot of your lock screen (Launcher control > Power menu (side key) > Calibrate the patch; the stock menu is not changed and nothing flashes); Launcher control is regrouped into 10 collapsible groups with a fixed Restore everything footer; unlock and restart behaviour measured and checked on the emulator (a plain PIN resumes only the custom launcher, a tablet restart keeps mode, tiles and status bar); the patch never swallows a touch while the menu is closed and no longer writes to disk when the menu opens or closes"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  02e0419 ckpt 515: review fixes in product code (patchShow: no fsync, never touchable when menu c
  766791a ckpt 514: gate read: G3 run 229 49/0; G2 run 228 79/1 (AA1 in im = known flake); G1 run 
  e9ac0e4 ckpt 513: final gate in flight on 0888e76: G1 run 227 id 42269849507 (base,legacy,e1,y2,
  0888e76 ckpt 512: takeover done: ps control fixed and PROVEN (run 226 15/0), rg alone green (run
  130c89a ckpt 511: ps red-crop control FIXED (test-only, tools/emulator_scenario.sh): bare rebind
  9ff5695 ckpt 510: TAKEOVER by branch claude/new-session-8o8uj9 (ff to 7a9vpi tip 024ede6; old ch
  024ede6 ckpt 509: runs 219 (e1,y2,b4,n,o,x,ad,lq) GREEN; run 222 ps: resting decision rest=true 
  0ac789f ckpt 508: ps run 218: after a helper restart + lock the patch window EXISTS (3 dumpsys l
  f5e5278 ckpt 507: ps run 217: the helper log is empty because the calibration happened BEFORE th
  14ad080 ckpt 506: ps run 216: positive control still fails (red crop NOT visible at rest) and th
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
