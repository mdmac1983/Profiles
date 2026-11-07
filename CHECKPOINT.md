# CHECKPOINT 611 — read me first, then TASKS.md

**Written:** 2026-11-07T17:15:32Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `33926ee` (this checkpoint is the commit after it)

## Just done
Tj's new message (17:12 UTC): BD1 hidden apps (Telegram, Claude) were in the Recents list after unlocking to the custom launcher (they must NEVER appear there unless unhidden in the drawer); BD2 the unlock to the custom launcher is slow and keeps the black screen up for a long time (speed it up as much as possible, ideally everything behind Solitaire); written to TASKS BD1/BD2 in his words

## Do this next
finish v0.8.4 first (gate runs #297-#299 due 17:19, then pt,ae,ps,x1 for BC5, ship); then BD1/BD2: read HANDOFF.md (ReturnReset, RecentsFilter AP1, TaskOrigin, Solitaire cover, UNLOCKSPEED), ask Tj for the tablet report (the clean-up log and the unlock timing) and plan the fix

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  9c8439b ckpt 610: BC5 coded: TjAccessibilityService pinPadCameUp (patch alpha 0 at once + untouc
  35fa162 ckpt 609: Tj's answer (17:15 UTC): for the pull up for the PIN the patch must disappear 
  6ec98fc ckpt 608: Tj's new message (17:05 UTC): 'Also the lock screen still takes screenshots so
  b8f4fb9 ckpt 607: gate dispatched 17:02 UTC on a8f76b6 (the v0.8.4 candidate: BC1 edge fix + BC2
  a8f76b6 ckpt 606: emulator #296 read: the tap close is followed along the stock fade-out (close_
  daf4f15 ckpt 605: emulator #295 read (BC1 edge covered to 0.1 level; BC2: click + dialog-removal
  e079c40 ckpt 604: merged Tj's 16:13 message (a browser question, captured on main only) into the
  a502af8 ckpt 603: BC2/BC1 emulator proof coded: pt phase F (the menu closed by BACK / a tap in t
  1c33d22 ckpt 602: BC1b + BC2b step 1 coded: PatchRest.coverDisplayEdges (core to the display edg
  c564672 ckpt 601: Tj's new message (16:10 UTC, v0.8.3 installed, video of the sliver): BC1 the w
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
