# Profiles (`app.orionmd.profiles`)

A home-screen launcher for **one specific tablet**: an Alps 711 (`tb8168p1_bsp`, MediaTek MT8168B, 32-bit ARM, 2 GB RAM,
600 x 1024, 170 dpi) running **Android 10 (API 29) Go edition**, with the stock **Quickstep v10** launcher installed. It is
sideloaded and never published. GitHub Actions builds and signs every APK; Claude Code sessions write the code.

The idea in one sentence: the tablet behaves like **two user accounts on one Android user**. The **custom launcher**
(a port of AOSP Launcher3 Go that looks exactly like the stock one) is the everyday, locked-down world. **Quickstep**
(the stock launcher, untouched) is the private world, reachable only through a secret handshake at the lock screen.
Going back cleans up after Quickstep: its apps, Recents cards, notifications and radios.

> **Fork, 2026-11-09:** renamed to **Profiles**, app ID `app.orionmd.profiles` (the Java package stays `com.tj.launcher`), versions restart at **1.0** (+0.1 per update; versionCode keeps rising from 37), releases only in `mdmac1983/Profiles`, signed with the owner's own key (GitHub secret `SIGNING_KEYSTORE_BASE64`). New in 1.0: in-app updates (`UpdateRules`, `Updater`, `UpdateReceiver`, `UpdateStatusReceiver`) and the User guide / Features pages (`HelpActivity`, `res/raw/`). Planned: 1.1 profiles Owner / FBOP / Rental with a handshake map (TASKS BR1/BS1).
> Written from the code on 2026-11-08 (TASKS BM1). For day-to-day work, read `HANDOFF.md` → `CLAUDE.md` → `CHECKPOINT.md`.

---

## Contents
1. [Features](#1-features)
2. [How it fits together](#2-how-it-fits-together)
3. [Repository structure](#3-repository-structure)
4. [Code map: `com.tj.launcher`](#4-code-map-comtjlauncher)
5. [Launcher3 port](#5-launcher3-port)
6. [Permissions and the adb grant](#6-permissions-and-the-adb-grant)
7. [Tests](#7-tests)
8. [Build, CI and release](#8-build-ci-and-release)
9. [Tooling and the session handoff system](#9-tooling-and-the-session-handoff-system)
10. [Safety rules](#10-safety-rules)
11. [Where to read more](#11-where-to-read-more)

---

## 1. Features

Most features have their own switch in **Launcher control**, a settings screen that can only be opened while Quickstep
is active. A feature that changes an Android setting is snapshotted first and undone by **Restore everything**.

### 1.1 Two launchers, one Home button
- **`RouterActivity` is the only HOME activity.** It is invisible. On every Home press it sends you to the custom
  Launcher3 or to stock Quickstep (`Mode`: CUSTOM is the default). If anything goes wrong it falls back to the stock launcher.
- **Crash-loop breaker** (`CrashGuard`): three crashes within 2 minutes switch to Quickstep and restore every change.
- **Stray-home guard** (`StrayHomeGuard`): if the stock home appears in custom mode by any route other than the
  handshake, the app presses Home again.
- **Keep-alive** (`KeepAliveService`, `KeepAlivePolicy`, `ProcessLife`): a foreground service that does nothing, so
  the launcher process stays in RAM and starts instantly. It has a fuse that switches it off after crashes.

### 1.2 Looks identical to the stock launcher
The reference photos are in `research/reference/stock/` (`INDEX.md`): 5 columns x 8 rows, **no dock**, no all-apps
caret, no home search bar, and a 50 % white drawer scrim (`TjLook`, Launcher3 patches in `launcher3/PATCHES.md`).

### 1.3 Hidden apps and a clean Recents list
- **App whitelist** (`HiddenApps`, `AppVisibility`, `HiddenAppsActivity`): the custom drawer shows only the apps you
  allowed. Apps that were installed at the first run stay visible. A new install starts hidden.
- **Recents shows only what belongs to the custom drawer**: launches get `FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS`
  (`RecentsPolicy`, `RecentsGuard`). `RecentsSweep` removes the Recents tasks of every app that is not in the drawer
  (by task id, through the shell helper). `RecentsFilter` closes such a card on screen when the helper is not running.
- **Clean start** (`clean_start`, on by default in release builds): every custom session starts with an empty Recents
  list and the home screen alone. No Solitaire stand-in.

### 1.4 Into Quickstep: secret unlock handshakes
The tablet has one PIN. A **plain PIN always opens the custom launcher**. To open Quickstep you do a handshake first and
then type the PIN (`UnlockMethods`, `UnlockPick`, `ProfileSession`). There are 16 methods. You can turn on several at
once, and at least one always stays on:

| Pad | Keys | Body / sensors |
| :- | :- | :- |
| Double erase *(default)*, Tap and erase, Erase ladder, Peek x3, Peek x5 | Volume code, Volume ping-pong, Both volume keys, Hold-then-tap, Three Backs + Volume Up, Volume Up + double erase, Side-key flurry | Charger flick, Tablet twirl (rotation), Triple shake, Double knock (accelerometer, `MotionDetector`) |

The key sequences are matched by `KeySeq`. An optional 40 ms buzz confirms the handshake. `ResumeTarget` can reopen the
app that was in front when you locked, instead of the Quickstep home screen. `UsageGap` and `LockLog` make sure a plain
PIN goes to custom even when the helper's process was killed.

**Master failsafes into Quickstep** that do not need the lock screen (`FailsafeKeys`, `TapCode`, `SecretEntrances`,
`EntranceWatcher`): a Konami-style key sequence, a hidden hotspot in a corner of Solitaire (5 taps, then hold), and a
page-flick pattern on the home screen.

### 1.5 Back to custom, with a reset
**Ways back** (`ReturnMethods`, `BackToCustomActivity`, `ReturnTile`, `HomeDoublePress`): double Home, a "Back to
custom" icon, a quick-settings tile, volume codes, three Backs + Volume Down, both volume keys, a side-key flurry,
shake or knock.

**Return reset** (`ReturnReset`, `ResetPolicy`, `TaskOrigin`, `AppOrigin`, `QuickstepUsage`, `RecentsDirty`): closes the
apps opened in Quickstep, clears notifications and turns radios off.
- **With the shell helper** running, it works invisibly: it force-stops apps and removes tasks by id, with nothing shown on screen.
- **Without the helper**, it drives the screen behind a cover. The cover used to be black. Since BL1 it is the
  **home curtain** (`HomeCurtain`, `HomeCurtainRules`, `HomeLayer`): a picture of your actual home screen, made from
  the wallpaper and the launcher's own pixels. If there is no matching picture, the black cover is used.
- Apps used in both worlds are handled per task: only the tasks that Quickstep created are removed.

### 1.6 The lock screen
- **Lock quiet** (`LockQuiet`): Wi-Fi, Bluetooth and notifications are off whenever the lock screen is up. They come
  back only through the handshake.
- **No banners** (`NotifQuiet`, `NotifQuietPolicy`, `NotifQuietHold`, `NotifSightings`): pop-up notifications are held
  off on the lock screen and in the custom launcher. The original value is stored outside the app's own data, so
  Clear data cannot leave it stuck.
- **Lock-screen number** (`LockNumber`): an optional serial number drawn at the bottom of the lock screen. It
  disappears when you pull up the PIN pad. Size and brightness can be set.
- **Pencil cover**: a cover over the quick-settings "edit" pencil in the custom launcher and on the lock screen.

### 1.7 Power menu: the Screenshot entry is hidden
The power menu (side key) of this ROM has a Screenshot entry. The accessibility helper hides it so that the menu looks
like it has only Power off and Restart. The power key cannot be intercepted on Android 10, so the work is in the overlay:
- **Lock screen**: a real-pixel **wallpaper patch** sits over the Screenshot spot. It is calibrated from one screenshot
  (`PatchShot`, `PatchCalibrator`, `AutoCalibration`, `PatchProfile`). It rests invisibly (`PatchRest`). When the menu
  opens it follows the stock 300 ms scrim fade (`PatchFade`), and its open brightness is measured on the tablet itself
  (`ScrimFit`, `ScrimMeasure`). Light and dark themes have separate calibrations.
- **Home screen** (`HomePatchRules`, `HomeShot`, `HomeLayer`): the same patch technique for the unlocked home. It uses
  one crop per wallpaper offset step.
- Modes (`PowerMenuRule`, `Features.POWER_*`): off / hide screenshot / wallpaper / block completely.
  `PowerMenuDump` records the real menu layout for calibration.

### 1.8 Different system settings per launcher
Each launcher gets its own value for these settings. They all need the adb permission (`WRITE_SECURE_SETTINGS`):
- **Quick-settings tiles** (`TileProfile`): one tile in custom, three in Quickstep.
- **Keyboard** (`KeyboardSwitch`, `KeyboardPolicy`): the stock keyboard in custom, SwiftKey in Quickstep.
- **Status bar** (`ImmersiveProfile`): hidden while custom is active.
- **Battery saver** (`BatterySaverGuard`, `BatterySaverPolicy`): turned off when custom becomes active and, optionally,
  kept off. It drives Android's real state machine and never writes `low_power` directly.
- **Animations** (`AnimationSpeedup`, `AnimationPolicy`, `AnimationHolds`): the three animation scales are 0 only
  inside an unlocked Quickstep session. The lock screen and the custom launcher keep real animations.

### 1.9 Wallpaper picker
The long-press menu offers **Wallpapers** only. It always opens the system picker (`WallpaperPickerChoice`), never a
chooser. A bundled AOSP picker without "My photos" is in `wallpaperpicker/`, and `PickerGuard` hides the "My photos" tile.

### 1.10 The shell helper (optional, adb-started)
`ShellServer` runs as the **shell user** (`app_process`, started by a single adb line, dies at reboot). It listens on
loopback TCP 127.0.0.1:47631-47635 and checks the peer's uid. `ShellProtocol` only runs jobs from an allow-list:
force-stop, list and remove tasks, notification packages, permission grants, screenshots, APK copies, probes.
`ShellHelper` is the app-side client. `HelperLife` and `HelperStatus` record whether the helper is alive.
Every helper feature falls back to today's behaviour when the helper is gone.

### 1.11 Setup and diagnostics
- **`SetupActivity`**: the whole adb to-do list as one screen, with a **Do everything** button.
- **`AdbGuideActivity`**: the adb commands step by step, each with a Copy button.
- **`TabletReport`** + **`ReportState`**: a text report of the tablet's facts and the launcher's state. Process it with
  `tools/tablet-intake.sh`, which redacts identifiers.
- **Phone guide app** (`phoneguide/`, `com.tj.phoneguide`): a separate APK with the adb guide for an OTG phone.
  Its steps are in `phoneguide/src/main/assets/steps.txt`.
- **`ControlActivity`** (Launcher control): collapsible groups (Everyday, Lock screen, Unlock into Quickstep, Power
  menu, Return to custom, ...). Every option is a real on/off switch, and the screen has a status header and **Restore everything**.
- Research-only entry points that the app itself never runs: `ShellProbe`, `InputProbe`.

---

## 2. How it fits together

```
 Home press ──► RouterActivity ──► Mode = CUSTOM ──► Launcher3 (ported, com.android.launcher3.Launcher)
                    │                                   └─ TjHooks: resume/pause, touch, wallpaper offset, state
                    └────────────► Mode = QUICKSTEP ─► stock Quickstep (StockLauncher)

 TjAccessibilityService  (same process and main thread as the Launcher)
   ├─ lock screen: handshakes (UnlockMethods/KeySeq/MotionDetector), LockQuiet, LockNumber, pencil cover
   ├─ power menu:  PatchRest / PatchShot / PatchFade / ScrimMeasure (lock), HomePatchRules / HomeShot (home)
   ├─ unlock:      ProfileSession decides custom vs Quickstep → Mode.set → ReturnReset / RecentsSweep
   └─ guards:      RecentsFilter, PickerGuard, StrayHomeGuard, BatterySaverGuard, NotifQuiet, AnimationSpeedup

 ShellServer (shell uid, optional) ◄── loopback TCP ── ShellHelper  (force-stop, RMTASKS, LISTTASKS, ...)

 Features (prefs "tj_features") · RestoreStore (snapshot of every system setting) · CrashGuard (installed by TjApplication)
```

Design principles visible throughout the code:
- **Pure rules + thin Android shells.** Every decision is a pure Java class with JVM tests: `*Rules`, `*Policy`,
  `*Holds`, `ProfileSession`, `TaskOrigin.plan`, `KeySeq`, `MotionDetector`, `ScrimFit`. The Android-facing class
  only gathers facts and applies the result.
- **Level-triggered, not event-triggered.** Anything that must never get stuck (animation holds, banner holds, the
  patch) re-reads the facts on each trigger instead of trusting the last event.
- **App-only first, helper second.** Features that need the shell helper degrade to the app-only path.
- **Release default ON, debug default OFF** for new behaviour switches, so the older emulator sections keep their expectations.

---

## 3. Repository structure

| Path | What |
| :- | :- |
| `app/` | The launcher APK. `src/main/java/com/tj/launcher/` = all of our code (90 classes, Java); `AndroidManifest.xml` merged from AOSP plus our components; `src/test/` = 59 JVM test classes. |
| `launcher3/` | AOSP Launcher3 **android-10.0.0_r47, Go flavour, without the Quickstep recents layer** (`src`, `src_ui_overrides`, `go/src`, `res`, `go/res`, `protos`, `proto_overrides`, `src_plugins`, `plugin_core_src`, `iconloaderlib`). Compiled into `:app` as source sets. Each change from upstream is listed in `launcher3/PATCHES.md`. |
| `wallpaperpicker/` | AOSP wallpaper picker (android-10.0.0_r9) without "My photos" (`PATCHES.md`). Compiled into `:app`. |
| `phoneguide/` | Separate APK `com.tj.phoneguide`: the phone-side adb guide (`GuideActivity`, `GuideParser`, `A11yLine`). |
| `testnotifier/` | CI-only test app (flavours `quick` / `ctl`): posts notifications and opens a second task for the emulator scenario. Never released. |
| `signing/` | The base64 release keystore, committed on purpose (see `signing/README.md`). SHA-256 pinned in `tools/keystore.fingerprint`. |
| `.github/workflows/` | `android.yml` (build, sign, Release), `emulator.yml` (the scenario, by section), `ci.yml` (PR tests + lint). |
| `tools/` | Build wrapper, hooks, checkpoint/ship scripts, emulator scenario, gate planner, adb scripts, photo/scrim analysis (section 9). |
| `research/` | Design notes, AOSP excerpts, measurements, tablet checklists, stock reference photos (`reference/stock/`), redacted tablet reports (`tablet/`). |
| Root docs | `HANDOFF.md` (start here), `CLAUDE.md` (working agreement), `BRIEF.md` (platform decisions), `TASKS.md` (every request in Tj's words), `CHECKPOINT.md`, `INBOX.md` / `OUTBOX.md` (message log), `BUILDLOG.md` (releases). |

Gradle modules (`settings.gradle.kts`): `:app`, `:phoneguide`, `:testnotifier`, `:iconloaderlib`.
Toolchain: AGP 8.13.2, Gradle 8.14.3, JDK 17 target. `minSdk 29`, `targetSdk 28` (the AOSP Go value, which lets the
reset toggle Wi-Fi and Bluetooth), `compileSdk 36`. Dependencies: `iconloaderlib`, androidx `dynamicanimation`,
`recyclerview`, `preference`, and `protobuf-javanano`.

---

## 4. Code map: `com.tj.launcher`

Line counts are approximate. The one large class is `TjAccessibilityService` (~4,400 lines).

| Area | Classes |
| :- | :- |
| Entry, modes, safety | `TjApplication`, `RouterActivity`, `Mode`, `StockLauncher`, `CrashGuard`, `StrayHomeGuard`, `SwitchMigration`, `RestoreStore`, `Features`, `TjLook`, `TjHooks` |
| Lock-screen handshake | `UnlockMethods`, `UnlockPick`, `KeySeq`, `MotionDetector`, `ProfileSession`, `ResumeTarget`, `UsageGap`, `LockLog` |
| Failsafes into Quickstep | `FailsafeKeys`, `TapCode`, `SecretEntrances`, `EntranceWatcher` |
| Ways back + reset | `ReturnMethods`, `BackToCustomActivity`, `ReturnTile`, `HomeDoublePress`, `ReturnReset`, `ResetPolicy`, `TaskOrigin`, `AppOrigin`, `QuickstepUsage`, `RecentsDirty` |
| Home curtain (BL1) | `HomeCurtain`, `HomeCurtainRules`, `HomeLayer` |
| Apps + Recents | `HiddenApps`, `AppVisibility`, `HiddenAppsActivity`, `RecentsPolicy`, `RecentsGuard`, `RecentsFilter`, `RecentsSweep` |
| Lock-screen quiet | `LockQuiet`, `NotifQuiet`, `NotifQuietPolicy`, `NotifQuietHold`, `NotifSightings`, `LockNumber` |
| Power-menu patch (lock) | `PowerMenuRule`, `PowerMenuDump`, `PatchRest`, `PatchShot`, `PatchCalibrator`, `AutoCalibration`, `PatchProfile`, `PatchFade`, `ScrimFit`, `ScrimMeasure` |
| Power-menu patch (home) | `HomePatchRules`, `HomeShot`, `HomeLayer` |
| Per-launcher settings | `TileProfile`, `KeyboardSwitch`, `KeyboardPolicy`, `ImmersiveProfile`, `BatterySaverGuard`, `BatterySaverPolicy`, `AnimationSpeedup`, `AnimationPolicy`, `AnimationHolds`, `DemoModeProfile` (retired; only a one-time cleanup remains) |
| Wallpaper | `WallpaperPickerChoice`, `PickerGuard` |
| Process / RAM | `KeepAliveService`, `KeepAlivePolicy`, `ProcessLife` |
| Shell helper | `ShellServer`, `ShellProtocol`, `ShellHelper`, `HelperLife`, `HelperStatus` |
| Accessibility helper | `TjAccessibilityService`, `EventNoise` (drops other apps' content-change noise before any scan) |
| Setup + reports | `SetupActivity`, `AdbGuideActivity`, `TabletReport`, `ReportState`, `ControlActivity` |
| Research only | `ShellProbe`, `InputProbe` |

Manifest components of our own: `RouterActivity` (HOME), `ControlActivity`, `HiddenAppsActivity`, `SetupActivity`,
`AdbGuideActivity`, `BackToCustomActivity`, `TjAccessibilityService`, `KeepAliveService`, `ReturnTile` (QS tile).
The rest (`LauncherProvider`, `SettingsActivity`, `AddItemActivity`, the shortcut and session receivers,
`WallpaperPickerActivity`) come from AOSP.

---

## 5. Launcher3 port

`launcher3/PATCHES.md` lists every change from upstream. The main hooks into our code:
- `Launcher` no longer has the HOME category (the router does). `onResume`/`onPause`/`dispatchTouchEvent` call
  `TjHooks`, and `onCreate` registers its state manager with `TjHooks.watchState`.
- `AppFilter.shouldShowApp` → `HiddenApps`. Install and promise icons skip apps that are not allowed.
- `BaseDraggingActivity.startActivitySafely` → `RecentsPolicy.apply`.
- `WallpaperOffsetInterpolator` → `TjHooks.onWallpaperOffset`, which the home patch and the curtain need.
- `OptionsPopupView`: Wallpapers only, through `WallpaperPickerChoice`.
- Look: `device_profiles.xml` 5 x 8, `hotseatBarSizePx = 0`, no caret, `QSB_ON_FIRST_SCREEN = false`,
  `allAppsScrimColor #80FFFFFF`. The resource namespace stays `com.android.launcher3`.
- Manifest: no `RECEIVE_BOOT_COMPLETED` (nothing runs at boot), `allowBackup=false`, and authorities on `${packageName}`.

---

## 6. Permissions and the adb grant

Normal permissions: Wi-Fi/Bluetooth state, wallpaper, usage stats (after the user grants it), foreground service,
vibrate, internet (loopback only, for the helper), expand status bar, reorder tasks.
**One adb session** (`SetupActivity`, `tools/adb-all.sh` / `.bat`, the phone guide) grants these:
`WRITE_SECURE_SETTINGS` (tiles, keyboard, status bar, animations, banners, battery saver), `DUMP`, `PACKAGE_USAGE_STATS`,
the accessibility service, and the shell helper's start line. Each feature that needs a grant shows "Needs the adb
permission" in Launcher control and does nothing until the grant is there.

---

## 7. Tests

- **JVM unit tests**: `app/src/test/java/com/tj/launcher/`, 59 classes, one per pure rule class (e.g.
  `ProfileSessionTest`, `UnlockMethodsTest`, `RecentsSweepTest`, `HomeCurtainRulesTest`, `ScrimFitTest`).
  Run with `bash tools/gradle.sh :app:testDebugUnitTest`. Also in `phoneguide/` (`GuideTest`).
- **Emulator scenario**: `tools/emulator_scenario.sh` (~3,900 lines) runs on an API 29 emulator at 600 x 1024,
  170 dpi, font scale 1.2, through `emulator.yml`. It is split into named sections. The **core** set is
  `base,n,o,ac,e1,x1,rb,last`: lock safety (never locked out, never a crash loop, never a factory reset). Optional
  sections (`hp`, `cu`, `pt`, `nq`, `an`, `cs`, ...) run only when the change touches their files.
  `tools/gate-map.txt` lists them, and `bash tools/gate.sh <base-rev>` plans the parallel runs for a diff.
- **Shell self-tests**: `tools/test_handoff.sh` (hooks, workflows parse, no push triggers, briefing size),
  `test_tablet_intake.sh`, `test_cover_resumes.sh`, `test_pen_sample.sh`, `test_uinode.sh`.
- **Tablet checklists**: `research/TABLET_CHECKLIST_vX.Y.Z.md`. Only Tj's tablet can confirm some behaviour.

---

## 8. Build, CI and release

```bash
bash tools/install-hooks.sh                      # first thing every session
bash tools/gradle.sh :app:testDebugUnitTest      # tests (installs the SDK on first use)
bash tools/gradle.sh :app:lintDebug              # lint
bash tools/ckpt.sh "what I did" "what is next"   # checkpoint after every step
bash tools/gate.sh <last-release-commit>         # which emulator sections this change needs
bash ship.sh "note"                              # full local gate + push
```
- Never call `./gradlew` directly. `tools/gradle.sh` sets up the SDK and refuses a release build without the right keystore.
- **`android.yml`** (manual dispatch / tags only, never on push): gates, unit tests, lint, release build, signature
  check against the pinned fingerprint, then a GitHub Release with `Launcher-vX.apk` and `PhoneGuide-vX.apk`.
  `tools/record-release.sh` writes the line in `BUILDLOG.md` once the run is green.
- **`emulator.yml`**: takes a `sections` list and prints a compact `RESULTS` block at the end of the log.
- **`ci.yml`**: tests + lint on pull requests.
- Actions minutes are the scarce resource, so no workflow has a `push:` trigger (`test_handoff.sh` enforces this).

---

## 9. Tooling and the session handoff system

The project is built by many Claude Code sessions that can be cut off at any moment, so the tooling saves state
continuously:

| Script | Role |
| :- | :- |
| `tools/install-hooks.sh` | Installs the hooks below into the session settings (merges, never overwrites). |
| `tools/capture_inbox.sh` / `capture_outbox.sh` | Saves every message from Tj (`INBOX.md`) and every substantial reply (`OUTBOX.md`), with keys redacted. |
| `tools/autosave.sh` | After each tool call: secret scan (`secretscan.sh`), commit, push. |
| `tools/ckpt.sh` | Deliberate checkpoint: rewrites `CHECKPOINT.md` (did / next), runs the fast checks, pushes. |
| `tools/resume.sh` | Session-start briefing: sync state, interrupted-change warning, checkpoint, open tasks. |
| `tools/push.sh`, `MIRROR_MAIN` | Pushes the session branch and fast-forwards `main`. |
| `ship.sh`, `tools/record-release.sh`, `tools/verify-apk.sh`, `tools/checkkeystore.sh` | Release gate and record. |
| `tools/emulator_scenario.sh`, `gate.sh`, `gate-map.txt` | Emulator scenario and tiered gate. |
| `tools/adb-*.sh`, `adb-all.bat` | Grant, set up, probe and restore the tablet over adb. |
| `tools/tablet-intake.sh`, `tablet_intake.py` | Takes in a tablet report and redacts serial, IMEI, MAC, IP, e-mail and Wi-Fi names. |
| `tools/photo_rectify.py`, `pt_analyze.py`, `pb_probe.py`, `pb_ui.py`, `uinode.py`, `patch_photo_step.py` | Measurement: rectify phone photos, analyse films of the patch fade, probe the panel shadow, read UI dumps. |
| `tools/fetch_launcher3.sh` | Fetches upstream AOSP to diff against `launcher3/`. |

---

## 10. Safety rules

These come from `research/SAFETY_2026-11-05.md` and override every feature:
- Nothing may ever need a factory reset. Every Android setting is snapshotted before it is changed, only the values
  the app wrote itself are undone, and **Restore everything** puts all of them back.
- Nothing runs at boot. The shell helper dies at reboot.
- A plain PIN always reaches the custom launcher, and Quickstep is always reachable through a failsafe.
- The crash breaker ends any crash loop by falling back to stock.
- No secrets in the repo except the deliberately committed signing key. Tj's photos never enter the repo (it is public).

---

## 11. Where to read more
- `HANDOFF.md`: one-page onboarding, the architecture table, decisions already made, pitfalls.
- `BRIEF.md`: platform decisions and build traps.
- `TASKS.md`: every request in Tj's words, with its status and the test that proves it.
- `research/`: one note per design (e.g. `PROFILES_2026-11-04.md`, `HOME_PATCH_2026-11-08.md`,
  `BL1_HOME_CURTAIN_2026-11-08.md`, `NOTIFICATIONS_QUIET_2026-11-08.md`, `ANIMATIONS_2026-11-08.md`).
- `launcher3/PATCHES.md`, `wallpaperpicker/PATCHES.md`: every change from AOSP.
