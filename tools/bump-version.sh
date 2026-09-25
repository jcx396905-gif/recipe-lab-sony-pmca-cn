#!/usr/bin/env bash
# Opens a new version cycle: rewrites AndroidManifest.xml to the given release version.
#
# The manifest always holds the NEXT TARGET RELEASE. Dev builds derive X.Y.Z-dev.N from it
# at build time without touching the file, so this is the only place a version is ever typed.
#
# Usage: tools/bump-version.sh 1.1.0
set -euo pipefail

NEW="${1:-}"
case "$NEW" in
  [0-9]*.[0-9]*.[0-9]*) ;;
  *) echo "usage: $0 <x.y.z>" >&2; exit 1 ;;
esac

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MANIFEST="$ROOT/AndroidManifest.xml"

IFS=. read -r MAJOR MINOR PATCH <<<"$NEW"
CODE=$(( MAJOR * 10000000 + MINOR * 100000 + PATCH * 1000 + 999 ))

OLD="$(sed -n 's/.*android:versionName="\([^"]*\)".*/\1/p' "$MANIFEST")"
OLD_CODE="$(sed -n 's/.*android:versionCode="\([^"]*\)".*/\1/p' "$MANIFEST")"

# Equal is fine: semantic-release calls this with the version the manifest may already
# hold. Going backwards is not.
if [ "$CODE" -lt "$OLD_CODE" ]; then
  echo "bump-version.sh: $NEW (code $CODE) is behind $OLD (code $OLD_CODE). versionCode must never go backwards." >&2
  exit 1
fi

sed -i "s/android:versionCode=\"[^\"]*\"/android:versionCode=\"$CODE\"/" "$MANIFEST"
sed -i "s/android:versionName=\"[^\"]*\"/android:versionName=\"$NEW\"/" "$MANIFEST"

echo "$OLD ($OLD_CODE) -> $NEW ($CODE)"
git -C "$ROOT" --no-pager diff -- AndroidManifest.xml
echo
echo "Not committed. Commit as:  chore(release): $NEW"
