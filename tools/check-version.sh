#!/usr/bin/env bash
# CI gate `version-consistency`.
#
# 1. AndroidManifest.xml holds a bare X.Y.Z (no -dev suffix committed, ever).
# 2. versionCode == formula(versionName, P=999).
# 3. No version string has crept back into README.md or MainActivity.java — the manifest
#    is the single source of truth and mirrors are how drift starts.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
FAIL=0

NAME="$(sed -n 's/.*android:versionName="\([^"]*\)".*/\1/p' AndroidManifest.xml)"
CODE="$(sed -n 's/.*android:versionCode="\([^"]*\)".*/\1/p' AndroidManifest.xml)"

case "$NAME" in
  [0-9]*.[0-9]*.[0-9]*)
    case "$NAME" in
      *-*|*+*) echo "FAIL: committed versionName '$NAME' carries a suffix; the manifest holds the target release only" >&2; FAIL=1 ;;
    esac
    ;;
  *) echo "FAIL: versionName '$NAME' is not X.Y.Z" >&2; FAIL=1 ;;
esac

if [ "$FAIL" -eq 0 ]; then
  IFS=. read -r MAJOR MINOR PATCH <<<"$NAME"
  WANT=$(( MAJOR * 10000000 + MINOR * 100000 + PATCH * 1000 + 999 ))
  if [ "$CODE" != "$WANT" ]; then
    echo "FAIL: versionCode $CODE != $WANT (formula for $NAME with P=999). Run tools/bump-version.sh $NAME" >&2
    FAIL=1
  fi
fi

# The manifest must never fall behind the last published release. Once semantic-release
# owns the version this catches a hand-edit or a release commit that failed to land;
# ahead is legal, because the manifest leads the next release until the tag exists.
LAST_TAG="$(git describe --tags --abbrev=0 --match 'v*' 2>/dev/null || true)"
if [ "$FAIL" -eq 0 ] && [ -n "$LAST_TAG" ]; then
  TAG_NAME="${LAST_TAG#v}"
  case "$TAG_NAME" in
    [0-9]*.[0-9]*.[0-9]*)
      IFS=. read -r TM TN TP <<<"$TAG_NAME"
      TAG_CODE=$(( TM * 10000000 + TN * 100000 + TP * 1000 + 999 ))
      if [ "$CODE" -lt "$TAG_CODE" ]; then
        echo "FAIL: manifest $NAME ($CODE) is behind the last release $LAST_TAG ($TAG_CODE)" >&2
        FAIL=1
      fi
      ;;
  esac
fi

# A bare X.Y.Z anywhere in these files means someone reintroduced a mirror.
for f in README.md src/com/voxivoid/recipelab/MainActivity.java; do
  if grep -nE '(Version|Recipe Lab) [0-9]+\.[0-9]+\.[0-9]+' "$f" >/dev/null 2>&1; then
    echo "FAIL: hard-coded version string in $f — the manifest is the only source of truth:" >&2
    grep -nE '(Version|Recipe Lab) [0-9]+\.[0-9]+\.[0-9]+' "$f" >&2
    FAIL=1
  fi
done

[ "$FAIL" -eq 0 ] && echo "version-consistency OK: $NAME ($CODE)"
exit "$FAIL"
