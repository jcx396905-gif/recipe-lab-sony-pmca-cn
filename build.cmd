@echo off
setlocal
cd /d %~dp0
REM ---- toolchain locations (override via environment) ----
if "%JAVA_HOME%"=="" set JAVA_HOME=C:\Users\%USERNAME%\.jdks\temurin-17.0.5
if "%ANDROID_SDK%"=="" set ANDROID_SDK=%LOCALAPPDATA%\Android\Sdk
if "%ANDROID_NDK%"=="" set ANDROID_NDK=C:\Users\%USERNAME%\pmca\ndk\android-ndk-r16b
if "%BUILD_TOOLS%"=="" set BUILD_TOOLS=30.0.3
if "%PLATFORM_JAR%"=="" set PLATFORM_JAR=%ANDROID_SDK%\platforms\android-28\android.jar
set JAVA=%JAVA_HOME%\bin
set BT=%ANDROID_SDK%\build-tools\%BUILD_TOOLS%
set AJ=%PLATFORM_JAR%

REM The platform's errno.h shim is for the libc-less updater build; it shadows the NDK's <errno.h>. Park it.
if exist jni\platform\errno.h ren jni\platform\errno.h errno.h.updater_only

echo [0/7] ndk-build (armeabi, android-14 headers, runs on Android 2.3.7)
call "%ANDROID_NDK%\ndk-build.cmd" NDK_PROJECT_PATH="%~dp0." APP_BUILD_SCRIPT="%~dp0jni\Android.mk" NDK_APPLICATION_MK="%~dp0jni\Application.mk" NDK_LIBS_OUT="%~dp0out\libs" NDK_OUT="%~dp0out\obj" -j4 || exit /b 1

rmdir /s /q out\gen out\classes out\dex out\apklib 2>nul
mkdir out\gen out\classes out\dex out\apklib\lib\armeabi
copy /Y out\libs\armeabi\librecipelab.so out\apklib\lib\armeabi\ >nul

echo [1/7] aapt R.java
"%BT%\aapt.exe" package -f -m -J out\gen -M AndroidManifest.xml -S res -I "%AJ%" || exit /b 1
echo [2/7] javac
"%JAVA%\javac.exe" -encoding UTF-8 --release 8 -Xlint:-options -cp "%AJ%" -d out\classes out\gen\com\voxivoid\recipelab\R.java src\com\voxivoid\recipelab\*.java || exit /b 1
echo [3/7] d8
setlocal enabledelayedexpansion
set CLASSES=
for /r out\classes %%f in (*.class) do set CLASSES=!CLASSES! "%%f"
"%JAVA%\java.exe" -cp "%BT%\lib\d8.jar" com.android.tools.r8.D8 --release --min-api 10 --lib "%AJ%" --output out\dex !CLASSES! || exit /b 1
endlocal
echo [4/7] aapt package + dex + native lib
"%BT%\aapt.exe" package -f -M AndroidManifest.xml -S res -I "%AJ%" -F out\unaligned.apk || exit /b 1
pushd out\dex
"%BT%\aapt.exe" add ..\unaligned.apk classes.dex || exit /b 1
popd
pushd out\apklib
"%BT%\aapt.exe" add ..\unaligned.apk lib/armeabi/librecipelab.so || exit /b 1
popd
echo [5/7] zipalign
"%BT%\zipalign.exe" -f 4 out\unaligned.apk out\aligned.apk || exit /b 1
echo [6/7] sign (v1 only; any self-signed key works on the camera)
if not exist debug.keystore "%JAVA%\keytool.exe" -genkeypair -keystore debug.keystore -alias recipelab -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname "CN=RecipeLab" || exit /b 1
call "%BT%\apksigner.bat" sign --ks debug.keystore --ks-pass pass:android --key-pass pass:android --min-sdk-version 10 --v1-signing-enabled true --v2-signing-enabled false --v3-signing-enabled false --out RecipeLab.apk out\aligned.apk || exit /b 1
echo [7/7] verify
call "%BT%\apksigner.bat" verify --min-sdk-version 10 RecipeLab.apk || exit /b 1
if not exist dist mkdir dist
copy /Y RecipeLab.apk dist\RecipeLab.apk >nul
echo BUILD OK: %~dp0RecipeLab.apk (copied to dist\)
