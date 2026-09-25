#!/usr/bin/env bash
# Unit tests: the CI gate `test`, and how to run them locally.
#
# Compiles the camera-free classes (Recipes, Params) against a plain JDK -- no SDK, no NDK, no android.jar --
# then everything under test/, and runs it with the JUnit console launcher. The launcher is one jar, fetched
# from Maven Central into out/test/ on first use and checked against the SHA-256 pinned below.
#
#   tools/test.sh              run everything
#   tools/test.sh Recipes      only test classes whose name contains "Recipes"
#
# Toolchain: JAVA_HOME pointing at a JDK 17 (as for build.sh), or javac on PATH.
# JUNIT_JAR=<path> uses an already downloaded launcher instead (offline).
set -euo pipefail
cd "$(dirname "$0")/.."

JUNIT_VERSION=1.14.4
JUNIT_SHA256=7c6968cbcaf4301c729f202b23b7d736c5d88be625fc7d27ad5d746146a8bc28
JUNIT_URL="https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/$JUNIT_VERSION/junit-platform-console-standalone-$JUNIT_VERSION.jar"
JUNIT_JAR="${JUNIT_JAR:-out/test/junit-platform-console-standalone-$JUNIT_VERSION.jar}"

if [ -n "${JAVA_HOME:-}" ]; then JAVA="$JAVA_HOME/bin"; else JAVA="$(dirname "$(command -v javac 2>/dev/null || echo /nonexistent/javac)")"; fi
[ -x "$JAVA/javac" ] || { echo "test.sh: no javac -- set JAVA_HOME to a JDK 17" >&2; exit 1; }

# The classes under test, by name on purpose. They are compiled without android.jar, so anything in them that
# reaches for android.* fails right here. New camera-free logic goes into one of these (or a new file listed here)
# with a test next to it; MainActivity and the views stay out because they cannot run off the camera.
UNITS=(
  src/com/voxivoid/recipelab/Recipes.java
  src/com/voxivoid/recipelab/Params.java
  src/com/voxivoid/recipelab/Favourites.java
  src/com/voxivoid/recipelab/DevTools.java
)

mkdir -p out/test
if [ ! -e "$JUNIT_JAR" ]; then
  echo "fetching junit-platform-console-standalone $JUNIT_VERSION"
  curl -sSfL --retry 3 -o "$JUNIT_JAR.part" "$JUNIT_URL"
  mv -f "$JUNIT_JAR.part" "$JUNIT_JAR"
fi
if ! printf '%s  %s\n' "$JUNIT_SHA256" "$JUNIT_JAR" | sha256sum -c --quiet -; then
  rm -f "$JUNIT_JAR"
  echo "test.sh: $JUNIT_JAR does not match the pinned SHA-256; removed it" >&2
  exit 1
fi

rm -rf out/test/classes out/test/test-classes out/test/reports
mkdir -p out/test/classes out/test/test-classes

echo "[1/3] javac units (same flags as build.sh, no android.jar)"
"$JAVA/javac" -encoding UTF-8 --release 8 -Xlint:-options -d out/test/classes "${UNITS[@]}"

echo "[2/3] javac tests"
find test -name '*.java' | sort > out/test/sources.txt
"$JAVA/javac" -encoding UTF-8 --release 8 -Xlint:-options -cp "out/test/classes:$JUNIT_JAR" -d out/test/test-classes "@out/test/sources.txt"

echo "[3/3] junit"
FILTER=()
[ -n "${1:-}" ] && FILTER=(--include-classname ".*$1.*")
"$JAVA/java" -jar "$JUNIT_JAR" execute \
  --class-path "out/test/classes:out/test/test-classes" --scan-class-path \
  --reports-dir out/test/reports --details=tree --disable-banner --fail-if-no-tests "${FILTER[@]}"
