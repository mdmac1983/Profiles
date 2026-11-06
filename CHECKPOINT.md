# CHECKPOINT 541 — read me first, then TASKS.md

**Written:** 2026-11-06T22:34:40Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-019b8e69-16dn91` · **builds on:** `97e801d` (this checkpoint is the commit after it)

## Just done
dispatched on ckpt 540 (sha 97e801d, 22:34 UTC): pt #249 (run id 42305657809), delta gate 3 runs: #250 id 42305692631 {base,y2,t,v,ps,uf}, #251 id 42305696260 {x1,e1,b6,o,bs,lq,ao,rb}, #252 id 42305699960 {n,x,ac,ad,ae,pa,rg,last}

## Do this next
read pt #249 first (list_workflow_jobs resource_id=run id -> get_job_logs tail 110): P first dim vs S onset, fade vs jump open_max, close curves; choose PatchFade.LEAD_MS (constant only); then gate runs (about 19 min): any FAIL -> read the log tail; reviewer agent report: scratchpad/review-v081.md; then bash ship.sh 'v0.8.1 ...'

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  97e801d ckpt 540: emulator.yml: ffmpeg install step has retries + 4 min timeout (run 248 hung 6 
  39c5ade ckpt 539: pt run 247: dialog event now handled in 2-17 ms (was 80-280) but the first dim
  a52dfe7 ckpt 538: quietWork cancelled in cancelPatchWork; TASKS AZ1 FLICKER FINDINGS written (ru
  ce7e0db ckpt 537: pt run 245 analysed: main-thread stall (PIN-pad scan + power-menu scan at the 
  6232c7e ckpt 536: pt fixes: fast path now reacts to the dialog's own window-state event (no getW
  c62f08c ckpt 535: pt shows the patch darkens BEFORE the scrim is visible (dev -26% at onset, bot
  4b8eabb ckpt 534: TASKS: AZ STATUS block; AW2 and AW3 ticked (run #239); AW4 check in lq; runs p
  58c4d76 ckpt 533: AW5 auto-calibration + AU2/AU4/AU6/AU7/AU8 coded; sections pa and pt added; fa
  d40e485 ckpt 532: AZ1 core coded (PatchFade + service fade + scan deferral + fast path), AW2 til
  3fca9b6 ckpt 531: AZ2 PROVEN on real Android 10 (run #238, section bs: 13 PASS; bug reproduced w
```
