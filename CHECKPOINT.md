# CHECKPOINT 576 — read me first, then TASKS.md

**Written:** 2026-11-07T02:55:17Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `b8a78c1` (this checkpoint is the commit after it)

## Just done
v0.8.2 RELEASED + RECORDED: android.yml #29 id 42330571999 success, Release v0.8.2 (Launcher-v0.8.2.apk, signed), BUILDLOG ae30730; BA1/BA2/BA3/BA4b ticked; HANDOFF 0g refreshed

## Do this next
WAIT FOR TJ to install v0.8.2 and report (TASKS BA4c): his 'Menu dimming measurement' note / tablet report (bash tools/tablet-intake.sh) and one straight photo of the open menu (python3 -I tools/patch_photo_step.py <photo>: expect NO CLEAR STEP; if the tablet measured ~0.80 / 0 offset the step is NOT the scrim) + battery-saver part B of research/TABLET_CHECKLIST_v0.8.2.md. Standing: AU5, RS1a-c, HLP1; Tj decisions CBR2, AF2, AE6

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  a730d5e ckpt 575: android.yml run #29 id 42330571999 dispatched 02:42:37Z on main 06f59a9 (full_
  06f59a9 ckpt 574: gated v0.8.2 (code 27) and pushed it: the handoff self-test, the full unit sui
  dda9e97 ckpt 573: dispatched on 24831a0: #281 id 42329499381 and #282 id 42329501526 (v,pa,pt an
  24831a0 ckpt 572: final gate #277 read: uf = emulator went offline (infra), pt FAIL = test assum
  cd7a0a6 ckpt 571: tools/patch_photo_step.py written + selftest OK (v0.8.1 photos: patch 5-7 unit
  0cac1e7 ckpt 570: BA4 photos measured (patch 4-6 units darker, additive scrim part, TASKS BA4a);
  2675d47 ckpt 569: AZ4 root cause found (stale tj_restore.xml snapshot restores lock-screen notif
  2719c14 ckpt 568: FINAL GATE on dced3e9: #276 A{n,o,t,x,ac,bs,lq} id 42324558796, #277 B{base,v,
  dced3e9 ckpt 567: version 0.8.2 / code 27 (final gate runs on this commit)
  3815d6b ckpt 566: review of BA1/BA2 done: 3 major fixed (memory wipe on abandoned handshake, res
```

(7 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
