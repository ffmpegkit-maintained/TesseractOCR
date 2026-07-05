package dev.ffmpegkit.tesseract

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File

/**
 * Main entry point for on-device OCR.
 *
 * Typical use:
 * ```
 * val ocr = TesseractOCR()
 * ocr.initialize(context, language = "eng")
 * val result = ocr.recognize(bitmap)
 * println(result.text)
 * ocr.release()
 * ```
 *
 * A single instance is **not** thread-safe: serialise calls, or use one instance
 * per worker. All heavy work runs on [Dispatchers.Default].
 */
class TesseractOCR {

    private var handle: Long = 0L
    private var config: TesseractConfig = TesseractConfig()

    /** True between a successful [initialize] and [release]. */
    val isInitialized: Boolean
        get() = handle != 0L

    /**
     * Extract bundled tessdata and start the engine.
     *
     * @param context  any Context (application context is retained internally-safe).
     * @param language one or more `+`-joined codes, e.g. "eng" or "eng+fra".
     * @param config   OCR options.
     * @throws TesseractException.LanguageDataMissing if a traineddata file is absent.
     * @throws TesseractException.InitFailed on native failure.
     */
    suspend fun initialize(
        context: Context,
        language: String = "eng",
        config: TesseractConfig = TesseractConfig(),
    ) = withContext(Dispatchers.Default) {
        if (isInitialized) release()
        val appContext = context.applicationContext
        val dataPath = TesseractInitializer.ensureDataPath(appContext)
        // Verify each requested language is present before hitting native code.
        for (code in language.split("+").filter { it.isNotBlank() }) {
            if (!TesseractInitializer.traineddataFile(appContext, code).exists()) {
                throw TesseractException.LanguageDataMissing(code)
            }
        }
        this@TesseractOCR.config = config
        val h = TesseractJNI.nativeInit(dataPath, language, config.ocrEngineMode)
        if (h == 0L) throw TesseractException.InitFailed(dataPath, language)
        TesseractJNI.nativeSetPageSegMode(h, config.pageSegMode)
        handle = h
    }

    /** Recognise text in [bitmap]. */
    suspend fun recognize(bitmap: Bitmap): TesseractResult = withContext(Dispatchers.Default) {
        val h = handle
        if (h == 0L) throw TesseractException.NotInitialized()
        val started = System.currentTimeMillis()
        val (pixels, bpp, bpl) = bitmap.toGrayscaleBytes()
        TesseractJNI.nativeSetImage(h, pixels, bitmap.width, bitmap.height, bpp, bpl)
        val text = TesseractJNI.nativeGetUTF8Text(h)
        val confidence = TesseractJNI.nativeGetMeanConfidence(h)
        val words = parseWords(TesseractJNI.nativeGetWords(h))
        TesseractResult(
            text = text.trim(),
            confidence = confidence,
            words = words,
            processingTimeMs = System.currentTimeMillis() - started,
        )
    }

    /** Decode an image file (PNG/JPEG/…) and recognise it. */
    suspend fun recognizeFile(path: String): TesseractResult = withContext(Dispatchers.Default) {
        val file = File(path)
        if (!file.exists()) throw TesseractException.InvalidImage("File not found: $path")
        val bitmap = BitmapFactory.decodeFile(path)
            ?: throw TesseractException.InvalidImage("Cannot decode image: $path")
        recognize(bitmap)
    }

    /**
     * Register an already-downloaded `*.traineddata` file so it can be used as a
     * language on the next [initialize]. Copies [file] into the tessdata dir.
     */
    suspend fun addLanguage(context: Context, language: String, file: File) =
        withContext(Dispatchers.Default) {
            if (!file.exists()) throw TesseractException.LanguageDataMissing(language)
            val target = TesseractInitializer.traineddataFile(context.applicationContext, language)
            target.parentFile?.mkdirs()
            file.copyTo(target, overwrite = true)
            Unit
        }

    /** Languages currently available on disk. */
    fun getAvailableLanguages(context: Context): List<String> =
        TesseractInitializer.availableLanguages(context.applicationContext)

    /** Native Tesseract version string, e.g. "5.5.0". */
    fun getVersion(): String = TesseractJNI.nativeGetVersion()

    /** Stop the engine and free native memory. Safe to call more than once. */
    fun release() {
        val h = handle
        if (h != 0L) {
            TesseractJNI.nativeEnd(h)
            handle = 0L
        }
    }

    // --- helpers -----------------------------------------------------------

    private fun parseWords(json: String): List<TesseractWord> {
        if (json.isBlank() || json == "[]") return emptyList()
        val array = JSONArray(json)
        val out = ArrayList<TesseractWord>(array.length())
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            out += TesseractWord(
                text = o.optString("text"),
                confidence = o.optInt("confidence"),
                boundingBox = Rect(
                    o.optInt("left"), o.optInt("top"), o.optInt("right"), o.optInt("bottom"),
                ),
            )
        }
        return out
    }

    /**
     * Convert a bitmap to single-channel grayscale bytes (bpp=1). Tesseract works
     * fine on 8-bit gray and it is 4× smaller than RGBA across the JNI boundary.
     * @return Triple(pixels, bytesPerPixel, bytesPerLine).
     */
    private fun Bitmap.toGrayscaleBytes(): Triple<ByteArray, Int, Int> {
        val w = width
        val h = height
        val argb = IntArray(w * h)
        getPixels(argb, 0, w, 0, 0, w, h)
        val gray = ByteArray(w * h)
        for (i in argb.indices) {
            val p = argb[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            // Rec. 601 luma.
            gray[i] = ((r * 299 + g * 587 + b * 114) / 1000).toByte()
        }
        return Triple(gray, 1, w)
    }
}
