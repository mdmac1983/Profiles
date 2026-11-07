# CHECKPOINT 609 — read me first, then TASKS.md

**Written:** 2026-11-07T17:07:31Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `7fb66f3` (this checkpoint is the commit after it)

## Just done
Tj's answer (17:15 UTC): for the pull up for the PIN the patch must disappear immediately, as fast as possible -> TASKS BC5 (pinHide latch + fast bouncer-view event signal + lastBouncer fallback); the v0.8.4 gate runs #297-#299 still in flight on a8f76b6

## Do this next
implement BC5 in TjAccessibilityService (pinHide, pinPadCameUp, patchShow honours it, fastMenuClose bouncer class check, updateLockNumber flip), scenario pt phase G (menu open, wm dismiss-keyguard, helper lines + PATCHEV), then read the 3 gate runs, re-run pt,ae,ps,x1 on the final head, ship v0.8.4

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  6ec98fc ckpt 608: Tj's new message (17:05 UTC): 'Also the lock screen still takes screenshots so
  b8f4fb9 ckpt 607: gate dispatched 17:02 UTC on a8f76b6 (the v0.8.4 candidate: BC1 edge fix + BC2
  a8f76b6 ckpt 606: emulator #296 read: the tap close is followed along the stock fade-out (close_
  daf4f15 ckpt 605: emulator #295 read (BC1 edge covered to 0.1 level; BC2: click + dialog-removal
  e079c40 ckpt 604: merged Tj's 16:13 message (a browser question, captured on main only) into the
  a502af8 ckpt 603: BC2/BC1 emulator proof coded: pt phase F (the menu closed by BACK / a tap in t
  1c33d22 ckpt 602: BC1b + BC2b step 1 coded: PatchRest.coverDisplayEdges (core to the display edg
  c564672 ckpt 601: Tj's new message (16:10 UTC, v0.8.3 installed, video of the sliver): BC1 the w
  d56dd0b ckpt 600: v0.8.3 is SHIPPED and the AN1 question is CLOSED: proving run #294 id 42350538
  ee295be ckpt 599: v0.8.3 SHIPPED: android.yml run 30 id 42350762180 on bf6a931 green (handoff ga
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
