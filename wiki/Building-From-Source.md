# Building From Source

You don't need to build the AAR to use it (`implementation("dev.ffmpegkit-maintained:tesseract-android:5.5.0")`).
These notes are for contributors and for reproducing the artifact.

## Prerequisites

- JDK 17
- Android SDK, **NDK r27c** (`27.2.12479018`), **CMake 3.22.1**
- Ninja (`apt-get install ninja-build`)

```bash
sdkmanager "ndk;27.2.12479018" "cmake;3.22.1"
```

## Clone with submodules

Leptonica and Tesseract are pinned git submodules:

```bash
git clone --recursive https://github.com/ffmpegkit-maintained/TesseractOCR.git
cd TesseractOCR
# or, if already cloned:
git submodule update --init --recursive
```

| Submodule | Upstream | Pinned tag |
|---|---|---|
| `leptonica` | github.com/DanBloomberg/leptonica | 1.84.1 |
| `tesseract` | github.com/tesseract-ocr/tesseract | 5.5.0 |

## Build

```bash
./gradlew :library:assembleRelease
# → library/build/outputs/aar/library-release.aar
```

The native build (`library/src/main/jni/CMakeLists.txt`) does, in order:

1. Build **Leptonica** as `libleptonica.so` (`BUILD_PROG=OFF`, `SW_BUILD=OFF`).
2. Build **Tesseract** as `libtesseract.so` (training tools, curl, archive,
   graphics all disabled; `OPENMP_BUILD=OFF` for Free).
3. Build the JNI bridge `libtesseract_jni.so`, linking both.

All `.so` are linked with 16 KB max-page-size for Android 15 compatibility.

## ABIs

Free builds **arm64-v8a** only (`abiFilters` in `library/build.gradle.kts`). The Pro
variant adds `x86_64`.

## Run the sample

```bash
./gradlew :sample:installDebug
```

Pick an image; it OCRs with the bundled English model and shows text + confidence.

## Publishing

Handled by GitHub Actions:

- `build-debug.yml` — CI build on push/PR.
- `build-release.yml` — release AAR + GitHub Release on `v*` tags.
- `publish-maven.yml` — manual, verifies the native lib then publishes to Maven
  Central (irreversible; run only after validation).
