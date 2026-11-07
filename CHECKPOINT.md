# CHECKPOINT 601 — read me first, then TASKS.md

**Written:** 2026-11-07T16:10:48Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `cb07a33` (this checkpoint is the commit after it)

## Just done
Tj's new message (16:10 UTC, v0.8.3 installed, video of the sliver): BC1 the wallpaper patch must cover to the right screen edge (a thin gray sliver of the stock card shows at the far right where the Screenshot button was); BC2 the patch flickers for a split second when the open menu is closed by a tap in the middle or by pulling up for the PIN (the stock scrim fades OUT, the patch does not follow). Written to TASKS BC1/BC2 in his words

## Do this next
analyze the video in the scratchpad ONLY (ffmpeg frames, never into the repo): find the sliver's x extent and the flicker frames; then read PatchRest.mask / TjAccessibilityService.buildPatchMasks (right edge clamped at the display: fr = min(f, w - cr) leaves 2 px of fringe) and the close path (PatchFade, patch hide on menu close / bouncer); fix both; JVM tests; emulator pt (close_max metrics exist in tools/pt_analyze.py); ship v0.8.4 only after the gate

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  d56dd0b ckpt 600: v0.8.3 is SHIPPED and the AN1 question is CLOSED: proving run #294 id 42350538
  ee295be ckpt 599: v0.8.3 SHIPPED: android.yml run 30 id 42350762180 on bf6a931 green (handoff ga
  4f584eb ckpt 598: android.yml full build RUNNING: run 30 id 42350762180 on main bf6a931 (v0.8.3 
  95ccfd5 ckpt 597: Tj said 'Build it now' (07:19 UTC): dispatched android.yml full_build=true on 
  7faba16 ckpt 596: run #293 id 42349158289 (base,x1,v,ac on 8309cc6) AN1 FAILED AGAIN (67 PASS 1 
  8309cc6 ckpt 595: run #292 id 42347734404 (base,x1,v,ac,ps) on 94671f9: 78 PASS, 1 FAIL = AN1 'g
  661d9df ckpt 594: run #291 id 42347732366 (pt) on 94671f9 GREEN, 0 FAIL: PB reach 28 rows (v0.8.
  34cd3da ckpt 593: gated v0.8.3 (code 28) and pushed it: the handoff self-test, the full unit sui
  c517c03 ckpt 592: HANDOFF.md 0g refreshed for v0.8.3 + old handoff-launcher.md marked superseded
  fa034b7 ckpt 591: Tj confirmed: it was the camera flashes (BB3 reading a); BB3a per-theme slots 
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
