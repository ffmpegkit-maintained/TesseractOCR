# Languages

## Bundled (Free)

The Free AAR ships **English** (`eng.traineddata`, from
[`tessdata_fast`](https://github.com/tesseract-ocr/tessdata_fast), ~4 MB) inside
`assets/tessdata/`. It is extracted to app-private storage on first
`initialize()` — nothing to download, works offline.

```kotlin
ocr.initialize(context, language = "eng")
```

## Adding a language at runtime

Download any `*.traineddata` (from `tessdata_fast`, `tessdata`, or `tessdata_best`),
then register it:

```kotlin
// e.g. after downloading fra.traineddata to a File
ocr.addLanguage(context, language = "fra", file = downloadedFile)
ocr.initialize(context, language = "fra")
```

`addLanguage` copies the file into the engine's tessdata dir. On the next
`initialize`, that code becomes usable.

## Multiple languages at once

Join codes with `+`:

```kotlin
ocr.initialize(context, language = "eng+fra")
```

Tesseract will recognise text in any of the listed languages. More languages =
slower init and slightly slower recognition.

## Which model quality?

| Pack | Size / lang | Speed | Accuracy |
|---|---|---|---|
| `tessdata_fast` | ~1–5 MB | Fastest | Good (Free default) |
| `tessdata` | ~10–15 MB | Medium | Better |
| `tessdata_best` | ~15–30 MB | Slowest | **Best** (Pro default) |

Full language list & downloads:
<https://github.com/tesseract-ocr/tessdata_fast>

## Checking what's installed

```kotlin
val langs = ocr.getAvailableLanguages(context)   // e.g. ["eng", "fra"]
```

## Pro

**[Tesseract Pro](https://www.jokobee.com/tesseract)** bundles **12 languages** using
`tessdata_best` (highest accuracy) — no runtime download needed.
