package dev.ffmpegkit.tesseract

/**
 * Internal bridge to the native libraries. Not part of the public API — use
 * [TesseractOCR]. Loads leptonica → tesseract → tesseract_jni (deps first).
 */
internal object TesseractJNI {

    init {
        System.loadLibrary("leptonica")
        System.loadLibrary("tesseract")
        System.loadLibrary("tesseract_jni")
    }

    /** @return native handle, or 0 on failure. dataPath = dir containing tessdata/. */
    external fun nativeInit(dataPath: String, language: String, oem: Int): Long

    external fun nativeSetPageSegMode(handle: Long, mode: Int)

    /** RGBA/RGB/gray bytes. bpp = bytes per pixel, bpl = bytes per line. */
    external fun nativeSetImage(handle: Long, pixels: ByteArray, width: Int, height: Int, bpp: Int, bpl: Int)

    external fun nativeGetUTF8Text(handle: Long): String

    external fun nativeGetMeanConfidence(handle: Long): Int

    /** @return JSON: `[{"text":..,"confidence":..,"left":..,"top":..,"right":..,"bottom":..}]`. */
    external fun nativeGetWords(handle: Long): String

    external fun nativeEnd(handle: Long)

    external fun nativeGetVersion(): String
}
