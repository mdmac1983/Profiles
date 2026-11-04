#!/usr/bin/env bash
# adb-probe.sh — READ-ONLY facts about Tj's tablet, so the launcher can be planned against the real device (power menu, restarts, Settings clone, profiles).
#   bash tools/adb-probe.sh            # writes probe-out/launcher-probe.txt  (device facts + settings NAMES, no values of private settings)
#   bash tools/adb-probe.sh --pull     # also copies the tablet's own Settings app (the APK) into probe-out/ (about 20-60 MB) for the Settings clone
# It changes NOTHING on the tablet. It prints no accounts, no serial number, no Wi-Fi names, no settings VALUES except the few marked below.
# Windows without bash: every command below is a plain `adb shell ...` line, you can paste them one by one (they are also listed in research/ADB_COMMANDS.txt section 12).
set -u
OUT=probe-out; mkdir -p "$OUT"; F="$OUT/launcher-probe.txt"; : > "$F"
adb get-state >/dev/null 2>&1 || { echo "No device found: USB debugging on, tablet plugged in, Allow tapped?"; exit 1; }
sec(){ { echo; echo "===== $1"; } >> "$F"; }
run(){ { echo "\$ adb shell $*"; adb shell "$@" 2>&1 | tr -d '\r'; } >> "$F"; }

sec "build"
for p in ro.build.fingerprint ro.build.version.release ro.build.version.sdk ro.build.version.security_patch ro.build.type ro.product.model ro.product.manufacturer ro.lineage.version ro.modversion ro.build.display.id; do run getprop $p; done
sec "boot / encryption (decides whether the helper can run before the first unlock after a restart)"
for p in ro.crypto.state ro.crypto.type ro.boot.verifiedbootstate ro.boot.flash.locked ro.boot.veritymode; do run getprop $p; done
sec "users (decides whether a real second Android user is possible)"
run pm get-max-users
run pm list users
run settings get global user_switcher_enabled
sec "adb over network (decides whether the launcher could start its own helper after every restart, no PC)"
run getprop service.adb.tcp.port
run getprop persist.adb.tcp.port
run settings get global adb_enabled
run "settings list global | grep -i adb | cut -d= -f1"
sec "setting NAMES that could matter (names only)"
run "settings list secure | cut -d= -f1 | grep -iE 'power|qs|tile|zen|policy|nav|gesture|screenshot|lock|menu|assist' | sort"
run "settings list global | cut -d= -f1 | grep -iE 'power|zen|policy|nav|gesture|screenshot|reboot|restart|menu|assist' | sort"
run "settings list system | cut -d= -f1 | grep -iE 'power|menu|screenshot|nav|gesture' | sort"
sec "lineage / vendor power-menu settings (values, no private data)"
run "content query --uri content://lineagesettings/secure 2>&1 | grep -iE 'power_menu' "
run "settings get secure power_menu_actions"
sec "overlays that could be switched on/off (names)"
run "cmd overlay list | head -60"
sec "the Settings app (for the cloned Settings)"
run pm path com.android.settings
run "dumpsys package com.android.settings | grep -E 'versionName|versionCode|sharedUser|codePath|flags=|targetSdk' | head -12"
run "pm list packages -s | grep -iE 'settings|setup|durasp|launcher|systemui|quickstep'"
run "dumpsys package com.android.settings | grep -oE 'android\\.settings\\.[A-Z_0-9]+' | sort -u"
sec "launcher / home apps"
run "cmd package resolve-activity -a android.intent.action.MAIN -c android.intent.category.HOME | head -8"
run pm path com.android.launcher3
sec "done"
echo "wrote $F"
if [ "${1:-}" = "--pull" ]; then
  for pkg in com.android.settings com.android.systemui; do
    for apk in $(adb shell pm path $pkg | tr -d '\r' | sed 's/^package://'); do adb pull "$apk" "$OUT/${pkg}_$(basename "$apk")" >/dev/null 2>&1 && echo "pulled $apk"; done
  done
fi
echo "Send me probe-out/launcher-probe.txt (paste it). If you used --pull, the .apk files are in probe-out/ too."
