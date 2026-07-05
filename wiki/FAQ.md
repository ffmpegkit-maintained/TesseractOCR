# FAQ

## `recognize` returns empty text

- The image may be too low-resolution or low-contrast. See **[Performance](Performance)**.
- Wrong PSM: a full-page mode on a single word (or vice-versa) can yield nothing.
  Try `pageSegMode = 6` or `7`.
- Confirm the language is right for the text (`getVersion()` / `getAvailableLanguages()`).

## Low confidence / garbled output

- Upscale small text (glyphs ≥ 20 px), increase contrast, deskew.
- For numbers/codes, set `whitelistChars`.
- Try `tessdata_best` models (Pro) for hard inputs.

## `TesseractException.LanguageDataMissing`

The requested language's `*.traineddata` isn't on disk. English is bundled; for
others, download the file and call `addLanguage(context, code, file)` before
`initialize`. See **[Languages](Languages)**.

## `UnsatisfiedLinkError` / lib not found

Ensure your app includes the `arm64-v8a` ABI. The Free AAR is **arm64-v8a only** —
it will not load on an `x86_64` emulator. Use a physical arm64 device, or
**[Pro](https://www.jokobee.com/tesseract)** which adds x86_64.

## Does it work offline?

Yes. Everything is on-device. English works with zero network. Additional languages
just need their traineddata present (bundled or added once).

## Can I OCR a PDF?

Not directly — render the PDF page to a `Bitmap` (e.g. `PdfRenderer`) and pass that
to `recognize`.

## Is it thread-safe?

A single `TesseractOCR` instance is not thread-safe. Serialise calls, or use one
instance per worker thread.

## Why is my first call slow?

`initialize` extracts the bundled traineddata to storage and loads the LSTM model
into memory. Subsequent `recognize` calls are much faster. Keep the instance alive
and reuse it.

## License / commercial use?

The library is Apache 2.0 — fine for commercial apps. Tesseract (Apache 2.0),
Leptonica (BSD-2-Clause), and the bundled English model (Apache 2.0) permit
redistribution.

## How is Pro different?

12 bundled languages, `tessdata_best` models, OpenMP multi-threading, batch/advanced
API, and x86_64 support. See **[Pro Integration](Pro-Integration)**.
