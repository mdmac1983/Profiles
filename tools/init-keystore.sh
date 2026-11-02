#!/usr/bin/env bash
# init-keystore.sh — create Launcher's signing keystore. A ONE-TIME, DELIBERATE step.
#
# WHY THIS SCRIPT EXISTS AND WHY IT IS NOT RUN AUTOMATICALLY
# ------------------------------------------------------------
# Launcher has no keystore yet — this project is at the foundation stage, and
# nothing has ever been built or installed.
#
# Generating a keystore is SAFE right up until the moment something signed
# with it is installed on a real phone — before that, it's just a file. But
# it is still not something a foundation session should do silently, unasked,
# as a side effect of "setting things up": it's the one artifact in this
# whole project that becomes permanently, irreversibly load-bearing the
# instant it's used for real, and its passwords/alias/DN are a real decision,
# not a formality. So this script exists, ready to run, and BRIEF.md says
# plainly: run it deliberately, when actually about to build something
# installable for the first time — not before.
#
# WHAT IT DOES
# ------------
# 1. Refuses if app/sideload.jks already exists (never silently overwrite).
# 2. Generates a 2048-bit RSA keypair, 27-year validity (long enough that
#    expiry is never the reason this has to be regenerated), alias "launcher".
# 3. Pins its SHA-256 fingerprint into tools/keystore.fingerprint — THIS file
#    (unlike the .jks itself) gets committed to git, so tools/checkkeystore.sh
#    has something durable to verify every future build against, on any
#    account, in any container, forever after.
# 4. Prints the base64 needed for the GitHub Actions secret
#    (SIGNING_KEYSTORE_BASE64 — see .github/workflows/android.yml) and a loud
#    reminder to back the actual keystore file up out of band, NOW, because
#    this container will not survive the session.
#
#   bash tools/init-keystore.sh
#
set -uo pipefail
D="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"; cd "$D" || exit 1

KS="app/sideload.jks"
FP_FILE="tools/keystore.fingerprint"

if [ -f "$KS" ]; then
  echo "  FAIL  $KS already exists — refusing to overwrite it."
  echo "        If anything signed with it has EVER been installed anywhere, a new"
  echo "        keystore is exactly the failure BRIEF.md warns about: Android forces"
  echo "        an uninstall first, erasing whatever data is on that phone. Read"
  echo "        BRIEF.md's keystore section before doing anything else here."
  exit 1
fi

if ! command -v keytool >/dev/null 2>&1; then
  echo "  FAIL  no keytool on PATH — cannot generate a keystore in this container."
  exit 1
fi

mkdir -p app
if ! keytool -genkeypair -v \
    -keystore "$KS" -alias launcher -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass launcher -keypass launcher \
    -dname "CN=Launcher, OU=Personal, O=TJ" >/tmp/init-keystore.log 2>&1; then
  echo "  FAIL  keytool did not succeed:"
  tail -20 /tmp/init-keystore.log | sed 's/^/        /'
  exit 1
fi

FP="$(keytool -list -keystore "$KS" -storepass launcher 2>/dev/null \
      | sed -n 's/.*SHA-256): *//p' | tr -d ' \r' | head -1)"
if [ -z "$FP" ]; then
  echo "  FAIL  keystore was created but its fingerprint could not be read back."
  exit 1
fi
printf '%s\n' "$FP" > "$FP_FILE"

echo "  OK    created $KS"
echo "        alias: launcher   password: launcher (store and key, both)"
echo "        SHA-256: $FP"
echo "        pinned into $FP_FILE — commit this file (tools/checkkeystore.sh and"
echo "        tools/verify-apk.sh now enforce every future build against it)"
echo
echo "  ============================================================================"
echo "  BACK UP app/sideload.jks NOW, OUT OF BAND — this container will not survive"
echo "  the session, and once anything signed with this key is installed anywhere,"
echo "  losing the file means NOTHING can ever be updated over it again. Save it to"
echo "  Tj's password manager or personal cloud storage before doing anything else."
echo "  ============================================================================"
echo
echo "  Also needed as a GitHub Actions secret before CI can sign a build"
echo "  (Settings -> Secrets and variables -> Actions -> SIGNING_KEYSTORE_BASE64):"
echo
echo "    base64 -w0 $KS"
echo
echo "  (that command intentionally prints nothing here — run it yourself when"
echo "  you're ready to paste the result into GitHub, so the key material never"
echo "  sits in a chat transcript or a log)"
