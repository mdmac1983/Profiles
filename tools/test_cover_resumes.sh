#!/usr/bin/env bash
# Offline test of cover_resumes (the scenario's real function text, a stubbed adb): bash tools/test_cover_resumes.sh [scenario file]. Pins the BK1 gate-triage logic of UF2 / CS-B5 (2026-11-08).
set -u
SCEN="${1:-tools/emulator_scenario.sh}"
OUT="$(mktemp -d)"; export OUT
START=$(grep -n '^cover_resumes(){' "$SCEN" | cut -d: -f1)
END=$(awk -v s="$START" 'NR>s && /^}/ {print NR; exit}' "$SCEN")
sed -n "${START},${END}p" "$SCEN" > "$OUT/fn.sh"
# the stub: "adb logcat ..." prints the synthetic cover lines kept in $OUT/fake-logcat.txt
adb(){ if [ "$1" = logcat ]; then cat "$OUT/fake-logcat.txt"; fi; }
. "$OUT/fn.sh"

RE=' (com.android.launcher3/|com.android.contacts/)'
fails=0
t(){ # t <name> <expected: empty|hit> <cover lines> <events>
  local name="$1" exp="$2" cover="$3" ev="$4" got
  printf '%s' "$cover" > "$OUT/fake-logcat.txt"
  printf '%s' "$ev" > "$OUT/ev.txt"
  got="$(cover_resumes "$OUT/ev.txt" "$RE")"
  if [ "$exp" = empty ] && [ -z "$got" ]; then echo "ok    $name"
  elif [ "$exp" = hit ] && [ -n "$got" ]; then echo "ok    $name -> flagged: $(echo "$got" | tr '\n' '|')"
  else echo "FAIL  $name (expected $exp, got: [$got])"; fails=$((fails+1)); fi
}
C1=$'11-08 10:00:01.000 I/TjA11y( 100): RESET cover shown\n11-08 10:00:03.000 I/TjA11y( 100): RESET cover removed\n'
CUS=$'11-08 10:00:00.900 com.tj.launcher/com.android.launcher3.Launcher\n'
t "stock HOME inside the cover's life"            empty "$C1" "${CUS}11-08 10:00:02.000 com.android.launcher3/.Launcher"$'\n'"11-08 10:00:03.100 com.tj.launcher/com.android.launcher3.Launcher"$'\n'
t "stock HOME 500 ms BEFORE the cover"            hit   "$C1" "11-08 10:00:00.500 com.android.launcher3/.Launcher"$'\n'
t "stock HOME 500 ms AFTER the cover is gone"     hit   "$C1" "11-08 10:00:03.500 com.android.launcher3/.Launcher"$'\n'
t "Contacts resumed outside the cover"            hit   "$C1" "11-08 10:00:05.000 com.android.contacts/.activities.PeopleActivity"$'\n'
t "no cover at all, stock HOME resumes"           hit   ""    "11-08 10:00:02.000 com.android.launcher3/.Launcher"$'\n'
t "no cover, only ours resumes"                   empty ""    "${CUS}"
t "20 ms before 'shown' is within the slack"      empty "$C1" "11-08 10:00:00.980 com.android.launcher3/.Launcher"$'\n'
t "60 ms before 'shown' is outside the slack"     hit   "$C1" "11-08 10:00:00.940 com.android.launcher3/.Launcher"$'\n'
t "cover never removed: everything after 'shown'" empty $'11-08 10:00:01.000 I/TjA11y( 100): RESET cover shown\n' "11-08 10:00:09.000 com.android.launcher3/.Launcher"$'\n'
C2=$'11-08 09:00:01.000 I/TjA11y( 100): RESET cover shown\n11-08 09:00:02.000 I/TjA11y( 100): RESET cover removed\n11-08 10:00:01.000 I/TjA11y( 100): RESET cover shown\n11-08 10:00:03.000 I/TjA11y( 100): RESET cover removed\n'
t "an OLD cover must not cover a new resume"      hit   "$C2" "11-08 09:30:00.000 com.android.launcher3/.Launcher"$'\n'
t "the LAST cover covers its own resume"          empty "$C2" "11-08 10:00:02.500 com.android.launcher3/.Launcher"$'\n'
echo "failures: $fails"; exit $fails
