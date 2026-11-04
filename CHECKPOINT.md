# CHECKPOINT 240 — read me first, then TASKS.md

**Written:** 2026-11-04T18:24:41Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `3864aad` (this checkpoint is the commit after it)

## Just done
O1-O5 coded: 10 more lock-screen methods (UnlockMethods + MotionDetector, service wiring: hw keys/charger/rotation/sensor), drop-down multi-select with min-one in Control, big hidden-app switches, lock-time prepare (preCleanAtLock: close Quickstep apps+notifications at lock via the helper, flip to custom behind the keyguard, prearm at screen-off), tiles remember Quickstep arrangement + tile guard; unit tests + lint green; scenario sections o + t added

## Do this next
dispatch emulator.yml sections 'o,t' (~5 min); read via artifact; fix; then full run + ship v0.5.0 (versionCode 15)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  d6e9381 ckpt 239: New session: wrote Tj's O1-O5 (more lock-screen methods + dropdown multi-selec
  3a268c7 ckpt 238: N9 tick actually applied in TASKS.md (the first attempt failed on a regex; ckp
  1bf7d6a ckpt 237: N9 DONE: run 51 FULL emulator = green (0 FAIL) in 15m05s after the REC1 tapapp
  13b6eb6 ckpt 236: N9 proof: run 48 cold n6 = 3.6 min green; run 49 WARM n6 = 3.5 min green (cach
  047609d ckpt 235: N9 proof run 48 (sections=n6, COLD cache): green in 3.6 min total (AVD create 
  b7966f1 ckpt 234: N9 built: emulator_scenario.sh sections (want X), emulator.yml sections input 
  9f71e4f ckpt 233: RELEASED v0.4.0 (Build APK run 41798376459 green, Release published + signed, 
  2e7509b ckpt 232: v0.4.0 (code 14) gated + pushed; android.yml full_build=true dispatched on mai
  2f54373 ckpt 231: gated v0.4.0 (code 14) and pushed it: the handoff self-test, the full unit sui
  bee2b86 ckpt 230: Run 47: 76 PASS / 0 FAIL (INVISIBLE force-stop 3/3 in 166 ms, no cover, Recent
```

(9 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
