# Performance

OCR quality and speed depend far more on the **input image** than on any flag.

## 1. Give Tesseract a clean image

- **Resolution**: aim for ~300 DPI equivalent. Text glyphs should be ≥ 20 px tall.
  Upscale tiny screenshots; downscale huge photos (Tesseract is slow on 12 MP).
- **Binarise / contrast**: high-contrast black text on white works best. Leptonica's
  internal Otsu thresholding handles most cases, but pre-processing (grayscale,
  contrast boost, deskew) helps a lot.
- **Crop** to the text region — less background = faster and more accurate.

This library converts the bitmap to 8-bit grayscale before handing it to Tesseract,
which is smaller and faster across JNI than RGBA.

## 2. Pick the right Page Segmentation Mode

The default `PSM_AUTO` (3) analyses full-page layout. If you already know the shape
of your input, a tighter mode is faster and more reliable:

| Input | PSM |
|---|---|
| Full document / mixed layout | 3 (auto) |
| One paragraph / uniform block | 6 |
| A single line (receipts, labels) | 7 |
| A single word | 8 |
| A single character | 10 |

```kotlin
ocr.initialize(context, "eng", TesseractConfig(pageSegMode = 7))
```

## 3. Restrict the character set

For numeric fields, IDs, or codes, a whitelist cuts errors dramatically:

```kotlin
TesseractConfig(whitelistChars = "0123456789")
```

## 4. Model quality vs speed

`tessdata_fast` (Free default) is the fastest LSTM model. `tessdata_best`
(Pro default) is more accurate but slower and larger. Match the pack to your
accuracy budget — see **[Languages](Languages)**.

## 5. OpenMP (Pro)

The Free build is **single-threaded** (no OpenMP). On large images this leaves cores
idle. **[Tesseract Pro](https://www.jokobee.com/tesseract)** ships with OpenMP
enabled, which parallelises recognition across cores for a meaningful speed-up on big
inputs.

## 6. Measure in release, not debug

Native code built in **debug** is 10–50× slower (unoptimised). Always benchmark a
**release** build — this AAR is compiled with `-O3`.

## Typical timings

Rough guide on a modern arm64 device, single line of text, `tessdata_fast`:
~100–400 ms per `recognize`. Full pages and `tessdata_best` are proportionally
slower. `result.processingTimeMs` reports the actual time per call.
