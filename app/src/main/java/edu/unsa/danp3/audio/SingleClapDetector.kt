
package edu.unsa.danp3.audio

import android.util.Log
import edu.unsa.danp3.record.AmplitudeClipListener

class SingleClapDetector(
    /**
     * required loudness to determine it is a clap
     */
    private val amplitudeThreshold: Int = DEFAULT_AMPLITUDE_DIFF
) : AmplitudeClipListener {

    companion object {
        /**
         * requires a little of noise by the user to trigger, background noise may
         * trigger it
         */
        const val AMPLITUDE_DIFF_LOW = 10000
        const val AMPLITUDE_DIFF_MED = 18000

        /**
         * requires a lot of noise by the user to trigger. background noise isn't
         * likely to be this loud
         */
        const val AMPLITUDE_DIFF_HIGH = 25000
        private const val DEFAULT_AMPLITUDE_DIFF = AMPLITUDE_DIFF_MED
    }

    private val TAG by lazy { SingleClapDetector::class.simpleName }


    override fun heard(maxAmplitude: Int): Boolean {
        var clapDetected = false
        if (maxAmplitude >= amplitudeThreshold) {
            Log.d(TAG, "heard a clap")
            clapDetected = true
        }
        return clapDetected
    }

}