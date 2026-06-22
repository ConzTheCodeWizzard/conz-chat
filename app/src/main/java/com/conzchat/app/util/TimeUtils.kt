package com.conzchat.app.util

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    fun formatKikTime(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val now = Calendar.getInstance()
        val msgCal = Calendar.getInstance().apply { timeInMillis = timestamp }

        val diffMs = now.timeInMillis - timestamp
        val diffDays = (diffMs / 86400000).toInt()

        val hours = msgCal.get(Calendar.HOUR_OF_DAY)
        val mins = msgCal.get(Calendar.MINUTE).toString().padStart(2, '0')
        val ampm = if (hours >= 12) "PM" else "AM"
        val h = if (hours % 12 == 0) 12 else hours % 12
        val timeStr = "$h:$mins $ampm"

        return when {
            diffDays == 0 -> timeStr
            diffDays == 1 -> "Yesterday $timeStr"
            diffDays < 7 -> {
                val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                "${days[msgCal.get(Calendar.DAY_OF_WEEK) - 1]} $timeStr"
            }
            else -> {
                val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                "${months[msgCal.get(Calendar.MONTH)]} ${msgCal.get(Calendar.DAY_OF_MONTH)} $timeStr"
            }
        }
    }

    fun formatRelativeTime(timestamp: Long): String {
        val diffMs = System.currentTimeMillis() - timestamp
        return when {
            diffMs < 60_000 -> "just now"
            diffMs < 3_600_000 -> "${diffMs / 60_000}m ago"
            diffMs < 86_400_000 -> "${diffMs / 3_600_000}h ago"
            else -> formatKikTime(timestamp)
        }
    }

    fun daysOnApp(createdAt: Long): Int {
        return ((System.currentTimeMillis() - createdAt) / 86400000).toInt()
    }
}
