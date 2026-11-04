# CHECKPOINT 236 — read me first, then TASKS.md

**Written:** 2026-11-04T08:55:36Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `b0dfcca` (this checkpoint is the commit after it)

## Just done
N9 proof: run 48 cold n6 = 3.6 min green; run 49 WARM n6 = 3.5 min green (cache hit, scenario 2m03) -> the AVD cache gave no measurable gain; run 50 FULL = 75 PASS / 1 FAIL: REC1 tapped drawer coordinates and hit Contacts instead of Clock (drawer order differs on a snapshot boot) = test fragility, not product. Fixes: tapapp finds icons by label (uiautomator), AVD cache/snapshot DROPPED (back to run-47 environment), docs updated

## Do this next
dispatch a FULL emulator run (empty sections) on the branch; expect 76 PASS / 0 FAIL; then tick N9 with the honest timings (n6 3.5-3.6 min; full ~15 min) and tell Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M CLAUDE.md
     M research/HANDOFF_2026-11-04.md

## Last ten checkpoints
```
  047609d ckpt 235: N9 proof run 48 (sections=n6, COLD cache): green in 3.6 min total (AVD create 
  b7966f1 ckpt 234: N9 built: emulator_scenario.sh sections (want X), emulator.yml sections input 
  9f71e4f ckpt 233: RELEASED v0.4.0 (Build APK run 41798376459 green, Release published + signed, 
  2e7509b ckpt 232: v0.4.0 (code 14) gated + pushed; android.yml full_build=true dispatched on mai
  2f54373 ckpt 231: gated v0.4.0 (code 14) and pushed it: the handoff self-test, the full unit sui
  bee2b86 ckpt 230: Run 47: 76 PASS / 0 FAIL (INVISIBLE force-stop 3/3 in 166 ms, no cover, Recent
  9521144 ckpt 229: Wrote N9 (faster emulator workflow, after v0.4.0) into TASKS from Tj's yes
  f6e7aae ckpt 228: Run 46 read: 74 PASS, INVISIBLE WORKS on the emulator ('force-stopped 3 of 3 a
  e1834c0 ckpt 227: Run 45 read: emulator survived (run 44 death was a flake); only 3 FAIL, all N6
  c762a6d ckpt 226: Run 44 read: tap-erase/prearm/plain-unlock/icon PASS; EMULATOR DIED (qemu gone
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
