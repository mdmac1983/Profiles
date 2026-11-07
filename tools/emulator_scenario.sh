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
# 2026-11-06 watchdog: three emulators went "device offline" for good (runs 146 and 150/157 of 2026-11-06, infrastructure, not the product) and every later adb call then waited out its 60 s limit, so a dead run took 45 minutes.
# If the real adb does not say "device" for 4 minutes in a row (a restart takes about 20 s), the scenario stops here and says so in the results (a FAIL line), instead of burning the step timeout.
( bad=0; while sleep 30; do kill -0 $$ 2>/dev/null || exit 0; if [ "$(command adb get-state 2>/dev/null | tr -d '\r')" = device ]; then bad=0; else bad=$((bad+1)); fi; if [ "$bad" -ge 8 ]; then echo "FAIL  the emulator went offline (adb did not report a device for 4 minutes): the run was stopped here, nothing after this point ran" >> "$OUT/results.txt"; echo "FAIL  the emulator went offline (adb did not report a device for 4 minutes): the run was stopped here"; kill -TERM $$ 2>/dev/null; exit 0; fi; done ) &
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
#      Names: base legacy x1 e1 y2 b6 b4 n n6 o t u s p x v g rs rs2 rs3 rs4 ka ac rt im gc pf rb last. Setup, the install and the helper functions below always run.
SECTIONS="${SECTIONS:-}"
# per-section wall time (AJ1): every section that starts prints how long the PREVIOUS one took ("INFO  TIME <section> <s> s"), so the next cut of the gate is data-driven
_SEC=""; _SECT0=0
_sectime(){ local n; n=$(date +%s); [ -n "$_SEC" ] && info "TIME $_SEC $((n-_SECT0)) s"; _SEC="$1"; _SECT0=$n; }
want(){ local r=1; if [ -z "$SECTIONS" ]; then r=0; else case ",$SECTIONS," in *",$1,"*) r=0;; esac; fi; [ "$r" -eq 0 ] && _sectime "$1"; return "$r"; }
info "sections: ${SECTIONS:-ALL}"
# helpers that more than one section uses (hoisted here so a section-only run has them)
# pa* (sections pa and pt): the automatic-calibration test features, the lock screen, the stored-crop status, the pixel compare
pafeat(){ # pafeat <automatic true|false> <manual calibration window end, epoch ms; 0 = none>: wipes the stored crop and the learned menu place
  adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs files; rm -f files/patchshot_r0.png files/patchshot_r1.png; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_radios_off\\\" value=\\\"false\\\" /><boolean name=\\\"lock_quiet\\\" value=\\\"false\\\" /><boolean name=\\\"allow_setup_autorun\\\" value=\\\"true\\\" /><boolean name=\\\"lock_patch\\\" value=\\\"true\\\" /><boolean name=\\\"patch_auto_cal\\\" value=\\\"$1\\\" /><string name=\\\"patch_cal_until\\\">$2</string><string name=\\\"power_mode\\\">hide_screenshot</string></map>\" > shared_prefs/tj_features.xml'"; }
palock(){ adb shell input keyevent KEYCODE_POWER; sleep 2; adb shell input keyevent KEYCODE_WAKEUP; sleep 3; }     # the lock screen, screen on
pastatus(){ adb shell "run-as $PKG cat shared_prefs/tj_features.xml" 2>/dev/null | grep -o 'name="patch_shot_status">[^<]*' | sed 's/.*>//'; }
panote(){ adb shell "run-as $PKG cat shared_prefs/tj_features.xml" 2>/dev/null | grep -o 'name="patch_auto_note">[^<]*' | sed 's/.*>//'; }
pamae(){ compare -metric MAE -extract "$3" "$OUT/$1.png" "$OUT/$2.png" null: 2>&1 | grep -o '([0-9.e+-]*)' | tr -d '()'; }
panums(){ echo "$1" | grep -o "$2=[^ ]*" | head -1 | grep -o '[0-9]*' | tr '\n' ' '; }
# tap an app icon in the OPEN drawer by its label (found with uiautomator), falling back to fixed coordinates: the drawer order is not the same on every emulator boot
tapapp(){ local l="$1" fx="$2" fy="$3" b; adb shell uiautomator dump /sdcard/tjui.xml >/dev/null 2>&1
  b=$(adb shell cat /sdcard/tjui.xml 2>/dev/null | tr '>' '\n' | grep -F "text=\"$l\"" | grep -o 'bounds="[^"]*"' | head -1 | grep -o '[0-9]\+' | tr '\n' ' ')
  set -- $b
  if [ "$#" -ge 4 ]; then adb shell input tap $(( ($1+$3)/2 )) $(( ($2+$4)/2 )); else adb shell input tap "$fx" "$fy"; fi; }
setfeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"; }
# vfeat <lock_patch true|false> [skew]: feature file for the lock-patch emulator tests (sections v and x; defined up here so a section-only run has it)
vfeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"power_card\\\" value=\\\"true\\\" /><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"patch_auto_cal\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><boolean name=\\\"allow_setup_autorun\\\" value=\\\"true\\\" /><boolean name=\\\"lock_patch\\\" value=\\\"$1\\\" /><int name=\\\"patch_test_skew\\\" value=\\\"${2:-0}\\\" /><string name=\\\"power_mode\\\">hide_screenshot</string></map>\" > shared_prefs/tj_features.xml'"; }
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
adb shell input swipe 300 700 300 700 1200; sleep 2; shot 02-longpress
# AE7 (Tj, 2026-11-06, photo of the stock popup): a long press on the home screen shows WALLPAPERS ONLY (no Widgets, no Home settings). The label may read "Styles & wallpapers" on an image that has a style picker.
adb shell uiautomator dump /sdcard/lp.xml >/dev/null 2>&1; adb shell cat /sdcard/lp.xml 2>/dev/null | tr '>' '\n' > "$OUT/longpress-dump.txt"
info "AE7 long-press menu texts: $(grep -o 'text="[^"]*"' $OUT/longpress-dump.txt | grep -iE 'wallpaper|widget|setting|style' | tr '\n' ' ')"
check "AE7: a long press on the home screen offers Wallpapers" grep -qiE 'text="[^"]*wallpapers"' "$OUT/longpress-dump.txt"
check "AE7: ... and ONLY that: no Widgets, no Home settings" bash -c "! grep -qE 'text=\"(Widgets|Home settings)\"' $OUT/longpress-dump.txt"
adb shell input keyevent KEYCODE_BACK; sleep 1
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
setfeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"; }
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
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><boolean name=\\\"home_type_launcher\\\" value=\\\"false\\\" /></map>\" > shared_prefs/tj_features.xml'"
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
# ---- AR1 (Tj, 2026-11-06: "improve the ui and organization for ease of use and intuitive design"): Launcher control is a list of collapsible GROUPS; the page is read from top to bottom through three screens
adb shell uiautomator dump /sdcard/ctl1.xml >/dev/null 2>&1; adb shell cat /sdcard/ctl1.xml 2>/dev/null | tr '>' '\n' > "$OUT/control-dump.txt"
for AR1I in 2 3 4 5 6; do   # slow, short swipes (a fast fling skipped whole groups in run 211): every part of the page is dumped
  adb shell input swipe 300 800 300 400 800; sleep 1
  adb shell uiautomator dump /sdcard/ctl$AR1I.xml >/dev/null 2>&1; adb shell cat /sdcard/ctl$AR1I.xml 2>/dev/null | tr '>' '\n' >> "$OUT/control-dump.txt"
done
AR1MISS=""; for g in 'Everyday' 'Lock screen' 'Unlock into Quickstep' 'Power menu' 'Recents' 'Return to the custom launcher' 'Battery' 'Quick settings' 'Safety' 'Advanced'; do grep -q "text=\"$g" "$OUT/control-dump.txt" || AR1MISS="$AR1MISS [$g]"; done
info "AR1: group headers missing from the page: ${AR1MISS:-none}; texts seen: $(grep -o 'text="[^"]\{3,40\}"' $OUT/control-dump.txt | sed 's/text=//' | sort -u | tr '\n' ' ' | cut -c1-1200)"
check "AR1: Launcher control is grouped: every group header is on the page" bash -c "[ -z \"$AR1MISS\" ]"
check "AR1: 'Restore everything' stays reachable (a fixed footer, not inside a closed group)" grep -q 'text="Restore everything"' "$OUT/control-dump.txt"

# ---- uninstall must leave a working stock home
adb uninstall "$PKG" > "$OUT/uninstall.txt" 2>&1; sleep 2; home; resumed > "$OUT/resumed-uninstalled.txt"
check "after uninstall, stock launcher is Home" grep -q 'com.android.launcher3/' "$OUT/resumed-uninstalled.txt"


fi # section base
# ---- PHASE 2 (after the safety checks, so it cannot disturb them): power-menu helper on a fresh install
adb install -r "$APK" >/dev/null 2>&1
adb shell cmd package set-home-activity "$PKG/com.tj.launcher.RouterActivity" >/dev/null 2>&1
prefhome(){ { echo "== $1"; adb shell cmd package resolve-activity -a android.intent.action.MAIN -c android.intent.category.HOME | grep -E 'name=|packageName=' | head -2; adb shell dumpsys package preferred-xml 2>/dev/null | grep -iE 'tj|launcher' | head -4; } >> "$OUT/prefhome.txt" 2>&1; }
# ---- power-menu helper (screenshots only: judged by eye + overlay window check)
setpower(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"power_card\\\" value=\\\"true\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><string name=\\\"power_mode\\\">$1</string></map>\" > shared_prefs/tj_features.xml'"; adb shell am force-stop "$PKG"; sleep 1; }
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
setlabel(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><boolean name=\\\"recents_only_target\\\" value=\\\"true\\\" /><string name=\\\"return_app_label\\\">$1</string></map>\" > shared_prefs/tj_features.xml'"; }
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
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><boolean name=\\\"home_type_launcher\\\" value=\\\"true\\\" /></map>\" > shared_prefs/tj_features.xml'"
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
# Poll up to 20 s more instead of trusting the fixed 14 s: in run 125 (2026-11-05) a loaded runner had not cleared the notification in time, while the same code passed in run 124. The check is about WHETHER the
# real shade clears it (no notification access), not how fast (the speed is measured separately, W1).
NPOLL=0
for k in 1 2 3 4 5 6 7 8 9 10; do
  adb shell dumpsys notification --noredact | awk '/Notification List:/{f=1} /mArchive|Archived notifications/{f=0} f' | grep -c tjtag2 > "$OUT/notif2-after.txt" 2>&1
  [ "$(tr -d ' \r\n' < $OUT/notif2-after.txt)" = 0 ] && break
  NPOLL=$k; sleep 2
done
adb shell dumpsys activity recents | grep -cE 'Recent #' > "$OUT/recents-after.txt" 2>&1
for APP in com.android.contacts com.android.gallery3d; do echo "$APP $(adb shell pidof $APP | tr -d '\r\n')" >> "$OUT/apps2-after.txt"; done
adb logcat -d -s TjA11y:I | grep -E 'RESET|STRAY|LOCKNUM' > "$OUT/reset2-log.txt" 2>&1
check "reset2: the chosen app (Calendar) was opened after the accessibility reset" grep -qi 'calendar' "$OUT/resumed-reset2.txt"
check "reset2: the notification was cleared through the real shade (no notification access)" bash -c "[ \"$(tr -d ' \r\n' < $OUT/notif2-before.txt)\" != 0 ] && [ \"$(tr -d ' \r\n' < $OUT/notif2-after.txt)\" = 0 ]"
info "recents entries before=$(tr -d ' \r\n' < $OUT/recents-before.txt) after=$(tr -d ' \r\n' < $OUT/recents-after.txt); apps still running: $(cat $OUT/apps2-after.txt | tr '\n' ';')"
info "reset2: test notification count before=$(tr -d ' \r\n' < $OUT/notif2-before.txt) after=$(tr -d ' \r\n' < $OUT/notif2-after.txt) (after $NPOLL extra 2 s poll(s)); the helper's RESET lines: $(grep RESET "$OUT/reset2-log.txt" 2>/dev/null | sed -E 's/^[0-9-]+ [0-9:.]+ +[0-9]+ +[0-9]+ //' | cut -c1-90 | tr '\n' ';' | cut -c1-300)"
adb shell svc wifi enable >/dev/null 2>&1

beat "before V5 + V2"
# ---- V5 + V2: Solitaire (Clock here) opens when the custom launcher STARTS; keyboard switch applies without error once granted
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS > "$OUT/grant-secure.txt" 2>&1
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"true\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><boolean name=\\\"keyboard_switch\\\" value=\\\"true\\\" /><string name=\\\"return_app_label\\\">Clock</string></map>\" > shared_prefs/tj_features.xml'"
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
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /></map>\" > shared_prefs/tj_features.xml'"
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
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"power_card\\\" value=\\\"true\\\" /><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><string name=\\\"power_mode\\\">hide_screenshot</string></map>\" > shared_prefs/tj_features.xml'"
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
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><boolean name=\\\"unlock_pick\\\" value=\\\"true\\\" /></map>\" > shared_prefs/tj_features.xml'"
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
setcal(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"; }
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
setfeat2(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><boolean name=\\\"unlock_m_taperase\\\" value=\\\"true\\\" /><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"; }
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
setfeat3(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"true\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><boolean name=\\\"unlock_m_ladder\\\" value=\\\"true\\\" /><boolean name=\\\"unlock_m_charger\\\" value=\\\"true\\\" /><boolean name=\\\"unlock_m_peek5\\\" value=\\\"true\\\" /><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"; }
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
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><boolean name=\\\"profile_hide_pencil_native\\\" value=\\\"true\\\" /><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"
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
# the setup screen. The autorun's screenshot job has just opened the tablet's SETTINGS screens, so the setup screen is no longer in front (run 121, 2026-11-05: the scan dumped Settings, found none of the five
# labels and "2 pages"): open it again (no autorun this time), go to its top, then scroll down in SMALL, slow steps until the page stops changing (a dump only lists what is on screen, and eight fast 500 px swipes can fling past a whole button)
adb shell am start -n "$PKG/com.tj.launcher.SetupActivity" >/dev/null 2>&1; sleep 3
shot 76b-setup-screen-reopened
for i in 1 2 3 4 5 6; do adb shell input swipe 300 250 300 900 150; done; sleep 1
: > "$OUT/x-setup-ui.xml"; XLAST=""; XPAGES=0
for i in $(seq 1 24); do
  adb shell uiautomator dump /sdcard/sx.xml >/dev/null 2>&1; adb shell cat /sdcard/sx.xml > "$OUT/x-sx.tmp" 2>/dev/null
  cat "$OUT/x-sx.tmp" >> "$OUT/x-setup-ui.xml"; echo >> "$OUT/x-setup-ui.xml"; XPAGES=$i
  XCUR=$(grep -o 'text="[^"]*"' "$OUT/x-sx.tmp" | md5sum | cut -c1-12)
  [ "$XCUR" = "$XLAST" ] && break
  XLAST="$XCUR"
  adb shell input swipe 300 750 300 450 900; sleep 1
done
ui_all(){ local p; for p in "$@"; do grep -qF -- "$p" "$OUT/x-setup-ui.xml" || return 1; done; }
XMISS=""; for p in '1. Do everything that is missing' '2. Collect the tablet report' '2b. Share a SHORT summary' '3. Side-key test' '4. Collect EVERYTHING'; do grep -qF -- "$p" "$OUT/x-setup-ui.xml" || XMISS="$XMISS [$p]"; done
info "PG2: setup screen scan: $XPAGES page(s) to the end; numbered steps not found:${XMISS:- none}"
[ -n "$XMISS" ] && info "PG2: what the scan saw instead: $(grep -o 'text="[^"]\{3,60\}"' "$OUT/x-setup-ui.xml" | head -6 | tr '\n' ' ') | in front: $(adb shell dumpsys window 2>/dev/null | grep -m1 mCurrentFocus | cut -c1-120)"
check "PG2: the setup screen offers the numbered steps (1 do everything, 2 collect, 2b short summary, 3 side-key test, 4 everything)" ui_all '1. Do everything that is missing' '2. Collect the tablet report' '2b. Share a SHORT summary' '3. Side-key test' '4. Collect EVERYTHING'
adb pull "$SHARE/summary.txt" "$OUT/x-summary.txt" >/dev/null 2>&1
sum_all(){ local p; for p in "$@"; do grep -qE -- "$p" "$OUT/x-summary.txt" || return 1; done; }
check "PG3: the SHORT summary (for pasting or sharing when the file cannot be attached) was written, is under 12 KB, and holds the state and the parser self-check" bash -c "[ -s $OUT/x-summary.txt ] && [ \$(wc -c < $OUT/x-summary.txt) -lt 12000 ]" 
check "PG3: the short summary has the same section titles the intake tool reads" sum_all 'TABLET SUMMARY' 'THE LAUNCHER ON THIS TABLET' 'PARSER SELF-CHECK' "this launcher's permissions" '^model: '
python3 -I tools/tablet_intake.py - --out "$OUT/x-intake-sum" --date 2026-01-01 --label summary < "$OUT/x-summary.txt" > "$OUT/x-intake-sum-console.txt" 2>&1; RCS=$?
check "PG3: the intake tool reads the short summary too (exit 0, FACTS.md says it is the short one)" bash -c "[ $RCS = 0 ] && grep -q 'SHORT summary' $OUT/x-intake-sum/2026-01-01/FACTS.md"
info "PG2: report $(wc -c < "$OUT/x-report.txt" 2>/dev/null) bytes, $(wc -l < "$OUT/x-report.txt" 2>/dev/null) lines; keytest: $(grep '^keytest:' "$OUT/x-autorun-done.txt" | cut -c1-330)"
check "PG2: the report holds the launcher's own state (version, mode, helper, accessibility, crash breaker)" rep_all 'THE LAUNCHER ON THIS TABLET' '^launcher version: ' '^mode: ' '^shell helper reachable: yes' '^accessibility helper bound: ' '^crash breaker tripped: '
check "AD8: the report shows what the launcher costs here (process memory, start-up time, helper load)" rep_all '^process memory \(AD8\): total PSS [0-9]+ MB, java heap [0-9]+ of [0-9]+ MB' '^start-up \(AD8\): Application\.onCreate [0-9]+ ms, process fork to the end of it [0-9]+ ms' '^accessibility helper load \(AD8\): connected [0-9]+ min, [0-9]+ events'
info "AD8: $(grep -E '^(process memory|start-up|accessibility helper load) \(AD8\)' "$OUT/x-report.txt" 2>/dev/null | tr '\n' '|' | cut -c1-420)"
check "PG2: the report ran the launcher's own readers on this system (PARSER SELF-CHECK: Recents tasks, apps with a notification)" rep_all 'PARSER SELF-CHECK' '^Recents tasks parsed: [0-9]' '^Apps with a notification now'
check "PG2: the probe list added the samples (notification records, side-key device, policy_control, crash log, the launcher's log)" rep_all 'NotificationRecord\(' 'getevent -pl' 'settings get global policy_control' 'logcat -b crash' 'TjReset:I'
check "PG2: the side-key routine ran end to end in the emulator (KEYTEST markers, the 'no power menu' flag answered OK, then cleared)" kt_all 'KEYTEST-START' 'KEYTEST-END' 'disable2(0x8) -> OK' 'cleared'
check "PG2: getevent's output reached the helper through the pipe (KEYTEST-LINES n with n above 0: the shell can read the input devices here)" bash -c "grep '^keytest:' $OUT/x-autorun-done.txt | grep -qE 'KEYTEST-LINES [1-9]'"
check "PG3: the report carries the key-test verdict on ONE line (it survives the report's length cut) and says getevent works" rep_all '^keytest_summary: the side key was NOT seen \([0-9]+ input lines were read, so getevent works'
check "PG3: the key was not pressed on the emulator, so no KEY_POWER line is claimed" bash -c "! grep '^keytest:' $OUT/x-autorun-done.txt | grep -q 'KEY_POWER'"
rm -rf "$OUT/x-intake"
python3 -I tools/tablet_intake.py "$OUT/x-report.txt" --out "$OUT/x-intake" --date 2026-01-01 --label emulator > "$OUT/x-intake-console.txt" 2>&1; RCI=$?
info "PG3: intake of the real report: exit $RCI; $(head -c 400 "$OUT/x-intake-console.txt" | tr '\n' '|')"
info "PG3: notification records in the real report: $(grep -o 'NotificationRecord(0x[0-9a-fA-F]*: pkg=[^ ]* user=[^ ]*' "$OUT/x-report.txt" 2>/dev/null | sed 's/NotificationRecord(0x[0-9a-fA-F]*: //' | sort | uniq -c | tr -s ' ' | tr '\n' ';' | head -c 240) | the launcher's reader says:$(grep -m1 '^Apps with a notification now' "$OUT/x-report.txt" 2>/dev/null | cut -d: -f2- | cut -c1-120)"
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
    # AC6 / V5 measurement (info only): how much of its brightness the strip next to the menu keeps when the stock dialog's scrim is up, and how much the closed lock screen keeps of the test wallpaper (the keyguard's own dim)
    MC="$(convert "$OUT/83-lock-closed-patch-off.png" -crop "$RING" +repage -scale 1x1 -depth 8 txt:- 2>/dev/null | tail -1 | grep -o '([0-9, ]*)' | head -1 | tr -d '() ')"
    MO="$(convert "$OUT/82-lock-menu-open.png" -crop "$RING" +repage -scale 1x1 -depth 8 txt:- 2>/dev/null | tail -1 | grep -o '([0-9, ]*)' | head -1 | tr -d '() ')"
    info "V5: mean colour of the strip next to the menu: closed ($MC) vs open ($MO): the open menu's scrim keeps $(echo "$MC $MO" | awk -F'[ ,]' 'NF>=8 && $1>0 && $2>0 && $3>0 {printf "R %.3f G %.3f B %.3f", $5/$1, $6/$2, $7/$3}') of the closed brightness"
    # V5 (v0.7.3): the patch is dimmed like the stock dialog's scrim, so the slot must match the CLOSED lock screen multiplied by the brightness the strip next to the menu keeps (measured just above)
    KO="$(echo "$MC $MO" | awk -F'[ ,]' 'NF>=8 && $1>0 && $2>0 && $3>0 {printf "%.4f", ($5/$1+$6/$2+$7/$3)/3}')"
    [ -n "$KO" ] && { convert "$OUT/83-lock-closed-patch-off.png" -crop "$FREE" +repage -channel RGB -evaluate Multiply "$KO" +channel "$OUT/v-closed-slot-dimmed.png" 2>/dev/null; convert "$OUT/82-lock-menu-open.png" -crop "$FREE" +repage "$OUT/v-open-slot.png" 2>/dev/null; }
    MAED="$(compare -metric MAE "$OUT/v-open-slot.png" "$OUT/v-closed-slot-dimmed.png" null: 2>&1 | grep -o '([0-9.e+-]*)' | tr -d '()')"
    info "V5: the freed slot of the open menu vs the closed lock screen dimmed by the measured $KO: MAE $MAED (the undimmed comparison above is $MAEF)"
    check "V5: the part of the old menu below our short card matches the lock screen dimmed like the stock menu's scrim (MAE < 1.5 %)" awk -v m="$MAED" 'BEGIN{exit !(m!="" && m+0 < 0.015)}'
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
# the tick: restart the guide (back at the top), tick the FIRST card, "Take me to my next step" must put the SECOND card at the top of the screen, and the tick must survive another restart.
# A dump only lists what is on screen, so the boxes are FOUND, not assumed: tools/uinode.py looks for the Done box in the SAME ROW as the card's own title, and gseek swipes one step at a time until it is on screen.
# (Runs 117/118, 2026-11-05: a blind swipe plus "the first Done in the dump" ticked nothing, which failed this check and the restart check for a reason that was not in the app. tools/test_uinode.sh pins the finder.)
guinode(){ python3 -I tools/uinode.py "$@"; }
gseek(){ # gseek <dump file> <down|up> <tries> <uinode args>: fresh dump; if the node is not on screen swipe one step (down = further down the page) and look again; 0 = found, the last dump stays in the file
  local out="$1" dir="$2" max="$3" k; shift 3
  for k in $(seq 0 "$max"); do
    gdump "$out"
    [ -n "$(guinode "$out" "$@")" ] && return 0
    if [ "$dir" = down ]; then adb shell input swipe 300 800 300 500 500; else adb shell input swipe 300 500 300 800 500; fi
    sleep 1
  done
  return 1
}
gtapn(){ # gtapn <dump file> <uinode args>: tap the centre of the node uinode.py finds in that dump
  local d="$1" b; shift; b=$(guinode "$d" "$@"); set -- $b; [ "$#" -ge 4 ] || return 1
  adb shell input tap $(( ($1+$3)/2 )) $(( ($2+$4)/2 ))
}
CARD1=$(grep -m1 '^@ ' phoneguide/src/main/assets/steps.txt | sed 's/^@ //'); CARD2=$(grep '^@ ' phoneguide/src/main/assets/steps.txt | sed -n 2p | sed 's/^@ //')
GTITLE1="1.  $CARD1"; GTITLE2="2.  $CARD2"      # how the app writes a title: number, two spaces, the title (a must card has a star in front)
adb shell am force-stop com.tj.phoneguide; sleep 1
adb shell am start -n com.tj.phoneguide/.GuideActivity >/dev/null 2>&1; sleep 3
gseek "$OUT/g-ui-r1.xml" down 12 --contains Done --exact --row-of "$GTITLE1"; GF1=$?
gtapn "$OUT/g-ui-r1.xml" --contains Done --exact --row-of "$GTITLE1"; GT1=$?; sleep 1
gdump "$OUT/g-ui-r1b.xml"
TICK1=$(guinode "$OUT/g-ui-r1b.xml" --contains Done --exact --row-of "$GTITLE1" --attr checked)
info "PG1: card 1 [$GTITLE1]: Done box found=$([ "$GF1" = 0 ] && echo yes || echo NO) tapped=$([ "$GT1" = 0 ] && echo yes || echo NO) checked afterwards=${TICK1:-not on screen}"
check "PG1: tapping the Done box in card 1's row ticks card 1 (the box reads checked=true)" test "$TICK1" = true
gseek "$OUT/g-ui-r1c.xml" up 12 --contains "Take me to my next step"; GFB=$?
gtapn "$OUT/g-ui-r1c.xml" --contains "Take me to my next step"; sleep 2
gdump "$OUT/g-ui-r2.xml"
shot 94-phoneguide-next-step
GTOP=$(guinode "$OUT/g-ui-r2.xml" --contains "$GTITLE2" | awk '{print $2}')
info "PG1: button found=$([ "$GFB" = 0 ] && echo yes || echo NO); after 'Take me to my next step' card 2 [$GTITLE2] has its top at y=${GTOP:-not on screen}"
check "PG1: 'Take me to my next step' scrolled to the second card (its title is within the top 250 px)" bash -c "[ -n '$GTOP' ] && [ '$GTOP' -le 250 ]"
adb shell am force-stop com.tj.phoneguide; sleep 1
adb shell am start -n com.tj.phoneguide/.GuideActivity >/dev/null 2>&1; sleep 3
gseek "$OUT/g-ui-r3.xml" down 12 --contains Done --exact --row-of "$GTITLE1"
TICK3=$(guinode "$OUT/g-ui-r3.xml" --contains Done --exact --row-of "$GTITLE1" --attr checked)
info "PG1: after a restart of the guide card 1's Done box reads checked=${TICK3:-not on screen}"
check "PG1: the tick on card 1 survived a restart of the guide" test "$TICK3" = true
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
kafeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string><boolean name=\\\"keep_alive\\\" value=\\\"$1\\\" /></map>\" > shared_prefs/tj_features.xml'"; }
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
# KA1 (v0.7.2): with NO stored choice (a fresh install, or after the breaker / "Restore everything") keep-alive is OFF: nothing is posted until Tj switches it on. (The next block is the positive control: the same flow with the switch ON.)
setfeat; adb shell am force-stop "$PKG"; rebind; home; sleep 4
adb shell dumpsys activity services "$PKG" > "$OUT/ka-services-default.txt" 2>&1
check "KA1: with no stored choice keep-alive is OFF by default (no foreground service)" bash -c "! grep -q 'isForeground=true' $OUT/ka-services-default.txt"
# switch ON (explicit; the default is OFF since v0.7.2)
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
adb shell "run-as $PKG cat shared_prefs/tj_crash.xml 2>/dev/null" > "$OUT/ka-crashprefs-after-restore.txt"
check "AC1: Restore everything PAUSES the lock-screen automation (nothing switches launchers for you until you go back to custom)" grep -q 'name="paused" value="true"' "$OUT/ka-crashprefs-after-restore.txt"
adb shell "run-as $PKG sh -c 'rm -f shared_prefs/tj_crash.xml'"   # a paused app would make every later section behave differently
# ---- KA1 (v0.7.2): the FUSE, end to end on the real Android crash path. On the real tablet SystemUI could not draw the keep-alive notification's icon, so Android crashed the launcher over it ("Bad notification ... Couldn't create
#      icon") three times in seven seconds, the breaker tripped and Tj's switches were wiped. Here the service posts an icon that does not exist (test key keep_alive_test_bad_icon = 1), which SystemUI cannot draw either, so Android crashes
#      the app the same way. Expected: exactly ONE such crash, keep-alive switches ITSELF off and is marked blocked, the 3-crash breaker does NOT trip, Tj's other switches survive, no keep-alive service afterwards.
kafuse(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><boolean name=\\\"home_type_launcher\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string><boolean name=\\\"keep_alive\\\" value=\\\"true\\\" /><int name=\\\"keep_alive_test_bad_icon\\\" value=\\\"$1\\\" /><boolean name=\\\"lock_number\\\" value=\\\"true\\\" /></map>\" > shared_prefs/tj_features.xml; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map></map>\" > shared_prefs/tj_crash.xml'"; }
adb shell am force-stop "$PKG"; setmode custom; kafuse 2; adb logcat -b crash -c   # knob 2 = no small icon: Android itself refuses it ("Bad notification ..."), the same crash family as the tablet's (run 130: knob 1, an unknown icon id, did NOT crash AOSP's SystemUI)
rebind; home; sleep 16; home; sleep 4
adb logcat -b crash -d > "$OUT/ka-fuse-crash.txt" 2>&1
adb shell "run-as $PKG cat shared_prefs/tj_features.xml" 2>/dev/null | tr '>' '\n' > "$OUT/ka-fuse-features.txt"
adb shell "run-as $PKG cat shared_prefs/tj_crash.xml" 2>/dev/null | tr '>' '\n' > "$OUT/ka-fuse-crashprefs.txt"
adb shell dumpsys activity services "$PKG" > "$OUT/ka-fuse-services.txt" 2>&1
info "KA1 fuse: crashes of $PKG in the crash buffer: $(grep -c "Process: $PKG," $OUT/ka-fuse-crash.txt); 'Bad notification' lines: $(grep -c 'Bad notification' $OUT/ka-fuse-crash.txt); features: $(grep -oE 'name=\"(keep_alive|keep_alive_blocked|lock_number)\" value=\"[a-z]*\"' $OUT/ka-fuse-features.txt | tr '\n' ' '); crash prefs: $(tr '\n' ' ' < $OUT/ka-fuse-crashprefs.txt | cut -c1-200)"
KA_FUSE_N=$(grep -c "Process: $PKG," "$OUT/ka-fuse-crash.txt")
if [ "$KA_FUSE_N" -gt 0 ]; then   # the crash happened: now everything about it is asserted
check "KA1: a notification Android refuses makes it crash the launcher with 'Bad notification' (the tablet's crash family)" grep -q 'Bad notification' "$OUT/ka-fuse-crash.txt"
check "KA1: it happened exactly ONCE (the sticky restart did not post the notification again)" bash -c "[ \"\$(grep -c 'Process: $PKG,' $OUT/ka-fuse-crash.txt)\" -eq 1 ]"
check "KA1: the fuse switched keep-alive OFF" grep -q 'name="keep_alive" value="false"' "$OUT/ka-fuse-features.txt"
check "KA1: the fuse marked keep-alive as blocked (it stays off until Tj switches it on again)" grep -q 'name="keep_alive_blocked" value="true"' "$OUT/ka-fuse-features.txt"
check "KA1: the 3-crash breaker did NOT trip over it" bash -c "! grep -q 'tripped.*true' $OUT/ka-fuse-crashprefs.txt"
check "KA1: Tj's other switches survived (the lock number is still on)" grep -q 'name="lock_number" value="true"' "$OUT/ka-fuse-features.txt"
check "KA1: no keep-alive foreground service afterwards" bash -c "! grep -q 'isForeground=true' $OUT/ka-fuse-services.txt"
else
# runs 130 and 132 (2026-11-05): AOSP's SystemUI on the emulator accepts a notification with an unknown icon id AND one with no small icon, so Android never crashes the app here. The tablet's MtkSystemUI did crash it ("Couldn't create icon").
# The fuse LOGIC is proven by the JVM tests (KeepAlivePolicyTest: the exception match, fuseHandles; CrashGuardTest: record() routes it); this end-to-end run is only possible on an image whose SystemUI refuses the icon.
info "KA1 fuse end-to-end: NOT REPRODUCIBLE on this emulator (0 crashes with knob 2 = no small icon): the checks of the crash path were skipped, the logic is covered by KeepAlivePolicyTest + CrashGuardTest"
check "KA1: with the knob set the service still ran normally (no crash, switch still on, not blocked)" bash -c "grep -q 'name=\"keep_alive\" value=\"true\"' $OUT/ka-fuse-features.txt && ! grep -q 'keep_alive_blocked.*true' $OUT/ka-fuse-features.txt"
fi
# INFO only (nothing asserted): the tablet's own variant, an icon resource id that does not exist. AOSP's SystemUI may simply ignore it (run 130 saw no crash), MediaTek's crashed the launcher with "Couldn't create icon".
adb shell am force-stop "$PKG"; setmode custom; kafuse 1; adb logcat -b crash -c
rebind; home; sleep 10; home; sleep 3
adb logcat -b crash -d > "$OUT/ka-fuse1-crash.txt" 2>&1
info "KA1 variant 1 (unknown icon id, informational): crashes of $PKG: $(grep -c "Process: $PKG," $OUT/ka-fuse1-crash.txt); 'Bad notification' lines: $(grep -c 'Bad notification' $OUT/ka-fuse1-crash.txt)"
adb logcat -b crash -c
setfeat; setmode custom; adb shell am force-stop "$PKG"; ensurehome; rebind; home
fi # section ka
if want ac; then # section ac
ensurehome
# ---- AC1-AC9 (Tj, 2026-11-05 ~22:17; v0.7.3): the Solitaire switch gates every automatic open, the lock-screen rules do not depend on a wiped switch, a wrong PIN after the gesture keeps the gesture,
#      the handshake puts Quickstep behind the lock at once whatever the pre-arm switch says, battery saver is switched off in the custom launcher the moment it is on, the decision log exists and holds no digit.
beat "before AC"
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS >/dev/null 2>&1
# acfeat <prearm true|false> <startup_open true|false> <battery_saver_off true|false> [quickstep_on true|false] [strict true|false] (AN1: strict = the old fight-the-tile behaviour, default false): the usual test features (Calendar stands in for Solitaire) + the OLD end-on-lock switch stored as FALSE (the breaker's wipe): it must have no effect any more
acfeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"$2\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"${4:-false}\\\" /><boolean name=\\\"profile_battery_saver_strict\\\" value=\\\"${5:-false}\\\" /><boolean name=\\\"profile_prearm\\\" value=\\\"$1\\\" /><boolean name=\\\"profile_end_on_lock\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_off\\\" value=\\\"$3\\\" /><boolean name=\\\"unlock_m_taperase\\\" value=\\\"true\\\" /><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"; }
lowpower(){ adb shell settings get global low_power | tr -d '\r\n'; }

# ---- AC3: battery saver. Make the test deterministic: not charging, 60 %
adb shell dumpsys battery unplug >/dev/null 2>&1; adb shell dumpsys battery set level 60 >/dev/null 2>&1
acfeat true false true false true; setmode quickstep; rebind; home; sleep 2     # AN1: these AC3 / AG1 checks are the STRICT mode (the old behaviour, now opt-in)
adb shell settings put global low_power 1; sleep 3
info "AC3: Quickstep, battery saver switched on from outside: low_power=$(lowpower)"
check "AC3: in Quickstep battery saver is left alone (still on 3 s later)" bash -c "[ \"$(lowpower)\" = 1 ]"
adb logcat -c
adb shell "input keyevent KEYCODE_HOME; input keyevent KEYCODE_HOME"; sleep 5     # the real way back to custom (double Home): Mode.set(CUSTOM)
AC3B="$(lowpower)"; adb logcat -d -s TjBatSaver:I | grep BATSAVER > "$OUT/ac3-log.txt" 2>&1
info "AC3: back in the custom launcher (it was switched on in Quickstep): low_power=$AC3B; log: $(head -c 160 $OUT/ac3-log.txt | tr '\n' '|')"
check "AC3: returning to the custom launcher switched battery saver off (turned on in Quickstep, still on when custom began)" bash -c "[ \"$AC3B\" = 0 ]"
adb shell settings put global low_power 1; sleep 2
AC3L="$(lowpower)"
check "AC3 (strict): turned on again while the custom launcher is active, it is switched off again within 2 s (the observer, no polling)" bash -c "[ \"$AC3L\" = 0 ]"
setmode custom; adb shell settings put global low_power 1; rebind; sleep 4
AC3P="$(lowpower)"
check "AC3 (strict): a launcher process that STARTS while battery saver is on switches it off (prepared at process start)" bash -c "[ \"$AC3P\" = 0 ]"
acfeat true false false false true; setmode custom; rebind; sleep 2
adb shell settings put global low_power 1; sleep 3
AC3S="$(lowpower)"
check "AC3: with its switch OFF the guard leaves battery saver alone (control)" bash -c "[ \"$AC3S\" = 1 ]"
# ---- AZ2 (Tj, 2026-11-06 20:40; replaces AG1): Quickstep is 100 % STOCK. The v0.7.5 - v0.8.0 launcher switched the saver ON when Quickstep began (a direct write of low_power), which left Android 10's battery saver
#      state machine out of step: the tile and Settings could not turn it off any more. Now the launcher never writes the saver ON, not even in strict mode (section bs proves the Settings screen end to end).
acfeat true false true false true; setmode quickstep; rebind; home; sleep 2
adb shell settings put global low_power 0; sleep 3
check "AZ2: in Quickstep a battery saver that is off stays off (the launcher never switches it ON, not even in strict mode)" bash -c "[ \"$(lowpower)\" = 0 ]"
# ---- AN1 (Tj, 2026-11-06: "the battery saver tile sometimes works. But it is mostly unresponsive. When it does work it turns on very slow ... It wasn't happening before"): WITHOUT strict the tile is the boss between launcher changes
acfeat true false true true; setmode quickstep; rebind; home; sleep 2     # strict OFF (the default), both rules on
adb shell settings put global low_power 1; sleep 2
adb logcat -c
adb shell settings put global low_power 0; sleep 3
check "AN1: in Quickstep a battery saver that is turned OFF (as the tile does) stays off: nothing switches it back on" bash -c "[ \"$(lowpower)\" = 0 ]"
adb shell settings put global low_power 1; sleep 3
check "AN1: turned on again it stays on" bash -c "[ \"$(lowpower)\" = 1 ]"
adb shell "input keyevent KEYCODE_HOME; input keyevent KEYCODE_HOME"; sleep 5     # a REAL change to the custom launcher (double Home)
check "AN1: a real change to the custom launcher still switches battery saver off at once" bash -c "[ \"$(lowpower)\" = 0 ]"
adb logcat -c
adb shell settings put global low_power 1; sleep 3
check "AN1: in the custom launcher a battery saver turned on by the user (the tile, the Settings screen) stays on until the launcher changes again" bash -c "[ \"$(lowpower)\" = 1 ]"
adb logcat -d -s TjBatSaver:I | grep -c "BATSAVER off" > "$OUT/an1-offs.txt" 2>&1
check "AN1: the guard wrote nothing to battery saver in between (no 'BATSAVER off' line)" bash -c "[ \"\$(tr -d ' \\r\\n' < $OUT/an1-offs.txt)\" = 0 ]"
adb shell settings put global low_power 0; adb shell dumpsys battery reset >/dev/null 2>&1

# ---- the lock screen tests need a PIN (the same one as section n)
adb shell locksettings set-pin 1234 > "$OUT/ac-setpin.txt" 2>&1

# ---- AC2 / AC8: the Solitaire (Calendar) switch is OFF: waking to the lock screen must start nothing, but the custom home is still prepared
acfeat true false true; setmode custom; rebind; home; sleep 2
adb shell am force-stop com.android.calendar; sleep 1; adb logcat -c
lockpad
adb shell pidof com.android.calendar | tr -d '\r\n' > "$OUT/ac-a-off-pid.txt"
adb shell dumpsys activity recents | grep -c "A=com.android.calendar" > "$OUT/ac-a-off-recents.txt"
adb logcat -d -s TjA11y:I | grep PROFILE > "$OUT/ac-a-off-log.txt" 2>&1
info "AC2: switch OFF, locked wake: calendar pid='$(cat $OUT/ac-a-off-pid.txt)' recents=$(tr -d ' \r\n' < $OUT/ac-a-off-recents.txt); PROFILE: $(tail -1 $OUT/ac-a-off-log.txt | cut -c1-200)"
check "AC2/AC8: with 'Open Solitaire' OFF the lock-screen preparation starts NO Solitaire stand-in (no process)" bash -c "[ -z \"\$(cat $OUT/ac-a-off-pid.txt)\" ]"
check "AC2: and it is not in Recents" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/ac-a-off-recents.txt)\" = 0 ]"
check "AC8: the custom home WAS still prepared behind the lock screen" grep -q "custom home" "$OUT/ac-a-off-log.txt"
pad 1 2 3 4 e; sleep 4
resumed > "$OUT/ac-a-off-resumed.txt"
check "AC1/AC8: a plain PIN lands on the custom launcher at once" grep -q "com.tj.launcher" "$OUT/ac-a-off-resumed.txt"
check "AC2: still no Solitaire stand-in after the unlock" bash -c "[ -z \"\$(adb shell pidof com.android.calendar | tr -d '\r\n')\" ]"
# positive control: with the switch ON the same flow does start it
acfeat true true true; setmode custom; rebind; home; sleep 3
adb shell am force-stop com.android.calendar; sleep 1
lockpad; sleep 3
check "AC2 (control): with 'Open Solitaire' ON the locked wake starts the stand-in behind the lock screen" bash -c "[ -n \"\$(adb shell pidof com.android.calendar | tr -d '\r\n')\" ]"
pad 1 2 3 4 e; sleep 3

# ---- AC1: a Quickstep session, the old end-on-lock switch stored as FALSE: a plain PIN must still give the custom launcher
acfeat true false true; setmode quickstep; rebind; home; sleep 2
lockpad; pad 1 2 3 4 e; sleep 7
getmode > "$OUT/ac1-mode.txt"; resumed > "$OUT/ac1-resumed.txt"
check "AC1: a plain PIN after a Quickstep session ends it although the old switch is stored as off" grep -q custom "$OUT/ac1-mode.txt"
check "AC1: and the custom account (our launcher, or the stand-in app the return reset opens) is on screen, not Quickstep" bash -c "grep -qE 'calendar|com.tj.launcher' $OUT/ac1-resumed.txt && ! grep -q 'com.android.launcher3/' $OUT/ac1-resumed.txt"

# ---- AC9: the pre-arm switch is OFF, the handshake still puts Quickstep behind the lock screen AT ONCE (a wrong PIN keeps the tablet locked, so the log is the evidence)
acfeat false false true; setmode custom; rebind; home; sleep 2; adb logcat -c
lockpad; pad 1 d 1 d 1 d 4 4 4 4 e; sleep 2
adb logcat -d -s TjA11y:I | grep -E "PROFILE|ROUTER" > "$OUT/ac9-log.txt" 2>&1
info "AC9: $(tail -2 $OUT/ac9-log.txt | cut -c1-200 | tr '\n' '|')"
check "AC9: with 'switch behind the lock screen' OFF the handshake still started Quickstep behind the lock screen at once" grep -q "ROUTER start stock" "$OUT/ac9-log.txt"
# ---- AC7: ...and the RIGHT PIN typed plainly after that wrong one still unlocks into Quickstep (the gesture survives a typo)
sleep 2; pad 1 2 3 4 e; sleep 7
getmode > "$OUT/ac7-mode.txt"; resumed > "$OUT/ac7-resumed.txt"
check "AC7: a wrong PIN after the gesture, then the right PIN plain: Quickstep (the gesture is not lost)" grep -q quickstep "$OUT/ac7-mode.txt"
check "AC7: and the stock launcher is on screen" grep -q "com.android.launcher3/" "$OUT/ac7-resumed.txt"

# ---- AC1 safety net: the helper is NOT running at the unlock; when it comes back the Quickstep session is ended (Android's usage events say the keyguard was shown after the session began)
adb shell appops set "$PKG" GET_USAGE_STATS allow >/dev/null 2>&1
acfeat true false true
NOWMS=$(( $(adb shell date +%s | tr -d '\r\n') * 1000 ))
adb shell am force-stop "$PKG"
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><string name=\\\"mode\\\">quickstep</string><long name=\\\"changedAt\\\" value=\\\"$(( NOWMS - 5000 ))\\\" /></map>\" > shared_prefs/tj_mode.xml'"
adb shell settings put secure enabled_accessibility_services ""; sleep 2          # the helper is away
lockpad; pad 1 2 3 4 e; sleep 6                                                   # a plain PIN that nobody sees
getmode > "$OUT/ac-b6-before.txt"
adb shell dumpsys usagestats 2>/dev/null | grep -iE "KEYGUARD" | tail -3 | cut -c1-160 > "$OUT/ac-b6-usagestats.txt"
info "AC1 safety net: usage events about the keyguard on this emulator: $(tr '\n' '|' < $OUT/ac-b6-usagestats.txt | cut -c1-300)"
rebind; sleep 9                                                                    # the helper comes back, asks the usage events, ends the session
getmode > "$OUT/ac-b6-after.txt"
adb shell "run-as $PKG cat files/lock-events.log" 2>/dev/null | grep RECONCILE > "$OUT/ac-b6-log.txt"
check "AC1 safety net (control): while the helper is away a plain PIN is not seen, the mode is still Quickstep" grep -q quickstep "$OUT/ac-b6-before.txt"
check "AC1 safety net: when the helper comes back it ends the Quickstep session (the keyguard was shown after it began, no handshake)" grep -q custom "$OUT/ac-b6-after.txt"
check "AC1 safety net: the lock log says RECONCILE" test -s "$OUT/ac-b6-log.txt"

# ---- AC1 / AC7 evidence: the persistent decision log exists, says what happened, and holds no digit of the PIN
adb shell "run-as $PKG cat files/lock-events.log" > "$OUT/ac-lock-events.txt" 2>&1
info "AC log: $(wc -l < $OUT/ac-lock-events.txt | tr -d ' ') lines; last: $(tail -2 $OUT/ac-lock-events.txt | cut -c1-120 | tr '\n' '|')"
check "AC-B5: the lock-screen decision log exists and records the unlocks" grep -q "UNLOCK" "$OUT/ac-lock-events.txt"
check "AC-B5: it holds no digit of the PIN (1234 / 4444 never appear)" bash -c "! grep -qE '1234|4444' $OUT/ac-lock-events.txt"
adb shell locksettings clear --old 1234 > "$OUT/ac-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
setfeat; setmode custom; adb shell am force-stop "$PKG"; ensurehome; rebind; home
fi # section ac
# ---- shared helpers of sections bs and bl (BA2 needs the same real-Settings driver): defined once, at global scope
# ----------------------------------------------------------------------------------------------------
lowpower(){ adb shell settings get global low_power | tr -d '\r\n'; }
# bsfeat <guard switch true|false> <battery_saver_last text> <bs_unstick_done true|false>: the usual test features (the custom launcher opens no app)
bsfeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_strict\\\" value=\\\"false\\\" /><boolean name=\\\"profile_prearm\\\" value=\\\"true\\\" /><boolean name=\\\"profile_end_on_lock\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_off\\\" value=\\\"$1\\\" /><boolean name=\\\"bs_unstick_done\\\" value=\\\"$3\\\" /><string name=\\\"battery_saver_last\\\">$2</string><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"; }
# bsfind <ui dump> <resource-id suffix> <text fragment>: the centre "x y" of the node whose resource-id ends with the suffix OR whose text contains the fragment (case-insensitive); nothing when there is none
bsfind(){ python3 -I -c '
import re, sys
import xml.etree.ElementTree as ET
path, rid, frag = sys.argv[1], sys.argv[2], sys.argv[3].lower()
try:
    root = ET.parse(path).getroot()
except Exception:
    sys.exit(0)
for n in root.iter("node"):
    t = (n.get("text") or "").lower()
    i = n.get("resource-id") or ""
    if (rid and i.endswith(rid)) or (frag and frag in t):
        m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", n.get("bounds", ""))
        if m:
            x1, y1, x2, y2 = (int(g) for g in m.groups())
            print((x1 + x2) // 2, (y1 + y2) // 2)
            break
' "$1" "$2" "$3"; }
# bstap on|off: open the Battery saver screen of Settings and tap its "Turn on now" / "Turn off now" button, like a person; 0 = tapped
bstap(){ local id="state_$1_button" frag="turn $1 now" xy
  adb shell am start -a android.settings.BATTERY_SAVER_SETTINGS >/dev/null 2>&1; sleep 3
  adb shell uiautomator dump /sdcard/bsui.xml >/dev/null 2>&1; adb exec-out cat /sdcard/bsui.xml > "$OUT/bs-ui-$1.xml" 2>/dev/null
  xy=$(bsfind "$OUT/bs-ui-$1.xml" "$id" "$frag")
  [ -n "$xy" ] || { info "BS: no '$frag' button on the Battery saver screen (dump bs-ui-$1.xml)"; return 1; }
  adb shell input tap $xy; sleep 2; return 0; }
# the state machine's own view, for the evidence (Android 10 prints it in `dumpsys power`)
bsstate(){ adb shell dumpsys power 2>/dev/null | tr -d '\r' | grep -iA12 'battery saver state machine' | grep -iE 'mState|Enabled|full=' | tr -s ' ' | tr '\n' ';' | cut -c1-200; }

if want bs; then # section bs
ensurehome
beat "before BS"
# ---- AZ2 (Tj, 2026-11-06 20:40: "If I open the full settings and go to battery saver, then try to turn it off there, it says it turned off but it never does").
#      ROOT CAUSE (Android 10 source, research/AOSP10_BATTERYSAVER_AND_POWERMENU_2026-11-06.md): BatterySaverStateMachine switches the saver when Settings.Global low_power is WRITTEN but keeps its own state, so the real
#      "Turn on now" / "Turn off now" button of the Settings screen is then ignored. This section drives the REAL Settings screen (what a person does) on the real Android 10 system image:
#        BS1 control: the stock path works.  BS2 the bug as Tj saw it (INFO only: it documents Android, with the state machine's own dump).
#        BS3 the launcher's switch-off in the custom launcher, then a "Turn on now" tap: it must still work (the self-heal), and so must "Turn off now" after it.  BS4 a saver an older version left stuck is cleared once.
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS >/dev/null 2>&1
adb shell dumpsys battery unplug >/dev/null 2>&1; adb shell dumpsys battery set level 60 >/dev/null 2>&1
# ---- BS1 control: nothing of ours is involved; the stock path must work on this image
bsfeat true "" true; setmode quickstep; rebind; home; sleep 2
adb shell settings put global low_power 0; sleep 1
bstap on; BS1ON="$(lowpower)"
info "BS1: Settings 'Turn on now' -> low_power=$BS1ON; state machine: $(bsstate)"
check "BS1 (control): the Settings button 'Turn on now' switches battery saver on" bash -c "[ \"$BS1ON\" = 1 ]"
bstap off; BS1OFF="$(lowpower)"
check "BS1 (control): the Settings button 'Turn off now' switches it off again" bash -c "[ \"$BS1OFF\" = 0 ]"
home

# ---- BS2 the bug as Tj saw it: the saver is switched ON by a direct write (what v0.7.5 - v0.8.0 did when Quickstep began), then the person taps 'Turn off now'. INFO only: it documents what Android does.
adb shell settings put global low_power 1; sleep 2
BS2PRE="$(bsstate)"
bstap off; BS2="$(lowpower)"
info "BS2 (the old bug): after a direct write low_power=1, Settings 'Turn off now' left low_power=$BS2 (1 = ignored by Android = Tj's report); state machine before: $BS2PRE; after: $(bsstate)"
adb shell dumpsys power 2>/dev/null | tr -d '\r' | grep -iA14 'battery saver state machine' > "$OUT/bs-state-machine.txt" 2>&1
adb shell settings put global low_power 0; sleep 2; home

# ---- BS3 the launcher's switch-off in the custom launcher, then 'Turn on now': Android's state machine is out of step (MANUAL_ON, saver off) and ignores the tap; the self-heal must carry it out
bsfeat true "" true; setmode quickstep; rebind; home; sleep 2
adb shell settings put global low_power 0; sleep 1
bstap on; BS3A="$(lowpower)"
check "BS3a: in Quickstep the Settings button switches battery saver on" bash -c "[ \"$BS3A\" = 1 ]"
home; adb logcat -c
adb shell "input keyevent KEYCODE_HOME; input keyevent KEYCODE_HOME"; sleep 5     # the real way back to custom (double Home): Mode.set(CUSTOM) -> the guard writes low_power = 0
BS3B="$(lowpower)"; BS3BSTATE="$(bsstate)"
check "BS3b: back in the custom launcher the saver is switched off (AC3 still works)" bash -c "[ \"$BS3B\" = 0 ]"
setmode quickstep; rebind; home; sleep 2
adb logcat -c
bstap on; sleep 1; BS3C="$(lowpower)"
adb logcat -d -s TjBatSaver:I | grep BATSAVER > "$OUT/bs3-log.txt" 2>&1
info "BS3: after the switch-off (state machine: $BS3BSTATE) 'Turn on now' -> low_power=$BS3C; heal log: $(grep -c 'BATSAVER healed' $OUT/bs3-log.txt) line(s); $(head -c 200 $OUT/bs3-log.txt | tr '\n' '|')"
check "BS3c: after the launcher switched it off, the Settings button 'Turn on now' still switches battery saver on (the self-heal)" bash -c "[ \"$BS3C\" = 1 ]"
bstap off; BS3D="$(lowpower)"
check "BS3d: and 'Turn off now' switches it off again (the state machine agrees again)" bash -c "[ \"$BS3D\" = 0 ]"
home

# ---- BS4 a saver that v0.7.5 - v0.8.0 forced on (its last note says so) is cleared ONCE when the new version starts; Settings then works from a consistent state
adb shell am force-stop "$PKG"; sleep 1   # BA2: the app must NOT be running while the test writes its preferences file behind its back (a live process saves its own copy on the next change of low_power and would erase the injected state)
bsfeat true "12:00:00  switched battery saver ON in Quickstep (the process start)" false
adb shell settings put global low_power 1; sleep 1
setmode quickstep; rebind; sleep 4
BS4A="$(lowpower)"
info "BS4: the old version's forced-on saver after the new version started: low_power=$BS4A"
check "BS4a: a battery saver the old launcher had forced on is cleared once by the new version" bash -c "[ \"$BS4A\" = 0 ]"
bstap on; BS4B="$(lowpower)"
check "BS4b: and then the Settings button switches it on" bash -c "[ \"$BS4B\" = 1 ]"
bstap off; BS4C="$(lowpower)"
check "BS4c: and off again" bash -c "[ \"$BS4C\" = 0 ]"
adb shell settings put global low_power 0; adb shell dumpsys battery reset >/dev/null 2>&1
setmode custom; rebind; home
fi # section bs

if want bl; then # section bl
ensurehome
# ---- BA2 (Tj, 2026-11-07 00:07: "make the lock screen always have battery saver turned off permanently when the lock screen is up, no matter which launcher was on before the screen lock. If I go to the custom launcher,
#      have the battery saver remain turned off, but if I unlock to quickstep, keep the same battery saver state as the last time I was in quickstep. Only implement these battery saver features if you are confident they
#      can work and not break anything"). Driven like a person, on the real Android 10 image: the saver is turned on and off through the REAL Settings screen (so Android's state machine is in the state a person leaves
#      it in), the tablet is locked with the power key and unlocked with the real PIN pad (the handshake gesture = Quickstep, a plain PIN = the custom launcher). The proof that nothing was corrupted is the last step of
#      each round: the real Settings button "Turn off now" must really turn the saver off (the AZ2 bug was exactly that it did not).
#        BL1 locked from Quickstep with the saver on: off on the lock screen, kept off, back after the gesture.   BL2 locked from the custom launcher: off, and a plain unlock keeps it off.
#        BL3 the memory survives a detour in the custom launcher.   BL4 a plug-in while locked: nothing is switched on from outside afterwards.   BL5 the lock-screen switch (the restore switch is JVM-tested).   The PIN is ALWAYS cleared again at the end.
beat "before BL"
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS >/dev/null 2>&1
adb shell dumpsys battery unplug >/dev/null 2>&1; adb shell dumpsys battery set level 60 >/dev/null 2>&1
blfeat(){ # blfeat <lock-screen rule true|false> <bring Quickstep's saver back true|false>: the usual test features (no radios / helper clean-up in the way); a FRESH file, so the memory of an earlier round is gone
  adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_strict\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_off\\\" value=\\\"true\\\" /><boolean name=\\\"bs_unstick_done\\\" value=\\\"true\\\" /><boolean name=\\\"lock_quiet\\\" value=\\\"false\\\" /><boolean name=\\\"profile_radios_off\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_lock_off\\\" value=\\\"$1\\\" /><boolean name=\\\"profile_battery_saver_lock_restore\\\" value=\\\"$2\\\" /></map>\" > shared_prefs/tj_features.xml'"; }
bllog(){ adb logcat -d -s TjBatSaver:I | grep BATSAVER | tail -${1:-5} | cut -c1-190 | tr '\n' '|'; }
blround(){ # blround <lock rule> <restore> <quickstep|custom>: a fresh round in the given mode, saver off, nothing plugged in
  blfeat "$1" "$2"; adb shell am force-stop "$PKG"; sleep 1
  setmode "$3"; rebind; home; sleep 3
  adb shell settings put global low_power 0; sleep 1; adb logcat -c; }
adb shell locksettings set-pin 1234 > "$OUT/bl-setpin.txt" 2>&1
# ---------------- BL1: locked from Quickstep with the saver on (turned on through the real Settings screen = Android's state machine says MANUAL_ON)
blround true true quickstep
bstap on; BL1A="$(lowpower)"
check "BL1a (setup): in Quickstep the real Settings button switched battery saver on" bash -c "[ \"$BL1A\" = 1 ]"
home; sleep 2; adb logcat -c
adb shell input keyevent KEYCODE_POWER; sleep 3                      # the lock moment: the screen is off (the helper switches the saver off about 0.7 s later, while the display is dark)
BL1B="$(lowpower)"
adb shell input keyevent KEYCODE_WAKEUP; sleep 4                     # the plain lock screen
BL1C="$(lowpower)"
info "BL1: locked from Quickstep with the saver on: low_power=$BL1B (screen off) / $BL1C (lock screen); $(bllog 4)"
check "BL1b: the lock screen has battery saver OFF (locked from Quickstep with the saver on)" bash -c "[ \"$BL1B\" = 0 ] && [ \"$BL1C\" = 0 ]"
adb shell settings put global low_power 1; sleep 3                   # something turns it on while the lock screen is up (Android's low-battery trigger, a notification button)
BL1D="$(lowpower)"
check "BL1c: a saver that is turned on while the lock screen is up is switched off again within moments" bash -c "[ \"$BL1D\" = 0 ]"
adb shell input swipe 300 900 300 300 300; sleep 4                   # the PIN pad
pad d d 1 2 3 4 e; sleep 9                                           # the gesture: Quickstep again
BL1E="$(lowpower)"
info "BL1: after the gesture unlock into Quickstep: low_power=$BL1E; state machine: $(bsstate); $(bllog 5)"
check "BL1d: a gesture unlock into Quickstep brings battery saver back (it was on the last time in Quickstep)" bash -c "[ \"$BL1E\" = 1 ]"
bstap off; BL1F="$(lowpower)"
info "BL1: the real Settings 'Turn off now' -> low_power=$BL1F; state machine: $(bsstate)"
check "BL1e: Android's own state agrees: the real Settings button 'Turn off now' really switches it off" bash -c "[ \"$BL1F\" = 0 ]"
bstap on; BL1G="$(lowpower)"; bstap off
check "BL1f: and 'Turn on now' works again too" bash -c "[ \"$BL1G\" = 1 ]"
home; sleep 1; adb shell settings put global low_power 0; sleep 1
# ---------------- BL2: locked from the custom launcher (a saver somebody turned on there: AN1 leaves it until the lock)
blround true true custom
adb shell settings put global low_power 1; sleep 3
BL2A="$(lowpower)"
check "BL2a (setup): in the custom launcher a saver turned on by the person stays on until the tablet is locked" bash -c "[ \"$BL2A\" = 1 ]"
adb shell input keyevent KEYCODE_POWER; sleep 3; adb shell input keyevent KEYCODE_WAKEUP; sleep 4
BL2B="$(lowpower)"
check "BL2b: locked from the custom launcher the lock screen has battery saver OFF too" bash -c "[ \"$BL2B\" = 0 ]"
adb shell input swipe 300 900 300 300 300; sleep 4
pad 1 2 3 4 e; sleep 8                                               # a plain PIN: the custom launcher
BL2C="$(lowpower)"
info "BL2: after a plain PIN (custom launcher): low_power=$BL2C; $(bllog 4)"
check "BL2c: a plain unlock into the custom launcher leaves battery saver off" bash -c "[ \"$BL2C\" = 0 ]"
# ---------------- BL3: the memory survives a detour in the custom launcher (Quickstep with the saver on -> custom -> lock + plain PIN -> lock + gesture)
blround true true quickstep
bstap on; BL3A="$(lowpower)"; home; sleep 1
adb shell "input keyevent KEYCODE_HOME; input keyevent KEYCODE_HOME"; sleep 5     # the real way back to custom (double Home): the end of a Quickstep session
BL3B="$(lowpower)"
check "BL3a: the custom launcher has the saver off when a Quickstep session that had it on ends" bash -c "[ \"$BL3A\" = 1 ] && [ \"$BL3B\" = 0 ]"
lockpad; sleep 4; pad 1 2 3 4 e; sleep 8                              # lock + a plain PIN: still the custom launcher
BL3C="$(lowpower)"
check "BL3b: a lock and a plain unlock in between keep it off" bash -c "[ \"$BL3C\" = 0 ]"
lockpad; sleep 4; pad d d 1 2 3 4 e; sleep 9                          # the gesture: Quickstep again
BL3D="$(lowpower)"
info "BL3: after the gesture unlock into Quickstep (last Quickstep session had it on): low_power=$BL3D; state machine: $(bsstate); $(bllog 5)"
check "BL3c: the gesture unlock into Quickstep brings back the saver Quickstep had the last time" bash -c "[ \"$BL3D\" = 1 ]"
bstap off; BL3E="$(lowpower)"
check "BL3d: Android's own state agrees (the real Settings button turns it off)" bash -c "[ \"$BL3E\" = 0 ]"
home; sleep 1
# ---------------- BL4: plugged in while locked: Android's own state is reset by that, so nothing may be switched on from outside afterwards (a direct write would leave 'Turn off now' dead: the AZ2 bug)
blround true true quickstep
bstap on; BL4A="$(lowpower)"; home; sleep 1
adb shell input keyevent KEYCODE_POWER; sleep 3; adb shell input keyevent KEYCODE_WAKEUP; sleep 4
BL4B="$(lowpower)"
adb shell dumpsys battery set ac 1 >/dev/null 2>&1; sleep 3          # plugged in while locked ...
adb shell dumpsys battery unplug >/dev/null 2>&1; sleep 3            # ... and back on battery
adb shell input swipe 300 900 300 300 300; sleep 4
pad d d 1 2 3 4 e; sleep 9
BL4C="$(lowpower)"
info "BL4: plugged in while locked, then the gesture unlock: low_power=$BL4C; $(bllog 5)"
check "BL4a: after a plug-in while locked the gesture unlock does NOT switch battery saver back on from outside" bash -c "[ \"$BL4A\" = 1 ] && [ \"$BL4B\" = 0 ] && [ \"$BL4C\" = 0 ]"
bstap on; BL4D="$(lowpower)"; bstap off; BL4E="$(lowpower)"
check "BL4b: and Android's own state is sound: the Settings button turns the saver on and off again" bash -c "[ \"$BL4D\" = 1 ] && [ \"$BL4E\" = 0 ]"
home; sleep 1; adb shell dumpsys battery set level 60 >/dev/null 2>&1
# ---------------- BL5: the lock-screen switch
blround false true quickstep
adb shell settings put global low_power 1; sleep 2
adb shell input keyevent KEYCODE_POWER; sleep 3; adb shell input keyevent KEYCODE_WAKEUP; sleep 4
BL5A="$(lowpower)"
check "BL5: with the lock-screen switch OFF the saver is left alone on the lock screen (control)" bash -c "[ \"$BL5A\" = 1 ]"
adb shell settings put global low_power 0; sleep 1
adb shell settings put global low_power 0; adb shell dumpsys battery reset >/dev/null 2>&1
adb shell locksettings clear --old 1234 > "$OUT/bl-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
check "BL: the PIN used for the test was cleared again" bash -c "! grep -qiE 'fail|error|exception' $OUT/bl-clearpin.txt"
setfeat; setmode custom; adb shell am force-stop "$PKG"; ensurehome; rebind; home
fi # section bl
if want ad; then # section ad
ensurehome
# ---- AD5 + AD7a (Tj, 2026-11-06). AD5: "when I unlock to quickstep, it minimizes my open apps and goes to the home screen. Is this necessary?" -> a gesture unlock brings back the Quickstep app that was open at the lock (switch
#      "A gesture unlock comes back to the app I had open", default ON; OFF = the Quickstep home as before). AD7a: "when I lock the screen from quickstep, the power menu overlay doesn't work" -> the cover works on EVERY lock screen.
#      The PIN is ALWAYS cleared again at the end.
beat "before AD"
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS >/dev/null 2>&1
# NOTE (2026-11-06, bisect runs 176/177): "Close Quickstep's apps ... the moment the tablet locks" (profile_preclean, default ON, Tj: keep it ON) closes the Quickstep apps AT LOCK whenever the shell helper is
#      running. Section v starts the helper and leaves it running, so in the long gate order AD5 had nothing to resume (the lock log said "Quickstep's apps + notifications closed"): a test-order artifact, not a bug.
#      AD5's premise is "the apps are still open at the lock", so this section switches the clean-up OFF explicitly instead of depending on whether a helper happens to be alive.
adfeat(){ # adfeat none|true|false: the resume switch (none = nothing stored = the default, ON); profile_preclean is always written OFF here
  case "$1" in
    none) adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"power_card\\\" value=\\\"true\\\" /><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string><string name=\\\"power_mode\\\">hide_screenshot</string><boolean name=\\\"profile_preclean\\\" value=\\\"false\\\" /></map>\" > shared_prefs/tj_features.xml'";;
    *)    adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"power_card\\\" value=\\\"true\\\" /><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string><string name=\\\"power_mode\\\">hide_screenshot</string><boolean name=\\\"resume_qs_app\\\" value=\\\"$1\\\" /><boolean name=\\\"profile_preclean\\\" value=\\\"false\\\" /></map>\" > shared_prefs/tj_features.xml'";;
  esac; }
adopen(){ adb shell monkey -p com.android.contacts -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3; }   # an app opened IN Quickstep
adb shell locksettings set-pin 1234 > "$OUT/ad-setpin.txt" 2>&1
# ---------------- round 1: the switch has no stored value (default ON)
adfeat none; setmode custom; adb shell am force-stop "$PKG"; ensurehome; rebind; home; sleep 2
lockpad; pad d d 1 2 3 4 e; sleep 7                           # custom -> handshake -> a Quickstep session begins (the Quickstep home)
adopen; sleep 2
adb logcat -c
lockpad; pad d d 1 2 3 4 e; sleep 7                           # locked from Quickstep with an app open, the same gesture again
resumed > "$OUT/ad-r1.txt"; shot 150-ad-resume
adb logcat -d -s TjA11y:I > "$OUT/ad-r1-log.txt" 2>&1
info "AD5 r1 evidence: in front: $(grep -m1 -E 'mResumedActivity' $OUT/ad-r1.txt | cut -c1-140) | log: $(grep -E 'ROUTER|PROFILE|resume|HANDSHAKE|remember' $OUT/ad-r1-log.txt | tail -10 | cut -c25-170 | tr '\n' '|' | cut -c1-1100)"
check "AD5: after the gesture the app that was open (Contacts) is back in front, not the Quickstep home" grep -q 'com.android.contacts' "$OUT/ad-r1.txt"
check "AD5: the helper says it brought that app back" grep -q 'ROUTER resume com.android.contacts' "$OUT/ad-r1-log.txt"
# ---------------- AD7a: lock again from this Quickstep session: the cover must still work on the lock screen
getmode > "$OUT/ad-mode-at-lock.txt"
lockpad; adb logcat -c
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3; shot 151-ad-lock-menu-from-quickstep
adb logcat -d -s TjA11y:I > "$OUT/ad-lockmenu-log.txt" 2>&1
adb shell input keyevent KEYCODE_BACK; sleep 2
info "AD7a: the mode flag at this lock: $(cat $OUT/ad-mode-at-lock.txt); menu log: $(grep -E 'menuOpen' $OUT/ad-lockmenu-log.txt | tail -1 | cut -c1-160)"
check "AD7a: the lock began in Quickstep (the mode flag still says quickstep)" grep -q 'quickstep' "$OUT/ad-mode-at-lock.txt"
check "AD7a: the power-menu cover works on that lock screen (action=1)" grep -q 'action=1' "$OUT/ad-lockmenu-log.txt"
# the same lock screen, plain PIN, no gesture: the custom launcher (AD1)
adb shell input swipe 300 900 300 300 300; sleep 2
pad 1 2 3 4 e; sleep 7
resumed > "$OUT/ad-plain.txt"
check "AD1: a plain PIN after that Quickstep session opens the custom launcher" bash -c "grep -qiE 'calendar|com.tj.launcher' $OUT/ad-plain.txt && ! grep -q 'com.android.contacts' $OUT/ad-plain.txt"
# ---------------- round 2: the switch OFF = the Quickstep home as before
adfeat false; adb shell am force-stop "$PKG"; ensurehome; rebind; home; sleep 2
lockpad; pad d d 1 2 3 4 e; sleep 7
adopen; sleep 2
lockpad; pad d d 1 2 3 4 e; sleep 7
resumed > "$OUT/ad-r2.txt"; shot 152-ad-switch-off
check "AD5: with the switch OFF the gesture opens the Quickstep home (the open app stays minimised)" grep -q 'com.android.launcher3/' "$OUT/ad-r2.txt"
adb shell locksettings clear --old 1234 > "$OUT/ad-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
check "AD: the PIN used for the test was cleared again" bash -c "! grep -qiE 'fail|error|exception' $OUT/ad-clearpin.txt"
adb logcat -c
setfeat; setmode custom; adb shell am force-stop "$PKG"; adb shell "run-as $PKG rm -f shared_prefs/tj_origin.xml"; ensurehome; rebind; home   # (the origin marks of this section must not leak into the next one: run 181, rt AA3)
fi # section ad
if want se; then # section se
ensurehome
# ---- AD4 (Tj, 2026-11-06): hidden, switchable entrances to Quickstep from the custom launcher (no hardware key, no motion sensor): (1) PAGE FLICK = six alternating swipes on the home screen, (2) ROTATION TILE = five changes of the
#      rotation lock in 10 s, (3) CHARGER FLICK = in, out, in. Each is proven (it takes Tj to Quickstep) AND given a negative control (one swipe short / three toggles / switched off) so the check cannot pass by accident.
beat "before SE"
sefeat(){ # sefeat <flick> <rotate> <charger> (true|false)
  adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string><boolean name=\\\"entrance_flick\\\" value=\\\"$1\\\" /><boolean name=\\\"entrance_rotate_tile\\\" value=\\\"$2\\\" /><boolean name=\\\"entrance_charger\\\" value=\\\"$3\\\" /></map>\" > shared_prefs/tj_features.xml'"; }
sefresh(){ adb shell am force-stop "$PKG"; setmode custom; ensurehome; rebind; home; sleep 3; }   # a fresh custom home screen with the new switches
flickchain(){ # flickchain R L R L ...: the swipes run in ONE adb call (each `input swipe` starts in about 0.3 s, so every gap stays far below the 2.5 s limit); R = finger moves right (400 px of 600), L = left
  local cmd="" d; for d in "$@"; do if [ "$d" = R ]; then cmd="$cmd input swipe 100 600 500 600 120;"; else cmd="$cmd input swipe 500 600 100 600 120;"; fi; done
  adb shell "$cmd"; }
SE_ROT0="$(adb shell settings get system accelerometer_rotation | tr -d '\r\n')"; [ "$SE_ROT0" = "1" ] || SE_ROT0=0
rotchain(){ # rotchain N: N changes of the rotation lock in ONE adb call, 0.3 s apart (the observer treats changes closer than 120 ms as one; the code needs 5 inside 10 s)
  adb shell "v=\$(settings get system accelerometer_rotation); i=0; while [ \$i -lt $1 ]; do if [ \"\$v\" = 1 ]; then v=0; else v=1; fi; settings put system accelerometer_rotation \$v; i=\$((i+1)); sleep 0.3; done"; }
# -------- page flick
sefeat true true false; sefresh; adb logcat -c
flickchain R L R L R; sleep 3; getmode > "$OUT/se-flick5.txt"                       # five alternating swipes
check "SE-F1: five alternating swipes are NOT enough (still the custom launcher)" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/se-flick5.txt)\" = custom ]"
sefresh
flickchain R L R L R L; sleep 3; getmode > "$OUT/se-flick6.txt"; resumed > "$OUT/se-flick6-front.txt"      # six alternating swipes (no pause in the middle: each gap must stay under 2.5 s)
check "SE-F2: six alternating swipes take Tj to Quickstep" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/se-flick6.txt)\" = quickstep ]"
check "SE-F3: and the Quickstep home is in front" grep -q 'com.android.launcher3/' "$OUT/se-flick6-front.txt"
sefresh
flickchain R R R R R R; sleep 3; getmode > "$OUT/se-flick-same.txt"                # six swipes the SAME way: not a code
check "SE-F4: six swipes in the same direction are NOT a code" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/se-flick-same.txt)\" = custom ]"
sefeat false true false; sefresh
flickchain R L R L R L; sleep 3; getmode > "$OUT/se-flick-off.txt"
check "SE-F5: with the page-flick switch OFF nothing happens" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/se-flick-off.txt)\" = custom ]"
# -------- rotation tile
sefeat true true false; sefresh
rotchain 3; sleep 3; getmode > "$OUT/se-rot3.txt"
check "SE-R1: three changes of the rotation lock are NOT enough" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/se-rot3.txt)\" = custom ]"
sefresh
rotchain 5; sleep 3; getmode > "$OUT/se-rot5.txt"
check "SE-R2: five changes in 10 s take Tj to Quickstep" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/se-rot5.txt)\" = quickstep ]"
sefeat true false false; sefresh
rotchain 5; sleep 3; getmode > "$OUT/se-rot-off.txt"
check "SE-R3: with the rotation-tile switch OFF nothing happens" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/se-rot-off.txt)\" = custom ]"
adb shell settings put system accelerometer_rotation $SE_ROT0
# -------- charger flick (default OFF)
sefeat true true false; sefresh
adb shell dumpsys battery unplug >/dev/null 2>&1; sleep 1        # the emulator's battery starts "on AC": begin from unplugged so in / out / in are three real changes
adb shell "dumpsys battery set ac 1; dumpsys battery set ac 0; dumpsys battery set ac 1" >/dev/null 2>&1; sleep 3; getmode > "$OUT/se-chg-off.txt"
check "SE-C1: the charger flick is OFF by default: in, out, in does nothing" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/se-chg-off.txt)\" = custom ]"
adb shell dumpsys battery reset >/dev/null 2>&1; sleep 1
sefeat true true true; sefresh
adb shell dumpsys battery unplug >/dev/null 2>&1; sleep 1
adb shell "dumpsys battery set ac 1; dumpsys battery set ac 0; dumpsys battery set ac 1" >/dev/null 2>&1; sleep 3; getmode > "$OUT/se-chg-on.txt"
check "SE-C2: with the charger flick ON, in / out / in takes Tj to Quickstep" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/se-chg-on.txt)\" = quickstep ]"
adb shell dumpsys battery reset >/dev/null 2>&1
adb logcat -d -s TjEntrance:I > "$OUT/se-log.txt" 2>&1
info "SE entrance log: $(grep -c 'ENTRANCE' $OUT/se-log.txt) entrance(s): $(grep 'ENTRANCE' $OUT/se-log.txt | sed 's/.*ENTRANCE //' | tr '\n' ',' | cut -c1-120)"
check "SE-L: each success is in the lock log / entrance log (flick, rotate_tile, charger)" bash -c "grep -q 'ENTRANCE flick' $OUT/se-log.txt && grep -q 'ENTRANCE rotate_tile' $OUT/se-log.txt && grep -q 'ENTRANCE charger' $OUT/se-log.txt"
adb shell settings put system accelerometer_rotation $SE_ROT0
setfeat; setmode custom; adb shell am force-stop "$PKG"; adb shell "run-as $PKG rm -f shared_prefs/tj_origin.xml"; ensurehome; rebind; home   # (the origin marks of this section must not leak into the next one: run 181, rt AA3)
fi # section se
if want ae; then # section ae
ensurehome
# ---- AE3 / AD7b (Tj, 2026-11-06): "an accessibility overlay of the wallpaper patch exactly where the screenshot button shows up ... any time I'm on the lock screen ... hidden without delay".
#      The lock-screen patch RESTS visible over the Screenshot slot (switch lock_patch_always, default OFF).
#      Phase A: NO helper and a deliberately WRONG wallpaper model (skew 30 %): the resting patch is then visibly different from the lock screen, which proves that it paints, that it paints only BELOW the card
#      and that it is not touchable; it must hide while the PIN pad is up and come back. Phase B: the helper runs, the patch is a PHOTO of the real lock screen: with the menu closed it must be invisible.
#      The PIN is ALWAYS cleared again at the end.
beat "before AE"
adb shell pm grant "$PKG" android.permission.READ_EXTERNAL_STORAGE > "$OUT/ae-grant.txt" 2>&1
aefeat(){ # aefeat <resting true|false> <lock_patch true|false> <skew %>
  adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"power_card\\\" value=\\\"true\\\" /><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"patch_auto_cal\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><boolean name=\\\"allow_setup_autorun\\\" value=\\\"true\\\" /><boolean name=\\\"lock_patch\\\" value=\\\"$2\\\" /><boolean name=\\\"lock_patch_always\\\" value=\\\"$1\\\" /><boolean name=\\\"patch_rest_model\\\" value=\\\"true\\\" /><int name=\\\"patch_test_skew\\\" value=\\\"$3\\\" /><string name=\\\"power_mode\\\">hide_screenshot</string></map>\" > shared_prefs/tj_features.xml'"; }
aelock(){ adb shell input keyevent KEYCODE_POWER; sleep 2; adb shell input keyevent KEYCODE_WAKEUP; sleep 3; }     # the lock screen, screen on
aemae(){ compare -metric MAE -extract "$3" "$OUT/$1.png" "$OUT/$2.png" null: 2>&1 | grep -o '([0-9.e+-]*)' | tr -d '()'; }
aenums(){ echo "$1" | grep -o "$2=[^ ]*" | head -1 | grep -o '[0-9]*' | tr '\n' ' '; }
adb shell pkill -f com.tj.launcher.ShellServer >/dev/null 2>&1; sleep 1                       # phase A: no helper
aefeat true true 30; adb shell am force-stop "$PKG"; sleep 1
setmode quickstep; rebind; home
adb shell am start -n "$PKG/.SetupActivity" --ez test_lock_wallpaper true >/dev/null 2>&1; sleep 6
setmode custom; rebind; home
adb shell locksettings set-pin 1234 > "$OUT/ae-setpin.txt" 2>&1
aelock; adb logcat -c
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3        # the first menu on the lock screen: the app learns where it sits
adb shell input keyevent KEYCODE_BACK; sleep 6
shot 90-ae-rest-on-wrong-model
adb shell dumpsys window windows | grep -A10 TjWallpaperPatch > "$OUT/ae-a-window.txt" 2>&1
adb shell input swipe 300 900 300 300 300; sleep 4                  # the PIN pad comes up: the resting patch must hide
adb shell input keyevent KEYCODE_BACK; sleep 4                      # ... and goes away again: the patch must come back
adb logcat -d -s TjA11y:I | grep -E "PATCH|CARD area" > "$OUT/ae-a-log.txt" 2>&1
info "AE3 A log: $(grep -E 'PATCH rest' $OUT/ae-a-log.txt | cut -c25-120 | tr '\n' '|' | cut -c1-400)"
check "AE3: with the switch on and the menu closed the patch RESTS visible on the lock screen (log 'PATCH rest on')" grep -q "PATCH rest on" "$OUT/ae-a-log.txt"
check "AE3: the resting patch window exists and is NOT touchable (it can never block a tap)" bash -c "grep -q TjWallpaperPatch $OUT/ae-a-window.txt && grep -q NOT_TOUCHABLE $OUT/ae-a-window.txt"
check "AE3: it hides while the PIN pad is up (log 'PATCH rest off (PIN pad up)')" grep -q "PATCH rest off (PIN pad up)" "$OUT/ae-a-log.txt"
check "AE3: ... and comes back when the pad is gone (a second 'PATCH rest on')" bash -c "[ \"\$(grep -c 'PATCH rest on' $OUT/ae-a-log.txt)\" -ge 2 ]"
CL="$(grep 'CARD area' "$OUT/ae-a-log.txt" | tail -1)"
SLOT=""   # v0.7.6 (review of v0.7.5): under `set -u` a missing CARD line must report a FAIL, not abort the whole scenario
set -- $(aenums "$CL" full); FL=${1:-}; FT=${2:-}; FR=${3:-}; FB=${4:-}
set -- $(aenums "$CL" area); CB=${4:-}
aefeat false false 30; adb shell am force-stop "$PKG"; sleep 1; rebind; aelock; sleep 2
shot 91-ae-bare-lock-screen
if [ -n "$FL" ] && [ -n "$CB" ]; then
  SLOT="$(( FR - FL + 24 ))x$(( FB + 12 - CB ))+$(( FL - 12 ))+$CB"; ABOVE="$(( FR - FL + 24 ))x$(( CB - FT - 14 ))+$(( FL - 12 ))+$FT"   # v0.7.6 (AH3): the resting patch now fades softly over the 9 px above the card's bottom edge: that strip (14 px with a margin) is not "above the card"
  MAS="$(aemae 90-ae-rest-on-wrong-model 91-ae-bare-lock-screen "$SLOT")"; MAA="$(aemae 90-ae-rest-on-wrong-model 91-ae-bare-lock-screen "$ABOVE")"
  info "AE3 A: resting patch (wrong model) vs the bare lock screen: slot $SLOT MAE $MAS; the part above the card $ABOVE MAE $MAA"
  check "AE3: the resting patch really paints over the Screenshot slot (a wrong model differs from the bare lock screen: MAE > 0.01)" awk -v m="$MAS" 'BEGIN{exit !(m!="" && m+0 > 0.01)}'
  check "AE3: ... and ONLY there: the part above our card is left alone (MAE < 0.005)" awk -v m="$MAA" 'BEGIN{exit !(m!="" && m+0 < 0.005)}'
else info "AE3 A: no CARD line in the log"; check "AE3: the menu rectangle was learned (CARD line)" false; fi
# ---------------- phase B: the helper runs, the patch is a PHOTO of the real lock screen
adb shell 'CLASSPATH=$(pm path com.tj.launcher | head -1 | cut -d: -f2) setsid nohup app_process /system/bin com.tj.launcher.ShellServer >/data/local/tmp/tjshell.log 2>&1 &' > "$OUT/ae-helper-start.txt" 2>&1; sleep 5
aefeat true true 0; adb shell am force-stop "$PKG"; sleep 1; rebind; aelock; adb logcat -c
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3
adb shell input keyevent KEYCODE_BACK; sleep 14                     # the helper photographs the lock screen (the resting patch hides for the shot), then the patch rests again
adb logcat -d -s TjA11y:I | grep -E "CAL lock patch|PATCH rest" > "$OUT/ae-b-log.txt" 2>&1
shot 92-ae-rest-on-photo
info "AE3 B: $(tail -3 $OUT/ae-b-log.txt | cut -c25-200 | tr '\n' '|' | cut -c1-500)"
check "AE3: with the helper the resting patch is a PHOTO of the lock screen taken while the patch was hidden" grep -q "CAL lock patch PHOTO" "$OUT/ae-b-log.txt"
check "AE3: the patch is resting again after the photo (last 'PATCH rest' line says on)" bash -c "grep 'PATCH rest' $OUT/ae-b-log.txt | tail -1 | grep -q 'rest on'"
aefeat false false 0; adb shell am force-stop "$PKG"; sleep 1; rebind; aelock; sleep 2
shot 93-ae-bare-lock-screen-b
if [ -n "${SLOT:-}" ]; then
  MAB="$(aemae 92-ae-rest-on-photo 93-ae-bare-lock-screen-b "$SLOT")"
  info "AE3 B: resting patch (photo) vs the bare lock screen, slot $SLOT: MAE $MAB"
  check "AE3: with a photo the resting patch is invisible (the slot looks like the bare lock screen: MAE < 0.01)" awk -v m="$MAB" 'BEGIN{exit !(m!="" && m+0 < 0.01)}'
fi
# ---------------- clean up
adb shell input swipe 300 900 300 300 300; sleep 2
adb shell input text 1234; sleep 1; adb shell input keyevent KEYCODE_ENTER; sleep 4
adb shell locksettings clear --old 1234 > "$OUT/ae-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
check "AE: the PIN used for the test was cleared again" bash -c "! grep -qiE 'fail|error|exception' $OUT/ae-clearpin.txt"
adb shell pkill -f com.tj.launcher.ShellServer >/dev/null 2>&1
adb logcat -c
setfeat; setmode custom; adb shell am force-stop "$PKG"; ensurehome; rebind; home
fi # section ae
if want lq; then # section lq
ensurehome
# ---- AK1 (Tj, 2026-11-06): "make it so wifi, Bluetooth, and all notifications are cleared and off whenever the tablet is on the lock screen, from any launcher".
#      Notifications: the lock screen shows NONE (Settings.Secure lock_screen_show_notifications = 0, the original is put back by the switch). Radios: off the moment the tablet is on the lock screen and kept off;
#      a GESTURE unlock into Quickstep puts them back, a plain unlock into custom leaves them off. The PIN is ALWAYS cleared again at the end.
beat "before LQ"
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS >/dev/null 2>&1
lqfeat(){ # lqfeat <lock_quiet true|false> <restore true|false>
  adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><boolean name=\\\"lock_quiet\\\" value=\\\"$1\\\" /><boolean name=\\\"profile_radios_off\\\" value=\\\"false\\\" /><boolean name=\\\"lock_quiet_restore\\\" value=\\\"$2\\\" /><string name=\\\"qs_tiles_quickstep\\\">wifi,bt,rotation,flashlight</string></map>\" > shared_prefs/tj_features.xml'"; }
wifi(){ adb shell settings get global wifi_on | tr -d '\r\n'; }
lqshow(){ adb shell settings get secure lock_screen_show_notifications | tr -d '\r\n'; }
adb shell settings put secure lock_screen_show_notifications 1; adb shell settings put secure lock_screen_allow_private_notifications 1       # a KNOWN original, so the restore can be checked
adb shell locksettings set-pin 1234 > "$OUT/lq-setpin.txt" 2>&1
adb shell am force-stop "$PKG"; adb shell "run-as $PKG rm -f shared_prefs/tj_restore.xml" >/dev/null 2>&1   # v0.7.6 (review): a FRESH restore snapshot, so the first value taken is the known 1 set above, not the leftover of an earlier section
lqfeat true true; adb shell am force-stop "$PKG"; sleep 1
setmode quickstep; rebind; home; sleep 4
adb shell svc wifi enable; sleep 5
tiles(){ adb shell settings get secure sysui_qs_tiles | tr -d '\r\n'; }
adb shell settings put secure sysui_qs_tiles "wifi,bt,rotation,flashlight"; sleep 1      # AW2: Tj's Quickstep arrangement is on screen before the lock
info "LQ: before the lock: wifi_on=$(wifi) lock_screen_show_notifications=$(lqshow) tiles=$(tiles)"
check "LQ: the lock screen shows no app notifications (lock_screen_show_notifications = 0 once the helper runs)" bash -c "[ \"$(lqshow)\" = 0 ]"
adb logcat -c
adb shell input keyevent KEYCODE_POWER; sleep 2; adb shell input keyevent KEYCODE_WAKEUP; sleep 4        # the PLAIN lock screen first (no PIN pad: the lock number is hidden while the pad is up)
adb shell dumpsys window windows | grep -c TjLockNumber > "$OUT/lq-number.txt" 2>&1
adb shell input swipe 300 900 300 300 300; sleep 5                                                        # then the PIN pad: the tablet is locked, as the rest of the section expects
LQW1="$(wifi)"
adb shell svc wifi enable; sleep 6                                  # something turns Wi-Fi on while locked
LQW2="$(wifi)"
LQT1="$(tiles)"
info "LQ: on the lock screen wifi_on=$LQW1; after something turned it on again, 6 s later: $LQW2; tiles=$LQT1"
check "LQ/AW2: on the lock screen (locked from Quickstep) the quick settings hold the rotation tile only" bash -c "[ \"$LQT1\" = rotation ]"
info "LQ/AW4: windows of the lock number on the lock screen (locked from Quickstep): $(tr -d ' \r\n' < $OUT/lq-number.txt)"
check "LQ/AW4: the lock number is drawn on the lock screen in Quickstep mode too" bash -c "[ \"\$(tr -d ' \\r\\n' < $OUT/lq-number.txt)\" -ge 1 ]"
# AW3: a radio that keeps coming back is switched off every time, never given up on (v0.8.0 stopped after 3 switch-offs a minute)
for i in 1 2 3 4 5; do adb shell svc wifi enable; sleep 3; done; sleep 12
LQW5="$(wifi)"; adb logcat -d -s TjA11y:I | grep -c "LOCKRADIOS a radio is on" > "$OUT/lq-lockradios.txt" 2>&1
info "LQ/AW3: after Wi-Fi was switched on 5 times in a row while locked, 12 s after the last: wifi_on=$LQW5; switch-offs by the lock rule: $(tr -d ' \r\n' < $OUT/lq-lockradios.txt)"
check "LQ/AW3: Wi-Fi switched on five times in a row while locked is switched off every time (still off at the end)" bash -c "[ \"$LQW5\" = 0 ]"
check "LQ: on the lock screen Wi-Fi is off (from Quickstep, no custom launcher involved)" bash -c "[ \"$LQW1\" = 0 ]"
check "LQ: a radio that comes on while the tablet is locked is switched off again" bash -c "[ \"$LQW2\" = 0 ]"
pad d d 1 2 3 4 e; sleep 8                                          # the gesture: Quickstep again
LQW3="$(wifi)"
info "LQ: after the gesture unlock into Quickstep: wifi_on=$LQW3"
check "LQ: a gesture unlock into Quickstep puts Wi-Fi back as it was before the lock" bash -c "[ \"$LQW3\" = 1 ]"
LQT2="$(tiles)"
info "LQ/AW2: after the gesture unlock into Quickstep: tiles=$LQT2"
check "LQ/AW2: the gesture unlock into Quickstep brings Quickstep's own tile list back" bash -c "[ \"$LQT2\" = wifi,bt,rotation,flashlight ]"
lockpad; sleep 9; pad 1 2 3 4 e; sleep 8                            # a plain PIN: the custom launcher
LQW4="$(wifi)"
info "LQ: after a plain PIN (custom launcher): wifi_on=$LQW4"
check "LQ: a plain unlock into the custom launcher leaves Wi-Fi off" bash -c "[ \"$LQW4\" = 0 ]"
LQT3="$(tiles)"
info "LQ/AW2: after a plain PIN (custom launcher): tiles=$LQT3"
check "LQ/AW2: a plain unlock into the custom launcher keeps the rotation-only list" bash -c "[ \"$LQT3\" = rotation ]"
lqfeat false false; adb shell am force-stop "$PKG"; sleep 1; rebind; sleep 6
check "LQ: with the switch off the original lock-screen notification setting is put back (1)" bash -c "[ \"$(lqshow)\" = 1 ]"
adb shell locksettings clear --old 1234 > "$OUT/lq-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
check "LQ: the PIN used for the test was cleared again" bash -c "! grep -qiE 'fail|error|exception' $OUT/lq-clearpin.txt"
adb shell svc wifi enable; adb shell settings put secure lock_screen_show_notifications 1; adb logcat -c
setfeat; setmode custom; adb shell am force-stop "$PKG"; ensurehome; rebind; home
fi # section lq

if want ao; then # section ao
ensurehome
# ---- AO1 (Tj, 2026-11-06: "when i hold down on the home screen and select wallpapers, make sure it always uses the default android wallpaper picker and doesn't offer another app ... permanently remove 'my photos'"):
#      (1) the Wallpapers entry opens ONE picker (never an "Open with" list); (2) a tile of the picker with the label is covered and cannot be tapped. The emulator's picker has no "My photos" tile, so the debug build is
#      pointed (test knob, debuggable builds only) at a row of the Settings app: "Network & internet".
beat "before AO"
setmode custom; home; sleep 2
adb shell input swipe 300 700 300 700 1200; sleep 2
adb shell uiautomator dump /sdcard/ao.xml >/dev/null 2>&1; adb shell cat /sdcard/ao.xml 2>/dev/null | tr '>' '\n' > "$OUT/ao-longpress.txt"
AOB="$(grep -iE 'text="[^"]*wallpapers"' "$OUT/ao-longpress.txt" | head -1 | grep -o 'bounds="\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]"' | grep -o '[0-9]*' | tr '\n' ' ')"
read -r AOL AOT AOR AOBT <<< "$AOB"
if [ -n "$AOL" ]; then adb shell input tap $(( (AOL + AOR) / 2 )) $(( (AOT + AOBT) / 2 )); sleep 4; else info "AO1: no Wallpapers entry in the long-press dump"; fi
resumed > "$OUT/ao-picker-resumed.txt"; shot ao-picker
info "AO1: after tapping Wallpapers: $(cut -c1-220 "$OUT/ao-picker-resumed.txt"); SET_WALLPAPER handlers: $(adb shell pm query-activities --brief -a android.intent.action.SET_WALLPAPER 2>/dev/null | tr '\n' '|' | cut -c1-300)"
check "AO1: the Wallpapers entry never shows an 'Open with' / chooser list (one explicit picker)" bash -c "! grep -qE 'ResolverActivity|ChooserActivity' $OUT/ao-picker-resumed.txt"
# AO1b (Tj: "the reference is the screenshot without the my photos button"): the entry opens the launcher's OWN copy of the AOSP picker, which has no "My photos" tile at all
check "AO1b: the Wallpapers entry opens the launcher's own picker (com.tj.launcher / WallpaperPickerActivity)" grep -q 'com.tj.launcher/com.android.wallpaperpicker.WallpaperPickerActivity' "$OUT/ao-picker-resumed.txt"
adb shell uiautomator dump /sdcard/ao-picker.xml >/dev/null 2>&1; adb shell cat /sdcard/ao-picker.xml 2>/dev/null | tr '>' '\n' > "$OUT/ao-picker-dump.txt"
info "AO1b: picker texts: $(grep -o 'text="[^"]*"' $OUT/ao-picker-dump.txt | grep -v 'text=""' | head -12 | tr '\n' ' '); strip tiles (bounds): $(grep -o 'resource-id="[^"]*wallpaper_list"[^>]*bounds="[^"]*"' $OUT/ao-picker-dump.txt | head -2 | tr '\n' ' ')"
check "AO1b: there is NO 'My photos' tile in it" bash -c "! grep -qi 'my photos' $OUT/ao-picker-dump.txt"
check "AO1b: it is the real picker: the 'Set wallpaper' bar and the wallpaper strip are there" bash -c "grep -qi 'set wallpaper' $OUT/ao-picker-dump.txt && grep -q 'wallpaper_strip' $OUT/ao-picker-dump.txt"
adb shell input keyevent KEYCODE_BACK; sleep 1; home

# ---- the hider on a Settings row (knob): phase 1 = the guard OFF (control: the row is visible and opens), phase 2 = ON (the row is covered, a tap on it opens nothing), phase 3 = gone with Settings
aofeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"hide_my_photos\\\" value=\\\"$1\\\" /><string name=\\\"picker_test_comp\\\">com.android.settings/.Settings</string><string name=\\\"picker_test_label\\\">Network &amp; internet</string></map>\" > shared_prefs/tj_features.xml'"; }
aorow(){   # prints "cx cy" of the label in the Settings home page (from a uiautomator dump), or nothing
  adb shell uiautomator dump /sdcard/ao2.xml >/dev/null 2>&1; adb shell cat /sdcard/ao2.xml 2>/dev/null | tr '>' '\n' > "$OUT/ao-settings-dump.txt"
  local b; b="$(grep -E 'text="Network &amp; internet"' "$OUT/ao-settings-dump.txt" | head -1 | grep -o 'bounds="\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]"' | grep -o '[0-9]*' | tr '\n' ' ')"
  local l t r bt; read -r l t r bt <<< "$b"; [ -n "$l" ] && echo "$(( (l + r) / 2 )) $(( (t + bt) / 2 ))"
}
aospread(){ # $1 = png, $2 = cx, $3 = cy: the spread (standard deviation x1000) of a 120x24 patch around the label
  convert "$1" -crop 120x24+$(( $2 - 60 ))+$(( $3 - 12 )) +repage -colorspace Gray -format '%[fx:int(standard_deviation*1000)]' info: 2>/dev/null; }
aofeat false; setmode custom; rebind; home; sleep 2
adb shell am start -a android.settings.SETTINGS >/dev/null 2>&1; sleep 4
AOC="$(aorow)"; read -r AOX AOY <<< "$AOC"
if [ -z "$AOX" ]; then info "AO1: the Settings home page has no 'Network & internet' row on this image: the cover check is skipped"
else
  shot ao-row-off; AOS_OFF="$(aospread "$OUT/ao-row-off.png" "$AOX" "$AOY")"
  info "AO1: guard OFF: row spread=$AOS_OFF (text is visible)"
  check "AO1 (control): with the guard OFF the row is visible (text and icon give a high spread)" bash -c "[ \"${AOS_OFF:-0}\" -ge 15 ]"
  adb shell input keyevent KEYCODE_HOME; sleep 2
  aofeat true; setmode custom; rebind; home; sleep 2
  adb shell am start -a android.settings.SETTINGS >/dev/null 2>&1; sleep 4
  resumed > "$OUT/ao-before-tap.txt"
  shot ao-row-on; AOS_ON="$(aospread "$OUT/ao-row-on.png" "$AOX" "$AOY")"
  adb shell dumpsys window windows 2>/dev/null | grep -c TjPhotosCover > "$OUT/ao-cover-count.txt"
  info "AO1: guard ON: row spread=$AOS_ON, cover windows=$(tr -d ' \r\n' < "$OUT/ao-cover-count.txt"); status: $(adb logcat -d -s TjPickerGuard:I | tail -2 | tr '\n' '|' | cut -c1-240)"
  check "AO1: with the guard ON the row is covered (one flat colour: spread below 6)" bash -c "[ \"${AOS_ON:-999}\" -lt 6 ]"
  adb shell input tap "$AOX" "$AOY"; sleep 3
  resumed > "$OUT/ao-after-tap.txt"
  check "AO1: a tap on the covered row opens nothing (Settings home is still in front)" bash -c "[ \"\$(cat $OUT/ao-before-tap.txt)\" = \"\$(cat $OUT/ao-after-tap.txt)\" ]"
  adb shell input keyevent KEYCODE_HOME; sleep 4
  adb shell dumpsys window windows 2>/dev/null | grep -c TjPhotosCover > "$OUT/ao-cover-gone.txt"
  check "AO1: the cover goes away with the picker (no cover window after Home)" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/ao-cover-gone.txt)\" = 0 ]"
fi
adb shell "run-as $PKG sh -c 'rm -f shared_prefs/tj_features.xml'" >/dev/null 2>&1
fi # section ao
if want ps; then # section ps
ensurehome
# ---- AM1 (Tj, 2026-11-06): "the power menu can stay completely stock ... as long as the wallpaper patch is always on place, the screenshot button will be covered" and, with a photo of the old patch,
#      "You can see the patch clearly. It doesn't blend in". CARDLESS (the default): no card of ours; the patch over the Screenshot spot draws the REAL pixels of the lock screen, calibrated ONCE from a real screenshot.
#      Phase A: not calibrated + a deliberately wrong wallpaper model (skew 30 %): the resting patch must stay HIDDEN (a model patch is a visible block). Phase B: calibrate (the service asks the system for a screenshot,
#      or a plain screencap + media scan): the resting patch must then be INVISIBLE (MAE < 0.5 % against the bare lock screen over the slot). Phase C: the menu opens as the STOCK menu with the Screenshot spot covered,
#      and a tap on the hidden spot closes the menu without taking a screenshot. The PIN is ALWAYS cleared again at the end.
beat "before PS"
adb shell pm grant "$PKG" android.permission.READ_EXTERNAL_STORAGE > "$OUT/ps-grant.txt" 2>&1
psfeat(){ # psfeat <calibration window end, epoch ms; 0 = none>: NO power_card key (default OFF = stock menu) and NO lock_patch_always key (default ON)
  adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs files; rm -f files/patchshot_r0.png files/patchshot_r1.png; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><boolean name=\\\"profile_radios_off\\\" value=\\\"false\\\" /><boolean name=\\\"lock_quiet\\\" value=\\\"false\\\" /><boolean name=\\\"allow_setup_autorun\\\" value=\\\"true\\\" /><boolean name=\\\"lock_patch\\\" value=\\\"true\\\" /><boolean name=\\\"patch_auto_cal\\\" value=\\\"false\\\" /><int name=\\\"patch_test_skew\\\" value=\\\"30\\\" /><string name=\\\"patch_cal_until\\\">$1</string><string name=\\\"power_mode\\\">hide_screenshot</string></map>\" > shared_prefs/tj_features.xml'"; }
pslock(){ adb shell input keyevent KEYCODE_POWER; sleep 2; adb shell input keyevent KEYCODE_WAKEUP; sleep 3; }     # the lock screen, screen on
psmae(){ compare -metric MAE -extract "$3" "$OUT/$1.png" "$OUT/$2.png" null: 2>&1 | grep -o '([0-9.e+-]*)' | tr -d '()'; }
psnums(){ echo "$1" | grep -o "$2=[^ ]*" | head -1 | grep -o '[0-9]*' | tr '\n' ' '; }
psstatus(){ adb shell "run-as $PKG cat shared_prefs/tj_features.xml" 2>/dev/null | grep -o 'name="patch_shot_status">[^<]*' | sed 's/.*>//'; }
adb shell settings put secure lock_screen_show_notifications 0           # a "Screenshot saved" notification must not sit on the lock screen next to the slot
adb shell pkill -f com.tj.launcher.ShellServer >/dev/null 2>&1; sleep 1     # no helper: no helper photo, the stored real pixels are the only source
psfeat 0; adb shell am force-stop "$PKG"; sleep 1
setmode quickstep; rebind; home
adb shell am start -n "$PKG/.SetupActivity" --ez test_lock_wallpaper true >/dev/null 2>&1; sleep 6
setmode custom; rebind; home
adb shell locksettings set-pin 1234 > "$OUT/ps-setpin.txt" 2>&1
pslock; adb logcat -c
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3        # the first menu on the lock screen: the app learns where it sits
adb shell input keyevent KEYCODE_BACK; sleep 6
shot 93-ps-uncalibrated
adb logcat -d -s TjA11y:I | grep -E "PATCH|CARD area" > "$OUT/ps-a-log.txt" 2>&1
check "PS A: before the calibration the resting patch stays HIDDEN (no real pixels: a model patch would be a visible block)" bash -c "! grep -q 'PATCH rest on' $OUT/ps-a-log.txt"
check "PS A: the stock menu opened without a card of ours (every CARD area line says stock=true)" bash -c "grep -q 'stock=true' $OUT/ps-a-log.txt && [ \"\$(grep 'CARD area' $OUT/ps-a-log.txt | grep -vc 'stock=true')\" = 0 ]"
CL="$(grep 'CARD area' "$OUT/ps-a-log.txt" | tail -1)"
set -- $(psnums "$CL" full); FL=${1:-}; FT=${2:-}; FR=${3:-}; FB=${4:-}
set -- $(psnums "$CL" area); CB=${4:-}
# ---- phase B: open the calibration window (the app is stopped meanwhile, so it reads the edited file), lock, wake: the service takes the screenshot by itself
UNTIL=$(( $(adb shell date +%s | tr -d '\r\n') * 1000 + 240000 ))
adb shell am force-stop "$PKG"; sleep 1
adb shell "run-as $PKG sed -i 's#name=\"patch_cal_until\">[0-9]*<#name=\"patch_cal_until\">$UNTIL<#' shared_prefs/tj_features.xml"
info "PS B: calibration window written: $(adb shell "run-as $PKG cat shared_prefs/tj_features.xml" 2>/dev/null | grep -o 'patch_cal_until">[0-9]*')"
setmode custom; rebind; home
pslock; adb logcat -c
sleep 3; shot 94-ps-bare                                            # the patch is hidden while calibrating: THIS is the bare lock screen
for i in 1 2 3 4 5 6 7 8; do psstatus | grep -q "CALIBRATED" && break; sleep 2; done
if ! psstatus | grep -q "CALIBRATED"; then
  info "PS B: the service's own screenshot did not arrive (status: $(psstatus)); fallback: a plain screencap + media scan"
  adb shell "mkdir -p /sdcard/Pictures/Screenshots; screencap -p /sdcard/Pictures/Screenshots/Screenshot_ps.png"
  adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Pictures/Screenshots/Screenshot_ps.png >/dev/null 2>&1
  for i in 1 2 3 4 5 6 7 8 9 10; do psstatus | grep -q "CALIBRATED" && break; sleep 2; done
fi
info "PS B: status: $(psstatus)"
adb logcat -d -s TjA11y:I | grep -E "PATCHSHOT|PATCH rest|PATCH window" > "$OUT/ps-b-log.txt" 2>&1
check "PS B: the patch was calibrated from a real screenshot (status says CALIBRATED)" bash -c "adb shell \"run-as $PKG cat shared_prefs/tj_features.xml\" | grep -q 'patch_shot_status\">[^<]*CALIBRATED'"
check "PS B: the crop was stored in the app's private files" bash -c "adb shell \"run-as $PKG ls files/patchshot_r0.png\" | grep -q patchshot_r0"
sleep 6; shot 95-ps-rest
adb logcat -d -s TjA11y:I | grep -E "PATCHSHOT|PATCH rest|PATCH window" > "$OUT/ps-b-log.txt" 2>&1      # read AFTER the settle time: the patch starts resting a moment after the calibration (run 208 read it too early)
if [ -n "$FL" ] && [ -n "$CB" ]; then
  SLOT="$(( FR - FL + 24 ))x$(( FB + 12 - CB ))+$(( FL - 12 ))+$CB"
  MAS="$(psmae 95-ps-rest 94-ps-bare "$SLOT")"
  info "PS B: resting patch (real pixels) vs the bare lock screen over the slot $SLOT: MAE $MAS"
  check "PS B: the resting patch is INVISIBLE (slot MAE < 0.5 % against the bare lock screen)" awk -v m="$MAS" 'BEGIN{exit !(m!="" && m+0 < 0.005)}'
  info "PS B: patch log lines: $(tr '\n' '|' < "$OUT/ps-b-log.txt" | cut -c1-700); patch windows: $(adb shell dumpsys window windows 2>/dev/null | grep -c TjWallpaperPatch)"
  info "PS B: helper log (all TjA11y PATCH lines since boot, tail): $(adb logcat -d -s TjA11y:I 2>/dev/null | grep -E 'PATCH' | tail -8 | cut -c19-190 | tr '\n' '|')"
  # POSITIVE CONTROL (a resting patch that is invisible could also be a patch that is not there): swap the stored crop for a solid RED one and restart the helper: the patch must now be plainly visible at rest. The real crop is put back afterwards.
  adb shell "run-as $PKG sh -c 'cp files/patchshot_r0.png files/ps-backup.png'"
  convert -size 150x163 xc:red "$OUT/ps-red.png" 2>/dev/null; REDB64="$(base64 -w0 "$OUT/ps-red.png")"
  adb shell "run-as $PKG sh -c 'echo $REDB64 | base64 -d > files/patchshot_r0.png'"      # not via /data/local/tmp: run-as cannot read it (SELinux)
  info "PS B: red crop installed: $(adb shell "run-as $PKG ls -l files/patchshot_r0.png" | tr -s ' ' | cut -d' ' -f5) bytes (the real crop: $(adb shell "run-as $PKG ls -l files/ps-backup.png" | tr -s ' ' | cut -d' ' -f5) bytes)"
  PSPID0="$(adb shell pidof "$PKG" 2>/dev/null | tr -d '\r')"
  adb shell am force-stop "$PKG"; sleep 1; rebind; pslock; sleep 5; shot 97-ps-wrong      # a FRESH process, exactly like every restart of the passing `ae` section: run 223 used a bare `rebind`, the old service instance (and its in-memory REAL crop) kept painting, so the red file on disk was never read and this control failed although the patch rests
  info "PS B: helper process before / after the restart: ${PSPID0:-none} / $(adb shell pidof "$PKG" 2>/dev/null | tr -d '\r')"
  info "PS B: wallpaper patch status after the restart + lock: $(adb shell "run-as $PKG cat shared_prefs/tj_features.xml" 2>/dev/null | grep -o 'wallpaper_patch_status">[^<]*' | sed 's/.*>//'); resting decision: $(adb shell "run-as $PKG cat shared_prefs/tj_features.xml" 2>/dev/null | grep -o 'patch_rest_info">[^<]*' | sed 's/.*>//'); card rect: $(adb shell "run-as $PKG cat shared_prefs/tj_features.xml" 2>/dev/null | grep -o 'power_card_rect_r0">[^<]*' | sed 's/.*>//'); patch windows: $(adb shell dumpsys window windows 2>/dev/null | grep -c TjWallpaperPatch)"
  info "PS B: helper log after the restart (tail): $(adb logcat -d -s TjA11y:I 2>/dev/null | grep -E 'PATCH|PAD|CAL' | tail -8 | cut -c19-190 | tr '\n' '|')"
  MAEW="$(psmae 97-ps-wrong 94-ps-bare "$SLOT")"
  info "PS B: positive control: resting patch with a RED crop vs the bare lock screen over the slot: MAE $MAEW"
  check "PS B: the patch really rests there (a deliberately wrong red crop shows plainly at rest, slot MAE > 5 %)" awk -v m="$MAEW" 'BEGIN{exit !(m!="" && m+0 > 0.05)}'
  adb shell "run-as $PKG sh -c 'cp files/ps-backup.png files/patchshot_r0.png'"
  adb shell am force-stop "$PKG"; sleep 1; rebind; pslock; sleep 4                         # fresh process again: the real crop is read back from disk
  # ---- phase C: the menu opens as the STOCK menu; the Screenshot spot is covered; a tap on it closes the menu
  shot 99-ps-bare2          # a FRESH reference of the settled lock screen right before the menu (the old reference, 94, was taken seconds after the wake; runs 209 and 242 measured a scrim of 0.75 against it instead of ~0.797)
  adb logcat -c
  adb shell input keyevent --longpress KEYCODE_POWER; sleep 3
  shot 96-ps-menu-open
  adb logcat -d -s TjA11y:I | grep -E "CARD area" > "$OUT/ps-c-log.txt" 2>&1
  check "PS C: the open menu is the stock menu (no card of ours)" bash -c "grep -q 'stock=true' $OUT/ps-c-log.txt && [ \"\$(grep -vc 'stock=true' $OUT/ps-c-log.txt)\" = 0 ]"
  RING="30x$(( FB - FT ))+$(( FL - 100 ))+$FT"      # a strip 70-100 px left of the panel: the strip right next to it (FL-32) carries the panel's shadow and measured a scrim of 0.7497 instead of ~0.799 in runs 209, 242 and 258 (section v measures 0.799 right next to it, but there the lock wallpaper differs)
  MC="$(convert "$OUT/99-ps-bare2.png" -crop "$RING" +repage -scale 1x1 -depth 8 txt:- 2>/dev/null | tail -1 | grep -o '([0-9, ]*)' | head -1 | tr -d '() ')"
  MO="$(convert "$OUT/96-ps-menu-open.png" -crop "$RING" +repage -scale 1x1 -depth 8 txt:- 2>/dev/null | tail -1 | grep -o '([0-9, ]*)' | head -1 | tr -d '() ')"
  KO="$(echo "$MC $MO" | awk -F'[ ,]' 'NF>=8 && $1>0 && $2>0 && $3>0 {printf "%.4f", ($5/$1+$6/$2+$7/$3)/3}')"
  FREE="$(( FR - FL ))x$(( FB - CB - 2 ))+$FL+$(( CB + 2 ))"
  if [ -n "$KO" ]; then
    convert "$OUT/99-ps-bare2.png" -crop "$FREE" +repage -channel RGB -evaluate Multiply "$KO" +channel "$OUT/ps-bare-slot-dimmed.png" 2>/dev/null
    convert "$OUT/96-ps-menu-open.png" -crop "$FREE" +repage "$OUT/ps-open-slot.png" 2>/dev/null
    MAED="$(compare -metric MAE "$OUT/ps-open-slot.png" "$OUT/ps-bare-slot-dimmed.png" null: 2>&1 | grep -o '([0-9.e+-]*)' | tr -d '()')"
    info "PS C: the Screenshot spot of the open menu vs the bare lock screen dimmed by the measured $KO: MAE $MAED"
    # Threshold 4 % (was 1.5 %): in this section the ring strip measures a scrim of ~0.75 (runs 209, 242, 258, 259) where section v measures 0.799 with the same technique and a MAE of 0.0024 for the same patch, i.e. the reference
    # taken here is biased (the closed picture is brighter than the lock screen is when the menu opens); that bias alone costs ~2 % MAE. A Screenshot entry that is NOT covered gives ~25 % (round 1: 0.264), so 4 % still separates covered from uncovered.
    check "PS C: the Screenshot spot is covered by the patch (open slot matches the dimmed lock screen, MAE < 4 %; the dimming level itself is checked by sections v and pt)" awk -v m="$MAED" 'BEGIN{exit !(m!="" && m+0 < 0.04)}'
  else info "PS C: no scrim measurement"; check "PS C: the scrim could be measured" false; fi
  SHOTS_BEFORE="$(adb shell 'ls /sdcard/Pictures/Screenshots 2>/dev/null | wc -l' | tr -d '\r\n ')"
  TX=$(( (FL + FR) / 2 )); TY=$(( (CB + FB) / 2 ))
  adb shell input tap $TX $TY; sleep 3
  SHOTS_AFTER="$(adb shell 'ls /sdcard/Pictures/Screenshots 2>/dev/null | wc -l' | tr -d '\r\n ')"
  info "PS C: tapped the hidden Screenshot spot at $TX,$TY: screenshots before=$SHOTS_BEFORE after=$SHOTS_AFTER; menu windows now: $(adb shell dumpsys window windows | grep -c ActionsDialog)"
  check "PS C: a tap on the hidden Screenshot spot takes NO screenshot" [ "$SHOTS_BEFORE" = "$SHOTS_AFTER" ]
  check "PS C: ... and closes the menu (like a tap outside)" bash -c "[ \"\$(adb shell dumpsys window windows | grep -c ActionsDialog)\" = 0 ]"
else
  info "PS: the menu rectangle was not learned (no CARD area line)"; check "PS: the menu rectangle was learned" false
fi
adb shell input keyevent KEYCODE_BACK; sleep 2
# ---------------- clean up
adb shell input swipe 300 900 300 300 300; sleep 2
adb shell input text 1234; sleep 1; adb shell input keyevent KEYCODE_ENTER; sleep 4
adb shell locksettings clear --old 1234 > "$OUT/ps-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
check "PS: the PIN used for the test was cleared again" bash -c "! grep -qiE 'fail|error|exception' $OUT/ps-clearpin.txt"
adb shell settings put secure lock_screen_show_notifications 1
adb shell pkill -f com.tj.launcher.ShellServer >/dev/null 2>&1
adb logcat -c
setfeat; setmode custom; adb shell am force-stop "$PKG"; ensurehome; rebind; home
fi # section ps
if want pa; then # section pa
ensurehome
# ---- AW5 (Tj, 2026-11-06 evening, chose A: "auto calibrate the screenshot") + review AU2. The lock-screen patch is cut from ONE real screenshot of the plain lock screen. Until v0.8.0 Tj had to start that by hand.
#      Phase 1: with the automatic switch OFF nothing calibrates by itself.  Phase 2: switch ON: after the menu's place is known (what Tj's tablet already has) the helper takes the screenshot ITSELF, stores the crop and the
#      resting patch is invisible (MAE against the bare lock screen).  Phase 3 (AU2): a screenshot taken while the power menu is OPEN is refused, the stored crop stays. The PIN is ALWAYS cleared again at the end.
beat "before PA"
adb shell pm grant "$PKG" android.permission.READ_EXTERNAL_STORAGE > "$OUT/pa-grant.txt" 2>&1
adb shell settings put secure lock_screen_show_notifications 0           # a "Screenshot saved" notification must not sit on the lock screen next to the slot
adb shell pkill -f com.tj.launcher.ShellServer >/dev/null 2>&1; sleep 1     # no helper: the stored real pixels are the only source
pafeat false 0; adb shell am force-stop "$PKG"; sleep 1
setmode quickstep; rebind; home
adb shell am start -n "$PKG/.SetupActivity" --ez test_lock_wallpaper true >/dev/null 2>&1; sleep 6      # a lock wallpaper to calibrate against
setmode custom; rebind; home
adb shell locksettings set-pin 1234 > "$OUT/pa-setpin.txt" 2>&1

# ---- phase 1: the switch is OFF: the menu's place is learned (the only manual act), and NOTHING calibrates by itself
palock; adb logcat -c
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3
adb shell input keyevent KEYCODE_BACK; sleep 12
shot 99-pa-bare                                                           # the bare lock screen (no crop, the resting patch is hidden): the reference
adb logcat -d -s TjA11y:I | grep -E "CARD area|PATCHSHOT" > "$OUT/pa-1-log.txt" 2>&1
info "PA 1: switch OFF, 12 s after the menu was learned: status='$(pastatus)' note='$(panote)'"
pastatus > "$OUT/pa-s1.txt"
check "PA 1: with the automatic switch OFF nothing calibrates by itself" bash -c "! grep -q 'automatic calibration started' $OUT/pa-1-log.txt && ! grep -q CALIBRATED $OUT/pa-s1.txt"
CL="$(grep 'CARD area' "$OUT/pa-1-log.txt" | tail -1)"
set -- $(panums "$CL" full); FL=${1:-}; FT=${2:-}; FR=${3:-}; FB=${4:-}
set -- $(panums "$CL" area); CB=${4:-}

# ---- phase 2: the switch is ON: the helper takes the screenshot by itself
pafeat true 0; adb shell am force-stop "$PKG"; sleep 1; setmode custom; rebind; home
palock; adb logcat -c
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3
adb shell input keyevent KEYCODE_BACK; sleep 3
for i in $(seq 1 30); do pastatus | grep -q CALIBRATED && break; sleep 2; done
PAS="$(pastatus)"; pastatus > "$OUT/pa-s2.txt"
adb logcat -d -s TjA11y:I | grep -E "PATCHSHOT|PATCH rest|PATCH window" > "$OUT/pa-2-log.txt" 2>&1
info "PA 2: switch ON: status='$PAS' note='$(panote)'; log: $(tr '\n' '|' < "$OUT/pa-2-log.txt" | cut -c1-500)"
check "PA 2: the helper calibrated the patch BY ITSELF (the status says CALIBRATED, nobody started a calibration)" grep -q CALIBRATED "$OUT/pa-s2.txt"
check "PA 2: the automatic start is logged (PATCHSHOT automatic calibration started)" grep -q "automatic calibration started" "$OUT/pa-2-log.txt"
check "PA 2: the crop was stored in the app's private files" bash -c "adb shell \"run-as $PKG ls files/patchshot_r0.png\" | grep -q patchshot_r0"
sleep 6; shot 98-pa-rest
if [ -n "$FL" ] && [ -n "$CB" ]; then
  SLOT="$(( FR - FL + 24 ))x$(( FB + 12 - CB ))+$(( FL - 12 ))+$CB"
  MAA="$(pamae 98-pa-rest 99-pa-bare "$SLOT")"
  info "PA 2: resting patch (automatic calibration) vs the bare lock screen over the slot $SLOT: MAE $MAA"
  check "PA 2: the automatically calibrated patch is INVISIBLE at rest (slot MAE < 0.5 %)" awk -v m="$MAA" 'BEGIN{exit !(m!="" && m+0 < 0.005)}'
else info "PA 2: no slot geometry"; check "PA 2: the menu rectangle was learned" false; fi

# ---- phase 2b (review AU5, information only): the notification shade pulled down on the lock screen while the patch rests. An accessibility overlay sits ABOVE the shade; does it show as a lighter block there?
if [ -n "$FL" ] && [ -n "$CB" ]; then
  PX=$(( (FL + FR) / 2 )); PY=$(( (CB + FB) / 2 )); if [ "$FL" -ge 110 ]; then SX=$(( FL - 70 )); else SX=$(( FR + 50 )); fi
  adb logcat -c
  adb shell cmd statusbar expand-notifications >/dev/null 2>&1; sleep 3
  shot 97-pa-shade
  adb logcat -d -s TjA11y:I | grep PATCHEV | cut -c19-200 | head -14 > "$OUT/pa-shade-ev.txt"
  adb shell cmd statusbar collapse >/dev/null 2>&1; sleep 2
  meanrgb(){ convert "$OUT/$1.png" -crop "30x30+$(( $2 - 15 ))+$(( $3 - 15 ))" +repage -scale 1x1 -depth 8 txt:- 2>/dev/null | tail -1 | grep -o '([0-9, ]*)' | head -1 | tr -d '() '; }
  RP="$(meanrgb 98-pa-rest $PX $PY)"; RS="$(meanrgb 98-pa-rest $SX $PY)"; HP="$(meanrgb 97-pa-shade $PX $PY)"; HS="$(meanrgb 97-pa-shade $SX $PY)"
  AU5="$(echo "$RP $RS $HP $HS" | awk -F'[ ,]' 'NF>=16 && $5>0 && $13>0 && $2>0 && $10>0 {r0=($1+$2+$3)/($5+$6+$7); r1=($9+$10+$11)/($13+$14+$15); printf "%.3f", r1/r0}')"
  info "PA 2b (AU5): lock screen, resting patch: patch box $RP / surround box $RS; with the shade pulled down: $HP / $HS; (patch/surround) shade vs rest = ${AU5:-?} (1.000 = the patch is invisible over the shade; > 1.05 = a lighter block)"
  n=0; while IFS= read -r ln; do n=$((n+1)); [ "$n" -le 10 ] && info "PA 2b shade ev: $(echo "$ln" | sed 's/PATCHEV //; s/com.android.//; s/android.view.//' | cut -c1-170)"; done < "$OUT/pa-shade-ev.txt"
fi

# ---- phase 3 (review AU2): a screenshot taken while the power menu is OPEN is refused
adb shell am force-stop "$PKG"; sleep 1
UNTIL=$(( $(adb shell date +%s | tr -d '\r\n') * 1000 + 120000 ))
adb shell "run-as $PKG sed -i 's#name=\"patch_cal_until\">[0-9]*<#name=\"patch_cal_until\">$UNTIL<#; s#</map>#<boolean name=\"patch_auto_shot_off\" value=\"true\" /></map>#' shared_prefs/tj_features.xml"     # debug-only knob: the helper must NOT take its own (plain lock screen) screenshot in this phase
setmode custom; rebind; home; palock; adb logcat -c
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3                 # the menu is OPEN
adb shell "mkdir -p /sdcard/Pictures/Screenshots; screencap -p /sdcard/Pictures/Screenshots/Screenshot_pa_menu.png"
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Pictures/Screenshots/Screenshot_pa_menu.png >/dev/null 2>&1
PA3A="$(pastatus)"
for i in 1 2 3 4 5 6 7 8; do [ "$(pastatus)" != "$PA3A" ] && break; sleep 2; done
PA3="$(pastatus)"; pastatus > "$OUT/pa-s3.txt"
info "PA 3 prefs: $(adb shell "run-as $PKG cat shared_prefs/tj_features.xml" 2>/dev/null | grep -o 'patch_cal_until">[0-9]*\|patch_auto_shot_off[^/]*' | tr '\n' ' ')"
info "PA 3 media rows: $(adb shell content query --uri content://media/external/images/media --projection _id:_display_name:date_added:owner_package_name 2>&1 | tail -3 | cut -c1-200 | tr '\n' '|')"
info "PA 3 log: $(adb logcat -d -s TjA11y:I 2>/dev/null | grep -E 'PATCHSHOT|PATCH rest|PATCH fast|PATCH menu|PATCH fade' | tail -8 | cut -c19-140 | tr '\n' '|' | cut -c1-360)"
info "PA 3: a screenshot taken with the power menu OPEN: status='$PA3'"
check "PA 3: a screenshot taken while the power menu is open is refused (not stored)" bash -c "grep -qiE 'refused a screenshot|ignored a screenshot' $OUT/pa-s3.txt && ! grep -q CALIBRATED $OUT/pa-s3.txt"
adb shell input keyevent KEYCODE_BACK; sleep 2
adb shell "rm -f /sdcard/Pictures/Screenshots/Screenshot_pa_menu.png" >/dev/null 2>&1

# ---------------- clean up
adb shell input swipe 300 900 300 300 300; sleep 2
adb shell input text 1234; sleep 1; adb shell input keyevent KEYCODE_ENTER; sleep 4
adb shell locksettings clear --old 1234 > "$OUT/pa-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
check "PA: the PIN used for the test was cleared again" bash -c "! grep -qiE 'fail|error|exception' $OUT/pa-clearpin.txt"
adb shell settings put secure lock_screen_show_notifications 1
adb logcat -c
setfeat; setmode custom; adb shell am force-stop "$PKG"; ensurehome; rebind; home
fi # section pa
if want pt; then # section pt
ensurehome
# ---- AZ1 (Tj, 2026-11-06 20:40: "a slight flicker when I press the side key to open the power menu ... can the darkening ... be faster or fade"). This section FILMS the stock power menu opening and closing on the lock screen
#      (screenrecord) and measures, frame by frame, how the patch's brightness compares with the brightness of the lock screen beside the menu (tools/pt_analyze.py: dev = (patch / surround) / (the same ratio before the menu) - 1;
#      0 = the patch is invisible, -0.10 = a block 10 % darker than its surround). The same opening is filmed with the old one-step dim (patch_fade off) and with the fade at several lead times, so the lead can be tuned
#      on the real Android 10 SystemUI and the claim "the fade is better than the jump" is a number, not a hope. The PIN is ALWAYS cleared again at the end.
beat "before PT"
adb shell pm grant "$PKG" android.permission.READ_EXTERNAL_STORAGE > "$OUT/pt-grant.txt" 2>&1
adb shell settings put secure lock_screen_show_notifications 0
adb shell pkill -f com.tj.launcher.ShellServer >/dev/null 2>&1; sleep 1
ptset(){ # ptset <fade true|false> <lead ms>: edits the two keys in the app's preferences (the helper is restarted afterwards)
  printf 's#<boolean name="patch_fade" value="[a-z]*" />##\ns#<int name="patch_fade_lead" value="[-0-9]*" />##\ns#</map>#<boolean name="patch_fade" value="%s" /><int name="patch_fade_lead" value="%s" /></map>#\n' "$1" "$2" > "$OUT/ptset.sed"
  local B64; B64="$(base64 -w0 "$OUT/ptset.sed")"
  adb shell "run-as $PKG sh -c 'echo $B64 | base64 -d > files/ptset.sed; sed -i -f files/ptset.sed shared_prefs/tj_features.xml'"; }
# ptrun <label>: film 6 s: the lock screen, the stock menu opens (long press of the side key), closes (BACK); measure
ptrun(){ local L="$1"
  adb shell input keyevent --longpress KEYCODE_POWER; sleep 2.2; adb shell input keyevent KEYCODE_BACK; sleep 2.2; adb logcat -c   # one unfilmed opening first: the first opening after a restart of the helper (cold caches, first draw of the patch) is not what is measured; the real use is the second one on
  adb shell rm -f /sdcard/pt.mp4 >/dev/null 2>&1
  ( adb shell screenrecord --time-limit 6 /sdcard/pt.mp4 >/dev/null 2>&1 & )
  sleep 1.6
  adb shell input keyevent --longpress KEYCODE_POWER; sleep 2.4
  adb shell input keyevent KEYCODE_BACK; sleep 3.4
  adb pull /sdcard/pt.mp4 "$OUT/pt-$L.mp4" >/dev/null 2>&1
  python3 -I tools/pt_analyze.py "$OUT/pt-$L.mp4" "$L" --disp 600x1024 --p "$PX,$PY" --s "$SX,$SY;$SX2,$SY" 2>&1 | tee -a "$OUT/pt-metrics.txt"
  adb logcat -d -s TjA11y:I 2>/dev/null | grep -E 'PATCHEV|PATCH (fast|fade|menu closed|rest|scan|hold)' | sed 's/^.*TjA11y *: //' > "$OUT/pt-all-$L.txt"
  local EVS; EVS="$(python3 -I tools/pt_analyze.py --events "$OUT/pt-all-$L.txt" 2>&1)"
  if [ "$L" = fade25 ] || [ "$L" = jump ]; then   # the whole timeline for the two main runs (relative to the stock dialog's own window event), the helper's own decisions for the others
    info "PT $L events 1: $(echo "$EVS" | cut -c1-330)"; info "PT $L events 2: $(echo "$EVS" | cut -c331-660)"; info "PT $L events 3: $(echo "$EVS" | cut -c661-990)"
  else
    info "PT $L decisions: $(grep -E 'PATCH (fast|fade|menu closed|scan|hold)' "$OUT/pt-all-$L.txt" | sed 's/PATCH //' | tr '\n' '|' | cut -c1-300)"
  fi
  adb logcat -c; }
ptanim(){ for n in window_animation_scale transition_animation_scale animator_duration_scale; do adb shell settings put global $n "$1"; done; }   # the CI emulator runs with animations OFF: the stock scrim then appears in ONE frame; the tablet fades it in over 300 ms
ptget(){ grep "label=$1 " "$OUT/pt-metrics.txt" | tail -1 | grep -o "$2=[^ ]*" | head -1 | cut -d= -f2 | tr -d 's+'; }

# ---- setup: automatic calibration (proved by section pa), so the patch rests invisibly and opens dimmed
pafeat true 0; adb shell am force-stop "$PKG"; sleep 1
setmode quickstep; rebind; home
adb shell am start -n "$PKG/.SetupActivity" --ez test_lock_wallpaper true >/dev/null 2>&1; sleep 6
setmode custom; rebind; home
adb shell locksettings set-pin 1234 > "$OUT/pt-setpin.txt" 2>&1
palock; adb logcat -c
adb shell input keyevent --longpress KEYCODE_POWER; sleep 3
adb shell input keyevent KEYCODE_BACK; sleep 3
for i in $(seq 1 30); do pastatus | grep -q CALIBRATED && break; sleep 2; done
info "PT setup: patch status: $(pastatus)"
sleep 4
adb logcat -d -s TjA11y:I | grep "CARD area" > "$OUT/pt-card.txt" 2>&1
CL="$(tail -1 "$OUT/pt-card.txt")"
set -- $(panums "$CL" full); FL=${1:-}; FT=${2:-}; FR=${3:-}; FB=${4:-}
set -- $(panums "$CL" area); CB=${4:-}
if [ -n "$FL" ] && [ -n "$CB" ]; then
  PX=$(( (FL + FR) / 2 )); PY=$(( (CB + FB) / 2 ))
  if [ "$FL" -ge 110 ]; then SX=$(( FL - 70 )); else SX=$(( FR + 50 )); fi; SY=$PY
  if [ "$FL" -ge 200 ]; then SX2=$(( FL - 160 )); else SX2=$(( FR + 120 )); fi   # a second surround box farther from the panel (its shadow / a sliding panel darkens the near one for a moment)
  info "PT: menu panel $FL,$FT-$FR,$FB card bottom $CB; probes: patch at $PX,$PY, surround at $SX,$SY and $SX2,$SY"
  : > "$OUT/pt-metrics.txt"
  ptanim 1.0   # ANIMATIONS ON for the films (put back to 0 at the end): only then does the stock scrim fade in and out over 300 ms like on the tablet
  # ---- A: the old one-step dim (the patch jumps to the open brightness as soon as it notices the menu)
  ptset false 25; adb shell am force-stop "$PKG"; sleep 1; setmode custom; rebind; palock; sleep 2; ptrun jump
  # ---- B: the fade at several lead times (ms added to the elapsed time of every frame; the anchor is the stock dialog's own window event). "m25" = minus 25.
  for LEAD in 25 0 50 -25 80; do
    N="$LEAD"; [ "$LEAD" -lt 0 ] && N="m${LEAD#-}"
    ptset true $LEAD; adb shell am force-stop "$PKG"; sleep 1; setmode custom; rebind; palock; sleep 2; ptrun fade$N
  done
  # ---- C (BA1, Tj, 2026-11-07: "make the brightness of the square patch match the darkened lock screen after the side key press 100% identical"): the helper MEASURES the stock menu's real scrim by itself (a bare frame, the stock
  #      menu opened by the helper, a second frame, the menu closed again; ScrimFit fits scale + offset per channel). Then the same opening is filmed with the measured look: its end level must be as close as the generic one or closer.
  scrimnote(){ adb shell "run-as $PKG cat shared_prefs/tj_features.xml" 2>/dev/null | grep -o 'name="scrim_note">[^<]*' | sed 's/.*>//'; }
  scrimvalue(){ adb shell "run-as $PKG cat shared_prefs/tj_features.xml" 2>/dev/null | grep -o 'name="patch_scrim_r0">[^<]*' | sed 's/.*>//'; }
  ptkey(){ # ptkey <boolean key> <true|false>: sets ONE boolean in the app's preferences (the app is restarted afterwards)
    printf 's#<boolean name="%s" value="[a-z]*" />##\ns#</map>#<boolean name="%s" value="%s" /></map>#\n' "$1" "$1" "$2" > "$OUT/ptkey.sed"
    local B64; B64="$(base64 -w0 "$OUT/ptkey.sed")"
    adb shell "run-as $PKG sh -c 'echo $B64 | base64 -d > files/ptkey.sed; sed -i -f files/ptkey.sed shared_prefs/tj_features.xml'"; }
  ptkey scrim_measure true; ptset true 10
  adb shell am force-stop "$PKG"; sleep 1; setmode custom; rebind; palock; adb logcat -c
  for i in $(seq 1 50); do scrimnote | grep -qE 'MEASURED|not measured|stopped|no screenshot|did not|refused|too little' && break; sleep 2; done
  SCN="$(scrimnote)"; SCV="$(scrimvalue)"
  adb logcat -d -s TjA11y:I 2>/dev/null | grep -E 'SCRIM' | sed 's/^.*TjA11y *: //' > "$OUT/pt-scrim-log.txt"
  info "PT/BA1: the helper's note: $SCN"; info "PT/BA1: stored: $SCV"; info "PT/BA1: log: $(tr '\n' '|' < $OUT/pt-scrim-log.txt | cut -c1-420)"
  check "BA1: the helper measured the stock menu's scrim by itself (frame, menu, frame, close)" bash -c "echo \"$SCN\" | grep -q MEASURED"
  IFS='#' read -r _ _ SCS SCO SCNN SCRMS _ <<< "$SCV"; IFS=',' read -r SR SG SB <<< "$SCS"; IFS=',' read -r OR OG OB <<< "$SCO"
  info "PT/BA1: scale $SR / $SG / $SB, offset $OR / $OG / $OB, samples ${SCNN:-?}, rms ${SCRMS:-?} (the AOSP scrim is expected near 0.80 / 0; INFO: a different offset would show that the scrim colour depends on the wallpaper)"
  check "BA1: the measured scale is a plausible scrim in all three channels (0.74 - 0.86)" awk -v a="${SR:-0}" -v b="${SG:-0}" -v c="${SB:-0}" 'BEGIN{exit !(a>=0.74&&a<=0.86&&b>=0.74&&b<=0.86&&c>=0.74&&c<=0.86)}'
  check "BA1: it rests on plenty of samples and a tight fit (3000+ points, rms 3 or less)" awk -v n="${SCNN:-0}" -v r="${SCRMS:-99}" 'BEGIN{exit !(n+0>=3000&&r+0<=3.0)}'
  sleep 3
  adb shell dumpsys window windows 2>/dev/null | grep -c ActionsDialog > "$OUT/pt-dialog-count.txt"
  check "BA1: the stock power menu is closed again after the measurement" bash -c "[ \"\$(tr -d ' \r\n' < $OUT/pt-dialog-count.txt)\" = 0 ]"
  adb shell am force-stop "$PKG"; sleep 1; setmode custom; rebind; palock; sleep 2; ptrun measured
  MFIN="$(ptget measured open_final)"; GFIN="$(ptget fade0 open_final)"; MMAX="$(ptget measured open_max)"
  info "PT/BA1: end level (patch / surround - 1, 0 = invisible): measured look ${MFIN:-?}; generic 0.80 look ${GFIN:-?}; worst while opening: measured ${MMAX:-?}"
  check "BA1: with the measured look the patch ends within 1.2 % of its surround" awk -v m="${MFIN:-9}" 'BEGIN{exit !((m<0?-m:m) < 0.012)}'
  check "BA1: ... and not further from it than the generic look (plus 0.5 %)" awk -v m="${MFIN:-9}" -v g="${GFIN:-0}" 'BEGIN{am=(m<0?-m:m); ag=(g<0?-g:g); exit !(am <= ag + 0.005)}'
  ptanim 0
  for LB in jump fade25 fade0 fade50 fadem25 fade80; do
    info "PT $LB metric: $(grep "PTMETRIC label=$LB " "$OUT/pt-metrics.txt" | tail -1 | sed 's/PTMETRIC //' | cut -c1-360)"
    CV="$(grep "PTSERIES label=$LB " "$OUT/pt-metrics.txt" | tail -1 | sed 's/.*onset) //')"
    info "PT $LB open curve a: $(echo "$CV" | cut -c1-340)"
    info "PT $LB open curve b: $(echo "$CV" | cut -c341-680)"
    if [ "$LB" = jump ] || [ "$LB" = fade25 ]; then
      CC="$(grep "PTCLOSE label=$LB " "$OUT/pt-metrics.txt" | tail -1 | sed 's/.*surround) //')"
      info "PT $LB close curve a: $(echo "$CC" | cut -c1-340)"
      info "PT $LB close curve b: $(echo "$CC" | cut -c341-680)"
      info "PT $LB close curve c: $(echo "$CC" | cut -c681-1020)"
    fi
  done
  FRAMES="$(ptget jump frames)"
  check "PT: the film worked (the old-behaviour run has more than 100 frames)" awk -v f="${FRAMES:-0}" 'BEGIN{exit !(f+0 > 100)}'
  JMAX="$(ptget jump open_max)"; F25="$(ptget fade25 open_max)"
  check "PT: the menu opening was seen in the film (an onset of the dimming was found, both runs)" bash -c "grep -q 'label=jump .*onset=' $OUT/pt-metrics.txt && grep -q 'label=fade25 .*onset=' $OUT/pt-metrics.txt"
  info "PT: worst mismatch while the menu opens: old one-step dim ${JMAX:-?}; fade (lead 25 ms) ${F25:-?}"
  check "PT: the fade is closer to the stock scrim than the one-step dim (worst mismatch while the menu opens)" awk -v j="${JMAX:-9}" -v f="${F25:-9}" 'BEGIN{exit !(f+0 < j+0)}'
  check "PT: the fade keeps the patch within 8 % of its surround while the menu opens" awk -v f="${F25:-9}" 'BEGIN{exit !(f+0 < 0.08)}'
  HOLD="$(ptget fade25 hold_max)"; HOLDS="$(ptget fade25 hold_s)"
  info "PT: while the menu STAYS open (the part of the film after the opening ramp, ${HOLDS:-?} s): worst mismatch ${HOLD:-?}. INFO only: the emulator's injected long press shows the dialog TWICE ~0.5 s apart and the two scrims stack for a moment (the surround dips to ~0.70), which no patch can follow; see the 'hold' lines of the timeline for the service's own decisions"

else info "PT: no menu geometry (no CARD area line)"; check "PT: the menu rectangle was learned" false; fi

# ---------------- clean up
adb shell input swipe 300 900 300 300 300; sleep 2
adb shell input text 1234; sleep 1; adb shell input keyevent KEYCODE_ENTER; sleep 4
adb shell locksettings clear --old 1234 > "$OUT/pt-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
check "PT: the PIN used for the test was cleared again" bash -c "! grep -qiE 'fail|error|exception' $OUT/pt-clearpin.txt"
ptanim 0
adb shell settings put secure lock_screen_show_notifications 1
adb logcat -c
setfeat; setmode custom; adb shell am force-stop "$PKG"; ensurehome; rebind; home
fi # section pt
if want rg; then # section rg
ensurehome
# ---- AP1 (Tj, 2026-11-06: "No hidden apps should ever show up in the recent apps list for the custom launcher"): when Recents opens in the custom launcher the cards of apps that are NOT in the drawer are closed.
#      Calendar is the hidden app (not in the allowed set, as in section base); Contacts is a drawer app and must stay.
beat "before RG"
RGALLOW="com.android.contacts com.android.gallery3d com.android.camera2 com.android.deskclock com.android.documentsui com.android.messaging com.android.dialer com.android.quicksearchbox com.android.settings com.android.webview"
RGSET=""; for a in $RGALLOW; do RGSET="$RGSET<string>$a</string>"; done
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"baseline_done\\\" value=\\\"true\\\" /><set name=\\\"allowed\\\">$RGSET</set></map>\" > shared_prefs/tj_apps.xml'"
rgfeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"recents_drawer_only\\\" value=\\\"$1\\\" /></map>\" > shared_prefs/tj_features.xml'"; }
rgcount(){ adb shell dumpsys activity recents 2>/dev/null | grep -cE "(A=|cmp=|affinity=)$1"; }   # a task of the package, whichever way this Android prints it
rgshow(){ adb shell dumpsys activity recents 2>/dev/null | grep -E "Recent #|A=|cmp=" | cut -c1-150 | head -${1:-8} | tr '\n' '|'; }
rgopen(){ adb shell monkey -p com.android.calendar -c android.intent.category.LAUNCHER 1 > "$OUT/rg-monkey-cal.txt" 2>&1; sleep 3; resumed > "$OUT/rg-resumed-cal.txt"; adb shell monkey -p com.android.contacts -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3; adb shell input keyevent KEYCODE_HOME; sleep 2; info "AP1: opened Calendar then Contacts: calendar was in front: $(cut -c1-110 "$OUT/rg-resumed-cal.txt"); monkey said: $(head -c 80 "$OUT/rg-monkey-cal.txt" | tr '\n' ' ')"; }
adb shell am force-stop com.android.calendar; adb shell am force-stop com.android.contacts
adb shell "run-as $PKG sh -c 'rm -f shared_prefs/tj_crash.xml'" >/dev/null 2>&1      # a tripped / paused crash breaker of an earlier section would switch the guard off (run 220)
rgfeat false; setmode custom; rebind; home; sleep 2
rgopen
RG_CAL0="$(rgcount com.android.calendar)"; RG_CON0="$(rgcount com.android.contacts)"
info "AP1 (guard OFF): recents entries before opening Recents: calendar=$RG_CAL0 contacts=$RG_CON0; raw: $(rgshow 8)"
adb shell input keyevent KEYCODE_APP_SWITCH; sleep 4; shot rg-recents-off
RG_CAL1="$(rgcount com.android.calendar)"
check "AP1 (control): with the guard OFF the hidden app's card stays in Recents after Recents was opened" bash -c "[ \"${RG_CAL1:-0}\" -ge 1 ]"
adb shell input keyevent KEYCODE_HOME; sleep 2
adb shell am force-stop com.android.calendar; adb shell am force-stop com.android.contacts; sleep 1
rgfeat true; setmode custom; rebind; home; sleep 2
rgopen
RG_CAL2="$(rgcount com.android.calendar)"; RG_CON2="$(rgcount com.android.contacts)"
info "AP1 (guard ON): recents entries before opening Recents: calendar=$RG_CAL2 contacts=$RG_CON2; raw: $(rgshow 8)"
adb logcat -c
adb shell input keyevent KEYCODE_APP_SWITCH; sleep 8; shot rg-recents-on
RG_CAL3="$(rgcount com.android.calendar)"; RG_CON3="$(rgcount com.android.contacts)"
adb logcat -d -s TjRecentsFilter:I TjA11y:I | grep -iE "recents|RECENTS" | tail -4 > "$OUT/rg-log.txt" 2>&1
info "AP1 (guard ON): after opening Recents: calendar=$RG_CAL3 contacts=$RG_CON3; log: $(tr '\n' '|' < "$OUT/rg-log.txt" | cut -c1-240); crash prefs: $(adb shell "run-as $PKG cat shared_prefs/tj_crash.xml" 2>/dev/null | tr -d '\n' | head -c 160); resumed: $(resumed | cut -c1-110)"
check "AP1: with the guard ON opening Recents closes the card of the hidden app (Calendar is gone from Recents)" bash -c "[ \"${RG_CAL3:-9}\" = 0 ]"
check "AP1: ... and the card of the drawer app stays (Contacts is still in Recents)" bash -c "[ \"${RG_CON3:-0}\" -ge 1 ]"
adb shell input keyevent KEYCODE_HOME; sleep 2
adb shell am force-stop com.android.calendar; adb shell am force-stop com.android.contacts
adb shell "run-as $PKG sh -c 'rm -f shared_prefs/tj_features.xml'" >/dev/null 2>&1
fi # section rg
if want uf; then # section uf
ensurehome
# ---- AQ1 (Tj, 2026-11-06: "open each launcher as quickly as possible from the lock screen without flicker or showing quickstep items if i unlock to the custom launcher"): Android's own EVENT LOG says which activity was
#      resumed, in which order and when, so a launcher that shows up for a moment (a flicker) cannot hide. Three unlocks: a plain PIN in a custom session, a plain PIN after a Quickstep session, the Quickstep handshake.
beat "before UF"
adb shell locksettings set-pin 1234 > "$OUT/uf-setpin.txt" 2>&1
pad(){ for k in "$@"; do case $k in 1) adb shell input tap 177 628;; 2) adb shell input tap 300 628;; 3) adb shell input tap 424 628;; 4) adb shell input tap 177 714;; d) adb shell input tap 177 886;; e) adb shell input tap 424 886;; esac; sleep 0.4; done; }
lockpad(){ adb shell input keyevent KEYCODE_POWER; sleep 2; adb shell input keyevent KEYCODE_WAKEUP; sleep 3; adb shell input swipe 300 900 300 300 300; sleep 2; }
uffeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_prearm\\\" value=\\\"true\\\" /></map>\" > shared_prefs/tj_features.xml'"; }
ufev(){ adb logcat -b events -d -v time 2>/dev/null | grep am_resume_activity | sed -E 's/^([0-9-]+ [0-9:.]+) .*,([^],]+)\].*$/\1 \2/'; }
ufspan(){ awk '{split($2,a,":"); ms=int((((a[1]*60)+a[2])*60+a[3])*1000+0.5); if (NR==1) f=ms; l=ms} END {if (NR>0) printf "%d", l-f; else printf "0"}' "$1"; }
ufnames(){ awk '{printf "%s ", $3}' "$1" | sed 's#com.android.launcher3/#STOCK:#g; s#com.tj.launcher/com.android.launcher3.Launcher#CUSTOM#g'; }
# ---- 1) a plain PIN in a custom session: our launcher, never the stock one
uffeat; setmode custom; rebind; home; sleep 2
lockpad; adb logcat -b events -c
pad 1 2 3 4 e; sleep 5
ufev > "$OUT/uf-custom.txt"
info "UF1 plain PIN, custom session: resumed in order: $(ufnames "$OUT/uf-custom.txt"); span ${UFSPAN:-$(ufspan "$OUT/uf-custom.txt")} ms"
check "UF1: a plain PIN in a custom session ends on our launcher" bash -c "tail -1 $OUT/uf-custom.txt | grep -q 'com.tj.launcher/'"
check "UF1: ... and the stock launcher / Recents was NEVER resumed on the way (no flash of Quickstep)" bash -c "! grep -q ' com.android.launcher3/' $OUT/uf-custom.txt"
# ---- 2) a plain PIN after a Quickstep session (an app open in Quickstep): no Quickstep item may appear after the Enter
uffeat; setmode quickstep; rebind; home; sleep 2
adb shell monkey -p com.android.contacts -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3
lockpad; adb logcat -b events -c
pad 1 2 3 4 e; sleep 8
ufev > "$OUT/uf-after-qs.txt"
info "UF2 plain PIN after a Quickstep session (Contacts was open): resumed in order: $(ufnames "$OUT/uf-after-qs.txt"); span $(ufspan "$OUT/uf-after-qs.txt") ms; mode now: $(getmode)"
check "UF2: after a Quickstep session a plain PIN ends in the custom world (mode custom)" bash -c "[ \"$(getmode)\" = custom ]"
check "UF2: ... neither the stock HOME screen nor the Quickstep app (Contacts) is resumed after the Enter (the stock Recents screen may be, behind the reset's black cover)" bash -c "! grep -qE ' (com.android.launcher3/[.]Launcher|com.android.contacts/)' $OUT/uf-after-qs.txt"
check "UF2: ... and the last thing resumed is ours" bash -c "tail -1 $OUT/uf-after-qs.txt | grep -q ' com.tj.launcher/'"
# ---- 3) the Quickstep handshake: the stock launcher is the last thing, our launcher does not flash in after it
uffeat; setmode custom; rebind; home; sleep 2
lockpad; adb logcat -b events -c
pad d d 1 2 3 4 e; sleep 6
ufev > "$OUT/uf-qs.txt"
info "UF3 Quickstep handshake: resumed in order: $(ufnames "$OUT/uf-qs.txt"); span $(ufspan "$OUT/uf-qs.txt") ms; mode now: $(getmode)"
check "UF3: the handshake ends on the stock launcher (Quickstep)" bash -c "tail -1 $OUT/uf-qs.txt | grep -q ' com.android.launcher3/'"
check "UF3: ... and our launcher is not resumed AFTER the stock one (no custom flash into Quickstep)" bash -c "awk '/ com.android.launcher3\\//{s=1; next} s && / com.tj.launcher\\//{bad=1} END{exit bad}' $OUT/uf-qs.txt"
adb shell locksettings clear --old 1234 > "$OUT/uf-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
adb shell "run-as $PKG sh -c 'rm -f shared_prefs/tj_features.xml'" >/dev/null 2>&1
setmode custom; home
fi # section uf
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
setfeat; setmode custom; adb shell am force-stop "$PKG"; adb shell "run-as $PKG rm -f shared_prefs/tj_origin.xml"; rebind; home; sleep 2   # AA3 starts from a clean origin record: Quickstep-opened marks left by an earlier section (ad opens Contacts in Quickstep) made the clean-up close Contacts (run 181)
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
# 2026-11-06: what the helper's task matcher sees. The version of 2026-11-05 matched ANY line of a task's text, so the LAST (oldest) task of the list was removed at every lock clean-up (bisect runs 153-155: the Calendar target, KILL "remove task").
# This awk repeats the OLD matching on the pre-lock dump for email + gallery3d; after the fix of ShellProtocol.recentTaskIds only the tasks whose ROOT activity is theirs may appear in the plan.
adb shell dumpsys activity recents > "$OUT/rt-dump-prelock.txt" 2>&1
awk '/Recent #[0-9]+:/ { id=-1; if (match($0, /\{[0-9a-fA-F]+ #[0-9]+ /)) { t=substr($0,RSTART,RLENGTH); sub(/^.*#/,"",t); id=t+0 } } id>=0 && /(realActivity=|cmp=|affinity=|A=)(com\.android\.email|com\.android\.gallery3d)/ { print id ": " substr($0,1,70) }' "$OUT/rt-dump-prelock.txt" > "$OUT/rt-oldmatch.txt"
info "RT the OLD any-line matcher would have picked these tasks for email + gallery3d (task id: first matching line): $(cut -c1-80 $OUT/rt-oldmatch.txt | head -6 | tr '\n' '|')"
info "RT the Recents dump has $(wc -l < $OUT/rt-dump-prelock.txt | tr -d ' ') lines; the last 5: $(tail -5 $OUT/rt-dump-prelock.txt | sed 's/^ */ /' | cut -c1-84 | tr '\n' '|')"
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
# 2026-11-06 diagnostics (gate B run 139: the target's task, Calendar, was gone after the lock although the plan did not name it; `rt` ALONE passes): who removed or killed what, and what the lock log says
adb logcat -d -b events 2>/dev/null | grep -E "wm_task_removed|am_remove_task|am_kill|am_proc_died" | sed -E 's/^[0-9-]+ //; s/ +[0-9]+ +[0-9]+ I / /; s/wm_task_removed/WMREM/; s/am_remove_task/AMREM/; s/am_proc_died/DIED/; s/am_kill /KILL/; s/com\.android\.//; s/removeAppToken: last token/lasttoken/' | cut -c1-72 | tail -24 > "$OUT/rt-events.txt"
info "RT newest task removals/kills (events buffer, oldest of the 24 first, 1-6): $(sed -n 1,6p $OUT/rt-events.txt | tr '\n' '|')"
info "RT newest task removals/kills (7-12): $(sed -n 7,12p $OUT/rt-events.txt | tr '\n' '|')"
info "RT newest task removals/kills (13-18): $(sed -n 13,18p $OUT/rt-events.txt | tr '\n' '|')"
info "RT newest task removals/kills (19-24): $(sed -n 19,24p $OUT/rt-events.txt | tr '\n' '|')"
info "RT calendar left anywhere? activities: [$(adb shell dumpsys activity activities 2>/dev/null | grep -i 'calendar' | head -3 | sed 's/^ *//' | cut -c1-100 | tr '\n' '|')] helper processes: [$(adb shell ps -A 2>/dev/null | grep -E 'ShellServer' | tr -s ' ' | cut -d' ' -f2,9- | cut -c1-70 | tr '\n' '|')] features file: [$(adb shell "run-as $PKG cat shared_prefs/tj_features.xml" 2>/dev/null | grep -oE 'name=\"[a-z_]+\"( value=\"[a-z]+\")?|>[A-Za-z ]+<' | tr -d '\r' | tr '\n' ' ' | cut -c1-150)]"
adb shell "run-as $PKG cat files/lock-events.log" 2>/dev/null | tail -60 > "$OUT/rt-lock-events.txt"
info "RT lock log, lines naming a reconcile or a dirty mark: $(grep -E 'RECONCILE|DIRTY' $OUT/rt-lock-events.txt | cut -c1-120 | head -4 | tr '\n' '|')"
sed -n '/HANDSHAKE/,$p' "$OUT/rt-lock-events.txt" | cut -c1-80 > "$OUT/rt-lock-after.txt"
info "RT lock log from the handshake on (1-6): $(sed -n 1,6p $OUT/rt-lock-after.txt | tr '\n' '|')"
info "RT lock log from the handshake on (7-12): $(sed -n 7,12p $OUT/rt-lock-after.txt | tr '\n' '|')"
info "RT appop GET_USAGE_STATS: $(adb shell appops get $PKG GET_USAGE_STATS 2>&1 | tr -d '\r' | head -1 | cut -c1-80); battery: $(adb shell dumpsys battery | grep -E 'AC powered|USB powered|level' | tr -s ' ' | tr '\n' ',' | cut -c1-120)"
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
imfeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string><boolean name=\\\"profile_hide_status_bar\\\" value=\\\"$1\\\" /></map>\" > shared_prefs/tj_features.xml'"; }
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
rawfeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string><boolean name=\\\"raw_key_log\\\" value=\\\"true\\\" /></map>\" > shared_prefs/tj_features.xml'"; }
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
T_PAD0=$(date -u +%T); lockpad; T_PAD1=$(date -u +%T); pad d d 1 2 3 4 e; T_PAD2=$(date -u +%T); sleep 7        # the default handshake -> Quickstep
getmode > "$OUT/pf-mode-a.txt"
check "PF-A0: a Quickstep session began" grep -q quickstep "$OUT/pf-mode-a.txt"
info "PF A in front right after the handshake: $(resumed | cut -c1-200)"
adb shell settings put secure sysui_qs_tiles "wifi,bt,rotation,flashlight,airplane" >/dev/null 2>&1   # Tj arranges Quickstep's tiles with the pencil: here, written directly
T_QN=$(date -u +%T); adb shell monkey -p $QN -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 3; adb shell input keyevent KEYCODE_HOME; T_HOME=$(date -u +%T); sleep 1
info "PF A in Quickstep: notifications [$(pf_notifs)]"
check "PF-A1: the Quickstep app posted its notification (so the test means something)" pf_has $QN
adb shell "run-as $PKG cat shared_prefs/tj_origin.xml" 2>/dev/null | tr '>' '\n' | grep -E "set name|string|tracked" | tr '\n' ' ' > "$OUT/pf-origin-pre.txt"
info "PF A tj_origin.xml BEFORE the lock (pending = marked Quickstep-opened): $(cut -c1-400 $OUT/pf-origin-pre.txt)"
adb logcat -c
T_LOCK=$(date -u +%T); adb shell input keyevent KEYCODE_POWER; sleep 10                      # LOCK: the session ends; the clean-up runs behind the lock screen
info "PF A timeline (UTC, same clock as the lock log): lockpad began $T_PAD0, PIN pad up $T_PAD1, Enter typed by $T_PAD2, Quickstep app opened $T_QN, Home pressed $T_HOME, lock pressed $T_LOCK"
adb logcat -d -s TjReset:I TjA11y:I TjTasks:I > "$OUT/pf-log-a.txt" 2>&1
info "PF A after the lock: notifications [$(pf_notifs)]; the clean-up said: $(grep 'AT LOCK' $OUT/pf-log-a.txt | tail -1 | cut -c1-420)"
adb shell "run-as $PKG cat files/lock-events.log" 2>/dev/null | tail -45 > "$OUT/pf-lock-events.txt"; adb shell "run-as $PKG cat shared_prefs/tj_origin.xml" 2>/dev/null | tr '>' '\n' | grep -E "set name|string|tracked" | tr '\n' ' ' > "$OUT/pf-origin.txt"
info "PF A lock log DIRTY lines: $(grep DIRTY $OUT/pf-lock-events.txt | cut -c1-135 | head -3 | tr '\n' '|')"
sed -n '/HANDSHAKE/,$p' "$OUT/pf-lock-events.txt" > "$OUT/pf-lock-after.txt"
info "PF A lock log after the handshake (1-5): $(sed -n 1,5p $OUT/pf-lock-after.txt | cut -c1-84 | tr '\n' '|')"
info "PF A lock log after the handshake (6-10): $(sed -n 6,10p $OUT/pf-lock-after.txt | cut -c1-84 | tr '\n' '|')"
info "PF A lock log after the handshake (11-15): $(sed -n 11,15p $OUT/pf-lock-after.txt | cut -c1-84 | tr '\n' '|')"
info "PF A tj_origin.xml (after the lock): $(cut -c1-200 $OUT/pf-origin.txt)"
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
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /><string name=\\\"return_app_label\\\">Calendar</string><boolean name=\\\"profile_hide_status_bar\\\" value=\\\"true\\\" /><boolean name=\\\"keep_alive\\\" value=\\\"true\\\" /></map>\" > shared_prefs/tj_features.xml'"
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
adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"power_card\\\" value=\\\"true\\\" /><string name=\\\"power_mode\\\">hide_screenshot</string><boolean name=\\\"guard_stock_home\\\" value=\\\"false\\\" /><boolean name=\\\"startup_open\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_on_quickstep\\\" value=\\\"false\\\" /></map>\" > shared_prefs/tj_features.xml'"; adb shell am force-stop "$PKG"; sleep 1
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
_sectime END   # the last section's time
echo "failed checks: $FAILS" | tee -a "$OUT/results.txt"
ls -la "$OUT"
exit "$FAILS"
