# Tesseract OCR for Android — Wiki

Prebuilt **Tesseract 5.5.0 + Leptonica 1.84.1** AAR for Android. On-device text
recognition, no NDK, no rebuild, English bundled.

```kotlin
implementation("dev.ffmpegkit-maintained:tesseract-android:5.5.0")
```

## New here? Start with the tutorial

**→ [Beginner Tutorial](Tutorial)** — from zero to OCR, step by step, with code for
every image source (file, gallery, camera, assets, network).

## Pages

- **[Beginner Tutorial](Tutorial)** — step-by-step guide with copy-pasteable code.
- **[Quick Start](Quick-Start)** — install, first OCR pass in 5 lines.
- **[How Image Decoding Works](Architecture-Image-Decoding)** — why there are no
  image codecs in the AAR (and why JPEG/PNG/WebP/HEIC all work anyway).
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
