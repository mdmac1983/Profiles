# CHECKPOINT 149 — read me first, then TASKS.md

**Written:** 2026-11-03T04:56:14Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `7fe6321` (this checkpoint is the commit after it)

## Just done
Run 27 (54a3b53): emulator dies in reset2 again (same spot as run 26, so reproducible, not a one-off); logcat stream had died at 60s because of my adb timeout wrapper. Now: real adb binary for the stream, heartbeat with mem/qemu/oom info and +3/+7/+14s beats inside reset2.

## Do this next
Dispatch emulator.yml run 28; if the emulator dies again read heartbeat.txt (mem/qemu/oom) + logcat-stream.txt end; hypotheses: OOM on runner, or Recents Clear all now succeeding kills the emulator. Ship v0.3.2 only when green.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  54a3b53 ckpt 148: Run 26 (382895b): emulator device vanished after reset2 (adb: device not found
  382895b ckpt 147: Run 25: V4 PASS, W5 3/3 PASS, W2 PASS; reset2 Recents still TIMEOUT. Suspect: 
  bf3405d ckpt 146: Emulator scenario: X1 lock-screen number checks (PIN set/cleared)
  33b0b04 ckpt 145: X1 built: LockNumber overlay (a11y, NOT_TOUCHABLE) shows 71125011907547 at mea
  9ff7be8 ckpt 144: Recorded X1 (lock-screen number) with Tj's explicit L10 relaxation; photo save
  7844fd6 ckpt 143: Scenario V4 check now tests the exclude flag instead of grepping the raw dump
  82896a4 ckpt 142: Emulator run 1 (6686f1b): W5 3/3 PASS, W2 flag PASS; reset2 Recents Clear all 
  03ac2f6 ckpt 141: X2 branches audited: closed 5 unmergeable dependabot PRs, resume.sh ignores de
  ac9b2c1 ckpt 140: Bumped to 0.3.2/code 8, FIRST_INSTALL notes
  6686f1b ckpt 139: Emulator scenario: W5 stray-home redirect + quickstep-mode negative, W2 runtim
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
