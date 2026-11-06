# CHECKPOINT 441 — read me first, then TASKS.md

**Written:** 2026-11-06T04:52:42Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-22ba09da-s9vh1t` · **builds on:** `8ccaceb` (this checkpoint is the commit after it)

## Just done
v0.7.5 prep: AE7 long-press + AE3/AD7b resting lock patch (PatchRest + service + Control switch + wallpaper-kind probe) coded in scratch worktree, unit 359/0; saved as research/pending/v075-all.patch (NOT applied)

## Do this next
write emulator section ae (resting patch) in the worktree; read gates 180/181 at ~05:03; after v0.7.4 ships: git apply research/pending/v075-all.patch, bump 0.7.5, gate

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     D research/pending/v075-ae7-longpress.patch
    ?? research/pending/v075-all.patch

## Last ten checkpoints
```
  8ccaceb ckpt 440: v0.7.5 prep: AE7 long-press Wallpapers-only coded in scratch worktree, saved a
  d47af7f ckpt 439: AF3: Tj parked the photo-label and privacy questions; launcher work only (AE7 
  14d9155 ckpt 438: TK3: v,ad run 179 SUCCESS on 364e8c0 (AD5 fixed by test change); waiting final
  3250146 ckpt 437: Tj's 7 photos: AE7 long-press Wallpapers-only, AE8 compare, AF2 privacy (4 old
  40a804e ckpt 436: FINAL gates on 364e8c0 dispatched 04:37Z: v,ad=run 179 (id 42163620016), gate 
  364e8c0 ckpt 435: AD5 root cause = profile_preclean closes apps at lock when section v leaves th
  1dec65c ckpt 434: AD5 bisect dispatched on e610616: v,ad=run 176 (id 42162299197), ka,ad=run 177
  e610616 ckpt 433: AD5 diagnostics: lock-log 'stock home, no resume: <why>' + ResumeTarget.whyNot
  2262058 ckpt 432: TK3 gate 2: B' only AD5 x2 fails (consistent in v,ka,ac,ad; passes in ac,ad,se
  2167b58 ckpt 431: AE3/AD7b build plan AE3-1..6 written (docs only); gates A=174 B'=175 running
```
