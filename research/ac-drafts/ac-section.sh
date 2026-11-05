if want ac; then # section ac
ensurehome
# ---- AC1-AC9 (Tj, 2026-11-05 ~22:17; v0.7.3): the Solitaire switch gates every automatic open, the lock-screen rules do not depend on a wiped switch, a wrong PIN after the gesture keeps the gesture,
#      the handshake puts Quickstep behind the lock at once whatever the pre-arm switch says, battery saver is switched off in the custom launcher the moment it is on, the decision log exists and holds no digit.
beat "before AC"
adb shell pm grant "$PKG" android.permission.WRITE_SECURE_SETTINGS >/dev/null 2>&1
# acfeat <prearm true|false> <startup_open true|false> <battery_saver_off true|false>: the usual test features (Calendar stands in for Solitaire) + the OLD end-on-lock switch stored as FALSE (the breaker's wipe): it must have no effect any more
acfeat(){ adb shell "run-as $PKG sh -c 'mkdir -p shared_prefs; printf \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" standalone=\\\"yes\\\"?><map><boolean name=\\\"startup_open\\\" value=\\\"$2\\\" /><boolean name=\\\"profile_prearm\\\" value=\\\"$1\\\" /><boolean name=\\\"profile_end_on_lock\\\" value=\\\"false\\\" /><boolean name=\\\"profile_battery_saver_off\\\" value=\\\"$3\\\" /><boolean name=\\\"unlock_m_taperase\\\" value=\\\"true\\\" /><string name=\\\"return_app_label\\\">Calendar</string></map>\" > shared_prefs/tj_features.xml'"; }
lowpower(){ adb shell settings get global low_power | tr -d '\r\n'; }

# ---- AC3: battery saver. Make the test deterministic: not charging, 60 %
adb shell dumpsys battery unplug >/dev/null 2>&1; adb shell dumpsys battery set level 60 >/dev/null 2>&1
acfeat true false true; setmode quickstep; rebind; home; sleep 2
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
check "AC3: turned on again while the custom launcher is active, it is switched off again within 2 s (the observer, no polling)" bash -c "[ \"$AC3L\" = 0 ]"
setmode custom; adb shell settings put global low_power 1; rebind; sleep 4
AC3P="$(lowpower)"
check "AC3: a launcher process that STARTS while battery saver is on switches it off (prepared at process start)" bash -c "[ \"$AC3P\" = 0 ]"
acfeat true false false; setmode custom; rebind; sleep 2
adb shell settings put global low_power 1; sleep 3
AC3S="$(lowpower)"
check "AC3: with its switch OFF the guard leaves battery saver alone (control)" bash -c "[ \"$AC3S\" = 1 ]"
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
check "AC1: and the custom launcher (not Quickstep) is on screen" grep -q "com.tj.launcher" "$OUT/ac1-resumed.txt"

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

# ---- AC1 / AC7 evidence: the persistent decision log exists, says what happened, and holds no digit of the PIN
adb shell "run-as $PKG cat files/lock-events.log" > "$OUT/ac-lock-events.txt" 2>&1
info "AC log: $(wc -l < $OUT/ac-lock-events.txt | tr -d ' ') lines; last: $(tail -2 $OUT/ac-lock-events.txt | cut -c1-120 | tr '\n' '|')"
check "AC-B5: the lock-screen decision log exists and records the unlocks" grep -q "UNLOCK" "$OUT/ac-lock-events.txt"
check "AC-B5: it holds no digit of the PIN (1234 / 4444 never appear)" bash -c "! grep -qE '1234|4444' $OUT/ac-lock-events.txt"
adb shell locksettings clear --old 1234 > "$OUT/ac-clearpin.txt" 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1; sleep 1
setfeat; setmode custom; adb shell am force-stop "$PKG"; ensurehome; rebind; home
fi # section ac
