# One cable line for Claude: the lock-screen power menu, exactly (AC5 / AC6, 2026-11-05)

Why: the lock-screen cover (wallpaper patch + Power off / Restart card) cannot be made pixel-exact from the code alone. The shell helper is unreachable on this tablet, so the app has no photo of the lock screen and uses a model. A few real screenshots of the real lock screen (menu closed, menu open) plus the system's own numbers for the menu window are enough for Claude to compute the exact size, position and dimming and to ship them as defaults. **Read-only: it only takes screenshots and prints system facts into one folder. Nothing is changed, nothing to undo** (delete the folder `Downloads/launcher-probe/lock` afterwards if you like).

Paste this ONE line into the phone's ADB shell (bare form, no `adb shell` in front), with the tablet connected:

```
d=/sdcard/Download/launcher-probe/lock; mkdir -p $d; (wm size; wm density; settings get system font_scale; settings get global low_power; settings get global low_power_sticky; settings get global low_power_trigger_level; getprop ro.build.version.release; dumpsys window policy | grep -iE "isShowing|occluded|keyguard" | head -8) > $d/info.txt 2>&1; i=0; while [ $i -lt 18 ]; do i=$((i+1)); screencap -p $d/s$i.png; dumpsys window windows 2>/dev/null | grep -A22 -E "Window #[0-9]+ Window.* (ActionsDialog|GlobalActions)" > $d/w$i.txt; sleep 2; done; ls -l $d | head -60
```

It runs for about 70 seconds. While it runs, on the TABLET:
1. Right away press the side key once (the tablet locks), press it again (the lock screen shows). Leave the lock screen alone for about 15 seconds, no PIN pad pulled up.
2. Hold the side key until the power menu appears (the stock one, or ours if the custom launcher is active and the cover is on). Leave it open for about 15 seconds. Do not touch anything.
3. Close the menu (tap outside it), then wait for the line to print the file list. Unlock as usual.

Then send Claude the folder `Downloads/launcher-probe/lock` (zip it in the Files app, or share the folder). Claude reads it with `tools/tablet-intake.sh` (screenshots are kept locally, never committed by hand).

Best result: do it TWICE, once with the custom launcher active and the lock-screen cover on (what you normally see), once after switching to Quickstep (the pure stock menu on the lock screen). If you only have time for one, do the custom one.

## Speed facts (optional second line, also read-only)
```
d=/sdcard/Download/launcher-probe; mkdir -p $d; for c in 'dumpsys package com.tj.launcher | grep -iE "versionName|versionCode|dexopt|status=|lastUpdateTime"' 'dumpsys meminfo com.tj.launcher | grep -E "TOTAL|Java Heap|Native Heap|Graphics"' 'dumpsys gfxinfo com.tj.launcher | grep -E "Total frames|Janky|50th|90th|95th|99th"' 'dumpsys battery | grep -E "level|powered|status"' 'cmd appops get com.tj.launcher RUN_ANY_IN_BACKGROUND' 'dumpsys deviceidle whitelist | grep -c tj.launcher'; do echo "## $c"; sh -c "$c" 2>&1; done > $d/speed-probe.txt; ls -l $d/speed-probe.txt
```

## What helps most, in order, once the cable is back
1. The lock-menu line above (decides AC6: exact card, patch brightness, then the permanent cover).
2. After the v0.7.2 / v0.7.3 update: `cmd package compile -m speed -f com.tj.launcher` (an app update throws the compiled code away; this is the only speed step an update loses). Solitaire too if you keep it opening automatically: `cmd package compile -m speed -f <its package>` (Claude will give the exact package from the report).
3. The 6-probe line saved in TASKS.md (REAL Recents + notification record formats, the launcher's process priority, quick-settings tiles, the side-key device): it makes the clean-up parsers and "keep the launcher in memory" provable on the real tablet.
