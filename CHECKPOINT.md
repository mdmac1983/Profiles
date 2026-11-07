# CHECKPOINT 617 — read me first, then TASKS.md

**Written:** 2026-11-07T17:42:59Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `35f7f90` (this checkpoint is the commit after it)

## Just done
gated v0.8.4 (code 29) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.8.4 "v0.8.4: the wallpaper patch now covers the screen's right edge (no gray sliver beside the hidden Screenshot slot); closing the power menu by a tap fades the patch out with the stock menu (no flicker); the patch disappears at once when the PIN pad comes up, with or without the power menu open"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M OUTBOX.md

## Last ten checkpoints
```
  7f3b5d6 ckpt 616: final-head gate read on d530023: #300 (pt,last) green; #301 (ae,ps,pa,x1,v) 43
  253981c ckpt 615: final-head runs dispatched 17:22 UTC on d530023: #300 id 42396001116 (pt,last)
  d530023 ckpt 614: gate read: #297 and #298 green on a8f76b6, #299 (pt,last) 3 FAIL = the noisy o
  986ddfa ckpt 613: Tj corrected BC5 (17:17 UTC): the PIN-pad hide must work with NO power menu op
  4a16127 ckpt 612: BD1/BD2 leads written to TASKS (ReturnReset black cover, TaskOrigin/RMTASKS by
  6bf1bcb ckpt 611: Tj's new message (17:12 UTC): BD1 hidden apps (Telegram, Claude) were in the R
  9c8439b ckpt 610: BC5 coded: TjAccessibilityService pinPadCameUp (patch alpha 0 at once + untouc
  35fa162 ckpt 609: Tj's answer (17:15 UTC): for the pull up for the PIN the patch must disappear 
  6ec98fc ckpt 608: Tj's new message (17:05 UTC): 'Also the lock screen still takes screenshots so
  b8f4fb9 ckpt 607: gate dispatched 17:02 UTC on a8f76b6 (the v0.8.4 candidate: BC1 edge fix + BC2
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
