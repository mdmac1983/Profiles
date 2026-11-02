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
