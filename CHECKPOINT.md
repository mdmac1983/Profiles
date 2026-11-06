# CHECKPOINT 439 — read me first, then TASKS.md

**Written:** 2026-11-06T04:48:48Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-22ba09da-s9vh1t` · **builds on:** `a780143` (this checkpoint is the commit after it)

## Just done
AF3: Tj parked the photo-label and privacy questions; launcher work only (AE7 long-press, AE3 AD7b patch); v0.7.4 final gates still running

## Do this next
check runs 180/181; develop AE7 + AE3 in a scratch worktree (NOT on the branch until v0.7.4 ships), save patches to research/pending/

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  14d9155 ckpt 438: TK3: v,ad run 179 SUCCESS on 364e8c0 (AD5 fixed by test change); waiting final
  3250146 ckpt 437: Tj's 7 photos: AE7 long-press Wallpapers-only, AE8 compare, AF2 privacy (4 old
  40a804e ckpt 436: FINAL gates on 364e8c0 dispatched 04:37Z: v,ad=run 179 (id 42163620016), gate 
  364e8c0 ckpt 435: AD5 root cause = profile_preclean closes apps at lock when section v leaves th
  1dec65c ckpt 434: AD5 bisect dispatched on e610616: v,ad=run 176 (id 42162299197), ka,ad=run 177
  e610616 ckpt 433: AD5 diagnostics: lock-log 'stock home, no resume: <why>' + ResumeTarget.whyNot
  2262058 ckpt 432: TK3 gate 2: B' only AD5 x2 fails (consistent in v,ka,ac,ad; passes in ac,ad,se
  2167b58 ckpt 431: AE3/AD7b build plan AE3-1..6 written (docs only); gates A=174 B'=175 running
  165fbd1 ckpt 430: TK3 gate 2 dispatched on b5c0efe: A=run 174 (id 42159304859), B'(no g)=run 175
  b5c0efe ckpt 429: TK3: review fixes RV1-RV5 coded + unit 351/0 + lint ok; dispatching gate A + B
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
