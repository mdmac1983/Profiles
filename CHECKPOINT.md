# CHECKPOINT 345 — read me first, then TASKS.md

**Written:** 2026-11-05T18:39:07Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `a6409d9` (this checkpoint is the commit after it)

## Just done
Tj's step 3 result (1:37): grants, app-ops, battery whitelist and speed compile all done by hand on the tablet (INTERNET granted=true, accessibility helper already on, compile Success); rule added to CLAUDE.md: one short step per message, commands in the LAST message of the turn

## Do this next
step 4 sent: logcat -d -b crash | tail -40 (the first-open crash, CR1); WAIT for the screenshot; then step 5: on the tablet tap '2. Collect the tablet report' (app side only), then on the phone ls -l /sdcard/Android/data/com.tj.launcher/files/probe/ and getenforce (MBX1 evidence); MBX1 as v0.7.2 afterwards

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M CLAUDE.md
     M TASKS.md

## Last ten checkpoints
```
  cbfb881 ckpt 344: Tj's step 3 screenshot (1:33): with a LIVE helper the launcher's log says unix
  0b4b40c ckpt 343: Tj: the tablet says Shell helper NOT running right after a fresh restart (help
  97cb41f ckpt 342: Tj's step 1 screenshot (1:31 phone clock): helper restarted OK ([1] 12440, uni
  000d69b ckpt 341: Tj says the launcher crashed when first opened on the tablet and now seems to 
  c87f389 ckpt 340: Tj's 3rd screenshots: the helper starts fine (unix up for uid 10243, tcp 47631
  bf2b0c8 ckpt 339: Tj's 2nd screenshot (1:02): helper start attempt 1 left no process, attempt 2 
  336c9e7 ckpt 338: Tj is in the CABLE SESSION (screenshot 12:59 phone clock): shell uid 2000 OK, 
  63a17ba ckpt 337: v0.7.1 RELEASED (android.yml run 20 green, signed): https://github.com/tjshea9
  e06b833 ckpt 336: v0.7.0 experiment (run 129) = identical legacy-alone failure on 2a547f6: the n
  4686fda ckpt 335: ship.sh gates passed again for v0.7.1 (code 18; handoff, unit, lint green; pus
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
