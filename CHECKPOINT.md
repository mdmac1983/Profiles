# CHECKPOINT 610 — read me first, then TASKS.md

**Written:** 2026-11-07T17:09:17Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `a9b03dd` (this checkpoint is the commit after it)

## Just done
BC5 coded: TjAccessibilityService pinPadCameUp (patch alpha 0 at once + untouchable + latched until the menu is gone; signs: keyguard PIN/Password/Pattern/SecurityContainer/HostView view events from SystemUI, the PIN-pad scan flip), patchShow honours pinHide, setPatchTouchable never on while hidden; scenario pt phase G (menu open + wm dismiss-keyguard, soft check pin hide logged when a PIN pad came up); pt_analyze knows 'pin hide' rows

## Do this next
read the 3 gate runs #297 id 42394250639, #298 id 42394253079, #299 id 42394255486 (dispatched 17:02 on a8f76b6, due ~17:19); then dispatch pt,ae,ps,x1 on the final head (BC5 changed the patch visibility), read phase G (PT/BC5 lines: did 'pin hide' fire, how soon after the input), bump 0.8.4 / code 29, ship.sh, android.yml full_build on main, release, tell Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  35fa162 ckpt 609: Tj's answer (17:15 UTC): for the pull up for the PIN the patch must disappear 
  6ec98fc ckpt 608: Tj's new message (17:05 UTC): 'Also the lock screen still takes screenshots so
  b8f4fb9 ckpt 607: gate dispatched 17:02 UTC on a8f76b6 (the v0.8.4 candidate: BC1 edge fix + BC2
  a8f76b6 ckpt 606: emulator #296 read: the tap close is followed along the stock fade-out (close_
  daf4f15 ckpt 605: emulator #295 read (BC1 edge covered to 0.1 level; BC2: click + dialog-removal
  e079c40 ckpt 604: merged Tj's 16:13 message (a browser question, captured on main only) into the
  a502af8 ckpt 603: BC2/BC1 emulator proof coded: pt phase F (the menu closed by BACK / a tap in t
  1c33d22 ckpt 602: BC1b + BC2b step 1 coded: PatchRest.coverDisplayEdges (core to the display edg
  c564672 ckpt 601: Tj's new message (16:10 UTC, v0.8.3 installed, video of the sliver): BC1 the w
  d56dd0b ckpt 600: v0.8.3 is SHIPPED and the AN1 question is CLOSED: proving run #294 id 42350538
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
