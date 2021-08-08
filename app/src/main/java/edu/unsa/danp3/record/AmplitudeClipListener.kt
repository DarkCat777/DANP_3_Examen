
package edu.unsa.danp3.record

interface AmplitudeClipListener {
    /**
     * return true if recording should stop
     */
    fun heard(maxAmplitude: Int): Boolean
}