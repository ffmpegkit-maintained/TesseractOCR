package dev.ffmpegkit.tesseract

/**
 * OCR options.
 *
 * @param pageSegMode  Page Segmentation Mode (PSM). 3 = PSM_AUTO (auto layout).
 *                     Use 6 for a single uniform block, 7 for a single text line,
 *                     10 for a single character.
 * @param ocrEngineMode OCR Engine Mode (OEM). 3 = OEM_DEFAULT (uses LSTM).
 *                     1 = OEM_LSTM_ONLY, 0 = legacy Tesseract only.
 * @param whitelistChars Restrict recognition to these characters (e.g. "0123456789").
 * @param preserveInterwordSpaces Keep multiple spaces between words.
 */
data class TesseractConfig(
    val pageSegMode: Int = 3,
    val ocrEngineMode: Int = 3,
    val whitelistChars: String = "",
    val preserveInterwordSpaces: Boolean = false,
)
