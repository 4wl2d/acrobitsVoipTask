package com.tomilov.acrobitsvoip.ui

import java.util.Locale

object CallDurationFormatter {
    fun format(elapsedSeconds: Long): String {
        val safeSeconds = elapsedSeconds.coerceAtLeast(0)
        val hours = safeSeconds / 3_600
        val minutes = (safeSeconds % 3_600) / 60
        val seconds = safeSeconds % 60

        return if (hours > 0) {
            String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
        }
    }
}
