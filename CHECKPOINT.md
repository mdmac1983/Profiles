# CHECKPOINT 222 — read me first, then TASKS.md

**Written:** 2026-11-04T06:12:26Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `d1bebea` (this checkpoint is the commit after it)

## Just done
Run 41: 5 FAIL all in scenario section N (not product): tap-erase was off by default in the test, adb input keyevent bypasses the a11y key filter. PASS: lock-screen prearm behind keyguard, no BAL block, plain PIN ends Quickstep, icon return. Fixed scenario (emulator-console key events, tap-erase enabled, key-filter log check)

## Do this next
implement N7 (boot ends Quickstep session), N6 (ShellServer/ShellHelper invisible clean-up), then re-run emulator.yml once for everything

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  d1bebea ckpt 221: Wrote N6/N7/N8 (invisible adb clean-up, restart survival, real-profile plausib
  a719c05 ckpt 220: Lint + debug build green locally; PROFILES doc has N4/N5 ladder; emulator run 
  d563818 ckpt 219: Dispatched emulator.yml on claude/admiring-gates-1ut5m3 (run not yet read)
  39eb540 ckpt 218: N1-N5 coded: buttons removed, UnlockMethods x6, gesture removed, ProfileSessio
  1b534f7 ckpt 217: Built N1 (buttons removed), N2 (UnlockMethods x6 + tests), N3a (gesture remove
  d29d8c4 ckpt 216: Wrote N1-N3 (Tj's 2026-11-04 3-part request) into TASKS.md; read RouterActivit
  87e5f36 ckpt 215: Handoff written for a new session: research/HANDOFF_2026-11-04.md (v0.3.7 rele
  47b476a ckpt 212: RELEASED v0.3.7 (Build APK green, Release published, signed, 9.19 MB; emulator
  fe43e6c ckpt 210: v0.3.7 gated (code 13) and pushed (3451263 on main); android.yml full_build di
  3451263 ckpt 209: gated v0.3.7 (code 13) and pushed it: the handoff self-test, the full unit sui
```
