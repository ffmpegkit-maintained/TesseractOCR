# API Reference

Package `dev.ffmpegkit.tesseract`.

## `TesseractOCR`

The main entry point. One instance holds one native engine.

| Member | Signature | Notes |
|---|---|---|
| `isInitialized` | `val: Boolean` | True between `initialize` and `release`. |
| `initialize` | `suspend (context, language = "eng", config = TesseractConfig())` | Extracts tessdata, starts engine. |
| `recognize` | `suspend (bitmap: Bitmap): TesseractResult` | OCR a bitmap. |
| `recognizeFile` | `suspend (path: String): TesseractResult` | Decode + OCR an image file. |
| `addLanguage` | `suspend (context, language, file: File)` | Register a downloaded traineddata. |
| `getAvailableLanguages` | `(context): List<String>` | Codes on disk. |
| `getVersion` | `(): String` | Native Tesseract version, e.g. `"5.5.0"`. |
| `release` | `()` | Free native memory. Safe to call twice. |

```kotlin
val ocr = TesseractOCR()
ocr.initialize(context, "eng")
val r = ocr.recognize(bitmap)
ocr.release()
```

### Threading

`suspend` functions run on a background dispatcher internally. A single instance is
**not** thread-safe — don't call `recognize` concurrently on the same instance.

## `TesseractConfig`

```kotlin
data class TesseractConfig(
    val pageSegMode: Int = 3,            // PSM — 3 = auto layout
    val ocrEngineMode: Int = 3,          // OEM — 3 = default (LSTM)
    val whitelistChars: String = "",     // restrict to these chars, e.g. "0123456789"
    val preserveInterwordSpaces: Boolean = false,
)
```

Common **PSM** values: `3` auto, `6` single uniform block, `7` single line,
`10` single character. Pass a config to `initialize`:

```kotlin
ocr.initialize(context, "eng", TesseractConfig(pageSegMode = 7))
```

## `TesseractResult`

```kotlin
data class TesseractResult(
    val text: String,                 // full recognised text
    val confidence: Int,              // mean confidence 0–100
    val words: List<TesseractWord>,   // per-word detail
    val processingTimeMs: Long,       // recognition wall time
)

data class TesseractWord(
    val text: String,
    val confidence: Int,
    val boundingBox: android.graphics.Rect,   // pixel coords in the source image
)
```

## `TesseractException`

Sealed type; subtypes:

- `LanguageDataMissing(language)` — traineddata absent; bundle it or `addLanguage`.
- `InitFailed(dataPath, language)` — native `Init()` failed.
- `NotInitialized` — OCR called before `initialize` / after `release`.
- `InvalidImage(message)` — image could not be decoded/read.
