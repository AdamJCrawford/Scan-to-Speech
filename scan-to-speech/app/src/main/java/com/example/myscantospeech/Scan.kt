package com.example.myscantospeech

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.text.Text

class Scan : AppCompatActivity() {
    fun scan(image: InputImage){
        val recognizer = getTextRecognizer()
        // [END get_detector_default]

        // [START run_detector]
        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Want to save text and set speakBtn to enable
                val text = visionText.text
                Log.e("Text Recognition Success: ", text)
            }
            .addOnFailureListener { er ->
                // Task failed with an exception
                // ...
                Log.e( er.message, "Failed Text Recognition")
            }
    }

    private fun processTextBlock(result: Text) {
        // [START ml-kit_process_text_block]
        val resultText = result.text
        for (block in result.textBlocks) {
            val blockText = block.text
            val blockCornerPoints = block.cornerPoints
            val blockFrame = block.boundingBox
            for (line in block.lines) {
                val lineText = line.text
                val lineCornerPoints = line.cornerPoints
                val lineFrame = line.boundingBox
                for (element in line.elements) {
                    val elementText = element.text
                    val elementCornerPoints = element.cornerPoints
                    val elementFrame = element.boundingBox
                }
            }
        }
        // [END ml-kit_process_text_block]
    }

    private fun getTextRecognizer(): TextRecognizer {
        // [START ml-kit_local_doc_recognizer]
        return TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        // [END ml-kit_local_doc_recognizer]
    }
}