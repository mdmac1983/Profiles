#!/usr/bin/env bash
# test_handoff.sh — hermetic self-test of the handoff/checkpoint system itself (~seconds).
# Runs as a fast check inside ckpt.sh, as a ship.sh gate, and as the first gate of both
# workflows. It builds a throwaway clone and never touches the real settings file or remote.
#   bash tools/test_handoff.sh        # exit 0 = all green
set -uo pipefail
D="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
PASS=0; FAIL=0
ok(){ PASS=$((PASS+1)); }
bad(){ FAIL=$((FAIL+1)); echo "  FAIL  $1"; }
check(){ if eval "$2" >/dev/null 2>&1; then ok; else bad "$1"; fi; }

# ---------------------------------------------------------------- workflow minute rules
for W in "$D"/.github/workflows/*.yml; do
  # No `push:` trigger that is not tags-only (autosave pushes after every tool call).
  if python3 -I - "$W" <<'PY'
import sys, re
t = open(sys.argv[1]).read()
m = re.search(r'^on:\n((?:[ \t]+.*\n|\n)+)', t, re.M)
block = m.group(1) if m else ''
bad = False
lines = block.splitlines()
for i, l in enumerate(lines):
    if re.match(r'^  push:\s*$', l):
        nxt = lines[i+1] if i+1 < len(lines) else ''
        if 'branches' in nxt or 'paths' in nxt or not nxt.strip().startswith('tags'):
            bad = True
    if re.match(r'^  push:\s*\{', l):
        bad = True
sys.exit(1 if bad else 0)
PY
  then ok; else bad "$(basename "$W") has a non-tag push trigger (would burn Actions minutes)"; fi
done

# ---------------------------------------------------------------- fixture repo + fake remote
git init -q --bare "$T/remote.git"
git clone -q "$T/remote.git" "$T/work" 2>/dev/null
mkdir -p "$T/work/tools" "$T/work/app"
cp "$D"/tools/*.sh "$D"/tools/session-root-hooks.json "$D"/tools/MIRROR_MAIN "$T/work/tools/" 2>/dev/null
echo off > "$T/work/tools/MIRROR_MAIN"
cp "$D"/ship.sh "$T/work/" 2>/dev/null
cd "$T/work" || exit 1
rm -f tools/test_handoff.sh   # the fixture must not recurse into this test via ckpt.sh
git config user.email t@t; git config user.name t
cat > app/build.gradle.kts <<'G'
        versionCode = 1
        versionName = "0.1.0"
G
printf '# BUILDLOG\n' > BUILDLOG.md
printf '# TASKS\n- [ ] first task\n' > TASKS.md
git checkout -q -b sess1
export CLAUDE_HOOK_SETTINGS="$T/settings.json" CLAUDE_REPO_ROOT="$T"   # never the real file

# ---------------------------------------------------------------- hooks installer
echo '{"permissions":{"allow":["Bash"]},"hooks":{"Stop":[{"hooks":[{"type":"command","command":"echo other # novig-checkpoint-hooks"}]}]}}' > "$CLAUDE_HOOK_SETTINGS"
check "install-hooks reports MISSING before install" '! bash tools/install-hooks.sh --check'
check "install-hooks installs"                      'bash tools/install-hooks.sh --quiet && bash tools/install-hooks.sh --check'
check "install-hooks is idempotent (no change)"     'B=$(cat "$CLAUDE_HOOK_SETTINGS"); bash tools/install-hooks.sh --quiet; [ "$B" = "$(cat "$CLAUDE_HOOK_SETTINGS")" ]'
check "install-hooks keeps permissions + novig hook" 'python3 -I -c "
import json,os
d=json.load(open(os.environ[\"CLAUDE_HOOK_SETTINGS\"]))
assert d[\"permissions\"][\"allow\"]==[\"Bash\"]
assert any(\"novig-checkpoint-hooks\" in h[\"command\"] for e in d[\"hooks\"][\"Stop\"] for h in e[\"hooks\"])
assert any(\"launcher-checkpoint-hooks\" in h[\"command\"] for e in d[\"hooks\"][\"Stop\"] for h in e[\"hooks\"])
"'
check "hooks point only at this repo's tools/" 'python3 -I -c "
import json,os
d=json.load(open(os.environ[\"CLAUDE_HOOK_SETTINGS\"]))
cmds=[h[\"command\"] for ev in d[\"hooks\"].values() for e in ev for h in e[\"hooks\"] if \"launcher-checkpoint-hooks\" in h[\"command\"]]
assert cmds and all(\"$PWD/tools/\" in c and \"*\" not in c for c in cmds), cmds
"'

# ---------------------------------------------------------------- secret scan
printf '%s\n' "token=ghp_$(printf 'a%.0s' $(seq 1 36))" > leak.txt
git add -A >/dev/null 2>&1
check "secretscan blocks a GitHub token" '! bash tools/secretscan.sh'
git reset -q; rm -f leak.txt

# ---------------------------------------------------------------- autosave + push
echo hi > a.txt
check "autosave commits and pushes"  'bash tools/autosave.sh --text >/dev/null; git log -1 --format=%s | grep -q "^auto-checkpoint:" && [ "$(bash tools/unpushed.sh)" = 0 ]'
check "branch reached the remote"    'git ls-remote --heads origin sess1 | grep -q sess1'
check "main is NOT touched while MIRROR_MAIN=off" '! git ls-remote --heads origin main | grep -q main'
printf 'ghp_%s\n' "$(printf 'b%.0s' $(seq 1 36))" > leak2.txt
check "autosave refuses a credential"  'bash tools/autosave.sh --text | grep -q "AUTOSAVE BLOCKED"; ! git log --format=%s | head -1 | grep -q leak; ! git ls-tree -r HEAD --name-only | grep -q leak2'
rm -f leak2.txt

# ---------------------------------------------------------------- ckpt numbering + CHECKPOINT.md
bash tools/ckpt.sh "did one" "do two" >/dev/null 2>&1
N1="$(sed -n '1s/^# CHECKPOINT \([0-9]*\).*/\1/p' CHECKPOINT.md)"
check "ckpt writes CHECKPOINT.md with did/next" '[ -n "$N1" ] && grep -q "did one" CHECKPOINT.md && grep -q "do two" CHECKPOINT.md'
bash tools/ckpt.sh "did two" "do three" >/dev/null 2>&1
N2="$(sed -n '1s/^# CHECKPOINT \([0-9]*\).*/\1/p' CHECKPOINT.md)"
check "ckpt numbers only go up"  '[ "${N2:-0}" -gt "${N1:-99}" ]'
check "ckpt pushed everything"   '[ "$(bash tools/unpushed.sh)" = 0 ]'

# ---------------------------------------------------------------- resume: briefing, interruption, stranded branch
OUT="$(bash tools/resume.sh --text 2>&1)"
check "resume prints CHECKPOINT + open task" 'printf "%s" "$OUT" | grep -q "do three" && printf "%s" "$OUT" | grep -q "first task"'
check "resume reports a clean state (no interruption warning)" '! printf "%s" "$OUT" | grep -q "INTERRUPTED MID-CHANGE"'
echo wip > b.txt; bash tools/autosave.sh --text >/dev/null
OUT="$(bash tools/resume.sh --text 2>&1)"
check "resume flags an interrupted session"  'printf "%s" "$OUT" | grep -q "INTERRUPTED MID-CHANGE"'
bash tools/ckpt.sh "finished wip" "next" >/dev/null 2>&1
git checkout -q -b sess2 2>/dev/null; echo more > c.txt; bash tools/ckpt.sh "other session work" "continue" >/dev/null 2>&1
git checkout -q sess1 2>/dev/null
OUT="$(bash tools/resume.sh --text 2>&1)"
check "resume names another branch's unlanded work" 'printf "%s" "$OUT" | grep -q "sess2 has"'
check "resume briefing fits the 10,000-char hook cap (with a long INBOX + TASKS)" '
  for i in $(seq 1 80); do echo "- [ ] open task number $i with a reasonably long description of what must be done" >> TASKS.md; done
  head -c 6000 /dev/zero | tr "\0" "x" >> INBOX.md
  [ "$(bash tools/resume.sh --text | wc -c)" -lt 10000 ]'

# ---------------------------------------------------------------- the phone guide list has ONE source (the app's asset); the research copy must be identical
check "phone-guide steps.txt equals research/ADB_PHONE_COMMANDS.txt (edit the asset, then cp it)" 'cmp -s "$D/phoneguide/src/main/assets/steps.txt" "$D/research/ADB_PHONE_COMMANDS.txt"'
check "no phone-guide command starts with adb (the OTG shell takes the bare command)" '! grep -E "^(cmd|alt):[[:space:]]*adb( |$)" "$D/phoneguide/src/main/assets/steps.txt"'
check "no phone-guide command is destructive" '! grep -E "^(cmd|alt):.*(pm uninstall|pm clear|wipe|factory|rm -|reboot|setprop|locksettings|pm hide|pm suspend|svc wifi|svc data|settings put global (airplane|adb_enabled|development))" "$D/phoneguide/src/main/assets/steps.txt"'

# ---------------------------------------------------------------- inbox capture
printf '{"prompt":"please build X, my key is ghp_%s ok"}' "$(printf 'c%.0s' $(seq 1 36))" | bash tools/capture_inbox.sh
check "inbox captures the message, redacts the key, and commits it" 'grep -q "please build X" INBOX.md && grep -q "REDACTED-KEY" INBOX.md && ! grep -q "ghp_c" INBOX.md && [ "$(bash tools/unpushed.sh)" = 0 ]'

# ---------------------------------------------------------------- ship gate: version must increase
echo '| v0.1.0 | code 1 | 2026-01-01T00:00Z | first' >> BUILDLOG.md
SHIPOUT="$(bash ship.sh "x" 2>&1)"
check "ship refuses a non-increased versionCode" 'printf "%s" "$SHIPOUT" | grep -q "not higher" && ! printf "%s" "$SHIPOUT" | grep -q "ready for GitHub"'

echo "handoff self-test: $PASS passed, $FAIL failed"
[ "$FAIL" -eq 0 ]
