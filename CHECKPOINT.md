# CHECKPOINT 574 — read me first, then TASKS.md

**Written:** 2026-11-07T02:42:23Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `464ae68` (this checkpoint is the commit after it)

## Just done
gated v0.8.2 (code 27) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.8.2 "v0.8.2: the tablet measures the stock power menu's real dimming so the wallpaper patch ends level with the dimmed lock screen; battery saver is off whenever the lock screen is up, and Quickstep's saver state comes back after a gesture unlock when Android cannot have lost track"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  dda9e97 ckpt 573: dispatched on 24831a0: #281 id 42329499381 and #282 id 42329501526 (v,pa,pt an
  24831a0 ckpt 572: final gate #277 read: uf = emulator went offline (infra), pt FAIL = test assum
  cd7a0a6 ckpt 571: tools/patch_photo_step.py written + selftest OK (v0.8.1 photos: patch 5-7 unit
  0cac1e7 ckpt 570: BA4 photos measured (patch 4-6 units darker, additive scrim part, TASKS BA4a);
  2675d47 ckpt 569: AZ4 root cause found (stale tj_restore.xml snapshot restores lock-screen notif
  2719c14 ckpt 568: FINAL GATE on dced3e9: #276 A{n,o,t,x,ac,bs,lq} id 42324558796, #277 B{base,v,
  dced3e9 ckpt 567: version 0.8.2 / code 27 (final gate runs on this commit)
  3815d6b ckpt 566: review of BA1/BA2 done: 3 major fixed (memory wipe on abandoned handshake, res
  9281c6d ckpt 565: gate #271: ps phase B 4 FAIL (bright layer over the slot, after x1,e1,bl); dia
  3056094 ckpt 564: BA1 proven: pt #272 15 PASS (MEASURED 0.7997/0.7999/0.7999 off 0.03/0.0035/0.0
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
