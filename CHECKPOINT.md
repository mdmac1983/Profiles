# CHECKPOINT 246 — read me first, then TASKS.md

**Written:** 2026-11-04T18:34:03Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `3046a4b` (this checkpoint is the commit after it)

## Just done
Run 52 (o,t) read: 19 PASS / 1 FAIL (emulator has no Bluetooth tile: test fixed); O4 pre-clean at lock WORKS (11 apps force-stopped in 164 ms before any wake), erase ladder/charger/peek5/tile guard PASS; found a race (Solitaire started with the launcher lost to it) -> Router now starts the target AFTER the home. Run 53 (second user): switch ends on the other user's own LOCK SCREEN = not seamless; user-0 Contacts process stayed alive. R1 accelerometer return (SHAKE/KNOCK ReturnMethods) coded; section p (demo mode vs pencil/gear/power menu)

## Do this next
dispatch sections o,t,p; read s (run 54) + o,t,p; write research/IDEAS_2026-11-04.md + research/ADB_TODO.md + update in-app guide; final answer

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M .github/workflows/emulator.yml
     M CHECKPOINT.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  d4a4b05 ckpt 245: Wrote R1-R3 (accelerometer back to custom, adb to-do list, adb for power menu/
  95fb3fc ckpt 244: Added experiment section 's' (pm suspend/hide effect on Recents/notifications/
  fbf54bc ckpt 243: Wrote Q1-Q4 (Tj's deep-research request: separation ideas, instant power menu,
  2bba67e ckpt 242: Added research section 'u' (second Android user experiment: switch time, first
  656ee09 ckpt 241: Wrote P1 (separate profiles keeping their own apps open: decide, only build if
  a6fdf8c ckpt 240: O1-O5 coded: 10 more lock-screen methods (UnlockMethods + MotionDetector, serv
  d6e9381 ckpt 239: New session: wrote Tj's O1-O5 (more lock-screen methods + dropdown multi-selec
  3a268c7 ckpt 238: N9 tick actually applied in TASKS.md (the first attempt failed on a regex; ckp
  1bf7d6a ckpt 237: N9 DONE: run 51 FULL emulator = green (0 FAIL) in 15m05s after the REC1 tapapp
  13b6eb6 ckpt 236: N9 proof: run 48 cold n6 = 3.6 min green; run 49 WARM n6 = 3.5 min green (cach
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
