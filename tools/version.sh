#!/usr/bin/env bash
# Computes the version for a build. Sourced by build.sh and by CI.
#
# Source of truth: android:versionName in AndroidManifest.xml, which always holds the
# NEXT TARGET RELEASE (X.Y.Z, no suffix). Everything else is derived from it.
#
#   RELEASE=1  -> X.Y.Z          versionCode = formula(P=999)
#   otherwise  -> X.Y.Z-dev.N    versionCode = formula(P=N)
#                N = commits since the last v* tag
#
# Exports: VERSION_BASE VERSION_NAME VERSION_CODE DEV_COUNT
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MANIFEST="$ROOT/AndroidManifest.xml"

# DEV_BASE_VERSION lets a dev build label itself with the version semantic-release would
# publish next (see tools/next-version.sh), instead of the one already released.
VERSION_BASE="${DEV_BASE_VERSION:-$(sed -n 's/.*android:versionName="\([^"]*\)".*/\1/p' "$MANIFEST")}"
[ -n "$VERSION_BASE" ] || { echo "version.sh: no android:versionName in $MANIFEST" >&2; exit 1; }

case "$VERSION_BASE" in
  [0-9]*.[0-9]*.[0-9]*) ;;
  *) echo "version.sh: versionName '$VERSION_BASE' is not X.Y.Z (the manifest must hold the next target release, without a suffix)" >&2; exit 1 ;;
esac

IFS=. read -r MAJOR MINOR PATCH <<<"$VERSION_BASE"

if [ "${RELEASE:-0}" = "1" ]; then
  # A release tag must match the manifest, or the tag is lying about what it contains.
  # RELEASE_TAG lets a reusable-workflow caller name the tag; on a tag push the runner
  # supplies GITHUB_REF_NAME.
  TAG="${RELEASE_TAG:-${GITHUB_REF_NAME:-}}"
  if [ -n "$TAG" ] && [ "$TAG" != "v$VERSION_BASE" ]; then
    echo "version.sh: tag '$TAG' does not match manifest version 'v$VERSION_BASE'" >&2
    exit 1
  fi
  DEV_COUNT=0
  P=999
  VERSION_NAME="$VERSION_BASE"
else
  LAST_TAG="$(git -C "$ROOT" describe --tags --abbrev=0 --match 'v*' 2>/dev/null || true)"
  if [ -n "$LAST_TAG" ]; then
    DEV_COUNT="$(git -C "$ROOT" rev-list --count "$LAST_TAG..HEAD")"
  else
    DEV_COUNT="$(git -C "$ROOT" rev-list --count HEAD)"
  fi
  # 0 would collide with "no build yet"; a dev build is always at least .1
  [ "$DEV_COUNT" -ge 1 ] || DEV_COUNT=1
  if [ "$DEV_COUNT" -gt 998 ]; then
    echo "version.sh: $DEV_COUNT commits since $LAST_TAG exceeds the 998 dev builds per cycle. Cut a release." >&2
    exit 1
  fi
  P="$DEV_COUNT"
  VERSION_NAME="$VERSION_BASE-dev.$DEV_COUNT"
fi

# versionCode = MAJOR*10_000_000 + MINOR*100_000 + PATCH*1_000 + P
# P=999 for a release, so a release always outranks every prerelease that preceded it.
VERSION_CODE=$(( MAJOR * 10000000 + MINOR * 100000 + PATCH * 1000 + P ))

export VERSION_BASE VERSION_NAME VERSION_CODE DEV_COUNT
