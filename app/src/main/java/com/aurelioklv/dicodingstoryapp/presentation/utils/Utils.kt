package com.aurelioklv.dicodingstoryapp.presentation.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private const val TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
private const val WPM = 240

fun getTimeMillisFromString(dateTimeString: String): Long {
    val sdf = SimpleDateFormat(TIMESTAMP_PATTERN)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val date = sdf.parse(dateTimeString) as Date
    return date.time
}

fun getTimeAgo(timeMillis: Long): String {
    val now = System.currentTimeMillis()
    val diffMillis = now - timeMillis

    val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
    val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
    val weeks = days / 7
    val months = days / 30
    val years = days / 365

    return when {
        seconds < 60 -> "$seconds seconds ago"
        minutes < 60 -> "$minutes minutes ago"
        hours < 24 -> "$hours hours ago"
        days < 7 -> "$days days ago"
        weeks < 4 -> "$weeks weeks ago"
        months < 12 -> "$months months ago"
        else -> "$years years ago"
    }
}

fun getReadingTimeMinute(text: String): Int {
    val words = text.split(Regex("\\s+")).size
    val minute = words / WPM
    return if (minute == 0) 1 else minute
}

fun getFrontName(name: String): String {
    return name.substringBefore(" ")
}

