# CHECKPOINT 194 — read me first, then TASKS.md

**Written:** 2026-11-04T01:55:04Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `334ae78` (this checkpoint is the commit after it)

## Just done
Queued D6 (one tile in custom, three in Quickstep): answer = yes via sysui_qs_tiles + WRITE_SECURE_SETTINGS; the minimum is only in the tile editor. D5 research: Quickstep Recents cards expose an accessibility 'Close' action (TaskView, R.string.accessibility_close_task) so single cards can be dismissed.

## Do this next
Continue D1/D3 research+build (CLOSE_SYSTEM_DIALOGS globalactions pre-signal, latency log), D5 (origin tracking customApps/quickstepApps, dismiss only Quickstep-launched cards via the 'Close' action, Method A default OFF, run the clear on every Quickstep->custom switch), D2 UI rewrite, D4, D0, then D6; answer ALL of D0-D6 in the final reply in order

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  TASKS.md

## Last ten checkpoints
```
  68e1751 ckpt 193: Recorded D0-D5 (Tj's five requests after v0.3.5)
  8ffbca6 ckpt 192: RELEASED v0.3.5 (Build APK green, Release published, signed, 9.18 MB; emulator
  1c28d66 ckpt 191: v0.3.5 gated (code 11) and pushed (d917f52 on main); android.yml full_build di
  d917f52 ckpt 190: gated v0.3.5 (code 11) and pushed it: the handoff self-test, the full unit sui
  fb9c70d ckpt 189: pre-ship: v0.3.5: editable lock-screen number, in-app adb setup guide + permis
  711e74a ckpt 188: record-release.sh now refuses when build.gradle versionName != the tag (preven
  95e73c5 ckpt 187: BUILDLOG fix: v0.3.4 is code 10 (record-release read the already-bumped 0.3.5/
  5f5a1f4 ckpt 186: RELEASED v0.3.4 (Build APK green, Release published, signed, 9.17 MB; emulator
  7046504 ckpt 185: v0.3.5 coded: C1 EditText for the lock number (+sanitize tests), C2 AdbGuideAc
  9af18f5 ckpt 184: v0.3.4 gated (code 10) and pushed; android.yml full_build dispatched on main. 
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
