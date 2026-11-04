# CHECKPOINT 202 — read me first, then TASKS.md

**Written:** 2026-11-04T02:32:56Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `046cb76` (this checkpoint is the commit after it)

## Just done
v0.3.6 gated (code 12) and pushed (046cb76 on main); android.yml full_build dispatched. Emulator run 38 = 55 PASS / 0 FAIL.

## Do this next
Wait ~4 min: get_release_by_tag v0.3.6, then record-release v0.3.6 (version in build.gradle is still 0.3.6, so the guard passes); send Tj the link and the answers to D0-D7 in order

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  046cb76 ckpt 201: gated v0.3.6 (code 12) and pushed it: the handoff self-test, the full unit sui
  5c6f4b7 ckpt 200: pre-ship: v0.3.6: new settings UI with real switches, separate Recents per lau
  4faa6e4 ckpt 199: Run 37 (09352d0): 54 PASS / 1 FAIL = D3 broadcast check. Android 10 source con
  09352d0 ckpt 198: D2 UI rewrite done (ControlActivity: sections + Switch rows + status card + ra
  dfd53e6 ckpt 197: D3 coded (CLOSE_SYSTEM_DIALOGS globalactions receiver -> earlyCover; POWERLAT 
  860022d ckpt 196: D6 coded: TileProfile (sysui_qs_tiles per mode, snapshot via RestoreStore, hoo
  c8da53b ckpt 195: D5 built (AppOrigin tracking, selective Quickstep-only Recents dismissal via t
  0e8c56a ckpt 194: Queued D6 (one tile in custom, three in Quickstep): answer = yes via sysui_qs_
  68e1751 ckpt 193: Recorded D0-D5 (Tj's five requests after v0.3.5)
  8ffbca6 ckpt 192: RELEASED v0.3.5 (Build APK green, Release published, signed, 9.18 MB; emulator
```
