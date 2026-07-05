package dev.ffmpegkit.tesseract

/** Base type for all Tesseract errors. */
sealed class TesseractException(message: String, cause: Throwable? = null) :
    Exception(message, cause) {

    /** tessdata could not be extracted or the requested language file is missing. */
    class LanguageDataMissing(language: String, cause: Throwable? = null) :
        TesseractException(
            "Missing tessdata for language '$language'. " +
                "Bundle '$language.traineddata' in assets/tessdata/ or call addLanguage().",
            cause,
        )

    /** Native TessBaseAPI.Init() returned non-zero. */
    class InitFailed(dataPath: String, language: String) :
        TesseractException("Tesseract Init failed (dataPath='$dataPath', language='$language').")

    /** An OCR call was made before initialize() or after release(). */
    class NotInitialized :
        TesseractException("TesseractOCR is not initialized. Call initialize() first.")

    /** The supplied image/bitmap could not be read. */
    class InvalidImage(message: String, cause: Throwable? = null) :
        TesseractException(message, cause)
}
