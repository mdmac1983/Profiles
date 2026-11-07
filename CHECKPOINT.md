# CHECKPOINT 571 — read me first, then TASKS.md

**Written:** 2026-11-07T02:20:01Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `970eb43` (this checkpoint is the commit after it)

## Just done
tools/patch_photo_step.py written + selftest OK (v0.8.1 photos: patch 5-7 units darker); local gradle tests+lint running

## Do this next
read #277 results after its 50-min job limit (02:22Z; run 42324561598 job 128256727879, get_job_logs tail 80); then re-run on final code whatever it did not prove (base,v,uf,last and pt), then bash ship.sh

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  0cac1e7 ckpt 570: BA4 photos measured (patch 4-6 units darker, additive scrim part, TASKS BA4a);
  2675d47 ckpt 569: AZ4 root cause found (stale tj_restore.xml snapshot restores lock-screen notif
  2719c14 ckpt 568: FINAL GATE on dced3e9: #276 A{n,o,t,x,ac,bs,lq} id 42324558796, #277 B{base,v,
  dced3e9 ckpt 567: version 0.8.2 / code 27 (final gate runs on this commit)
  3815d6b ckpt 566: review of BA1/BA2 done: 3 major fixed (memory wipe on abandoned handshake, res
  9281c6d ckpt 565: gate #271: ps phase B 4 FAIL (bright layer over the slot, after x1,e1,bl); dia
  3056094 ckpt 564: BA1 proven: pt #272 15 PASS (MEASURED 0.7997/0.7999/0.7999 off 0.03/0.0035/0.0
  27728f8 ckpt 563: BA4 written: Tj will send a photo of the patch later (his words in TASKS)
  102771e ckpt 562: waiting: pt #272 job 128248073003, gates 128247261992 + 128247271172, code rev
  747eebd ckpt 561: pt phase C fixed (no extra lock); gate jobs: 128247261992 {n,o,t,x,ac,bs,lq}, 
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
