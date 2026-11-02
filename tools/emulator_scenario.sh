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

adb shell cmd overlay enable com.android.internal.systemui.navbar.threebutton 2>/dev/null; adb shell wm size 600x1024; adb shell wm overscan 0,0,0,48; adb shell wm density 170; adb shell settings put system font_scale 1.2; sleep 2
adb install -r "$APK" 2>&1 | tee "$OUT/install.txt"
adb shell dumpsys package "$PKG" > "$OUT/package-dump.txt" 2>&1

# ---- the manifest must not contain anything that can touch the lock screen / device policy (L10 rule 1)
for bad in BIND_DEVICE_ADMIN SYSTEM_ALERT_WINDOW DISABLE_KEYGUARD RECEIVE_BOOT_COMPLETED; do
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

# ---- double-press Home in Quickstep mode returns to the custom launcher (BACK1)
setmode quickstep; home; sleep 2
adb shell "input keyevent KEYCODE_HOME; input keyevent KEYCODE_HOME"; sleep 4; resumed > "$OUT/resumed-doublehome.txt"
check "double Home in Quickstep mode returns to the custom launcher" grep -q 'com.tj.launcher/com.android.launcher3.Launcher' "$OUT/resumed-doublehome.txt"
setmode custom; home

# ---- RESET1: double Home back to custom closes apps, clears notifications, turns Wi-Fi off and opens the chosen app
setfeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"; }
setfeat; setmode quickstep
adb shell cmd notification allow_listener "$PKG/com.android.launcher3.notification.NotificationListener" > "$OUT/allow-listener.txt" 2>&1; sleep 2
adb shell svc wifi enable; sleep 2
for APP in com.android.contacts com.android.gallery3d com.android.email; do adb shell monkey -p $APP -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 2; adb shell input keyevent KEYCODE_HOME; sleep 1; done
for APP in com.android.contacts com.android.gallery3d com.android.email; do echo "$APP $(adb shell pidof $APP | tr -d '\r\n')" >> "$OUT/apps-before.txt"; done
adb shell dumpsys activity processes | grep -E 'Proc #|oom:' | grep -iE 'contacts|gallery3d|email' > "$OUT/oom-before.txt" 2>&1
adb shell cmd notification post -S bigtext -t TjTest tjtag1 "hello from the test" >/dev/null 2>&1
sleep 2
adb shell dumpsys notification --noredact | awk '/Notification List:/{f=1} /mArchive|Archived notifications/{f=0} f' | grep -c tjtag1 > "$OUT/notif-before.txt" 2>&1
home; sleep 2
adb shell "input keyevent KEYCODE_HOME; input keyevent KEYCODE_HOME"; sleep 7
resumed > "$OUT/resumed-reset.txt"
for APP in com.android.contacts com.android.gallery3d com.android.email; do echo "$APP $(adb shell pidof $APP | tr -d '\r\n')" >> "$OUT/apps-after.txt"; done
adb shell dumpsys activity processes | grep -E 'Proc #|oom:' | grep -iE 'contacts|gallery3d|email' > "$OUT/oom-after.txt" 2>&1
adb shell dumpsys notification --noredact | awk '/Notification List:/{f=1} /mArchive|Archived notifications/{f=0} f' | grep -c tjtag1 > "$OUT/notif-after.txt" 2>&1
adb shell settings get global wifi_on > "$OUT/wifi-after.txt" 2>&1
adb logcat -d -s TjReset:I > "$OUT/tjreset-log.txt" 2>&1
check "reset: the chosen app (Calendar) was opened" grep -qi 'calendar' "$OUT/resumed-reset.txt"
check "reset: at least one of the three test apps was closed (killBackgroundProcesses works)" bash -c 'n=0; for a in contacts gallery3d email; do b=$(grep "android.$a " '"$OUT"'/apps-before.txt | cut -d" " -f2); f=$(grep "android.$a " '"$OUT"'/apps-after.txt | cut -d" " -f2); [ -n "$b" ] && [ "$b" != "$f" ] && n=$((n+1)); done; [ $n -ge 1 ]'
check "reset: the test notification was cleared" bash -c "[ \"$(cat $OUT/notif-before.txt | tr -d ' \r\n')\" != 0 ] && [ \"$(cat $OUT/notif-after.txt | tr -d ' \r\n')\" = 0 ]"
check "reset: Wi-Fi was turned off" bash -c "[ \"$(cat $OUT/wifi-after.txt | tr -d ' \r\n')\" = 0 ]"
adb shell svc wifi enable >/dev/null 2>&1
setmode custom; home

# ---- HIDE1 / LOCK1: hidden apps (drawer + search), new installs hidden, settings unreachable in custom mode
ALLOW="com.android.contacts com.android.gallery3d com.android.camera2 com.android.deskclock com.android.documentsui com.android.messaging com.android.dialer com.android.quicksearchbox com.android.settings com.android.webview"
SET=""; for a in $ALLOW; do SET="$SET<string>$a</string>"; done
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"baseline_done\\\" value=\\\"true\\\" /><set name=\\\"allowed\\\">$SET</set></map>\" > shared_prefs/tj_apps.xml'"
setmode custom; home; sleep 2
adb shell input swipe 300 900 300 200 400; sleep 3; shot 20-hidden-drawer
adb shell input tap 300 60; sleep 2; adb shell input text cal; sleep 2; shot 21-search-hidden-app
adb shell input keyevent KEYCODE_BACK; adb shell input keyevent KEYCODE_BACK; sleep 1
home
adb shell "run-as $PKG cat shared_prefs/tj_apps.xml" > "$OUT/apps-prefs-before.txt" 2>&1
adb shell pm uninstall -k --user 0 com.android.gallery3d > "$OUT/gallery-uninstall.txt" 2>&1; sleep 3
adb shell pm install-existing --user 0 com.android.gallery3d >> "$OUT/gallery-uninstall.txt" 2>&1; sleep 4
adb shell "run-as $PKG cat shared_prefs/tj_apps.xml" > "$OUT/apps-prefs-after.txt" 2>&1
check "hidden: contacts allowed before, gallery allowed before" bash -c "grep -q gallery3d $OUT/apps-prefs-before.txt && grep -q contacts $OUT/apps-prefs-before.txt"
check "hidden: a REINSTALLED app is forgotten, so it is hidden until Tj allows it" bash -c "! grep -q gallery3d $OUT/apps-prefs-after.txt && grep -q contacts $OUT/apps-prefs-after.txt"
adb shell input swipe 300 900 300 200 400; sleep 3; shot 22-drawer-after-reinstall
adb shell input keyevent KEYCODE_HOME; sleep 2
# LOCK1: Launcher control must not open while the custom launcher is on
setmode custom; home
adb shell am start -n "$PKG/com.tj.launcher.ControlActivity" >/dev/null 2>&1; sleep 3; resumed > "$OUT/resumed-control-in-custom.txt"
check "lock: Launcher control does not open in custom mode" bash -c "! grep -q ControlActivity $OUT/resumed-control-in-custom.txt"
adb shell pm install-existing --user 0 com.android.gallery3d >/dev/null 2>&1
home

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
adb shell input keyevent --longpress KEYCODE_POWER; sleep 0.3; shot 15-card-immediately; sleep 2.7; shot 11-power-menu-hide-screenshot; a11ydump hide
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

# ---- PWR2 research: which hardware keys reach an accessibility service? (debug build only)
rebind; setmode custom; home
adb shell input keyevent KEYCODE_VOLUME_UP; sleep 1
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; adb shell input keyevent KEYCODE_BACK; sleep 1
adb shell input keyevent KEYCODE_POWER; sleep 2; adb shell input keyevent KEYCODE_WAKEUP; adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 2
adb logcat -d -s TjA11y:I | grep -E 'KEY code' > "$OUT/key-diag.txt" 2>&1
# ---- REC1: apps launched from the custom launcher stay out of Recents; the target app (Clock here) stays in.
#      Uses apps NOT launched earlier in this run (a task that already exists keeps its old Recents entry: the flag only applies when a task is created).
setlabel(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">$1</string></map>\" > shared_prefs/tj_features.xml'"; }
setlabel Clock; setmode custom; home; sleep 2
adb shell input swipe 300 900 300 200 400; sleep 3; adb shell input tap 300 255; sleep 4; shot 32-launched-messaging; adb shell input keyevent KEYCODE_HOME; sleep 2     # Messaging (row 2, col 3)
adb shell input swipe 300 900 300 200 400; sleep 3; adb shell input tap 300 135; sleep 4; shot 33-launched-clock; adb shell input keyevent KEYCODE_HOME; sleep 2         # Clock = target (row 1, col 3)
adb shell dumpsys activity recents > "$OUT/recents-policy.txt" 2>&1
adb shell input keyevent KEYCODE_APP_SWITCH; sleep 3; shot 31-recents-only-target; adb shell input keyevent KEYCODE_HOME; sleep 1
check "recents: the target app (Clock) is in Recents" grep -qiE 'clock' "$OUT/recents-policy.txt"
check "recents: an ordinary app launched from the launcher (Messaging) is NOT in Recents" bash -c "! grep -qi 'messaging' $OUT/recents-policy.txt"
check "recents: our own launcher is NOT in Recents (V4)" bash -c "! grep -q 'com.tj.launcher' $OUT/recents-policy.txt"

# ---- RESET2: accessibility helper ON, notification access OFF (like the Go tablet): double Home presses "Clear all" in the real shade and Recents, closes apps, then opens the app
info(){ echo "INFO  $1" | tee -a "$OUT/results.txt"; }
setfeat; setmode quickstep; rebind
for APP in com.android.contacts com.android.gallery3d; do adb shell monkey -p $APP -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 2; adb shell input keyevent KEYCODE_HOME; sleep 1; done
adb shell dumpsys activity recents | grep -cE 'Recent #' > "$OUT/recents-before.txt" 2>&1
adb shell cmd notification post -S bigtext -t TjTest tjtag2 "second test" >/dev/null 2>&1; sleep 2
adb shell dumpsys notification --noredact | awk '/Notification List:/{f=1} /mArchive|Archived notifications/{f=0} f' | grep -c tjtag2 > "$OUT/notif2-before.txt" 2>&1
home; sleep 1
adb shell "input keyevent KEYCODE_HOME; input keyevent KEYCODE_HOME"; sleep 14
shot 30-after-reset2; resumed > "$OUT/resumed-reset2.txt"
adb shell dumpsys notification --noredact | awk '/Notification List:/{f=1} /mArchive|Archived notifications/{f=0} f' | grep -c tjtag2 > "$OUT/notif2-after.txt" 2>&1
adb shell dumpsys activity recents | grep -cE 'Recent #' > "$OUT/recents-after.txt" 2>&1
for APP in com.android.contacts com.android.gallery3d; do echo "$APP $(adb shell pidof $APP | tr -d '\r\n')" >> "$OUT/apps2-after.txt"; done
adb logcat -d -s TjA11y:I | grep RESET > "$OUT/reset2-log.txt" 2>&1
check "reset2: the chosen app (Calendar) was opened after the accessibility reset" grep -qi 'calendar' "$OUT/resumed-reset2.txt"
check "reset2: the notification was cleared through the real shade (no notification access)" bash -c "[ \"$(tr -d ' \r\n' < $OUT/notif2-before.txt)\" != 0 ] && [ \"$(tr -d ' \r\n' < $OUT/notif2-after.txt)\" = 0 ]"
info "recents entries before=$(tr -d ' \r\n' < $OUT/recents-before.txt) after=$(tr -d ' \r\n' < $OUT/recents-after.txt); apps still running: $(cat $OUT/apps2-after.txt | tr '\n' ';')"
adb shell svc wifi enable >/dev/null 2>&1

# ---- V5 + V2: Solitaire (Clock here) opens when the custom launcher STARTS; keyboard switch applies without error once granted
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS > "$OUT/grant-secure.txt" 2>&1
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"true\\\" /><boolean name=\\\"keyboard_switch\\\" value=\\\"true\\\" /><string name=\\\"return_app_label\\\">Clock</string></map>\" > shared_prefs/tj_features.xml'"
adb shell settings get secure default_input_method > "$OUT/ime-before.txt" 2>&1
setmode quickstep; home; sleep 1
adb shell "input keyevent KEYCODE_HOME; input keyevent KEYCODE_HOME"; sleep 6        # Mode.set(CUSTOM) runs inside the app -> KeyboardSwitch.apply
adb shell settings get secure default_input_method > "$OUT/ime-after-custom.txt" 2>&1
adb shell am force-stop "$PKG"; sleep 1; adb shell input keyevent KEYCODE_HOME; sleep 6; resumed > "$OUT/resumed-startup.txt"
check "startup: the target app (Clock) is in front when the custom launcher starts" grep -qi 'clock' "$OUT/resumed-startup.txt"
adb logcat -d -s TjKeyboard:I > "$OUT/keyboard-log.txt" 2>&1
info "keyboard: before=$(tr -d '\r\n' < $OUT/ime-before.txt) afterCustom=$(tr -d '\r\n' < $OUT/ime-after-custom.txt); $(tail -1 $OUT/keyboard-log.txt | cut -c1-160)"
setlabel Clock; home

# ---- LAST STEP: our replacement card must press the REAL "Restart" button (the emulator reboots after this)
setmode custom; setpower hide_screenshot; rebind; home
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 14-card-before-restart
RECT="$(adb logcat -d -s TjA11y:I | grep -o 'bounds=Rect([0-9, -]*)' | tail -1 | grep -o '[0-9]*' | tr '\n' ' ')"
set -- $RECT
if [ "$#" -ge 4 ]; then
  X=$(( ($1 + $3) / 2 )); Y=$(( $2 + ($4 - $2) * 3 / 4 ))
  echo "card rect: $RECT -> tap Restart at $X,$Y" | tee "$OUT/press-restart.txt"
  adb shell dumpsys window windows | grep -iE 'Window\{[^}]*(TjA11y|com.tj.launcher|ActionsDialog)' -A0 | head -6 >> "$OUT/press-restart.txt"
  adb shell input tap $X $Y; sleep 2
  adb logcat -d -s TjA11y:I > "$OUT/tja11y-full.txt" 2>&1
  grep "press" "$OUT/tja11y-full.txt" >> "$OUT/press-restart.txt"
  adb shell dumpsys input 2>/dev/null | grep -iE 'TjA11y|com.tj.launcher|ActionsDialog' | head -8 >> "$OUT/press-restart.txt"
else
  echo "no card rect found in log" | tee -a "$OUT/press-restart.txt"
fi
check "card's Restart pressed the real Restart button" grep -q "press 'Restart' -> true" "$OUT/press-restart.txt"
echo "failed checks: $FAILS" | tee -a "$OUT/results.txt"
ls -la "$OUT"
exit "$FAILS"
