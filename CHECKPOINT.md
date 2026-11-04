# CHECKPOINT 195 — read me first, then TASKS.md

**Written:** 2026-11-04T01:59:18Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `dfb3dc5` (this checkpoint is the commit after it)

## Just done
D5 built (AppOrigin tracking, selective Quickstep-only Recents dismissal via the card 'Close' action with Clear-all fallback, Method A default OFF, Control 'Use custom launcher' also runs the reset, kill keeps custom-launched apps); unit tests+build green; scenario B4 now writes tj_origin.xml. D7 research written to research/NOTES_2026-11-04.md.

## Do this next
D6 per-mode tiles (TileProfile: snapshot sysui_qs_tiles, write 'rotation' on custom / chosen list on Quickstep, hook in Mode.set, needs WRITE_SECURE_SETTINGS), then D2 UI rewrite, D3 close-system-dialogs pre-signal + latency log, D4 pencil speed, D0 patch home-only; emulator run; ship v0.3.6; final reply answers D0-D7 in order

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  research/NOTES_2026-11-04.md

## Last ten checkpoints
```
  0e8c56a ckpt 194: Queued D6 (one tile in custom, three in Quickstep): answer = yes via sysui_qs_
  68e1751 ckpt 193: Recorded D0-D5 (Tj's five requests after v0.3.5)
  8ffbca6 ckpt 192: RELEASED v0.3.5 (Build APK green, Release published, signed, 9.18 MB; emulator
  1c28d66 ckpt 191: v0.3.5 gated (code 11) and pushed (d917f52 on main); android.yml full_build di
  d917f52 ckpt 190: gated v0.3.5 (code 11) and pushed it: the handoff self-test, the full unit sui
  fb9c70d ckpt 189: pre-ship: v0.3.5: editable lock-screen number, in-app adb setup guide + permis
  711e74a ckpt 188: record-release.sh now refuses when build.gradle versionName != the tag (preven
  95e73c5 ckpt 187: BUILDLOG fix: v0.3.4 is code 10 (record-release read the already-bumped 0.3.5/
  5f5a1f4 ckpt 186: RELEASED v0.3.4 (Build APK green, Release published, signed, 9.17 MB; emulator
  7046504 ckpt 185: v0.3.5 coded: C1 EditText for the lock number (+sanitize tests), C2 AdbGuideAc
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
