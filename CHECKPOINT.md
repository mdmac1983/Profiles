# CHECKPOINT 227 — read me first, then TASKS.md

**Written:** 2026-11-04T07:39:57Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `41c4490` (this checkpoint is the commit after it)

## Just done
Run 45 read: emulator survived (run 44 death was a flake); only 3 FAIL, all N6: helper check opened a TCP socket on the MAIN thread -> NetworkOnMainThreadException (null message) so it always looked unreachable. Fixed: ShellHelper.reachable() pings on a short-lived thread (used by ReturnReset and Control); better diagnostics; scenario keeps the helper's own log

## Do this next
run 46 is dispatched: read artifact; expect 0 FAIL (N6 INVISIBLE, no cover, Recents cards gone); then tick N1-N8 + ship v0.4.0 (ship.sh, android.yml full_build, record-release)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  c762a6d ckpt 226: Run 44 read: tap-erase/prearm/plain-unlock/icon PASS; EMULATOR DIED (qemu gone
  74302d6 ckpt 225: Run 43 read (artifact): restart check now PASS; tap-erase, prearm, plain-unloc
  6d001dc ckpt 224: Run 42 read from the artifact: PASS tap-erase handshake, prearm behind keyguar
  6c619c5 ckpt 223: N6 shell helper (ShellProtocol/ShellServer/ShellHelper, invisible path in Retu
  23ee234 ckpt 222: Run 41: 5 FAIL all in scenario section N (not product): tap-erase was off by d
  d1bebea ckpt 221: Wrote N6/N7/N8 (invisible adb clean-up, restart survival, real-profile plausib
  a719c05 ckpt 220: Lint + debug build green locally; PROFILES doc has N4/N5 ladder; emulator run 
  d563818 ckpt 219: Dispatched emulator.yml on claude/admiring-gates-1ut5m3 (run not yet read)
  39eb540 ckpt 218: N1-N5 coded: buttons removed, UnlockMethods x6, gesture removed, ProfileSessio
  1b534f7 ckpt 217: Built N1 (buttons removed), N2 (UnlockMethods x6 + tests), N3a (gesture remove
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
