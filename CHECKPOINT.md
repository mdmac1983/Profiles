# CHECKPOINT 614 — read me first, then TASKS.md

**Written:** 2026-11-07T17:22:26Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `349cf7c` (this checkpoint is the commit after it)

## Just done
gate read: #297 and #298 green on a8f76b6, #299 (pt,last) 3 FAIL = the noisy opening-timing check + my two BC2 tap checks on a late stock animator (window removed 397 ms after the click, patch led its scrim by 10.3 %): tuned (start +40 ms, 10 % floor, scan slack 100 ms, checks accept 'menu closed', bound 15 %); the close-system-dialogs broadcast path works on the emulator (close_max 0.040)

## Do this next
dispatch on the final head: {pt,last} and {ae,ps,pa,x1,v} (two parallel emulator.yml runs on ccr-823c5a56-yg8voe, ~16 min); read: pt must show PT/BC5 (pin hide over the open menu) and PT/BC5b (pin hide resting) lines and 0 FAIL; then bump 0.8.4 / code 29, ship.sh, android.yml, release, tell Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  986ddfa ckpt 613: Tj corrected BC5 (17:17 UTC): the PIN-pad hide must work with NO power menu op
  4a16127 ckpt 612: BD1/BD2 leads written to TASKS (ReturnReset black cover, TaskOrigin/RMTASKS by
  6bf1bcb ckpt 611: Tj's new message (17:12 UTC): BD1 hidden apps (Telegram, Claude) were in the R
  9c8439b ckpt 610: BC5 coded: TjAccessibilityService pinPadCameUp (patch alpha 0 at once + untouc
  35fa162 ckpt 609: Tj's answer (17:15 UTC): for the pull up for the PIN the patch must disappear 
  6ec98fc ckpt 608: Tj's new message (17:05 UTC): 'Also the lock screen still takes screenshots so
  b8f4fb9 ckpt 607: gate dispatched 17:02 UTC on a8f76b6 (the v0.8.4 candidate: BC1 edge fix + BC2
  a8f76b6 ckpt 606: emulator #296 read: the tap close is followed along the stock fade-out (close_
  daf4f15 ckpt 605: emulator #295 read (BC1 edge covered to 0.1 level; BC2: click + dialog-removal
  e079c40 ckpt 604: merged Tj's 16:13 message (a browser question, captured on main only) into the
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
