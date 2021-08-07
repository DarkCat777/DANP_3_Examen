package edu.unsa.danp3.audio

import android.util.Log
import edu.unsa.danp3.record.AudioClipListener
import kotlin.math.sqrt

class LoudNoiseDetector(
    private val volumeThreshold: Double = DEFAULT_LOUDNESS_THRESHOLD.toDouble()
) : AudioClipListener {

    private val TAG by lazy { LoudNoiseDetector::class.simpleName }

    companion object {
        const val DEFAULT_LOUDNESS_THRESHOLD = 2000
    }

    override fun heard(audioData: ShortArray, sampleRate: Int): Boolean {
        var heard = false
        // use rms to take the entire audio signal into account
        // and discount anyone single high amplitude
        val currentVolume = rootMeanSquared(audioData)
        if (currentVolume > volumeThreshold) {
            Log.d(TAG, "heard")
            heard = true
        }
        return heard
    }

    private fun rootMeanSquared(nums: ShortArray): Double {
        var ms = 0.0
        for (i in nums.indices) {
            ms += (nums[i] * nums[i]).toDouble()
        }
        ms /= nums.size.toDouble()
        return sqrt(ms)
    }
}