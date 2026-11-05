# CHECKPOINT 357 — read me first, then TASKS.md

**Written:** 2026-11-05T20:01:51Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-9ab65fa5-vp812y` · **builds on:** `354b658` (this checkpoint is the commit after it)

## Just done
step 12: DuraSpeed back on (1,1); a11y service STUCK in Binding services; step 13 sent

## Do this next
Tj sends step 13 screenshot (dumpsys activity services com.tj.launcher grep): no ServiceRecord = stale bind -> ask Tj to restart the tablet then re-check Bound services; requested=true received=false = our main thread/vendor; then cable-done line; then KA1 PNG + default OFF + CrashGuard fuse + CBR1 + A11Y plain-words state in report; BATCH1 vs mailbox decision for Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  354b658 ckpt 356: Tj: step 11 Done (no screenshot); step 12 sent (read-back + ACC1 grep); BATCH1
  f174df9 ckpt 355: step 10: helper dead at 14:45 with DuraSpeed off; step 11 (DuraSpeed back on) 
  b0580ee ckpt 354: new session resumed from handoff: v0.7.2 plan (KA1 fuse, MBX1 security finding
  92ecc01 ckpt 353: active-memory handoff #1 written and pushed: handoff-launcher.md (13 sections,
  b807da4 ckpt 352: Tj's step 9 (2:18): DuraSpeed now OFF, helper restarted 14:17:57 (EST tablet c
  e252af8 ckpt 351: Tj's step 8 (2:13): the helper started 13:31 is DEAD at 14:13 (silent, tj.log 
  3f63046 ckpt 350: tablet intake 2026-11-05: Tj's first real report saved (3 red flags); the cras
  cedcd7c ckpt 349: Tj's step 7 (1:52): the shell CAN read the report the launcher wrote (owner u0
  ec20d98 ckpt 348: Tj's step 6 photo: the app-side report works without the helper (31 KB, in the
  a9cef92 ckpt 347: Tj's step 5 (1:47): SELinux Enforcing, build = MediaTek alps user/release-keys
```
