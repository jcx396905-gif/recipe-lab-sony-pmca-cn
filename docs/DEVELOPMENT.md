# Development notes

Reverse-engineering notes, the settings-store map, and how to build Recipe Lab.
For using the app, see the [README](../README.md). For the branch/commit/release rules,
see [CONTRIBUTING.md](CONTRIBUTING.md).

---

**Contents**

- [Source layout](#source-layout)
- [Settings slots](#settings-slots)
- [Developer menu](#developer-menu-c1)
- [Exit rule](#exit-rule)
- [Live preview](#live-preview)
- [Developing on WSL](#developing-on-wsl)
- [Building](#building)
- [Unit tests](#unit-tests)
- [Installing on the camera](#installing-on-the-camera)
- [Versioning](#versioning)
- [Adding recipes](#adding-recipes)

---

## Source layout

```
AndroidManifest.xml            package com.voxivoid.recipelab
src/com/voxivoid/recipelab/
  MainActivity.java            UI state, key handling, the camera (CameraEx via reflection), store + sync
  Params.java                  the parameter rows: slot ids, store encodings, preview parameters, chip
                               navigation, HUD strings — pure functions, no Android, covered by test/
  Recipes.java                 the 77 recipes, brands, GROUP_START / GROUP_COUNT, table navigation
  Favourites.java              the favourites list: stored by name in the app's preferences, and how the browser
                               walks the Favourites group — pure functions, no Android, covered by test/
  DevTools.java                the developer menu rows and the sample run's delays, messages and manifest —
                               pure functions, no Android, covered by test/
  res/raw/ids.txt              every settings entry of 16 bytes or less, used by the snapshot/diff tool
  PickerView.java              Canvas-drawn brand browser (Favourites first, then the brands)
  MenuView.java                Canvas-drawn modal list: the developer menu behind C1
  Legend.java                  Canvas-drawn key icons and the favourite star, fit-to-width (camera font has no symbol glyphs).
                               The legend names the keys a body may not have; the four-way and the wheel are left out
                               as self-evident, and a hold is the centre-button icon labelled "(hold)"
  StarView.java                the star next to the recipe name when it is a favourite
  HintBar.java                 legend view under the panel (uses Legend)
  NativeBackup.java            JNI: read / write / attr / sync
jni/jni.cpp                    Backup_read / Backup_write / Backup_sync_all via OpenMemories-Platform
jni/platform/                  git submodule: ma1co/OpenMemories-Platform
res/                           layout, shape drawables, launcher icon
test/com/voxivoid/recipelab/   JUnit tests for Recipes and Params (see Unit tests)
build.sh                       the build: ndk-build, aapt, javac, d8, zipalign, apksigner
build.cmd                      the same seven steps on Windows
tools/                         version computation, bumping, the unit tests, and the CI gates
```

## Settings slots

Found by disassembling the camera app's parameter registration in `libObj.so`):

| setting | id | notes |
|---|---|---|
| Creative Style | `0x01070175` | index in the runtime `color-mode-values` list (verified by menu diff: 1 standard, 2 vivid, 3 neutral, 6 mono, **14 sepia**). 13 is a style the menu never selected for us, so `Recipes.STYLE_NAMES[13]` is `null` and the chip skips it; 4..12 are still guessed from the runtime list order and each needs its own menu diff |
| Contrast | `0x01070178` | signed byte |
| Saturation | `0x01070187` | signed byte, core accepts ±16 |
| Sharpness | `0x0107018a` | signed byte |
| Picture Profile no. | `0x0107031c` | 0 off, 3 = alternate colour matrix (no gamma on this body) |
| WB mode | `0x01070019` | 1 auto, 14 colour temperature |
| WB Kelvin | `0x01070018` | Kelvin / 100 |
| WB A-B / G-M | `0x01070017` / `0x01070016` + per-mode copies: AWB `0x0107067f` / `0x0107067e`, colour temp `0x01070683` / `0x01070682` | signed, magenta positive (menu G1 = 0xff). The camera applies the per-mode copy (verified end-to-end) |
| Picture Effect | `0x010706f1` | index in `picture-effect-values` (verified: Retro = 4) |
| Effect sub-setting | `0x010709d8` high-key tint · `0x010706f3` toy tone · `0x010706ee` partial-colour hue · `0x010706ef` posterization | index in the runtime value list (high-key tint verified) |
| Exposure bias | `0x010700b8` + copy `0x01070c7f` | 1/3 EV steps, signed (verified: +0.7 = 2); both written |
| DRO | `0x01070104` (+ level byte `0x01070775`) | Off 0, Auto 1, Lv1–5 = 2–6; level byte 1 for Off/Auto, Lv n = n+1 (verified) |
| Quality: file format | `0x01070013` (+ mirror `0x01070aa9`) | RAW = 1, RAW+JPEG = 2, JPEG = 0 (verified) |
| Quality: JPEG level | `0x01070014` (+ mirror `0x01070aaa`) | Std = 0, Fine = 1 (verified) |

## Developer menu (C1)

**C1** opens a modal list of the tools that are not part of using the app (`MenuView`, rows and strings in
`DevTools`). Up / down or the wheel move, the centre button runs a row, MENU or C1 closes it:

| row | what it does |
|---|---|
| **Settings snapshot** / **Settings diff** | the snapshot / diff tool below; the row's name says which half is next |
| **Read-only check — 26 slots** | the read-only check below: does this body flag any slot a recipe writes |
| **Shoot samples — 77 recipes** | the sample run below |
| **Settle delay — 1.2 s** | the delay the sample run waits after applying a recipe; the centre button cycles 0.8 / 1.2 / 2.0 / 3.0 / 5.0 s, kept in the app's preferences |

C1 is missing on several supported bodies (issue #18), which is fine for a developer menu and would not be for
anything in the app proper — see [Centre button hold](#centre-button-hold).

### Snapshot / diff tool

How the slots above were found, and how to find the next one. The row runs `snapshotOrDiff()`, over every
id in `res/raw/ids.txt` (each settings entry of 16 bytes or less):

1. **First press** writes `snapshot.bin` into `getFilesDir()` — the current value of every id.
2. Leave the app, change **one** thing in the camera menus, reopen.
3. **Second press** re-reads every id, diffs it against the snapshot, shows the changed ones as
   `id:old>new` (first 14 on screen), appends the same line to `diff.txt` in `getFilesDir()`, and deletes
   `snapshot.bin` — so the next press starts a fresh snapshot.

Whatever shows up is the slot for the menu item you changed. Change one thing at a time or the diff is useless:
the camera rewrites unrelated entries on its own, so a second change means guessing which id belongs to what.

### Read-only check

Whether **backup protection** can stop a recipe on this body, which is the question the old `PROTECTED` badge
got wrong ([#19](https://github.com/voxivoid/recipe-lab-sony-pmca/issues/19)).

Protection is not a global write lock. Each settings slot carries attributes, and `BACKUP_ATTR_READ_ONLY`
(`Params.ATTR_READ_ONLY`) is the one that matters: a slot with that bit is refused with
`-BACKUP_ERROR_READ_ONLY` while protection is on and accepted once OpenMemories-Tweak has turned protection off.
A slot **without** the bit is writable either way. The badge called `Backup_guess_protection()`, which writes one
hardcoded read-only slot (`0x010d008f`) back over itself and reports whether that was refused — a true answer
about that slot, and no answer at all about Creative Style, saturation or white balance.

So the app asks per slot instead. The row reads `NativeBackup.attr(id)` for every entry of `Params.allSlots()` —
the 26 slots any recipe can write — and reports `26 recipe slots checked · none read-only`, or names the rows
that are flagged. The full list goes to `locks.txt` in `getFilesDir()`, one line per slot
(`01070175 STYLE attr=0`), which is what a compatibility report should quote. A slot the camera will not answer
for counts as writable: the write path reports a refusal properly, so a failed probe must not block a recipe.

The same check runs before every write (`Params.lockedFrom`, over the slots that write would touch). When
it finds one, nothing is written at all — a refusal half-way through would leave half a recipe in the store — and
the message names the settings and the Protection tweak. If the camera refuses anyway,
`Params.writeFailedMessage` names the slot that stopped it and how many bytes went in first.

**Run it on a factory body with protection still on.** That is the measurement the removal rests on: if a body
reports nothing read-only there, no recipe can ever be refused on it, whatever the protection flag says.

### Sample run

One frame per recipe, in table order — the capture half of issue #17. The run is a timed loop on the activity's
`Handler`, not a thread:

1. stage recipe *n* as a live preview, forcing **JPEG Fine** (a RAW frame carries no look, and a Picture Effect
   needs JPEG at all); the store is never written, so a run changes nothing permanent
2. wait the **settle delay** so the preview pipeline catches up
3. `takePicture`, then `cancelTakePicture` and `startPreview` `DevTools.SHUTTER_MS` later — the shutter key's
   press / release, automated
4. next recipe, until the table ends

While it runs, a sticky line counts the frames (`Shooting 12 / 77 · Velvia — MENU stops`) and **every key is
swallowed** so nothing walks the table underneath it; **MENU** stops the run. The run also stops in `onPause` — it
cannot outlive the camera it shoots with. When it ends, the recipe the user was on is staged again.

The frames are identified by **order**: the camera names the files, and the run appends its own list to
`samples.txt` in `getFilesDir()`, one line per frame —

```
# recipe-lab samples  ·  77 frames in recipe order  ·  settle 1200 ms  ·  frame|recipe|brand|values
01|FACTORY (ST)|Sony|Standard  0/0
02|Sony PT (portrait)|Sony|Portrait  0/0
```

**What the run cannot tell you.** Whether the settle delay is long enough is a property of the camera: too short and
a frame still carries the previous recipe's look. Shoot a run, then store two or three of the same recipes by hand
and shoot them again — if the frames differ, raise the delay in the menu and shoot again. Same for
`DevTools.SHUTTER_MS` and the `startPreview` between frames: both are what a capture needs on *this* body, and
neither a build nor `tools/test.sh` can say anything about them.

## Exit rule

The camera writes some live parameters (exposure bias, WB fine-tune) straight back into the settings
store, so on exit the app sets the live parameters to the *stored* values rather than to its launch snapshot —
otherwise a freshly stored recipe would be undone the moment the app closes.

## Live preview

Goes through `Camera.Parameters`: `color-mode`, `saturation`, `contrast`, `sharpness`,
`whitebalance`, `color-temperture-white-balance`, `light-balance-for-white-balance`,
`color-compensation-for-white-balance`, `rgb-matrix` (Q10, 1.0 = 1024) + `rgb-matrix-mode`, `picture-effect`,
`exposure-compensation` (1/3 EV steps), `dro-mode` + `dro-level`.
**Key scan codes:** wheel 522 / 523, top dial 525 / 526, AEL 532, C1 622, Fn 520, trash 595, centre 232, MENU 514.

## Centre button hold

The centre button is the one key every PlayMemories body has, so anything new that needs a key goes on a **hold** of it
rather than on Fn / AEL / C1, which several bodies lack (issue #18). Today a hold (`HOLD_MS`, 600 ms) marks the recipe
as a favourite. To make room for it, ENTER's short action — store on the main screen, pick in the browser, focus a chip —
runs on the key **release** instead of the press; a hold that has fired swallows the release. The hold is timed with a
`Handler.postDelayed` armed on the press and cancelled on the release, so it does not depend on the firmware
delivering key-repeat events. Held keys are cleared in `onPause`.

Favourites live in `getPreferences(MODE_PRIVATE)` under `favourites`, as recipe **names** joined with `|` (so a table
that gains a recipe does not shift the marks); a name the table no longer has is dropped on load. They are app storage,
not the camera settings store: a power cycle keeps them, an uninstall does not.

## Developing on WSL

This is how the machine is set up: everything except talking to the camera happens inside WSL.

**Keep the repository on the Linux filesystem** — `~/code/...`, never `/mnt/c/...`. Windows
drives are reached over the 9p protocol, where each file operation costs milliseconds instead
of microseconds. A build does thousands of them, so the difference is seconds versus minutes,
and every git command crawls.

Where things live:

| | |
|---|---|
| `~/toolchains/jdk17` | JDK 17 (Temurin) |
| `~/Android/Sdk` | build-tools 30.0.3, platform-28, NDK r16b |
| `~/.keys/recipelab-release.keystore` | the signing key, `chmod 600` |
| `~/code/sony-pmca-re` | Sony-PMCA-RE — dumps, the updater shell, installing |
| `~/code/a6000-dumps` | firmware and settings-store dumps |
| `~/code/pmca-scripts` | camera helper scripts |

Worth putting in `~/.bashrc`:

```bash
export JAVA_HOME=$HOME/toolchains/jdk17
export ANDROID_SDK=$HOME/Android/Sdk
export ANDROID_NDK=$ANDROID_SDK/ndk/16.1.4479499
export PATH="$HOME/.local/bin:$PATH"      # gh lives here
export BROWSER=wsl-browser                # so `gh auth login` opens Windows Firefox
```

To build with the project key instead of a throwaway one:

```bash
export ANDROID_KEYSTORE_B64="$(base64 -w0 ~/.keys/recipelab-release.keystore)"
export ANDROID_KEYSTORE_PASSWORD=android
export ANDROID_KEY_ALIAS=probe
export ANDROID_KEY_PASSWORD=android
```

An APK signed with a different key **cannot be installed over an existing Recipe Lab** — the
app has to be removed first — so use these whenever you are updating a camera that already
has it.

`npm ci` is only needed for the release tooling (semantic-release, `tools/next-version.sh`).
No JavaScript ships in the APK.

### Things that bite

- **`apksigner` and `keytool` exec `java` from `PATH`**, so `JAVA_HOME` on its own is not
  enough; `build.sh` prepends `$JAVA_HOME/bin` for exactly this. Without it, step 6 fails with
  `exec: java: not found`.
- **`BROWSER` is word-split**, so a path containing spaces cannot be used directly.
  `~/.local/bin/wsl-browser` is a two-line wrapper that quotes the Windows Firefox path.
- **`sudo` prompts for a password**, so anything needing root — usbip tools, udev rules —
  cannot be scripted unattended.
- **The camera is invisible from WSL.** See [Installing on the camera](#installing-on-the-camera).

## Building

Toolchain, both platforms: **JDK 17**, Android SDK **build-tools 30.0.3** with a platform jar (**API 28**), and
**NDK r16b** — the last NDK with the GCC toolchain this Android 2.3.7 target needs. `jni/Application.mk` pins
`APP_ABI := armeabi`, `APP_STL := stlport_static`, `APP_PLATFORM := android-14`, `NDK_TOOLCHAIN_VERSION := 4.9`;
that combination is what rules out every later NDK.

```
git clone --recursive https://github.com/voxivoid/recipe-lab-sony-pmca.git
cd recipe-lab-sony-pmca
```

**Linux / WSL / macOS** — `build.sh`. This is what CI runs and the supported way to build:

```bash
export JAVA_HOME=$HOME/toolchains/jdk17
export ANDROID_SDK=$HOME/Android/Sdk
export ANDROID_NDK=$ANDROID_SDK/ndk/16.1.4479499
./build.sh                 # X.Y.Z-dev.N
RELEASE=1 ./build.sh       # X.Y.Z — the tag must match the manifest
```

Setting the toolchain up from nothing, no root required:

```bash
# JDK 17
curl -sL -o jdk.tar.gz "https://api.adoptium.net/v3/binary/latest/17/ga/linux/x64/jdk/hotspot/normal/eclipse"
mkdir -p ~/toolchains/jdk17 && tar xzf jdk.tar.gz -C ~/toolchains/jdk17 --strip-components=1

# Android cmdline-tools, then the three packages
curl -sL -o cmdline.zip "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
mkdir -p ~/Android/Sdk/cmdline-tools && unzip -q cmdline.zip -d /tmp/ct
mv /tmp/ct/cmdline-tools ~/Android/Sdk/cmdline-tools/latest
yes | ~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager --licenses >/dev/null
~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager \
  "build-tools;30.0.3" "platforms;android-28" "ndk;16.1.4479499"
```

About 3 GB installed. `build.cmd` is the Windows equivalent and is kept in step with
`build.sh`, but the toolchain it needs is no longer installed on this machine.

To sign with the project key rather than a throwaway one, set
`ANDROID_KEYSTORE_B64` (`base64 -w0 <keystore>`), `ANDROID_KEYSTORE_PASSWORD`,
`ANDROID_KEY_ALIAS` and `ANDROID_KEY_PASSWORD` — the same four values CI holds as secrets.

Both scripts park the platform's `errno.h` shim (updater-only, it shadows the NDK header), then run ndk-build,
aapt, javac (`-encoding UTF-8`), d8 (invoked as `java -cp d8.jar`, because `d8.bat` uses whatever Java is on
PATH), zipalign and apksigner — **v1 signing only**, since the camera does not understand v2/v3.
`build.sh` restores `errno.h` from an `EXIT` trap, so an aborted build never leaves the submodule dirty.

**Signing.** With no keystore configured, both scripts generate a throwaway key. An APK signed with a
different key **cannot be installed over an existing one** — the camera would need the app removed first.
CI therefore signs with the project key, held as the `ANDROID_KEYSTORE_B64` repo secret; set the same four
`ANDROID_KEYSTORE_*` variables locally if you need a build that updates an existing install in place.

## Unit tests

```bash
export JAVA_HOME=$HOME/toolchains/jdk17
./tools/test.sh              # everything
./tools/test.sh Writes       # only test classes whose name contains "Writes"
```

That is the whole of the `test` CI job on work branches; `dev-build` runs the same script before every
development build and `create-release` before anything is pushed to `main`. It needs a JDK 17 and nothing else: `Recipes.java`, `Params.java` and `Favourites.java`
are compiled against the bare JDK — no `android.jar`, no NDK — then the tests under `test/` are compiled and run
with the JUnit 5 console launcher, one jar fetched from Maven Central into `out/test/` on first use and checked
against a SHA-256 pinned in the script (`JUNIT_JAR=<path>` points it at a copy when offline). Reports land in
`out/test/reports/`.

**What is covered.** Everything that decides without the camera lives in `Params`, `Recipes` and `Favourites`, and the
tests pin it down:

| | |
|---|---|
| `RecipesTest` | the table itself — 77 recipes, group order, every value inside its row's range, kelvin in whole hundreds, sub-parameters that exist for the effect; labels, `summary()`, wrap-around navigation |
| `ParamsCodecTest` | how the store encodes each row (DRO bytes, PP3 for the matrix, magenta-positive G-M, the quality pair, signed vs unsigned slots) and how it reads back |
| `ParamsWritesTest` | which bytes ENTER writes for a recipe — golden lists for a few, and every recipe stored over a factory camera, then on top of each other, read back through the same decoder |
| `ParamsPreviewTest` | the `Camera.Parameters` the live preview sets, recipe by recipe |
| `ParamsChipsTest` | chip visibility, stepping (wrap vs clamp, the effect → SUB / quality side effects), LEFT/RIGHT and UP/DOWN landing spots, chip text |
| `ParamsHudTest` | the meta line, the minimal pill, the quality prompt |
| `ParamsToolsTest` | the snapshot tool's id list — including that `res/raw/ids.txt` is well formed and lists every slot the app writes — and its diff lines |
| `DevToolsTest` | the developer menu's rows and settle delays, the sample run's progress / finish lines, and its manifest — a parsable line per recipe, in run order |
| `FavouritesTest` | the favourites list — stored by name, unknown names dropped, marking order kept, toggle, the highlight after a removal — and the browser's group order with Favourites first |

**What is not, and cannot be.** `MainActivity` (key dispatch, overlays, the camera and the JNI store), the
Canvas views (`PickerView`, `PromptView`, `MenuView`, `HintBar`, `Legend`) and `jni/jni.cpp` need a running camera or an
Android runtime; there is no Gradle and no Robolectric here, and a mock of `CameraEx` would prove nothing. Those
stay on the [on-camera checklist](CONTRIBUTING.md#on-the-camera). Likewise the slot ids themselves: a
test can show that the app writes `0x01070175 = 6`, not that the camera means B&W by it.

**Keeping it that way.** New logic that does not need the camera goes into `Params` (or `Recipes`, `Favourites`, or a
new class listed in `UNITS` in `tools/test.sh`) with a test next to it, and is called from `MainActivity`, never the
other way round. `tools/test.sh` compiles those classes without `android.jar` on purpose: an `android.*` import in either fails there before it fails in CI. Tests
are plain JUnit 5 (`org.junit.jupiter.api`), one behaviour per method, no mocking library; `Fixtures` has a
factory-fresh camera as rows and as store bytes and a fake store to write into.

### Installing on the camera

**Build in WSL, install from Windows.** WSL2 is a VM with no USB controller, so the camera is
only reachable from the Windows side. Everything else — building, dumps, git, releases — is
WSL-native.

```bash
./build.sh                              # in the repo
~/code/pmca-scripts/install-to-camera.sh   # copies the APK over and drives Sony-PMCA-RE
```

The camera must be on, with `Setup → USB Connection` set to **Mass Storage**. The script
uses the Windows Python at
`C:\Users\voxiv\AppData\Local\Programs\Python\Python311\python.exe` against the
Sony-PMCA-RE checkout at `C:\Users\voxiv\pmca\src`; those two are the only things this
project still needs on Windows.

If you would rather install from inside WSL as well, `~/code/pmca-scripts/setup-usb-wsl.sh`
sets up the Linux half of USB forwarding (usbip tools, the `054c` udev rule, pyusb). The
Windows half is `usbipd-win`: `winget install dorssel.usbipd-win` once from an Administrator
PowerShell, `usbipd bind --busid <id>` once per camera, then `usbipd.exe attach --wsl --busid
<id>` after each replug. Forwarding a USB device needs a Windows-side driver either way —
that part cannot be removed.

## Versioning

`AndroidManifest.xml` `android:versionName` is the **single source of truth**, and always holds the *next
target release* (`X.Y.Z`, no suffix). Nothing else stores a version — not the README, not the Java source.

```
versionCode = MAJOR*10_000_000 + MINOR*100_000 + PATCH*1_000 + P
P = N    dev prerelease, N = commits since the last v* tag (1..998)
P = 999  release
```

`999` makes a release outrank every prerelease before it, so `1.1.0` installs cleanly over `1.1.0-dev.42`
instead of being refused as a downgrade.

| script | does |
|---|---|
| `tools/version.sh` | computes `VERSION_NAME` / `VERSION_CODE` for a build; sourced by `build.sh` |
| `tools/bump-version.sh <x.y.z>` | opens the next cycle — the only way a version is ever typed |
| `tools/check-version.sh` | CI gate: manifest is consistent and no version mirror has crept back in |
| `tools/dev-notes.js <version>` | prints the notes for a dev build from the commits since the last `v*` tag, using the same semantic-release generator and preset as a release |

A build never mutates the checked-in manifest; it writes `out/AndroidManifest.xml` and points `aapt` there.

## Adding recipes

Adding a recipe is one line in `Recipes.java` inside its brand block. Adding a brand is a new entry in `GROUPS` plus
a block of recipes.

**Saturation is steep on this body.** The menu shows ±3 but the core takes ±16, and the sample frames put numbers on
it: against the Factory look, `sat -4` keeps about half the chroma, `-6` is close to grey, `-8` and below is grey
(measured chroma 0). A "muted" look is -2 to -4; -6 is a monochrome with a tint. Positive values are gentler:
`+5` with the PP3 matrix roughly doubles the chroma.

**Colour-temperature recipes cannot be judged indoors.** A fixed kelvin renders relative to the light in the room,
not to the recipe's intent: 5600K under warm indoor light comes out amber, and 3200K comes out nearly neutral.
Shoot those eight in daylight before deciding anything about them.
