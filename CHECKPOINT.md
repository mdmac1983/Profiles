# CHECKPOINT 543 — read me first, then TASKS.md

**Written:** 2026-11-06T22:45:19Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-019b8e69-16dn91` · **builds on:** `cecc569` (this checkpoint is the commit after it)

## Just done
pt run 249: 9 PASS: fade open_max 3-6 % vs jump 26 %; found the mid-open glitch (a scan ~0.5 s after the open finds no menu text while the dialog window is up -> patch back to bright rest for ~170 ms): hold rule (keep the patch while the dialog window is in the list, 3 misses max), PatchFade.LEAD_MS 25 -> 10 (sweep -25..80), scan/hold lines in the pt timeline, new pt check hold_max

## Do this next
dispatch pt again (emulator.yml sections=pt): check hold_max < 0.08, open_max, no 'menu closed' mid-open in the timeline; read gate runs 250/251/252; rerun lq,bs,pa,ps (reviewer fixes touch calibration/battery/tile); then ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  964beab ckpt 542: review of v0.8.1 (agent report) fixed: stored crop invalidated when it no long
  dc94174 ckpt 541: dispatched on ckpt 540 (sha 97e801d, 22:34 UTC): pt #249 (run id 42305657809),
  97e801d ckpt 540: emulator.yml: ffmpeg install step has retries + 4 min timeout (run 248 hung 6 
  39c5ade ckpt 539: pt run 247: dialog event now handled in 2-17 ms (was 80-280) but the first dim
  a52dfe7 ckpt 538: quietWork cancelled in cancelPatchWork; TASKS AZ1 FLICKER FINDINGS written (ru
  ce7e0db ckpt 537: pt run 245 analysed: main-thread stall (PIN-pad scan + power-menu scan at the 
  6232c7e ckpt 536: pt fixes: fast path now reacts to the dialog's own window-state event (no getW
  c62f08c ckpt 535: pt shows the patch darkens BEFORE the scrim is visible (dev -26% at onset, bot
  4b8eabb ckpt 534: TASKS: AZ STATUS block; AW2 and AW3 ticked (run #239); AW4 check in lq; runs p
  58c4d76 ckpt 533: AW5 auto-calibration + AU2/AU4/AU6/AU7/AU8 coded; sections pa and pt added; fa
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
