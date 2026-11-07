# CHECKPOINT 613 — read me first, then TASKS.md

**Written:** 2026-11-07T17:18:03Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `92a28dd` (this checkpoint is the commit after it)

## Just done
Tj corrected BC5 (17:17 UTC): the PIN-pad hide must work with NO power menu open (the resting patch too): pinPadCameUp now hides the resting patch (alpha 0 at once, away 2.5 s) as well as the open one; fastMenuClose's bouncer sign no longer needs the menu; scenario pt phase H (swipe up on the plain lock screen, 'pin hide ... resting' check); TASKS BC5b

## Do this next
read the 3 gate runs (#297 id 42394250639, #298 id 42394253079, #299 id 42394255486, due ~17:19); then dispatch pt,ae,ps,x1 on this head (BC5 + BC5b), read PT/BC5 and PT/BC5b lines; ship v0.8.4

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  4a16127 ckpt 612: BD1/BD2 leads written to TASKS (ReturnReset black cover, TaskOrigin/RMTASKS by
  6bf1bcb ckpt 611: Tj's new message (17:12 UTC): BD1 hidden apps (Telegram, Claude) were in the R
  9c8439b ckpt 610: BC5 coded: TjAccessibilityService pinPadCameUp (patch alpha 0 at once + untouc
  35fa162 ckpt 609: Tj's answer (17:15 UTC): for the pull up for the PIN the patch must disappear 
  6ec98fc ckpt 608: Tj's new message (17:05 UTC): 'Also the lock screen still takes screenshots so
  b8f4fb9 ckpt 607: gate dispatched 17:02 UTC on a8f76b6 (the v0.8.4 candidate: BC1 edge fix + BC2
  a8f76b6 ckpt 606: emulator #296 read: the tap close is followed along the stock fade-out (close_
  daf4f15 ckpt 605: emulator #295 read (BC1 edge covered to 0.1 level; BC2: click + dialog-removal
  e079c40 ckpt 604: merged Tj's 16:13 message (a browser question, captured on main only) into the
  a502af8 ckpt 603: BC2/BC1 emulator proof coded: pt phase F (the menu closed by BACK / a tap in t
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
