package edu.unsa.danp3

import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import edu.unsa.danp3.audio.ConsistentFrequencyDetector
import edu.unsa.danp3.audio.LoudNoiseDetector
import edu.unsa.danp3.audio.LoudNoiseDetectorAboveNormal
import edu.unsa.danp3.audio.SingleClapDetector
import edu.unsa.danp3.job.RecordAmplitudeJob
import edu.unsa.danp3.job.RecordAudioJob
import edu.unsa.danp3.util.AudioTaskUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null
    private lateinit var photoView: ImageView
    private lateinit var outputDirectory: File
    private lateinit var btnRecordAudioCapture: ImageButton
    private lateinit var viewFinder: PreviewView
    private lateinit var statusTV: TextView
    private var SELECTED_DETECTOR by Delegates.notNull<Int>()

    private lateinit var coroutineReference: Job

    companion object {
        private val TAG by lazy { MainActivity::class.simpleName }
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.camera)
        btnRecordAudioCapture = findViewById(R.id.record_audio_capture_button)
        viewFinder = findViewById(R.id.view_finder)
        photoView = findViewById(R.id.view_finder_result)
        statusTV = findViewById(R.id.status_record)
        SELECTED_DETECTOR = this.intent.extras!!.getInt(Menu.AUDIO_DETECTOR_EXTRA)
        initComponents()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initComponents() {
        // Tomar fotografia con el metodo [takePhoto()]
        outputDirectory = getOutputDirectory()
        // Inicializar camara
        startCamera()
        // Setteo de img al boton
        btnRecordAudioCapture.setBackgroundResource(R.drawable.ic_audio_message)
        // Setteo de operaciones al boton
        btnRecordAudioCapture.setOnClickListener {
            // Darle a stop si la corutina esta activa, no ha sido cancelada y no ha sido completada y ademas la variable ha sido inicializada
            if (!(this::coroutineReference.isInitialized &&
                        !coroutineReference.isCancelled &&
                        !coroutineReference.isCompleted) ||
                !this::coroutineReference.isInitialized
            ) {
                var result = false
                lifecycleScope.launch(Dispatchers.Main) {
                    // Inicializar el logger
                    val statusText = this@MainActivity.resources.getString(R.string.status) + "Grabando ..."
                    statusTV.text = statusText
                    btnRecordAudioCapture.setBackgroundResource(R.drawable.ic_stop)
                }
                when (SELECTED_DETECTOR) {
                    Menu.SINGLE_CLAP_DETECTOR -> {
                        // Do coroutine
                        coroutineReference = lifecycleScope.launch(Dispatchers.Default) {
                            result = RecordAmplitudeJob.execute(
                                this@MainActivity.getExternalFilesDir(RecordAmplitudeJob.TEMP_AUDIO_DIR_NAME)!!.absolutePath,
                                SingleClapDetector(),
                                this.coroutineContext.job
                            )
                        }
                    }
                    Menu.LOUD_NOISE_DETECTOR -> {
                        // Do coroutine
                        coroutineReference = lifecycleScope.launch(Dispatchers.Default) {
                            result = RecordAudioJob.execute(
                                LoudNoiseDetector(),
                                this.coroutineContext.job
                            )
                        }
                    }
                    Menu.LOUD_NOISE_ABOVE_NORMAL_DETECTOR -> {
                        // Do coroutine
                        coroutineReference = lifecycleScope.launch(Dispatchers.Default) {
                            result = RecordAudioJob.execute(
                                LoudNoiseDetectorAboveNormal(),
                                this.coroutineContext.job
                            )
                        }
                    }
                    Menu.CONSISTENT_FREQUENCY_DETECTOR -> {
                        // Do coroutine
                        coroutineReference = lifecycleScope.launch(Dispatchers.Default) {
                            result = RecordAudioJob.execute(
                                ConsistentFrequencyDetector(
                                    historySize = 3,
                                    rangeThreshold = 100,
                                    ConsistentFrequencyDetector.DEFAULT_SILENCE_THRESHOLD
                                ),
                                this.coroutineContext.job
                            )
                        }
                    }
                }
                coroutineReference.invokeOnCompletion {
                    lifecycleScope.launch(Dispatchers.Main) {
                        btnRecordAudioCapture.setBackgroundResource(R.drawable.ic_audio_message)
                        Log.e(TAG, "RESULT: $result")
                        if (result) {
                            takePhoto()
                            Toast.makeText(
                                this@MainActivity,
                                "Se detecto un sonido y se tomo una foto: " + AudioTaskUtil.now,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "No se detectaron sonidos y no se tomo una foto",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        val statusText =
                            this@MainActivity.resources.getString(R.string.status) + "Se detuvo la grabación."
                        statusTV.text = statusText
                    }
                }
            } else {
                this.coroutineReference.cancel()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name))
                .apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Preview
            val preview = Preview.Builder().build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().build()
            // Select back camera as a default

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                val cameraControl = camera.cameraControl
                viewFinder.setOnTouchListener(View.OnTouchListener setOnTouchListener@{ _: View, motionEvent: MotionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> return@setOnTouchListener true
                        MotionEvent.ACTION_UP -> {
                            // Get the MeteringPointFactory from PreviewView
                            val factory = viewFinder.meteringPointFactory

                            // Create a MeteringPoint from the tap coordinates
                            val point = factory.createPoint(motionEvent.x, motionEvent.y)

                            // Create a MeteringAction from the MeteringPoint, you can configure it to specify the metering mode
                            val action = FocusMeteringAction.Builder(point).build()

                            // Trigger the focus and metering. The method returns a ListenableFuture since the operation
                            // is asynchronous. You can use it get notified when the focus is successful or if it fails.
                            cameraControl.startFocusAndMetering(action)
                            Log.e(TAG, "Action Execute")
                            return@setOnTouchListener true
                        }
                        else -> return@setOnTouchListener false
                    }
                })
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return
        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )
        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    photoView.setImageBitmap(BitmapFactory.decodeFile(photoFile.absolutePath))
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }

}