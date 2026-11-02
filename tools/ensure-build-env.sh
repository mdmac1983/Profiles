#!/usr/bin/env bash
# ensure-build-env.sh — make this container able to build an APK, automatically.
#
# WHY THIS EXISTS
# ---------------
# Every session runs in a FRESH container: the repo is cloned, but the Android
# SDK is not in it (2-4 GB, and not something to keep in git). So the first
# build in any container used to fail with "no Android SDK — run
# tools/setup-android-sdk.sh", and a human had to notice and run a second
# command. That is a manual step standing between "Claude is ready to build"
# and a build, in a project whose whole point is surviving handoffs without
# manual steps.
#
# So: the build entry points call this, and it provisions whatever is missing.
# Idempotent and near-instant once ready (~0.3s), so calling it every time
# costs nothing.
#
# WHAT IT CANNOT DO
# -----------------
# The signing keystore. Before the first release, app/sideload.jks simply
# does not exist yet (see BRIEF.md) — tools/init-keystore.sh creates it, but
# only when deliberately run, not from inside this script. After a keystore
# exists, it is deliberately not in git and this script cannot conjure one
# from nothing either way — generating a REPLACEMENT once anything has
# shipped is the one thing that must never happen (BRIEF.md: it forces an
# uninstall and erases whatever's on the phone). So this reports the state
# clearly and, for a release, refuses. That refusal is the point, not a gap.
#
#   bash tools/ensure-build-env.sh            # provision what is missing
#   bash tools/ensure-build-env.sh --release  # also REQUIRE a correct keystore
#   bash tools/ensure-build-env.sh --check    # report only, provision nothing
#   bash tools/ensure-build-env.sh --quiet    # speak only when acting/failing
#
# exit 0 = ready to build, 1 = not ready (reason printed)
#
# Fully generic — all keystore specifics are delegated to checkkeystore.sh.
set -uo pipefail
D="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"; cd "$D" || exit 1

RELEASE=0; CHECK=0; QUIET=0
for a in "$@"; do
  case "$a" in
    --release) RELEASE=1 ;;
    --check)   CHECK=1 ;;
    --quiet)   QUIET=1 ;;
  esac
done
note(){ [ "$QUIET" -eq 1 ] || echo "$1"; }

# Find the SDK: an existing install wins (/opt/android-sdk on the web containers), else the
# per-user default that tools/setup-android-sdk.sh creates.
if [ -z "${ANDROID_HOME:-}" ]; then
  for c in /opt/android-sdk /root/android-sdk "$HOME/android-sdk"; do
    [ -d "$c/platforms" ] && { ANDROID_HOME="$c"; break; }
  done
  ANDROID_HOME="${ANDROID_HOME:-/root/android-sdk}"
fi
export ANDROID_HOME

# ---- java: cannot be provisioned here, and nothing works without it ----------
if ! command -v java >/dev/null 2>&1; then
  echo "  FAIL  no java on PATH — gradlew cannot run. This container is broken;"
  echo "        start a fresh session rather than trying to install a JDK."
  exit 1
fi

# ---- the Android SDK: THIS is the part that provisions itself ----------------
SDK_OK=0
[ -d "$ANDROID_HOME/platforms/android-36" ] && SDK_OK=1

if [ "$SDK_OK" -eq 0 ]; then
  if [ "$CHECK" -eq 1 ]; then
    echo "  ..    android sdk not installed yet (would be installed on first build)"
    exit 1
  fi
  echo "  ..    no Android SDK in this container — installing it now (~5 min, once)."
  echo "        This is automatic; nothing to do. Downloading from dl.google.com..."
  if bash tools/setup-android-sdk.sh; then
    if [ -d "$ANDROID_HOME/platforms/android-36" ]; then
      echo "  OK    Android SDK installed at $ANDROID_HOME"
    else
      echo "  FAIL  setup-android-sdk.sh reported success but android-36 is missing."
      exit 1
    fi
  else
    # A cold pull can genuinely fail on a network hiccup or a Google/Maven 429
    # — see BRIEF.md's build traps. Say which it is rather than letting the
    # caller read it as a code problem.
    echo "  FAIL  could not install the Android SDK. This is usually the network"
    echo "        (a cold dl.google.com pull, or a 429) and not a code problem —"
    echo "        see BRIEF.md's build traps. Re-run this to retry:"
    echo "          bash tools/ensure-build-env.sh"
    exit 1
  fi
else
  note "  OK    android sdk present at $ANDROID_HOME"
fi

# ---- local.properties: how Gradle actually finds the SDK ---------------------
# Rewritten when it points somewhere that no longer exists — a stale path
# copied from another container fails in a way that reads like a Gradle bug.
if [ ! -f local.properties ] || ! grep -q "^sdk.dir=$ANDROID_HOME$" local.properties 2>/dev/null; then
  echo "sdk.dir=$ANDROID_HOME" > local.properties
  note "  OK    local.properties points at $ANDROID_HOME"
fi

# ---- the signing keystore ----------------------------------------------------
# Advisory for tests and debug work, FATAL for a release: an unsigned or
# wrongly-signed release APK is worse than no APK, because it installs.
# NOT ${QUIET:+--quiet} — QUIET is "0" or "1", and "0" is a NON-EMPTY string,
# so that form passes --quiet always and silently swallowed the keystore line
# on every normal run. Test for the value.
KSARGS=""; [ "$QUIET" -eq 1 ] && KSARGS="--quiet"
bash tools/checkkeystore.sh $KSARGS >/tmp/ks-check.$$ 2>&1; KS=$?
if [ "$KS" -eq 0 ]; then
  [ "$QUIET" -eq 1 ] || cat /tmp/ks-check.$$
elif [ "$RELEASE" -eq 1 ]; then
  cat /tmp/ks-check.$$
  echo "  FAIL  cannot build a RELEASE without the correct signing keystore."
  rm -f /tmp/ks-check.$$
  exit 1
else
  cat /tmp/ks-check.$$
  note "  ..    proceeding anyway — tests and non-release work do not need it."
fi
rm -f /tmp/ks-check.$$

note "  OK    build environment ready"
exit 0
