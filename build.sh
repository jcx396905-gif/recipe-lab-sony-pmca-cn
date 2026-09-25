#!/usr/bin/env bash
# Linux/WSL/macOS build — the port of build.cmd, used by CI and for local builds outside Windows.
# Keep the two in step: build.cmd is the Windows path and the two must produce the same APK.
#
#   ./build.sh              -> RecipeLab.apk at X.Y.Z-dev.N
#   RELEASE=1 ./build.sh    -> RecipeLab.apk at X.Y.Z (tag must match the manifest)
#
# Toolchain (override via environment):
#   JAVA_HOME  ANDROID_SDK  ANDROID_NDK  BUILD_TOOLS  PLATFORM_JAR
# Signing: set ANDROID_KEYSTORE_B64 + ANDROID_KEYSTORE_PASSWORD + ANDROID_KEY_ALIAS +
# ANDROID_KEY_PASSWORD to sign with the real key; otherwise a throwaway key is generated
# (the camera accepts any self-signed v1 key, but an APK signed with a different key cannot
# be installed *over* an existing one).
set -euo pipefail
cd "$(dirname "$0")"
ROOT="$PWD"

# ---- toolchain locations ----
: "${ANDROID_SDK:=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Android/Sdk}}}"
: "${ANDROID_NDK:?set ANDROID_NDK to an android-ndk-r16b installation}"
: "${BUILD_TOOLS:=30.0.3}"
: "${PLATFORM_JAR:=$ANDROID_SDK/platforms/android-28/android.jar}"
BT="$ANDROID_SDK/build-tools/$BUILD_TOOLS"
if [ -n "${JAVA_HOME:-}" ]; then JAVA="$JAVA_HOME/bin"; else JAVA="$(dirname "$(command -v javac)")"; fi
# apksigner and keytool are shell wrappers that exec `java` from PATH, so JAVA_HOME on its
# own is not enough. CI does not hit this because setup-java also puts java on PATH.
export PATH="$JAVA:$PATH"
AJ="$PLATFORM_JAR"

for f in "$AJ" "$BT/aapt" "$BT/zipalign" "$BT/apksigner" "$BT/lib/d8.jar" "$ANDROID_NDK/ndk-build"; do
  [ -e "$f" ] || { echo "missing: $f" >&2; exit 1; }
done

# The platform's errno.h shim is for the libc-less updater build; it shadows the NDK's <errno.h>.
# Park it for the duration of the build — and always put it back, even if the build dies, or the
# submodule is left permanently dirty.
restore_errno() { [ -e jni/platform/errno.h.updater_only ] && mv -f jni/platform/errno.h.updater_only jni/platform/errno.h || true; }
trap restore_errno EXIT
[ -e jni/platform/errno.h ] && mv -f jni/platform/errno.h jni/platform/errno.h.updater_only

# ---- version (single source of truth: AndroidManifest.xml, never mutated by a build) ----
# shellcheck source=tools/version.sh
. tools/version.sh
echo "building $VERSION_NAME (versionCode $VERSION_CODE)"

echo "[0/7] ndk-build (armeabi, android-14 headers, runs on Android 2.3.7)"
"$ANDROID_NDK/ndk-build" NDK_PROJECT_PATH="$ROOT" APP_BUILD_SCRIPT="$ROOT/jni/Android.mk" \
  NDK_APPLICATION_MK="$ROOT/jni/Application.mk" NDK_LIBS_OUT="$ROOT/out/libs" \
  NDK_OUT="$ROOT/out/obj" -j"$(nproc 2>/dev/null || echo 4)"

rm -rf out/gen out/classes out/dex out/apklib
mkdir -p out/gen out/classes out/dex out/apklib/lib/armeabi
cp -f out/libs/armeabi/librecipelab.so out/apklib/lib/armeabi/

# Version injection: aapt reads this copy, the checked-in manifest stays untouched.
MANIFEST=out/AndroidManifest.xml
sed -e "s/android:versionCode=\"[^\"]*\"/android:versionCode=\"$VERSION_CODE\"/" \
    -e "s/android:versionName=\"[^\"]*\"/android:versionName=\"$VERSION_NAME\"/" \
    AndroidManifest.xml > "$MANIFEST"

echo "[1/7] aapt R.java"
"$BT/aapt" package -f -m -J out/gen -M "$MANIFEST" -S res -I "$AJ"
echo "[2/7] javac"
"$JAVA/javac" -encoding UTF-8 --release 8 -Xlint:-options -cp "$AJ" -d out/classes \
  out/gen/com/voxivoid/recipelab/R.java src/com/voxivoid/recipelab/*.java
echo "[3/7] d8"
find out/classes -name '*.class' > out/classes.txt
"$JAVA/java" -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 --release --min-api 10 \
  --lib "$AJ" --output out/dex "@out/classes.txt"
echo "[4/7] aapt package + dex + native lib"
"$BT/aapt" package -f -M "$MANIFEST" -S res -I "$AJ" -F out/unaligned.apk
( cd out/dex   && "$BT/aapt" add ../unaligned.apk classes.dex )
( cd out/apklib && "$BT/aapt" add ../unaligned.apk lib/armeabi/librecipelab.so )
echo "[5/7] zipalign"
"$BT/zipalign" -f 4 out/unaligned.apk out/aligned.apk

echo "[6/7] sign (v1 only; the camera does not understand v2/v3)"
if [ -n "${ANDROID_KEYSTORE_B64:-}" ]; then
  KS="${RUNNER_TEMP:-$ROOT/out}/release.keystore"
  printf '%s' "$ANDROID_KEYSTORE_B64" | base64 -d > "$KS"
  trap 'rm -f "$KS"; restore_errno' EXIT
  KS_PASS="${ANDROID_KEYSTORE_PASSWORD:?}" KEY_PASS="${ANDROID_KEY_PASSWORD:?}" \
  "$BT/apksigner" sign --ks "$KS" --ks-key-alias "${ANDROID_KEY_ALIAS:?}" \
    --ks-pass env:KS_PASS --key-pass env:KEY_PASS \
    --min-sdk-version 10 --v1-signing-enabled true --v2-signing-enabled false \
    --v3-signing-enabled false --out RecipeLab.apk out/aligned.apk
else
  echo "      no ANDROID_KEYSTORE_B64 — using a throwaway key (cannot update an existing install)"
  [ -e debug.keystore ] || "$JAVA/keytool" -genkeypair -keystore debug.keystore -alias recipelab \
    -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname "CN=RecipeLab"
  "$BT/apksigner" sign --ks debug.keystore --ks-pass pass:android --key-pass pass:android \
    --min-sdk-version 10 --v1-signing-enabled true --v2-signing-enabled false \
    --v3-signing-enabled false --out RecipeLab.apk out/aligned.apk
fi

echo "[7/7] verify"
"$BT/apksigner" verify --min-sdk-version 10 RecipeLab.apk
sha256sum RecipeLab.apk > RecipeLab.apk.sha256
echo "BUILD OK: $ROOT/RecipeLab.apk  ($VERSION_NAME, versionCode $VERSION_CODE)"
