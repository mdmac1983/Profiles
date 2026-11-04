# CHECKPOINT 199 — read me first, then TASKS.md

**Written:** 2026-11-04T02:18:00Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `6e48b4e` (this checkpoint is the commit after it)

## Just done
Run 37 (09352d0): 54 PASS / 1 FAIL = D3 broadcast check. Android 10 source confirms PhoneWindowManager does not broadcast globalactions on power-menu open (only on the shutdown path): no pre-signal exists. Removed the dead receiver; D3 stays as a latency measurement (POWERLAT). Everything else passed: D6 tiles, B6/D4 warm pencil, B4, Y2, REC1 with Method A on, C5, B1, crash breaker.

## Do this next
Dispatch emulator.yml on the final v0.3.6 code, expect 0 FAIL; then ship v0.3.6 (ship.sh, android.yml, record-release before any bump); final reply answers D0-D7 in order

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  research/FIRST_INSTALL.md
    M  research/NOTES_2026-11-04.md

## Last ten checkpoints
```
  09352d0 ckpt 198: D2 UI rewrite done (ControlActivity: sections + Switch rows + status card + ra
  dfd53e6 ckpt 197: D3 coded (CLOSE_SYSTEM_DIALOGS globalactions receiver -> earlyCover; POWERLAT 
  860022d ckpt 196: D6 coded: TileProfile (sysui_qs_tiles per mode, snapshot via RestoreStore, hoo
  c8da53b ckpt 195: D5 built (AppOrigin tracking, selective Quickstep-only Recents dismissal via t
  0e8c56a ckpt 194: Queued D6 (one tile in custom, three in Quickstep): answer = yes via sysui_qs_
  68e1751 ckpt 193: Recorded D0-D5 (Tj's five requests after v0.3.5)
  8ffbca6 ckpt 192: RELEASED v0.3.5 (Build APK green, Release published, signed, 9.18 MB; emulator
  1c28d66 ckpt 191: v0.3.5 gated (code 11) and pushed (d917f52 on main); android.yml full_build di
  d917f52 ckpt 190: gated v0.3.5 (code 11) and pushed it: the handoff self-test, the full unit sui
  fb9c70d ckpt 189: pre-ship: v0.3.5: editable lock-screen number, in-app adb setup guide + permis
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
