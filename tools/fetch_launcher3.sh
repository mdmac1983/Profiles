#!/usr/bin/env bash
# fetch_launcher3.sh — download the PINNED AOSP sources the port is built from, into an isolated dir.
# Reproducible: a new container re-runs this instead of anyone carrying 12 MB around.
#   bash tools/fetch_launcher3.sh [dest]      (default dest: /tmp/launcher3-src, outside the repo)
# Pin: android-10.0.0_r47 (the last Android 10 tag). The stock device runs Quickstep v10 from the Alps BSP,
# which is this code plus vendor patches (RESEARCH in research/DESIGN_2026-11-02.md).
set -euo pipefail
DEST="${1:-/tmp/launcher3-src}"; TAG="android-10.0.0_r47"
BASE="https://android.googlesource.com/platform"
mkdir -p "$DEST/Launcher3" "$DEST/SystemUI-plugin"
curl -fsS -m 300 -L "$BASE/packages/apps/Launcher3/+archive/refs/tags/$TAG.tar.gz" | tar xz -C "$DEST/Launcher3"
curl -fsS -m 300 -L "$BASE/frameworks/base/+archive/refs/tags/$TAG/packages/SystemUI/plugin.tar.gz" | tar xz -C "$DEST/SystemUI-plugin"
echo "Launcher3 + SystemUI plugin API @ $TAG -> $DEST ($(du -sh "$DEST" | cut -f1))"
