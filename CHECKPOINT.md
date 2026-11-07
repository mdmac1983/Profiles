# CHECKPOINT 618 — read me first, then TASKS.md

**Written:** 2026-11-07T17:49:39Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `6151cae` (this checkpoint is the commit after it)

## Just done
v0.8.4 gated and pushed (code 29); #302 id 42397572681 (v alone) green after the V5 tolerance fix; #300 (pt,last) green; core on the FINAL app code dispatched 17:49 UTC on cc1f17f: #303 id 42398182125 {base,x1,e1,ac}, #304 id 42398184364 {n,o,rb}

## Do this next
read #303 and #304 at ~18:02 UTC (list_workflow_jobs latest; success = 0 FAIL); green -> dispatch android.yml (ref main, full_build=true), then get_release_by_tag v0.8.4 (Launcher-v0.8.4.apk), bash tools/record-release.sh v0.8.4 '<note>', refresh HANDOFF 0g (state + BC1/BC2/BC5 evidence + BC4/BD1/BD2 open), tell Tj (link on its own line; what to look at: right edge, tap-close fade, PIN-pull-up hide with and without the menu; the open-menu PIN case is untested on the emulator; 'Handoff ready: start a new session' for BD1/BD2/BC4)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  43cfe53 ckpt 617: gated v0.8.4 (code 29) and pushed it: the handoff self-test, the full unit sui
  7f3b5d6 ckpt 616: final-head gate read on d530023: #300 (pt,last) green; #301 (ae,ps,pa,x1,v) 43
  253981c ckpt 615: final-head runs dispatched 17:22 UTC on d530023: #300 id 42396001116 (pt,last)
  d530023 ckpt 614: gate read: #297 and #298 green on a8f76b6, #299 (pt,last) 3 FAIL = the noisy o
  986ddfa ckpt 613: Tj corrected BC5 (17:17 UTC): the PIN-pad hide must work with NO power menu op
  4a16127 ckpt 612: BD1/BD2 leads written to TASKS (ReturnReset black cover, TaskOrigin/RMTASKS by
  6bf1bcb ckpt 611: Tj's new message (17:12 UTC): BD1 hidden apps (Telegram, Claude) were in the R
  9c8439b ckpt 610: BC5 coded: TjAccessibilityService pinPadCameUp (patch alpha 0 at once + untouc
  35fa162 ckpt 609: Tj's answer (17:15 UTC): for the pull up for the PIN the patch must disappear 
  6ec98fc ckpt 608: Tj's new message (17:05 UTC): 'Also the lock screen still takes screenshots so
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
