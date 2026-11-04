# CHECKPOINT 198 — read me first, then TASKS.md

**Written:** 2026-11-04T02:03:08Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `5eb49af` (this checkpoint is the commit after it)

## Just done
D2 UI rewrite done (ControlActivity: sections + Switch rows + status card + radio + tile checkboxes + patch nudges + confirm dialog), scenario Method-A block fixed for the new default, version 0.3.6/code 12, FIRST_INSTALL notes, TASKS marked [~] (coded, not proven). Build+tests green.

## Do this next
Dispatch emulator.yml; read results (D3 broadcast, D6 tiles, B6 hidden-warm, Y2/B4 with AppOrigin, REC1 with Method A on, 06-control.png look); fix FAILs; ship v0.3.6 (ship.sh, android.yml, record-release BEFORE any bump); final reply answers D0-D7 in order

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  TASKS.md
    M  app/build.gradle.kts
    M  research/FIRST_INSTALL.md

## Last ten checkpoints
```
  dfd53e6 ckpt 197: D3 coded (CLOSE_SYSTEM_DIALOGS globalactions receiver -> earlyCover; POWERLAT 
  860022d ckpt 196: D6 coded: TileProfile (sysui_qs_tiles per mode, snapshot via RestoreStore, hoo
  c8da53b ckpt 195: D5 built (AppOrigin tracking, selective Quickstep-only Recents dismissal via t
  0e8c56a ckpt 194: Queued D6 (one tile in custom, three in Quickstep): answer = yes via sysui_qs_
  68e1751 ckpt 193: Recorded D0-D5 (Tj's five requests after v0.3.5)
  8ffbca6 ckpt 192: RELEASED v0.3.5 (Build APK green, Release published, signed, 9.18 MB; emulator
  1c28d66 ckpt 191: v0.3.5 gated (code 11) and pushed (d917f52 on main); android.yml full_build di
  d917f52 ckpt 190: gated v0.3.5 (code 11) and pushed it: the handoff self-test, the full unit sui
  fb9c70d ckpt 189: pre-ship: v0.3.5: editable lock-screen number, in-app adb setup guide + permis
  711e74a ckpt 188: record-release.sh now refuses when build.gradle versionName != the tag (preven
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
