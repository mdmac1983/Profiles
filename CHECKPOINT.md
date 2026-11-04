# CHECKPOINT 208 — read me first, then TASKS.md

**Written:** 2026-11-04T04:47:43Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `bb5de15` (this checkpoint is the commit after it)

## Just done
pre-ship: v0.3.7: choose the launcher at unlock (Delete twice before your PIN = Quickstep, plain PIN = custom; opt-in), return reset no longer leaves Recents open when there is no app to open, helper no longer asks for hardware-key access

## Do this next
ship.sh gates and releases this

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  TASKS.md

## Last ten checkpoints
```
  494614c ckpt 207: Run 39 (1eca5f1): 59 PASS / 1 FAIL. E1 WORKS on the emulator: the helper sees 
  feb3927 ckpt 206: Answered E2 (does the PIN matter): no, any numeric PIN works
  1eca5f1 ckpt 205: E1 coded (v0.3.7/code 13): UnlockPick token (Delete x2 on the empty PIN pad = 
  1bab462 ckpt 204: Recorded E1 (choose the launcher at unlock: two PINs / extra digit / gesture t
  4b3344b ckpt 203: RELEASED v0.3.6 (Build APK green, Release published, signed, 9.19 MB; emulator
  4e15095 ckpt 202: v0.3.6 gated (code 12) and pushed (046cb76 on main); android.yml full_build di
  046cb76 ckpt 201: gated v0.3.6 (code 12) and pushed it: the handoff self-test, the full unit sui
  5c6f4b7 ckpt 200: pre-ship: v0.3.6: new settings UI with real switches, separate Recents per lau
  4faa6e4 ckpt 199: Run 37 (09352d0): 54 PASS / 1 FAIL = D3 broadcast check. Android 10 source con
  09352d0 ckpt 198: D2 UI rewrite done (ControlActivity: sections + Switch rows + status card + ra
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
