package dev.ffmpegkit.tesseract

import android.content.Context
import java.io.File
import java.io.IOException

/**
 * Copies the bundled `*.traineddata` files from `assets/tessdata` into app-private
 * storage so the native engine can mmap them from a real filesystem path.
 *
 * Tesseract expects a *parent* directory that contains a `tessdata/` folder;
 * [ensureDataPath] returns that parent.
 */
internal object TesseractInitializer {

    private const val TESSDATA_DIR = "tessdata"

    /**
     * Extract every `*.traineddata` shipped in `assets/tessdata/` (if not already
     * present) and return the directory that *contains* `tessdata/`.
     */
    fun ensureDataPath(context: Context): String {
        val baseDir = File(context.filesDir, "tesseract")
        val tessDir = File(baseDir, TESSDATA_DIR)
        if (!tessDir.exists() && !tessDir.mkdirs()) {
            throw TesseractException.InvalidImage("Cannot create tessdata dir: $tessDir")
        }
        try {
            val assets = context.assets
            val bundled = assets.list(TESSDATA_DIR)?.filter { it.endsWith(".traineddata") }.orEmpty()
            for (name in bundled) {
                val target = File(tessDir, name)
                if (target.exists() && target.length() > 0) continue
                // Extract to a temp file then atomically rename, so an interrupted
                // copy never leaves a truncated (length>0) file that would be skipped
                // on the next run and mmap'd as corrupt traineddata.
                val tmp = File(tessDir, "$name.tmp")
                assets.open("$TESSDATA_DIR/$name").use { input ->
                    tmp.outputStream().use { output -> input.copyTo(output) }
                }
                if (!tmp.renameTo(target)) {
                    tmp.delete()
                    throw TesseractException.LanguageDataMissing(name)
                }
            }
        } catch (e: IOException) {
            throw TesseractException.LanguageDataMissing("<bundled>", e)
        }
        // Tesseract 5.x Init(datapath) looks for `<datapath>/<lang>.traineddata`
        // directly — datapath must BE the tessdata dir, not its parent.
        return tessDir.absolutePath
    }

    /** Absolute path of a single traineddata file inside the extracted tessdata dir. */
    fun traineddataFile(context: Context, language: String): File =
        File(File(context.filesDir, "tesseract"), "$TESSDATA_DIR/$language.traineddata")

    /** Languages already available on disk (extracted or added at runtime). */
    fun availableLanguages(context: Context): List<String> {
        val tessDir = File(File(context.filesDir, "tesseract"), TESSDATA_DIR)
        return tessDir.listFiles { f -> f.name.endsWith(".traineddata") }
            ?.map { it.name.removeSuffix(".traineddata") }
            ?.sorted()
            .orEmpty()
    }
}
