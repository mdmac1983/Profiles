#!/usr/bin/env bash
# test_tablet_intake.sh — self-test of tools/tablet_intake.py (runs inside tools/test_handoff.sh; ~2 s, no network, writes only to a temp dir).
# Pins the rules that keep a tablet report SAFE to commit and USEFUL to read:
#   private strings are redacted, a credential makes the intake refuse, a zip is never extracted (zip-slip names are ignored), sizes are capped,
#   system apps / screenshots are listed but not copied, every red flag the report can show is raised, and nothing is written outside --out.
#   bash tools/test_tablet_intake.sh        # exit 0 = all green
set -uo pipefail
D="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
PASS=0; FAIL=0
ok(){ PASS=$((PASS+1)); }
bad(){ FAIL=$((FAIL+1)); echo "  FAIL  $1"; }
check(){ if eval "$2" >/dev/null 2>&1; then ok; else bad "$1"; fi; }
SAMPLE="$D/tools/testdata/tablet-sample/tablet-report.txt"
INTAKE="bash $D/tools/tablet-intake.sh"
PRIVATE='R52N90ABCDE|192\.168\.1\.77|aa:bb:cc:dd:ee:ff|a@example\.com|356938035643809|HomeNet-5G'

check "the sample report exists and holds the planted private strings" '[ "$(grep -cE "$PRIVATE" "$SAMPLE")" -ge 3 ]'

# ---------------------------------------------------------------- a folder (the PC route / the Downloads folder): text kept + redacted, apk and png listed or dropped
mkdir -p "$T/in/shots" "$T/in/apk"
cp "$SAMPLE" "$T/in/tablet-report.txt"
echo "== adb-all 2026-11-05  tablet: Acme Tab10" > "$T/in/status.txt"
head -c 4000 /dev/urandom > "$T/in/apk/com.android.settings.apk"
printf '\x89PNG\r\n\x1a\n' > "$T/in/shots/SETTINGS.png"; head -c 2000 /dev/urandom >> "$T/in/shots/SETTINGS.png"
echo '<hierarchy><node text="Wi-Fi" /></hierarchy>' > "$T/in/shots/SETTINGS.xml"
ln -s /etc/passwd "$T/in/passwd-link.txt"
SUM="$(sha256sum "$T/in/apk/com.android.settings.apk" | cut -d' ' -f1)"
OUT1="$($INTAKE "$T/in" --out "$T/out" --date 2026-11-05 --label folder 2>&1)"; RC1=$?
check "folder: exit 0"                                   '[ "$RC1" = 0 ]'
check "folder: FACTS.md, INTAKE.txt, report, status, LATEST.md exist" '[ -f "$T/out/2026-11-05/FACTS.md" ] && [ -f "$T/out/2026-11-05/INTAKE.txt" ] && [ -f "$T/out/2026-11-05/tablet-report.txt" ] && [ -f "$T/out/2026-11-05/status.txt" ] && [ -f "$T/out/LATEST.md" ]'
check "folder: NO planted private string survives anywhere in the output" '! grep -rE "$PRIVATE" "$T/out"'
check "folder: the redaction markers are there (and the facts are still readable)" 'grep -q "<email>" "$T/out/2026-11-05/tablet-report.txt" && grep -q "<ip>" "$T/out/2026-11-05/tablet-report.txt" && grep -q "<mac>" "$T/out/2026-11-05/tablet-report.txt" && grep -q "Acme Tab10" "$T/out/2026-11-05/FACTS.md"'
check "folder: the apk is listed with its sha256 but NOT copied" 'grep -q "$SUM" "$T/out/2026-11-05/apk-list.txt" && [ -z "$(find "$T/out" -name "*.apk")" ]'
check "folder: the png is NOT copied by default"           '[ -z "$(find "$T/out" -name "*.png")" ]'
check "folder: the screen-structure xml is kept"           '[ -f "$T/out/2026-11-05/shots-SETTINGS.xml" ]'
check "folder: a symlink inside the folder is not followed" '! grep -rq "root:" "$T/out"'
check "folder: a clean sample shows no red flag"           'grep -q "none found by the automatic checks" "$T/out/2026-11-05/FACTS.md"'
check "folder: the table answers the open questions"       'grep -q "Side-key test (Power menu v2) | the side key WAS seen" "$T/out/2026-11-05/FACTS.md" && grep -q "Recents format check | the launcher.s reader parsed 2" "$T/out/2026-11-05/FACTS.md" && grep -q "Encryption" "$T/out/2026-11-05/FACTS.md"'
check "folder: LATEST.md is the newest FACTS.md"           'grep -q "Latest tablet intake: 2026-11-05" "$T/out/LATEST.md" && grep -q "Red flags" "$T/out/LATEST.md"'
OUT2="$($INTAKE "$T/in" --out "$T/out" --date 2026-11-05 2>&1)"
check "a second intake on the same day gets its own folder (nothing is overwritten)" '[ -d "$T/out/2026-11-05-2" ] && [ -f "$T/out/2026-11-05/FACTS.md" ] && grep -q "2026-11-05-2" "$T/out/LATEST.md"'

# ---------------------------------------------------------------- --keep-images keeps ONLY a small png
head -c 400000 /dev/urandom > "$T/in/shots/BIG.png"
$INTAKE "$T/in" --out "$T/out2" --date 2026-11-06 --keep-images >/dev/null 2>&1
check "--keep-images keeps the small png and drops the big one" '[ -f "$T/out2/2026-11-06/shots-SETTINGS.png" ] && [ ! -f "$T/out2/2026-11-06/shots-BIG.png" ]'

# ---------------------------------------------------------------- a ZIP: never extracted; zip-slip and absolute names ignored; the real report still read
python3 -I - "$T" "$SAMPLE" <<'PY'
import sys, zipfile
t, sample = sys.argv[1], sys.argv[2]
with zipfile.ZipFile(t + "/in.zip", "w") as z:
    z.write(sample, "launcher-probe/tablet-report.txt")
    z.writestr("../evil.txt", "owned")
    z.writestr("/abs-evil.txt", "owned")
    z.writestr("launcher-probe/../../evil2.txt", "owned")
    z.writestr("launcher-probe/apk/x.apk", b"APKDATA" * 100)
PY
$INTAKE "$T/in.zip" --out "$T/outz/sub" --date 2026-11-05 >/dev/null 2>&1; RCZ=$?
check "zip: exit 0 and the report was read"             '[ "$RCZ" = 0 ] && [ -f "$T/outz/sub/2026-11-05/FACTS.md" ]'
check "zip: nothing was written outside --out (zip-slip)" '[ ! -e "$T/outz/evil.txt" ] && [ ! -e "$T/evil.txt" ] && [ ! -e "$T/outz/evil2.txt" ] && [ ! -e /abs-evil.txt ] && [ "$(find "$T" -name "*evil*" | wc -l)" = 0 ]'
check "zip: unsafe member names are listed as ignored"   'grep -q "unsafe path" "$T/outz/sub/2026-11-05/INTAKE.txt"'
check "zip: nothing from the zip was extracted to disk"  '[ -z "$(find "$T/outz" -name "*.apk")" ] && grep -q "x.apk" "$T/outz/sub/2026-11-05/apk-list.txt"'

# ---------------------------------------------------------------- a single file, and the report text on standard input (Tj pasted it into the chat)
$INTAKE "$SAMPLE" --out "$T/outf" --date 2026-11-05 >/dev/null 2>&1
check "a single file works"                              '[ -f "$T/outf/2026-11-05/FACTS.md" ]'
$INTAKE - --out "$T/outs" --date 2026-11-05 < "$SAMPLE" >/dev/null 2>&1
check "standard input works and says where it came from" '[ -f "$T/outs/2026-11-05/FACTS.md" ] && grep -q "pasted text" "$T/outs/2026-11-05/FACTS.md" && ! grep -rE "$PRIVATE" "$T/outs"'

# ---------------------------------------------------------------- a credential: refuse the WHOLE intake, save nothing
mkdir -p "$T/cred"
{ cat "$SAMPLE"; printf 'leaked: ghp_%s\n' "$(printf 'k%.0s' $(seq 1 36))"; } > "$T/cred/tablet-report.txt"
OUTC="$($INTAKE "$T/cred" --out "$T/outc" --date 2026-11-05 2>&1)"; RCC=$?
check "credential: refused (exit 4) and says so"         '[ "$RCC" = 4 ] && printf "%s" "$OUTC" | grep -q REFUSED'
check "credential: NOTHING was saved"                    '[ -z "$(find "$T/outc" -type f 2>/dev/null)" ]'

# ---------------------------------------------------------------- caps and wrong input
mkdir -p "$T/big"; head -c 13000000 /dev/zero > "$T/big/tablet-report.txt"
OUTB="$($INTAKE "$T/big" --out "$T/outb" --date 2026-11-05 2>&1)"; RCB=$?
check "a report over the size cap is not read (exit 3, nothing saved)" '[ "$RCB" = 3 ] && [ -z "$(find "$T/outb" -type f 2>/dev/null)" ]'
OUTN="$($INTAKE "$T/does-not-exist" --out "$T/outn" 2>&1)"; RCN=$?
check "a missing input is reported (exit 2)"             '[ "$RCN" = 2 ] && printf "%s" "$OUTN" | grep -q "not found"'
mkdir -p "$T/empty"; echo hi > "$T/empty/readme.txt"
$INTAKE "$T/empty" --out "$T/oute" >/dev/null 2>&1; RCE=$?
check "a folder with no report is reported (exit 3)"     '[ "$RCE" = 3 ]'

# ---------------------------------------------------------------- every red flag the report can show is raised (one bad tablet, all at once)
mkdir -p "$T/badin"
python3 -I - "$SAMPLE" "$T/badin/tablet-report.txt" <<'PY'
import sys
s = open(sys.argv[1]).read()
def rep(a, b):
    global s
    assert a in s, a
    s = s.replace(a, b)
rep("shell helper reachable: yes", "shell helper reachable: no")
rep("accessibility helper bound: yes", "accessibility helper bound: no")
rep("crash breaker tripped: no", "crash breaker tripped: yes")
rep("retail-demo flag (must be 0 or not set): (not set)", "retail-demo flag (must be 0 or not set): 1")
rep("DUMP: granted", "DUMP: NOT granted")
rep("android: 10 (API 29)", "android: 11 (API 30)")
rep("\tUserInfo{0:Owner:13} running", "\tUserInfo{0:Owner:13} running\n\tUserInfo{10:Guest:4} running")
rep("Recents tasks parsed: 2", "Recents tasks parsed: 0")
rep("Apps with a notification now (as the clean-up sees them): [com.android.systemui, com.whatsapp]", "Apps with a notification now (as the clean-up sees them): (none)")
rep("keytest_summary: the side key WAS seen (held 1.5 s); the shell MAY switch the normal power menu off", "keytest_summary: the side key was NOT seen (no KEY_POWER event in 9 s); the shell was REFUSED when it tried to switch the normal power menu off")
rep("keytest_menu: not_shown", "keytest_menu: shown")
rep("$ logcat -b crash -d -v time | grep -A12 'com.tj.launcher' | head -60\n", "$ logcat -b crash -d -v time | grep -A12 'com.tj.launcher' | head -60\nFATAL EXCEPTION: main\n  Process: com.tj.launcher, PID: 4242\n")
open(sys.argv[2], "w").write(s)
PY
$INTAKE "$T/badin" --out "$T/outbad" --date 2026-11-05 >/dev/null 2>&1
F="$T/outbad/2026-11-05/FACTS.md"
check "red flag: helper not running"                      'grep -q "shell helper was NOT running" "$F"'
check "red flag: accessibility helper not bound"          'grep -q "accessibility helper is not bound" "$F"'
check "red flag: crash breaker tripped"                   'grep -q "crash breaker TRIPPED" "$F"'
check "red flag: retail-demo flag"                        'grep -q "device_demo_mode = 1" "$F"'
check "red flag: a permission is missing"                 'grep -q "Permissions NOT granted: DUMP" "$F"'
check "red flag: not Android 10"                          'grep -q "Android is not 10" "$F"'
check "red flag: more than one Android user"              'grep -q "2 Android users" "$F"'
check "red flag: Recents reader mismatch"                 'grep -q "PARSER MISMATCH: the Recents sample" "$F"'
check "red flag: notification reader mismatch"            'grep -q "PARSER MISMATCH: the notification sample" "$F"'
check "red flag: side key not seen"                       'grep -q "side key was NOT seen" "$F"'
check "red flag: shell refused to switch the menu off"    'grep -q "did not get a clean OK" "$F"'
check "red flag: normal menu still appeared"              'grep -q "NORMAL power menu still appeared" "$F"'
check "red flag: a crash log line"                        'grep -q "crash log has" "$F"'
check "the red flags come BEFORE the table"               '[ "$(grep -n "## Red flags" "$F" | cut -d: -f1)" -lt "$(grep -n "## Answers" "$F" | cut -d: -f1)" ]'

# ---------------------------------------------------------------- the guide tells Tj to send the file this tool reads
check "the phone guide names tablet-report.txt as the file to send" 'grep -q "tablet-report.txt" "$D/phoneguide/src/main/assets/steps.txt"'
check "research/tablet/README.md explains the flow"      '[ -f "$D/research/tablet/README.md" ] && grep -q "tablet-intake.sh" "$D/research/tablet/README.md"'

echo "tablet intake self-test: $PASS passed, $FAIL failed"
[ "$FAIL" -eq 0 ]
