package edu.unsa.danp3.record

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder.AudioSource
import android.util.Log
import kotlinx.coroutines.Job

/**
 * record an audio clip and pass it to the listener
 */
class AudioClipRecorder(
    private val audioClipListener: AudioClipListener,
    private val job: Job
) {
    companion object {
        const val RECORDER_SAMPLERATE_CD = 44100
        const val RECORDER_SAMPLERATE_8000 = 8000
    }

    private val TAG by lazy { AudioClipRecorder::class.simpleName }
    private val DEFAULT_BUFFER_INCREASE_FACTOR = 3

    private var heard: Boolean = false
    private var continueRecording: Boolean = false
    private lateinit var audioRecord: AudioRecord

    fun startRecordingForTime(
        millisecondsPerAudioClip: Int,
        sampleRate: Int, encoding: Int
    ): Boolean {
        val percentOfASecond = millisecondsPerAudioClip.toFloat() / 1000.0f
        val numSamplesRequired = (sampleRate.toFloat() * percentOfASecond).toInt()
        val bufferSize: Int = determineCalculatedBufferSize(
            sampleRate, encoding,
            numSamplesRequired
        )
        return doRecording(
            sampleRate, encoding, bufferSize,
            numSamplesRequired, DEFAULT_BUFFER_INCREASE_FACTOR
        )
    }

    private fun doRecording(
        sampleRate: Int, encoding: Int,
        recordingBufferSize: Int, readBufferSize: Int,
        bufferIncreaseFactor: Int
    ): Boolean {
        if (recordingBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Bad encoding value, see logcat")
            return false
        } else if (recordingBufferSize == AudioRecord.ERROR) {
            Log.e(TAG, "Error creating buffer size")
            return false
        }

        // give it extra space to prevent overflow
        val increasedRecordingBufferSize = recordingBufferSize * bufferIncreaseFactor
        audioRecord = AudioRecord(
            AudioSource.MIC, sampleRate,
            AudioFormat.CHANNEL_IN_MONO, encoding,
            increasedRecordingBufferSize
        )
        val readBuffer = ShortArray(readBufferSize)
        continueRecording = true
        Log.d(
            TAG, "start recording, " + "recording bufferSize: "
                    + increasedRecordingBufferSize
                    + " read buffer size: " + readBufferSize
        )

        //Note: possible IllegalStateException
        //if audio recording is already recording or otherwise not available
        //AudioRecord.getState() will be AudioRecord.STATE_UNINITIALIZED
        audioRecord.startRecording()
        while (continueRecording) {
            val bufferResult: Int = audioRecord.read(readBuffer, 0, readBufferSize)
            //in case external code stopped this while read was happening
            if (!continueRecording || job.isCancelled) {
                break
            }
            // check for error conditions
            if (bufferResult == AudioRecord.ERROR_INVALID_OPERATION) {
                Log.e(TAG, "error reading: ERROR_INVALID_OPERATION")
            } else if (bufferResult == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "error reading: ERROR_BAD_VALUE")
            } else  // no errors, do processing
            {
                heard = audioClipListener.heard(readBuffer, sampleRate)
                if (heard) {
                    stopRecording()
                }
            }
        }
        done()
        return heard
    }

    private fun stopRecording() {
        continueRecording = false
    }

    fun done() {
        Log.d(TAG, "shut down audioRecord")
        audioRecord.stop()
        audioRecord.release()
    }

    private fun determineCalculatedBufferSize(sampleRate: Int, encoding: Int, numSamplesInBuffer: Int): Int {
        val minBufferSize: Int = determineMinimumBufferSize(sampleRate, encoding)
        var bufferSize: Int
        // each sample takes two bytes, need a bigger buffer
        bufferSize = if (encoding == AudioFormat.ENCODING_PCM_16BIT) {
            numSamplesInBuffer * 2
        } else {
            numSamplesInBuffer
        }
        if (bufferSize < minBufferSize) {
            Log.w(
                TAG, "Increasing buffer to hold enough samples "
                        + minBufferSize + " was: " + bufferSize
            )
            bufferSize = minBufferSize
        }
        return bufferSize
    }

    private fun determineMinimumBufferSize(sampleRate: Int, encoding: Int): Int {
        return AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            encoding
        )
    }
}