# CHECKPOINT 256 — read me first, then TASKS.md

**Written:** 2026-11-04T18:51:07Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `a1f089e` (this checkpoint is the commit after it)

## Just done
Run 57 (t): all PASS incl. Q3 (demo flag follows the mode, no pencil in custom, lock screen + plain PIN fine with the flag on). Run 56 (x, old code): in-app route PASS (Do everything via helper, report 59 KB, Settings/SystemUI/Launcher3/framework APKs copied); adb-all.sh had 2 bugs: adb shell inside the read loop ate stdin (probe stopped at 8 lines) and ps -A shows names not args (helper looked absent) -> fixed (</dev/null, ps -A -o ARGS | grep '[S]hellServer') in script, .bat and probe list

## Do this next
dispatch sections x (new code: device line, publish folder, shots, apks); then full run + ship v0.5.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  e58575b ckpt 255: T1 built: device-shell helper lines (plain / sh -c / two-step) in ShellProtoco
  fe11070 ckpt 254: Wrote T1 (adb from a phone / adb OTG app) into TASKS
  5019f17 ckpt 253: ADB_TODO: FAST PATH on top (one session: tools/adb-all.sh / adb-all.bat, then 
  5a4b7aa ckpt 252: Run 55 read: o,t,p = 20 PASS 0 FAIL (Solitaire in front 1.5 s after unlock = r
  01ef1e0 ckpt 251: S1+S2 built: tools/adb-all.sh/.bat + shared probe list (app/src/main/resources
  51c2ccb ckpt 250: S1: tools/adb-all.sh (+ .bat for Windows, CRLF) + tools/probe-commands.txt (on
  3893c30 ckpt 249: Wrote S1-S3 (one-session adb: PC script + in-app Tablet setup & report via the
  3f46b2f ckpt 248: Run 54 (s) read: pm suspend works (Recents card removed, process stays, card N
  75dc67a ckpt 247: R2 done: research/ADB_TODO.md (every adb command in order: setup, permissions 
  5943b62 ckpt 246: Run 52 (o,t) read: 19 PASS / 1 FAIL (emulator has no Bluetooth tile: test fixe
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
