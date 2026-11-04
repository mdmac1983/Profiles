# CHECKPOINT 233 — read me first, then TASKS.md

**Written:** 2026-11-04T08:24:26Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `f6ba6a2` (this checkpoint is the commit after it)

## Just done
RELEASED v0.4.0 (Build APK run 41798376459 green, Release published + signed, 8.78 MB, code 14; emulator run 47 76 PASS/0 FAIL); recorded in BUILDLOG

## Do this next
N9: faster Emulator check (TASKS.md): section input on emulator.yml + emulator_scenario.sh, AVD cache, parallel jobs, trim sleeps; prove with a section-only run + a full run; update HANDOFF/CLAUDE.md

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  2e7509b ckpt 232: v0.4.0 (code 14) gated + pushed; android.yml full_build=true dispatched on mai
  2f54373 ckpt 231: gated v0.4.0 (code 14) and pushed it: the handoff self-test, the full unit sui
  bee2b86 ckpt 230: Run 47: 76 PASS / 0 FAIL (INVISIBLE force-stop 3/3 in 166 ms, no cover, Recent
  9521144 ckpt 229: Wrote N9 (faster emulator workflow, after v0.4.0) into TASKS from Tj's yes
  f6e7aae ckpt 228: Run 46 read: 74 PASS, INVISIBLE WORKS on the emulator ('force-stopped 3 of 3 a
  e1834c0 ckpt 227: Run 45 read: emulator survived (run 44 death was a flake); only 3 FAIL, all N6
  c762a6d ckpt 226: Run 44 read: tap-erase/prearm/plain-unlock/icon PASS; EMULATOR DIED (qemu gone
  74302d6 ckpt 225: Run 43 read (artifact): restart check now PASS; tap-erase, prearm, plain-unloc
  6d001dc ckpt 224: Run 42 read from the artifact: PASS tap-erase handshake, prearm behind keyguar
  6c619c5 ckpt 223: N6 shell helper (ShellProtocol/ShellServer/ShellHelper, invisible path in Retu
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
