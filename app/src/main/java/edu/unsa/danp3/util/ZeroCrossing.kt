package edu.unsa.danp3.util

object ZeroCrossing {
    /**
     * calculate frequency using zero crossings
     */
    fun calculate(sampleRate: Int, audioData: ShortArray): Int {
        val numSamples = audioData.size
        var numCrossing = 0
        for (p in 0 until numSamples - 1) {
            if (audioData[p] > 0 && audioData[p + 1] <= 0 ||
                audioData[p] < 0 && audioData[p + 1] >= 0
            ) {
                numCrossing++
            }
        }
        val numSecondsRecorded = numSamples.toFloat() / sampleRate.toFloat()
        val numCycles = (numCrossing / 2).toFloat()
        val frequency = numCycles / numSecondsRecorded
        return frequency.toInt()
    }
}