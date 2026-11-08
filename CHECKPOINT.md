# CHECKPOINT 688 — read me first, then TASKS.md

**Written:** 2026-11-08T08:03:19Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2r9ji2` · **builds on:** `5b3d990` (this checkpoint is the commit after it)

## Just done
gated v0.8.9 (code 34) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.8.9 "the power menu on the unlocked custom home screen is now the stock one: the Screenshot button is hidden by a patch of your real wallpaper plus the Launcher's own icons, like on the lock screen (the home screen goes blank for about a second and the screen flashes once the first time, per wallpaper and per home page; new switch in Launcher control, and my old card covers the buttons whenever the patch could be wrong); the start-of-session clean-up also looks in the shade when something was posted even if no status-bar icon shows; the tablet report shows the home patch state and which other windows took it away"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  7a30b64 ckpt 687: EXPERIMENT 07:50 (temporary, debug-only): TjHooks.exp bit mask (Settings.Globa
  b3b55ce ckpt 686: v0.8.9 NOT SHIPPED: 8 CI runs in flight 07:13-07:22: delta gate #366 hp, #367 
  5a7e019 ckpt 685: v0.8.9: W5 (stock home redirect, section legacy) failed twice on main but base
  db1c63e ckpt 684: v0.8.9 DELTA GATE 07:14 on 97905e3 (product delta since 9c40e5c = the foreign-
  e5d3cb9 ckpt 683: v0.8.9 gate rerun map 06:58: #359 pt alone 27 PASS 1 FAIL (BC2 closetap) so #3
  f619254 ckpt 682: v0.8.9 GATE READ 06:40: #356 (b6,n,n6,v,pa,rg,cs2,hp,pf,last) 89 PASS 2 FAIL =
  3d26ef4 ckpt 681: v0.8.9 GATE dispatched 06:05 UTC on 9c40e5c (hp alone on it = #354 28 PASS): #
  9c40e5c ckpt 680: v0.8.9: first gate (#351-353 on e60f9ad) CANCELLED at 05:55 UTC on purpose: a 
  fd738df ckpt 679: v0.8.9 GATE dispatched 05:47 UTC on e60f9ad (product code final, version 0.8.9
  e60f9ad ckpt 678: BI1c: hp FULLY GREEN: run #349 (4d78cfb, id 42451187830) 25 PASS; run #350 (01
```

(8 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
