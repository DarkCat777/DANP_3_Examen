/*
 * Copyright 2012 Greg Milette and Adam Stroud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.unsa.danp3.audio

import android.util.Log
import edu.unsa.danp3.record.AudioClipListener
import kotlin.math.sqrt

/**
 * alternative Loud Noise detector that tracks the difference between
 * the new noise and an averagre value. It might be useful in some situations.
 */
class LoudNoiseDetectorAboveNormal : AudioClipListener {

    private val TAG by lazy { LoudNoiseDetectorAboveNormal::class.simpleName }

    private var averageVolume: Double
    private val lowPassAlpha = 0.5
    private val STARTING_AVERAGE = 100.0
    private val INCREASE_FACTOR = 100.0

    init {
        averageVolume = STARTING_AVERAGE
    }

    override fun heard(audioData: ShortArray, sampleRate: Int): Boolean {
        var heard = false
        // use rms to take the entire audio signal into account
        // and discount anyone single high amplitude
        val currentVolume = rootMeanSquared(audioData)
        val volumeThreshold = averageVolume * INCREASE_FACTOR

        if (currentVolume > volumeThreshold) {
            Log.d(TAG, "heard")
            heard = true
        } else {
            // Big changes should have very little affect on
            // the average value but if the average volume does increase
            // consistently let the average increase too
            averageVolume = lowPass(currentVolume, averageVolume)
        }
        return heard
    }

    private fun lowPass(current: Double, last: Double): Double {
        return last * (1.0 - lowPassAlpha) + current * lowPassAlpha
    }

    private fun rootMeanSquared(nums: ShortArray): Double {
        var ms = 0.0
        for (num in nums)
            ms += (num * num).toDouble()

        ms /= nums.size.toDouble()
        return sqrt(ms)
    }

}