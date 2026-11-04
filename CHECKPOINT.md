# CHECKPOINT 235 — read me first, then TASKS.md

**Written:** 2026-11-04T08:38:17Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `b7966f1` (this checkpoint is the commit after it)

## Just done
N9 proof run 48 (sections=n6, COLD cache): green in 3.6 min total (AVD create 53 s + cache save 7 s + scenario 71 s), ran only N6 checks, 0 FAIL (Recents 22->0). Dispatched run (sections=n6, WARM cache) and a FULL run (empty) side by side

## Do this next
read both: warm n6 time (expect ~2.5 min) and the full run result (expect 0 FAIL, ~15 min or less with snapshot boot); then tick N9 in TASKS with timings and tell Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  b7966f1 ckpt 234: N9 built: emulator_scenario.sh sections (want X), emulator.yml sections input 
  9f71e4f ckpt 233: RELEASED v0.4.0 (Build APK run 41798376459 green, Release published + signed, 
  2e7509b ckpt 232: v0.4.0 (code 14) gated + pushed; android.yml full_build=true dispatched on mai
  2f54373 ckpt 231: gated v0.4.0 (code 14) and pushed it: the handoff self-test, the full unit sui
  bee2b86 ckpt 230: Run 47: 76 PASS / 0 FAIL (INVISIBLE force-stop 3/3 in 166 ms, no cover, Recent
  9521144 ckpt 229: Wrote N9 (faster emulator workflow, after v0.4.0) into TASKS from Tj's yes
  f6e7aae ckpt 228: Run 46 read: 74 PASS, INVISIBLE WORKS on the emulator ('force-stopped 3 of 3 a
  e1834c0 ckpt 227: Run 45 read: emulator survived (run 44 death was a flake); only 3 FAIL, all N6
  c762a6d ckpt 226: Run 44 read: tap-erase/prearm/plain-unlock/icon PASS; EMULATOR DIED (qemu gone
  74302d6 ckpt 225: Run 43 read (artifact): restart check now PASS; tap-erase, prearm, plain-unloc
```
