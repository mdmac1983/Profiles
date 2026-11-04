# CHECKPOINT 196 — read me first, then TASKS.md

**Written:** 2026-11-04T02:00:21Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `9bc7732` (this checkpoint is the commit after it)

## Just done
D6 coded: TileProfile (sysui_qs_tiles per mode, snapshot via RestoreStore, hook in Mode.set, sanitize tests), scenario check D6. Build+tests green.

## Do this next
D3 (CLOSE_SYSTEM_DIALOGS 'globalactions' receiver -> earlyCover + latency log), D4 (warm pencil window), D0 (patch only on the home screen: Launcher state hook + calibration offsets), then D2 UI rewrite incl. tile choices

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  c8da53b ckpt 195: D5 built (AppOrigin tracking, selective Quickstep-only Recents dismissal via t
  0e8c56a ckpt 194: Queued D6 (one tile in custom, three in Quickstep): answer = yes via sysui_qs_
  68e1751 ckpt 193: Recorded D0-D5 (Tj's five requests after v0.3.5)
  8ffbca6 ckpt 192: RELEASED v0.3.5 (Build APK green, Release published, signed, 9.18 MB; emulator
  1c28d66 ckpt 191: v0.3.5 gated (code 11) and pushed (d917f52 on main); android.yml full_build di
  d917f52 ckpt 190: gated v0.3.5 (code 11) and pushed it: the handoff self-test, the full unit sui
  fb9c70d ckpt 189: pre-ship: v0.3.5: editable lock-screen number, in-app adb setup guide + permis
  711e74a ckpt 188: record-release.sh now refuses when build.gradle versionName != the tag (preven
  95e73c5 ckpt 187: BUILDLOG fix: v0.3.4 is code 10 (record-release read the already-bumped 0.3.5/
  5f5a1f4 ckpt 186: RELEASED v0.3.4 (Build APK green, Release published, signed, 9.17 MB; emulator
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
