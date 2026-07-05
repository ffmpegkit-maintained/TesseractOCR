# Pro Integration

**[Tesseract Pro](https://www.jokobee.com/tesseract)** is a drop-in superset of the
Free AAR, distributed via Gumroad (not Maven/JitPack).

## What Pro adds

| | Free | **Pro** |
|---|:---:|:---:|
| Bundled languages | English | **12 languages** |
| Model quality | `tessdata_fast` | **`tessdata_best`** |
| OpenMP multi-threading | ✗ | ✅ |
| Batch API (`TesseractBatch`) | ✗ | ✅ |
| Advanced API (`TesseractAdvanced`) | ✗ | ✅ |
| ABI | arm64-v8a | arm64-v8a + **x86_64** |

Same package (`dev.ffmpegkit.tesseract`), same core API — your Free code keeps
working unchanged.

## Installing the Pro AAR

After purchase you receive `tesseract-android-pro-<version>.aar` (and the sample
guide). Add it as a local module or file dependency:

```kotlin
// app/libs/tesseract-android-pro-5.5.0.aar
dependencies {
    implementation(files("libs/tesseract-android-pro-5.5.0.aar"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
```

## Batch OCR

```kotlin
val batch = TesseractBatch(ocr)
val results = batch.recognizeAll(listOf(bitmap1, bitmap2, bitmap3))
results.forEach { println(it.text) }
```

## Advanced options

```kotlin
val advanced = TesseractAdvanced(ocr)
val hocr = advanced.recognizeHocr(bitmap)     // hOCR / structured output
val tsv  = advanced.recognizeTsv(bitmap)      // TSV with layout
```

## x86_64 emulator support

Pro ships `arm64-v8a` **and** `x86_64`, so it runs on standard Android emulators —
handy for CI and desktop testing.

## Get Pro

**→ [jokobee.com/tesseract](https://www.jokobee.com/tesseract)**
