# CHECKPOINT 210 — read me first, then TASKS.md

**Written:** 2026-11-04T04:48:57Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `3451263` (this checkpoint is the commit after it)

## Just done
v0.3.7 gated (code 13) and pushed (3451263 on main); android.yml full_build dispatched. Emulator run 40 = 60 PASS / 0 FAIL.

## Do this next
Wait ~4 min: get_release_by_tag v0.3.7, then bash tools/record-release.sh v0.3.7 (build.gradle is still 0.3.7); send Tj the link + how to use the unlock choice (turn helper off/on once, Launcher control > Lock screen > Choose the launcher when I unlock)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  3451263 ckpt 209: gated v0.3.7 (code 13) and pushed it: the handoff self-test, the full unit sui
  3ecc89a ckpt 208: pre-ship: v0.3.7: choose the launcher at unlock (Delete twice before your PIN 
  494614c ckpt 207: Run 39 (1eca5f1): 59 PASS / 1 FAIL. E1 WORKS on the emulator: the helper sees 
  feb3927 ckpt 206: Answered E2 (does the PIN matter): no, any numeric PIN works
  1eca5f1 ckpt 205: E1 coded (v0.3.7/code 13): UnlockPick token (Delete x2 on the empty PIN pad = 
  1bab462 ckpt 204: Recorded E1 (choose the launcher at unlock: two PINs / extra digit / gesture t
  4b3344b ckpt 203: RELEASED v0.3.6 (Build APK green, Release published, signed, 9.19 MB; emulator
  4e15095 ckpt 202: v0.3.6 gated (code 12) and pushed (046cb76 on main); android.yml full_build di
  046cb76 ckpt 201: gated v0.3.6 (code 12) and pushed it: the handoff self-test, the full unit sui
  5c6f4b7 ckpt 200: pre-ship: v0.3.6: new settings UI with real switches, separate Recents per lau
```
