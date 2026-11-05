#!/usr/bin/env bash
# test_uinode.sh — self-test of tools/uinode.py, the node finder the emulator scenario's phone-guide checks use (runs inside tools/test_handoff.sh; ~1 s, no emulator).
# Why it exists (2026-11-05, emulator runs 117/118): a text-grep for "the first Done box in the dump" ticked the wrong card, so two phone-guide checks failed
# for a reason that was not in the app. Each such mistake costs a ~4 minute CI run, so the finder is pinned here on a dump shaped like uiautomator's.
#   bash tools/test_uinode.sh        # exit 0 = all green
set -uo pipefail
D="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
PASS=0; FAIL=0
ok(){ PASS=$((PASS+1)); }
bad(){ FAIL=$((FAIL+1)); echo "  FAIL  $1"; }
check(){ if eval "$2" >/dev/null 2>&1; then ok; else bad "$1"; fi; }
U(){ python3 -I "$D/tools/uinode.py" "$@"; }

# a dump shaped like the phone guide's: the button, then two cards whose title and Done box share a row (card 1 not ticked, card 2 ticked)
cat > "$T/dump.xml" <<'XML'
<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
<hierarchy rotation="0">
  <node index="0" text="" class="android.widget.FrameLayout" bounds="[0,0][600,1024]">
    <node index="0" text="Tablet adb guide" class="android.widget.TextView" bounds="[10,100][590,150]" />
    <node index="1" text="Take me to my next step" class="android.widget.Button" bounds="[10,300][590,360]" />
    <node index="2" text="PART 1: BEFORE the cable" class="android.widget.TextView" bounds="[10,400][590,450]" />
    <node index="3" text="★ 1.  Install the newest launcher" class="android.widget.TextView" bounds="[10,500][400,540]" />
    <node index="4" text="Done" class="android.widget.CheckBox" checked="false" bounds="[420,490][590,550]" />
    <node index="5" text="2.  Allow USB debugging" class="android.widget.TextView" bounds="[10,700][400,740]" />
    <node index="6" text="Done" class="android.widget.CheckBox" checked="true" bounds="[420,690][590,750]" />
    <node index="7" text="Say &quot;Done&quot; when finished" class="android.widget.TextView" bounds="[10,800][590,840]" />
  </node>
</hierarchy>
XML

check "a node is found by part of its text and its bounds are printed as 'x1 y1 x2 y2'" '[ "$(U "$T/dump.xml" --contains "Take me to")" = "10 300 590 360" ]'
check "--exact matches the whole text only (a longer text that contains Done is not a Done box)" '[ "$(U "$T/dump.xml" --contains Done --exact --nth 3)" = "" ] && [ "$(U "$T/dump.xml" --contains Done --nth 3)" = "10 800 590 840" ]'
check "--nth picks the second match"                     '[ "$(U "$T/dump.xml" --contains Done --exact --nth 2)" = "420 690 590 750" ]'
check "--row-of finds the Done box in the row of card 1's title (not the first one in the file by luck)" '[ "$(U "$T/dump.xml" --contains Done --exact --row-of "1.  Install")" = "420 490 590 550" ]'
check "--row-of finds the Done box in the row of card 2's title" '[ "$(U "$T/dump.xml" --contains Done --exact --row-of "2.  Allow")" = "420 690 590 750" ]'
check "--attr checked reads card 1's box as false"       '[ "$(U "$T/dump.xml" --contains Done --exact --row-of "1.  Install" --attr checked)" = "false" ]'
check "--attr checked reads card 2's box as true"        '[ "$(U "$T/dump.xml" --contains Done --exact --row-of "2.  Allow" --attr checked)" = "true" ]'
check "the star in a title is read (UTF-8)"              '[ "$(U "$T/dump.xml" --contains "★ 1.")" = "10 500 400 540" ]'
check "no match prints nothing and exits 0 (the caller scrolls and looks again)" 'out=$(U "$T/dump.xml" --contains "no such text"); rc=$?; [ -z "$out" ] && [ "$rc" = 0 ]'
check "an anchor that is not on screen prints nothing and exits 0" 'out=$(U "$T/dump.xml" --contains Done --exact --row-of "9.  Not here"); rc=$?; [ -z "$out" ] && [ "$rc" = 0 ]'
check "a Done box with no title row at the same height is not matched" 'out=$(U "$T/dump.xml" --contains Done --exact --row-of "PART 1"); [ -z "$out" ]'
check "an unreadable file exits 1"                       'U "$T/missing.xml" --contains x; [ $? = 1 ]'
check "a file that is not XML exits 1"                   'echo "not xml" > "$T/bad.xml"; U "$T/bad.xml" --contains x; [ $? = 1 ]'
check "an unknown option exits 2"                        'U "$T/dump.xml" --bogus; [ $? = 2 ]'
# the dumper may print a status line after the document, and a half-scrolled node is reported with the clipped bounds it has
{ cat "$T/dump.xml"; echo "UI hierchary dumped to: /sdcard/gui.xml"; } > "$T/tail.xml"
check "text printed after </hierarchy> is ignored"       '[ "$(U "$T/tail.xml" --contains "Take me to")" = "10 300 590 360" ]'
# the scenario uses it to tap the centre of what it found
check "the centre of a box is computed from the printed bounds the way the scenario does" 'set -- $(U "$T/dump.xml" --contains Done --exact --row-of "1.  Install"); [ $(( ($1+$3)/2 )) = 505 ] && [ $(( ($2+$4)/2 )) = 520 ]'
# the scenario really calls it (so this file cannot stay green while the checks go back to blind swipes)
check "the emulator scenario's phone-guide tick check uses uinode.py" 'grep -q "tools/uinode.py" "$D/tools/emulator_scenario.sh" && grep -q -- "--row-of" "$D/tools/emulator_scenario.sh"'

echo "uinode self-test: $PASS passed, $FAIL failed"
[ "$FAIL" -eq 0 ]
