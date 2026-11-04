# CHECKPOINT 203 — read me first, then TASKS.md

**Written:** 2026-11-04T02:36:43Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `63bd788` (this checkpoint is the commit after it)

## Just done
RELEASED v0.3.6 (Build APK green, Release published, signed, 9.19 MB; emulator run 38 = 55 PASS / 0 FAIL); BUILDLOG recorded (code 12)

## Do this next
Wait for Tj's v0.3.6 tablet report: Recents separation (do Quickstep-opened cards get dismissed one by one? fallback = Clear all), tiles per launcher (needs adb WRITE_SECURE_SETTINGS), wallpaper patch alignment nudges, power-menu 'Speed of the last menu' line, pencil speed; his decision on D7 options (multi-user check: adb shell pm get-max-users); optional C4 gear -> cloned Settings (needs SET1)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  4e15095 ckpt 202: v0.3.6 gated (code 12) and pushed (046cb76 on main); android.yml full_build di
  046cb76 ckpt 201: gated v0.3.6 (code 12) and pushed it: the handoff self-test, the full unit sui
  5c6f4b7 ckpt 200: pre-ship: v0.3.6: new settings UI with real switches, separate Recents per lau
  4faa6e4 ckpt 199: Run 37 (09352d0): 54 PASS / 1 FAIL = D3 broadcast check. Android 10 source con
  09352d0 ckpt 198: D2 UI rewrite done (ControlActivity: sections + Switch rows + status card + ra
  dfd53e6 ckpt 197: D3 coded (CLOSE_SYSTEM_DIALOGS globalactions receiver -> earlyCover; POWERLAT 
  860022d ckpt 196: D6 coded: TileProfile (sysui_qs_tiles per mode, snapshot via RestoreStore, hoo
  c8da53b ckpt 195: D5 built (AppOrigin tracking, selective Quickstep-only Recents dismissal via t
  0e8c56a ckpt 194: Queued D6 (one tile in custom, three in Quickstep): answer = yes via sysui_qs_
  68e1751 ckpt 193: Recorded D0-D5 (Tj's five requests after v0.3.5)
```
