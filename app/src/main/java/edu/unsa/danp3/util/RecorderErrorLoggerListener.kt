package edu.unsa.danp3.util

import android.media.MediaRecorder
import android.util.Log

class RecorderErrorLoggerListener : MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener {
    private val TAG_LOG = RecorderErrorLoggerListener::class.java.name

    override fun onError(mr: MediaRecorder, what: Int, extra: Int) {
        Log.d(
            TAG_LOG,
            "error in media recorder detected: $what ex: $extra"
        )
        if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
            Log.d(TAG_LOG, "it was a media recorder error unknown")
        } else {
            Log.d(TAG_LOG, "unknown media error")
        }
    }

    override fun onInfo(mr: MediaRecorder, what: Int, extra: Int) {
        Log.d(
            TAG_LOG,
            "info in media recorder detected: $what ex: $extra"
        )
        when (what) {
            MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN -> {
                Log.d(TAG_LOG, "it was a MEDIA_INFO_UNKNOWN")
            }
            MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED -> {
                Log.d(TAG_LOG, "it was a MEDIA_RECORDER_INFO_MAX_DURATION_REACHED")
            }
            MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED -> {
                Log.d(TAG_LOG, "it was a MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED")
            }
            else -> {
                Log.d(TAG_LOG, "unknown info")
            }
        }
    }
}