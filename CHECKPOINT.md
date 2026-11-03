# CHECKPOINT 145 — read me first, then TASKS.md

**Written:** 2026-11-03T04:20:43Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `667d9fd` (this checkpoint is the commit after it)

## Just done
X1 built: LockNumber overlay (a11y, NOT_TOUCHABLE) shows 71125011907547 at measured size/position, hidden when PIN pad visible/unlocked/screen off; clean inpainted wallpaper in res/drawable-nodpi + Control button; restore turns it off. Unit tests + debug build green.

## Do this next
Read emulator run 25 results (results.txt) and fix FAILs; add emulator lock-screen check (locksettings set-pin 1234, power off/on, screenshot, swipe up, overlay window TjLockNumber gone, ALWAYS clear the pin); then ship v0.3.2

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  9ff7be8 ckpt 144: Recorded X1 (lock-screen number) with Tj's explicit L10 relaxation; photo save
  7844fd6 ckpt 143: Scenario V4 check now tests the exclude flag instead of grepping the raw dump
  82896a4 ckpt 142: Emulator run 1 (6686f1b): W5 3/3 PASS, W2 flag PASS; reset2 Recents Clear all 
  03ac2f6 ckpt 141: X2 branches audited: closed 5 unmergeable dependabot PRs, resume.sh ignores de
  ac9b2c1 ckpt 140: Bumped to 0.3.2/code 8, FIRST_INSTALL notes
  6686f1b ckpt 139: Emulator scenario: W5 stray-home redirect + quickstep-mode negative, W2 runtim
  5d9b02a ckpt 138: Built W1-W5 in code, unit tests+debug build green: StrayHomeGuard (a11y presse
  90c18f8 ckpt 137: Wrote Tj's v0.3.1 feedback W1-W5 into TASKS.md
  2d4e527 ckpt 136: HANDOFF: TASKS.md STATUS SNAPSHOT (stale boxes closed, 6 real open items), res
  b9faf25 ckpt 133: Released v0.3.1 (V1-V5). Emulator 26/27 before release; the 27th (launcher not
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
