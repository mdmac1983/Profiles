# CHECKPOINT 350 — read me first, then TASKS.md

**Written:** 2026-11-05T19:00:00Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `5d04a4b` (this checkpoint is the commit after it)

## Just done
tablet intake 2026-11-05: Tj's first real report saved (3 red flags); the crash breaker tripped = the launcher is in QUICKSTEP (stock behaviour) since the 3 crashes at 12:48; keep-alive off, accessibility helper not bound, helper dead by 13:47 (HLP1 confirmed), MediaTek DuraSpeed suspect; facts + ACC1/CBR1 in TASKS.md

## Do this next
WAIT for Tj's step 8 screenshot (cable back): helper alive?, duraspeed values, accessibility bound/crashed lines, tj.log, shell-write test (ls -l of the probe folder); then tell Tj the custom launcher is OFF (breaker) and how to switch it back; v0.7.2 = KA1 + MBX1 + UX1 + ACC1 + CBR1 + PG5/PG6 + RS1 (+ HLP1)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  cedcd7c ckpt 349: Tj's step 7 (1:52): the shell CAN read the report the launcher wrote (owner u0
  ec20d98 ckpt 348: Tj's step 6 photo: the app-side report works without the helper (31 KB, in the
  a9cef92 ckpt 347: Tj's step 5 (1:47): SELinux Enforcing, build = MediaTek alps user/release-keys
  f04ea27 ckpt 346: Tj's crash log (1:39): the first-open crash = the keep-alive notification (id 
  6accd5d ckpt 345: Tj's step 3 result (1:37): grants, app-ops, battery whitelist and speed compil
  cbfb881 ckpt 344: Tj's step 3 screenshot (1:33): with a LIVE helper the launcher's log says unix
  0b4b40c ckpt 343: Tj: the tablet says Shell helper NOT running right after a fresh restart (help
  97cb41f ckpt 342: Tj's step 1 screenshot (1:31 phone clock): helper restarted OK ([1] 12440, uni
  000d69b ckpt 341: Tj says the launcher crashed when first opened on the tablet and now seems to 
  c87f389 ckpt 340: Tj's 3rd screenshots: the helper starts fine (unix up for uid 10243, tcp 47631
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
