# BUILDLOG — every shipped release, in order

Written ONLY by `tools/record-release.sh`, after GitHub Actions has built, signed and
published a version — never by hand, never before that run is green. `ship.sh` and
`.github/workflows/android.yml` gate the next `versionCode` against this file: a line here is a
claim that a release EXISTS and is installable.

No releases yet.

<!-- format once entries start:
| v0.1.0 | code 1 | 2026-01-01T00:00Z | what changed this release
-->
| v0.2.0 | code 2 | 2026-11-02T15:15Z | safe core test build: Launcher3 Go port + router + Quickstep switch + crash breaker + restore; no system settings touched
| v0.2.1 | code 3 | 2026-11-02T18:29Z | Go icon size, 5-col grid/drawer, 50% drawer scrim (measured), opt-in power-menu helper (hide Screenshot / block menu)
| v0.2.2 | code 4 | 2026-11-02T19:21Z | 8 rows no dock, two-button power card, double-Home back to custom launcher
| v0.2.3 | code 5 | 2026-11-02T20:23Z | return reset on double-Home (close apps, clear notifications, Wi-Fi/BT off, open Solitaire), more responsive twist, faster power-menu cover
| v0.3.0 | code 6 | 2026-11-02T20:56Z | hide apps + hidden new installs, settings locked to Quickstep mode, Recents-only-Solitaire (launch flag), Clear-all switches via accessibility, Solitaire glitch fix
| v0.3.1 | code 7 | 2026-11-02T23:33Z | more responsive twist, keyboard switch (adb grant), faster reset, launcher out of Recents, Solitaire at launcher start
| v0.3.2 | code 8 | 2026-11-03T05:14Z | v0.3.2: Quickstep lock-out (W5), Recents runtime exclude (W2), faster reset (W1), pre-built power card + side-key early show (W3/W4), lock-screen number (X1), branch cleanup (X2)
| v0.3.3 | code 9 | 2026-11-03T20:40Z | v0.3.3: power-menu cover on the lock screen, faster double-Home reset (skip empty shade, animations off), lock-screen number matched to Tj's measurements, wallpaper-cover power mode, original leaf wallpaper
| v0.3.4 | code 10 | 2026-11-04T00:49Z | v0.3.4: launcher hidden from Recents (home-type, default ON by Tj's choice), Launcher control scrolls, quick-settings pencil hidden, double-Home shortest path (skip empty shade/Recents, black cover, instant Solitaire)
| v0.3.5 | code 11 | 2026-11-04T01:08Z | v0.3.5: editable lock-screen number, in-app adb setup guide + permissions declared in advance, Quickstep started as a home-type task (never in Recents)
| v0.3.6 | code 12 | 2026-11-04T02:36Z | v0.3.6: new settings UI with real switches, separate Recents per launcher, quick-settings tiles per launcher, faster pencil cover, home-screen-only wallpaper patch with nudges, power-menu latency readout
| v0.3.7 | code 13 | 2026-11-04T04:52Z | v0.3.7: choose the launcher at unlock (Delete twice before your PIN = Quickstep, plain PIN = custom; opt-in), return reset no longer leaves Recents open when there is no app to open, helper no longer asks for hardware-key access
| v0.4.0 | code 14 | 2026-11-04T08:24Z | v0.4.0: lock-screen handshakes (6 methods, no gesture into Quickstep), two-profile lock-in (Solitaire + custom home pre-armed behind the lock screen, plain unlock ends a Quickstep session, restart ends it), 7 ways back to custom, master failsafe (Konami key code, Solitaire hotspot, helper-off triple Home), invisible adb clean-up helper, wallpaper/number-reset buttons removed
| v0.5.0 | code 15 | 2026-11-04T22:19Z | v0.5.0: more lock-screen methods (drop-down, min one), bigger hidden-app toggles, lock-time prep (close Quickstep apps at lock), per-launcher tiles, opt-in native pencil/gear hide, accelerometer return (shake/knock), one-session adb setup (tools/adb-all.sh/.bat) + in-app Tablet setup & report, phone adb route, fixes: lost Enter tap at unlock
| v0.6.0 | code 16 | 2026-11-04T23:19Z | v0.6.0: unlock stopwatch + compile-ahead job (U1/U2), lock-screen power-menu patch as a photo of the real lock screen with a two-button card (V1-V4), extended probe (W1/W2), PhoneGuide phone app (Y2-Y4)
| v0.7.0 | code 17 | 2026-11-05T05:10Z | v0.7.0: task-precise Recents clean-up at the lock (only Quickstep's own tasks go; apps both worlds use are spared unless they have a notification), keep-alive service for a faster unlock (default on, switch in Control), optional status-bar hiding in custom (off by default), safety audit with fixes, efficiency pass for the MT8168, notification-panel rules, phone guide 1.1 (every command line run in CI, compile undo fixed), Button Mapper and open-but-hidden research
| v0.7.1 | code 18 | 2026-11-05T08:34Z | v0.7.1: PhoneGuide 1.2 (short-cable plan: 3 parts, next-step button, ticks that stay); tablet report now holds the launcher's own state, a parser self-check, the side-key test (Power menu v2 readiness) and a short text summary (setup button 2b); tools/tablet-intake.sh saves it for Claude; code-review fixes (key test survives many input devices, the guide's adb-shell switch keeps the page)
| v0.7.2 | code 19 | 2026-11-05T23:06Z | v0.7.2: keep-alive crash fix (PNG icon, default off, fuse), breaker auto-clear on a newer version, honest setup-screen wording
| v0.7.3 | code 20 | 2026-11-06T03:04Z | v0.7.3: a plain PIN always ends in the custom launcher (AC1, no switch needed; paused only by the crash breaker or Restore everything); lock-screen power-menu card taken from the real panel, ends where Screenshot begins, dimmed wallpaper patch (AC6); gesture unlock more reliable: pad latch survives a wrong PIN, stock home started behind the lock at once, verify + restart (AC7, AC9); Solitaire stand-in only when its switch is on (AC2, AC8); battery-saver handling (AC3); lock log for evidence
| v0.7.4 | code 21 | 2026-11-06T05:50Z | v0.7.4: the power-menu cover works on every lock screen (AD7a); a gesture unlock brings back the Quickstep app that was open, switchable (AD5); hidden switchable entrances to Quickstep: page flick, Auto-rotate tile x5, charger (AD4); tablet report measurements and a capped short summary (AD8, PG3); code-review fixes (stale resume app, 6 h clock, lock-leftover rule)
| v0.7.5 | code 22 | 2026-11-06T06:52Z | v0.7.5: long press on the home screen shows Wallpapers only (AE7); battery saver follows the launcher, on and staying on in Quickstep, off in custom (AG1); an app opened in Quickstep is found from Android's usage log and closed when you return to custom (AI1); the home-screen power-menu patch dims like the stock menu, with a Menu dimming nudge (AH1); optional resting lock-screen patch switch, off by default (AE3)
| v0.7.6 | code 23 | 2026-11-06T07:57Z | v0.7.6: lock-screen number now matches the stock one (bigger, Medium weight, lower) with size/weight/brightness steppers in Launcher control; lock screen stays quiet (no Wi-Fi, Bluetooth or app notifications); soft-edged lock-screen patch aligned per tablet; battery-saver cap/breaker and review fixes
