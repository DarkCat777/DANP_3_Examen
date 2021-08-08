package edu.unsa.danp3

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class Menu : AppCompatActivity() {
    companion object {
        const val SINGLE_CLAP_DETECTOR = 1
        const val LOUD_NOISE_DETECTOR = 2
        const val LOUD_NOISE_ABOVE_NORMAL_DETECTOR = 3
        const val CONSISTENT_FREQUENCY_DETECTOR = 4
        const val AUDIO_DETECTOR_EXTRA = "audio_processing"
    }

    private val TAG by lazy { Menu::class.simpleName }
    private lateinit var btnSingleClapDetector: CardView
    private lateinit var btnLoudNoiseDetector: CardView
    private lateinit var btnLoudNoiseDetectorAboveNormal: CardView
    private lateinit var btnConsistentFrequencyDetector: CardView

    private lateinit var permissionsRequest: ActivityResultLauncher<Array<out String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        // Incialización de los botones
        btnSingleClapDetector = findViewById(R.id.single_clap_detector)
        btnLoudNoiseDetector = findViewById(R.id.loud_noise_detector)
        btnLoudNoiseDetectorAboveNormal = findViewById(R.id.loud_noise_detector_above_normal)
        btnConsistentFrequencyDetector = findViewById(R.id.consistent_frequency_detector)
        // Consentimiento de permisos del usuario.
        permissionsRequest =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsGrantedMap ->
                Log.e(TAG, permissionsGrantedMap.toString())
                if (permissionsGrantedMap.values.contains(false)) {
                    // Se otorgaron los permisos necesarios
                    Toast.makeText(
                        this,
                        "No se concedieron los permisos suficientes para el funcionamiento del aplicativo.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Setteando control/acciones de los botones
                    onClickListener()
                }
            }
        permissionsRequest.launch(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA))

    }

    private fun onClickListener() {
        btnSingleClapDetector.setOnClickListener {
            val intent = Intent(this@Menu, MainActivity::class.java)
            intent.putExtra(AUDIO_DETECTOR_EXTRA, SINGLE_CLAP_DETECTOR)
            startActivity(intent)
        }
        btnLoudNoiseDetector.setOnClickListener {
            val intent = Intent(this@Menu, MainActivity::class.java)
            intent.putExtra(AUDIO_DETECTOR_EXTRA, LOUD_NOISE_DETECTOR)
            startActivity(intent)
        }
        btnLoudNoiseDetectorAboveNormal.setOnClickListener {
            val intent = Intent(this@Menu, MainActivity::class.java)
            intent.putExtra(AUDIO_DETECTOR_EXTRA, LOUD_NOISE_ABOVE_NORMAL_DETECTOR)
            startActivity(intent)
        }
        btnConsistentFrequencyDetector.setOnClickListener {
            val intent = Intent(this@Menu, MainActivity::class.java)
            intent.putExtra(AUDIO_DETECTOR_EXTRA, CONSISTENT_FREQUENCY_DETECTOR)
            startActivity(intent)
        }
    }
}