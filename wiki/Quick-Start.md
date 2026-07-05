# Quick Start

## 1. Add the dependency

```kotlin
// build.gradle.kts (app)
dependencies {
    implementation("dev.ffmpegkit-maintained:tesseract-android:5.5.0")
}
```

No `mavenCentral()` tweaks needed — it's a normal Central artifact. For JitPack
instead:

```kotlin
// settings.gradle.kts
repositories { maven("https://jitpack.io") }
// build.gradle.kts (app)
implementation("com.github.ffmpegkit-maintained:TesseractOCR:5.5.0")
```

## 2. Run OCR

```kotlin
import dev.ffmpegkit.tesseract.TesseractOCR

class MyViewModel : ViewModel() {
    private val ocr = TesseractOCR()

    suspend fun read(bitmap: Bitmap): String {
        if (!ocr.isInitialized) {
            ocr.initialize(context, language = "eng")   // English is bundled
        }
        return ocr.recognize(bitmap).text
    }

    override fun onCleared() { ocr.release() }
}
```

`initialize`, `recognize`, and `recognizeFile` are `suspend` functions — call them
from a coroutine. They already move work to a background dispatcher, so you don't
need `withContext` yourself.

## 3. Clean up

Call `ocr.release()` when done (e.g. `onCleared()` / `onDestroy()`) to free native
memory. A released instance can be re-initialised.

## Notes

- A single `TesseractOCR` instance is **not** thread-safe — serialise calls or use
  one instance per worker.
- The bundled model is `tessdata_fast` English. For other languages see
  **[Languages](Languages)**.
- See **[Performance](Performance)** for choosing the right Page Segmentation Mode.
