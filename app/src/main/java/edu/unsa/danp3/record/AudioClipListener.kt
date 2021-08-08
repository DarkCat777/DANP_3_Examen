
package edu.unsa.danp3.record

interface AudioClipListener {
    fun heard(audioData: ShortArray, sampleRate: Int): Boolean
}