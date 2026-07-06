# Beginner Tutorial — From Zero to OCR

This walks you through adding on-device OCR to an Android app, step by step, with
copy-pasteable code. No prior Tesseract or NDK knowledge needed. English works out
of the box; other languages are one step away.

- [1. Add the dependency](#1-add-the-dependency)
- [2. Create and initialize the engine](#2-create-and-initialize-the-engine)
- [3. Run your first OCR](#3-run-your-first-ocr)
- [4. Read the result](#4-read-the-result)
- [5. Feeding images from any source](#5-feeding-images-from-any-source)
- [6. Clean up](#6-clean-up)
- [7. A complete, working example](#7-a-complete-working-example)
- [8. Common tweaks](#8-common-tweaks)
- [Next steps](#next-steps)

---

## 1. Add the dependency

```kotlin
// build.gradle.kts (module)
dependencies {
    implementation("dev.ffmpegkit-maintained:tesseract-android:5.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
```

Nothing else — `mavenCentral()` is already in a default Android project. No NDK, no
CMake, no traineddata to ship: the English model is **bundled inside the AAR**.

> Only need the arm64 ABI? This is the Free tier (arm64-v8a). For x86_64 emulator
> support and 12 bundled languages, see **[Pro Integration](Pro-Integration)**.

## 2. Create and initialize the engine

`TesseractOCR` is the single entry point. `initialize` extracts the bundled model
and starts the engine. Every heavy call is a `suspend` function, so call them from a
coroutine (e.g. `lifecycleScope`).

```kotlin
import dev.ffmpegkit.tesseract.TesseractOCR

class MainActivity : AppCompatActivity() {
    private val ocr = TesseractOCR()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            ocr.initialize(this@MainActivity, language = "eng")
            // engine is ready
        }
    }
}
```

`initialize` is safe to call once and reuse. Do **not** create a new `TesseractOCR`
per image — keep one instance alive.

## 3. Run your first OCR

Give `recognize` a `Bitmap`:

```kotlin
lifecycleScope.launch {
    val result = ocr.recognize(bitmap)
    println(result.text)
}
```

That's it. Under the hood the bitmap is converted to grayscale pixels and handed to
Tesseract — see **[How Image Decoding Works](Architecture-Image-Decoding)**.

## 4. Read the result

`recognize` returns a `TesseractResult`:

```kotlin
val result = ocr.recognize(bitmap)

result.text            // the full recognized text (String)
result.confidence      // mean confidence, 0–100 (Int)
result.processingTimeMs// how long recognition took (Long)

// Per-word detail with bounding boxes:
result.words.forEach { word ->
    println("${word.text}  ${word.confidence}%  ${word.boundingBox}")  // Rect in source pixels
}
```

Use `boundingBox` (an `android.graphics.Rect`) to draw overlays on the original image.

## 5. Feeding images from any source

Tesseract only cares about a `Bitmap`. Decode from wherever your image lives —
**JPEG, PNG, WebP, HEIC, and BMP all work**, because Android's `BitmapFactory` does
the decoding (see [Architecture](Architecture-Image-Decoding)).

### a) From a file path (JPEG/PNG/… on disk)

The library has a one-call helper:

```kotlin
val result = ocr.recognizeFile("/storage/emulated/0/Download/receipt.jpg")
```

Or decode it yourself if you want the bitmap too:

```kotlin
val bitmap = BitmapFactory.decodeFile(path)
val result = ocr.recognize(bitmap)
```

### b) From the gallery / a `content://` URI (photo picker)

```kotlin
private val pickImage =
    registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        lifecycleScope.launch {
            val bitmap = contentResolver.openInputStream(uri).use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return@launch
            val result = ocr.recognize(bitmap)
            println(result.text)
        }
    }

// launch it:
pickImage.launch("image/*")
```

### c) From the camera

If you captured a full-size photo to a file (via `MediaStore` / `FileProvider`):

```kotlin
val result = ocr.recognizeFile(photoFile.absolutePath)
```

For a thumbnail returned as a `Bitmap` in the intent extras:

```kotlin
val bitmap = intent.extras?.get("data") as? Bitmap ?: return
val result = ocr.recognize(bitmap)
```

### d) From bundled assets or a drawable resource

```kotlin
// assets/sample.png
val bitmap = assets.open("sample.png").use { BitmapFactory.decodeStream(it) }
val result = ocr.recognize(bitmap)

// res/drawable/sample.png
val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.sample)
val result2 = ocr.recognize(bitmap2)
```

### e) From the network (raw bytes)

Download the bytes with your HTTP client, then decode:

```kotlin
val bytes: ByteArray = httpClient.get(imageUrl)          // your networking
val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
val result = ocr.recognize(bitmap)
```

> **Big images?** Downscale before OCR — Tesseract is slow on 12 MP photos and
> doesn't need them. `BitmapFactory.Options(inSampleSize = 2)` halves each dimension.
> Aim for text glyphs ~20–40 px tall. See **[Performance](Performance)**.

## 6. Clean up

Release the native memory when you're done (e.g. `onDestroy` / `onCleared`):

```kotlin
override fun onDestroy() {
    ocr.release()
    super.onDestroy()
}
```

A released instance can be re-initialized later. `release` is safe to call twice.

## 7. A complete, working example

Pick an image, OCR it, show the text and confidence:

```kotlin
class MainActivity : AppCompatActivity() {

    private val ocr = TesseractOCR()
    private lateinit var resultView: TextView

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@registerForActivityResult
            lifecycleScope.launch {
                val bitmap = contentResolver.openInputStream(uri).use {
                    BitmapFactory.decodeStream(it)
                } ?: run { resultView.text = "Could not decode image"; return@launch }

                resultView.text = "Recognizing…"
                val result = ocr.recognize(bitmap)
                resultView.text = "（${result.confidence}%）\n${result.text}"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resultView = findViewById(R.id.resultView)

        lifecycleScope.launch {
            ocr.initialize(this@MainActivity, language = "eng")
            findViewById<Button>(R.id.pickButton).setOnClickListener {
                pickImage.launch("image/*")
            }
        }
    }

    override fun onDestroy() {
        ocr.release()
        super.onDestroy()
    }
}
```

## 8. Common tweaks

**A single line or word (receipts, labels, license plates)** — set the Page
Segmentation Mode so Tesseract doesn't hunt for page layout:

```kotlin
import dev.ffmpegkit.tesseract.TesseractConfig

ocr.initialize(context, "eng", TesseractConfig(pageSegMode = 7)) // 7 = single line
```

**Digits only** — restrict the character set to cut errors:

```kotlin
ocr.initialize(context, "eng", TesseractConfig(whitelistChars = "0123456789"))
```

**Another language** — download its `*.traineddata`, register it, then use it:

```kotlin
ocr.addLanguage(context, "fra", downloadedFraFile)  // once
ocr.initialize(context, language = "fra")           // or "eng+fra" for both
```

See **[Languages](Languages)** for where to get models and which quality to pick.

## Next steps

- **[API Reference](API-Reference)** — every method and type.
- **[Performance](Performance)** — image prep, PSM modes, benchmarking in release.
- **[Languages](Languages)** — adding languages, model quality.
- **[How Image Decoding Works](Architecture-Image-Decoding)** — why there are no
  image codecs in the AAR, and every format you can feed it.
- **[FAQ](FAQ)** — empty text, low confidence, missing languages.
