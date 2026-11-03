# CHECKPOINT 147 — read me first, then TASKS.md

**Written:** 2026-11-03T04:22:43Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `cdbcc7d` (this checkpoint is the commit after it)

## Just done
Run 25: V4 PASS, W5 3/3 PASS, W2 PASS; reset2 Recents still TIMEOUT. Suspect: stray-home guard pressed Home during the reset (emulator Recents host may be the stock Launcher class). Guard now pauses 10s during runReset and 2.5s after any Mode change; scenario logs STRAY/LOCKNUM in reset2-log.

## Do this next
Dispatch emulator.yml (run 26) on claude/install-hooks-script-chvlig; read results: expect reset2 PASS and X1 3 PASS. If reset2 still fails, read reset2-log.txt (STRAY lines?) and tjreset log; else ship v0.3.2 (ship.sh then android.yml full_build=true)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  bf3405d ckpt 146: Emulator scenario: X1 lock-screen number checks (PIN set/cleared)
  33b0b04 ckpt 145: X1 built: LockNumber overlay (a11y, NOT_TOUCHABLE) shows 71125011907547 at mea
  9ff7be8 ckpt 144: Recorded X1 (lock-screen number) with Tj's explicit L10 relaxation; photo save
  7844fd6 ckpt 143: Scenario V4 check now tests the exclude flag instead of grepping the raw dump
  82896a4 ckpt 142: Emulator run 1 (6686f1b): W5 3/3 PASS, W2 flag PASS; reset2 Recents Clear all 
  03ac2f6 ckpt 141: X2 branches audited: closed 5 unmergeable dependabot PRs, resume.sh ignores de
  ac9b2c1 ckpt 140: Bumped to 0.3.2/code 8, FIRST_INSTALL notes
  6686f1b ckpt 139: Emulator scenario: W5 stray-home redirect + quickstep-mode negative, W2 runtim
  5d9b02a ckpt 138: Built W1-W5 in code, unit tests+debug build green: StrayHomeGuard (a11y presse
  90c18f8 ckpt 137: Wrote Tj's v0.3.1 feedback W1-W5 into TASKS.md
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
