# CHECKPOINT 371 — read me first, then TASKS.md

**Written:** 2026-11-05T20:43:36Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-9ab65fa5-vp812y` · **builds on:** `3deb910` (this checkpoint is the commit after it)

## Just done
Tj asked to release the next version + will an update need adb (answer: no); REL072 scope written; checkpoint-skill recap due

## Do this next
build v0.7.2 now (REL072 in TASKS): KA1 first (PNG icon drawable-mdpi/hdpi/xhdpi/xxhdpi + delete drawable/tj_ic_blank.xml, KeepAlivePolicy default OFF + blocked flag, CrashGuard fuse via a pure decide() + JVM tests, emulator ka section explicit ON + default-OFF check), then CBR1, UX1 wording; gate A+B; code-review; ship.sh; android.yml full_build; record-release

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  fa8132b ckpt 370: Tj: cable later; asked if he can use Launcher control now: yes, with cautions 
  c684404 ckpt 369: step 22: all permissions verified after restart (CBL1 closed); answer to 'ever
  8181b49 ckpt 368: step 21: lock number shows on the real lock screen (LK1 number+gesture OK); st
  1b94375 ckpt 367: step 20: Tj turned the lock number ON; step 21 (lock + look for the number) se
  0df7428 ckpt 366: step 19: double-erase handshake works on the real tablet (pad recognised), unl
  217aa31 ckpt 365: Tj: custom home live (default home ours, 0 crashes) but lock number missing + 
  b2464af ckpt 364: step 17: notification access null = a Go device refuses it (my audit was wrong
  8d77e87 ckpt 363: Tj made the custom launcher the default Home himself: first live custom run; s
  5038b3d ckpt 362: Tj: wants the permissions set up now; audit: only notification access missing;
  cb3d79a ckpt 361: step 16: default Home is the STOCK launcher (crash cleared ours) but mode flag
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
