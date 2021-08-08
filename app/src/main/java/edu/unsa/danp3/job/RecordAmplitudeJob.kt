package edu.unsa.danp3.job

import android.util.Log
import edu.unsa.danp3.record.AmplitudeClipListener
import edu.unsa.danp3.record.MaxAmplitudeRecorder
import kotlinx.coroutines.Job
import java.io.File
import java.io.IOException


object RecordAmplitudeJob {
    private val TAG by lazy { RecordAmplitudeJob::class.simpleName }

    const val TEMP_AUDIO_DIR_NAME = "temp_audio"
    // Run on Activity for pathStorage "context.getExternalFilesDir(TEMP_AUDIO_DIR_NAME).getAbsolutePath()"

    /**
     * time between amplitude checks
     */
    private const val CLIP_TIME = 1000L

    fun execute(pathStorage: String, amplitudeClipListener: AmplitudeClipListener, job: Job): Boolean {
        Log.d(TAG, "recording amplitude")
        val appStorageLocation: String = pathStorage + File.separator + "audio.3gp"
        val recorder = MaxAmplitudeRecorder(
            CLIP_TIME, appStorageLocation,
            amplitudeClipListener, job
        )
        val heard: Boolean = try {
            kotlin.runCatching {
                recorder.startRecording()
            }.getOrThrow()
        } catch (io: IOException) {
            Log.d(TAG, "failed to record", io)
            false
        } catch (se: IllegalStateException) {
            Log.d(TAG, "failed to record, recorder not setup properly", se)
            false
        } catch (re: RuntimeException) {
            Log.d(TAG, "failed to record, recorder already being used", re)
            false
        }
        return heard
    }
}