# CHECKPOINT 344 — read me first, then TASKS.md

**Written:** 2026-11-05T18:36:11Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `0b4b40c` (this checkpoint is the commit after it)

## Just done
Tj's step 3 screenshot (1:33): with a LIVE helper the launcher's log says unix Permission denied + TCP EPERM = no socket channel works on this ROM (HLP3 answered); MBX1 (file mailbox transport) added; sent step 3 = one line with the guide's grants + appops + deviceidle + granted check incl. INTERNET + accessibility get + compile

## Do this next
WAIT for Tj's screenshot of the end of that line (granted lines, services, Success); then step 4: logcat -d -b crash | tail -40 for CR1; then tap '2. Collect the tablet report' on the tablet (app side only) and share it; then ls -l of /sdcard/Android/data/com.tj.launcher/files/probe on the phone (MBX1 evidence); afterwards MBX1 as v0.7.2

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  0b4b40c ckpt 343: Tj: the tablet says Shell helper NOT running right after a fresh restart (help
  97cb41f ckpt 342: Tj's step 1 screenshot (1:31 phone clock): helper restarted OK ([1] 12440, uni
  000d69b ckpt 341: Tj says the launcher crashed when first opened on the tablet and now seems to 
  c87f389 ckpt 340: Tj's 3rd screenshots: the helper starts fine (unix up for uid 10243, tcp 47631
  bf2b0c8 ckpt 339: Tj's 2nd screenshot (1:02): helper start attempt 1 left no process, attempt 2 
  336c9e7 ckpt 338: Tj is in the CABLE SESSION (screenshot 12:59 phone clock): shell uid 2000 OK, 
  63a17ba ckpt 337: v0.7.1 RELEASED (android.yml run 20 green, signed): https://github.com/tjshea9
  e06b833 ckpt 336: v0.7.0 experiment (run 129) = identical legacy-alone failure on 2a547f6: the n
  4686fda ckpt 335: ship.sh gates passed again for v0.7.1 (code 18; handoff, unit, lint green; pus
  ee16860 ckpt 334: gated v0.7.1 (code 18) and pushed it: the handoff self-test, the full unit sui
```
