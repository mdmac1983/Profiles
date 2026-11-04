# CHECKPOINT 225 — read me first, then TASKS.md

**Written:** 2026-11-04T06:57:16Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `e39d6ba` (this checkpoint is the commit after it)

## Just done
Run 43 read (artifact): restart check now PASS; tap-erase, prearm, plain-unlock<=1.5s, icon PASS. N6 root cause: helper unreachable because the app had no INTERNET permission (socket EPERM) + SELinux blocks the abstract unix socket -> declared INTERNET (loopback only, disclosed to Tj in Control + manifest comment). Keys: console events accepted, key filter on, but handler not proven -> added KEY log + arrival check

## Do this next
dispatch emulator.yml run 44, read via artifact (curl signed URL into scratchpad/artNN): N6 INVISIBLE + 'KEY' arrival lines; if keys never arrive, make key tests informational (hardware path unprovable on emulator) and rely on KeySeq unit tests; then ticks + ship v0.4.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  6d001dc ckpt 224: Run 42 read from the artifact: PASS tap-erase handshake, prearm behind keyguar
  6c619c5 ckpt 223: N6 shell helper (ShellProtocol/ShellServer/ShellHelper, invisible path in Retu
  23ee234 ckpt 222: Run 41: 5 FAIL all in scenario section N (not product): tap-erase was off by d
  d1bebea ckpt 221: Wrote N6/N7/N8 (invisible adb clean-up, restart survival, real-profile plausib
  a719c05 ckpt 220: Lint + debug build green locally; PROFILES doc has N4/N5 ladder; emulator run 
  d563818 ckpt 219: Dispatched emulator.yml on claude/admiring-gates-1ut5m3 (run not yet read)
  39eb540 ckpt 218: N1-N5 coded: buttons removed, UnlockMethods x6, gesture removed, ProfileSessio
  1b534f7 ckpt 217: Built N1 (buttons removed), N2 (UnlockMethods x6 + tests), N3a (gesture remove
  d29d8c4 ckpt 216: Wrote N1-N3 (Tj's 2026-11-04 3-part request) into TASKS.md; read RouterActivit
  87e5f36 ckpt 215: Handoff written for a new session: research/HANDOFF_2026-11-04.md (v0.3.7 rele
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
