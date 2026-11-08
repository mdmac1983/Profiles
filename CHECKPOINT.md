# CHECKPOINT 699 — read me first, then TASKS.md

**Written:** 2026-11-08T17:11:50Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-dda19ce3-8dl2cy` · **builds on:** `783b81a` (this checkpoint is the commit after it)

## Just done
BJ1 GATE on head 783b81a dispatched 17:11 UTC: run #384 id 42503687941 = b6,n,o,hp,last; #385 id 42503690388 = e1,pt; #386 id 42503692964 = base,x1,ac,hf,rb (mapping by dispatch order, confirm from the 'sections:' INFO line). Tier 0 (full unit suite + lint) running locally

## Do this next
when the 3 runs end (~17:30): read each (list_workflow_jobs -> get_job_logs tail 60); all green -> bash ship.sh 'v0.8.10 note', android.yml full_build=true, get_release_by_tag, record-release.sh; FAIL -> compare with the v0.8.9 tag first

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  783b81a ckpt 698: BJ1c: run #383 (id 42502655352, head 825ae3d) VERIFIED the fix: hf 10 PASS 0 F
  5afdc9d ckpt 697: BJ1c: /code-review high (8 findings) handled: fade summary moved out of the la
  825ae3d ckpt 696: BJ1c: run #382 PROVED the cause (home started no wait for a burst's first wind
  22a8038 ckpt 695: BJ1b: per-fade frame statistics (first draw lateness, frames, slow frames, wor
  e321189 ckpt 694: BJ1b: emulator.yml run #382 id 42500697309 (sections=hf, head ae64eec) dispatc
  ae64eec ckpt 693: BJ1b: section hf (home fade film, 4 arms x 5 films) + debug-only switch home_b
  8c67d0a ckpt 692: BJ1 written into TASKS (Tj 16:21 UTC: home-screen patch looks right but the op
  a0dca77 ckpt 691: TASKS BI1a ticked; v0.8.9 is released and recorded, nothing else open except T
  4283fdd ckpt 690: v0.8.9 RELEASED 08:05 UTC and RECORDED 08:12 (android.yml run 36 id 4246129231
  bf2e8cf ckpt 689: v0.8.9 RELEASE BUILD triggered 08:03 UTC: android.yml run 36 id 42461292313 on
```
