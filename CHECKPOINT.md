# CHECKPOINT 582 — read me first, then TASKS.md

**Written:** 2026-11-07T05:51:10Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `83b529a` (this checkpoint is the commit after it)

## Just done
run #283 read: probe works (cores end 2/12/22 px below the panel as designed), 1 FAIL = noisy single fade film (now a 3-of-5 majority); stock screenshot shows a BRIGHT element (+136 levels) 4 px below the panel on the emulator = unexpected, blocks the shadow-tail measurement: added grid + window-list diagnostics

## Do this next
dispatch emulator.yml pt alone on ccr-823c5a56-yg8voe, read the 'PB grid' and 'PB windows' INFO lines (get_job_logs tail ~95): what is the white element below the panel (a window name? its x-extent) ; then measure the shadow beyond it or disable it, pick patch_extra_dp / bfeather defaults

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  31f893d ckpt 581: dispatched emulator.yml pt alone: run #283 id 42343234753 on aef44d4 (05:33Z),
  aef44d4 ckpt 580: BB1b step 1: patch core extension coded (PatchRest.slotWindow extra + clampExt
  9b150a1 ckpt 579: BB1 diagnosed from Tj's 2 new v0.8.2 photos: patch interior now right (left ed
  03502f8 ckpt 578: Tj tried v0.8.2: 'almost perfect but a very slight tinted line about where the
  564522c ckpt 577: Tj's screenshot: v0.8.2 measured ON THE TABLET scale 0.800 + offset 6/7/7 (tin
  7b42deb ckpt 576: v0.8.2 RELEASED + RECORDED: android.yml #29 id 42330571999 success, Release v0
  a730d5e ckpt 575: android.yml run #29 id 42330571999 dispatched 02:42:37Z on main 06f59a9 (full_
  06f59a9 ckpt 574: gated v0.8.2 (code 27) and pushed it: the handoff self-test, the full unit sui
  dda9e97 ckpt 573: dispatched on 24831a0: #281 id 42329499381 and #282 id 42329501526 (v,pa,pt an
  24831a0 ckpt 572: final gate #277 read: uf = emulator went offline (infra), pt FAIL = test assum
```

(7 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
