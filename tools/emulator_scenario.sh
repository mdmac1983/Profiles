#!/usr/bin/env bash
# emulator_scenario.sh — runs INSIDE the CI emulator (adb already connected). Screenshots land in emulator-out/.
# Not for local use. Keep each step tolerant: a failed step must not hide the screenshots of the others.
set -uo pipefail
OUT=emulator-out; mkdir -p "$OUT"
APK=app/build/outputs/apk/debug/app-debug.apk
PKG=com.tj.launcher
shot(){ adb exec-out screencap -p > "$OUT/$1.png" 2>/dev/null; }
adb shell wm size 600x1024; adb shell wm density 160; sleep 2
adb install -r "$APK" 2>&1 | tee "$OUT/install.txt"
adb shell dumpsys package "$PKG" | grep -E 'versionName|targetSdk|declared permissions' -A3 > "$OUT/package.txt" 2>&1
# set as the Home app (adb only in CI; the tablet uses Settings > Apps > Default apps > Home app)
adb shell cmd package set-home-activity "$PKG/com.android.launcher3.Launcher" 2>&1 | tee "$OUT/sethome.txt"
adb shell input keyevent KEYCODE_HOME; sleep 6; shot 01-home
adb shell cmd package resolve-activity -a android.intent.action.MAIN -c android.intent.category.HOME 2>&1 | head -5 > "$OUT/home-resolve.txt"
# long-press an empty spot on the workspace -> popup (Home settings / Wallpapers)
adb shell input swipe 300 700 300 700 1200; sleep 2; shot 02-longpress
adb shell input keyevent KEYCODE_BACK; sleep 1
# all apps: swipe up
adb shell input swipe 300 900 300 200 400; sleep 3; shot 03-drawer
adb shell input swipe 300 900 300 400 400; sleep 2; shot 04-drawer-scrolled
adb shell input keyevent KEYCODE_HOME; sleep 2
adb logcat -d -t 400 *:E > "$OUT/logcat-errors.txt" 2>&1
adb logcat -d | grep -iE 'AndroidRuntime|FATAL|launcher3' | tail -60 > "$OUT/logcat-launcher.txt" 2>&1
ls -la "$OUT"
