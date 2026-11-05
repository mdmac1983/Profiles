# CHECKPOINT 349 — read me first, then TASKS.md

**Written:** 2026-11-05T18:54:25Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `ec20d98` (this checkpoint is the commit after it)

## Just done
Tj's step 7 (1:52): the shell CAN read the report the launcher wrote (owner u0_a243, group sdcard_rw) and copied it to Downloads/launcher-probe: MBX1 viable in the app->shell direction; step 8 sent: helper liveness (started 1:31) + tj.log + a shell-writes-into-the-app-folder test

## Do this next
WAIT for Tj's step 8 screenshot (ps count, tj.log, ls -l of the probe folder with from-shell.txt: owner/mode); then tell Tj he can unplug and send tablet-report.txt from Downloads/launcher-probe later (Files app share or 2b); then intake it: bash tools/tablet-intake.sh <file>, read research/tablet/LATEST.md; v0.7.2 = KA1 + MBX1 + UX1 + PG5/PG6 + RS1 (+ HLP1 if the helper died)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  ec20d98 ckpt 348: Tj's step 6 photo: the app-side report works without the helper (31 KB, in the
  a9cef92 ckpt 347: Tj's step 5 (1:47): SELinux Enforcing, build = MediaTek alps user/release-keys
  f04ea27 ckpt 346: Tj's crash log (1:39): the first-open crash = the keep-alive notification (id 
  6accd5d ckpt 345: Tj's step 3 result (1:37): grants, app-ops, battery whitelist and speed compil
  cbfb881 ckpt 344: Tj's step 3 screenshot (1:33): with a LIVE helper the launcher's log says unix
  0b4b40c ckpt 343: Tj: the tablet says Shell helper NOT running right after a fresh restart (help
  97cb41f ckpt 342: Tj's step 1 screenshot (1:31 phone clock): helper restarted OK ([1] 12440, uni
  000d69b ckpt 341: Tj says the launcher crashed when first opened on the tablet and now seems to 
  c87f389 ckpt 340: Tj's 3rd screenshots: the helper starts fine (unix up for uid 10243, tcp 47631
  bf2b0c8 ckpt 339: Tj's 2nd screenshot (1:02): helper start attempt 1 left no process, attempt 2 
```
