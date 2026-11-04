# CHECKPOINT 197 — read me first, then TASKS.md

**Written:** 2026-11-04T02:01:30Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `98e57fe` (this checkpoint is the commit after it)

## Just done
D3 coded (CLOSE_SYSTEM_DIALOGS globalactions receiver -> earlyCover; POWERLAT latency stored in Features power_latency), D4 coded (warm pencil window: hidden not removed), D0 coded (Launcher state hook TjHooks.watchState; wallpaper patch only on the settled home screen; patch_dx/dy/scale_pct calibration keys). Scenario updated (D3 broadcast + latency info, B6 now checks 'hidden (kept warm)'). Build+tests green.

## Do this next
D2 UI rewrite of ControlActivity (sections + android.widget.Switch rows, power-mode radio, tile choices for D6, patch nudges, latency/diagnostics), then emulator run, then ship v0.3.6

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  tools/emulator_scenario.sh

## Last ten checkpoints
```
  860022d ckpt 196: D6 coded: TileProfile (sysui_qs_tiles per mode, snapshot via RestoreStore, hoo
  c8da53b ckpt 195: D5 built (AppOrigin tracking, selective Quickstep-only Recents dismissal via t
  0e8c56a ckpt 194: Queued D6 (one tile in custom, three in Quickstep): answer = yes via sysui_qs_
  68e1751 ckpt 193: Recorded D0-D5 (Tj's five requests after v0.3.5)
  8ffbca6 ckpt 192: RELEASED v0.3.5 (Build APK green, Release published, signed, 9.18 MB; emulator
  1c28d66 ckpt 191: v0.3.5 gated (code 11) and pushed (d917f52 on main); android.yml full_build di
  d917f52 ckpt 190: gated v0.3.5 (code 11) and pushed it: the handoff self-test, the full unit sui
  fb9c70d ckpt 189: pre-ship: v0.3.5: editable lock-screen number, in-app adb setup guide + permis
  711e74a ckpt 188: record-release.sh now refuses when build.gradle versionName != the tag (preven
  95e73c5 ckpt 187: BUILDLOG fix: v0.3.4 is code 10 (record-release read the already-bumped 0.3.5/
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
