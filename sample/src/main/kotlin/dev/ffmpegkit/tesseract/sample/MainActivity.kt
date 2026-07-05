package dev.ffmpegkit.tesseract.sample

import android.graphics.BitmapFactory
import android.os.Bundle
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
            ocr.initialize(this@MainActivity, language = "eng")
            resultView.text = getString(R.string.ready, ocr.getVersion())
        }

        pickButton.setOnClickListener { pickImage.launch("image/*") }
    }

    override fun onDestroy() {
        ocr.release()
        super.onDestroy()
    }
}
