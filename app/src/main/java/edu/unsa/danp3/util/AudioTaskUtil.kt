package edu.unsa.danp3.util

import android.widget.TextView
import java.text.DateFormat
import java.util.*

object AudioTaskUtil {
    val now: String
        get() {
            val now = Calendar.getInstance()
            return DateFormat.getTimeInstance().format(now.time)
        }

    fun appendToStartOfLog(log: TextView, appendThis: String) {
        var currentLog = log.text.toString()
        currentLog = """
            $appendThis
            $currentLog
            """.trimIndent()
        log.text = currentLog
    }
}