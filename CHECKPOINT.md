# CHECKPOINT 542 — read me first, then TASKS.md

**Written:** 2026-11-06T22:38:55Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-019b8e69-16dn91` · **builds on:** `7cbb7fd` (this checkpoint is the commit after it)

## Just done
review of v0.8.1 (agent report) fixed: stored crop invalidated when it no longer covers the patch window (PatchShot.invalidate), battery-saver heal skipped when low_power changed after the request, tile guard honours automation pause, touchable-on not re-armed, patchShow skips the keyguard binder call when the menu is open, screenshot owner accepted if a system package, calibrate button writes its own start, onInterrupt ends the calibration window

## Do this next
read pt #249 (42305657809) and the gate runs #250 42305692631, #251 42305696260, #252 42305699960 (all on ckpt 540 code: the reviewer fixes are NOT in them, they only touch calibration/battery/tile paths: rerun lq, bs, pa, ps after); then LEAD_MS, ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  dc94174 ckpt 541: dispatched on ckpt 540 (sha 97e801d, 22:34 UTC): pt #249 (run id 42305657809),
  97e801d ckpt 540: emulator.yml: ffmpeg install step has retries + 4 min timeout (run 248 hung 6 
  39c5ade ckpt 539: pt run 247: dialog event now handled in 2-17 ms (was 80-280) but the first dim
  a52dfe7 ckpt 538: quietWork cancelled in cancelPatchWork; TASKS AZ1 FLICKER FINDINGS written (ru
  ce7e0db ckpt 537: pt run 245 analysed: main-thread stall (PIN-pad scan + power-menu scan at the 
  6232c7e ckpt 536: pt fixes: fast path now reacts to the dialog's own window-state event (no getW
  c62f08c ckpt 535: pt shows the patch darkens BEFORE the scrim is visible (dev -26% at onset, bot
  4b8eabb ckpt 534: TASKS: AZ STATUS block; AW2 and AW3 ticked (run #239); AW4 check in lq; runs p
  58c4d76 ckpt 533: AW5 auto-calibration + AU2/AU4/AU6/AU7/AU8 coded; sections pa and pt added; fa
  d40e485 ckpt 532: AZ1 core coded (PatchFade + service fade + scan deferral + fast path), AW2 til
```

(7 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
