package dev.ffmpegkit.tesseract

import android.graphics.Rect

/**
 * Result of an OCR pass.
 *
 * @param text             Full recognised text.
 * @param confidence       Mean confidence, 0–100.
 * @param words            Words with per-word confidence and bounding box.
 * @param processingTimeMs Wall-clock recognition time, ms.
 */
data class TesseractResult(
    val text: String,
    val confidence: Int,
    val words: List<TesseractWord>,
    val processingTimeMs: Long,
)

/** A recognised word with its confidence and pixel bounding box. */
data class TesseractWord(
    val text: String,
    val confidence: Int,
    val boundingBox: Rect,
)
