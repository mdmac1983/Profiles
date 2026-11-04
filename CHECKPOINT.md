# CHECKPOINT 250 — read me first, then TASKS.md

**Written:** 2026-11-04T18:39:43Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `11aa71e` (this checkpoint is the commit after it)

## Just done
S1: tools/adb-all.sh (+ .bat for Windows, CRLF) + tools/probe-commands.txt (one shared read-only probe list): permissions, appops, whitelist, a11y merge, helper, optional browser, status table, probe, pull Settings/SystemUI/Launcher3/framework APKs, screenshots+UI dumps of stock Settings pages

## Do this next
S2: ShellProtocol RUN allow-list + ShellServer 'RUN id' + SetupActivity + TabletReport (+ autorun extra); then section x; read run 55; full run; ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    ?? tools/adb-all.bat

## Last ten checkpoints
```
  3893c30 ckpt 249: Wrote S1-S3 (one-session adb: PC script + in-app Tablet setup & report via the
  3f46b2f ckpt 248: Run 54 (s) read: pm suspend works (Recents card removed, process stays, card N
  75dc67a ckpt 247: R2 done: research/ADB_TODO.md (every adb command in order: setup, permissions 
  5943b62 ckpt 246: Run 52 (o,t) read: 19 PASS / 1 FAIL (emulator has no Bluetooth tile: test fixe
  d4a4b05 ckpt 245: Wrote R1-R3 (accelerometer back to custom, adb to-do list, adb for power menu/
  95fb3fc ckpt 244: Added experiment section 's' (pm suspend/hide effect on Recents/notifications/
  fbf54bc ckpt 243: Wrote Q1-Q4 (Tj's deep-research request: separation ideas, instant power menu,
  2bba67e ckpt 242: Added research section 'u' (second Android user experiment: switch time, first
  656ee09 ckpt 241: Wrote P1 (separate profiles keeping their own apps open: decide, only build if
  a6fdf8c ckpt 240: O1-O5 coded: 10 more lock-screen methods (UnlockMethods + MotionDetector, serv
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
