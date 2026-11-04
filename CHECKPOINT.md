# CHECKPOINT 204 — read me first, then TASKS.md

**Written:** 2026-11-04T03:47:56Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `cc698ac` (this checkpoint is the commit after it)

## Just done
Recorded E1 (choose the launcher at unlock: two PINs / extra digit / gesture token); closed C3 (wallpaper) per Tj

## Do this next
Research: (1) Android has ONE credential, wrong PIN counts as a failed attempt (so '10001' vs PIN 1000 cannot work as a second PIN); (2) fetch SystemUI keyguard_pin_view.xml (Android 10) for the key ids (key0-9, delete_button, key_enter) and NumPadKey accessibility; (3) feasibility test on the emulator: add typeViewClicked, log token ids ONLY (never digits); (4) if it works build opt-in 'Tap Delete twice before your PIN = unlock into Quickstep' with early Mode switch + revert if still locked

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  TASKS.md

## Last ten checkpoints
```
  4b3344b ckpt 203: RELEASED v0.3.6 (Build APK green, Release published, signed, 9.19 MB; emulator
  4e15095 ckpt 202: v0.3.6 gated (code 12) and pushed (046cb76 on main); android.yml full_build di
  046cb76 ckpt 201: gated v0.3.6 (code 12) and pushed it: the handoff self-test, the full unit sui
  5c6f4b7 ckpt 200: pre-ship: v0.3.6: new settings UI with real switches, separate Recents per lau
  4faa6e4 ckpt 199: Run 37 (09352d0): 54 PASS / 1 FAIL = D3 broadcast check. Android 10 source con
  09352d0 ckpt 198: D2 UI rewrite done (ControlActivity: sections + Switch rows + status card + ra
  dfd53e6 ckpt 197: D3 coded (CLOSE_SYSTEM_DIALOGS globalactions receiver -> earlyCover; POWERLAT 
  860022d ckpt 196: D6 coded: TileProfile (sysui_qs_tiles per mode, snapshot via RestoreStore, hoo
  c8da53b ckpt 195: D5 built (AppOrigin tracking, selective Quickstep-only Recents dismissal via t
  0e8c56a ckpt 194: Queued D6 (one tile in custom, three in Quickstep): answer = yes via sysui_qs_
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
