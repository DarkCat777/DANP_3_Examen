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
import edu.unsa.danp3.record.AmplitudeClipListener

/**
 * @author Greg Milette &#60;[gregorym@gmail.com](mailto:gregorym@gmail.com)&#62;
 */
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