package com.example.myscantospeech

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myscantospeech.databinding.ActivityMainBinding
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.view.MenuItem;
import android.graphics.Bitmap
import android.hardware.camera2.CameraDevice
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var textRecognizer: TextRecognizer
    private lateinit var bitmap: Bitmap
    private lateinit var imageCapture: ImageCapture
    private lateinit var uri: Uri

    private var btnSpeak: Button? = null
    private var btnCapture: Button? = null
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


        btnSpeak = findViewById(R.id.button2)
        btnCapture = findViewById(R.id.open)
        btnSpeak!!.isEnabled = false
        tts = TextToSpeech(this, this)

        btnSpeak!!.setOnClickListener { speakOut() }
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
        val scanBtn = findViewById<android.widget.Button>(R.id.Scan_Button)
        scanBtn.setOnClickListener {
            Toast.makeText(this, "Button has been pressed", Toast.LENGTH_SHORT).show()
        }

        val speakBtn = findViewById<android.widget.Button>(R.id.Speak_Button)
        speakBtn.setOnClickListener {
            Toast.makeText(this, "Button has been pressed", Toast.LENGTH_SHORT).show()
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
        startActivity(intent)
//        startActivityForResult(intent, 10)
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language not supported!")
            } else {
                btnSpeak!!.isEnabled = true
            }
        }
    }

    private fun speakOut() {
        val text = "Hello, this works."//etSpeak!!.text.toString()
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
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
        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat("MM-dd-yyyy", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
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
        textRecognizer.close()
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
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if(requestCode == 10){
//            if(data != null){
//                uri = data.data!!
//                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
//                findViewById<ImageView>(R.id.img).setImageBitmap(bitmap)
//
//            }
//
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }
//    bitmap = data?.extras!!["data"] as Bitmap

    // Handles when one of the icons is clicked

    fun galleryClicked(item: android.view.MenuItem) {
        Toast.makeText(this, "Button has been pressed", Toast.LENGTH_SHORT).show()
    }

    fun cameraClicked(item: android.view.MenuItem) {
        Toast.makeText(this, "Button has been pressed", Toast.LENGTH_SHORT).show()
    }

    fun settingsClicked(item: android.view.MenuItem) {
        Toast.makeText(this, "Button has been pressed", Toast.LENGTH_SHORT).show()
    }
}