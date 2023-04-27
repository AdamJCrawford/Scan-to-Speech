package com.example.myscantospeech

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.text.Text


class Scan : AppCompatActivity() {
    private lateinit var text:String

    fun scan(image: InputImage){
        val recognizer = getTextRecognizer()
        // [END get_detector_default]

        // [START run_detector]
        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Want to save text and set speakBtn to enable
                text = visionText.text
                Log.e("Text Recogniton Success: ", text)
            }
            .addOnFailureListener { er ->
                // Task failed with an exception
                // ...
                Log.e( er.message, "Failed Text Recognition")
            }
    }


    private fun getTextRecognizer(): TextRecognizer {
        // [START ml-kit_local_doc_recognizer]
        return TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        // [END ml-kit_local_doc_recognizer]
    }
}