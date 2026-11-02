#!/usr/bin/env bash
# emulator_scenario.sh — runs INSIDE the CI emulator (adb connected). Screenshots + results land in emulator-out/. Exit != 0 if a SAFETY assertion fails.
# Not for local use. Screenshot steps tolerate failure; the SAFETY checks (marked CHECK) decide the exit code.
set -uo pipefail
# every adb call is bounded: a hung command must cost seconds, not the whole run (an earlier run hung for 15+ minutes)
adb(){ timeout 60 "$(command -v adb)" "$@"; }
export -f adb
OUT=emulator-out; mkdir -p "$OUT"
APK=app/build/outputs/apk/debug/app-debug.apk
PKG=com.tj.launcher
FAILS=0
shot(){ adb exec-out screencap -p > "$OUT/$1.png" 2>/dev/null; }
resumed(){ adb shell dumpsys activity activities | grep -E 'mResumedActivity|ResumedActivity' | head -1; }
check(){ # check "<name>" <command...>
  local name="$1"; shift
  if "$@" >/dev/null 2>&1; then echo "PASS  $name" | tee -a "$OUT/results.txt"; else echo "FAIL  $name" | tee -a "$OUT/results.txt"; FAILS=$((FAILS+1)); fi
}
setmode(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><string name=\\\"mode\\\">$1</string></map>\" > shared_prefs/tj_mode.xml'"; adb shell am force-stop "$PKG"; sleep 1; }
getmode(){ adb shell "run-as $PKG cat shared_prefs/tj_mode.xml 2>/dev/null" | grep -o 'mode">[a-z]*' | cut -d'>' -f2; }
home(){ adb shell input keyevent KEYCODE_HOME; sleep 4; }

adb shell cmd overlay enable com.android.internal.systemui.navbar.threebutton 2>/dev/null; adb shell wm size 600x1024; adb shell wm density 170; adb shell settings put system font_scale 1.2; sleep 2
adb install -r "$APK" 2>&1 | tee "$OUT/install.txt"
adb shell dumpsys package "$PKG" > "$OUT/package-dump.txt" 2>&1

# ---- the manifest must not contain anything that can touch the lock screen / device policy (L10 rule 1)
for bad in BIND_DEVICE_ADMIN SYSTEM_ALERT_WINDOW DISABLE_KEYGUARD RECEIVE_BOOT_COMPLETED WRITE_SECURE_SETTINGS; do
  check "manifest has no $bad" bash -c "! grep -q $bad $OUT/package-dump.txt"
done
check "declares no INSTALL_SHORTCUT permission itself" bash -c "! grep -E 'declared permissions' -A6 $OUT/package-dump.txt | grep -q 'com.android.launcher.permission.INSTALL_SHORTCUT'"

# ---- the router is the HOME app
adb shell cmd package set-home-activity "$PKG/com.tj.launcher.RouterActivity" 2>&1 | tee "$OUT/sethome.txt"
adb shell cmd package resolve-activity -a android.intent.action.MAIN -c android.intent.category.HOME > "$OUT/home-resolve.txt" 2>&1
check "HOME resolves to our router" grep -q 'com.tj.launcher.RouterActivity' "$OUT/home-resolve.txt"

# ---- CUSTOM mode: Home shows our Launcher
setmode custom; home; shot 01-home; resumed | tee "$OUT/resumed-custom.txt"
check "custom mode: our Launcher is on screen" grep -q 'com.tj.launcher/com.android.launcher3.Launcher' "$OUT/resumed-custom.txt"
adb shell input swipe 300 700 300 700 1200; sleep 2; shot 02-longpress; adb shell input keyevent KEYCODE_BACK; sleep 1
adb shell input swipe 300 900 300 200 400; sleep 3; shot 03-drawer
adb shell input keyevent KEYCODE_HOME; sleep 3

# ---- QUICKSTEP mode: Home goes to the stock launcher, and back
setmode quickstep; home; shot 05-quickstep-mode; resumed | tee "$OUT/resumed-quickstep.txt"
check "quickstep mode: stock launcher is on screen" bash -c "grep -q 'com.android.launcher3/' $OUT/resumed-quickstep.txt && ! grep -q 'com.tj.launcher' $OUT/resumed-quickstep.txt"
setmode custom; home; resumed > "$OUT/resumed-custom2.txt"
check "switching back to custom works" grep -q 'com.tj.launcher/com.android.launcher3.Launcher' "$OUT/resumed-custom2.txt"

# ---- crash-loop breaker: 3 crashes in 2 minutes must flip the mode to quickstep by itself
setmode custom; home
adb shell "run-as $PKG rm -f shared_prefs/tj_crash.xml"; adb shell am force-stop "$PKG"; sleep 1; home
for i in 1 2 3; do
  adb shell am crash "$PKG"; sleep 5
  adb shell "run-as $PKG cat shared_prefs/tj_crash.xml" > "$OUT/crash-prefs-$i.txt" 2>&1
  adb shell input keyevent KEYCODE_HOME; sleep 4     # restarts the launcher process for the next crash, dismisses a crash dialog
done
sleep 3; echo "mode after 3 crashes: $(getmode)" | tee "$OUT/crash-mode.txt"
check "crash breaker switched to quickstep" bash -c "[ \"$(getmode)\" = quickstep ]"
home; resumed > "$OUT/resumed-after-crashes.txt"
check "after the breaker, Home shows the stock launcher" bash -c "grep -q 'com.android.launcher3/' $OUT/resumed-after-crashes.txt && ! grep -q 'com.tj.launcher' $OUT/resumed-after-crashes.txt"

# ---- the control icon opens (reachable from the stock launcher)
adb shell am start -n "$PKG/com.tj.launcher.ControlActivity" >/dev/null 2>&1; sleep 3; shot 06-control

# ---- uninstall must leave a working stock home
adb uninstall "$PKG" > "$OUT/uninstall.txt" 2>&1; sleep 2; home; resumed > "$OUT/resumed-uninstalled.txt"
check "after uninstall, stock launcher is Home" grep -q 'com.android.launcher3/' "$OUT/resumed-uninstalled.txt"


# ---- PHASE 2 (after the safety checks, so it cannot disturb them): power-menu helper on a fresh install
adb install -r "$APK" >/dev/null 2>&1
adb shell cmd package set-home-activity "$PKG/com.tj.launcher.RouterActivity" >/dev/null 2>&1
prefhome(){ { echo "== $1"; adb shell cmd package resolve-activity -a android.intent.action.MAIN -c android.intent.category.HOME | grep -E 'name=|packageName=' | head -2; adb shell dumpsys package preferred-xml 2>/dev/null | grep -iE 'tj|launcher' | head -4; } >> "$OUT/prefhome.txt" 2>&1; }
# ---- power-menu helper (screenshots only: judged by eye + overlay window check)
setpower(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><string name=\\\"power_mode\\\">$1</string></map>\" > shared_prefs/tj_features.xml'"; adb shell am force-stop "$PKG"; sleep 1; }
A11Y="$PKG/com.tj.launcher.TjAccessibilityService"
rebind(){ adb shell settings put secure enabled_accessibility_services ""; sleep 1; adb shell settings put secure enabled_accessibility_services "$A11Y"; adb shell settings put secure accessibility_enabled 1; sleep 3; }
a11ydump(){ { echo "== $1"; adb shell settings get secure enabled_accessibility_services; adb shell dumpsys accessibility | grep -iE 'tj|Bound services|Enabled services' | head -8; resumed; adb shell dumpsys window windows | grep -iE 'Window\{[^}]*(GlobalActions|ActionsDialog|TjA11y|com.tj.launcher)' | head -6; adb logcat -d -s TjA11y:I | tail -8; } >> "$OUT/a11y-diag.txt" 2>&1; }
rebind
prefhome start
setmode custom; home; prefhome after-setmode-home
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 10-power-menu-plain-mode-off; prefhome after-menu
adb shell input keyevent KEYCODE_BACK; sleep 1
setpower hide_screenshot; prefhome after-setpower; rebind; prefhome after-rebind; home; prefhome after-home
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 11-power-menu-hide-screenshot; a11ydump hide
adb shell dumpsys window windows | grep -iE 'Window\{.*(TjA11y|com.tj.launcher)' | head -5 > "$OUT/power-hide-windows.txt" 2>&1
adb shell input keyevent KEYCODE_BACK; sleep 1
setpower block; rebind; home
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 12-power-menu-block; a11ydump block
adb shell input keyevent KEYCODE_BACK; sleep 1
setmode quickstep; setpower hide_screenshot; prefhome after-setpower; rebind; prefhome after-rebind; home; prefhome after-home
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 13-power-menu-quickstep-mode; a11ydump quickstep
adb shell input keyevent KEYCODE_BACK; sleep 1
adb shell settings put secure enabled_accessibility_services ""; adb shell settings put secure accessibility_enabled 0
setpower off; setmode custom

adb logcat -d | grep -iE 'AndroidRuntime|FATAL|TjRouter|TjHooks|TjRestore' | tail -80 > "$OUT/logcat-tj.txt" 2>&1

# ---- LAST STEP: our replacement card must press the REAL "Restart" button (the emulator reboots after this)
setmode custom; setpower hide_screenshot; rebind; home
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 14-card-before-restart
RECT="$(adb logcat -d -s TjA11y:I | grep -o 'bounds=Rect([0-9, -]*)' | tail -1 | grep -o '[0-9]*' | tr '\n' ' ')"
set -- $RECT
if [ "$#" -ge 4 ]; then
  X=$(( ($1 + $3) / 2 )); Y=$(( $2 + ($4 - $2) * 3 / 4 ))
  echo "card rect: $RECT -> tap Restart at $X,$Y" | tee "$OUT/press-restart.txt"
  adb shell input tap $X $Y; sleep 2
  adb logcat -d -s TjA11y:I 2>/dev/null | grep "press" >> "$OUT/press-restart.txt"
else
  echo "no card rect found in log" | tee -a "$OUT/press-restart.txt"
fi
check "card's Restart pressed the real Restart button" grep -q "press 'Restart' -> true" "$OUT/press-restart.txt"
echo "failed checks: $FAILS" | tee -a "$OUT/results.txt"
ls -la "$OUT"
exit "$FAILS"
