# CHECKPOINT 579 — read me first, then TASKS.md

**Written:** 2026-11-07T05:27:17Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `3d4b006` (this checkpoint is the commit after it)

## Just done
BB1 diagnosed from Tj's 2 new v0.8.2 photos: patch interior now right (left edge +1..2 units); a DARK BAND (6-10 units) just below the patch bottom = the stock card's shadow shown through the 9 px fringe / past the 2 px core

## Do this next
BB1b: extend the opaque core below the panel (PatchRest.slotWindow extra param, hidden pref patch_extra_dp ~22 dp, within the 24 px crop margin) + JVM tests; add emulator section pb (tools/pb_probe.py) measuring shadow with patch OFF and the line with patch ON, extra 0 vs new default in ONE run; then gate sections from tools/gate.sh, ship v0.8.3

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  03502f8 ckpt 578: Tj tried v0.8.2: 'almost perfect but a very slight tinted line about where the
  564522c ckpt 577: Tj's screenshot: v0.8.2 measured ON THE TABLET scale 0.800 + offset 6/7/7 (tin
  7b42deb ckpt 576: v0.8.2 RELEASED + RECORDED: android.yml #29 id 42330571999 success, Release v0
  a730d5e ckpt 575: android.yml run #29 id 42330571999 dispatched 02:42:37Z on main 06f59a9 (full_
  06f59a9 ckpt 574: gated v0.8.2 (code 27) and pushed it: the handoff self-test, the full unit sui
  dda9e97 ckpt 573: dispatched on 24831a0: #281 id 42329499381 and #282 id 42329501526 (v,pa,pt an
  24831a0 ckpt 572: final gate #277 read: uf = emulator went offline (infra), pt FAIL = test assum
  cd7a0a6 ckpt 571: tools/patch_photo_step.py written + selftest OK (v0.8.1 photos: patch 5-7 unit
  0cac1e7 ckpt 570: BA4 photos measured (patch 4-6 units darker, additive scrim part, TASKS BA4a);
  2675d47 ckpt 569: AZ4 root cause found (stale tj_restore.xml snapshot restores lock-screen notif
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
