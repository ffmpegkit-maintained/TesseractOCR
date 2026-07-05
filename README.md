# Tesseract OCR for Android

[![Maven Central](https://img.shields.io/maven-central/v/dev.ffmpegkit-maintained/tesseract-android)](https://central.sonatype.com/artifact/dev.ffmpegkit-maintained/tesseract-android)
[![JitPack](https://jitpack.io/v/ffmpegkit-maintained/TesseractOCR.svg)](https://jitpack.io/#ffmpegkit-maintained/TesseractOCR)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Website](https://img.shields.io/badge/website-jokobee.com-blue.svg)](https://www.jokobee.com/tesseract)

**On-device text recognition — prebuilt Tesseract 5 + Leptonica AAR, one Gradle
line, no NDK, no rebuild.** English bundled, works offline out of the box.

Wiring Tesseract into an Android app normally means building Tesseract **and**
Leptonica from source with the NDK, wrangling CMake and JNI, and shipping traineddata
yourself. We prebuild all of it into a single AAR with a clean Kotlin coroutine API.

## Install (Free)

```kotlin
dependencies {
    implementation("dev.ffmpegkit-maintained:tesseract-android:5.5.0")
}
```

<sub>Also on JitPack: `com.github.ffmpegkit-maintained:TesseractOCR:5.5.0` (module `library`).</sub>

## Quick start

```kotlin
val ocr = TesseractOCR()
ocr.initialize(context, language = "eng")   // English model is bundled

val result = ocr.recognize(bitmap)
println(result.text)                          // recognised text
println("confidence ${result.confidence}%")   // 0–100
result.words.forEach { w -> println("${w.text} @ ${w.boundingBox}") }

ocr.release()
```

All calls are `suspend` functions and run off the main thread. English
(`eng.traineddata`, `tessdata_fast`) ships **inside the AAR** — no download, no
assets to manage. Need more languages? Drop any `*.traineddata` in and call
`addLanguage()`, or step up to Pro (12 languages bundled).

## What's inside

| | |
|---|---|
| Engine | Tesseract **5.5.0** + Leptonica **1.84.1**, LSTM |
| Bundled model | English (`eng`, tessdata_fast, ~4 MB) |
| ABI | `arm64-v8a` |
| Min SDK | API 24 (Android 7.0) |
| 16 KB pages | ✅ Android 15 ready |
| API | Kotlin coroutines (`suspend`) |
| OpenMP | ✗ (single-thread) |

## Free vs Pro

| | **Free** (this) | **Pro** |
|---|:---:|:---:|
| Engine | Tesseract 5 + Leptonica | Tesseract 5 + Leptonica |
| Bundled languages | English | **12 languages** (best-quality models) |
| Model quality | `tessdata_fast` | **`tessdata_best`** (higher accuracy) |
| OpenMP multi-threading | ✗ | ✅ (faster on large images) |
| Batch / advanced API | ✗ | ✅ (`TesseractBatch`, `TesseractAdvanced`) |
| ABI | arm64-v8a | arm64-v8a + **x86_64** (emulator) |
| Channel | Maven Central + JitPack + GitHub Release | Gumroad |
| Price | **Free** | one-time |

**→ [Get Tesseract Pro](https://www.jokobee.com/tesseract)** — 12 languages,
best-quality models, OpenMP, batch API, x86_64 emulator support.

## Documentation

- **[Wiki](../../wiki)** — Quick Start, languages, API reference, performance, FAQ, Pro.
- **[sample/](sample)** — a minimal pick-an-image-and-OCR demo app.

## Building from source

```bash
git clone --recursive https://github.com/ffmpegkit-maintained/TesseractOCR.git
cd TesseractOCR
./gradlew :library:assembleRelease
```

Requires NDK r27c (`27.2.12479018`) and CMake 3.22.1. Leptonica and Tesseract are
pinned git submodules (1.84.1 / 5.5.0).

## License

Apache 2.0 — see [LICENSE](LICENSE). Tesseract and Leptonica are Apache 2.0 and
BSD-2-Clause respectively. Bundled `eng.traineddata` is Apache 2.0
([tessdata_fast](https://github.com/tesseract-ocr/tessdata_fast)).

---

Maintained by **[Jokobee](https://www.jokobee.com)** (Luc Côté).
