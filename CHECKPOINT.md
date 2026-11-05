# CHECKPOINT 291 — read me first, then TASKS.md

**Written:** 2026-11-05T02:04:10Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-8b1c3755-1tp147` · **builds on:** `5c3e323` (this checkpoint is the commit after it)

## Just done
AB2 efficiency (code, compiled, not yet unit-tested): EventNoise (drops other apps' content-change events, narrows power-menu scan to system windows except right after a window change), probeWanted/keyguardLockedFast caches, SharedPreferences handle caches, ReturnReset.targetPackage cached 10 min + persisted + async refresh, single-package openApp/startTargetBehindLock/findAppLabel, findPencil skips app windows; EventNoiseTest written

## Do this next
run the whole unit suite (bash tools/gradle.sh :app:testDebugUnitTest), fix failures; then AB1 SAFETY doc, AB3 notification-aware sparing + pf section, re-run emulator sections, AB4, AB5, ship 0.7.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M app/src/main/java/com/tj/launcher/EventNoise.java
     M app/src/main/java/com/tj/launcher/TjAccessibilityService.java
    ?? app/src/test/java/com/tj/launcher/EventNoiseTest.java

## Last ten checkpoints
```
  2fc12ca ckpt 290: Tj's new message recorded as AB1-AB5: safety audit (no factory reset, ever), e
  da09334 ckpt 289: AA1 (ImmersiveProfile: policy_control immersive.status=* per mode, merge/resto
  11306b2 ckpt 288: AA2 built: KeepAliveService (foreground, min-importance silent notification) +
  cbf34e0 ckpt 287: Added emulator research section rs (info lines only): R1 policy_control immers
  b62dbca ckpt 286: Tj: build #2 (policy_control immersive), #3 (keep-alive service), #4 (helper t
  a0d9170 ckpt 285: Wrote research/REEXAMINE_2026-11-05.md re-examining every goal with fresh idea
  e403aa7 ckpt 284: Resumed on Opus; read IDEAS/PROFILES/NOTES research. Tj asked to re-examine ev
  f12b6ca ckpt 283: v0.6.0 RELEASED and recorded (android.yml run 18 green; the Release has the La
  246f6c6 ckpt 282: v0.6.0 (code 16) gated with ship.sh and pushed (main == branch == eb2e260). Se
  eb2e260 ckpt 281: gated v0.6.0 (code 16) and pushed it: the handoff self-test, the full unit sui
```

(6 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
