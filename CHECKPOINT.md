# CHECKPOINT 207 — read me first, then TASKS.md

**Written:** 2026-11-04T04:25:48Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `fcad449` (this checkpoint is the commit after it)

## Just done
Run 39 (1eca5f1): 59 PASS / 1 FAIL. E1 WORKS on the emulator: the helper sees the PIN-pad key clicks by id (log: UNLOCKPICK enter/quickstep/custom, no digits), Delete x2 -> Quickstep, plain PIN -> custom. The 1 FAIL exposed a real bug: with no Solitaire installed the return reset left RecentsActivity on screen. Fixed: ReturnReset.openApp presses Home (svc.goHome) when no app matches.

## Do this next
Dispatch emulator.yml (final v0.3.7 code), expect 0 FAIL; ship v0.3.7 (ship.sh, android.yml, record-release before any bump); reply to Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  app/src/main/java/com/tj/launcher/ReturnReset.java
    M  app/src/main/java/com/tj/launcher/TjAccessibilityService.java

## Last ten checkpoints
```
  feb3927 ckpt 206: Answered E2 (does the PIN matter): no, any numeric PIN works
  1eca5f1 ckpt 205: E1 coded (v0.3.7/code 13): UnlockPick token (Delete x2 on the empty PIN pad = 
  1bab462 ckpt 204: Recorded E1 (choose the launcher at unlock: two PINs / extra digit / gesture t
  4b3344b ckpt 203: RELEASED v0.3.6 (Build APK green, Release published, signed, 9.19 MB; emulator
  4e15095 ckpt 202: v0.3.6 gated (code 12) and pushed (046cb76 on main); android.yml full_build di
  046cb76 ckpt 201: gated v0.3.6 (code 12) and pushed it: the handoff self-test, the full unit sui
  5c6f4b7 ckpt 200: pre-ship: v0.3.6: new settings UI with real switches, separate Recents per lau
  4faa6e4 ckpt 199: Run 37 (09352d0): 54 PASS / 1 FAIL = D3 broadcast check. Android 10 source con
  09352d0 ckpt 198: D2 UI rewrite done (ControlActivity: sections + Switch rows + status card + ra
  dfd53e6 ckpt 197: D3 coded (CLOSE_SYSTEM_DIALOGS globalactions receiver -> earlyCover; POWERLAT 
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
