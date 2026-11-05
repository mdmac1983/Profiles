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
info(){ echo "INFO  $1" | tee -a "$OUT/results.txt"; }
setmode(){ heal; adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><string name=\\\"mode\\\">$1</string></map>\" > shared_prefs/tj_mode.xml'"; adb shell am force-stop "$PKG"; sleep 1; }
getmode(){ adb shell "run-as $PKG cat shared_prefs/tj_mode.xml 2>/dev/null" | grep -o 'mode">[a-z]*' | cut -d'>' -f2; }
# Native (SIGSEGV) crash of OUR process inside Android's own libraries (libz / libhwui heap faults seen on this emulator in full runs 59 and 60, never a Java exception):
# Android then clears our default-home setting (AppErrors.clearPackagePreferredForHomeActivities, the accepted cost of the B1 HOME-type launch) and the accessibility helper
# restarts late, which made EVERY later check fail. heal() notices such a crash, says so in results.txt (never silently) and puts the default home + helper back.
CRASHSEEN=0
ensurehome(){ adb shell cmd package set-home-activity "$PKG/com.tj.launcher.RouterActivity" >/dev/null 2>&1; }   # section-only runs and the crash-breaker block can leave no default home (the "Choose a Home app" chooser, run 66)
heal(){ local n; n=$(command adb logcat -d -b all 2>/dev/null | grep -c "Fatal signal.*($PKG)")
  if [ "${n:-0}" -gt "$CRASHSEEN" ]; then CRASHSEEN=$n
    echo "INFO  NATIVE CRASH #$n of $PKG inside an Android system library (not a Java exception); re-asserting the default home and the helper so the rest of the run is meaningful. Checks that FAIL within ~15 s after this line are caused by the crash." | tee -a "$OUT/results.txt"
    command adb logcat -d -b all 2>/dev/null | grep -A14 "Fatal signal.*($PKG)" | tail -30 > "$OUT/native-crash-$n.txt"
    adb shell cmd package set-home-activity "$PKG/com.tj.launcher.RouterActivity" >/dev/null 2>&1
    declare -F rebind >/dev/null && rebind
  fi; }
home(){ heal; adb shell input keyevent KEYCODE_HOME; sleep 4; }

# stream a filtered logcat to the RUNNER (survives an emulator crash, which has cost us the evidence before) + a heartbeat after each phase
( command adb logcat -v time '*:W' TjA11y:I TjReset:I TjRouter:I TjHooks:I TjRecentsGuard:I > "$OUT/logcat-stream.txt" 2>&1 & ) 2>/dev/null   # `command adb`: the real binary, NOT our 60 s timeout wrapper
beat(){ echo "$(date +%T) $1 :: $(adb get-state 2>&1 | tr -d '\r') | mem: $(free -m | awk 'NR==2{print "used="$3" free="$4" avail="$7}') | qemu procs: $(pgrep -fc qemu-system 2>/dev/null) | oom: $(sudo dmesg 2>/dev/null | grep -ciE 'out of memory|killed process')" >> "$OUT/heartbeat.txt"; }
beat start
adb shell cmd overlay enable com.android.internal.systemui.navbar.threebutton 2>/dev/null; adb shell wm size 600x1024; adb shell wm overscan 0,0,0,48; adb shell wm density 170; adb shell settings put system font_scale 1.2; sleep 2
adb install -r "$APK" 2>&1 | tee "$OUT/install.txt"
adb shell dumpsys package "$PKG" > "$OUT/package-dump.txt" 2>&1
# C2: the permissions for later features are DECLARED (so pm grant works without a reinstall) and nothing risky is declared
for perm in DUMP CHANGE_CONFIGURATION PACKAGE_USAGE_STATS; do
  check "C2: manifest declares android.permission.$perm in advance" grep -q "android.permission.$perm" "$OUT/package-dump.txt"
done
adb shell pm grant "$PKG" android.permission.DUMP > "$OUT/grant-dump.txt" 2>&1
adb shell dumpsys package "$PKG" | grep -E "android.permission.DUMP" > "$OUT/dump-granted.txt" 2>&1
check "C2: adb pm grant DUMP works and shows granted=true" bash -c "! grep -qi 'exception\|error' $OUT/grant-dump.txt && grep -q 'granted=true' $OUT/dump-granted.txt"

# ---- SECTIONS: the Emulator check can run only some parts (N9, Tj 2026-11-04: "make the test runs fast").
#      SECTIONS="n,n6" bash tools/emulator_scenario.sh   -> only those sections (~4-5 min instead of ~15);  empty = EVERYTHING (the release run).
#      Names: base legacy x1 e1 y2 b6 b4 n n6 o t u s p x v g rs last. Setup, the install and the helper functions below always run.
SECTIONS="${SECTIONS:-}"
want(){ [ -z "$SECTIONS" ] && return 0; case ",$SECTIONS," in *",$1,"*) return 0;; esac; return 1; }
info "sections: ${SECTIONS:-ALL}"
# helpers that more than one section uses (hoisted here so a section-only run has them)
# tap an app icon in the OPEN drawer by its label (found with uiautomator), falling back to fixed coordinates: the drawer order is not the same on every emulator boot
tapapp(){ local l="$1" fx="$2" fy="$3" b; adb shell uiautomator dump /sdcard/tjui.xml >/dev/null 2>&1
  b=$(adb shell cat /sdcard/tjui.xml 2>/dev/null | tr '>' '\n' | grep -F "text=\"$l\"" | grep -o 'bounds="[^"]*"' | head -1 | grep -o '[0-9]\+' | tr '\n' ' ')
  set -- $b
  if [ "$#" -ge 4 ]; then adb shell input tap $(( ($1+$3)/2 )) $(( ($2+$4)/2 )); else adb shell input tap "$fx" "$fy"; fi; }
setfeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"; }
# vfeat <lock_patch true|false> [skew]: feature file for the lock-patch emulator tests (sections v and x; defined up here so a section-only run has it)
vfeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"allow_setup_autorun\\\" value=\\\"true\\\" /><boolean name=\\\"lock_patch\\\" value=\\\"$1\\\" /><int name=\\\"patch_test_skew\\\" value=\\\"${2:-0}\\\" /><string name=\\\"power_mode\\\">hide_screenshot</string></map>\" > shared_prefs/tj_features.xml'"; }
pad(){ # keys: 1 2 3 4 d(delete) e(enter) on the emulator's 600x1024 PIN pad
  for k in "$@"; do case $k in 1) adb shell input tap 177 628;; 2) adb shell input tap 300 628;; 3) adb shell input tap 424 628;; 4) adb shell input tap 177 714;; d) adb shell input tap 177 886;; e) adb shell input tap 424 886;; esac; sleep 0.4; done; }
lockpad(){ adb shell input keyevent KEYCODE_POWER; sleep 2; adb shell input keyevent KEYCODE_WAKEUP; sleep 3; adb shell input swipe 300 900 300 300 300; sleep 2; }
if want base; then # section base
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
# C5: the STOCK launcher started by our router is a HOME-type task too (an explicit component made it an ordinary task that Recents listed as "Quickstep")
adb shell dumpsys activity activities > "$OUT/activities-quickstep.txt" 2>&1
check "C5: the stock launcher task started by the router is in the HOME-type stack" bash -c "awk '/Stack #/{h=(\$0 ~ /type=home/)} h' $OUT/activities-quickstep.txt | grep -q 'com.android.launcher3/'"
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

# ---- crash-loop breaker: 3 crashes in 2 minutes must flip the mode to quickstep by itself.
#      Runs with the home-type switch OFF: with it ON (the default) Android itself clears the default home on the FIRST crash (AppErrors.clearPackagePreferredForHomeActivities), which is a different, known behaviour.
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"home_type_launcher\\\" value=\\\"false\\\" /></map>\" > shared_prefs/tj_features.xml'"
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


fi # section base
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
if want legacy; then # section legacy
setmode custom; home; prefhome after-setmode-home
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 10-power-menu-plain-mode-off; prefhome after-menu
adb shell input keyevent KEYCODE_BACK; sleep 1
setpower hide_screenshot; prefhome after-setpower; rebind; prefhome after-rebind; home; prefhome after-home
adb shell input keyevent --longpress KEYCODE_POWER; sleep 0.3; shot 15-card-immediately; sleep 2.7; shot 11-power-menu-hide-screenshot; a11ydump hide
adb shell dumpsys window windows | grep -iE 'Window\{.*(TjA11y|com.tj.launcher)' | head -5 > "$OUT/power-hide-windows.txt" 2>&1
adb shell input keyevent KEYCODE_BACK; sleep 1; adb shell dumpsys window windows | grep -c 'type=2032' > "$OUT/warm-overlay-count.txt" 2>&1; info "W3/W4: accessibility-overlay windows still attached after the power menu closed (pre-built card): $(tr -d ' \r\n' < $OUT/warm-overlay-count.txt)"
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
adb logcat -d -s TjA11y:I | grep -E 'POWER key down' > "$OUT/key-diag.txt" 2>&1
# ---- REC1: apps launched from the custom launcher stay out of Recents; the target app (Clock here) stays in.
#      Uses apps NOT launched earlier in this run (a task that already exists keeps its old Recents entry: the flag only applies when a task is created).
setlabel(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"recents_only_target\\\" value=\\\"true\\\" /><string name=\\\"return_app_label\\\">$1</string></map>\" > shared_prefs/tj_features.xml'"; }
setlabel Clock; setmode custom; home; sleep 2
adb shell input swipe 300 900 300 200 400; sleep 3; tapapp Messaging 300 255; sleep 4; shot 32-launched-messaging; adb shell input keyevent KEYCODE_HOME; sleep 2     # Messaging (row 2, col 3)
adb shell input swipe 300 900 300 200 400; sleep 3; tapapp Clock 300 135; sleep 4; shot 33-launched-clock; adb shell input keyevent KEYCODE_HOME; sleep 2         # Clock = target (row 1, col 3)
adb shell input keyevent KEYCODE_APP_SWITCH; sleep 3; shot 31-recents-only-target
adb shell dumpsys activity recents > "$OUT/recents-policy.txt" 2>&1     # taken while the Recents screen is open: the (excluded) launcher is no longer the top task, so a leak would show here
adb shell input keyevent KEYCODE_HOME; sleep 1
check "recents: the target app (Clock) is in Recents" grep -qiE 'clock' "$OUT/recents-policy.txt"
check "recents: an ordinary app launched from the launcher (Messaging) is NOT in Recents" bash -c "! grep -qi 'messaging' $OUT/recents-policy.txt"
# The raw dump always lists the current top task even when it is excluded, so test the thing that matters: every task of ours carries FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS (0x800000).
ourflags(){ grep -o 'flg=0x[0-9a-f]* [^}]*cmp=com.tj.launcher/' "$1" | grep -o 'flg=0x[0-9a-f]*' | cut -d= -f2; }
V4BAD=0; for f in $(ourflags "$OUT/recents-policy.txt"); do [ $(( f & 0x800000 )) -eq 0 ] && V4BAD=$((V4BAD+1)); done
check "recents: every task of our launcher carries the exclude-from-Recents flag (V4)" test "$V4BAD" -eq 0
# ---- B1: our Launcher is a HOME-type task (never listed in Recents, not even as the most recent task). The switch is OFF by default (a crash then clears the default home), so turn it on here.
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"home_type_launcher\\\" value=\\\"true\\\" /></map>\" > shared_prefs/tj_features.xml'"
setmode custom; home; sleep 2
adb shell dumpsys activity activities > "$OUT/activities-home.txt" 2>&1
check "B1: our Launcher task is in the HOME-type stack" bash -c "awk '/Stack #/{h=(\$0 ~ /type=home/)} h' $OUT/activities-home.txt | grep -q 'com.tj.launcher/com.android.launcher3.Launcher'"
adb shell input keyevent KEYCODE_APP_SWITCH; sleep 3
adb shell dumpsys activity recents > "$OUT/recents-from-home.txt" 2>&1; shot 52-recents-from-custom-home
check "B1: Recents opened from the custom home does not list com.tj.launcher among the visible tasks" bash -c "! sed -n '/Visible recent tasks/,\$p' $OUT/recents-from-home.txt | grep -q com.tj.launcher"
adb shell input keyevent KEYCODE_HOME; sleep 2

beat "before RESET2"
# ---- RESET2: accessibility helper ON, notification access OFF (like the Go tablet): double Home presses "Clear all" in the real shade and Recents, closes apps, then opens the app
info(){ echo "INFO  $1" | tee -a "$OUT/results.txt"; }
setfeat; setmode quickstep; rebind
for APP in com.android.contacts com.android.gallery3d; do adb shell monkey -p $APP -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 2; adb shell input keyevent KEYCODE_HOME; sleep 1; done
adb shell dumpsys activity recents | grep -cE 'Recent #' > "$OUT/recents-before.txt" 2>&1
adb shell cmd notification post -S bigtext -t TjTest tjtag2 "second test" >/dev/null 2>&1; sleep 2
adb shell dumpsys notification --noredact | awk '/Notification List:/{f=1} /mArchive|Archived notifications/{f=0} f' | grep -c tjtag2 > "$OUT/notif2-before.txt" 2>&1
home; sleep 1
beat "reset2: before double Home"
adb shell "input keyevent KEYCODE_HOME; input keyevent KEYCODE_HOME"; sleep 3; beat "reset2: +3s"; sleep 4; beat "reset2: +7s"; sleep 7; beat "reset2: +14s"
shot 30-after-reset2; resumed > "$OUT/resumed-reset2.txt"
adb shell dumpsys notification --noredact | awk '/Notification List:/{f=1} /mArchive|Archived notifications/{f=0} f' | grep -c tjtag2 > "$OUT/notif2-after.txt" 2>&1
adb shell dumpsys activity recents | grep -cE 'Recent #' > "$OUT/recents-after.txt" 2>&1
for APP in com.android.contacts com.android.gallery3d; do echo "$APP $(adb shell pidof $APP | tr -d '\r\n')" >> "$OUT/apps2-after.txt"; done
adb logcat -d -s TjA11y:I | grep -E 'RESET|STRAY|LOCKNUM' > "$OUT/reset2-log.txt" 2>&1
check "reset2: the chosen app (Calendar) was opened after the accessibility reset" grep -qi 'calendar' "$OUT/resumed-reset2.txt"
check "reset2: the notification was cleared through the real shade (no notification access)" bash -c "[ \"$(tr -d ' \r\n' < $OUT/notif2-before.txt)\" != 0 ] && [ \"$(tr -d ' \r\n' < $OUT/notif2-after.txt)\" = 0 ]"
info "recents entries before=$(tr -d ' \r\n' < $OUT/recents-before.txt) after=$(tr -d ' \r\n' < $OUT/recents-after.txt); apps still running: $(cat $OUT/apps2-after.txt | tr '\n' ';')"
adb shell svc wifi enable >/dev/null 2>&1

beat "before V5 + V2"
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

beat "before W5: the stock"
# ---- W5: the stock launcher's home screen must never appear in custom mode unless Tj did the gesture (the helper presses Home); W2: our tasks carry the exclude flag at runtime
STOCK="$(adb shell cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME 2>/dev/null | tr -d '\r' | grep '/' | grep -v com.tj.launcher | head -1)"
echo "stock home component: $STOCK" | tee "$OUT/stock-component.txt"
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /></map>\" > shared_prefs/tj_features.xml'"
setmode custom; rebind; home
adb logcat -c
adb shell am start -a android.intent.action.MAIN -c android.intent.category.HOME -n "$STOCK" >/dev/null 2>&1; sleep 5
resumed > "$OUT/resumed-stray.txt"; shot 40-stray-stock-home
adb logcat -d -s TjA11y:I | grep STRAY > "$OUT/stray-log.txt" 2>&1
if [ -n "$STOCK" ]; then
  check "W5: stock home started without the gesture (custom mode) is sent back to our launcher" grep -q 'com.tj.launcher/com.android.launcher3.Launcher' "$OUT/resumed-stray.txt"
  check "W5: the helper logged the redirect" grep -q STRAY "$OUT/stray-log.txt"
  setmode quickstep; rebind; home
  adb shell am start -a android.intent.action.MAIN -c android.intent.category.HOME -n "$STOCK" >/dev/null 2>&1; sleep 4
  resumed > "$OUT/resumed-quickstep-guard.txt"
  check "W5: in Quickstep mode (after the gesture) the stock home is left alone" bash -c "! grep -q 'com.tj.launcher/com.android.launcher3.Launcher' $OUT/resumed-quickstep-guard.txt"
  setmode custom; home
else
  info "W5: no separate stock home component on this emulator image; guard not exercised"
fi
adb shell dumpsys activity recents > "$OUT/recents-flags.txt" 2>&1
OURS="$(grep -o 'flg=0x[0-9a-f]* [^}]*cmp=com.tj.launcher/[A-Za-z.0-9]*' "$OUT/recents-flags.txt" | head -5)"
BAD=0; for f in $(echo "$OURS" | grep -o 'flg=0x[0-9a-f]*' | cut -d= -f2); do [ $(( f & 0x800000 )) -eq 0 ] && BAD=$((BAD+1)); done
if [ -n "$OURS" ]; then check "W2: every task of ours in the Recents dump carries FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS" test "$BAD" -eq 0; else info "W2: no task of ours listed in the Recents dump (fine: excluded)"; fi

beat "before X1: the numbe"
# ---- W3b: wallpaper patch. With power mode "wallpaper" a permanent patch sits over the power-menu card area and must look IDENTICAL to the wallpaper there (compare with the same screen, patch off).
beat "before W3b"
adb shell pm grant "$PKG" android.permission.READ_EXTERNAL_STORAGE > "$OUT/grant-storage.txt" 2>&1
setmode custom; setpower wallpaper; rebind; home
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 49-wallpaper-mode-menu-open
adb logcat -d -s TjA11y:I | grep -E "POWERSIG|POWERLAT" > "$OUT/power-latency.txt" 2>&1
check "D3: the card latency was measured and stored" grep -q POWERLAT "$OUT/power-latency.txt"
info "D3 latency: $(grep POWERLAT "$OUT/power-latency.txt" | tail -1 | cut -c1-200)"
adb shell input keyevent KEYCODE_BACK; sleep 3
PRECT="$(adb logcat -d -s TjA11y:I | grep -o 'bounds=Rect([0-9, -]*)' | tail -1 | grep -o '[0-9]*' | tr '\n' ' ')"
shot 50-patch-on
adb shell dumpsys window windows | grep -c TjWallpaperPatch > "$OUT/patch-windows.txt" 2>&1
adb logcat -d -s TjA11y:I | grep PATCH > "$OUT/patch-log.txt" 2>&1
setpower off; rebind; home; sleep 2; shot 51-patch-off
adb shell dumpsys window windows | grep -c TjWallpaperPatch > "$OUT/patch-windows-off.txt" 2>&1
set -- $PRECT
if [ "$#" -ge 4 ]; then
  GEOM="$(( $3 - $1 ))x$(( $4 - $2 ))+$1+$2"
  MAE="$(compare -metric MAE -extract "$GEOM" "$OUT/50-patch-on.png" "$OUT/51-patch-off.png" null: 2>&1 | grep -o '([0-9.e+-]*)' | tr -d '()')"
  info "W3b raw compare output: $(compare -metric MAE -extract "$GEOM" "$OUT/50-patch-on.png" "$OUT/51-patch-off.png" null: 2>&1 | head -c 120)"
  echo "patch region $GEOM normalised MAE vs wallpaper-only: $MAE" | tee "$OUT/patch-diff.txt"
  check "W3b: a wallpaper patch window exists while the menu is closed" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/patch-windows.txt)\" -ge 1 ]"
  check "W3b control: the patch window is gone when the mode is off (so the comparison is patch vs no patch)" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/patch-windows-off.txt)\" = 0 ]"
  check "W3b: the patch is identical to the wallpaper underneath (region MAE < 0.5 %)" awk -v m="$MAE" 'BEGIN{exit !(m!="" && m+0 < 0.005)}'
else
  info "W3b: the menu rectangle was not learned (no bounds in the log); patch not compared"
  check "W3b: the menu rectangle was learned" false
fi
setpower off; setmode custom

fi # section legacy
if want x1; then # section x1
ensurehome
# ---- X1: the number on the lock screen shows on the lock screen and goes when the PIN pad is pulled up. The PIN is ALWAYS cleared again below.
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><string name=\\\"power_mode\\\">hide_screenshot</string></map>\" > shared_prefs/tj_features.xml'"
setmode custom; rebind; home
adb shell locksettings set-pin 1234 > "$OUT/setpin.txt" 2>&1
adb shell input keyevent KEYCODE_POWER; sleep 2
adb shell input keyevent KEYCODE_WAKEUP; sleep 3
shot 41-lockscreen
convert "$OUT/41-lockscreen.png" -crop 420x70+90+880 +repage -channel RGB -separate -evaluate-sequence min -threshold 80% -trim -format '%wx%h%O' info: > "$OUT/lock-number-bbox.txt" 2>&1
info "Y3: lock-screen number white-pixel box on the emulator (WxH+x+y of the crop): $(cat $OUT/lock-number-bbox.txt)"
adb logcat -c
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 43-lock-power-menu
adb logcat -d -s TjA11y:I > "$OUT/lock-menu-log.txt" 2>&1
adb shell input keyevent KEYCODE_BACK; sleep 2
check "Y1: the power-menu cover also works on the lock screen" grep -q 'action=1' "$OUT/lock-menu-log.txt"
adb shell dumpsys window windows | grep -c TjLockNumber > "$OUT/lock-shown.txt" 2>&1
adb shell input swipe 300 900 300 300 300; sleep 2
shot 42-lockscreen-pinpad
adb shell dumpsys window windows | grep -c TjLockNumber > "$OUT/lock-pad.txt" 2>&1
adb logcat -d -s TjA11y:I | grep LOCKNUM > "$OUT/locknum-log.txt" 2>&1
adb shell input text 1234; sleep 1; adb shell input keyevent KEYCODE_ENTER; sleep 3
adb shell locksettings clear --old 1234 > "$OUT/clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
check "X1: the PIN used for the test was cleared again" bash -c "! grep -qiE 'fail|error|exception' $OUT/clearpin.txt"
check "X1: the number overlay is on the lock screen" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/lock-shown.txt)\" -ge 1 ]"
check "X1: the number overlay is gone when the PIN pad is pulled up" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/lock-pad.txt)\" = 0 ]"
home
fi # section x1
if want e1; then # section e1
ensurehome
# ---- E1: unlock-time launcher choice (opt-in). Tap Delete twice on the empty PIN pad before the PIN = Quickstep; a plain PIN = custom launcher. The test PIN is ALWAYS cleared at the end.
beat "before E1"
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"unlock_pick\\\" value=\\\"true\\\" /></map>\" > shared_prefs/tj_features.xml'"
setmode custom; rebind; home
adb shell locksettings set-pin 1234 > "$OUT/e1-setpin.txt" 2>&1
pad(){ # keys: 1 2 3 4 d(delete) e(enter) on the emulator's 600x1024 PIN pad
  for k in "$@"; do case $k in 1) adb shell input tap 177 628;; 2) adb shell input tap 300 628;; 3) adb shell input tap 424 628;; 4) adb shell input tap 177 714;; d) adb shell input tap 177 886;; e) adb shell input tap 424 886;; esac; sleep 0.4; done; }
lockpad(){ adb shell input keyevent KEYCODE_POWER; sleep 2; adb shell input keyevent KEYCODE_WAKEUP; sleep 3; adb shell input swipe 300 900 300 300 300; sleep 2; }
adb logcat -c
lockpad; shot 44-e1-pinpad
pad d d 1 2 3 4 e; sleep 6
resumed > "$OUT/e1-resumed-token.txt"; getmode > "$OUT/e1-mode-token.txt"
adb logcat -d -s TjA11y:I | grep UNLOCKPICK > "$OUT/e1-log-token.txt" 2>&1
check "E1: Delete x2 before the PIN unlocks into QUICKSTEP (mode switched)" grep -q quickstep "$OUT/e1-mode-token.txt"
check "E1: and the stock launcher is on screen" grep -q 'com.android.launcher3/' "$OUT/e1-resumed-token.txt"
adb logcat -c
lockpad
pad 1 2 3 4 e; sleep 7
resumed > "$OUT/e1-resumed-plain.txt"; getmode > "$OUT/e1-mode-plain.txt"
adb logcat -d -s TjA11y:I | grep UNLOCKPICK > "$OUT/e1-log-plain.txt" 2>&1
check "E1: a plain PIN returns to the CUSTOM launcher (mode switched back)" grep -q custom "$OUT/e1-mode-plain.txt"
check "E1: and our Launcher is on screen" grep -q 'com.tj.launcher' "$OUT/e1-resumed-plain.txt"
adb shell locksettings clear --old 1234 > "$OUT/e1-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
check "E1: the test PIN was cleared again" bash -c "! grep -qiE 'fail|error|exception' $OUT/e1-clearpin.txt"
home


fi # section e1
if want y2; then # section y2
ensurehome
# ---- Y2: the return reset with the animation speed-up: animations are turned off for the reset and put back exactly as they were
beat "before Y2"
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS > "$OUT/grant-secure2.txt" 2>&1
for n in window_animation_scale transition_animation_scale animator_duration_scale; do adb shell settings put global $n 1.0; done
setcal(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"; }
setcal
setmode quickstep; rebind; home; sleep 1
adb logcat -c
adb shell "input keyevent KEYCODE_HOME; input keyevent KEYCODE_HOME"; sleep 12
adb logcat -d -s TjA11y:I TjAnim:I TjReset:I > "$OUT/y2-log.txt" 2>&1
adb shell settings get global window_animation_scale > "$OUT/anim-after.txt" 2>&1
adb shell settings get global animator_duration_scale >> "$OUT/anim-after.txt" 2>&1
resumed > "$OUT/resumed-y2.txt"
adb shell settings get secure sysui_qs_tiles > "$OUT/qs-tiles-custom.txt" 2>&1
check "D6: back in custom mode the quick-settings tiles are just 'rotation'" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/qs-tiles-custom.txt)\" = rotation ]"
check "Y2b: animations were turned off for the reset" grep -q 'animations off for the reset' "$OUT/y2-log.txt"
check "Y2b: animation scales are back to 1.0 afterwards" bash -c "[ \"\$(sed -n 1p $OUT/anim-after.txt | tr -d ' \r\n')\" = 1.0 ] && [ \"\$(sed -n 2p $OUT/anim-after.txt | tr -d ' \r\n')\" = 1.0 ]"
check "Y2: the app (Calendar) was opened by the faster reset" grep -qi 'calendar' "$OUT/resumed-y2.txt"
info "Y2: $(grep -E 'RESET (recents|notifications)' "$OUT/y2-log.txt" | cut -c1-120 | tr '\n' ';' | cut -c1-320)"
for n in window_animation_scale transition_animation_scale animator_duration_scale; do adb shell settings put global $n 0; done
setmode custom; home

fi # section y2
if want b6; then # section b6
ensurehome
# ---- B6: the quick-settings pencil is covered (and uncovered when the panel closes)
beat "before B6"
setpower off; setmode custom; rebind; home
adb logcat -c
adb shell cmd statusbar expand-settings; sleep 3; shot 53-qs-panel
adb shell dumpsys window windows | grep -c TjPencilCover > "$OUT/pencil-windows.txt" 2>&1
adb logcat -d -s TjA11y:I | grep PENCIL > "$OUT/pencil-log.txt" 2>&1
adb shell cmd statusbar collapse; sleep 2
adb shell dumpsys window windows | grep -c TjPencilCover > "$OUT/pencil-windows-after.txt" 2>&1
adb logcat -d -s TjA11y:I | grep PENCIL > "$OUT/pencil-log2.txt" 2>&1
check "B6: a cover window sits over the pencil while the panel is open" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/pencil-windows.txt)\" -ge 1 ]"
check "B6/D4: when the panel closes the cover is hidden (the window is kept warm for an instant next time)" grep -qE 'PENCIL cover (hidden|removed)' "$OUT/pencil-log2.txt"
fi # section b6
if want b4; then # section b4
ensurehome
# ---- B4: nothing to clear in Recents -> that step is skipped; cover shown and removed in the normal run
check "B4: the black cover was shown and removed during the reset" bash -c "grep -q 'RESET cover shown' $OUT/y2-log.txt && grep -q 'RESET cover removed' $OUT/y2-log.txt"
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"tracked\\\" value=\\\"true\\\" /></map>\" > shared_prefs/tj_origin.xml'"
setcal; setmode quickstep; rebind; home; sleep 1
adb logcat -c
adb shell "input keyevent KEYCODE_HOME; input keyevent KEYCODE_HOME"; sleep 9
adb logcat -d -s TjA11y:I TjReset:I > "$OUT/b4-log.txt" 2>&1
resumed > "$OUT/resumed-b4.txt"
check "B4: with Recents untouched the reset skips the Recents step" grep -q 'recents=false' "$OUT/b4-log.txt"
check "B4: the app (Calendar) is in front after the shorter reset" grep -qi 'calendar' "$OUT/resumed-b4.txt"
setmode custom; home

fi # section b4
if want n; then # section n
ensurehome
# ---- N4 / N5: ways back to custom, the master failsafe, and the lock-screen profile switch (v0.4.0)
beat "before N"
# `adb shell input keyevent` is INJECTED and bypasses the accessibility key filter, so hardware keys are sent through the emulator console (a real input device) instead.
ekey(){ for c in "$@"; do adb emu event send "EV_KEY:$c:1" "EV_SYN:0:0" "EV_KEY:$c:0" "EV_SYN:0:0" >> "$OUT/n-emu-keys.txt" 2>&1; sleep 0.45; done; }
setfeat2(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"unlock_m_taperase\\\" value=\\\"true\\\" /><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"; }
setfeat; setmode quickstep; rebind; home; sleep 2
adb shell am start -n "$PKG/com.tj.launcher.BackToCustomActivity" > "$OUT/n4-icon-start.txt" 2>&1; sleep 5
getmode > "$OUT/n4-icon-mode.txt"
check "N4: the Back-to-custom icon switches Quickstep to custom" grep -q custom "$OUT/n4-icon-mode.txt"
adb logcat -d -s TjA11y:I | grep "key filter" > "$OUT/n5-keyfilter-log.txt" 2>&1
check "N5: the helper asked for the key filter (want=true in Quickstep with the volume code on)" grep -q "key filter want=true" "$OUT/n5-keyfilter-log.txt"
ekey 115 > /dev/null; sleep 1
adb logcat -d -s TjA11y:I | grep " KEY " > "$OUT/n5-key-arrival.txt" 2>&1
info "N4/N5: hardware key events seen by the helper after one console Volume Up: $(wc -l < $OUT/n5-key-arrival.txt) ($(head -c 100 $OUT/n5-key-arrival.txt | tr '\n' ' '))"
if grep -qiE '^KO|error|unknown' "$OUT/n-emu-keys.txt" 2>/dev/null || [ "$(wc -l < $OUT/n5-key-arrival.txt | tr -d ' ')" = 0 ]; then
  info "N4/N5: console key events never reached the helper (the emulator's virtual keyboard bypasses the accessibility key filter: 0 seen, console said: $(head -c 60 $OUT/n-emu-keys.txt | tr '\n' ' ')); the key sequences are proven by KeySeq JVM tests, the hardware path only on the tablet"
else
  setfeat; setmode quickstep; rebind; home; sleep 2
  ekey 114 114 115 115; sleep 5
  getmode > "$OUT/n4-volcode-mode.txt"
  check "N4: the volume code (Down, Down, Up, Up) switches Quickstep to custom" grep -q custom "$OUT/n4-volcode-mode.txt"
  setfeat; setmode custom; rebind; home; sleep 2
  ekey 115 115 114 114 158 158 115 114; sleep 4
  getmode > "$OUT/n5-konami-mode.txt"; resumed > "$OUT/n5-konami-resumed.txt"
  check "N5: the Konami key code switches the custom launcher to Quickstep (master failsafe)" grep -q quickstep "$OUT/n5-konami-mode.txt"
  check "N5: and the stock launcher is on screen" grep -q 'com.android.launcher3/' "$OUT/n5-konami-resumed.txt"
  setfeat; setmode custom; rebind; home; sleep 2
  ekey 115 115 114 114; sleep 2
  getmode > "$OUT/n5-half-mode.txt"
  check "N5: half of the key code does nothing" grep -q custom "$OUT/n5-half-mode.txt"
fi

# N2/N3 on the lock screen: tap-and-erase handshake = Quickstep, the screen wake puts the custom stack behind the keyguard, a plain PIN = custom
setfeat2; setmode custom; rebind; home
adb shell locksettings set-pin 1234 > "$OUT/n2-setpin.txt" 2>&1
adb logcat -c
lockpad; shot 46-n2-pinpad
adb shell dumpsys accessibility 2>/dev/null | grep -iE 'Tj|eventTypes|flags=|Enabled services|Bound services' | head -12 > "$OUT/n2-a11y-state.txt"
adb shell "run-as $PKG cat shared_prefs/tj_features.xml" > "$OUT/n2-features.txt" 2>&1
adb logcat -d -s TjA11y:I | grep PROFILE > "$OUT/n3-prearm-log.txt" 2>&1
adb logcat -d | grep -c "Abort background activity starts" > "$OUT/n3-bal.txt" 2>&1
check "N3: the lock screen (screen off, or the wake) put the custom stack in front behind the keyguard" grep -qE "woke locked|screen off, locked|custom home" "$OUT/n3-prearm-log.txt"
check "N3: Android did not block the helper's start behind the keyguard" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/n3-bal.txt)\" = 0 ]"
pad 1 d 1 d 1 d 1 2 3 4 e; sleep 6
getmode > "$OUT/n2-taperase-mode.txt"; resumed > "$OUT/n2-taperase-resumed.txt"
adb logcat -d -s TjA11y:I | grep -E "UNLOCKPICK|UNLOCKCLICK|PROFILE" > "$OUT/n2-taperase-log.txt" 2>&1
check "N2: tap-and-erase x3 before the PIN unlocks into Quickstep" grep -q quickstep "$OUT/n2-taperase-mode.txt"
check "N2: and the stock launcher is on screen" grep -q 'com.android.launcher3/' "$OUT/n2-taperase-resumed.txt"
lockpad
pad 1 2 3 4 e; sleep 1.5; resumed > "$OUT/n3-plain-1s5.txt"; sleep 5.5
getmode > "$OUT/n3-plain-mode.txt"; resumed > "$OUT/n3-plain-resumed.txt"
info "N3: 1.5 s after the Enter tap of a plain PIN (a Quickstep session ended): $(cat $OUT/n3-plain-1s5.txt)"
check "N3: a plain PIN after a Quickstep session ends it (custom mode)" grep -q custom "$OUT/n3-plain-mode.txt"
check "N3: and Solitaire/our launcher is on screen, not Quickstep" bash -c "grep -qiE 'calendar|com.tj.launcher' $OUT/n3-plain-resumed.txt"
# a CUSTOM-to-CUSTOM unlock: the wake pre-armed the stack, nothing is closed or cleared
lockpad
pad 1 2 3 4 e; sleep 2; resumed > "$OUT/n3-c2c-resumed.txt"
check "N3: a plain unlock from the custom account lands on the custom stack at once" bash -c "grep -qiE 'calendar|com.tj.launcher' $OUT/n3-c2c-resumed.txt"
adb shell locksettings clear --old 1234 > "$OUT/n2-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
check "N2: the test PIN was cleared again" bash -c "! grep -qiE 'fail|error|exception' $OUT/n2-clearpin.txt"
setfeat; setmode custom; home

fi # section n
if want n6; then # section n6
ensurehome
# ---- N6: the invisible clean-up helper (shell-uid process started by one adb line): Quickstep's apps are force-stopped with no cover, their Recents cards vanish
beat "before N6"
setfeat; setmode quickstep; rebind; home; sleep 2
adb shell 'CLASSPATH=$(pm path com.tj.launcher | head -1 | cut -d: -f2) setsid nohup app_process /system/bin com.tj.launcher.ShellServer >/data/local/tmp/tjshell.log 2>&1 &' > "$OUT/n6-start.txt" 2>&1; sleep 5
adb shell cat /data/local/tmp/tjshell.log > "$OUT/n6-server-log.txt" 2>&1
info "N6: the helper's own output: $(tr '\n' ' ' < $OUT/n6-server-log.txt | head -c 300)"
adb shell "ps -A -o ARGS | grep -c '[S]hellServer'" > "$OUT/n6-ps.txt" 2>&1
check "N6: the shell helper process is running (started by the one adb line)" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/n6-ps.txt)\" -ge 1 ]"
for APP in com.android.contacts com.android.gallery3d com.android.email; do adb shell monkey -p $APP -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 2; adb shell input keyevent KEYCODE_HOME; sleep 1; done
adb shell dumpsys activity recents 2>/dev/null | grep -ciE 'contacts|gallery3d|email' > "$OUT/n6-recents-before.txt"
alive(){ [ "$(command adb get-state 2>/dev/null | tr -d '\r\n')" = device ]; }
home; sleep 2
adb logcat -c
adb shell "input keyevent KEYCODE_HOME; input keyevent KEYCODE_HOME"; sleep 8
if ! alive; then info "N6: the EMULATOR DIED right after the double Home (qemu process gone; not a product result): rerun"; echo "emulator died" > "$OUT/n6-log.txt"; fi
adb logcat -d -s TjReset:I TjA11y:I >> "$OUT/n6-log.txt" 2>&1
adb shell dumpsys activity recents 2>/dev/null | grep -ciE 'contacts|gallery3d|email' > "$OUT/n6-recents-after.txt"
resumed > "$OUT/n6-resumed.txt"
info "N6: Recents entries of the three apps: before $(tr -d ' \r\n' < $OUT/n6-recents-before.txt), after $(tr -d ' \r\n' < $OUT/n6-recents-after.txt)"
check "N6: the return used the INVISIBLE path (helper answered, apps force-stopped)" grep -q "INVISIBLE: force-stopped" "$OUT/n6-log.txt"
check "N6: no black cover was raised for it" bash -c "! grep -q 'RESET cover shown' $OUT/n6-log.txt"
check "N6: the Quickstep apps' Recents cards are gone" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/n6-recents-after.txt)\" = 0 ]"
check "N6: the target app (Calendar) is on screen" grep -qi calendar "$OUT/n6-resumed.txt"
adb shell pkill -f com.tj.launcher.ShellServer >/dev/null 2>&1; sleep 1
adb shell "ps -A -o ARGS | grep -c '[S]hellServer'" > "$OUT/n6-ps2.txt" 2>&1
check "N6: the helper stops with the documented line" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/n6-ps2.txt)\" = 0 ]"
setfeat; setmode custom; home

fi # section n6
if want o; then # section o
ensurehome
# ---- O1 / O3 / O4 (v0.5.0): more lock-screen methods, closing at lock, instant unlock to custom
beat "before O"
setfeat3(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"unlock_m_ladder\\\" value=\\\"true\\\" /><boolean name=\\\"unlock_m_charger\\\" value=\\\"true\\\" /><boolean name=\\\"unlock_m_peek5\\\" value=\\\"true\\\" /><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"; }
setfeat3; setmode custom; rebind; home
adb shell locksettings set-pin 1234 > "$OUT/o-setpin.txt" 2>&1
# O1: erase ladder = Delete, digit, Delete, digit, Delete, digit, then the PIN
lockpad
pad d 1 d 1 d 1 1 2 3 4 e; sleep 6
getmode > "$OUT/o-ladder-mode.txt"; resumed > "$OUT/o-ladder-resumed.txt"
check "O1: the erase ladder (Delete, digit x3) before the PIN unlocks into Quickstep" grep -q quickstep "$OUT/o-ladder-mode.txt"
check "O1: and the stock launcher is on screen" grep -q 'com.android.launcher3/' "$OUT/o-ladder-resumed.txt"
# O1: charger flick = plug, unplug, plug on the lock screen
adb shell dumpsys battery set ac 0 >/dev/null 2>&1; adb shell dumpsys battery set usb 0 >/dev/null 2>&1; sleep 1
lockpad
adb shell dumpsys battery set ac 1 >/dev/null 2>&1; sleep 1.2; adb shell dumpsys battery set ac 0 >/dev/null 2>&1; sleep 1.2; adb shell dumpsys battery set ac 1 >/dev/null 2>&1; sleep 1.5
pad 1 2 3 4 e; sleep 6
getmode > "$OUT/o-charger-mode.txt"
check "O1: the charger flick (plug, unplug, plug) before the PIN unlocks into Quickstep" grep -q quickstep "$OUT/o-charger-mode.txt"
adb shell dumpsys battery reset >/dev/null 2>&1
# O1: peek five times = pull the pad up and away five times
lockpad
for i in 1 2 3 4; do adb shell input keyevent KEYCODE_BACK; sleep 1.6; adb shell input swipe 300 900 300 300 300; sleep 1.6; done
pad 1 2 3 4 e; sleep 6
getmode > "$OUT/o-peek5-mode.txt"
check "O1: peeking at the PIN pad five times before the PIN unlocks into Quickstep" grep -q quickstep "$OUT/o-peek5-mode.txt"
# a plain PIN (no handshake) from here must end up in the custom account
lockpad; pad 1 2 3 4 e; sleep 6
getmode > "$OUT/o-plain-mode.txt"
check "O1: a plain PIN is still the custom launcher (no handshake, no accident)" grep -q custom "$OUT/o-plain-mode.txt"

# O4 + O3: with the shell helper, locking from Quickstep closes its apps BEFORE any wake; the unlock then has nothing left to do
adb shell 'CLASSPATH=$(pm path com.tj.launcher | head -1 | cut -d: -f2) setsid nohup app_process /system/bin com.tj.launcher.ShellServer >/data/local/tmp/tjshell.log 2>&1 &' >/dev/null 2>&1; sleep 4
setfeat3; setmode quickstep; rebind; home; sleep 2
for APP in com.android.contacts com.android.gallery3d com.android.email; do adb shell monkey -p $APP -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 2; adb shell input keyevent KEYCODE_HOME; sleep 1; done
adb logcat -c
adb shell input keyevent KEYCODE_POWER; sleep 9          # lock: screen off; nobody wakes it
adb logcat -d -s TjReset:I TjA11y:I > "$OUT/o4-log-before-wake.txt" 2>&1
adb shell dumpsys activity recents 2>/dev/null | grep -ciE 'contacts|gallery3d|email' > "$OUT/o4-recents.txt"
getmode > "$OUT/o4-mode.txt"
check "O4: locking from Quickstep closed its apps AT LOCK (before any wake)" grep -q "AT LOCK: force-stopped" "$OUT/o4-log-before-wake.txt"
check "O4: their Recents cards were gone before the unlock" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/o4-recents.txt)\" = 0 ]"
check "O4: the custom account was already active behind the lock screen" grep -q custom "$OUT/o4-mode.txt"
adb logcat -c
adb shell input keyevent KEYCODE_WAKEUP; sleep 2; adb shell input swipe 300 900 300 300 300; sleep 2
pad 1 2 3 4 e; sleep 1.5
resumed > "$OUT/o3-resumed-1s5.txt"
adb logcat -d -s TjA11y:I > "$OUT/o3-log.txt" 2>&1
info "O3: 1.5 s after the Enter tap: $(cat $OUT/o3-resumed-1s5.txt)"
check "O3: Solitaire (Calendar here) is in front 1.5 s after the Enter tap, nothing left to do at the unlock" bash -c "grep -qi 'calendar' $OUT/o3-resumed-1s5.txt"
check "O3: no black cover and no clean-up work was needed at the unlock" bash -c "! grep -q 'RESET cover shown' $OUT/o3-log.txt"
adb shell pkill -f com.tj.launcher.ShellServer >/dev/null 2>&1
adb shell locksettings clear --old 1234 > "$OUT/o-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
check "O: the test PIN was cleared again" bash -c "! grep -qiE 'fail|error|exception' $OUT/o-clearpin.txt"
# O2: the hidden-apps chooser with its big switches (a screenshot to look at)
setfeat; setmode quickstep; home; adb shell am start -n "$PKG/com.tj.launcher.HiddenAppsActivity" >/dev/null 2>&1; sleep 3; shot 60-hidden-apps-big-switches
setfeat; setmode custom; home
fi # section o
if want t; then # section t
ensurehome
# ---- O5: different quick-settings tiles per launcher. SystemUI's REAL tile list is read back from its own dump, not just the setting.
beat "before T"
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS > "$OUT/t-grant.txt" 2>&1
tiles_now(){ adb shell dumpsys activity service com.android.systemui/.SystemUIService 2>/dev/null | grep -oE '[A-Za-z]+Tile:' | sort -u | tr '\n' ' '; }
setfeat; setmode custom; rebind; home
adb shell locksettings set-pin 1234 > "$OUT/t-setpin.txt" 2>&1
lockpad; pad d d 1 2 3 4 e; sleep 7                          # the default handshake -> Quickstep (Mode.set applies the Quickstep tiles)
adb shell settings get secure sysui_qs_tiles > "$OUT/t-quickstep-setting.txt" 2>&1
tiles_now > "$OUT/t-quickstep-systemui.txt"
info "O5: Quickstep: setting '$(tr -d '\r\n' < $OUT/t-quickstep-setting.txt)', SystemUI tiles: $(cat $OUT/t-quickstep-systemui.txt)"
check "O5: in Quickstep the tile setting is wifi,bt,rotation" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/t-quickstep-setting.txt)\" = wifi,bt,rotation ]"
check "O5: SystemUI really shows the Wi-Fi and rotation tiles in Quickstep (the emulator has no Bluetooth hardware, so SystemUI never builds that one)" bash -c "grep -q WifiTile $OUT/t-quickstep-systemui.txt && grep -q RotationLockTile $OUT/t-quickstep-systemui.txt"
adb shell am start -n "$PKG/com.tj.launcher.BackToCustomActivity" >/dev/null 2>&1; sleep 6    # back to custom: ONE tile
adb shell settings get secure sysui_qs_tiles > "$OUT/t-custom-setting.txt" 2>&1
tiles_now > "$OUT/t-custom-systemui.txt"
info "O5: custom: setting '$(tr -d '\r\n' < $OUT/t-custom-setting.txt)', SystemUI tiles: $(cat $OUT/t-custom-systemui.txt)"
check "O5: in the custom launcher the tile setting is just rotation" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/t-custom-setting.txt)\" = rotation ]"
check "O5: SystemUI really shows only the rotation tile (no Wi-Fi, no Bluetooth)" bash -c "grep -q RotationLockTile $OUT/t-custom-systemui.txt && ! grep -q WifiTile $OUT/t-custom-systemui.txt && ! grep -q BluetoothTile $OUT/t-custom-systemui.txt"
# Q3: the native pencil/gear switch (opt-in): the flag follows the mode, and the lock screen still works with it on
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_hide_pencil_native\\\" value=\\\"true\\\" /><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"
setmode quickstep; rebind; home; sleep 1
lockpad; pad d d 1 2 3 4 e; sleep 7                                      # handshake -> Quickstep: the flag must be OFF
adb shell settings get global device_demo_mode > "$OUT/t-demo-quickstep.txt" 2>&1
adb shell am start -n "$PKG/com.tj.launcher.BackToCustomActivity" >/dev/null 2>&1; sleep 6   # back to custom: the flag must be ON
adb shell settings get global device_demo_mode > "$OUT/t-demo-custom.txt" 2>&1
check "Q3: in Quickstep the demo flag is off (the pencil and gear are there)" bash -c "v=\$(tr -d ' \r\n' < $OUT/t-demo-quickstep.txt); [ \"\$v\" = 0 ] || [ \"\$v\" = null ]"
check "Q3: in the custom launcher the demo flag is on (pencil and gear hidden natively)" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/t-demo-custom.txt)\" = 1 ]"
adb shell cmd statusbar expand-settings; sleep 3
adb shell uiautomator dump /sdcard/tjq.xml >/dev/null 2>&1; adb shell cat /sdcard/tjq.xml 2>/dev/null | tr '>' '\n' > "$OUT/t-demo-qs.xml"; shot 77-qs-native-hide
check "Q3: the quick-settings panel really has no pencil in the custom launcher" bash -c "! grep -q 'resource-id=\"android:id/edit\"' $OUT/t-demo-qs.xml"
adb shell cmd statusbar collapse; sleep 1
lockpad; pad 1 2 3 4 e; sleep 6                                             # the lock screen + a plain PIN with the flag on
resumed > "$OUT/t-demo-unlock.txt"
check "Q3: the lock screen and a plain PIN still work with the flag on (custom stack in front)" bash -c "grep -qiE 'calendar|com.tj.launcher' $OUT/t-demo-unlock.txt"
# the guard: something adds a tile while the custom account is active -> it is taken out again
adb shell settings put secure sysui_qs_tiles "rotation,flashlight" >/dev/null 2>&1; sleep 4
adb shell settings get secure sysui_qs_tiles > "$OUT/t-guard-setting.txt" 2>&1
check "O5: an extra tile added while the custom launcher is active is removed again" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/t-guard-setting.txt)\" = rotation ]"
adb shell locksettings clear --old 1234 > "$OUT/t-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
setfeat; setmode custom; home
fi # section t
if want u; then # section u
ensurehome
# ---- P1 RESEARCH (informational only): would a real SECOND ANDROID USER give two launchers that keep their own apps open, with their own Recents and notifications? How seamless is the switch?
beat "before U"
adb shell pm get-max-users > "$OUT/u-maxusers.txt" 2>&1
info "P1: this emulator image: $(tr '\n' ' ' < $OUT/u-maxusers.txt)"
adb shell pm create-user TjB > "$OUT/u-create.txt" 2>&1
UID2="$(grep -oE 'id [0-9]+' "$OUT/u-create.txt" | grep -oE '[0-9]+' | head -1)"
info "P1: create-user said: $(tr '\n' ' ' < $OUT/u-create.txt) -> user id '${UID2:-none}'"
if [ -n "$UID2" ]; then
  # user 0 opens two apps
  for APP in com.android.contacts com.android.email; do adb shell monkey -p $APP -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 2; adb shell input keyevent KEYCODE_HOME; sleep 1; done
  adb shell pidof com.android.contacts > "$OUT/u-pid-before.txt" 2>&1
  adb shell dumpsys activity recents 2>/dev/null | grep -ciE 'contacts|email' > "$OUT/u-recents0-before.txt"
  # switch to the second user and time it
  T0=$(date +%s.%N); adb shell am switch-user "$UID2" > "$OUT/u-switch.txt" 2>&1
  for i in $(seq 1 60); do [ "$(adb shell am get-current-user 2>/dev/null | tr -d '\r\n')" = "$UID2" ] && break; sleep 0.5; done
  T1=$(date +%s.%N); shot 70-user-switch-first-frame; sleep 3; shot 71-user-switch-after-3s
  info "P1: switch 0 -> $UID2: current-user flipped after $(echo "$T1 - $T0" | bc -l | cut -c1-5) s (screenshots 70 / 71 show what the screen looks like while it happens)"
  adb shell dumpsys activity recents 2>/dev/null | grep -ciE 'contacts|email' > "$OUT/u-recents1.txt"
  info "P1: the SECOND user's Recents mention the first user's apps: $(tr -d ' \r\n' < $OUT/u-recents1.txt) time(s) (0 = separate lists)"
  adb shell dumpsys notification 2>/dev/null | grep -c "NotificationRecord" > "$OUT/u-notif1.txt"
  # back to user 0
  T2=$(date +%s.%N); adb shell am switch-user 0 >/dev/null 2>&1
  for i in $(seq 1 60); do [ "$(adb shell am get-current-user 2>/dev/null | tr -d '\r\n')" = "0" ] && break; sleep 0.5; done
  T3=$(date +%s.%N); sleep 3
  adb shell pidof com.android.contacts > "$OUT/u-pid-after.txt" 2>&1
  adb shell dumpsys activity recents 2>/dev/null | grep -ciE 'contacts|email' > "$OUT/u-recents0-after.txt"
  info "P1: switch $UID2 -> 0: $(echo "$T3 - $T2" | bc -l | cut -c1-5) s; the first user's Contacts process: before '$(tr -d ' \r\n' < $OUT/u-pid-before.txt)' after '$(tr -d ' \r\n' < $OUT/u-pid-after.txt)' (same number = it stayed open); Recents mentions before $(tr -d ' \r\n' < $OUT/u-recents0-before.txt) after $(tr -d ' \r\n' < $OUT/u-recents0-after.txt)"
  adb shell pm remove-user "$UID2" >/dev/null 2>&1
fi
fi # section u
if want s; then # section s
ensurehome
# ---- Q1/Q2 RESEARCH (informational only): can the shell user HIDE an app from Recents / notifications without closing it (pm suspend / pm hide)? Is an input monitor allowed (instant power-menu card)? What system knobs exist?
beat "before S"
setfeat; setmode custom; rebind; home
for APP in com.android.contacts com.android.email; do adb shell monkey -p $APP -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 2; adb shell input keyevent KEYCODE_HOME; sleep 1; done
snap(){ echo "$1: contacts pid '$(adb shell pidof com.android.contacts | tr -d '\r\n')', email pid '$(adb shell pidof com.android.email | tr -d '\r\n')', recents lines contacts $(adb shell dumpsys activity recents 2>/dev/null | grep -ci contacts) email $(adb shell dumpsys activity recents 2>/dev/null | grep -ci 'com.android.email')"; }
info "S0 baseline  $(snap)"
adb shell pm suspend com.android.contacts > "$OUT/s-suspend.txt" 2>&1; sleep 3
info "S1 pm suspend contacts -> [$(tr '\n' ' ' < $OUT/s-suspend.txt)]  $(snap suspended)"
adb shell monkey -p com.android.contacts -c android.intent.category.LAUNCHER 1 > "$OUT/s-start-suspended.txt" 2>&1; sleep 2; shot 72-suspended-app-start
info "S1b starting a suspended app: $(tr '\n' ' ' < $OUT/s-start-suspended.txt | head -c 160); resumed: $(resumed | head -c 120)"
adb shell input keyevent KEYCODE_HOME; sleep 1
adb shell pm unsuspend com.android.contacts > "$OUT/s-unsuspend.txt" 2>&1; sleep 3
info "S2 pm unsuspend contacts -> [$(tr '\n' ' ' < $OUT/s-unsuspend.txt)]  $(snap unsuspended)"
adb shell pm hide com.android.email > "$OUT/s-hide.txt" 2>&1; sleep 3
info "S3 pm hide email -> [$(tr '\n' ' ' < $OUT/s-hide.txt)]  $(snap hidden)"
adb shell pm unhide com.android.email > "$OUT/s-unhide.txt" 2>&1; sleep 3
info "S4 pm unhide email -> [$(tr '\n' ' ' < $OUT/s-unhide.txt)]  $(snap unhidden)"
# notifications of a suspended package: the shell package posts one, then is suspended
adb shell cmd notification post -t TjS tagS "hello" > "$OUT/s-post.txt" 2>&1; sleep 2
info "S5 notification posted by the shell package: $(adb shell dumpsys notification --noredact 2>/dev/null | grep -c 'tagS') line(s) mention it"
adb shell pm suspend com.android.shell > "$OUT/s-suspend-shell.txt" 2>&1; sleep 2
info "S5b pm suspend com.android.shell -> [$(tr '\n' ' ' < $OUT/s-suspend-shell.txt | head -c 200)]; notification lines now $(adb shell dumpsys notification --noredact 2>/dev/null | grep -c 'tagS'); (a hidden/suspended notification stays in the list but is flagged)"
adb shell dumpsys notification --noredact 2>/dev/null | grep -B2 -A12 'tagS' | head -40 > "$OUT/s-notif-dump.txt"
adb shell pm unsuspend com.android.shell >/dev/null 2>&1
# the input monitor probe (as the shell user)
adb shell 'CLASSPATH=$(pm path com.tj.launcher | head -1 | cut -d: -f2) app_process /system/bin com.tj.launcher.InputProbe' > "$OUT/s-inputprobe.txt" 2>&1
info "S6 input monitor probe: $(grep -E 'RESULT' $OUT/s-inputprobe.txt | tr '\n' ' ' | head -c 300)"
# knobs
adb shell cmd overlay list > "$OUT/s-overlays.txt" 2>&1
info "S7 overlays the shell user could switch: $(grep -c '\[' $OUT/s-overlays.txt) listed"
adb shell settings list secure > "$OUT/s-secure.txt" 2>&1; adb shell settings list global > "$OUT/s-global.txt" 2>&1; adb shell settings list system > "$OUT/s-system.txt" 2>&1
info "S8 settings that mention power / tiles / zen / policy: $(cat $OUT/s-secure.txt $OUT/s-global.txt $OUT/s-system.txt | grep -iE 'power|qs_tiles|zen|policy_control|navigation|immersive' | cut -d= -f1 | tr '\n' ' ' | head -c 400)"
adb shell getprop ro.build.fingerprint > "$OUT/s-build.txt" 2>&1; adb shell getprop ro.lineage.version >> "$OUT/s-build.txt" 2>&1
setfeat; setmode custom; home
fi # section s
if want p; then # section p
ensurehome
# ---- R3 RESEARCH (informational only): can adb alone hide the quick-settings pencil (and gear) with NO delay? Android's own QS footer hides them when the device is in "demo mode" (Settings.Global device_demo_mode).
beat "before P"
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS >/dev/null 2>&1
setfeat; setmode quickstep; home; sleep 2
pdump(){ adb shell uiautomator dump /sdcard/tjp.xml >/dev/null 2>&1; adb shell cat /sdcard/tjp.xml 2>/dev/null | tr '>' '\n' > "$OUT/p-$1.xml"; echo "edit(pencil) nodes: $(grep -c 'resource-id="android:id/edit"' $OUT/p-$1.xml) visible-bounds: $(grep 'resource-id="android:id/edit"' $OUT/p-$1.xml | grep -o 'bounds="[^"]*"' | head -1); settings_button nodes: $(grep -c 'settings_button' $OUT/p-$1.xml); user switch nodes: $(grep -c 'multi_user' $OUT/p-$1.xml)"; }
adb shell cmd statusbar expand-settings; sleep 3
info "P1 baseline (device_demo_mode=$(adb shell settings get global device_demo_mode | tr -d '\r\n')): $(pdump base)"; shot 73-qs-baseline
adb shell cmd statusbar collapse; sleep 1.5
adb shell settings put global device_demo_mode 1 > "$OUT/p-demo-put.txt" 2>&1; sleep 2
adb shell cmd statusbar expand-settings; sleep 3
info "P2 with device_demo_mode=1 -> [$(tr '\n' ' ' < $OUT/p-demo-put.txt)]: $(pdump demo)"; shot 74-qs-demomode
adb shell cmd statusbar collapse; sleep 1.5
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 75-power-menu-demomode
adb shell uiautomator dump /sdcard/tjp2.xml >/dev/null 2>&1; info "P3 power menu in demo mode: $(adb shell cat /sdcard/tjp2.xml 2>/dev/null | tr '>' '\n' | grep -oE 'text="(Power off|Restart|Screenshot|Emergency|Lockdown)"' | tr '\n' ' ')"
adb shell input keyevent KEYCODE_BACK; sleep 1
adb shell settings put global device_demo_mode 0 >/dev/null 2>&1; sleep 1
info "P4 statusbar commands on this Android: $(adb shell cmd statusbar help 2>&1 | grep -oE '^  [a-z-]+' | tr -d ' ' | tr '\n' ' ' | head -c 300)"
info "P5 system services the shell user can see: $(adb shell service list 2>/dev/null | grep -oE '(statusbar|activity_task|input|window|overlay|notification|user)[^ :]*' | sort -u | tr '\n' ' ' | head -c 300)"
setfeat; setmode custom; home
fi # section p
if want x; then # section x
ensurehome
# ---- S1 / S2 (Tj: "do everything in one session with adb"): the real tools/adb-all.sh against the emulator, then the in-app "Do everything" through the helper
beat "before X"
setfeat; setmode quickstep; rebind; home
rm -rf probe-out
bash tools/adb-all.sh --no-pull > "$OUT/x-adball-console.txt" 2>&1
cp -r probe-out "$OUT/x-probe-out" 2>/dev/null
check "S1: adb-all.sh finished (DONE line)" grep -q "^DONE" "$OUT/x-adball-console.txt"
check "S1: it granted WRITE_SECURE_SETTINGS (the tablet reports granted=true)" grep -q "WRITE_SECURE_SETTINGS.*granted=true" probe-out/status.txt
check "S1: it started the helper (a ShellServer process exists)" bash -c "grep -q 'OK    helper running' probe-out/status.txt"
check "S1: the probe report has the user / profile facts" bash -c "grep -q 'Maximum supported users' probe-out/launcher-probe.txt && grep -q 'pm list users' probe-out/launcher-probe.txt"
check "S1: the probe report has the Settings app facts" bash -c "grep -q 'dumpsys package com.android.settings' probe-out/launcher-probe.txt"
info "S1: probe report $(wc -l < probe-out/launcher-probe.txt) lines; status table: $(grep -c 'granted=' probe-out/status.txt) permission rows"
# T1: the DEVICE-shell helper line (what a phone running an adb app types, no "adb shell" prefix) starts the helper too
adb shell pkill -f com.tj.launcher.ShellServer >/dev/null 2>&1; sleep 1
echo 'CLASSPATH=$(pm path com.tj.launcher | head -1 | cut -d: -f2) setsid nohup app_process /system/bin com.tj.launcher.ShellServer >/dev/null 2>&1 &' | adb shell >/dev/null 2>&1; sleep 4
adb shell "ps -A -o ARGS | grep -c '[S]hellServer'" > "$OUT/x-devline-ps.txt" 2>&1
check "T1: the device-shell helper line (phone adb app, no adb shell prefix) starts the helper" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/x-devline-ps.txt)\" -ge 1 ]"
# S2: take three things away, then let the app put them back through the helper
adb shell pm revoke "$PKG" android.permission.DUMP >/dev/null 2>&1; adb shell pm revoke "$PKG" android.permission.CHANGE_CONFIGURATION >/dev/null 2>&1; adb shell cmd appops set "$PKG" RUN_IN_BACKGROUND default >/dev/null 2>&1
sleep 2
vfeat true 12
setmode quickstep; rebind; home
adb shell am start -n "$PKG/com.tj.launcher.SetupActivity" --ez autorun true >/dev/null 2>&1
SHARE=/sdcard/Android/data/$PKG/files/probe
for i in $(seq 1 60); do adb shell "[ -f $SHARE/autorun-done.txt ]" >/dev/null 2>&1 && break; sleep 3; done
adb shell cat "$SHARE/autorun-done.txt" > "$OUT/x-autorun-done.txt" 2>&1
adb shell ls -la "$SHARE" "$SHARE/apk" > "$OUT/x-share-ls.txt" 2>&1
adb shell "head -c 3000 $SHARE/tablet-report.txt" > "$OUT/x-report-head.txt" 2>&1
shot 76-setup-screen
info "S2: autorun summary: $(tr '\n' '|' < $OUT/x-autorun-done.txt | head -c 700)"
check "S2: the in-app Do-everything put the permissions back through the helper (DUMP + CHANGE_CONFIGURATION states: done)" bash -c "grep -q 'state Permission: read what is running (DUMP): done' $OUT/x-autorun-done.txt && grep -q 'state Permission: change configuration: done' $OUT/x-autorun-done.txt"
check "S2: the background app-op was put back too" grep -q 'state Allowed to run in the background: done' "$OUT/x-autorun-done.txt"
check "S2: the tablet report was written (app side + shell probe)" bash -c "grep -q 'TABLET REPORT' $OUT/x-report-head.txt && grep -q 'report [0-9][0-9][0-9][0-9]' $OUT/x-autorun-done.txt"
adb shell ls /sdcard/Download/launcher-probe /sdcard/Download/launcher-probe/shots /sdcard/Download/launcher-probe/apk > "$OUT/x-publish-ls.txt" 2>&1
check "T1: everything was published to Downloads/launcher-probe (report)" grep -q 'tablet-report.txt' "$OUT/x-publish-ls.txt"
check "T1: the Settings screenshots + screen dumps were taken (at least 10 png)" bash -c "[ \$(grep -c '\.png' $OUT/x-publish-ls.txt) -ge 10 ]"
check "T1: the tablet's own Settings app was copied to Downloads/launcher-probe/apk" grep -q 'com.android.settings.apk' "$OUT/x-publish-ls.txt"
adb shell pkill -f com.tj.launcher.ShellServer >/dev/null 2>&1
setfeat; setmode custom; home
fi # section x
if want v; then # section v
ensurehome
# ---- V1/V2 (Tj, 2026-11-04): on the LOCK screen a patch of the lock wallpaper hides the real power menu and our Power off / Restart menu has the stock item size (stock minus Screenshot).
#      U1/U2: the launcher compiled ahead of time starts faster; the unlock stopwatch logs how long the first window takes.
beat "before V"
adb shell pm grant "$PKG" android.permission.READ_EXTERNAL_STORAGE > "$OUT/v-grant.txt" 2>&1
vfeat true 12; adb shell am force-stop "$PKG"; sleep 1      # lock patch on + a deliberately WRONG model (12 % too big) that the calibration must correct
setmode quickstep; rebind; home
adb shell am start -n "$PKG/.SetupActivity" --ez test_lock_wallpaper true >/dev/null 2>&1; sleep 6
adb shell "run-as $PKG cat shared_prefs/tj_features.xml" | grep -o 'test_lock_wallpaper_done">[^<]*' > "$OUT/v-lockwp.txt" 2>&1
info "V2: test lock wallpaper: $(cat $OUT/v-lockwp.txt)"
setmode custom; rebind; home
adb shell 'CLASSPATH=$(pm path com.tj.launcher | head -1 | cut -d: -f2) setsid nohup app_process /system/bin com.tj.launcher.ShellServer >/data/local/tmp/tjshell.log 2>&1 &' > "$OUT/v-helper-start.txt" 2>&1; sleep 5
adb shell locksettings set-pin 1234 > "$OUT/v-setpin.txt" 2>&1
adb shell input keyevent KEYCODE_POWER; sleep 2; adb shell input keyevent KEYCODE_WAKEUP; sleep 3
adb logcat -c
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 80-lock-menu-first      # first time: the helper learns the rectangle and the item size
adb shell input keyevent KEYCODE_BACK; sleep 14      # the patch is built now; the helper photographs the lock screen and the app calibrates (about 5-8 s)
adb logcat -d -s TjA11y:I | grep "CAL lock patch" > "$OUT/v-cal.txt" 2>&1
adb pull /sdcard/Download/launcher-probe/cal.png "$OUT/v-cal-screenshot.png" >/dev/null 2>&1
info "V4: $(tail -1 $OUT/v-cal.txt | cut -c1-240)"
CW="$(identify -format '%w' "$OUT/v-cal-screenshot.png" 2>/dev/null)"; CH="$(identify -format '%h' "$OUT/v-cal-screenshot.png" 2>/dev/null)"
for PT in "300 300" "300 700" "540 490"; do set -- $PT
  GOT="$(convert "$OUT/v-cal-screenshot.png" -crop 1x1+$1+$2 -depth 8 txt:- 2>/dev/null | tail -1 | grep -o '([0-9, ]*)')"
  [ -n "$CW" ] && info "V4: calibration screenshot ${CW}x${CH} at $1,$2 = $GOT; the test wallpaper there is ($(( $1 * 255 / CW )),$(( $2 * 255 / CH )),$(( 255 - $1 * 255 / CW )))"
done
check "V4: the patch is a PHOTO of the real lock screen (the helper screenshot with the menu closed, cropped to the patch)" grep -q "CAL lock patch PHOTO" "$OUT/v-cal.txt"
adb shell dumpsys window windows | grep -c TjWallpaperPatch > "$OUT/v-patch-windows.txt" 2>&1
shot 81-lock-closed-patch-on
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 82-lock-menu-open
adb logcat -d -s TjA11y:I | grep -E "CARD area|PATCH|bounds=" > "$OUT/v-log.txt" 2>&1
adb shell input keyevent KEYCODE_BACK; sleep 3
vfeat false 0
adb shell am force-stop "$PKG"; sleep 1
rebind; adb shell input keyevent KEYCODE_POWER; sleep 2; adb shell input keyevent KEYCODE_WAKEUP; sleep 3
shot 83-lock-closed-patch-off
adb shell dumpsys window windows | grep -c TjWallpaperPatch > "$OUT/v-patch-windows-off.txt" 2>&1
CARDLINE="$(grep 'CARD area' "$OUT/v-log.txt" | tail -1)"
info "V1: $CARDLINE"
FULL="$(echo "$CARDLINE" | grep -o 'full=[^ ]*' | grep -o '[0-9]*' | tr '\n' ' ')"
AREA="$(echo "$CARDLINE" | grep -o 'area=[^ ]*' | head -1)"
check "V1: a lock-screen wallpaper patch window exists once the menu rectangle is known" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/v-patch-windows.txt)\" -ge 1 ]"
check "V1: the patch is gone when the lock-screen switch is off (control)" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/v-patch-windows-off.txt)\" = 0 ]"
set -- $FULL
if [ "$#" -ge 4 ]; then
  GEOM="$(( $3 - $1 ))x$(( $4 - $2 ))+$1+$2"
  MAEC="$(compare -metric MAE -extract "$GEOM" "$OUT/81-lock-closed-patch-on.png" "$OUT/83-lock-closed-patch-off.png" null: 2>&1 | grep -o '([0-9.e+-]*)' | tr -d '()')"
  echo "closed, patch on vs off, region $GEOM: MAE $MAEC" | tee "$OUT/v-diff-closed.txt"
  check "V2: with the menu closed the patch is invisible (lock screen looks the same with it on or off)" awk -v m="$MAEC" 'BEGIN{exit !(m!="" && m+0 < 0.005)}'
  CARDH="$(echo "$CARDLINE" | grep -o 'area=[^ ]*' | head -1 | grep -o '[0-9]*' | tr '\n' ' ')"
  set -- $CARDH
  if [ "$#" -ge 4 ]; then
    FREE="$(( $3 - $1 ))x$(( $(echo $FULL | cut -d' ' -f4) - $4 - 2 ))+$1+$(( $4 + 2 ))"
    MAEF="$(compare -metric MAE -extract "$FREE" "$OUT/82-lock-menu-open.png" "$OUT/83-lock-closed-patch-off.png" null: 2>&1 | grep -o '([0-9.e+-]*)' | tr -d '()')"
    echo "menu open: the freed slot $FREE vs the bare lock wallpaper: MAE $MAEF" | tee "$OUT/v-diff-open.txt"
    FL=$(echo $FULL | cut -d' ' -f1); FT=$(echo $FULL | cut -d' ' -f2); FB=$(echo $FULL | cut -d' ' -f4)    # (not "set --": the checks below still need the card's numbers in $1..$4)
    RING="30x$(( FB - FT ))+$(( FL - 32 ))+$FT"
    MAER="$(compare -metric MAE -extract "$RING" "$OUT/82-lock-menu-open.png" "$OUT/83-lock-closed-patch-off.png" null: 2>&1 | grep -o '([0-9.e+-]*)' | tr -d '()')"
    info "V2: next to the patch (strip $RING, outside it) the screen with the menu open differs from the closed lock screen by MAE $MAER (0 = the menu does not dim the surroundings)"
    check "V2: the part of the old menu below our short card shows exactly the lock wallpaper (MAE < 0.5 %)" awk -v m="$MAEF" 'BEGIN{exit !(m!="" && m+0 < 0.005)}'
    check "V1: our card is shorter than the stock menu rectangle (stock item size, no Screenshot slot)" bash -c "[ $(( $4 - $2 )) -lt $(( $(echo $FULL | cut -d' ' -f4) - $(echo $FULL | cut -d' ' -f2) )) ]"
  else info "V1: no card area in the log"; check "V1: the short card area was logged" false; fi
else info "V1: no CARD line in the log"; check "V1: the card was shown on the lock screen" false; fi
adb shell input swipe 300 900 300 300 300; sleep 2
adb logcat -c
adb shell input text 1234; sleep 1; adb shell input keyevent KEYCODE_ENTER; sleep 4
adb logcat -d -s TjA11y:I | grep UNLOCKSPEED > "$OUT/v-unlockspeed.txt" 2>&1
info "U1: $(cat $OUT/v-unlockspeed.txt | cut -c1-200)"
check "U1: the unlock stopwatch measured the launcher after a PIN unlock" grep -q UNLOCKSPEED "$OUT/v-unlockspeed.txt"
adb shell locksettings clear --old 1234 > "$OUT/v-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
# U2: compile ahead of time: cold start of our launcher before / after (informational numbers; the compile itself must succeed)
adb shell am force-stop "$PKG"; sleep 1
adb shell cmd package compile -m verify -f "$PKG" > /dev/null 2>&1
adb shell am force-stop "$PKG"; sleep 1
adb shell am start -W -n "$PKG/com.android.launcher3.Launcher" 2>&1 | grep -E "TotalTime|WaitTime" | tr '\n' ' ' > "$OUT/v-start-before.txt"
adb shell cmd package compile -m speed -f "$PKG" > "$OUT/v-compile.txt" 2>&1
adb shell am force-stop "$PKG"; sleep 1
adb shell am start -W -n "$PKG/com.android.launcher3.Launcher" 2>&1 | grep -E "TotalTime|WaitTime" | tr '\n' ' ' > "$OUT/v-start-after.txt"
info "U2: cold start with verify-only: $(cat $OUT/v-start-before.txt)   after 'compile -m speed': $(cat $OUT/v-start-after.txt)"
check "U2: 'cmd package compile -m speed -f' succeeds as the shell user" grep -qi success "$OUT/v-compile.txt"
setmode custom; home
fi # section v
if want g; then # section g
# ---- Y2 (Tj, 2026-11-04): the PHONE-side adb guide app. Installed here only to prove it opens, lists the commands and has Copy buttons.
beat "before G"
GAPK=phoneguide/build/outputs/apk/debug/phoneguide-debug.apk
adb install -r "$GAPK" > "$OUT/g-install.txt" 2>&1
adb shell am start -n com.tj.phoneguide/.GuideActivity >/dev/null 2>&1; sleep 3
shot 90-phoneguide-top
adb shell uiautomator dump /sdcard/gui.xml >/dev/null 2>&1; adb shell cat /sdcard/gui.xml > "$OUT/g-ui-top.xml" 2>/dev/null
check "Y2: the phone guide installs" grep -qi success "$OUT/g-install.txt"
check "Y2: the phone guide opens and shows its title" grep -q "Tablet adb guide" "$OUT/g-ui-top.xml"
# uiautomator only lists what is on screen, so scroll one page at a time (up to 14 pages) and keep every dump; the Copy buttons / first command may sit below the first screen
cp "$OUT/g-ui-top.xml" "$OUT/g-ui-p0.xml"
for i in $(seq 1 14); do
  adb shell input swipe 300 850 300 250 400; sleep 1
  adb shell uiautomator dump /sdcard/gui.xml >/dev/null 2>&1; adb shell cat /sdcard/gui.xml > "$OUT/g-ui-p$i.xml" 2>/dev/null
  [ "$i" = 5 ] && shot 91-phoneguide-middle
  grep -q 'pm grant' "$OUT/g-ui-p$i.xml" && grep -q 'Copy this line' "$OUT/g-ui-p$i.xml" && break
done
cat "$OUT"/g-ui-p*.xml > "$OUT/g-ui-all.xml"
info "Y2: scrolled $i page(s); 'Copy this line' seen $(grep -o 'Copy this line' "$OUT/g-ui-all.xml" | wc -l) time(s); 'pm grant' seen $(grep -o 'pm grant' "$OUT/g-ui-all.xml" | wc -l) time(s); sample texts: $(grep -o 'text="[^"]\{4,50\}"' "$OUT/g-ui-p1.xml" | head -8 | tr '\n' ' ')"
check "Y2: the list has Copy buttons (labelled 'Copy this line')" grep -q 'Copy this line' "$OUT/g-ui-all.xml"
check "Y2: the permissions section with its pm grant commands is reachable by scrolling" grep -q 'pm grant' "$OUT/g-ui-all.xml"
adb shell input keyevent KEYCODE_BACK; sleep 1
adb shell am force-stop com.tj.phoneguide; home
fi # section g
if want rs; then # section rs
ensurehome
# ---- RESEARCH (info lines only, no PASS/FAIL): AA1 what policy_control really does on Android 10, the development-level permissions, AA4 the "hide a task but keep it alive" candidates.
beat "before RS"
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS >/dev/null 2>&1
setfeat; setmode custom; home; sleep 2
mae(){ compare -metric MAE -extract "$3" "$OUT/$1.png" "$OUT/$2.png" null: 2>&1 | grep -o '([0-9.e+-]*)' | tr -d '()'; }
sbstate(){ adb shell dumpsys window windows 2>/dev/null | awk '/Window #[0-9]+ Window\{[^}]* StatusBar\}/{f=1;c=0} f{ if ($0 ~ /mFrame=|Frames:|[ (]frame=|mViewVisibility|isReadyForDisplay|mHasSurface/) print; c++; if (c>60) f=0 }' | head -5 | tr -s ' ' | tr '\n' ';' | cut -c1-330; }
# --- R1: Settings.Global policy_control = immersive.status=*  (the idea: hide the status bar while the custom launcher is active)
adb shell settings delete global policy_control >/dev/null 2>&1; sleep 1
shot 100-rs-home-baseline
info "R1 baseline (no policy_control): StatusBar window: $(sbstate)"
adb shell settings put global policy_control "immersive.status=*"; sleep 3
shot 101-rs-home-immersive
info "R1 immersive.status=* -> setting '$(adb shell settings get global policy_control | tr -d '\r\n')': status-bar strip changed by MAE $(mae 100-rs-home-baseline 101-rs-home-immersive 600x30+0+0) (0 = identical), the rest of the screen by MAE $(mae 100-rs-home-baseline 101-rs-home-immersive 600x900+0+100); StatusBar window: $(sbstate)"
adb shell input swipe 300 2 300 350 150; sleep 1.5; shot 102-rs-swipe1
info "R1 after ONE swipe down from the top edge: strip vs baseline MAE $(mae 100-rs-home-baseline 102-rs-swipe1 600x30+0+0), whole screen vs immersive MAE $(mae 101-rs-home-immersive 102-rs-swipe1 600x1024+0+0); StatusBar window: $(sbstate)"
adb shell input swipe 300 2 300 350 150; sleep 1.5; shot 103-rs-swipe2
info "R1 after a SECOND swipe: whole screen vs immersive MAE $(mae 101-rs-home-immersive 103-rs-swipe2 600x1024+0+0); StatusBar window: $(sbstate)"
adb shell cmd statusbar collapse; sleep 1.5
adb shell cmd statusbar expand-settings; sleep 2.5; shot 104-rs-expand-cmd
info "R1 'cmd statusbar expand-settings' under immersive: whole screen vs immersive MAE $(mae 101-rs-home-immersive 104-rs-expand-cmd 600x1024+0+0); StatusBar window: $(sbstate)"
adb shell cmd statusbar collapse; sleep 1.5
# an app opened while immersive is on
adb shell settings delete global policy_control >/dev/null 2>&1; sleep 1
adb shell monkey -p com.android.calendar -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 4; shot 105-rs-app-baseline
adb shell settings put global policy_control "immersive.status=*"; sleep 3; shot 106-rs-app-immersive
info "R1 an app (Calendar) with immersive.status=*: top strip changed by MAE $(mae 105-rs-app-baseline 106-rs-app-immersive 600x30+0+0), content below by MAE $(mae 105-rs-app-baseline 106-rs-app-immersive 600x900+0+100)"
home
# the lock screen with the setting on / off (L10: nothing may change there)
adb shell locksettings set-pin 1234 >/dev/null 2>&1
adb shell input keyevent KEYCODE_POWER; sleep 2; adb shell input keyevent KEYCODE_WAKEUP; sleep 3; shot 107-rs-lock-immersive
adb shell input swipe 300 900 300 300 300; sleep 1.5; adb shell input text 1234; sleep 1; adb shell input keyevent KEYCODE_ENTER; sleep 3
adb shell settings delete global policy_control >/dev/null 2>&1; sleep 2
adb shell input keyevent KEYCODE_POWER; sleep 2; adb shell input keyevent KEYCODE_WAKEUP; sleep 3; shot 108-rs-lock-baseline
info "R1 lock screen with immersive.status=* vs without: strip MAE $(mae 107-rs-lock-immersive 108-rs-lock-baseline 600x30+0+0), rest MAE $(mae 107-rs-lock-immersive 108-rs-lock-baseline 600x900+0+100) (0 = the lock screen is untouched)"
adb shell input swipe 300 900 300 300 300; sleep 1.5; adb shell input text 1234; sleep 1; adb shell input keyevent KEYCODE_ENTER; sleep 3
adb shell locksettings clear --old 1234 >/dev/null 2>&1; adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
adb shell settings delete global policy_control >/dev/null 2>&1
# --- R2: the permissions the shell may grant to an app that DECLARES them (protectionLevel contains "development"): they must be in the manifest BEFORE the cable session
adb shell pm list permissions -f 2>/dev/null | awk '/permission:/{n=$0; sub(/.*permission:/,"",n)} /protectionLevel:/ && /development/{print n}' > "$OUT/rs-dev-permissions.txt"
info "R2 development-level permissions on this Android 10 ($(wc -l < $OUT/rs-dev-permissions.txt)): $(tr '\n' ' ' < $OUT/rs-dev-permissions.txt | cut -c1-2800)"
# --- R3: the commands the shell has for tasks / stacks / displays, and the "hide a task but keep it alive" candidates
info "R3 am task/stack/display commands: $(adb shell am help 2>&1 | grep -E '^ +am (task|stack|display)' | sed 's/^ *//' | cut -c1-70 | tr '\n' '|' | cut -c1-900)"
adb shell monkey -p com.android.contacts -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3
adb shell monkey -p com.android.calendar -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3
adb shell dumpsys activity recents 2>/dev/null | grep -E "Recent #[0-9]+:" | sed 's/^ *//' | cut -c1-150 > "$OUT/rs-recents-before.txt"
info "R3 Recents now: $(tr '\n' '|' < $OUT/rs-recents-before.txt | cut -c1-700)"
CTASK=$(grep 'A=com.android.contacts' "$OUT/rs-recents-before.txt" | head -1 | grep -o ' #[0-9]* ' | tr -d ' #')
CSTACK=$(grep 'A=com.android.contacts' "$OUT/rs-recents-before.txt" | head -1 | grep -o 'StackId=[0-9]*' | cut -d= -f2)
adb shell am stack list > "$OUT/rs-stacks.txt" 2>&1
HOMESTACK=$(awk '/Stack id=/{sid=$2; sub(/id=/,"",sid)} /taskId=[0-9]+:.*(com.tj.launcher|com.android.launcher3)/{print sid; exit}' "$OUT/rs-stacks.txt")
info "R3 contacts task=$CTASK stack=$CSTACK, the stack holding our launcher=$HOMESTACK; stacks: $(grep -E 'Stack id=|taskId=' $OUT/rs-stacks.txt | sed 's/^ *//' | cut -c1-90 | tr '\n' '|' | cut -c1-900)"
CPID0=$(adb shell pidof com.android.contacts | tr -d '\r\n')
if [ -n "$CTASK" ] && [ -n "$HOMESTACK" ]; then
  adb shell am stack movetask "$CTASK" "$HOMESTACK" true > "$OUT/rs-movetask.txt" 2>&1; sleep 2
  adb shell dumpsys activity recents 2>/dev/null | grep -E "Recent #[0-9]+:" | sed 's/^ *//' | cut -c1-150 > "$OUT/rs-recents-after-move.txt"
  info "R3a 'am stack movetask $CTASK $HOMESTACK' -> [$(tr '\n' ' ' < $OUT/rs-movetask.txt | cut -c1-300)]; contacts still in Recents: $(grep -c 'A=com.android.contacts' $OUT/rs-recents-after-move.txt), pid before/after: $CPID0 / $(adb shell pidof com.android.contacts | tr -d '\r\n')"
else info "R3a skipped (no contacts task or no home stack id found)"; fi
adb shell settings put global overlay_display_devices "600x1024/170"; sleep 4
adb shell dumpsys activity activities 2>/dev/null | grep -E "^ *Display #[0-9]+" | sed 's/^ *//' | cut -c1-80 > "$OUT/rs-displays.txt"
info "R3b simulated secondary display: $(tr '\n' '|' < $OUT/rs-displays.txt)"
NEWD=$(grep -o 'Display #[0-9]*' "$OUT/rs-displays.txt" | grep -v '#0' | head -1 | tr -d 'Display #')
CSTACK2=$(adb shell dumpsys activity recents 2>/dev/null | grep 'A=com.android.calendar' | head -1 | grep -o 'StackId=[0-9]*' | cut -d= -f2)
if [ -n "$NEWD" ] && [ -n "$CSTACK2" ]; then
  adb shell am display move-stack "$CSTACK2" "$NEWD" > "$OUT/rs-movestack.txt" 2>&1; sleep 3
  adb shell dumpsys activity recents 2>/dev/null | grep -E "Recent #[0-9]+:" | sed 's/^ *//' | cut -c1-150 > "$OUT/rs-recents-after-display.txt"
  info "R3b 'am display move-stack $CSTACK2 $NEWD' -> [$(tr '\n' ' ' < $OUT/rs-movestack.txt | cut -c1-300)]; calendar still in Recents: $(grep -c 'A=com.android.calendar' $OUT/rs-recents-after-display.txt), alive: $(adb shell pidof com.android.calendar | tr -d '\r\n')"
  shot 109-rs-secondary-display
else info "R3b skipped (no second display or no calendar stack)"; fi
adb shell settings delete global overlay_display_devices >/dev/null 2>&1; sleep 3
adb shell am force-stop com.android.contacts; adb shell am force-stop com.android.calendar
setfeat; setmode custom; home
fi # section rs
if want last; then # section last
beat "before LAST STEP"
# ---- LAST STEP: our replacement card must press the REAL "Restart" button (the emulator reboots after this)
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><string name=\\\"power_mode\\\">hide_screenshot</string><boolean name=\\\"guard_stock_home\\\" value=\\\"false\\\" /><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /></map>\" > shared_prefs/tj_features.xml'"; adb shell am force-stop "$PKG"; sleep 1
setmode custom; rebind; home; sleep 3
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 14-card-before-restart
RECT="$(adb logcat -d -s TjA11y:I | grep -o 'bounds=Rect([0-9, -]*)' | tail -1 | grep -o '[0-9]*' | tr '\n' ' ')"
set -- $RECT
if [ "$#" -ge 4 ]; then
  X=$(( ($1 + $3) / 2 )); Y=$(( $2 + ($4 - $2) * 3 / 4 ))
  echo "card rect: $RECT -> tap Restart at $X,$Y" | tee "$OUT/press-restart.txt"
  adb shell dumpsys window windows | grep -iE 'Window\{[^}]*(TjA11y|com.tj.launcher|ActionsDialog)' -A0 | head -6 >> "$OUT/press-restart.txt"
  adb shell input tap $X $Y; sleep 0.6   # the tap REBOOTS the emulator: read the log before the device goes away
  adb logcat -d -s TjA11y:I > "$OUT/tja11y-full.txt" 2>&1
  grep "press" "$OUT/tja11y-full.txt" >> "$OUT/press-restart.txt"
  adb shell dumpsys input 2>/dev/null | grep -iE 'TjA11y|com.tj.launcher|ActionsDialog' | head -8 >> "$OUT/press-restart.txt"
else
  echo "no card rect found in log" | tee -a "$OUT/press-restart.txt"
fi
check "card's Restart pressed the real Restart button" grep -q "press 'Restart' -> true" "$OUT/press-restart.txt"
fi # section last
echo "failed checks: $FAILS" | tee -a "$OUT/results.txt"
ls -la "$OUT"
exit "$FAILS"
