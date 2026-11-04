# CHECKPOINT 226 — read me first, then TASKS.md

**Written:** 2026-11-04T07:20:51Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `d93b7d5` (this checkpoint is the commit after it)

## Just done
Run 44 read: tap-erase/prearm/plain-unlock/icon PASS; EMULATOR DIED (qemu gone, no OOM) at the N6 double-Home, so N6 'after 0'/'no cover' were bogus empty reads. Made ShellServer force-stop via IActivityManager binder, sequential, am fallback; scenario: key checks informational (console keys never reach a11y filter), alive() guard for N6

## Do this next
dispatch emulator.yml run 45; read artifact; if emulator dies again at N6 treat as product-caused: investigate (force-stop of which pkgs?), else if N6 INVISIBLE passes -> ticks N1-N8 + ship v0.4.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  74302d6 ckpt 225: Run 43 read (artifact): restart check now PASS; tap-erase, prearm, plain-unloc
  6d001dc ckpt 224: Run 42 read from the artifact: PASS tap-erase handshake, prearm behind keyguar
  6c619c5 ckpt 223: N6 shell helper (ShellProtocol/ShellServer/ShellHelper, invisible path in Retu
  23ee234 ckpt 222: Run 41: 5 FAIL all in scenario section N (not product): tap-erase was off by d
  d1bebea ckpt 221: Wrote N6/N7/N8 (invisible adb clean-up, restart survival, real-profile plausib
  a719c05 ckpt 220: Lint + debug build green locally; PROFILES doc has N4/N5 ladder; emulator run 
  d563818 ckpt 219: Dispatched emulator.yml on claude/admiring-gates-1ut5m3 (run not yet read)
  39eb540 ckpt 218: N1-N5 coded: buttons removed, UnlockMethods x6, gesture removed, ProfileSessio
  1b534f7 ckpt 217: Built N1 (buttons removed), N2 (UnlockMethods x6 + tests), N3a (gesture remove
  d29d8c4 ckpt 216: Wrote N1-N3 (Tj's 2026-11-04 3-part request) into TASKS.md; read RouterActivit
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
