package com.example.myscantospeech

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.myscantospeech.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var textRecognizer: TextRecognizer
    private lateinit var bitmap: Bitmap
    private lateinit var imageCapture: ImageCapture
    private lateinit var uri: Uri
    private lateinit var text:String

    private var scanBtn: Button? = null
    private var btnCapture: Button? = null
    private var tts: TextToSpeech? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        scanBtn = findViewById(R.id.Scan_Button)
        btnCapture = findViewById(R.id.open)

        tts = TextToSpeech(this, this)

        btnCapture!!.setOnClickListener { takePhoto() }

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )

        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Button listeners
        scanBtn = findViewById<android.widget.Button>(R.id.Scan_Button)

//        scanBtn!!.isEnabled=false
        scanBtn!!.setOnClickListener {
                if(!(::bitmap.isInitialized)){
                    Toast.makeText(this, "Select image first.", Toast.LENGTH_SHORT).show()
                } else {
                    scan(InputImage.fromBitmap(bitmap, 0))
                }
//            scan(image)
        }

    }

    //button menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menubar, menu)
        return true
    }

    //button menu buttons
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            R.id.settingsitem -> {
                openSettingsActivity()
                true
            }
            R.id.galleryitem -> {
                openGallery()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //open settings page
    fun openSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    //open photo gallery
    fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        //startActivity(intent, )
        startActivityForResult(intent, 10)
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language not supported!")
            } else {
            }
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build()
//            ImageCapture.Builder().build()
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }
    private fun takePhoto() {
        val name = SimpleDateFormat("MM-dd-yyyy", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/scantospeech")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    uri = output.getSavedUri()!!
                    bitmap =  MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    findViewById<ImageView>(R.id.img).setImageBitmap(bitmap)
                    scan(InputImage.fromBitmap(bitmap, 0))
                }
            }
        )
    }

    //    private fun startCamera() {
//        var cameraController = LifecycleCameraController(baseContext)
//        val previewView: PreviewView = viewBinding.viewFinder
//
//        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
//
//        cameraController.setImageAnalysisAnalyzer(
//            ContextCompat.getMainExecutor(this),
//            MlKitAnalyzer(
//                listOf(textRecognizer),
//                COORDINATE_SYSTEM_VIEW_REFERENCED,
//                ContextCompat.getMainExecutor(this)
//            ) { result: MlKitAnalyzer.Result? ->
//                val textRecognitionResults = result?.getValue(textRecognizer)
//                if ((textRecognitionResults == null) // ||
//                    // (textRecognitionResults.size == 0) ||
//                    // (textRecognitionResults.first() == null)
//                ) {
//                    previewView.overlay.clear()
//                    previewView.setOnTouchListener { _, _ -> false } //no-op
//                    return@MlKitAnalyzer
//                }
//
//                // val qrCodeViewModel = QrCodeViewModel(barcodeResults[0])
//                // val qrCodeDrawable = QrCodeDrawable(qrCodeViewModel)
//
//                // previewView.setOnTouchListener(qrCodeViewModel.qrCodeTouchCallback)
//                // previewView.overlay.clear()
//                // previewView.overlay.add(qrCodeDrawable)
//            }
//        )
//
//        cameraController.bindToLifecycle(this)
//        previewView.controller = cameraController
//    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        //textRecognizer.close() //this line crashes app when returning to main activity from settings
    }

    companion object {
        private const val TAG = "CameraX-MLKit"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).toTypedArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
// Display image selected from gallery
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if(requestCode == 10){
        if(data != null){
            uri = data.data!!
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            findViewById<ImageView>(R.id.img).setImageBitmap(bitmap)
            findViewById<ImageView>(R.id.img).rotation = 0.0F
            scan(InputImage.fromBitmap(bitmap, 0))

        }

    }
    super.onActivityResult(requestCode, resultCode, data)
}
//    bitmap = data?.extras!!["data"] as Bitmap

    fun scan(image: InputImage){
        val recognizer = getTextRecognizer()
        // [END get_detector_default]

        // [START run_detector]
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                text = visionText.text
                Toast.makeText(this, text, Toast.LENGTH_LONG).show()
                Log.i("Text Recogniton Success: ", text)
                tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
            }
            .addOnFailureListener { er ->
                // Task failed with an exception
                // ...
                Log.e( er.message, "Failed Text Recognition")
                Toast.makeText(this, "FAILED RECOGNITION", Toast.LENGTH_LONG).show() // placeholder
            }
    }


    private fun getTextRecognizer(): TextRecognizer {
        // [START ml-kit_local_doc_recognizer]
        return TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        // [END ml-kit_local_doc_recognizer]
    }
}