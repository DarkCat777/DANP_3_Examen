package edu.unsa.danp3.audio

import android.util.Log
import edu.unsa.danp3.record.AudioClipListener
import edu.unsa.danp3.util.AudioUtil.rootMeanSquared
import edu.unsa.danp3.util.ZeroCrossing.calculate
import java.util.*


/**
 * track a history of frequencies, and determine if a new frequency is within
 * the range of the ones in the history
 */
class ConsistentFrequencyDetector(
    historySize: Int, rangeThreshold: Int,
    silenceThreshold: Int
) : AudioClipListener {
    private val frequencyHistory: LinkedList<Int> = LinkedList()
    private val rangeThreshold: Int
    private val silenceThreshold: Int
    private val TAG by lazy {
        ConsistentFrequencyDetector::class.simpleName
    }

    companion object {
        const val DEFAULT_SILENCE_THRESHOLD = 2000
    }

    init {
        // pre-fill so modification is easy
        for (i in 0 until historySize) {
            frequencyHistory.add(Int.MAX_VALUE)
        }
        this.rangeThreshold = rangeThreshold
        this.silenceThreshold = silenceThreshold
    }

    override fun heard(audioData: ShortArray, sampleRate: Int): Boolean {
        val frequency = calculate(sampleRate, audioData)
        frequencyHistory.addFirst(frequency)
        // since history is always full, just remove the last
        frequencyHistory.removeLast()
        val range = calculateRange()
        var heard = false
        if (range < rangeThreshold) {
            // only trigger it isn't silence
            if (rootMeanSquared(audioData) > silenceThreshold) {
                Log.d(TAG, "heard")
                heard = true
            } else {
                Log.d(TAG, "not loud enough")
            }
        }
        return heard
    }

    private fun calculateRange(): Int {
        var min = Int.MAX_VALUE
        var max = Int.MIN_VALUE
        for (value in frequencyHistory) {
            if (value >= max)
                max = value
            if (value < min)
                min = value
        }
        return max - min
    }
}
