# CHECKPOINT 228 — read me first, then TASKS.md

**Written:** 2026-11-04T07:58:29Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `4adba70` (this checkpoint is the commit after it)

## Just done
Run 46 read: 74 PASS, INVISIBLE WORKS on the emulator ('force-stopped 3 of 3 apps in 66 ms; no cover'), helper reachable over TCP, server log shows unix+tcp up. 2 FAIL: (a) Recents cards stay after force-stop on Android 10 -> helper now also removes the packages' Recents tasks (dumpsys activity recents parse + IActivityTaskManager.removeTask), JVM-tested parser; (b) Restart check: stray-Quickstep guard pressed Home while the power menu was open (stock home surfaced after N6 force-stops) -> LAST STEP writes guard_stock_home=false

## Do this next
run 47 dispatched: expect 0 FAIL; read artifact (scratchpad/art47); then tick N1-N8, ship v0.4.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  e1834c0 ckpt 227: Run 45 read: emulator survived (run 44 death was a flake); only 3 FAIL, all N6
  c762a6d ckpt 226: Run 44 read: tap-erase/prearm/plain-unlock/icon PASS; EMULATOR DIED (qemu gone
  74302d6 ckpt 225: Run 43 read (artifact): restart check now PASS; tap-erase, prearm, plain-unloc
  6d001dc ckpt 224: Run 42 read from the artifact: PASS tap-erase handshake, prearm behind keyguar
  6c619c5 ckpt 223: N6 shell helper (ShellProtocol/ShellServer/ShellHelper, invisible path in Retu
  23ee234 ckpt 222: Run 41: 5 FAIL all in scenario section N (not product): tap-erase was off by d
  d1bebea ckpt 221: Wrote N6/N7/N8 (invisible adb clean-up, restart survival, real-profile plausib
  a719c05 ckpt 220: Lint + debug build green locally; PROFILES doc has N4/N5 ladder; emulator run 
  d563818 ckpt 219: Dispatched emulator.yml on claude/admiring-gates-1ut5m3 (run not yet read)
  39eb540 ckpt 218: N1-N5 coded: buttons removed, UnlockMethods x6, gesture removed, ProfileSessio
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
