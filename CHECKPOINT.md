# CHECKPOINT 138 — read me first, then TASKS.md

**Written:** 2026-11-03T03:57:18Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `c70e0ef` (this checkpoint is the commit after it)

## Just done
Built W1-W5 in code, unit tests+debug build green: StrayHomeGuard (a11y presses Home when stock home appears in custom mode), RecentsGuard (runtime setExcludeFromRecents + TaskDescription hedge), warm/early-key power card, faster reset (bg kill/radios, 20ms poll). Release a11y xml now has key-filter cap (debug override removed).

## Do this next
Add emulator_scenario.sh checks (stray stock home redirected; our tasks carry EXCLUDE flag; power card warm), run emulator.yml, then bump versionCode 8/0.3.2, FIRST_INSTALL notes, TASKS ticks, ship.sh

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  90c18f8 ckpt 137: Wrote Tj's v0.3.1 feedback W1-W5 into TASKS.md
  2d4e527 ckpt 136: HANDOFF: TASKS.md STATUS SNAPSHOT (stale boxes closed, 6 real open items), res
  b9faf25 ckpt 133: Released v0.3.1 (V1-V5). Emulator 26/27 before release; the 27th (launcher not
  b8682ed ckpt 131: gated v0.3.1 (code 7) and pushed it: the handoff self-test, the full unit suit
  9892812 ckpt 130: pre-ship: v0.3.1: more responsive twist, keyboard switch (adb grant), faster r
  bb1a50c ckpt 129: Fixes: double-Home window 0.9 s, router sets EXCLUDE_FROM_RECENTS on our Launc
  09e27d4 ckpt 128: V1-V5 built: gesture 18 deg/looser pinch/min 80px; keyboard switch (KeyboardPo
  0bdbd14 ckpt 122: Wrote Tj's v0.3.0 feedback V1-V5.
  81dda79 ckpt 120: Released v0.3.0. Emulator 25/26 before release (only the Recents check, whose 
  10c8d10 ckpt 118: gated v0.3.0 (code 6) and pushed it: the handoff self-test, the full unit suit
```

(9 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
