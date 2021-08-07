package edu.unsa.danp3.util

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import java.io.IOException
import java.io.PrintWriter
import kotlin.math.sqrt

object AudioUtil {
    private val TAG = AudioUtil::class.simpleName

    /**
     * creates a media recorder, or throws a [IOException] if
     * the path is not valid.
     *
     * @param sdCardPath should contain a .3gp extension
     */
    fun prepareRecorder(sdCardPath: String): MediaRecorder {
        if (!isStorageReady)
            throw IOException("SD card is not available")
        val recorder = MediaRecorder()
        //set a custom listener that just logs any messages
        val recorderListener = RecorderErrorLoggerListener()
        recorder.setOnErrorListener(recorderListener)
        recorder.setOnInfoListener(recorderListener)
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        Log.d(TAG, "recording to: $sdCardPath")
        recorder.setOutputFile(sdCardPath)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder.prepare()
        return recorder
    }

    private val isStorageReady: Boolean
        get() {
            val cardStatus = Environment.getExternalStorageState()
            return if (cardStatus == Environment.MEDIA_REMOVED || cardStatus == Environment.MEDIA_UNMOUNTED || cardStatus == Environment.MEDIA_UNMOUNTABLE || cardStatus == Environment.MEDIA_MOUNTED_READ_ONLY) {
                false
            } else {
                cardStatus == Environment.MEDIA_MOUNTED
            }
        }

    fun hasMicrophone(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(
            PackageManager.FEATURE_MICROPHONE
        )
    }

    fun isSilence(data: ShortArray): Boolean {
        var silence = false
        val RMS_SILENCE_THRESHOLD = 2000
        if (rootMeanSquared(data) < RMS_SILENCE_THRESHOLD) {
            silence = true
        }
        return silence
    }

    fun rootMeanSquared(nums: ShortArray): Double {
        var ms = 0.0
        for (num in nums) {
            ms += (num * num).toDouble()
        }
        ms /= nums.size.toDouble()
        return sqrt(ms)
    }

    fun countZeros(audioData: ShortArray): Int {
        var numZeros = 0
        for (audioDatum in audioData) {
            if (audioDatum.toInt() == 0) {
                numZeros++
            }
        }
        return numZeros
    }

    fun secondsPerSample(sampleRate: Int): Double {
        return 1.0 / sampleRate.toDouble()
    }

    fun numSamplesInTime(sampleRate: Int, seconds: Float): Int {
        return (sampleRate.toFloat() * seconds).toInt()
    }

    fun outputData(data: ShortArray, writer: PrintWriter) {
        for (datum in data) {
            writer.println(datum.toString())
        }
        if (writer.checkError()) {
            Log.w(TAG, "Error writing sensor event data")
        }
    }
}
