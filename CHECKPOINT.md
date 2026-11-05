# CHECKPOINT 363 — read me first, then TASKS.md

**Written:** 2026-11-05T20:21:21Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-9ab65fa5-vp812y` · **builds on:** `043c8d4` (this checkpoint is the commit after it)

## Just done
Tj made the custom launcher the default Home himself: first live custom run; step 18 (check default home, crashes, start time) sent

## Do this next
Tj sends step 18 screenshot: default Home = com.tj.launcher/.RouterActivity? crash count 0? Displayed time; ask what the tablet looks like; then step 17 notification access (allow_listener line) if not done; then step: proof of all grants; then cable-done; then KA1 etc; undo line if wrong: cmd package set-home-activity com.android.launcher3/.Launcher

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  5038b3d ckpt 362: Tj: wants the permissions set up now; audit: only notification access missing;
  cb3d79a ckpt 361: step 16: default Home is the STOCK launcher (crash cleared ours) but mode flag
  ddf42e9 ckpt 360: step 15: a11y service BOUND after restart = ACC1 solved (stale Binding from cr
  2035ff8 ckpt 359: step 14 done: Tj restarted the tablet (Back); step 15 sent (a11y Bound/Binding
  a8b34fd ckpt 358: step 13: no ServiceRecord = stale a11y Binding (ACC1 A); first-connect safety 
  5f0da9b ckpt 357: step 12: DuraSpeed back on (1,1); a11y service STUCK in Binding services; step
  354b658 ckpt 356: Tj: step 11 Done (no screenshot); step 12 sent (read-back + ACC1 grep); BATCH1
  f174df9 ckpt 355: step 10: helper dead at 14:45 with DuraSpeed off; step 11 (DuraSpeed back on) 
  b0580ee ckpt 354: new session resumed from handoff: v0.7.2 plan (KA1 fuse, MBX1 security finding
  92ecc01 ckpt 353: active-memory handoff #1 written and pushed: handoff-launcher.md (13 sections,
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
