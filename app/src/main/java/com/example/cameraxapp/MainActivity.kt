package com.example.cameraxapp

import android.Manifest
import android.content.ContentValues
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.cameraxapp.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    // instantiating variables
    private lateinit var viewBinding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // viewbinding initialization
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        // attempt at populating database with images but didnt get this working. not core though
        populateDatabaseWithPreMadeImages()

        //request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        // home button click listener
        viewBinding.homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        //set up listeners for capturing photo and video
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
        viewBinding.flashToggleButton.setOnClickListener { toggleFlash() }
        viewBinding.cameraSwitchButton.setOnClickListener { switchCamera() }
        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    // used from lab 1
    private fun takePhoto() {
        //get stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        //create time stamped name and MediaStore entry
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        //create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build()
        //set up image capture listener, triggered after photo is taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri.toString()
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

                    // Save the URI to the database
                    saveImageUriToDatabase(savedUri)
                }
            }
        )
    }

    // saving image URI's to database

    private fun saveImageUriToDatabase(savedUri: String) {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put("image_uri", savedUri)
        }
        db.insert("images", null, contentValues)
        db.close()

    }
    // taken from lab 1
    private fun startCamera(cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA) {
        // initialize camera provider
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // change cameraprovider from local to class member variable
            cameraProvider = cameraProviderFuture.get()

            try {

                cameraProvider?.unbindAll() // unbind all use cases first


                // set up preview
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                    }


                //build and assign imagecapture
                val imageCaptureBuilder = ImageCapture.Builder()
                imageCapture = imageCaptureBuilder.build()

                // set up recorder with highest quality
                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(
                            Quality.HIGHEST,
                            FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                        )
                    )
                    .build()

                videoCapture = VideoCapture.withOutput(recorder)


                // removed imageanalyzer
                cameraProvider?.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    videoCapture as VideoCapture<Recorder>
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // permission check


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    // starts camera once permissions granted
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }


    // toggles flash for camera
    private fun toggleFlash() {
        imageCapture?.let {
            val newFlashmode = when (it.flashMode) {
                ImageCapture.FLASH_MODE_ON -> {
                    viewBinding.flashToggleButton.text = "No flash"
                    viewBinding.flashToggleButton.setBackgroundColor(Color.GRAY)
                    ImageCapture.FLASH_MODE_OFF
                }

                else -> {
                    viewBinding.flashToggleButton.text = "Flash On"
                    viewBinding.flashToggleButton.setBackgroundColor(Color.BLUE)
                    ImageCapture.FLASH_MODE_ON
                }
            }
            it.flashMode = newFlashmode
            Log.d(
                TAG,
                "Flash mode changed to: ${if (newFlashmode == ImageCapture.FLASH_MODE_ON) "ON" else "OFF"}"
            )
        } ?: Log.e(TAG, "ImageCapture not initialized")
    }

    // lets you switch from front to back camera

    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCamera(CameraSelector.Builder().requireLensFacing(lensFacing).build())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                true
            }

            R.id.camera -> {
                if (allPermissionsGranted()) {
                    startCamera()
                } else {
                    ActivityCompat.requestPermissions(
                        this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                    )
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // function for saving the given images to internal storage
    private fun saveDrawableToInternalStorage(drawableId: Int): Uri? {
        val contextWrapper = ContextWrapper(applicationContext)
        val directory = contextWrapper.filesDir
        val file = File(directory, "image${drawableId}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            val bitmap = BitmapFactory.decodeResource(resources, drawableId)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return Uri.parse(file.absolutePath)
    }

    // function to populate the database with the above given images

    private fun populateDatabaseWithPreMadeImages() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM images", null)
        if (cursor != null && cursor.moveToFirst() && cursor.getInt(0) > 0) {
            cursor.close()
            return
        }
        cursor.close()
        val images = listOf(R.drawable.example_image_1, R.drawable.example_image_2, R.drawable.example_image_3, R.drawable.example_image_4)

        images.forEach { imageResId ->
            val imageUri = saveDrawableToInternalStorage(imageResId)
            if (imageUri != null) {
                val contentValues = ContentValues().apply {
                    put("image_uri", imageUri.toString())
                }
                db.insert("images", null, contentValues)
            }
        }
        db.close()
    }

}