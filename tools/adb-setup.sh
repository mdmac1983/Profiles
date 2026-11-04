#!/usr/bin/env bash
# adb-setup.sh — runs the SAFE adb lines from research/ADB_COMMANDS.txt (sections 2, 3, 4, 6) in one go. Every line is reversible (section 9 of that file).
#   bash tools/adb-setup.sh          # grant everything the launcher can use
#   bash tools/adb-setup.sh --check  # only show what is granted
# Nothing here installs software, touches the lock screen or makes the launcher a device admin.
set -u
PKG=com.tj.launcher
adb get-state >/dev/null 2>&1 || { echo "No device found: is USB debugging on and the tablet plugged in? (research/ADB_COMMANDS.txt section 0)"; exit 1; }
if [ "${1:-}" = "--check" ]; then
  adb shell dumpsys package "$PKG" | grep -E "WRITE_SECURE_SETTINGS|READ_EXTERNAL_STORAGE|android.permission.DUMP|CHANGE_CONFIGURATION" | sort -u
  exit 0
fi
run(){ echo "+ $*"; "$@" >/dev/null 2>&1 && echo "  ok" || echo "  skipped (older release without this permission, or already set)"; }
run adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS
run adb shell pm grant "$PKG" android.permission.READ_EXTERNAL_STORAGE
run adb shell dumpsys deviceidle whitelist +"$PKG"
run adb shell cmd appops set "$PKG" RUN_IN_BACKGROUND allow
run adb shell cmd appops set "$PKG" RUN_ANY_IN_BACKGROUND allow
run adb shell pm grant "$PKG" android.permission.DUMP
run adb shell pm grant "$PKG" android.permission.CHANGE_CONFIGURATION
run adb shell cmd appops set "$PKG" GET_USAGE_STATS allow
run adb shell cmd appops set "$PKG" WRITE_SETTINGS allow
echo; echo "Done. Check with: bash tools/adb-setup.sh --check"
