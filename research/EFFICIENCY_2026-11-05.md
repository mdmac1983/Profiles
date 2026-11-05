# Efficiency review, 2026-11-05 (AB2)

Tj: *"Review the code for the launcher for efficiency. It should be well coded and optimized for the hardware, armv7 mt8168 2gb ram android 10 go."*

**Hardware to design for:** MediaTek MT8168 (4 x Cortex-A53, 32-bit ARMv7 userland), 2 GB RAM, Android 10 **Go** (low-RAM device: small heaps, an aggressive low-memory killer, slow flash), 600x1024 at 170 dpi. The container cannot measure that tablet; the emulator is x86 and much faster, so the emulator numbers below are *relative* (what a change removes), not what the tablet will show. The honest tablet measurement is the "Last unlock" line in Launcher control after installing v0.7.0.

**Scope:** every class in `com.tj.launcher` (the launcher's own code, the accessibility helper, the shell helper, the reset / profile machinery). The ported AOSP Launcher3 itself was left alone: it already runs the Go-edition paths, `largeHeap` resolves to `false` on this screen size (`config_largeHeap`, `values-sw720dp` is the only `true`), hardware acceleration is on, there is one process, and `allowBackup` is off.

**Method:** classify every piece of work by how often it runs (per accessibility event, per foreground change, per return / lock, periodic, one-off) and fix the ones that run often and cost real time on the main thread or in other apps' UI threads.

## What was found and fixed

| # | Where it ran | Why it hurt a 2 GB Cortex-A53 | Fix | Proof |
| :- | :- | :- | :- | :- |
| 1 | **Every accessibility event of every app** (`TjAccessibilityService.onAccessibilityEvent`) | A game or a scrolling list sends hundreds of *content-changed* events a second. Each one ran the lock / pencil / layer / dirty-tracking checks and then `evaluate()`: two whole-view-tree text searches ("Power off", "Screenshot") in **every** window, including the foreground app's. A tree search makes that app's *own UI thread* walk its views, so the helper slowed the very app Tj was using. | `EventNoise.isNoise` drops a content-changed event that is not from SystemUI at the door (the power menu, the shade and the PIN pad are all SystemUI windows, so such an event cannot matter). `EventNoise.mayHoldPowerMenu` limits the text search to system windows, or only the window titled `ActionsDialog` when it exists; for 300 ms after a window-state / windows-changed event every window is searched as before, so an unusual power menu on another Android build is still found. The pencil search skips app and keyboard windows. | `EventNoiseTest` (9 tests incl. the Android constants); emulator `y2` (power menu cover), `b6` (pencil), `b4`, `t` unchanged |
| 2 | Per event: probe flag, keyguard state, preference handles | `Features.get` -> `getSharedPreferences` (a lock plus a map lookup) and `KeyguardManager.isKeyguardLocked()` (a binder round trip to system_server) on **every** event; the Solitaire-probe flag was read from preferences on every event. | probe flag cached 1 s, keyguard state memoised 250 ms for the per-event early-outs only (screen on/off and unlock broadcasts re-check at once), `SharedPreferences` handles cached in `Features` and `Mode`, the retail-flag read only for SystemUI events. | covered by the same runs; no behaviour change |
| 3 | **Finding the Solitaire app by its label** (`ReturnReset.targetPackage`, `openApp`, `startTargetBehindLock`, `findAppLabel`) | It asked Android for every launchable app and **loaded each app's label from its APK resources**, on the main thread: on every foreground-app change in Quickstep (dirty tracking, hotspot), on every return, at every lock, on every launch and every time Launcher control drew its status line. On a tablet with 100+ launchable activities and slow flash that is a stall of the order of 100-300 ms each time (an estimate for this class of hardware; the emulator logs give its own number). | The answer is cached for 10 minutes in memory (a "not installed" answer only 30 s, and an uninstalled cached app triggers a fresh scan at once: review of 2026-11-05), **remembered across process restarts** in the preferences (and checked to be still installed), and re-checked on a background thread; launches resolve the activities of that **one** package. Only the very first lookup for a label ever scans, on the calling thread. | `ReturnReset` logs `target app ... (full scan of N apps took X ms)` and `openApp: target resolved in Y ms`; emulator `rt`, `pf`, `b4` |
| 4 | Accessibility-list write (`A11YON`) | Not speed: safety. See `SAFETY_2026-11-05.md` section 4. | single-quoted + validated | `ShellProtocolTest` |

## Looked at and found fine (nothing to change)

* **Periodic work: none.** No `AlarmManager`, no `Timer`, no polling loop. Every `postDelayed` is a one-shot, coalesced debounce; the only repeating pollers (20-40 ms ticks) run during a return reset and end at a deadline (2.5-4.5 s). No wake lock anywhere.
* **Sensors:** the accelerometer is registered only while a shake / knock method is selected *and* the screen is on (default: off).
* **Receivers / observers:** dynamic, registered for as long as the accessibility service is bound, removed in `onUnbind` / `onDestroy`; the tile guard is a `ContentObserver` on one setting, rate-limited (`tileHist`).
* **Event types:** the service already subscribes to four types only (`windowStateChanged | windowsChanged | windowContentChanged | viewClicked`); `notificationTimeout` stays `0` on purpose: Android's throttle would delay the *windows-changed* event that makes the power-menu cover appear.
* **Bitmaps:** the lock-screen wallpaper is decoded with `inSampleSize` and released when the keyguard is not showing; the calibration screenshot is a transient 600x1024 int array (2.4 MB) freed at once; the lock "photo" is a crop the size of the patch, not the screen.
* **Keep-alive service:** does nothing (no thread, no timer, no wake lock); its cost is one notification and keeping a process that the accessibility helper already keeps (`oom_score_adj` 100 -> 50, measured in run 79).
* **Shell helper (`ShellServer`):** one JVM per boot on loopback; force-stops go through the activity manager's binder (no new JVM per app); the task list is parsed once per call.

## Considered and deliberately NOT done

* **R8 / minify for the release build.** It would shrink the dex and speed class loading, but this app calls hidden APIs by reflection, the shell helper is started by *class name* from the APK (`app_process ... com.tj.launcher.ShellServer`), and Launcher3 and the accessibility XML name classes as strings. Wrong keep rules would break the helper or the launcher in a way only the tablet shows. The guide's `cmd package compile -m speed` already pre-compiles the code. If wanted later: a separate release, keep rules for those entry points, a full emulator run.
* **`notificationTimeout` > 0** (see above).
* **Rewriting Launcher3 start-up.** It is AOSP's and the "as fast as possible" path (pre-arm behind the lock, keep-alive, compile) already works around it.

## What to measure on the tablet

Launcher control shows "Last unlock" (milliseconds from the unlock to the first window) and the card latency for the power menu. Compare v0.6.0 with v0.7.0 on the same tablet; the expected gain is in the helper's idle cost while another app is in front (item 1) and in every Quickstep window change (item 3).
