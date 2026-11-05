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
#      Names: base legacy x1 e1 y2 b6 b4 n n6 o t u s p x v g rs rs2 rs3 rs4 ka rt im gc pf rb last. Setup, the install and the helper functions below always run.
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
# AB1 (safety audit): Android's RETAIL-DEMO flag (device_demo_mode) is never turned on by the launcher any more (AOSP: it makes the system create a demo user and switch to it at boot).
#      A leftover from the retired option (its switch in the prefs + the flag on) is put back when the helper connects, and the flag stays off through a Quickstep session and back.
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_hide_pencil_native\\\" value=\\\"true\\\" /><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"
adb shell settings put global device_demo_mode 1 >/dev/null 2>&1                      # as if the old option had left it on
setmode quickstep; rebind; home; sleep 3                                              # the helper connects: the one-time cleanup runs
adb shell settings get global device_demo_mode > "$OUT/t-demo-cleanup.txt" 2>&1
check "AB1: a retail-demo flag left on by the retired option is put back to 0 when the helper connects" bash -c "v=\$(tr -d ' \r\n' < $OUT/t-demo-cleanup.txt); [ \"\$v\" = 0 ] || [ \"\$v\" = null ]"
lockpad; pad d d 1 2 3 4 e; sleep 7                                      # handshake -> Quickstep
adb shell am start -n "$PKG/com.tj.launcher.BackToCustomActivity" >/dev/null 2>&1; sleep 6   # back to custom
adb shell settings get global device_demo_mode > "$OUT/t-demo-custom.txt" 2>&1
check "AB1: after a Quickstep session and back in the custom launcher the retail-demo flag is still off" bash -c "v=\$(tr -d ' \r\n' < $OUT/t-demo-custom.txt); [ \"\$v\" = 0 ] || [ \"\$v\" = null ]"
lockpad; pad 1 2 3 4 e; sleep 6                                             # the lock screen + a plain PIN
resumed > "$OUT/t-demo-unlock.txt"
check "AB1: the lock screen and a plain PIN work (custom stack in front)" bash -c "grep -qiE 'calendar|com.tj.launcher' $OUT/t-demo-unlock.txt"
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
info "P1 baseline: $(pdump base)"; shot 73-qs-baseline
adb shell cmd statusbar collapse; sleep 1.5
# (the device_demo_mode experiment that stood here was REMOVED by the AB1 safety audit: that flag can make Android create a demo user at boot)
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
adb pull "$SHARE/tablet-report.txt" "$OUT/x-report.txt" >/dev/null 2>&1
shot 76-setup-screen
info "S2: autorun summary: $(tr '\n' '|' < $OUT/x-autorun-done.txt | head -c 700)"
check "S2: the in-app Do-everything put the permissions back through the helper (DUMP + CHANGE_CONFIGURATION states: done)" bash -c "grep -q 'state Permission: read what is running (DUMP): done' $OUT/x-autorun-done.txt && grep -q 'state Permission: change configuration: done' $OUT/x-autorun-done.txt"
check "S2: the background app-op was put back too" grep -q 'state Allowed to run in the background: done' "$OUT/x-autorun-done.txt"
check "S2: the tablet report was written (app side + shell probe)" bash -c "grep -q 'TABLET REPORT' $OUT/x-report-head.txt && grep -q 'report [0-9][0-9][0-9][0-9]' $OUT/x-autorun-done.txt"
adb shell ls /sdcard/Download/launcher-probe /sdcard/Download/launcher-probe/shots /sdcard/Download/launcher-probe/apk > "$OUT/x-publish-ls.txt" 2>&1
check "T1: everything was published to Downloads/launcher-probe (report)" grep -q 'tablet-report.txt' "$OUT/x-publish-ls.txt"
check "T1: the Settings screenshots + screen dumps were taken (at least 10 png)" bash -c "[ \$(grep -c '\.png' $OUT/x-publish-ls.txt) -ge 10 ]"
check "T1: the tablet's own Settings app was copied to Downloads/launcher-probe/apk" grep -q 'com.android.settings.apk' "$OUT/x-publish-ls.txt"
# PG2 / PG3 (Tj, 2026-11-05: "make sure claude can save and use all necessary information from the tablet"): the REAL report this app just wrote on a real Android 10 system (not the invented sample)
rep_all(){ local p; for p in "$@"; do grep -qE -- "$p" "$OUT/x-report.txt" || return 1; done; }
kt_all(){ local l p; l=$(grep '^keytest:' "$OUT/x-autorun-done.txt"); for p in "$@"; do printf '%s' "$l" | grep -qF -- "$p" || return 1; done; }
info "PG2: report $(wc -c < "$OUT/x-report.txt" 2>/dev/null) bytes, $(wc -l < "$OUT/x-report.txt" 2>/dev/null) lines; keytest: $(grep '^keytest:' "$OUT/x-autorun-done.txt" | cut -c1-330)"
check "PG2: the report holds the launcher's own state (version, mode, helper, accessibility, crash breaker)" rep_all 'THE LAUNCHER ON THIS TABLET' '^launcher version: ' '^mode: ' '^shell helper reachable: yes' '^accessibility helper bound: ' '^crash breaker tripped: '
check "PG2: the report ran the launcher's own readers on this system (PARSER SELF-CHECK: Recents tasks, apps with a notification)" rep_all 'PARSER SELF-CHECK' '^Recents tasks parsed: [0-9]' '^Apps with a notification now'
check "PG2: the probe list added the samples (notification records, side-key device, policy_control, crash log, the launcher's log)" rep_all 'NotificationRecord\(' 'getevent -pl' 'settings get global policy_control' 'logcat -b crash' 'TjReset:I'
check "PG2: the side-key routine ran end to end in the emulator (KEYTEST markers, the 'no power menu' flag answered OK, then cleared)" kt_all 'KEYTEST-START' 'KEYTEST-END' 'disable2(0x8) -> OK' 'cleared'
check "PG3: the report carries the key-test verdict on ONE line (it survives the report's length cut)" rep_all '^keytest_summary: the side key'
rm -rf "$OUT/x-intake"
python3 -I tools/tablet_intake.py "$OUT/x-report.txt" --out "$OUT/x-intake" --date 2026-01-01 --label emulator > "$OUT/x-intake-console.txt" 2>&1; RCI=$?
info "PG3: intake of the real report: exit $RCI; $(head -c 400 "$OUT/x-intake-console.txt" | tr '\n' '|')"
XF="$OUT/x-intake/2026-01-01/FACTS.md"
check "PG3: the intake tool read the REAL report (exit 0, FACTS.md and LATEST.md written)" bash -c "[ $RCI = 0 ] && [ -f $XF ] && [ -f $OUT/x-intake/LATEST.md ]"
check "PG3: FACTS.md answers from the real report (version, tablet, Recents check, notification check, side-key row)" bash -c "grep -qE 'Launcher version / mode \| [0-9]+\.[0-9]+' $XF && grep -q 'Android 10' $XF && grep -q 'Recents format check | the launcher.s reader parsed' $XF && ! grep -q 'Notification format check | (not in the report)' $XF && grep -q 'Side-key test (Power menu v2) | the side key' $XF"
check "PG3: no PARSER MISMATCH red flag on a stock Android 10 system (the readers match the real dump formats)" bash -c "! grep -q 'PARSER MISMATCH' $XF"
check "PG3: no IPv4 address is left in the saved report (redaction)" bash -c "! grep -qP '\b(?!127\.0\.0\.1\b)(?!0\.0\.0\.0\b)(?:(?:25[0-5]|2[0-4][0-9]|1?[0-9]?[0-9])\.){3}(?:25[0-5]|2[0-4][0-9]|1?[0-9]?[0-9])\b' $OUT/x-intake/2026-01-01/tablet-report.txt"
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
# ---- Y2 / PG1 (Tj, 2026-11-04 and 2026-11-05): the PHONE-side adb guide app, ordered for a SHORT cable session. Installed here to prove it opens, shows the plan, the three parts in order, the helper start card with its
#      Copy button, keeps FALLBACK / EXTRAS / UNDO closed until tapped (their pm grant lines stay hidden), remembers a tick across a restart, and that "Take me to my next step" scrolls to the first card not ticked yet.
beat "before G"
GAPK=phoneguide/build/outputs/apk/debug/phoneguide-debug.apk
adb install -r "$GAPK" > "$OUT/g-install.txt" 2>&1
adb shell pm clear com.tj.phoneguide >/dev/null 2>&1          # no ticks or switch left over from an earlier run
gdump(){ adb shell uiautomator dump /sdcard/gui.xml >/dev/null 2>&1; adb shell cat /sdcard/gui.xml > "$1" 2>/dev/null; }
# gbounds <text part> <dump>: "x1 y1 x2 y2" of the first node whose text contains the part; gtap taps its centre (a node is only in the dump while it is on screen)
gbounds(){ tr '>' '\n' < "$2" | grep -F "$1" | grep -o 'bounds="[^"]*"' | head -1 | grep -o '[0-9]\+' | tr '\n' ' '; }
gtap(){ local gb; gb=$(gbounds "$1" "$2"); set -- $gb; [ "$#" -ge 4 ] || return 1; adb shell input tap $(( ($1+$3)/2 )) $(( ($2+$4)/2 )); }
adb shell am start -n com.tj.phoneguide/.GuideActivity >/dev/null 2>&1; sleep 3
shot 90-phoneguide-top
gdump "$OUT/g-ui-top.xml"
check "Y2: the phone guide installs" grep -qi success "$OUT/g-install.txt"
check "Y2: the phone guide opens and shows its title" grep -q "Tablet adb guide" "$OUT/g-ui-top.xml"
check "PG1: the top of the guide shows the plan" grep -q "Your plan" "$OUT/g-ui-top.xml"
# uiautomator only lists what is on screen, so scroll one page at a time (up to 30 pages) and keep every dump; stop when the screen no longer changes (the end of the list)
cp "$OUT/g-ui-top.xml" "$OUT/g-ui-p0.xml"
GLAST=""
for i in $(seq 1 30); do
  adb shell input swipe 300 850 300 250 400; sleep 1
  gdump "$OUT/g-ui-p$i.xml"
  [ "$i" = 5 ] && shot 91-phoneguide-middle
  GCUR=$(grep -o 'text="[^"]*"' "$OUT/g-ui-p$i.xml" | md5sum | cut -c1-12)
  [ "$GCUR" = "$GLAST" ] && break
  GLAST="$GCUR"
done
shot 92-phoneguide-bottom
cp "$OUT/g-ui-p$i.xml" "$OUT/g-ui-bottom.xml"
cat "$OUT"/g-ui-p*.xml > "$OUT/g-ui-all.xml"
info "Y2: scrolled $i page(s) to the end; 'Copy this line' seen $(grep -o 'Copy this line' "$OUT/g-ui-all.xml" | wc -l) time(s); stars $(grep -o '★' "$OUT/g-ui-all.xml" | wc -l); 'tap to open' $(grep -o 'tap to open' "$OUT/g-ui-all.xml" | wc -l); first texts: $(grep -o 'text="[^"]\{4,50\}"' "$OUT/g-ui-p1.xml" | head -6 | tr '\n' ' ')"
check "Y2: the list has Copy buttons (labelled 'Copy this line')" grep -q 'Copy this line' "$OUT/g-ui-all.xml"
check "PG1: the 'Take me to my next step' button is there" grep -q 'Take me to my next step' "$OUT/g-ui-all.xml"
G1=$(grep -b -o 'PART 1: BEFORE' "$OUT/g-ui-all.xml" | head -1 | cut -d: -f1); G2=$(grep -b -o 'PART 2: WITH' "$OUT/g-ui-all.xml" | head -1 | cut -d: -f1); G3=$(grep -b -o 'PART 3: AFTER' "$OUT/g-ui-all.xml" | head -1 | cut -d: -f1)
check "PG1: PART 1 (before the cable), PART 2 (with the cable), PART 3 (after) appear in that order (offsets ${G1:-none} < ${G2:-none} < ${G3:-none})" bash -c "[ -n '$G1' ] && [ -n '$G2' ] && [ -n '$G3' ] && [ '$G1' -lt '$G2' ] && [ '$G2' -lt '$G3' ]"
check "PG1: PART 2 has the helper start card (ShellServer) with a Copy button, marked with a star" bash -c "grep -q 'ShellServer' $OUT/g-ui-all.xml && grep -q '★' $OUT/g-ui-all.xml"
check "PG1: FALLBACK, EXTRAS and UNDO are closed at first ('tap to open') and none of their pm grant lines is shown" bash -c "grep -q 'FALLBACK' $OUT/g-ui-all.xml && grep -q 'EXTRAS' $OUT/g-ui-all.xml && grep -q 'UNDO: take everything back' $OUT/g-ui-all.xml && grep -q 'tap to open' $OUT/g-ui-all.xml && ! grep -q 'pm grant' $OUT/g-ui-all.xml"
# open FALLBACK by tapping its header (it is on the last page now): the permission commands appear
gtap 'FALLBACK' "$OUT/g-ui-bottom.xml"; sleep 1
gdump "$OUT/g-ui-fb1.xml"; adb shell input swipe 300 850 300 450 300; sleep 1; gdump "$OUT/g-ui-fb2.xml"
cat "$OUT/g-ui-fb1.xml" "$OUT/g-ui-fb2.xml" > "$OUT/g-ui-fb.xml"
shot 93-phoneguide-fallback-open
check "PG1: tapping FALLBACK opens it and shows the permission commands (pm grant)" grep -q 'pm grant' "$OUT/g-ui-fb.xml"
# the tick: restart the guide (back at the top), tick the first card, "Take me to my next step" must put the SECOND card at the top of the screen, and the tick must survive another restart
adb shell am force-stop com.tj.phoneguide; sleep 1
adb shell am start -n com.tj.phoneguide/.GuideActivity >/dev/null 2>&1; sleep 3
adb shell input swipe 300 850 300 450 300; sleep 1      # the plan card fills the first screen: scroll a little so the button and card 1's Done box are both on screen
gdump "$OUT/g-ui-r1.xml"
CARD1=$(grep -m1 '^@ ' phoneguide/src/main/assets/steps.txt | sed 's/^@ //'); CARD2=$(grep '^@ ' phoneguide/src/main/assets/steps.txt | sed -n 2p | sed 's/^@ //')
gtap 'text="Done"' "$OUT/g-ui-r1.xml"; sleep 1
gtap 'Take me to my next step' "$OUT/g-ui-r1.xml"; sleep 2
gdump "$OUT/g-ui-r2.xml"
shot 94-phoneguide-next-step
GTOP=$(gbounds "$CARD2" "$OUT/g-ui-r2.xml" | awk '{print $2}')
info "PG1: card 1 [$CARD1] ticked; after 'Take me to my next step' card 2 [$CARD2] has its top at y=${GTOP:-not on screen}"
check "PG1: 'Take me to my next step' scrolled to the second card (its title is within the top 250 px)" bash -c "[ -n '$GTOP' ] && [ '$GTOP' -le 250 ]"
adb shell am force-stop com.tj.phoneguide; sleep 1
adb shell am start -n com.tj.phoneguide/.GuideActivity >/dev/null 2>&1; sleep 3
adb shell input swipe 300 850 300 450 300; sleep 1
gdump "$OUT/g-ui-r3.xml"
check "PG1: the tick on card 1 survived a restart of the guide" bash -c "tr '>' '\n' < $OUT/g-ui-r3.xml | grep -F 'text=\"Done\"' | head -1 | grep -q 'checked=\"true\"'"
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
if want ka; then # section ka
ensurehome
# ---- AA2: the keep-alive foreground service. Proof: it IS a foreground service, how it changes the launcher process's priority (oom_score_adj) with another app in front / the screen off, whether the
#      process survives `am kill-all`, that its notification is the quietest kind, and that "Restore everything" stops it.
beat "before KA"
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS >/dev/null 2>&1
kafeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string><boolean name=\\\"keep_alive\\\" value=\\\"$1\\\" /></map>\" > shared_prefs/tj_features.xml'"; }
adjof(){ local p; p=$(adb shell pidof $PKG | tr -d '\r' | awk '{print $1}'); if [ -n "$p" ]; then echo "pid=$p oom_score_adj=$(adb shell cat /proc/$p/oom_score_adj | tr -d '\r\n')"; else echo "NO PROCESS"; fi; }
kameasure(){ # kameasure <tag>: the launcher process priority with another app in front, with the screen off, and after `am kill-all`
  adb shell monkey -p com.android.contacts -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3
  KA_FRONT="$(adjof)"; info "KA $1: with Contacts in front: $KA_FRONT"
  adb shell input keyevent KEYCODE_SLEEP; sleep 4
  KA_OFF="$(adjof)"; info "KA $1: screen off: $KA_OFF"
  adb shell input keyevent KEYCODE_WAKEUP; sleep 2; adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
  adb shell am kill-all >/dev/null 2>&1; sleep 4
  KA_KILL="$(adjof)"; info "KA $1: after 'am kill-all': $KA_KILL"
}
# switch OFF first (the baseline): no foreground service, and what the process looks like without it
kafeat false; setmode custom; adb shell am force-stop "$PKG"; rebind; home; sleep 3
adb shell dumpsys activity services "$PKG" > "$OUT/ka-services-off.txt" 2>&1
kameasure OFF
check "AA2: with the switch OFF there is no foreground service" bash -c "! grep -q 'isForeground=true' $OUT/ka-services-off.txt"
# switch ON (the default)
kafeat true; adb shell am force-stop "$PKG"; rebind; home; sleep 4
adb shell dumpsys activity services "$PKG" > "$OUT/ka-services-on.txt" 2>&1
check "AA2: with the switch ON the keep-alive service is a foreground service" grep -q 'isForeground=true' "$OUT/ka-services-on.txt"
info "KA ON: service record: $(grep -E 'ServiceRecord|isForeground|foregroundId|foregroundNoti' $OUT/ka-services-on.txt | sed 's/^ *//' | cut -c1-140 | tr '\n' '|' | cut -c1-600)"
adb shell dumpsys notification --noredact 2>/dev/null | grep "tj_keep_alive" | grep -oE "mImportance=[0-9]+|importance=[0-9]+" | sort -u > "$OUT/ka-channel.txt"
info "KA ON: notification importance values seen: $(tr '\n' ' ' < $OUT/ka-channel.txt)"
check "AA2: its notification is silent (importance LOW or lower: Android floors a foreground service at LOW)" bash -c "grep -qE 'mImportance=(1|2)' $OUT/ka-channel.txt"
kameasure ON
check "AA2: the launcher process survives 'am kill-all' with the service" bash -c "echo '$KA_KILL' | grep -q 'oom_score_adj'"
check "AA2: with the service the process priority is at least foreground-service level (adj <= 200) with another app in front" bash -c "a=\$(echo '$KA_FRONT' | sed -n 's/.*oom_score_adj=\\([-0-9]*\\).*/\\1/p'); [ -n \"\$a\" ] && [ \"\$a\" -le 200 ]"
# "Restore everything" must stop it (L10): Quickstep mode, Launcher control, scroll to the end, tap the action and confirm
setmode quickstep; rebind; home; sleep 2
adb shell am start -n "$PKG/com.tj.launcher.ControlActivity" >/dev/null 2>&1; sleep 3
for i in 1 2 3 4 5 6 7 8 9 10 11 12 13 14; do adb shell input swipe 300 900 300 200 250; sleep 0.4; done
tapapp "Restore everything" 300 900; sleep 2; shot 110-ka-restore-dialog
tapapp "Restore" 0 0; sleep 1; tapapp "RESTORE" 0 0; sleep 3      # the dialog button may be drawn in capitals
adb shell dumpsys activity services "$PKG" > "$OUT/ka-services-restored.txt" 2>&1
adb shell "run-as $PKG cat shared_prefs/tj_features.xml" 2>/dev/null | tr '>' '\n' > "$OUT/ka-prefs-restored.txt"
info "KA after Restore everything: foreground service present: $(grep -c 'isForeground=true' $OUT/ka-services-restored.txt), keep_alive pref: $(grep -o 'keep_alive[^/]*' $OUT/ka-prefs-restored.txt | head -1)"
check "AA2: Restore everything stopped the keep-alive service" bash -c "! grep -q 'isForeground=true' $OUT/ka-services-restored.txt"
setfeat; setmode custom; home
fi # section ka
if want rs2; then # section rs2
ensurehome
# ---- RESEARCH 2 (info lines only): what may the SHELL user do on Android 10?  (a) StatusBarManager disable flags through the status-bar service (b) the raw power-key events (getevent / sendevent)
#      (c) the "hide a task but keep it alive" candidates, now with a correct task parser (some apps have a custom task affinity, e.g. android.task.contacts)
beat "before RS2"
setfeat; setmode custom; home; sleep 2
probe(){ adb shell "CLASSPATH=\$(pm path $PKG | head -1 | cut -d: -f2) app_process /system/bin com.tj.launcher.ShellProbe $*"; }
sbstate(){ adb shell dumpsys window windows 2>/dev/null | awk '/Window #[0-9]+ Window\{[^}]* StatusBar\}/{f=1;c=0} f{ if ($0 ~ /mFrame=/) print; c++; if (c>60) f=0 }' | head -1 | tr -s ' ' | cut -c1-80; }
# (a1) DISABLE2_GLOBAL_ACTIONS (0x8): does the stock power menu stay away?
( probe statusbar 0 0x8 22 > "$OUT/rs2-sb-globalactions.txt" 2>&1 & ) ; sleep 5
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 120-rs2-power-with-disable2
adb shell uiautomator dump /sdcard/rs2.xml >/dev/null 2>&1
info "RS2 a1 disable2(GLOBAL_ACTIONS=0x8): helper said [$(tr '\n' ' ' < $OUT/rs2-sb-globalactions.txt | cut -c1-400)]; power menu items on screen: $(adb shell cat /sdcard/rs2.xml 2>/dev/null | tr '>' '\n' | grep -oE 'text="(Power off|Restart|Screenshot)"' | tr '\n' ' ')"
adb shell input keyevent KEYCODE_BACK; sleep 18
# (a2) DISABLE_EXPAND (0x10000): can the shade still be pulled down / opened?
( probe statusbar 0x10000 0 20 > "$OUT/rs2-sb-expand.txt" 2>&1 & ) ; sleep 5
adb shell input swipe 300 2 300 600 150; sleep 1.5
adb shell cmd statusbar expand-settings; sleep 2
info "RS2 a2 disable(EXPAND=0x10000): helper said [$(tr '\n' ' ' < $OUT/rs2-sb-expand.txt | cut -c1-300)]; StatusBar window after a swipe AND expand-settings: $(sbstate)  (frame ending in 26 = still collapsed, 976 = expanded)"
adb shell cmd statusbar collapse; sleep 14
# (a3) DISABLE2_QUICK_SETTINGS (0x1) and DISABLE_NOTIFICATION_ICONS (0x20000)
( probe statusbar 0x20000 0x1 18 > "$OUT/rs2-sb-qs.txt" 2>&1 & ) ; sleep 5
adb shell cmd statusbar expand-settings; sleep 2.5; shot 121-rs2-qs-disabled
adb shell uiautomator dump /sdcard/rs2b.xml >/dev/null 2>&1
info "RS2 a3 disable(NOTIFICATION_ICONS) + disable2(QUICK_SETTINGS): helper said [$(tr '\n' ' ' < $OUT/rs2-sb-qs.txt | cut -c1-300)]; tiles on screen: $(adb shell cat /sdcard/rs2b.xml 2>/dev/null | tr '>' '\n' | grep -oE 'text="(Auto-rotate|Wi-Fi|Bluetooth)"' | tr '\n' ' ')"
adb shell cmd statusbar collapse; sleep 12
# (b) the raw power-key events: which input device has KEY_POWER, may the shell read it, and does a REAL kernel key event (sendevent) show up there and open the stock menu?
adb shell getevent -pl 2>&1 | awk '/add device/{d=$4} /name:/{n=$0} /KEY_POWER/{print d, n; exit}' > "$OUT/rs2-powerdev.txt"
POWERDEV=$(awk '{print $1}' "$OUT/rs2-powerdev.txt")
info "RS2 b device with KEY_POWER: [$(cat $OUT/rs2-powerdev.txt | tr -s ' ')]; all input devices: $(adb shell getevent -p 2>&1 | grep -E 'add device|name:' | tr -s ' ' | tr '\n' ' ' | cut -c1-400)"
if [ -n "$POWERDEV" ]; then
  ( adb shell "timeout 14 getevent -lt $POWERDEV" > "$OUT/rs2-getevent.txt" 2>&1 & ) ; sleep 2
  adb shell "sendevent $POWERDEV 1 116 1; sendevent $POWERDEV 0 0 0" > "$OUT/rs2-sendevent-down.txt" 2>&1
  sleep 1.3
  adb shell "sendevent $POWERDEV 1 116 0; sendevent $POWERDEV 0 0 0" > "$OUT/rs2-sendevent-up.txt" 2>&1
  sleep 2; shot 122-rs2-power-sendevent
  adb shell uiautomator dump /sdcard/rs2c.xml >/dev/null 2>&1
  info "RS2 b sendevent KEY_POWER held 1.3 s: sendevent said [$(cat $OUT/rs2-sendevent-down.txt $OUT/rs2-sendevent-up.txt | tr '\n' ' ' | cut -c1-200)]; getevent saw: $(grep -E 'KEY_POWER' $OUT/rs2-getevent.txt | tr -s ' ' | tr '\n' '|' | cut -c1-300); stock power menu on screen: $(adb shell cat /sdcard/rs2c.xml 2>/dev/null | tr '>' '\n' | grep -oE 'text="(Power off|Restart|Screenshot)"' | tr '\n' ' ')"
  adb shell input keyevent KEYCODE_BACK; sleep 1
else info "RS2 b skipped: no input device lists KEY_POWER"; fi
# (c) hide-a-task candidates with a correct parser: ids from `am stack list` ("taskId=N: pkg/activity")
adb shell monkey -p com.android.contacts -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3
adb shell monkey -p com.android.calendar -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3
adb shell am stack list > "$OUT/rs2-stacks.txt" 2>&1
awk '/Stack id=/{sid=$2; sub(/id=/,"",sid)} /taskId=[0-9]+:/{t=$1; sub(/taskId=/,"",t); sub(/:/,"",t); print sid, t, $2}' "$OUT/rs2-stacks.txt" > "$OUT/rs2-tasks.txt"
info "RS2 c tasks (stack task component): $(tr '\n' '|' < $OUT/rs2-tasks.txt | cut -c1-500)"
CT=$(grep 'com.android.contacts' "$OUT/rs2-tasks.txt" | head -1 | awk '{print $2}'); HS=$(grep -E 'com.tj.launcher|com.android.launcher3' "$OUT/rs2-tasks.txt" | head -1 | awk '{print $1}')
CALT=$(grep 'com.android.calendar' "$OUT/rs2-tasks.txt" | head -1 | awk '{print $1" "$2}')
recents(){ adb shell dumpsys activity recents 2>/dev/null | grep -E 'Recent #[0-9]+:|realActivity=' | sed 's/^ *//' | cut -c1-110 | tr '\n' '|' | cut -c1-700; }
info "RS2 c Recents before: $(recents)"
if [ -n "$CT" ] && [ -n "$HS" ]; then
  P0=$(adb shell pidof com.android.contacts | tr -d '\r\n')
  adb shell am stack movetask "$CT" "$HS" true > "$OUT/rs2-movetask.txt" 2>&1; sleep 2
  info "RS2 c1 'am stack movetask $CT $HS true' (contacts into the HOME stack) -> [$(tr '\n' ' ' < $OUT/rs2-movetask.txt | cut -c1-300)]; Recents after: $(recents); pid $P0 -> $(adb shell pidof com.android.contacts | tr -d '\r\n')"
else info "RS2 c1 skipped (contacts task=$CT home stack=$HS)"; fi
adb shell settings put global overlay_display_devices "600x1024/170"; sleep 4
NEWD=$(adb shell dumpsys activity activities 2>/dev/null | grep -oE '^ *Display #[0-9]+' | grep -oE '[0-9]+' | grep -v '^0$' | head -1)
CALS=$(echo "$CALT" | awk '{print $1}')
if [ -n "$NEWD" ] && [ -n "$CALS" ]; then
  adb shell am display move-stack "$CALS" "$NEWD" > "$OUT/rs2-movestack.txt" 2>&1; sleep 3
  info "RS2 c2 secondary display #$NEWD: 'am display move-stack $CALS $NEWD' -> [$(tr '\n' ' ' < $OUT/rs2-movestack.txt | cut -c1-300)]; Recents after: $(recents); calendar pid: $(adb shell pidof com.android.calendar | tr -d '\r\n')"
  shot 123-rs2-secondary-display
else info "RS2 c2 skipped (second display=$NEWD calendar stack=$CALS)"; fi
adb shell settings delete global overlay_display_devices >/dev/null 2>&1; sleep 3
adb shell am force-stop com.android.contacts; adb shell am force-stop com.android.calendar
setfeat; setmode custom; home
fi # section rs2
if want rt; then # section rt
ensurehome
# ---- AA3: the task-precise clean-up. With the shell helper running when a Quickstep session BEGINS, the tasks that exist then belong to the custom account. At the lock that ends the session only the NEW
#      tasks go: apps that exist only in Quickstep are closed as before, an app the custom account also has (Contacts here) is spared and loses only Quickstep's own extra task.
beat "before RT"
setfeat; setmode custom; rebind; home; sleep 2
adb shell 'CLASSPATH=$(pm path com.tj.launcher | head -1 | cut -d: -f2) setsid nohup app_process /system/bin com.tj.launcher.ShellServer >/data/local/tmp/tjshell.log 2>&1 &' >/dev/null 2>&1; sleep 5
rec(){ adb shell dumpsys activity recents 2>/dev/null | grep -oE '(realActivity|cmp|I)=[A-Za-z][A-Za-z0-9_.]*/' | sed 's/^[A-Za-z]*=//; s#/$##' | sort | uniq -c | tr -s ' ' | tr '\n' '|'; }
adb shell dumpsys activity recents > "$OUT/rt-recents-dump.txt" 2>&1; info "RT what Android prints for Recents (first lines): $(sed -n 1,16p $OUT/rt-recents-dump.txt | sed 's/^ *//' | cut -c1-110 | tr '\n' '|')"
# the custom account's world: Calendar (the target app here), Contacts (a real app; run 103 showed Android REPLACING its task, id 8 -> 20, once Quickstep opened it again) and the CI test app "ctl" started QUIET
# (no notification): its standard launch mode gives a REAL second task, so "one app, two worlds" is deterministic.
SN=com.tj.testnotifier.ctl
adb install -r -g testnotifier/build/outputs/apk/ctl/debug/testnotifier-ctl-debug.apk > "$OUT/rt-install-ctl.txt" 2>&1
check "AA3-S-1: the shared test app is installed" grep -q Success "$OUT/rt-install-ctl.txt"
for APP in com.android.contacts com.android.calendar; do adb shell monkey -p $APP -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 2; adb shell input keyevent KEYCODE_HOME; sleep 1; done
adb shell am start -n $SN/com.tj.testnotifier.MainActivity --ez quiet true >/dev/null 2>&1; sleep 2; adb shell input keyevent KEYCODE_HOME; sleep 1
info "RT custom account before Quickstep: $(rec)"
tl(){ adb shell dumpsys activity recents 2>/dev/null | grep -E '^ *\* Recent #' | sed -E 's/.*\{[0-9a-f]+ #([0-9]+) [AI]=([^ ]*) .*/\1:\2/; s/com\.android\.//' | tr '\n' ' '; }   # task id:affinity-or-component of every Recents task, newest first (run 99: Contacts vanished although spared)
SA=$(tl | tr ' ' '\n' | grep ":$SN\$" | head -1 | cut -d: -f1)
info "RT task ids before Quickstep: $(tl) (test app task: ${SA:-none})"
adb shell locksettings set-pin 1234 > "$OUT/rt-setpin.txt" 2>&1
adb logcat -c
lockpad; pad d d 1 2 3 4 e; sleep 7                      # the default handshake -> a Quickstep session BEGINS (Mode.set -> TaskOrigin.sessionStart)
getmode > "$OUT/rt-mode.txt"
adb logcat -d -s TjTasks:I TjA11y:I > "$OUT/rt-log-start.txt" 2>&1
info "RT session start: $(grep 'TjTasks' $OUT/rt-log-start.txt | tail -1 | cut -c1-300)"
check "AA3: a Quickstep session began (the handshake worked)" grep -q quickstep "$OUT/rt-mode.txt"
check "AA3: the custom account's tasks were remembered when it began" grep -q "session start: .* task(s) belong to the custom account" "$OUT/rt-log-start.txt"
# inside Quickstep: two apps that exist only here, Contacts again (real app: the task ids after each step are printed), and a REAL second task of the shared test app
for APP in com.android.gallery3d com.android.email com.android.contacts; do adb shell monkey -p $APP -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 2; adb shell input keyevent KEYCODE_HOME; sleep 1; info "RT task ids after opening ${APP#com.android.} in Quickstep: $(tl)"; done
# a REAL second task of the shared test app: another activity with its own task affinity (a relaunch with MULTIPLE_TASK is not it: Android then drops the OLDER task of the same affinity from Recents, run 106)
adb shell am start -n $SN/com.tj.testnotifier.SecondActivity >/dev/null 2>&1; sleep 2; adb shell input keyevent KEYCODE_HOME; sleep 1
info "RT during Quickstep: $(rec)"
TLB=$(tl); SB=$(echo "$TLB" | tr ' ' '\n' | grep ":$SN\.two\$" | head -1 | cut -d: -f1)      # SecondActivity's task: its affinity is <package>.two
info "RT task ids during Quickstep (just before the lock): $TLB (test app: custom task ${SA:-none}, Quickstep's second task ${SB:-none})"
check "AA3-S0: the shared test app has TWO tasks side by side in Recents (so the test means something)" bash -c "[ -n '$SA' ] && [ -n '$SB' ] && [ '$SA' != '$SB' ] && echo '$TLB' | tr ' ' '\n' | grep -q '^$SA:'"
SPID0=$(adb shell pidof $SN | tr -d '\r\n'); CPID0=$(adb shell pidof com.android.contacts | tr -d '\r\n'); CT0=$(echo "$TLB" | tr ' ' '\n' | grep ':android.task.contacts$' | head -1 | cut -d: -f1)
info "RT progress: about to lock, +${SECONDS}s (Contacts task ${CT0:-none}, processes: test app ${SPID0:-none}, Contacts ${CPID0:-none})"
adb logcat -c
adb shell input keyevent KEYCODE_POWER; sleep 10         # LOCK: the session ends; the helper cleans up before any wake
adb logcat -d -s TjReset:I TjA11y:I TjTasks:I > "$OUT/rt-log-lock.txt" 2>&1
info "RT after the lock: $(rec)"
info "RT the clean-up said: $(grep 'AT LOCK' $OUT/rt-log-lock.txt | tail -1 | cut -c1-420)"
info "RT the plan: $(grep 'session end' $OUT/rt-log-lock.txt | tail -1 | sed 's/^.*session end: //; s/base [^|]*| now [^|]*| //; s/com\.android\.//g' | cut -c1-340)"
TLA=$(tl); SPID1=$(adb shell pidof $SN | tr -d '\r\n'); CPID1=$(adb shell pidof com.android.contacts | tr -d '\r\n')
info "RT task ids after the lock: $TLA"
info "RT processes before -> after the lock: test app ${SPID0:-none} -> ${SPID1:-none}, Contacts ${CPID0:-none} -> ${CPID1:-none}"
info "RT progress: locked and read, +${SECONDS}s"
info "RT helper log tail: $(adb shell tail -n 6 /data/local/tmp/tjshell.log 2>/dev/null | tr '\n' '|' | cut -c1-300)"
adb shell dumpsys activity recents 2>/dev/null | grep -oE '(realActivity|cmp|I)=[A-Za-z][A-Za-z0-9_.]*/' | sed 's/^[A-Za-z]*=//; s#/$##' > "$OUT/rt-recents-after.txt"; adb shell dumpsys activity recents 2>/dev/null | grep 'Recent #' | sed 's/^ *//' | cut -c1-100 > "$OUT/rt-recents-headers-after.txt"
check "AA3: the lock-time clean-up used the task list (it reports the new/closed/removed/spared counts)" grep -q "tasks: [0-9]* new since the session began" "$OUT/rt-log-lock.txt"
check "AA3: Gallery (only opened in Quickstep) is gone from Recents" bash -c "! grep -qi 'gallery' $OUT/rt-recents-after.txt $OUT/rt-recents-headers-after.txt"
check "AA3: Email (only opened in Quickstep) is gone from Recents" bash -c "! grep -qi 'email' $OUT/rt-recents-after.txt $OUT/rt-recents-headers-after.txt"
check "AA3: Contacts (open in the custom account, only RESUMED by Quickstep) is left alone: the same task is still in Recents" bash -c "[ -n '$CT0' ] && echo '$TLA' | tr ' ' '\n' | grep -q '^$CT0:'"
check "AA3: Contacts is left alone: the same process before and after the lock" bash -c "[ -n '$CPID0' ] && [ '$CPID0' = '$CPID1' ]"
check "AA3-S1: Quickstep's SECOND task of the shared test app is gone after the lock (removed by id)" bash -c "[ -n '$SB' ] && ! echo '$TLA' | tr ' ' '\n' | grep -q '^$SB:'"
check "AA3-S2: the custom account's OWN task of the shared test app is still there (same id)" bash -c "[ -n '$SA' ] && echo '$TLA' | tr ' ' '\n' | grep -q '^$SA:'"
check "AA3-S3: the shared test app was not force-stopped (the same process before and after the lock)" bash -c "[ -n '$SPID0' ] && [ '$SPID0' = '$SPID1' ]"
check "AA3: Solitaire (Calendar here), the target, is untouched" bash -c "grep -qi 'calendar' $OUT/rt-recents-after.txt $OUT/rt-recents-headers-after.txt"
getmode > "$OUT/rt-mode-after.txt"
check "AA3: the custom account is active again behind the lock screen" grep -q custom "$OUT/rt-mode-after.txt"
info "RT progress: checks done, +${SECONDS}s"
adb shell input keyevent KEYCODE_WAKEUP; sleep 2; adb shell input swipe 300 900 300 300 300; sleep 2
pad 1 2 3 4 e; sleep 4
adb shell pkill -f com.tj.launcher.ShellServer >/dev/null 2>&1
info "RT progress: unlocked again, +${SECONDS}s"
adb uninstall $SN >/dev/null 2>&1
adb shell locksettings clear --old 1234 > "$OUT/rt-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
setfeat; setmode custom; home
fi # section rt
if want im; then # section im
ensurehome
# ---- AA1: the status-bar switch (Settings.Global policy_control = immersive.status=*) follows the launcher: hidden in the custom launcher, the ORIGINAL value (here a pre-existing one) back in Quickstep;
#      the lock screen and a plain PIN still work with it on.
beat "before IM"
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS >/dev/null 2>&1
imfeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string><boolean name=\\\"profile_hide_status_bar\\\" value=\\\"$1\\\" /></map>\" > shared_prefs/tj_features.xml'"; }
sbready(){ adb shell dumpsys window windows 2>/dev/null | awk '/Window #[0-9]+ Window\{[^}]* StatusBar\}/{f=1;c=0} f{ if ($0 ~ /isReadyForDisplay/) {print; exit} c++; if (c>60) exit}' | grep -o 'isReadyForDisplay()=[a-z]*' | head -1; }
adb shell settings delete global policy_control >/dev/null 2>&1
adb shell settings put global policy_control "immersive.preconfirms=*"        # a value somebody else set: it must survive and come back
imfeat true; setmode quickstep; rebind; home; sleep 2
adb shell locksettings set-pin 1234 > "$OUT/im-setpin.txt" 2>&1
lockpad; pad d d 1 2 3 4 e; sleep 7                                            # handshake -> Quickstep (Mode.set applies the QUICKSTEP value)
adb shell settings get global policy_control > "$OUT/im-quickstep.txt" 2>&1
info "IM Quickstep: policy_control='$(tr -d '\r\n' < $OUT/im-quickstep.txt)', status bar $(sbready)"
check "AA1: in Quickstep the original policy_control value is untouched" bash -c "[ \"\$(tr -d '\r\n' < $OUT/im-quickstep.txt)\" = 'immersive.preconfirms=*' ]"
check "AA1: in Quickstep the status bar is shown" bash -c "[ \"\$(echo '$(sbready)')\" = 'isReadyForDisplay()=true' ]"
adb shell am start -n "$PKG/com.tj.launcher.BackToCustomActivity" >/dev/null 2>&1; sleep 6   # back to custom: Mode.set(CUSTOM) applies the hidden bar
adb shell settings get global policy_control > "$OUT/im-custom.txt" 2>&1
info "IM custom: policy_control='$(tr -d '\r\n' < $OUT/im-custom.txt)', status bar $(sbready)"
check "AA1: in the custom launcher policy_control = the original entry + immersive.status=*" bash -c "[ \"\$(tr -d '\r\n' < $OUT/im-custom.txt)\" = 'immersive.preconfirms=*:immersive.status=*' ]"
check "AA1: in the custom launcher the status bar is hidden" bash -c "[ \"\$(echo '$(sbready)')\" = 'isReadyForDisplay()=false' ]"
shot 130-im-custom-home
adb shell input swipe 300 2 300 350 150; sleep 1.5
adb shell input swipe 300 2 300 350 150; sleep 1.5
adb shell dumpsys window windows 2>/dev/null | awk '/Window #[0-9]+ Window\{[^}]* StatusBar\}/{f=1;c=0} f{ if ($0 ~ /mFrame=/) {print; exit} c++; if (c>60) exit}' | tr -s ' ' > "$OUT/im-frame.txt"
check "AA1: it is a quieter look, NOT a lock: a second swipe from the top still opens the panel (so the rotation tile stays reachable)" grep -q '\[0,0\]\[600,976\]' "$OUT/im-frame.txt"
adb shell cmd statusbar collapse; sleep 1.5
lockpad; pad 1 2 3 4 e; sleep 6                                                  # the lock screen + a plain PIN with the bar hidden
resumed > "$OUT/im-unlock.txt"
check "AA1: the lock screen and a plain PIN still work with the bar hidden (custom stack in front)" bash -c "grep -qiE 'calendar|com.tj.launcher' $OUT/im-unlock.txt"
lockpad; pad d d 1 2 3 4 e; sleep 7                                             # handshake again -> Quickstep: the original comes back
adb shell settings get global policy_control > "$OUT/im-quickstep2.txt" 2>&1
check "AA1: back in Quickstep the original value is restored exactly" bash -c "[ \"\$(tr -d '\r\n' < $OUT/im-quickstep2.txt)\" = 'immersive.preconfirms=*' ]"
adb shell settings delete global policy_control >/dev/null 2>&1
adb shell locksettings clear --old 1234 > "$OUT/im-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
setfeat; setmode custom; home
fi # section im
if want rs3; then # section rs3
ensurehome
# ---- RESEARCH 3 (info lines only): (a) the corrected 'am stack move-task' into the HOME stack (b) a REAL power-key press written into the emulator's "Power Button" device by the emulator console:
#      does the shell (getevent) see it BEFORE the stock menu appears, and with DISABLE2_GLOBAL_ACTIONS set does the key still arrive while NO stock menu opens?
beat "before RS3"
rawfeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string><boolean name=\\\"raw_key_log\\\" value=\\\"true\\\" /></map>\" > shared_prefs/tj_features.xml'"; }
rawfeat; setmode custom; rebind; home; sleep 2
probe(){ adb shell "CLASSPATH=\$(pm path $PKG | head -1 | cut -d: -f2) app_process /system/bin com.tj.launcher.ShellProbe $*"; }
recents(){ adb shell dumpsys activity recents 2>/dev/null | grep -E 'Recent #[0-9]+:' | sed 's/^ *//' | cut -c1-95 | tr '\n' '|' | cut -c1-600; }
adb shell monkey -p com.android.contacts -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3
adb shell am stack list > "$OUT/rs3-stacks.txt" 2>&1
awk '/Stack id=/{sid=$2; sub(/id=/,"",sid)} /taskId=[0-9]+:/{t=$1; sub(/taskId=/,"",t); sub(/:/,"",t); print sid, t, $2}' "$OUT/rs3-stacks.txt" > "$OUT/rs3-tasks.txt"
CT=$(grep 'com.android.contacts' "$OUT/rs3-tasks.txt" | head -1 | awk '{print $2}'); HS=$(grep -E 'com.tj.launcher|com.android.launcher3' "$OUT/rs3-tasks.txt" | head -1 | awk '{print $1}')
info "RS3 a am stack help: $(adb shell am stack help 2>&1 | head -12 | tr -s ' ' | tr '\n' '|' | cut -c1-500)"
if [ -n "$CT" ] && [ -n "$HS" ]; then
  P0=$(adb shell pidof com.android.contacts | tr -d '\r\n')
  adb shell am stack move-task "$CT" "$HS" true > "$OUT/rs3-movetask.txt" 2>&1; sleep 2
  info "RS3 a 'am stack move-task $CT $HS true' (contacts into the HOME stack) -> [$(tr '\n' ' ' < $OUT/rs3-movetask.txt | cut -c1-300)]; Recents after: $(recents); pid $P0 -> $(adb shell pidof com.android.contacts | tr -d '\r\n')"
  adb shell am stack list 2>&1 | grep -E 'Stack id=|taskId=' | sed 's/^ *//' | cut -c1-90 | tr '\n' '|' > "$OUT/rs3-stacks-after.txt"; info "RS3 a stacks after: $(cat $OUT/rs3-stacks-after.txt | cut -c1-500)"
else info "RS3 a skipped (contacts task=$CT home stack=$HS)"; fi
adb shell am force-stop com.android.contacts; home
POWERDEV=$(adb shell getevent -pl 2>&1 | awk '/add device/{d=$4} /KEY_POWER/{print d; exit}')
info "RS3 b power device: $POWERDEV; emulator console: $(adb emu help 2>&1 | head -3 | tr '\n' ' ' | cut -c1-200)"
presskey(){ # presskey <hold seconds>: a real kernel-level power key press through the emulator console
  adb emu event send 1:116:1 0:0:0 > "$OUT/rs3-emu-down.txt" 2>&1; sleep "$1"; adb emu event send 1:116:0 0:0:0 > "$OUT/rs3-emu-up.txt" 2>&1; }
if [ -n "$POWERDEV" ]; then
  adb logcat -c
  ( adb shell "timeout 12 getevent -lt $POWERDEV" > "$OUT/rs3-getevent.txt" 2>&1 & ) ; sleep 2
  adb emu event send 1:115:1 0:0:0 >/dev/null 2>&1; sleep 0.3; adb emu event send 1:115:0 0:0:0 >/dev/null 2>&1; sleep 1      # control: a volume-up key does reach the accessibility key filter
  presskey 1.4; sleep 2; shot 131-rs3-power-console
  adb logcat -d -s TjA11y:I 2>/dev/null | grep KEYRAW | sed 's/.*KEYRAW/KEYRAW/' | sort | uniq -c | tr -s ' ' | tr '\n' '|' > "$OUT/rs3-keyraw.txt"
  info "RS3 b0 key codes the accessibility helper's key filter was handed (volume-up control + the real power press): $(cat $OUT/rs3-keyraw.txt)"
  adb shell uiautomator dump /sdcard/rs3.xml >/dev/null 2>&1
  adb logcat -d -v epoch -s TjA11y:I 2>/dev/null | grep 'menuOpen=true' | head -1 | cut -c1-120 > "$OUT/rs3-menu-seen.txt"
  info "RS3 b1 console said [$(cat $OUT/rs3-emu-down.txt $OUT/rs3-emu-up.txt | tr '\n' ' ' | cut -c1-160)]; getevent saw: $(grep -E 'KEY_POWER' $OUT/rs3-getevent.txt | tr -s ' ' | tr '\n' '|' | cut -c1-260); our helper saw the stock menu at: $(cat $OUT/rs3-menu-seen.txt); menu items on screen: $(adb shell cat /sdcard/rs3.xml 2>/dev/null | tr '>' '\n' | grep -oE 'text="(Power off|Restart|Screenshot)"' | tr '\n' ' ')"
  adb shell input keyevent KEYCODE_BACK; sleep 2
  ( probe statusbar 0 0x8 22 > "$OUT/rs3-sb.txt" 2>&1 & ) ; sleep 5
  ( adb shell "timeout 10 getevent -lt $POWERDEV" > "$OUT/rs3-getevent2.txt" 2>&1 & ) ; sleep 1.5
  presskey 1.4; sleep 2; shot 132-rs3-power-console-disabled
  adb shell uiautomator dump /sdcard/rs3b.xml >/dev/null 2>&1
  info "RS3 b2 with DISABLE2_GLOBAL_ACTIONS: probe said [$(tr '\n' ' ' < $OUT/rs3-sb.txt | cut -c1-200)]; getevent saw: $(grep -E 'KEY_POWER' $OUT/rs3-getevent2.txt | tr -s ' ' | tr '\n' '|' | cut -c1-260); menu items on screen: $(adb shell cat /sdcard/rs3b.xml 2>/dev/null | tr '>' '\n' | grep -oE 'text="(Power off|Restart|Screenshot)"' | tr '\n' ' ')"
  adb shell input keyevent KEYCODE_BACK; sleep 16
  info "RS3 b3 after the probe ended the stock menu is back: $(adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; adb shell uiautomator dump /sdcard/rs3c.xml >/dev/null 2>&1; adb shell cat /sdcard/rs3c.xml 2>/dev/null | tr '>' '\n' | grep -oE 'text="(Power off|Restart|Screenshot)"' | tr '\n' ' ')"
  adb shell input keyevent KEYCODE_BACK; sleep 1
else info "RS3 b skipped: no device lists KEY_POWER"; fi
setfeat; setmode custom; home
fi # section rs3
if want gc; then # section gc
ensurehome
# ---- AA5 (Tj has the OTG cable ONCE): EVERY command line of the phone guide (cmd:, alt: and undo:) is run in the emulator's device shell, exactly as an ADB OTG shell takes it (no "adb shell" prefix).
#      A typo or a command Android 10 does not have is found here for free; found on the tablet it would cost the only cable session.
beat "before GC"
setfeat; setmode custom; home; sleep 2
STEPS=phoneguide/src/main/assets/steps.txt
GCN=0; GCBAD=0; GCSKIP=0
gcrun(){ # gcrun <kind> <command line>
  local kind="$1" c="$2" o rc
  case "$c" in *CLASSPATH=PATH*) GCSKIP=$((GCSKIP+1)); return;; esac  # the two-step placeholder (the word PATH to be replaced by hand), not a command. NOT *PATH*: "CLASSPATH=" contains it, which skipped every helper-start and probe line until run 110
  # </dev/null: `adb shell` forwards ITS stdin to the device, and here stdin is the steps file the while-loop below is reading: run 99 ran 2 of ~60 lines because the first command swallowed the rest of the file
  # the guide offers the forms of the start line as ALTERNATIVES, not as a sequence: each is tried from a stopped helper, and must leave it running
  local starts=0; case "$c" in *ShellServer*) case "$c" in *pkill*|*grep*|*ShellProbe*) ;; *) starts=1;; esac;; esac
  [ "$starts" = 1 ] && { adb shell "pkill -f com.tj.launcher.ShellServer" </dev/null >/dev/null 2>&1; sleep 1; }
  o="$(adb shell "$c" </dev/null 2>&1)"; rc=$?
  if [ "$starts" = 1 ]; then
    sleep 4
    local n; n=$(adb shell "ps -A -o ARGS | grep -c '[S]hellServer'" </dev/null 2>&1 | tr -d '\r\n ')
    info "GC after the $kind start line [$(echo "$c" | cut -c1-60)...] the helper processes running: ${n:-?} ($(adb shell 'ps -A -o PID,PPID,STAT,ARGS | grep -iE "app_process|[S]hellServer" | grep -v grep' </dev/null 2>&1 | tr -s ' ' | cut -c1-90 | tr '\n' '|' | cut -c1-230))"
    [ "${n:-0}" -ge 1 ] 2>/dev/null || { GCBAD=$((GCBAD+1)); check "AA5: the guide's $kind start line left the helper running (processes: ${n:-?}; the line printed: $(echo "$o" | head -2 | tr '\n' ' ' | cut -c1-120))" false; }
  fi
  GCN=$((GCN+1))
  case "$c" in
    *"grep -c '[S]hellServer'"*) [ "$(echo "$o" | tr -d '\r\n ')" -ge 1 ] 2>/dev/null || { GCBAD=$((GCBAD+1)); check "AA5: the guide's 'Is the helper running?' card printed 1 after its start line (printed: $o)" false
         info "GC helper gone: processes now: $(adb shell 'ps -A -o PID,PPID,STAT,ARGS | grep -iE "app_process|[S]hellServer" | grep -v grep' </dev/null 2>&1 | tr -s ' ' | cut -c1-90 | tr '\n' '|' | cut -c1-230)"
         info "GC helper gone: log: $(adb logcat -d -b all 2>/dev/null | grep -iE 'ShellServer|app_process|lowmemorykiller|am_kill' | tail -4 | cut -c1-150 | tr '\n' '|' | cut -c1-330)"; }; return;;
    *"compile -m speed"*) echo "$o" | grep -qi success || { GCBAD=$((GCBAD+1)); check "AA5: the guide's compile card ended with Success (printed: $(echo "$o" | head -1 | cut -c1-120))" false; }; return;;
    *"ShellProbe statusbar"*) echo "$o" | grep -q 'disable2(0x8) -> OK' || { GCBAD=$((GCBAD+1)); check "AA5: the ShellProbe card answered OK (printed: $(echo "$o" | head -3 | tr '\n' ' ' | cut -c1-200))" false; }; return;;
  esac
  if echo "$o" | grep -q 'Unknown package'; then info "GC $kind [$c]: that package is not on this emulator (fine)"; return; fi
  if echo "$o" | grep -qiE 'not found|unknown (option|command)|usage:|syntax error|no such file|exception|error:|permission denied|inaccessible' || { [ "$rc" != 0 ] && ! echo "$c" | grep -qE 'grep|head|pkill'; }; then   # grep and pkill answer 1 for "nothing matched", which is not an error
    GCBAD=$((GCBAD+1)); check "AA5: guide $kind line works: [$(echo "$c" | cut -c1-110)] -> rc=$rc $(echo "$o" | head -2 | tr '\n' ' ' | cut -c1-160)" false
  fi
}
while IFS= read -r L; do case "$L" in cmd:*|alt:*) gcrun "${L%%:*}" "$(echo "${L#*:}" | sed 's/^ *//')";; esac; done < "$STEPS"
info "GC permissions granted after the first loop: $(adb shell dumpsys package $PKG | grep -c 'granted=true')"
while IFS= read -r L; do case "$L" in undo:*) U="$(echo "${L#*:}" | sed 's/^ *//')"; case "$U" in pm\ *|cmd\ *|settings\ *|dumpsys\ *|pkill\ *|am\ *) gcrun undo "$U";; esac;; esac; done < "$STEPS"
GCWANT=$(( $(grep -cE '^(cmd|alt):' "$STEPS") + $(grep -E '^undo:' "$STEPS" | sed 's/^undo: *//' | grep -cE '^(pm|cmd|settings|dumpsys|pkill|am) ') ))
info "GC ran $GCN guide command lines ($GCSKIP placeholder lines skipped) of $GCWANT in the file, $GCBAD problem(s)"
check "AA5: the loops reached EVERY phone-guide command line (ran $GCN + skipped $GCSKIP of $GCWANT)" bash -c "[ $((GCN+GCSKIP)) = $GCWANT ]"
check "AA5: every phone-guide command line (cmd, alt, undo) ran without an error in the device shell" bash -c "[ $GCBAD = 0 ]"
adb shell settings delete global policy_control >/dev/null 2>&1; adb shell settings put global device_demo_mode 0 >/dev/null 2>&1; adb shell pm enable com.android.browser >/dev/null 2>&1
adb shell pkill -f com.tj.launcher.ShellServer >/dev/null 2>&1
# the undo lines above revoked what the scenario had granted at its start (DUMP) and what other sections grant for themselves: put it back, so every section after this one runs as it did before
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS >/dev/null 2>&1
adb shell pm grant "$PKG" android.permission.DUMP >/dev/null 2>&1
rebind; setfeat; setmode custom; home
fi # section gc
if want rs4; then # section rs4
ensurehome
# ---- RESEARCH 4 (info lines only), the "keep Quickstep's apps OPEN but hidden" ideas that the shell's new powers make testable:
#      (a) DISABLE_RECENT (0x1000000): does the system Recents button stop opening Recents, so the custom side could offer its OWN list and Quickstep's tasks stay alive but unreachable?
#      (b) a Do-Not-Disturb policy that hides the notification LIST (SUPPRESSED_EFFECT_NOTIFICATION_LIST): are other apps' notifications hidden while they keep running?
beat "before RS4"
setfeat; setmode custom; home; sleep 2
probe(){ adb shell "CLASSPATH=\$(pm path $PKG | head -1 | cut -d: -f2) app_process /system/bin com.tj.launcher.ShellProbe $*"; }
adb shell input keyevent KEYCODE_APP_SWITCH; sleep 3; resumed > "$OUT/rs4-recents-base.txt"; shot 140-rs4-recents-baseline
info "RS4 a baseline: after the Recents key the front activity is: $(cat $OUT/rs4-recents-base.txt | tr -s ' ' | cut -c1-140)"
adb shell input keyevent KEYCODE_HOME; sleep 2
( probe statusbar 0x1000000 0 24 > "$OUT/rs4-sb.txt" 2>&1 & ) ; sleep 5
adb shell uiautomator dump /sdcard/rs4a.xml >/dev/null 2>&1
adb shell input keyevent KEYCODE_APP_SWITCH; sleep 3; resumed > "$OUT/rs4-recents-disabled.txt"; shot 141-rs4-recents-disabled
info "RS4 a with DISABLE_RECENT: probe said [$(tr '\n' ' ' < $OUT/rs4-sb.txt | cut -c1-200)]; nav-bar recents button in the screen dump: $(adb shell cat /sdcard/rs4a.xml 2>/dev/null | tr '>' '\n' | grep -c 'recent_apps') node(s); after the Recents key the front activity is: $(cat $OUT/rs4-recents-disabled.txt | tr -s ' ' | cut -c1-140)"
adb shell input keyevent KEYCODE_HOME; sleep 19
# (b)
adb shell cmd notification allow_dnd com.android.shell >/dev/null 2>&1
adb shell cmd notification post -t HELLOTITLE tagx hellobody > "$OUT/rs4-post.txt" 2>&1; sleep 2
adb shell cmd statusbar expand-notifications; sleep 2.5
adb shell uiautomator dump /sdcard/rs4b.xml >/dev/null 2>&1; shot 142-rs4-shade-baseline
info "RS4 b baseline: posted [$(tr '\n' ' ' < $OUT/rs4-post.txt | cut -c1-120)]; the notification is in the shade: $(adb shell cat /sdcard/rs4b.xml 2>/dev/null | grep -c HELLOTITLE) time(s)"
adb shell cmd statusbar collapse; sleep 1.5
( probe notifpolicy 0x1FC 2 22 > "$OUT/rs4-dnd.txt" 2>&1 & ) ; sleep 6
adb shell cmd statusbar expand-notifications; sleep 2.5
adb shell uiautomator dump /sdcard/rs4c.xml >/dev/null 2>&1; shot 143-rs4-shade-with-policy
info "RS4 b with the DND policy (hide list, priority-only): the notification is in the shade: $(adb shell cat /sdcard/rs4c.xml 2>/dev/null | grep -c HELLOTITLE) time(s); zen_mode=$(adb shell settings get global zen_mode | tr -d '\r\n'); probe ended OK: $(grep -c 'OK' $OUT/rs4-dnd.txt)"
info "RS4 b the probe said (tail): $(tr '\n' ' ' < $OUT/rs4-dnd.txt | sed 's/.*suppressedVisualEffects/suppressedVisualEffects/' | cut -c1-330)"
adb shell cmd statusbar collapse; sleep 17
adb shell cmd statusbar expand-notifications; sleep 2.5
adb shell uiautomator dump /sdcard/rs4d.xml >/dev/null 2>&1
info "RS4 b after the probe ended: the notification is back in the shade: $(adb shell cat /sdcard/rs4d.xml 2>/dev/null | grep -c HELLOTITLE) time(s); zen_mode=$(adb shell settings get global zen_mode | tr -d '\r\n')"
adb shell cmd statusbar collapse; sleep 1
adb shell cmd notification cancel tagx >/dev/null 2>&1; adb shell cmd notification disallow_dnd com.android.shell >/dev/null 2>&1; adb shell settings put global zen_mode 0 >/dev/null 2>&1
setfeat; setmode custom; home
fi # section rs4
if want pf; then # section pf
ensurehome
# ---- AB3 (Tj, 2026-11-05): "I want the notification panel to work normally in the custom launcher, but to always only have the one settings tile, regardless of the tiles i set for quickstep. I also want it
#      to hide the pencil or remove it, never show wifi or Bluetooth, and never show notifications from apps opened in the quickstep launcher."
#      Two CI-only test apps post a notification every time they are opened: "quick" (com.tj.testnotifier) is the app Tj opens in Quickstep, "ctl" (com.tj.testnotifier.ctl) is an app only the custom launcher
#      opened, whose notification must STAY. Pass A: an app only Quickstep opened. Pass B: an app BOTH launchers use (opened in custom first, then again in Quickstep).
beat "before PF"
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS >/dev/null 2>&1
QN=com.tj.testnotifier; CN=com.tj.testnotifier.ctl
adb install -r -g testnotifier/build/outputs/apk/quick/debug/testnotifier-quick-debug.apk > "$OUT/pf-install-quick.txt" 2>&1
adb install -r -g testnotifier/build/outputs/apk/ctl/debug/testnotifier-ctl-debug.apk > "$OUT/pf-install-ctl.txt" 2>&1
check "PF0: both test apps are installed" bash -c "grep -q Success $OUT/pf-install-quick.txt && grep -q Success $OUT/pf-install-ctl.txt"
pf_notifs(){ adb shell dumpsys notification 2>/dev/null | grep 'NotificationRecord(' | grep -o 'pkg=[A-Za-z0-9_.]* user=UserHandle{[0-9]*}' | sort | uniq -c | tr -s ' ' | tr '\n' '|'; }
pf_has(){ adb shell dumpsys notification 2>/dev/null | grep 'NotificationRecord(' | grep -q "pkg=$1 user="; }
pf_hasnt(){ ! pf_has "$1"; }
pf_tiles(){ adb shell dumpsys activity service com.android.systemui/.SystemUIService 2>/dev/null | grep -oE '[A-Za-z]+Tile:' | sort -u | tr '\n' ' '; }
pf_dump(){ adb shell uiautomator dump /sdcard/tjpf.xml >/dev/null 2>&1; adb shell cat /sdcard/tjpf.xml 2>/dev/null | tr '>' '\n' > "$OUT/pf-$1.xml"; }
# "tracked": the steady state after the first return (the one-time "first run closes every non-custom app" is not what is under test)
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"tracked\\\" value=\\\"true\\\" /></map>\" > shared_prefs/tj_origin.xml'"
setfeat; setmode custom; rebind; home; sleep 2
adb shell 'CLASSPATH=$(pm path com.tj.launcher | head -1 | cut -d: -f2) setsid nohup app_process /system/bin com.tj.launcher.ShellServer >/data/local/tmp/tjshell.log 2>&1 &' >/dev/null 2>&1; sleep 5
adb shell locksettings set-pin 1234 > "$OUT/pf-setpin.txt" 2>&1
adb shell monkey -p $CN -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3; adb shell input keyevent KEYCODE_HOME; sleep 1     # the custom account's own app: its notification must stay
info "PF custom account before Quickstep: notifications [$(pf_notifs)]"
check "PF0b: the custom-only test app posted its notification" pf_has $CN
# ---------------- pass A: an app only Quickstep opened
lockpad; pad d d 1 2 3 4 e; sleep 7                                   # the default handshake -> Quickstep
getmode > "$OUT/pf-mode-a.txt"
check "PF-A0: a Quickstep session began" grep -q quickstep "$OUT/pf-mode-a.txt"
adb shell settings put secure sysui_qs_tiles "wifi,bt,rotation,flashlight,airplane" >/dev/null 2>&1   # Tj arranges Quickstep's tiles with the pencil: here, written directly
adb shell monkey -p $QN -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3; adb shell input keyevent KEYCODE_HOME; sleep 1
info "PF A in Quickstep: notifications [$(pf_notifs)]"
check "PF-A1: the Quickstep app posted its notification (so the test means something)" pf_has $QN
adb logcat -c
adb shell input keyevent KEYCODE_POWER; sleep 10                      # LOCK: the session ends; the clean-up runs behind the lock screen
adb logcat -d -s TjReset:I TjA11y:I TjTasks:I > "$OUT/pf-log-a.txt" 2>&1
info "PF A after the lock: notifications [$(pf_notifs)]; the clean-up said: $(grep 'AT LOCK' $OUT/pf-log-a.txt | tail -1 | cut -c1-420)"
check "PF-A2: the notification of the app opened in Quickstep is GONE" pf_hasnt $QN
check "PF-A3: the notification of the app opened only in the custom launcher STAYS" pf_has $CN
adb shell input keyevent KEYCODE_WAKEUP; sleep 2; adb shell input swipe 300 900 300 300 300; sleep 2
pad 1 2 3 4 e; sleep 5
getmode > "$OUT/pf-mode-a2.txt"
check "PF-A4: the custom launcher is active again" grep -q custom "$OUT/pf-mode-a2.txt"
# ---------------- the panel in the custom launcher
adb shell settings get secure sysui_qs_tiles > "$OUT/pf-tiles-setting.txt" 2>&1
pf_tiles > "$OUT/pf-tiles-live.txt"
info "PF custom: tile setting '$(tr -d '\r\n' < $OUT/pf-tiles-setting.txt)', SystemUI tiles: $(cat $OUT/pf-tiles-live.txt)"
check "PF-T1: whatever Quickstep's tiles were (wifi,bt,rotation,flashlight,airplane), the custom setting is just rotation" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/pf-tiles-setting.txt)\" = rotation ]"
check "PF-T2: SystemUI shows the rotation tile and no Wi-Fi / Bluetooth / flashlight / airplane tile" bash -c "grep -q RotationLockTile $OUT/pf-tiles-live.txt && ! grep -qE 'WifiTile|BluetoothTile|FlashlightTile|AirplaneModeTile' $OUT/pf-tiles-live.txt"
adb shell input swipe 300 3 300 700 350; sleep 2.5                    # ONE swipe from the top edge
pf_dump shade1; shot 140-pf-shade-one-swipe
check "PF-S1: one swipe opens the panel and the custom app's notification is in it" grep -q "text=\"NOTIFIER $CN\"" "$OUT/pf-shade1.xml"
check "PF-S2: the Quickstep app's notification is not in the panel" bash -c "! grep -q 'text=\"NOTIFIER $QN\"' $OUT/pf-shade1.xml"
check "PF-S3: no Wi-Fi or Bluetooth tile in the panel" bash -c "! grep -qiE 'content-desc=\"(wi-?fi|bluetooth)' $OUT/pf-shade1.xml"
check "PF-S4: the rotation tile is in the panel" grep -qiE 'content-desc="[^"]*(auto-rotate|portrait|landscape)' "$OUT/pf-shade1.xml"
adb shell cmd statusbar collapse; sleep 1.5
adb shell cmd statusbar expand-settings; sleep 3; shot 141-pf-qs-full
adb shell dumpsys window windows | grep -c TjPencilCover > "$OUT/pf-pencil-windows.txt" 2>&1     # BEFORE the uiautomator dump: a dump connects UiAutomation, which makes Android unbind every accessibility service for a moment (the cover goes with it)
pf_dump qs
check "PF-P1: a cover window sits over the pencil while the full quick settings are open" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/pf-pencil-windows.txt)\" -ge 1 ]"
check "PF-P2: still no Wi-Fi or Bluetooth tile in the full quick settings" bash -c "! grep -qiE 'content-desc=\"(wi-?fi|bluetooth)' $OUT/pf-qs.xml"
adb shell cmd statusbar collapse; sleep 1.5
# ---------------- pass B: an app BOTH launchers use
adb shell monkey -p $QN -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3; adb shell input keyevent KEYCODE_HOME; sleep 1   # opened in the CUSTOM launcher first
check "PF-B0: the shared app posted a notification in the custom launcher" pf_has $QN
lockpad; pad d d 1 2 3 4 e; sleep 7                                   # Quickstep session 2
adb shell monkey -p $QN -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3; adb shell input keyevent KEYCODE_HOME; sleep 1   # and again in Quickstep
info "PF B in Quickstep: notifications [$(pf_notifs)]; pid of the shared app: $(adb shell pidof $QN | tr -d '\r\n')"
adb logcat -c
adb shell input keyevent KEYCODE_POWER; sleep 10
adb logcat -d -s TjReset:I TjA11y:I TjTasks:I > "$OUT/pf-log-b.txt" 2>&1
info "PF B after the lock: notifications [$(pf_notifs)]; the clean-up said: $(grep 'AT LOCK' $OUT/pf-log-b.txt | tail -1 | cut -c1-520)"
check "PF-B1: the shared app's notification is GONE (it is an app opened in Quickstep)" pf_hasnt $QN
check "PF-B2: the clean-up says it closed a shared app because of its notification" grep -q 'shared app(s) closed because they have a notification' "$OUT/pf-log-b.txt"
check "PF-B3: the custom-only app's notification still STAYS" pf_has $CN
adb shell input keyevent KEYCODE_WAKEUP; sleep 2; adb shell input swipe 300 900 300 300 300; sleep 2
pad 1 2 3 4 e; sleep 4
adb shell pkill -f com.tj.launcher.ShellServer >/dev/null 2>&1
adb shell locksettings clear --old 1234 > "$OUT/pf-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
adb uninstall $QN >/dev/null 2>&1; adb uninstall $CN >/dev/null 2>&1       # the test apps and their notifications go
setfeat; setmode custom; home
fi # section pf
if want rb; then # section rb
ensurehome
# ---- AB1 (Tj, 2026-11-05: "It is important that this project doesn't break anything or require me to factory reset my tablet"): the tablet RESTARTS with the riskiest options on (PIN set, hidden status bar,
#      one tile, keep-alive service, helper enabled) and comes back normally: boots, ONE user (no surprise demo user), no demo flag, the lock screen + PIN work, Home works, no crash, crash breaker not tripped.
beat "before RB"
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS >/dev/null 2>&1
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string><boolean name=\\\"profile_hide_status_bar\\\" value=\\\"true\\\" /><boolean name=\\\"keep_alive\\\" value=\\\"true\\\" /></map>\" > shared_prefs/tj_features.xml'"
setmode custom; rebind; home; sleep 2
adb shell locksettings set-pin 1234 > "$OUT/rb-setpin.txt" 2>&1
lockpad; pad d d 1 2 3 4 e; sleep 7                                        # handshake -> Quickstep
adb shell am start -n "$PKG/com.tj.launcher.BackToCustomActivity" >/dev/null 2>&1; sleep 6   # back to custom: Mode.set applies the tiles + the status-bar setting
adb shell settings get secure sysui_qs_tiles > "$OUT/rb-tiles-before.txt" 2>&1
adb shell settings get global policy_control > "$OUT/rb-policy-before.txt" 2>&1
adb shell pm list users > "$OUT/rb-users-before.txt" 2>&1
info "RB before the restart: tiles '$(tr -d '\r\n' < $OUT/rb-tiles-before.txt)', policy_control '$(tr -d '\r\n' < $OUT/rb-policy-before.txt)', users: $(grep -c 'UserInfo' $OUT/rb-users-before.txt), mode: $(getmode)"
adb shell "run-as $PKG cat shared_prefs/tj_crash.xml 2>/dev/null" > "$OUT/rb-crash-before.txt"
adb reboot >/dev/null 2>&1
# 1. wait until the OLD system is really gone (the first run read boot_completed=1 from it and carried on while Android was still shutting down)
for i in $(seq 1 60); do [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r\n')" = 1 ] || break; sleep 2; done
# 2. wait for the NEW system (bounded: 5 minutes)
RBOOT=0; RB_T0=$SECONDS
while [ $((SECONDS - RB_T0)) -lt 300 ]; do
  if [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r\n')" = 1 ]; then RBOOT=1; break; fi
  sleep 4
done
info "RB the tablet came back after the restart: $([ $RBOOT = 1 ] && echo yes || echo NO) (about $((SECONDS - RB_T0)) s after the old system was gone)"
check "RB1: the tablet boots again with every option on" [ "$RBOOT" = 1 ]
( command adb logcat -v time '*:W' TjA11y:I TjReset:I TjRouter:I TjHooks:I TjRecentsGuard:I >> "$OUT/logcat-stream.txt" 2>&1 & ) 2>/dev/null   # the stream died with the restart
sleep 10
adb shell pm list users > "$OUT/rb-users-after.txt" 2>&1
check "RB2: exactly one user exists after the restart (no demo user was created)" bash -c "[ \"\$(grep -c 'UserInfo' $OUT/rb-users-after.txt)\" = 1 ]"
adb shell settings get global device_demo_mode > "$OUT/rb-demo-after.txt" 2>&1
check "RB3: the retail-demo flag is off" bash -c "v=\$(tr -d ' \r\n' < $OUT/rb-demo-after.txt); [ \"\$v\" = 0 ] || [ \"\$v\" = null ]"
# 3. the lock screen of a restarted tablet with a PIN: the credential-encrypted storage (and with it every app, the helper included) is locked until the PIN is entered, so enter it like a person.
#    Run 100: four tries with the pad taps (the way every other section does it) never got past FallbackHome. A restarted tablet shows an extra line on the PIN screen, so the pad may sit lower than the
#    fixed coordinates; the PIN as key events does not depend on the layout. Tries alternate between the two (6 in all) and every failed try prints what the screen shows, so a failure explains itself.
rbstate(){ echo "user0 $(adb shell dumpsys user 2>/dev/null | grep -oE 'State: [A-Z_]+' | head -1), ce_available=$(adb shell getprop sys.user.0.ce_available | tr -d '\r\n'), keyguard: $(adb shell dumpsys window 2>/dev/null | grep -oE '(mShowingLockscreen|isKeyguardShowing|mDreamingLockscreen|mKeyguardGoingAway)=[a-z]+' | sort -u | tr '\n' ' ')"; }
rbui(){ adb shell uiautomator dump /sdcard/rbui.xml >/dev/null 2>&1; adb shell cat /sdcard/rbui.xml 2>/dev/null | grep -oE '(text|content-desc)="[^"]+"' | cut -d'"' -f2 | tr '\n' '|' | cut -c1-330; }
info "RB right after the restart: $(rbstate)"
info "RB the screen right after the restart shows: $(rbui)"
RBUNLOCKED=0
for try in 1 2 3 4; do
  adb shell input keyevent KEYCODE_WAKEUP; sleep 2; adb shell input swipe 300 900 300 300 300; sleep 2
  [ "$try" = 1 ] && shot 150-rb-lock-after-restart
  if [ $((try % 2)) = 1 ]; then adb shell input text 1234; sleep 1; adb shell input keyevent KEYCODE_ENTER; RBHOW=keys; else pad 1 2 3 4 e; RBHOW=taps; fi   # run 104: the taps did not unlock a restarted tablet, the key events did
  sleep 8
  resumed > "$OUT/rb-resumed.txt"
  if ! grep -q FallbackHome "$OUT/rb-resumed.txt"; then RBUNLOCKED=1; break; fi
  info "RB try $try ($RBHOW) did not unlock: $(rbstate)"
  info "RB try $try screen: $(rbui)"
done
info "RB unlock after the restart: $([ $RBUNLOCKED = 1 ] && echo "done on try $try ($RBHOW)" || echo "NOT done"); resumed: $(cut -c1-110 $OUT/rb-resumed.txt)"
info "RB display after the restart: $(adb shell wm size | tr -d '\r' | tr '\n' ' ')| $(adb shell wm density | tr -d '\r' | tr '\n' ' ')| overscan: $(adb shell dumpsys window displays | grep -m1 -oE 'overscan=\([0-9, -]*\)|mOverscan[A-Za-z]*=[^ ]*' | tr '\n' ' ')| font_scale=$(adb shell settings get system font_scale | tr -d '\r\n')"
check "RB4: after the restart the PIN unlocks and Home shows the launcher or Solitaire" bash -c "grep -qiE 'calendar|com.tj.launcher|launcher3' $OUT/rb-resumed.txt"
for i in $(seq 1 20); do adb shell dumpsys accessibility 2>/dev/null | grep -q 'Service\[label=' && break; sleep 2; done   # Android binds the helper a few seconds after the unlock
sleep 3
adb shell dumpsys accessibility > "$OUT/rb-a11y.txt" 2>&1
adb shell settings get secure enabled_accessibility_services > "$OUT/rb-a11y-list.txt" 2>&1
check "RB5a: the accessibility helper is still switched on after the restart (the list survived)" grep -q 'com.tj.launcher/' "$OUT/rb-a11y-list.txt"
check "RB5b: Android bound the helper again by itself (a bound accessibility service exists; the emulator has no other)" grep -q 'Service\[label=' "$OUT/rb-a11y.txt"
adb shell settings get secure sysui_qs_tiles > "$OUT/rb-tiles-after.txt" 2>&1
check "RB6: the one-tile list survived the restart" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/rb-tiles-after.txt)\" = \"\$(tr -d ' \r\n' < $OUT/rb-tiles-before.txt)\" ]"
adb shell dumpsys activity services "$PKG" > "$OUT/rb-services.txt" 2>&1
check "RB7: the keep-alive service is running again after the restart" grep -q KeepAliveService "$OUT/rb-services.txt"
adb shell "run-as $PKG cat shared_prefs/tj_crash.xml 2>/dev/null" > "$OUT/rb-crash-after.txt"
check "RB7b: the launcher data is readable again after the unlock (so RB8 and RB9 mean something)" bash -c "[ -n \"\$(adb shell run-as $PKG ls shared_prefs 2>/dev/null | head -1)\" ]"
check "RB8: the crash breaker did not trip during the restart" bash -c "! grep -q 'tripped.*true' $OUT/rb-crash-after.txt"
command adb logcat -d -b crash 2>/dev/null | grep -c "com.tj.launcher" > "$OUT/rb-crashes.txt"
check "RB9: the launcher did not crash while the tablet started up" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/rb-crashes.txt)\" = 0 ]"
info "RB after the restart: tiles '$(tr -d '\r\n' < $OUT/rb-tiles-after.txt)', policy_control '$(adb shell settings get global policy_control | tr -d '\r\n')', mode: $(getmode), resumed: $(cut -c1-120 $OUT/rb-resumed.txt)"
adb shell locksettings clear --old 1234 > "$OUT/rb-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
adb shell settings delete global policy_control >/dev/null 2>&1
# the scenario's display set-up (top of this file) is applied once; a restart can drop it, which would change every later section (run 114: the last step found no power menu after rb)
adb shell cmd overlay enable com.android.internal.systemui.navbar.threebutton 2>/dev/null; adb shell wm size 600x1024; adb shell wm overscan 0,0,0,48; adb shell wm density 170; adb shell settings put system font_scale 1.2; sleep 2
setfeat; setmode custom; rebind; home
fi # section rb
if want last; then # section last
beat "before LAST STEP"
# ---- LAST STEP: our replacement card must press the REAL "Restart" button (the emulator reboots after this)
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><string name=\\\"power_mode\\\">hide_screenshot</string><boolean name=\\\"guard_stock_home\\\" value=\\\"false\\\" /><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /></map>\" > shared_prefs/tj_features.xml'"; adb shell am force-stop "$PKG"; sleep 1
setmode custom; rebind; home; sleep 3
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 14-card-before-restart
info "LAST state: display $(adb shell wm size | tr -d '\r' | tr '\n' ' ')$(adb shell wm density | tr -d '\r' | tr '\n' ' '); a11y services bound: $(adb shell dumpsys accessibility | grep -c 'Service\[label='); stock menu windows: $(adb shell dumpsys window windows | grep -c 'ActionsDialog'); TjA11y log lines this boot: $(adb logcat -d -s TjA11y:I | wc -l); resumed: $(resumed | cut -c1-90)"
RECT="$(adb logcat -d -s TjA11y:I | grep -o 'bounds=Rect([0-9, -]*)' | tail -1 | grep -o '[0-9]*' | tr '\n' ' ')"
set -- $RECT
if [ "$#" -ge 4 ]; then
  X=$(( ($1 + $3) / 2 )); Y=$(( $2 + ($4 - $2) * 3 / 4 ))
  echo "card rect: $RECT -> tap Restart at $X,$Y" | tee "$OUT/press-restart.txt"
  adb shell dumpsys window windows | grep -iE 'Window\{[^}]*(TjA11y|com.tj.launcher|ActionsDialog)' -A0 | head -6 >> "$OUT/press-restart.txt"
  adb shell input tap $X $Y   # the tap REBOOTS the emulator: read the log before the device goes away. Poll (a single read after a fixed 0.6 s missed the line once, run 113)
  for i in 1 2 3 4 5 6 7 8 9 10; do adb logcat -d -s TjA11y:I > "$OUT/tja11y-full.txt" 2>&1; grep -q "press 'Restart'" "$OUT/tja11y-full.txt" && break; sleep 0.25; done
  grep "press" "$OUT/tja11y-full.txt" >> "$OUT/press-restart.txt"
  adb shell dumpsys input 2>/dev/null | grep -iE 'TjA11y|com.tj.launcher|ActionsDialog' | head -8 >> "$OUT/press-restart.txt"
else
  echo "no card rect found in log" | tee -a "$OUT/press-restart.txt"
fi
info "LAST what was found: $(tr '\n' '|' < "$OUT/press-restart.txt" | sed 's/[0-9][0-9]-[0-9][0-9] //g' | cut -c1-330)"
info "LAST card log tail: $(grep -iE 'card|press|menu' "$OUT/tja11y-full.txt" 2>/dev/null | tail -4 | cut -c1-120 | tr '\n' '|' | cut -c1-330)"
check "card's Restart pressed the real Restart button" grep -q "press 'Restart' -> true" "$OUT/press-restart.txt"
fi # section last
echo "failed checks: $FAILS" | tee -a "$OUT/results.txt"
ls -la "$OUT"
exit "$FAILS"
