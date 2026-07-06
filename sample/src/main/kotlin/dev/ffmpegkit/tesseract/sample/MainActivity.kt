package dev.ffmpegkit.tesseract.sample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dev.ffmpegkit.tesseract.TesseractOCR
import kotlinx.coroutines.launch

/**
 * Minimal demo: pick an image, run OCR with the bundled English model, show the
 * recognised text and mean confidence.
 */
class MainActivity : AppCompatActivity() {

    private val ocr = TesseractOCR()

    private lateinit var imageView: ImageView
    private lateinit var resultView: TextView

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@registerForActivityResult
            lifecycleScope.launch {
                val bitmap = contentResolver.openInputStream(uri).use {
                    BitmapFactory.decodeStream(it)
                } ?: run {
                    resultView.text = getString(R.string.error_decode)
                    return@launch
                }
                imageView.setImageBitmap(bitmap)
                resultView.text = getString(R.string.recognizing)
                val result = ocr.recognize(bitmap)
                resultView.text = buildString {
                    append("Confidence: ${result.confidence}%  ")
                    append("(${result.processingTimeMs} ms, ${result.words.size} words)\n\n")
                    append(result.text)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        resultView = findViewById(R.id.resultView)
        val pickButton = findViewById<Button>(R.id.pickButton)

        lifecycleScope.launch {
            try {
                ocr.initialize(this@MainActivity, language = "eng")
                resultView.text = getString(R.string.ready, ocr.getVersion())
                runSelfTest()
            } catch (e: Exception) {
                Log.e("TessSelfTest", "init/self-test failed", e)
                resultView.text = "Init failed: ${e.message}"
            }
        }

        pickButton.setOnClickListener { pickImage.launch("image/*") }
    }

    /**
     * Automated smoke test (read via `adb logcat -s TessSelfTest`): render a known
     * string to a bitmap, OCR it, and log the recognised text + confidence. Proves
     * the full pipeline (init → native SetImage → recognize → text) works on-device.
     */
    private suspend fun runSelfTest() {
        val expected = "HELLO TESSERACT 2026"
        val bitmap = Bitmap.createBitmap(900, 200, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply {
            drawColor(Color.WHITE)
            drawText(expected, 30f, 130f, Paint().apply {
                color = Color.BLACK
                textSize = 80f
                isAntiAlias = true
            })
        }
        val result = ocr.recognize(bitmap)
        val got = result.text.trim()
        val ok = got.replace("\n", " ").contains("HELLO TESSERACT")
        Log.i("TessSelfTest", "version=${ocr.getVersion()} expected='$expected' " +
            "got='$got' confidence=${result.confidence}% ms=${result.processingTimeMs} " +
            "PASS=$ok")
    }

    override fun onDestroy() {
        ocr.release()
        super.onDestroy()
    }
}
