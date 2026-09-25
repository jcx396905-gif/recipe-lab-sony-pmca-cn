#!/usr/bin/env bash
# Prints the version semantic-release would publish next, e.g. 1.2.0
#
# Falls back to the manifest version when nothing releasable has landed since the last
# tag, which is the common case early in a cycle. Used by dev-build so an in-progress APK
# is labelled with the version it is heading towards rather than the one already shipped.
#
# Needs: npm ci, and GITHUB_TOKEN in the environment.
set -uo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

OUT="$(npx semantic-release --dry-run --no-ci --branches "$(git rev-parse --abbrev-ref HEAD)" 2>&1)"
NEXT="$(printf '%s' "$OUT" | sed -n 's/.*next release version is \([0-9][0-9.]*\).*/\1/p' | tail -1)"

if [ -z "$NEXT" ]; then
  NEXT="$(sed -n 's/.*android:versionName="\([^"]*\)".*/\1/p' AndroidManifest.xml)"
  echo "next-version.sh: semantic-release reports no release; using the manifest version $NEXT" >&2
fi
printf '%s\n' "$NEXT"
