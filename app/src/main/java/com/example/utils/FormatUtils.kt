package com.example.utils

import android.text.format.DateUtils
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.util.Date

object FormatUtils {
    fun formatSize(bytes: Long): String {
        if (-1000 < bytes && bytes < 1000) {
            return "$bytes B"
        }
        val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
        var b = bytes
        while (b <= -999950 || b >= 999950) {
            b /= 1000
            ci.next()
        }
        return String.format("%.1f %cB", b / 1000.0, ci.current())
    }

    fun formatRelativeTime(timeInMillis: Long): String {
        val now = System.currentTimeMillis()
        return DateUtils.getRelativeTimeSpanString(
            timeInMillis,
            now,
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }
}
