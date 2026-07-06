# How Image Decoding Works

## TL;DR

**You don't need libjpeg, libpng, or libtiff.** JPEG, PNG, WebP, and HEIC all work
out of the box — Android decodes them for you.

## Why are there no image libraries in the AAR?

If you know Tesseract, you know Leptonica normally bundles **libjpeg-turbo**,
**libpng**, and **libtiff** so it can open image *files* directly. Look inside this
AAR and you won't find them — and that's **by design, not an oversight**.

On Android there's already a battle-tested image decoder in the platform, so we let
it do the decoding and hand Leptonica only what it actually needs: pixels.

1. You pass an image to `TesseractOCR.recognize(bitmap)` or
   `TesseractOCR.recognizeFile(path)`.
2. Android's **`BitmapFactory`** decodes the file (JPEG, PNG, WebP, HEIC — every
   format Android supports natively).
3. The bitmap is converted to **grayscale raw pixels** (Rec. 601 luma, 1 byte/pixel).
4. **Only raw pixels reach Leptonica** via `TessBaseAPI::SetImage` — it never sees
   encoded bytes, so it never needs an image codec.

```kotlin
// recognizeFile(path) in one glance:
val bitmap = BitmapFactory.decodeFile(path)   // Android decodes JPEG/PNG/WebP/HEIC…
val result = ocr.recognize(bitmap)            // → grayscale bytes → SetImage(raw pixels)
```

## What this buys you

- **More formats, not fewer** — Android decodes HEIC, WebP, and other formats that
  Leptonica alone can't. Anything `BitmapFactory` reads, you can OCR.
- **Smaller AAR** — no image-codec libraries bundled.
- **Simpler, more robust build** — fewer native dependencies means fewer things to
  break and fewer CVEs to track.
- **Identical OCR quality** — Tesseract works on pixels, not files. The decoder
  doesn't change the recognition result.

## "pixReadMemTiff" warnings in logcat

You may see non-fatal warnings at startup:

```
Error in pixReadMemTiff: function not present
Error in pixReadMem: tiff: no pix returned
Error in pixaGenerateFontFromString: pix not made
Error in bmfCreate: font pixa not made
```

These are **harmless**. On `Init`, Tesseract tries to build an internal debug bitmap
font through Leptonica's TIFF reader. Because we don't bundle libtiff, Leptonica
reports "function not present" and Tesseract simply continues without the debug
font. It does **not** affect OCR results, accuracy, or performance — you can ignore
these lines. (If we ever want them gone, it'll be through a Tesseract build flag,
not by adding unused libraries.)

## Supported image formats

Anything Android's `BitmapFactory` supports:

| Format | Supported |
|---|:---:|
| JPEG | ✓ |
| PNG | ✓ |
| WebP | ✓ |
| HEIC / HEIF | ✓ (Android 10+) |
| BMP | ✓ |
| GIF | ✓ (first frame) |

For anything `BitmapFactory` can't decode (e.g. multi-page TIFF, PDF), decode it to
a `Bitmap` yourself with the appropriate library and call `recognize(bitmap)`.
