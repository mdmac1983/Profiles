# CHECKPOINT 573 — read me first, then TASKS.md

**Written:** 2026-11-07T02:29:53Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `24831a0` (this checkpoint is the commit after it)

## Just done
dispatched on 24831a0: #281 id 42329499381 and #282 id 42329501526 (v,pa,pt and base,v,uf,last: check which is which in the INFO sections line)

## Do this next
read both (list_workflow_jobs latest then get_job_logs tail ~100; 404 = still running, expect done ~02:45Z): both success -> bash ship.sh 'v0.8.2: ...' then android.yml full_build=true on branch ccr-823c5a56-yg8voe -> get_release_by_tag v0.8.2 -> tools/record-release.sh; a FAIL -> diagnose first, do not just rerun

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  24831a0 ckpt 572: final gate #277 read: uf = emulator went offline (infra), pt FAIL = test assum
  cd7a0a6 ckpt 571: tools/patch_photo_step.py written + selftest OK (v0.8.1 photos: patch 5-7 unit
  0cac1e7 ckpt 570: BA4 photos measured (patch 4-6 units darker, additive scrim part, TASKS BA4a);
  2675d47 ckpt 569: AZ4 root cause found (stale tj_restore.xml snapshot restores lock-screen notif
  2719c14 ckpt 568: FINAL GATE on dced3e9: #276 A{n,o,t,x,ac,bs,lq} id 42324558796, #277 B{base,v,
  dced3e9 ckpt 567: version 0.8.2 / code 27 (final gate runs on this commit)
  3815d6b ckpt 566: review of BA1/BA2 done: 3 major fixed (memory wipe on abandoned handshake, res
  9281c6d ckpt 565: gate #271: ps phase B 4 FAIL (bright layer over the slot, after x1,e1,bl); dia
  3056094 ckpt 564: BA1 proven: pt #272 15 PASS (MEASURED 0.7997/0.7999/0.7999 off 0.03/0.0035/0.0
  27728f8 ckpt 563: BA4 written: Tj will send a photo of the patch later (his words in TASKS)
```
