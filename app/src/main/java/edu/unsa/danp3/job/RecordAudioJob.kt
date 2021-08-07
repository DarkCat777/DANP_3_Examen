package edu.unsa.danp3.job

import android.media.AudioFormat
import edu.unsa.danp3.record.AudioClipListener
import edu.unsa.danp3.record.AudioClipRecorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlin.coroutines.coroutineContext


object RecordAudioJob {
    suspend fun execute(audioClipListener: AudioClipListener): Boolean {
        val recorder = AudioClipRecorder(audioClipListener, coroutineContext.job)
        var heard = false
        for (i in 0..9) {
            try {
                heard = recorder.startRecordingForTime(
                    1000,
                    AudioClipRecorder.RECORDER_SAMPLERATE_8000,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                break
            } catch (ie: IllegalStateException) {
                // failed to setup, sleep and try again
                // if still can't set it up, just fail
                delay(100)
            }
        }
        //collect the audio
        return heard
    }
}