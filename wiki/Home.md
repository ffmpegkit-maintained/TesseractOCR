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
|---|---|---|
| Engine | Tesseract 5.x + Leptonica | Tesseract 5.x + Leptonica |
| Bundled languages | English (1) | **12** (eng, fra, spa, deu, ita, por, chi_sim, jpn, kor, ara, rus, hin) |
| Add more languages | ✅ 100+ downloadable | ✅ 100+ downloadable |
| Model quality | `tessdata_fast` (~4 MB) | `tessdata_best` (~15 MB, max accuracy) |
| OpenMP | ✗ single-thread | ✅ multi-thread (2–3× faster) |
| Core API | ✅ `recognize`, `recognizeFile`, confidence, bounding boxes | ✅ same |
| Advanced API | ✗ | ✅ hOCR, batch OCR, per-character confidence |
| ABI | arm64-v8a | arm64-v8a + x86_64 |
| Image formats | JPEG, PNG, WebP, HEIC (via Android) | same |
| Channel | Maven Central / JitPack / GitHub Release | Gumroad |
| Price | **$0** | **$24** · $62 team |

**→ [Get Pro](https://www.jokobee.com/tesseract)**

Maintained by [Jokobee](https://www.jokobee.com).
