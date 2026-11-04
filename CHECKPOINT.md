# CHECKPOINT 255 — read me first, then TASKS.md

**Written:** 2026-11-04T18:50:08Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `5bf6946` (this checkpoint is the commit after it)

## Just done
T1 built: device-shell helper lines (plain / sh -c / two-step) in ShellProtocol + 3 copy buttons in the setup screen; helper routines SHOTS + APKS and jobs MKDIR_PUBLISH/PUBLISH_REPORT publish everything to Downloads/launcher-probe; 'Collect EVERYTHING' button; research/ADB_PHONE.txt (phone/adb-OTG route + every command without the adb shell prefix); ADB_TODO links it; section x extended (device line via stdin, publish folder, >=10 screenshots, Settings apk)

## Do this next
dispatch sections x again (x run from 18:43 had the older code), read x+t at ~18:55, then full run + ship v0.5.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M app/src/test/java/com/tj/launcher/ShellProtocolTest.java

## Last ten checkpoints
```
  fe11070 ckpt 254: Wrote T1 (adb from a phone / adb OTG app) into TASKS
  5019f17 ckpt 253: ADB_TODO: FAST PATH on top (one session: tools/adb-all.sh / adb-all.bat, then 
  5a4b7aa ckpt 252: Run 55 read: o,t,p = 20 PASS 0 FAIL (Solitaire in front 1.5 s after unlock = r
  01ef1e0 ckpt 251: S1+S2 built: tools/adb-all.sh/.bat + shared probe list (app/src/main/resources
  51c2ccb ckpt 250: S1: tools/adb-all.sh (+ .bat for Windows, CRLF) + tools/probe-commands.txt (on
  3893c30 ckpt 249: Wrote S1-S3 (one-session adb: PC script + in-app Tablet setup & report via the
  3f46b2f ckpt 248: Run 54 (s) read: pm suspend works (Recents card removed, process stays, card N
  75dc67a ckpt 247: R2 done: research/ADB_TODO.md (every adb command in order: setup, permissions 
  5943b62 ckpt 246: Run 52 (o,t) read: 19 PASS / 1 FAIL (emulator has no Bluetooth tile: test fixe
  d4a4b05 ckpt 245: Wrote R1-R3 (accelerometer back to custom, adb to-do list, adb for power menu/
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
