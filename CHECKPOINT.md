# CHECKPOINT 346 — read me first, then TASKS.md

**Written:** 2026-11-05T18:43:45Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `6accd5d` (this checkpoint is the commit after it)

## Just done
Tj's crash log (1:39): the first-open crash = the keep-alive notification (id 7471) whose invisible icon tj_ic_blank (an EMPTY vector, resource 0x7f07009a) this tablet's SystemUI cannot create, so Android crashed the launcher 3 times in 7 s; CR1 answered, KA1 (fix for v0.7.2) added

## Do this next
step 5 sent: getenforce; getprop ro.build.fingerprint; ls -ld /sdcard/Android/data/com.tj.launcher/files (SELinux mode, ROM identity, can the shell see the app's shared folder = MBX1 evidence); WAIT for the screenshot; then offer: tap 2. Collect the tablet report (app side, shows 'crash breaker tripped'), then the side-key test from the phone; v0.7.2 = KA1 + MBX1 + PG5/PG6 + RS1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  6accd5d ckpt 345: Tj's step 3 result (1:37): grants, app-ops, battery whitelist and speed compil
  cbfb881 ckpt 344: Tj's step 3 screenshot (1:33): with a LIVE helper the launcher's log says unix
  0b4b40c ckpt 343: Tj: the tablet says Shell helper NOT running right after a fresh restart (help
  97cb41f ckpt 342: Tj's step 1 screenshot (1:31 phone clock): helper restarted OK ([1] 12440, uni
  000d69b ckpt 341: Tj says the launcher crashed when first opened on the tablet and now seems to 
  c87f389 ckpt 340: Tj's 3rd screenshots: the helper starts fine (unix up for uid 10243, tcp 47631
  bf2b0c8 ckpt 339: Tj's 2nd screenshot (1:02): helper start attempt 1 left no process, attempt 2 
  336c9e7 ckpt 338: Tj is in the CABLE SESSION (screenshot 12:59 phone clock): shell uid 2000 OK, 
  63a17ba ckpt 337: v0.7.1 RELEASED (android.yml run 20 green, signed): https://github.com/tjshea9
  e06b833 ckpt 336: v0.7.0 experiment (run 129) = identical legacy-alone failure on 2a547f6: the n
```
