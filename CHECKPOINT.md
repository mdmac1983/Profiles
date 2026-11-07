# CHECKPOINT 578 — read me first, then TASKS.md

**Written:** 2026-11-07T05:19:31Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `0cb8b8b` (this checkpoint is the commit after it)

## Just done
Tj tried v0.8.2: 'almost perfect but a very slight tinted line about where the bottom of the card would be' -> TASKS BB1 (photos he attached are the old v0.8.1 ones, byte-identical)

## Do this next
investigate BB1: read how the patch rect relates to the stock panel's full rect + shadow (TjAccessibilityService lockPatch geometry, PATCH_SIZE nudge), measure shadow extent / edge from Tj's photos, then decide a fix; emulator proof via a new INFO measurement in pt/pa

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  564522c ckpt 577: Tj's screenshot: v0.8.2 measured ON THE TABLET scale 0.800 + offset 6/7/7 (tin
  7b42deb ckpt 576: v0.8.2 RELEASED + RECORDED: android.yml #29 id 42330571999 success, Release v0
  a730d5e ckpt 575: android.yml run #29 id 42330571999 dispatched 02:42:37Z on main 06f59a9 (full_
  06f59a9 ckpt 574: gated v0.8.2 (code 27) and pushed it: the handoff self-test, the full unit sui
  dda9e97 ckpt 573: dispatched on 24831a0: #281 id 42329499381 and #282 id 42329501526 (v,pa,pt an
  24831a0 ckpt 572: final gate #277 read: uf = emulator went offline (infra), pt FAIL = test assum
  cd7a0a6 ckpt 571: tools/patch_photo_step.py written + selftest OK (v0.8.1 photos: patch 5-7 unit
  0cac1e7 ckpt 570: BA4 photos measured (patch 4-6 units darker, additive scrim part, TASKS BA4a);
  2675d47 ckpt 569: AZ4 root cause found (stale tj_restore.xml snapshot restores lock-screen notif
  2719c14 ckpt 568: FINAL GATE on dced3e9: #276 A{n,o,t,x,ac,bs,lq} id 42324558796, #277 B{base,v,
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
