# CHECKPOINT 251 — read me first, then TASKS.md

**Written:** 2026-11-04T18:43:38Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `9bf6b8f` (this checkpoint is the commit after it)

## Just done
S1+S2 built: tools/adb-all.sh/.bat + shared probe list (app/src/main/resources/probe-commands.txt, bundled in the APK), helper learns RUN <id> (fixed allow-list) / PROBE / A11YON, SetupActivity (status checklist, Do everything, Collect report + copy system APKs, autorun for the test), TabletReport (app-side facts), Control entry; unit tests+lint green; section x (adb-all.sh on the emulator + in-app autorun)

## Do this next
dispatch sections 'x' and read run 55 (o,t,p) results (should be done); then update ADB_TODO (fast path = adb-all), IDEAS doc pencil result, full run, ship v0.5.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M .github/workflows/emulator.yml
     M CHECKPOINT.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  51c2ccb ckpt 250: S1: tools/adb-all.sh (+ .bat for Windows, CRLF) + tools/probe-commands.txt (on
  3893c30 ckpt 249: Wrote S1-S3 (one-session adb: PC script + in-app Tablet setup & report via the
  3f46b2f ckpt 248: Run 54 (s) read: pm suspend works (Recents card removed, process stays, card N
  75dc67a ckpt 247: R2 done: research/ADB_TODO.md (every adb command in order: setup, permissions 
  5943b62 ckpt 246: Run 52 (o,t) read: 19 PASS / 1 FAIL (emulator has no Bluetooth tile: test fixe
  d4a4b05 ckpt 245: Wrote R1-R3 (accelerometer back to custom, adb to-do list, adb for power menu/
  95fb3fc ckpt 244: Added experiment section 's' (pm suspend/hide effect on Recents/notifications/
  fbf54bc ckpt 243: Wrote Q1-Q4 (Tj's deep-research request: separation ideas, instant power menu,
  2bba67e ckpt 242: Added research section 'u' (second Android user experiment: switch time, first
  656ee09 ckpt 241: Wrote P1 (separate profiles keeping their own apps open: decide, only build if
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
