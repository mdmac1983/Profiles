# CHECKPOINT 575 — read me first, then TASKS.md

**Written:** 2026-11-07T02:42:56Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `06f59a9` (this checkpoint is the commit after it)

## Just done
android.yml run #29 id 42330571999 dispatched 02:42:37Z on main 06f59a9 (full_build=true): GitHub is building + signing v0.8.2

## Do this next
wait until ~02:55Z; list_workflow_jobs latest on run 42330571999: success -> get_release_by_tag v0.8.2 (needs Launcher-v0.8.2.apk) -> bash tools/record-release.sh v0.8.2 with the note printed by ship.sh (it is in the ckpt 574 message) -> tick BA1/BA2/BA3 in TASKS -> tell Tj with the Release link on its own line (what he will see once: 2 camera flashes + menu opens by itself ~2 s; switch: Launcher control > Power menu > Measure the menu's real dimming); failure -> get_job_logs failed_only, diagnose, do not just rerun

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  06f59a9 ckpt 574: gated v0.8.2 (code 27) and pushed it: the handoff self-test, the full unit sui
  dda9e97 ckpt 573: dispatched on 24831a0: #281 id 42329499381 and #282 id 42329501526 (v,pa,pt an
  24831a0 ckpt 572: final gate #277 read: uf = emulator went offline (infra), pt FAIL = test assum
  cd7a0a6 ckpt 571: tools/patch_photo_step.py written + selftest OK (v0.8.1 photos: patch 5-7 unit
  0cac1e7 ckpt 570: BA4 photos measured (patch 4-6 units darker, additive scrim part, TASKS BA4a);
  2675d47 ckpt 569: AZ4 root cause found (stale tj_restore.xml snapshot restores lock-screen notif
  2719c14 ckpt 568: FINAL GATE on dced3e9: #276 A{n,o,t,x,ac,bs,lq} id 42324558796, #277 B{base,v,
  dced3e9 ckpt 567: version 0.8.2 / code 27 (final gate runs on this commit)
  3815d6b ckpt 566: review of BA1/BA2 done: 3 major fixed (memory wipe on abandoned handshake, res
  9281c6d ckpt 565: gate #271: ps phase B 4 FAIL (bright layer over the slot, after x1,e1,bl); dia
```
