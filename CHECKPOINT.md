# CHECKPOINT 205 — read me first, then TASKS.md

**Written:** 2026-11-04T03:51:01Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `ccd548a` (this checkpoint is the commit after it)

## Just done
E1 coded (v0.3.7/code 13): UnlockPick token (Delete x2 on the empty PIN pad = Quickstep, plain PIN = custom), service watches TYPE_VIEW_CLICKED key ids only (no digits), applies after USER_PRESENT; a11y config adds typeViewClicked+flagReportViewIds and DROPS the hardware-key capability; Control toggle; UnlockPickTest; emulator E1 test (taps on the keypad, PIN always cleared).

## Do this next
Dispatch emulator.yml; read E1 results (e1-log-token.txt, e1-mode-*.txt, e1-resumed-*.txt, 44-e1-pinpad.png); if the a11y service does not see the keypad clicks, answer 'not possible on this ROM' honestly; else ship v0.3.7; final reply answers E1 (PIN question) clearly

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  TASKS.md
    M  app/build.gradle.kts
    M  research/FIRST_INSTALL.md

## Last ten checkpoints
```
  1bab462 ckpt 204: Recorded E1 (choose the launcher at unlock: two PINs / extra digit / gesture t
  4b3344b ckpt 203: RELEASED v0.3.6 (Build APK green, Release published, signed, 9.19 MB; emulator
  4e15095 ckpt 202: v0.3.6 gated (code 12) and pushed (046cb76 on main); android.yml full_build di
  046cb76 ckpt 201: gated v0.3.6 (code 12) and pushed it: the handoff self-test, the full unit sui
  5c6f4b7 ckpt 200: pre-ship: v0.3.6: new settings UI with real switches, separate Recents per lau
  4faa6e4 ckpt 199: Run 37 (09352d0): 54 PASS / 1 FAIL = D3 broadcast check. Android 10 source con
  09352d0 ckpt 198: D2 UI rewrite done (ControlActivity: sections + Switch rows + status card + ra
  dfd53e6 ckpt 197: D3 coded (CLOSE_SYSTEM_DIALOGS globalactions receiver -> earlyCover; POWERLAT 
  860022d ckpt 196: D6 coded: TileProfile (sysui_qs_tiles per mode, snapshot via RestoreStore, hoo
  c8da53b ckpt 195: D5 built (AppOrigin tracking, selective Quickstep-only Recents dismissal via t
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
