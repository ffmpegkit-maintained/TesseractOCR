# Tesseract OCR for Android — Wiki

Prebuilt **Tesseract 5.5.0 + Leptonica 1.84.1** AAR for Android. On-device text
recognition, no NDK, no rebuild, English bundled.

```kotlin
implementation("dev.ffmpegkit-maintained:tesseract-android:5.5.0")
```

## Pages

- **[Quick Start](Quick-Start)** — install, first OCR pass in 5 lines.
- **[Languages](Languages)** — bundled English, adding more traineddata, model quality.
- **[API Reference](API-Reference)** — `TesseractOCR`, `TesseractConfig`, `TesseractResult`.
- **[Performance](Performance)** — image prep, PSM modes, release vs debug, OpenMP.
- **[FAQ](FAQ)** — common issues (empty text, low confidence, missing language).
- **[Pro Integration](Pro-Integration)** — 12 languages, best models, OpenMP, batch API.
- **[Building From Source](Building-From-Source)** — submodules, NDK, CMake.

## Free vs Pro

| | Free | Pro |
|---|:---:|:---:|
| Languages | English | 12 bundled |
| Models | tessdata_fast | tessdata_best |
| OpenMP | ✗ | ✅ |
| Batch / advanced API | ✗ | ✅ |
| ABI | arm64-v8a | arm64-v8a + x86_64 |
| Channel | Maven / JitPack / Release | Gumroad |

**→ [Get Pro](https://www.jokobee.com/tesseract)**

Maintained by [Jokobee](https://www.jokobee.com).
