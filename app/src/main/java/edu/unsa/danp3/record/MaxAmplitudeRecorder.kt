package edu.unsa.danp3.record

import android.media.MediaRecorder
import android.util.Log
import edu.unsa.danp3.util.AudioUtil.prepareRecorder
import edu.unsa.danp3.util.RecorderErrorLoggerListener
import kotlinx.coroutines.Job
import kotlin.properties.Delegates


class MaxAmplitudeRecorder(
    private val clipTime: Long,
    private val tmpAudioFile: String,
    private val clipListener: AmplitudeClipListener,
    private val job: Job
) {
    private val TAG by lazy { MaxAmplitudeRecorder::class.simpleName }

    private var continueRecording by Delegates.notNull<Boolean>()

    private lateinit var mediaRecorder: MediaRecorder

    fun startRecording(): Boolean {
        Log.e(TAG, "recording maxAmplitude")
        mediaRecorder = prepareRecorder(tmpAudioFile)

        // when an error occurs just stop recording
        mediaRecorder.setOnErrorListener { mr, what, extra ->
            // log it
            RecorderErrorLoggerListener().onError(mr, what, extra)
            // stop recording
            stopRecording()
        }

        //possible RuntimeException if Audio recording channel is occupied
        mediaRecorder.start()

        continueRecording = true
        var heard = false
        mediaRecorder.maxAmplitude
        while (continueRecording) {
            Log.e(TAG, "waiting while recording...")
            waitClipTime()
            Log.e(
                TAG,
                "continue recording: " + continueRecording + " cancelled after waiting? " + job.isCancelled
            )
            //in case external code stopped this while read was happening
            if (!continueRecording || job.isCancelled) {
                break
            }
            val maxAmplitude: Int = mediaRecorder.maxAmplitude
            Log.e(TAG, "current max amplitude: $maxAmplitude")
            heard = clipListener.heard(maxAmplitude)
            if (heard) {
                stopRecording()
            }
        }
        Log.e(TAG, "stopped recording max amplitude")
        done()
        return heard
    }


    private fun waitClipTime() {
        try {
            Thread.sleep(clipTime)
        } catch (e: InterruptedException) {
            Log.e(TAG, "interrupted")
        }
    }

    /**
     * stop recorder and clean up resources
     */
    fun done() {
        Log.e(TAG, "stop recording on done")
        try {
            mediaRecorder.stop()
        } catch (e: Exception) {
            Log.e(TAG, "failed to stop")
            return
        }
        mediaRecorder.release()
    }

    fun isRecording(): Boolean {
        return continueRecording
    }

    fun stopRecording() {
        continueRecording = false
    }
}