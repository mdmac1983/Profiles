# CHECKPOINT 445 — read me first, then TASKS.md

**Written:** 2026-11-06T05:06:38Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-22ba09da-s9vh1t` · **builds on:** `7defd73` (this checkpoint is the commit after it)

## Just done
TK3: rerun ad,se,rt = run 182 (id 42166392626) on 7defd73 (product == 364e8c0); gate A = run 180 (job 127714999595) still running

## Do this next
~05:21 read 182 (needs rt AA3 green) and gate A 180; both clean => ship v0.7.4 (git push origin 364e8c0-or-descendant-with-same-product:refs/heads/main, android.yml full_build, get_release_by_tag, record via main worktree, ls-remote check)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  7defd73 ckpt 444: TK3 gate B': AD5 passes; rt AA3 x2 = leftover origin marks from ad (test order
  b541689 ckpt 443: AG1 battery saver follows the launcher (Quickstep ON, custom OFF) coded in the
  3bfc319 ckpt 442: v0.7.5 prep: emulator section ae (resting patch, phases A wrong-model / B phot
  41a919e ckpt 441: v0.7.5 prep: AE7 long-press + AE3/AD7b resting lock patch (PatchRest + service
  8ccaceb ckpt 440: v0.7.5 prep: AE7 long-press Wallpapers-only coded in scratch worktree, saved a
  d47af7f ckpt 439: AF3: Tj parked the photo-label and privacy questions; launcher work only (AE7 
  14d9155 ckpt 438: TK3: v,ad run 179 SUCCESS on 364e8c0 (AD5 fixed by test change); waiting final
  3250146 ckpt 437: Tj's 7 photos: AE7 long-press Wallpapers-only, AE8 compare, AF2 privacy (4 old
  40a804e ckpt 436: FINAL gates on 364e8c0 dispatched 04:37Z: v,ad=run 179 (id 42163620016), gate 
  364e8c0 ckpt 435: AD5 root cause = profile_preclean closes apps at lock when section v leaves th
```
