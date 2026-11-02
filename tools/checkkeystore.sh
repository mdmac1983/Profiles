#!/usr/bin/env bash
# checkkeystore.sh — is app/sideload.jks present, and is it the RIGHT one?
#
# WHY THE FINGERPRINT AND NOT JUST THE FILE
# -----------------------------------------
# A missing keystore is the harmless case (expected, pre-first-release): the
# build fails immediately and says so. The dangerous case is a WRONG one — a
# different key builds and installs perfectly and only fails to update the
# phone in place, which Android reports at install time, long after the build
# looked green. An in-place update is the only thing that preserves whatever
# is already on the phone once anything ships (see BRIEF.md, "the signing
# keystore"), so the certificate is checked, not just the file's existence.
# A regenerated key with an IDENTICAL DN is still caught — verified below by
# tools/test_resume.sh, which generates two different keys under the same DN
# and confirms the second is rejected.
#
# WHY A PINNED FILE RATHER THAN A HARDCODED LITERAL
# ---------------------------------------------------
# Launcher's keystore does not exist yet (see BRIEF.md), so there is nothing to
# hardcode: the expected fingerprint instead lives in tools/keystore.fingerprint,
# a small COMMITTED file (unlike app/sideload.jks itself, which is gitignored)
# written once by tools/init-keystore.sh the moment the keystore is created.
# Before that file exists, this script can only report "no keystore yet" —
# which is the correct, honest answer at the foundation stage.
#
#   bash tools/checkkeystore.sh           # prints one status line
#   bash tools/checkkeystore.sh --quiet   # silent when correct
#
# exit 0 = correct (or present but unverifiable, or legitimately not-yet-created),
# 1 = missing (keystore not created, or fingerprint not yet pinned), 2 = WRONG
set -uo pipefail
D="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"; cd "$D" || exit 1

FP_FILE="tools/keystore.fingerprint"

QUIET=0
for a in "$@"; do
  case "$a" in
    --quiet) QUIET=1 ;;
    # Lets tools/verify-apk.sh and CI check an APK against the same value
    # without copying it. A second copy of this string is a second thing that
    # can be edited, and it is the one string in the project that must never
    # change once it's pinned.
    --expected)
      [ -f "$FP_FILE" ] && { cat "$FP_FILE"; exit 0; }
      exit 1
      ;;
  esac
done
say_ok(){ [ "$QUIET" -eq 1 ] || echo "$1"; }

if [ ! -f app/sideload.jks ]; then
  if [ -f "$FP_FILE" ]; then
    echo "  !!    a keystore WAS pinned (tools/keystore.fingerprint exists) but"
    echo "        app/sideload.jks is missing from this container. If anything has"
    echo "        ever been installed with it, do NOT run tools/init-keystore.sh to"
    echo "        make a new one — that erases whatever is on the phone. Get the real"
    echo "        file from Tj (or restore it from wherever it was backed up) instead."
    exit 1
  fi
  echo "  note  no signing keystore yet — expected pre-first-release (see BRIEF.md)."
  echo "        app/sideload.jks is not in git and is not auto-generated. When ready"
  echo "        to build a real APK for the first time, run tools/init-keystore.sh —"
  echo "        that is a deliberate, one-time, irreversible decision, not routine work."
  exit 1
fi

if [ ! -f "$FP_FILE" ]; then
  echo "  WARN  app/sideload.jks exists but tools/keystore.fingerprint does not — the"
  echo "        certificate was never pinned. If tools/init-keystore.sh created this"
  echo "        file, it should have written the fingerprint too; if this file arrived"
  echo "        some other way, run: keytool -list -keystore app/sideload.jks and"
  echo "        commit its SHA-256 to tools/keystore.fingerprint by hand before shipping."
  exit 1
fi
EXPECTED="$(cat "$FP_FILE" 2>/dev/null | tr -d ' \r\n')"

if ! command -v keytool >/dev/null 2>&1; then
  say_ok "  OK    signing keystore present (no keytool here to verify its fingerprint)"
  exit 0
fi

GOT="$(keytool -list -keystore app/sideload.jks -storepass launcher 2>/dev/null \
       | sed -n 's/.*SHA-256): *//p' | tr -d ' \r' | head -1)"

if [ "$GOT" = "$EXPECTED" ]; then
  say_ok "  OK    signing keystore present and matches the pinned certificate"
  exit 0
elif [ -z "$GOT" ]; then
  echo "  WARN  app/sideload.jks is present but unreadable with the documented"
  echo "        password — treat it as the wrong file; do NOT ship with it."
  exit 2
else
  echo "  !!    app/sideload.jks is the WRONG KEYSTORE. Signing with it produces"
  echo "        an APK that CANNOT update the phone in place — installing it over"
  echo "        anything already there erases that data. Get the right file; never"
  echo "        generate a replacement once something has shipped."
  echo "          expected $EXPECTED"
  echo "          got      $GOT"
  exit 2
fi
